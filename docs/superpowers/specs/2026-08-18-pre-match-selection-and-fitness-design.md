# Pre-Match Selection & Player Condition (Fitness & Morale) Design Spec

_Date: 2026-08-18 · Status: Approved_

## 1. Overview & Goals

Currently, the football manager simulation calculates club team strength by averaging all players across broad positional groups in the squad, ignoring `Player.fitness` and `Player.morale`, and without starting XI selection.

This design introduces:
1. **Tactical Formation Slots**: Explicit 11-player positional slot mappings for each formation (`4-4-2`, `4-3-3`, `5-3-2`).
2. **Pre-Match Selection & Lineup**: A `Lineup` data model representing starting 11 players and substitutes, with an intelligent `Lineup.autoSelect` fallback for AI clubs and unconfigured managers.
3. **Player Condition Scaling**: Dynamic `Player.effectiveOverall(position)` calculations incorporating fitness and morale.
4. **Post-Match Condition Lifecycle**: Matchday stamina drain for starters, recovery for rested players, and morale shifts driven by match outcomes in `SeasonRunner`.
5. **State Management & Persistence**: Resumable and serializable tracking of player condition and lineups in `SeasonState` and `Game`.

---

## 2. Domain & Tactical Model

### 2.1 Formation Slots (`simulation/Tactics.kt`)
Each `Formation` specifies an ordered list of 11 `Position` slots:
* **`FOUR_FOUR_TWO`**: `[GK, LB, CB, CB, RB, LM, CM, CM, RM, ST, ST]`
* **`FOUR_THREE_THREE`**: `[GK, LB, CB, CB, RB, CDM, CM, CAM, LW, ST, RW]`
* **`FIVE_THREE_TWO`**: `[GK, LWB, CB, CB, CB, RWB, CM, CM, CM, ST, ST]`

```kotlin
enum class Formation(val label: String, val attackModifier: Double, val defenseModifier: Double) {
    FOUR_FOUR_TWO("4-4-2", 1.00, 1.00),
    FOUR_THREE_THREE("4-3-3", 1.10, 0.90),
    FIVE_THREE_TWO("5-3-2", 0.90, 1.10);

    val slots: List<Position> get() = when (this) {
        FOUR_FOUR_TWO -> listOf(
            Position.GK,
            Position.LB, Position.CB, Position.CB, Position.RB,
            Position.LM, Position.CM, Position.CM, Position.RM,
            Position.ST, Position.ST,
        )
        FOUR_THREE_THREE -> listOf(
            Position.GK,
            Position.LB, Position.CB, Position.CB, Position.RB,
            Position.CDM, Position.CM, Position.CAM,
            Position.LW, Position.ST, Position.RW,
        )
        FIVE_THREE_TWO -> listOf(
            Position.GK,
            Position.LWB, Position.CB, Position.CB, Position.CB, Position.RWB,
            Position.CM, Position.CM, Position.CM,
            Position.ST, Position.ST,
        )
    }
}
```

### 2.2 Lineup Model & Auto-Selection (`simulation/Lineup.kt`)
A `Lineup` specifies the starting 11 player IDs and bench players:
```kotlin
@Serializable
data class Lineup(
    val starters: List<Long>,
    val substitutes: List<Long> = emptyList(),
) {
    init {
        require(starters.size == 11) { "Lineup must have exactly 11 starters" }
        require(starters.distinct().size == 11) { "Starters must be unique players" }
    }

    companion object {
        /**
         * Selects the highest rated available player for each formation slot,
         * taking effective rating (attributes + fitness + morale) into account.
         */
        fun autoSelect(players: List<Player>, tactics: Tactics): Lineup {
            require(players.size >= 11) { "Squad must have at least 11 players" }
            val available = players.toMutableList()
            val selectedStarters = mutableListOf<Long>()

            for (slot in tactics.formation.slots) {
                val best = available.maxByOrNull { it.effectiveOverall(slot) }
                    ?: error("No available player for slot $slot")
                selectedStarters.add(best.id)
                available.remove(best)
            }

            val bench = available.sortedByDescending { it.bestOverall() }.map { it.id }
            return Lineup(starters = selectedStarters, substitutes = bench)
        }
    }
}
```

### 2.3 Player Condition Scaling (`model/Player.kt`)
`Player` computes its match rating dynamically based on assigned position, current `fitness` (1–100), and `morale` (1–100):

* **Fitness factor**: `0.70 + 0.30 * (fitness / 100.0)` $\in [0.70, 1.00]$
* **Morale factor**: `0.90 + 0.20 * (morale / 100.0)` $\in [0.90, 1.10]$

```kotlin
fun effectiveOverall(position: Position): Int {
    val base = overall(position)
    val fitnessFactor = 0.70 + 0.30 * (fitness / 100.0)
    val moraleFactor = 0.90 + 0.20 * (morale / 100.0)
    return (base * fitnessFactor * moraleFactor).roundToInt().coerceIn(MIN_ATTRIBUTE, MAX_ATTRIBUTE)
}
```

---

## 3. Simulation & Team Strength Calculation (`simulation/Team.kt`)

`Team` is derived from the starting 11 players in their formation slots:

```kotlin
fun fromLineup(clubId: Long, starters: List<Player>, tactics: Tactics = Tactics()): Team {
    require(starters.size == 11) { "Starting XI must have exactly 11 players" }
    val slots = tactics.formation.slots
    val assigned = starters.zip(slots)

    fun groupRatings(group: PositionGroup): List<Int> =
        assigned.filter { (_, slot) -> slot.group == group }
            .map { (player, slot) -> player.effectiveOverall(slot) }

    fun average(values: List<Int>): Double =
        if (values.isEmpty()) MID_RATING.toDouble() else values.average()

    val attack = (0.7 * average(groupRatings(PositionGroup.ATTACKER)) +
        0.3 * average(groupRatings(PositionGroup.MIDFIELDER)))
        .roundToInt().coerceIn(MIN_ATTRIBUTE, MAX_ATTRIBUTE)

    val defense = (0.6 * average(groupRatings(PositionGroup.DEFENDER)) +
        0.2 * average(groupRatings(PositionGroup.GOALKEEPER)) +
        0.2 * average(groupRatings(PositionGroup.MIDFIELDER)))
        .roundToInt().coerceIn(MIN_ATTRIBUTE, MAX_ATTRIBUTE)

    return Team(clubId, attack, defense, tactics)
}

fun fromSquad(clubId: Long, players: List<Player>, tactics: Tactics = Tactics()): Team {
    val lineup = Lineup.autoSelect(players, tactics)
    val playerMap = players.associateBy { it.id }
    val starters = lineup.starters.map { playerMap.getValue(it) }
    return fromLineup(clubId, starters, tactics)
}
```

---

## 4. Season Simulation & Matchday Lifecycle

### 4.1 `SeasonState` (`simulation/season/SeasonState.kt`)
Stores the dynamic player condition and active custom lineups:
```kotlin
@Serializable
data class SeasonState(
    val league: League,
    val teams: List<Team>,
    val fixtures: List<Fixture>,
    val nextFixtureIndex: Int = 0,
    val results: List<MatchResult> = emptyList(),
    val standings: Standings,
    @Serializable(with = LocalDateSerializer::class)
    val currentDate: LocalDate,
    val humanClubId: Long? = null,
    val players: Map<Long, Player> = emptyMap(),
    val lineups: Map<Long, Lineup> = emptyMap(),
) {
    fun setLineup(clubId: Long, lineup: Lineup): SeasonState =
        copy(lineups = lineups + (clubId to lineup))

    fun setTactics(clubId: Long, tactics: Tactics): SeasonState {
        val updated = teams.firstOrNull { it.clubId == clubId }?.copy(tactics = tactics) ?: return this
        return copy(
            teams = teams.map { if (it.clubId == clubId) updated else it },
            standings = standings.copy(
                entries = standings.entries.map {
                    if (it.team.clubId == clubId) it.copy(team = updated) else it
                },
            ),
        )
    }
}
```

### 4.2 Matchday Execution & Condition Evolution (`simulation/season/SeasonRunner.kt`)
1. **Team Generation**:
   For each fixture in the matchday:
   - Resolve home and away starting 11 from `state.lineups[clubId]` or `Lineup.autoSelect(clubPlayers, clubTactics)`.
   - Build matchday `Team` instances using `Team.fromLineup(...)`.
2. **Match Engine**:
   Execute `engine.simulate(homeTeam, awayTeam)`.
3. **Player Condition Lifecycle**:
   For every club playing in this matchday:
   * **Fitness**:
     * Starters: $-12$ fitness $\rightarrow$ `(fitness - 12).coerceIn(1, 100)`.
     * Rested / Substitutes: $+18$ fitness $\rightarrow$ `(fitness + 18).coerceIn(1, 100)`.
   * **Morale**:
     * Win: $+8$ for starters, $+4$ for rested squad players.
     * Draw: Drifts 1 point towards neutral ($50$).
     * Loss: $-6$ for starters, $-3$ for rested squad players.
4. **Advance State**:
   Return new `SeasonState` containing updated `players`, new `results`, advanced `standings`, and incremented `nextFixtureIndex`.

---

## 5. Persistence & CLI Demo Integration

* `GameSerializationTest` and `Game.saveToFile` / `loadFromFile` will serialize `SeasonState.players` and `SeasonState.lineups`.
* `Main.kt` demo demonstrates:
  - Setting tactical plans and starting lineups.
  - Observing fitness depletion on starters across consecutive matches and tactical squad rotation benefits.
  - Mid-season saving and resuming with identical player condition values.

---

## 6. Testing Strategy

1. **`LineupTest`**:
   - `Lineup` validation (must have 11 unique starters).
   - `Lineup.autoSelect` accurately assigns highest rated players to matching formation slots.
2. **`PlayerConditionTest`**:
   - `effectiveOverall` scales downwards with low fitness and increases with high morale.
3. **`TeamLineupTest`**:
   - `Team.fromLineup` computes attack/defense from the 11 starting players.
   - Playing tired players results in lower team attack/defense ratings.
4. **`SeasonRunnerConditionTest`**:
   - Matchday simulation reduces fitness for starters and recovers fitness for rested players.
   - Match wins/losses adjust squad morale appropriately.
   - `setLineup` preserves user-chosen players for the next matchday.
5. **`GameSerializationTest`**:
   - Verify `SeasonState` with `players` and `lineups` round-trips without data loss.
