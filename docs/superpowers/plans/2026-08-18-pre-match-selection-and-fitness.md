# Pre-Match Selection & Player Condition Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement starting XI lineup selection, formation slots, player fitness and morale condition scaling, matchday condition lifecycle, and state persistence in the pure Kotlin Football Manager engine.

**Architecture:** Add `Formation.slots`, `Lineup`, and `Player.effectiveOverall()`. Update `Team.fromLineup` and `Team.fromSquad` to calculate team power from starting 11 players. Expand `SeasonState` and `SeasonRunner` to simulate matches with dynamic starting lineups and update post-match player condition (fitness drain/recovery, morale shifts), fully integrated into JSON serialization and CLI demo.

**Tech Stack:** Kotlin 2.4.10, Kotlinx Serialization JSON 1.11.0, JUnit 5, GitHub Actions CI via `gh`.

## Global Constraints
- Pure Kotlin JVM, no UI toolkit dependencies.
- No local Gradle/JDK build; validation runs via GitHub Actions CI (`git push` and `gh run watch`).
- All ratings and attributes remain on a 1..100 integer scale.
- Starters size is strictly 11 unique players matching `tactics.formation.slots`.
- Backward compatible: AI and default teams automatically select the best XI without breaking existing fixtures or callers.

---

### Task 1: Tactical Formation Slots & Player Effective Condition

**Files:**
- Modify: `engine/src/main/kotlin/com/footballmanager/simulation/Tactics.kt`
- Modify: `engine/src/main/kotlin/com/footballmanager/model/Player.kt`
- Create: `engine/src/test/kotlin/com/footballmanager/model/PlayerConditionTest.kt`
- Modify: `engine/src/test/kotlin/com/footballmanager/simulation/TacticsTest.kt`

**Interfaces:**
- Consumes: `Position`, `Player`, `Formation`, `PlayerAttributes`
- Produces:
  - `Formation.slots: List<Position>` (11 positions per formation)
  - `Player.effectiveOverall(position: Position): Int`

- [ ] **Step 1: Write tests for `Formation.slots` and `Player.effectiveOverall`**

Create `engine/src/test/kotlin/com/footballmanager/model/PlayerConditionTest.kt`:
```kotlin
package com.footballmanager.model

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlayerConditionTest {

    private fun testPlayer(fitness: Int = 100, morale: Int = 50): Player {
        val attributes = PlayerAttributes(Attribute.entries.associateWith { 80 })
        return Player(
            id = 1L,
            name = "Test Player",
            age = 25,
            nationality = "ID",
            naturalPositions = listOf(Position.ST),
            attributes = attributes,
            contract = Contract(expiresOn = LocalDate.of(2030, 6, 30)),
            fitness = fitness,
            morale = morale,
        )
    }

    @Test
    fun `effectiveOverall equals base overall at 100 fitness and 50 morale`() {
        val player = testPlayer(fitness = 100, morale = 50)
        assertEquals(player.overall(Position.ST), player.effectiveOverall(Position.ST))
    }

    @Test
    fun `effectiveOverall drops with lower fitness`() {
        val fullFit = testPlayer(fitness = 100, morale = 50)
        val tired = testPlayer(fitness = 50, morale = 50)
        val exhausted = testPlayer(fitness = 0, morale = 50)

        assertTrue(tired.effectiveOverall(Position.ST) < fullFit.effectiveOverall(Position.ST))
        assertTrue(exhausted.effectiveOverall(Position.ST) < tired.effectiveOverall(Position.ST))
        assertEquals(56, exhausted.effectiveOverall(Position.ST)) // 80 * 0.70 = 56
    }

    @Test
    fun `effectiveOverall increases with high morale and drops with low morale`() {
        val highMorale = testPlayer(fitness = 100, morale = 100)
        val lowMorale = testPlayer(fitness = 100, morale = 0)

        assertEquals(88, highMorale.effectiveOverall(Position.ST)) // 80 * 1.0 * 1.10 = 88
        assertEquals(72, lowMorale.effectiveOverall(Position.ST))  // 80 * 1.0 * 0.90 = 72
    }
}
```

Add slot tests to `engine/src/test/kotlin/com/footballmanager/simulation/TacticsTest.kt`:
```kotlin
    @Test
    fun `each formation has exactly 11 positional slots`() {
        for (formation in Formation.entries) {
            assertEquals(11, formation.slots.size, "formation $formation must have 11 slots")
            assertEquals(Position.GK, formation.slots.first(), "first slot must be GK")
        }
    }
```

- [ ] **Step 2: Implement `Formation.slots` and `Player.effectiveOverall`**

In `engine/src/main/kotlin/com/footballmanager/simulation/Tactics.kt`:
```kotlin
enum class Formation(val label: String, val attackModifier: Double, val defenseModifier: Double) {
    FOUR_FOUR_TWO("4-4-2", 1.00, 1.00),
    FOUR_THREE_THREE("4-3-3", 1.10, 0.90),
    FIVE_THREE_TWO("5-3-2", 0.90, 1.10);

    val slots: List<com.footballmanager.model.Position> get() = when (this) {
        FOUR_FOUR_TWO -> listOf(
            com.footballmanager.model.Position.GK,
            com.footballmanager.model.Position.LB,
            com.footballmanager.model.Position.CB,
            com.footballmanager.model.Position.CB,
            com.footballmanager.model.Position.RB,
            com.footballmanager.model.Position.LM,
            com.footballmanager.model.Position.CM,
            com.footballmanager.model.Position.CM,
            com.footballmanager.model.Position.RM,
            com.footballmanager.model.Position.ST,
            com.footballmanager.model.Position.ST,
        )
        FOUR_THREE_THREE -> listOf(
            com.footballmanager.model.Position.GK,
            com.footballmanager.model.Position.LB,
            com.footballmanager.model.Position.CB,
            com.footballmanager.model.Position.CB,
            com.footballmanager.model.Position.RB,
            com.footballmanager.model.Position.CDM,
            com.footballmanager.model.Position.CM,
            com.footballmanager.model.Position.CAM,
            com.footballmanager.model.Position.LW,
            com.footballmanager.model.Position.ST,
            com.footballmanager.model.Position.RW,
        )
        FIVE_THREE_TWO -> listOf(
            com.footballmanager.model.Position.GK,
            com.footballmanager.model.Position.LWB,
            com.footballmanager.model.Position.CB,
            com.footballmanager.model.Position.CB,
            com.footballmanager.model.Position.CB,
            com.footballmanager.model.Position.RWB,
            com.footballmanager.model.Position.CM,
            com.footballmanager.model.Position.CM,
            com.footballmanager.model.Position.CM,
            com.footballmanager.model.Position.ST,
            com.footballmanager.model.Position.ST,
        )
    }
}
```

In `engine/src/main/kotlin/com/footballmanager/model/Player.kt`:
```kotlin
    /** Overall rating when played in [position], modified by fitness and morale. */
    fun effectiveOverall(position: Position): Int {
        val base = overall(position)
        val fitnessFactor = 0.70 + 0.30 * (fitness.coerceIn(0, 100) / 100.0)
        val moraleFactor = 0.90 + 0.20 * (morale.coerceIn(0, 100) / 100.0)
        return (base * fitnessFactor * moraleFactor).roundToInt().coerceIn(MIN_ATTRIBUTE, MAX_ATTRIBUTE)
    }
```

- [ ] **Step 3: Commit Task 1 changes**

```bash
git add engine/src/main/kotlin/com/footballmanager/simulation/Tactics.kt \
        engine/src/main/kotlin/com/footballmanager/model/Player.kt \
        engine/src/test/kotlin/com/footballmanager/model/PlayerConditionTest.kt \
        engine/src/test/kotlin/com/footballmanager/simulation/TacticsTest.kt
git commit -m "feat: add formation positional slots and player effectiveOverall"
```

---

### Task 2: Lineup Model & Auto-Selection

**Files:**
- Create: `engine/src/main/kotlin/com/footballmanager/simulation/Lineup.kt`
- Create: `engine/src/test/kotlin/com/footballmanager/simulation/LineupTest.kt`

**Interfaces:**
- Consumes: `Player`, `Tactics`, `Formation.slots`
- Produces:
  - `Lineup(val starters: List<Long>, val substitutes: List<Long>)`
  - `Lineup.autoSelect(players: List<Player>, tactics: Tactics): Lineup`

- [ ] **Step 1: Write `LineupTest`**

Create `engine/src/test/kotlin/com/footballmanager/simulation/LineupTest.kt`:
```kotlin
package com.footballmanager.simulation

import com.footballmanager.seed.SeedData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class LineupTest {

    @Test
    fun `lineup requires exactly 11 unique starters`() {
        assertFailsWith<IllegalArgumentException> {
            Lineup(starters = (1L..10L).toList())
        }
        assertFailsWith<IllegalArgumentException> {
            Lineup(starters = listOf(1L, 1L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L))
        }
    }

    @Test
    fun `autoSelect assigns 11 unique starters and puts rest on bench`() {
        val game = SeedData.game()
        val squad = game.squad(1L)
        val tactics = Tactics(Formation.FOUR_THREE_THREE, Mentality.ATTACKING)

        val lineup = Lineup.autoSelect(squad, tactics)

        assertEquals(11, lineup.starters.size)
        assertEquals(11, lineup.starters.distinct().size)
        assertEquals(7, lineup.substitutes.size)
        assertEquals(squad.size, (lineup.starters + lineup.substitutes).distinct().size)
    }

    @Test
    fun `autoSelect picks goalkeeper for the GK slot`() {
        val game = SeedData.game()
        val squad = game.squad(1L)
        val tactics = Tactics(Formation.FOUR_FOUR_TWO)

        val lineup = Lineup.autoSelect(squad, tactics)
        val gk = game.player(lineup.starters.first())

        assertTrue(com.footballmanager.model.Position.GK in gk.naturalPositions)
    }
}
```

- [ ] **Step 2: Implement `Lineup` and `Lineup.autoSelect`**

Create `engine/src/main/kotlin/com/footballmanager/simulation/Lineup.kt`:
```kotlin
package com.footballmanager.simulation

import com.footballmanager.model.Player
import kotlinx.serialization.Serializable

@Serializable
data class Lineup(
    val starters: List<Long>,
    val substitutes: List<Long> = emptyList(),
) {
    init {
        require(starters.size == 11) { "Lineup must have exactly 11 starters (got ${starters.size})" }
        require(starters.distinct().size == 11) { "Starters must be unique players" }
    }

    companion object {
        fun autoSelect(players: List<Player>, tactics: Tactics): Lineup {
            require(players.size >= 11) { "Squad must have at least 11 players to select a lineup (got ${players.size})" }
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

- [ ] **Step 3: Commit Task 2 changes**

```bash
git add engine/src/main/kotlin/com/footballmanager/simulation/Lineup.kt \
        engine/src/test/kotlin/com/footballmanager/simulation/LineupTest.kt
git commit -m "feat: add Lineup model and auto-selection logic"
```

---

### Task 3: Team Strength from Lineup & Squad Selection

**Files:**
- Modify: `engine/src/main/kotlin/com/footballmanager/simulation/Team.kt`
- Create: `engine/src/test/kotlin/com/footballmanager/simulation/TeamLineupTest.kt`

**Interfaces:**
- Consumes: `Lineup`, `Tactics.formation.slots`, `Player.effectiveOverall`
- Produces:
  - `Team.fromLineup(clubId: Long, starters: List<Player>, tactics: Tactics): Team`
  - `Team.fromSquad(clubId: Long, players: List<Player>, tactics: Tactics): Team`

- [ ] **Step 1: Write `TeamLineupTest`**

Create `engine/src/test/kotlin/com/footballmanager/simulation/TeamLineupTest.kt`:
```kotlin
package com.footballmanager.simulation

import com.footballmanager.seed.SeedData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TeamLineupTest {

    @Test
    fun `team from lineup reflects starting players`() {
        val game = SeedData.game()
        val squad = game.squad(1L)
        val tactics = Tactics(Formation.FOUR_FOUR_TWO)
        val lineup = Lineup.autoSelect(squad, tactics)
        val playerMap = squad.associateBy { it.id }
        val starters = lineup.starters.map { playerMap.getValue(it) }

        val teamFromLineup = Team.fromLineup(1L, starters, tactics)
        val teamFromSquad = Team.fromSquad(1L, squad, tactics)

        assertEquals(teamFromLineup.attack, teamFromSquad.attack)
        assertEquals(teamFromLineup.defense, teamFromSquad.defense)
    }

    @Test
    fun `team strength drops when starters are exhausted`() {
        val game = SeedData.game()
        val squad = game.squad(1L)
        val tactics = Tactics(Formation.FOUR_FOUR_TWO)

        val freshTeam = Team.fromSquad(1L, squad, tactics)
        val tiredSquad = squad.map { it.copy(fitness = 40) }
        val tiredTeam = Team.fromSquad(1L, tiredSquad, tactics)

        assertTrue(tiredTeam.attack < freshTeam.attack, "attack should drop with low fitness")
        assertTrue(tiredTeam.defense < freshTeam.defense, "defense should drop with low fitness")
    }
}
```

- [ ] **Step 2: Update `Team.kt` with `fromLineup` and `fromSquad` delegation**

In `engine/src/main/kotlin/com/footballmanager/simulation/Team.kt`:
```kotlin
package com.footballmanager.simulation

import com.footballmanager.model.MAX_ATTRIBUTE
import com.footballmanager.model.MIN_ATTRIBUTE
import com.footballmanager.model.Player
import com.footballmanager.model.PositionGroup
import kotlin.math.roundToInt
import kotlinx.serialization.Serializable

@Serializable
data class Team(
    val clubId: Long,
    val attack: Int,
    val defense: Int,
    val tactics: Tactics = Tactics(),
) {
    init {
        require(attack in MIN_ATTRIBUTE..MAX_ATTRIBUTE) { "attack out of range: $attack" }
        require(defense in MIN_ATTRIBUTE..MAX_ATTRIBUTE) { "defense out of range: $defense" }
    }

    fun effectiveAttack(): Int =
        (attack * tactics.attackModifier).roundToInt().coerceIn(MIN_ATTRIBUTE, MAX_ATTRIBUTE)

    fun effectiveDefense(): Int =
        (defense * tactics.defenseModifier).roundToInt().coerceIn(MIN_ATTRIBUTE, MAX_ATTRIBUTE)

    companion object {
        private const val MID_RATING = 50

        fun fromLineup(clubId: Long, starters: List<Player>, tactics: Tactics = Tactics()): Team {
            require(starters.size == 11) { "Starting XI must have exactly 11 players (got ${starters.size})" }
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
    }
}
```

- [ ] **Step 3: Commit Task 3 changes**

```bash
git add engine/src/main/kotlin/com/footballmanager/simulation/Team.kt \
        engine/src/test/kotlin/com/footballmanager/simulation/TeamLineupTest.kt
git commit -m "feat: derive team power from lineup starting XI and condition"
```

---

### Task 4: SeasonState & SeasonRunner Matchday Condition Lifecycle

**Files:**
- Modify: `engine/src/main/kotlin/com/footballmanager/simulation/season/SeasonState.kt`
- Modify: `engine/src/main/kotlin/com/footballmanager/simulation/season/SeasonRunner.kt`
- Create: `engine/src/test/kotlin/com/footballmanager/simulation/season/SeasonRunnerConditionTest.kt`

**Interfaces:**
- Consumes: `Team.fromLineup`, `Lineup.autoSelect`, `Player`, `SeasonState`
- Produces:
  - `SeasonState(..., val players: Map<Long, Player>, val lineups: Map<Long, Lineup>)`
  - `SeasonState.setLineup(clubId: Long, lineup: Lineup): SeasonState`
  - `SeasonRunner.start(..., players: Map<Long, Player>, lineups: Map<Long, Lineup>): SeasonState`
  - `SeasonRunner.playNextMatchday(state): SeasonState` (with fitness/morale post-match progression)

- [ ] **Step 1: Write `SeasonRunnerConditionTest`**

Create `engine/src/test/kotlin/com/footballmanager/simulation/season/SeasonRunnerConditionTest.kt`:
```kotlin
package com.footballmanager.simulation.season

import com.footballmanager.model.League
import com.footballmanager.seed.SeedData
import com.footballmanager.simulation.Formation
import com.footballmanager.simulation.Lineup
import com.footballmanager.simulation.Tactics
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonRunnerConditionTest {

    @Test
    fun `playing a matchday depletes starter fitness and recovers bench fitness`() {
        val game = SeedData.game()
        val league = game.competitions.getValue(SeedData.LEAGUE_ID) as League
        val teams = SeedData.teams(game)
        val runner = SeasonRunner()

        val initialState = runner.start(
            league = league,
            teams = teams,
            startDate = SeedData.START_DATE,
            humanClubId = 1L,
            players = game.players,
        )

        val nextState = runner.playNextMatchday(initialState)

        val club1Squad = game.squad(1L)
        val club1Lineup = Lineup.autoSelect(club1Squad, teams.first { it.clubId == 1L }.tactics)
        val starterId = club1Lineup.starters.first()
        val benchId = club1Lineup.substitutes.first()

        val updatedStarter = nextState.players.getValue(starterId)
        val updatedBench = nextState.players.getValue(benchId)

        assertEquals(88, updatedStarter.fitness) // 100 - 12 = 88
        assertEquals(100, updatedBench.fitness)   // 100 + 18 capped at 100
    }

    @Test
    fun `custom lineup is respected during matchday execution`() {
        val game = SeedData.game()
        val league = game.competitions.getValue(SeedData.LEAGUE_ID) as League
        val teams = SeedData.teams(game)
        val runner = SeasonRunner()

        var state = runner.start(
            league = league,
            teams = teams,
            startDate = SeedData.START_DATE,
            humanClubId = 1L,
            players = game.players,
        )

        val squad = game.squad(1L)
        // Reverse squad order to create a custom lineup
        val customStarters = squad.takeLast(11).map { it.id }
        val customLineup = Lineup(starters = customStarters, substitutes = squad.dropLast(11).map { it.id })

        state = state.setLineup(1L, customLineup)
        state = runner.playNextMatchday(state)

        for (starterId in customStarters) {
            assertEquals(88, state.players.getValue(starterId).fitness)
        }
    }
}
```

- [ ] **Step 2: Update `SeasonState.kt` and `SeasonRunner.kt`**

In `engine/src/main/kotlin/com/footballmanager/simulation/season/SeasonState.kt`:
```kotlin
package com.footballmanager.simulation.season

import com.footballmanager.model.League
import com.footballmanager.model.Player
import com.footballmanager.serialization.LocalDateSerializer
import com.footballmanager.simulation.Lineup
import com.footballmanager.simulation.MatchResult
import com.footballmanager.simulation.Tactics
import com.footballmanager.simulation.Team
import java.time.LocalDate
import kotlinx.serialization.Serializable

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
    init {
        require(nextFixtureIndex in 0..fixtures.size) { "nextFixtureIndex out of range" }
        require(results.size == nextFixtureIndex) {
            "results (${results.size}) must match played fixtures ($nextFixtureIndex)"
        }
    }

    val isFinished: Boolean get() = nextFixtureIndex >= fixtures.size
    val remainingFixtures: Int get() = fixtures.size - nextFixtureIndex
    val nextMatchday: Int? get() = fixtures.getOrNull(nextFixtureIndex)?.round

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

In `engine/src/main/kotlin/com/footballmanager/simulation/season/SeasonRunner.kt`:
```kotlin
package com.footballmanager.simulation.season

import com.footballmanager.model.League
import com.footballmanager.model.Player
import com.footballmanager.simulation.Lineup
import com.footballmanager.simulation.MatchEngine
import com.footballmanager.simulation.Team
import java.time.LocalDate

class SeasonRunner(
    private val engine: MatchEngine = MatchEngine(),
) {
    fun start(
        league: League,
        teams: List<Team>,
        startDate: LocalDate,
        humanClubId: Long? = null,
        players: Map<Long, Player> = emptyMap(),
        lineups: Map<Long, Lineup> = emptyMap(),
    ): SeasonState {
        val fixtures = FixtureGenerator.generate(teams, startDate)
        return SeasonState(
            league = league,
            teams = teams,
            fixtures = fixtures,
            standings = Standings(teams.map { StandingEntry(it) }.sortedWith(Standings.comparator)),
            currentDate = startDate,
            humanClubId = humanClubId,
            players = players,
            lineups = lineups,
        )
    }

    fun playNextMatchday(state: SeasonState): SeasonState {
        require(!state.isFinished) { "season already finished" }

        val start = state.nextFixtureIndex
        val round = state.fixtures[start].round
        var end = start
        while (end < state.fixtures.size && state.fixtures[end].round == round) end++

        val teamOf = state.teams.associateBy { it.clubId }
        val matchdayFixtures = state.fixtures.subList(start, end)
        val activeLineups = mutableMapOf<Long, Lineup>()

        // 1. Build matchday teams using lineups & current player condition
        fun resolveTeam(clubId: Long): Team {
            val baseTeam = teamOf.getValue(clubId)
            val clubPlayers = state.players.values.filter { it.contract.squadStatus != null && it.id in (state.players.keys) }
                .ifEmpty { emptyList() }

            if (clubPlayers.isEmpty() || clubPlayers.size < 11) {
                return baseTeam
            }

            val lineup = state.lineups[clubId] ?: Lineup.autoSelect(clubPlayers, baseTeam.tactics)
            activeLineups[clubId] = lineup
            val starters = lineup.starters.map { state.players.getValue(it) }
            return Team.fromLineup(clubId, starters, baseTeam.tactics)
        }

        val matchResults = matchdayFixtures.map { fixture ->
            val homeClubId = fixture.home.clubId
            val awayClubId = fixture.away.clubId

            val homeTeam = if (state.players.isNotEmpty()) resolveTeam(homeClubId) else teamOf.getValue(homeClubId)
            val awayTeam = if (state.players.isNotEmpty()) resolveTeam(awayClubId) else teamOf.getValue(awayClubId)

            engine.simulate(homeTeam, awayTeam)
        }

        // 2. Update player condition based on match participation and results
        val updatedPlayers = state.players.toMutableMap()
        if (state.players.isNotEmpty()) {
            for (result in matchResults) {
                updateClubCondition(result.homeClubId, result.homeScore, result.awayScore, activeLineups, updatedPlayers)
                updateClubCondition(result.awayClubId, result.awayScore, result.homeScore, activeLineups, updatedPlayers)
            }
        }

        return state.copy(
            results = state.results + matchResults,
            standings = state.standings.withResults(matchdayFixtures, matchResults),
            nextFixtureIndex = end,
            currentDate = matchdayFixtures.last().date,
            players = updatedPlayers,
        )
    }

    private fun updateClubCondition(
        clubId: Long,
        goalsFor: Int,
        goalsAgainst: Int,
        activeLineups: Map<Long, Lineup>,
        playersMap: MutableMap<Long, Player>,
    ) {
        val lineup = activeLineups[clubId] ?: return
        val starterIds = lineup.starters.toSet()

        val (starterMoraleDelta, benchMoraleDelta) = when {
            goalsFor > goalsAgainst -> 8 to 4
            goalsFor < goalsAgainst -> -6 to -3
            else -> 0 to 0
        }

        for (starterId in lineup.starters) {
            playersMap[starterId]?.let { p ->
                val newFitness = (p.fitness - 12).coerceIn(1, 100)
                val newMorale = (p.morale + starterMoraleDelta).coerceIn(1, 100)
                playersMap[starterId] = p.copy(fitness = newFitness, morale = newMorale)
            }
        }

        for (benchId in lineup.substitutes) {
            playersMap[benchId]?.let { p ->
                val newFitness = (p.fitness + 18).coerceIn(1, 100)
                val newMorale = if (starterMoraleDelta == 0) {
                    when {
                        p.morale > 50 -> (p.morale - 1).coerceIn(1, 100)
                        p.morale < 50 -> (p.morale + 1).coerceIn(1, 100)
                        else -> 50
                    }
                } else {
                    (p.morale + benchMoraleDelta).coerceIn(1, 100)
                }
                playersMap[benchId] = p.copy(fitness = newFitness, morale = newMorale)
            }
        }
    }
}
```

- [ ] **Step 3: Commit Task 4 changes**

```bash
git add engine/src/main/kotlin/com/footballmanager/simulation/season/SeasonState.kt \
        engine/src/main/kotlin/com/footballmanager/simulation/season/SeasonRunner.kt \
        engine/src/test/kotlin/com/footballmanager/simulation/season/SeasonRunnerConditionTest.kt
git commit -m "feat: manage lineup and player condition updates across matchdays"
```

---

### Task 5: Serialization, CLI Demo & CI Verification

**Files:**
- Modify: `engine/src/main/kotlin/com/footballmanager/Main.kt`
- Modify: `engine/src/test/kotlin/com/footballmanager/serialization/GameSerializationTest.kt`

**Interfaces:**
- Consumes: `SeasonState.players`, `SeasonState.lineups`, `Game.saveToFile`, `Game.loadFromFile`
- Produces: Verified round-trip serialization and end-to-end demo execution in CI

- [ ] **Step 1: Update serialization tests for player condition and lineups in `SeasonState`**

In `engine/src/test/kotlin/com/footballmanager/serialization/GameSerializationTest.kt`:
```kotlin
    @Test
    fun `mid-season save preserves player fitness and lineups`() {
        val game = SeedData.game()
        val league = game.competitions.getValue(SeedData.LEAGUE_ID) as League
        val teams = SeedData.teams(game)
        val runner = SeasonRunner()

        var state = runner.start(league, teams, SeedData.START_DATE, humanClubId = 1L, players = game.players)
        state = runner.playNextMatchday(state)

        val gameWithSeason = game.copy(players = state.players, currentSeason = state)
        val json = Game.encodeToString(gameWithSeason)
        val decoded = Game.decodeFromString(json)

        assertEquals(gameWithSeason, decoded)
        assertEquals(state.players[1L]?.fitness, decoded.currentSeason?.players?.get(1L)?.fitness)
    }
```

- [ ] **Step 2: Update `Main.kt` demo to showcase rotation & condition**

In `engine/src/main/kotlin/com/footballmanager/Main.kt`:
Pass `game.players` to `runner.start(...)`, show starter fitness changes, and showcase how squad condition dynamically changes each matchday.

- [ ] **Step 3: Commit Task 5, push to GitHub, and verify via CI**

```bash
git add engine/src/main/kotlin/com/footballmanager/Main.kt \
        engine/src/test/kotlin/com/footballmanager/serialization/GameSerializationTest.kt
git commit -m "feat: persist player condition and showcase lineup rotation in demo"
git push origin main
gh run watch
```

Check `gh run view` to verify all tests in CI pass green.
