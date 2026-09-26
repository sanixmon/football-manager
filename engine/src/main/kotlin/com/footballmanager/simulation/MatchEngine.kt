package com.footballmanager.simulation

import com.footballmanager.logging.MatchLogger
import com.footballmanager.logging.NoOpMatchLogger
import com.footballmanager.model.Attribute
import com.footballmanager.model.Contract
import com.footballmanager.model.EmergencyGoalkeeper
import com.footballmanager.model.Player
import com.footballmanager.model.PlayerAttributes
import com.footballmanager.model.Position
import com.footballmanager.model.PositionGroup
import java.time.LocalDate
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * A deterministic, player-attributed 90-minute football match engine.
 *
 * Simulates a full match minute-by-minute (1..90) using probabilistic modeling
 * driven by player attributes, tactical choices, and an explicit [RandomSource].
 */
class MatchEngine(
    private val random: RandomSource = KotlinRandomSource(),
    private val logger: MatchLogger = NoOpMatchLogger,
) {

    /** Seeded convenience constructor. */
    constructor(seed: Long, logger: MatchLogger = NoOpMatchLogger) : this(KotlinRandomSource(Random(seed)), logger)

    fun simulate(
        home: Team,
        away: Team,
        homeStartersOverride: List<Player>? = null,
        homeSubsOverride: List<Player>? = null,
        awayStartersOverride: List<Player>? = null,
        awaySubsOverride: List<Player>? = null,
    ): MatchResult {
        require(home.clubId != away.clubId) { "home and away must be different clubs" }

        if (logger.isEnabled) {
            logger.onMatchStart(home, away)
        }

        // 1. Resolve starting XIs and benches
        val homeStarters = (homeStartersOverride ?: home.starters).ifEmpty {
            generateSyntheticStarters(home.clubId, home.effectiveAttack(), home.effectiveDefense(), home.tactics)
        }.toMutableList()
        val homeBench = (homeSubsOverride ?: home.substitutes).ifEmpty {
            generateSyntheticBench(home.clubId, home.effectiveAttack(), home.effectiveDefense())
        }.toMutableList()

        val awayStarters = (awayStartersOverride ?: away.starters).ifEmpty {
            generateSyntheticStarters(away.clubId, away.effectiveAttack(), away.effectiveDefense(), away.tactics)
        }.toMutableList()
        val awayBench = (awaySubsOverride ?: away.substitutes).ifEmpty {
            generateSyntheticBench(away.clubId, away.effectiveAttack(), away.effectiveDefense())
        }.toMutableList()

        // 2. Setup team in-match simulation contexts
        val homeCtx = TeamMatchContext(
            team = home,
            side = Side.HOME,
            activePlayers = homeStarters,
            benchPlayers = homeBench,
        )
        val awayCtx = TeamMatchContext(
            team = away,
            side = Side.AWAY,
            activePlayers = awayStarters,
            benchPlayers = awayBench,
        )

        val events = mutableListOf<MatchEvent>()
        val substitutions = mutableListOf<SubstitutionRecord>()

        // 3. Minute-by-minute simulation (1..90)
        for (minute in 1..MINUTES_PER_MATCH) {
            // A. Update Stamina / Fatigue
            homeCtx.decayStamina()
            awayCtx.decayStamina()

            // B. Automated Substitutions check
            processSubstitutions(homeCtx, minute, events, substitutions)
            processSubstitutions(awayCtx, minute, events, substitutions)

            // C. Possession Determination
            val homePower = homeCtx.currentAttackPower() * HOME_ADVANTAGE * home.tactics.passing.possessionModifier
            val awayPower = awayCtx.currentAttackPower() * away.tactics.passing.possessionModifier
            val homePossessionProb = (homePower / (homePower + awayPower)).coerceIn(0.15, 0.85)

            val homeHasBall = random.nextDouble() < homePossessionProb
            val attackingCtx = if (homeHasBall) homeCtx else awayCtx
            val defendingCtx = if (homeHasBall) awayCtx else homeCtx

            attackingCtx.possessions++

            // D. Chance Generation
            val baseChance = BASE_CHANCE_PER_MINUTE
            val mentalityMod = when (attackingCtx.team.tactics.mentality) {
                Mentality.ATTACKING -> 1.20
                Mentality.BALANCED -> 1.00
                Mentality.DEFENSIVE -> 0.80
            }
            val tempoMod = when (attackingCtx.team.tactics.tempo) {
                Tempo.FAST -> 1.15
                Tempo.NORMAL -> 1.00
                Tempo.SLOW -> 0.85
            }
            val chanceProb = (baseChance * mentalityMod * tempoMod).coerceIn(0.04, 0.40)

            if (random.nextDouble() < chanceProb) {
                events.add(
                    MatchEvent(
                        minute = minute,
                        type = MatchEventType.ATTACK,
                        side = attackingCtx.side,
                        teamId = attackingCtx.team.clubId,
                        description = "Attacking build-up by ${attackingCtx.side}",
                    ),
                )

                // E. Defending Challenge / Tackle
                val foulBaseProb = 0.20 * defendingCtx.team.tactics.pressing.ballWinningModifier
                if (random.nextDouble() < foulBaseProb) {
                    // Foul committed by defending player
                    val foulingDefender = defendingCtx.pickDefenderForAction(random)
                    if (foulingDefender != null) {
                        defendingCtx.fouls++
                        defendingCtx.playerStats(foulingDefender.id).let {
                            it.copy(fouls = it.fouls + 1)
                        }.also { defendingCtx.recordPlayerStat(it) }
                    }

                    events.add(
                        MatchEvent(
                            minute = minute,
                            type = MatchEventType.FOUL,
                            side = defendingCtx.side,
                            teamId = defendingCtx.team.clubId,
                            playerId = foulingDefender?.id,
                            description = "Foul committed by ${foulingDefender?.name ?: "Defender"}",
                        ),
                    )

                    // Card checks on foul
                    val cardRoll = random.nextDouble()
                    if (cardRoll < 0.015 && foulingDefender != null) {
                        // Straight RED CARD
                        defendingCtx.redCards++
                        defendingCtx.ejectPlayer(foulingDefender.id)
                        defendingCtx.playerStats(foulingDefender.id).let {
                            it.copy(redCards = it.redCards + 1, isRedCarded = true)
                        }.also { defendingCtx.recordPlayerStat(it) }

                        events.add(
                            MatchEvent(
                                minute = minute,
                                type = MatchEventType.RED_CARD,
                                side = defendingCtx.side,
                                teamId = defendingCtx.team.clubId,
                                playerId = foulingDefender.id,
                                description = "Straight RED card shown to ${foulingDefender.name}",
                            ),
                        )
                    } else if (cardRoll < 0.15 && foulingDefender != null) {
                        // Yellow Card
                        if (defendingCtx.yellowCarded.contains(foulingDefender.id)) {
                            // Second yellow -> RED CARD
                            defendingCtx.redCards++
                            defendingCtx.ejectPlayer(foulingDefender.id)
                            defendingCtx.playerStats(foulingDefender.id).let {
                                it.copy(yellowCards = it.yellowCards + 1, redCards = it.redCards + 1, isRedCarded = true)
                            }.also { defendingCtx.recordPlayerStat(it) }

                            events.add(
                                MatchEvent(
                                    minute = minute,
                                    type = MatchEventType.RED_CARD,
                                    side = defendingCtx.side,
                                    teamId = defendingCtx.team.clubId,
                                    playerId = foulingDefender.id,
                                    description = "Second yellow card! Red card shown to ${foulingDefender.name}",
                                ),
                            )
                        } else {
                            defendingCtx.yellowCards++
                            defendingCtx.yellowCarded.add(foulingDefender.id)
                            defendingCtx.playerStats(foulingDefender.id).let {
                                it.copy(yellowCards = it.yellowCards + 1)
                            }.also { defendingCtx.recordPlayerStat(it) }

                            events.add(
                                MatchEvent(
                                    minute = minute,
                                    type = MatchEventType.YELLOW_CARD,
                                    side = defendingCtx.side,
                                    teamId = defendingCtx.team.clubId,
                                    playerId = foulingDefender.id,
                                    description = "Yellow card shown to ${foulingDefender.name}",
                                ),
                            )
                        }
                    }
                } else {
                    // F. Shot Attempt
                    val shooter = attackingCtx.pickShooter(random)
                    if (shooter != null) {
                        attackingCtx.shots++
                        val shooterStats = attackingCtx.playerStats(shooter.id)
                        attackingCtx.recordPlayerStat(shooterStats.copy(shots = shooterStats.shots + 1))

                        events.add(
                            MatchEvent(
                                minute = minute,
                                type = MatchEventType.SHOT,
                                side = attackingCtx.side,
                                teamId = attackingCtx.team.clubId,
                                playerId = shooter.id,
                                description = "Shot by ${shooter.name}",
                            ),
                        )

                        // Is shot on target?
                        val onTargetProb = (0.50 + (shooter.attributes[Attribute.SHOOTING] / 300.0)).coerceIn(0.35, 0.75)
                        if (random.nextDouble() < onTargetProb) {
                            attackingCtx.shotsOnTarget++
                            val currentStats = attackingCtx.playerStats(shooter.id)
                            attackingCtx.recordPlayerStat(currentStats.copy(shotsOnTarget = currentStats.shotsOnTarget + 1))

                            events.add(
                                MatchEvent(
                                    minute = minute,
                                    type = MatchEventType.SHOT_ON_TARGET,
                                    side = attackingCtx.side,
                                    teamId = attackingCtx.team.clubId,
                                    playerId = shooter.id,
                                    description = "Shot on target by ${shooter.name}",
                                ),
                            )

                            // Goal vs Save
                            val gk = defendingCtx.activeGoalkeeper()
                            val gkskill = gk?.attributes?.get(Attribute.GOALKEEPER) ?: defendingCtx.team.defense
                            val shootSkill = shooter.attributes[Attribute.SHOOTING]

                            val goalProb = (0.35 + (shootSkill - gkskill) * 0.005).coerceIn(0.12, 0.65)
                            if (random.nextDouble() < goalProb) {
                                // GOAL!
                                attackingCtx.goals++
                                val goalStats = attackingCtx.playerStats(shooter.id)
                                attackingCtx.recordPlayerStat(goalStats.copy(goals = goalStats.goals + 1))

                                // Assister (optional)
                                val assister = attackingCtx.pickAssister(shooter.id, random)
                                if (assister != null) {
                                    val assistStats = attackingCtx.playerStats(assister.id)
                                    attackingCtx.recordPlayerStat(assistStats.copy(assists = assistStats.assists + 1))
                                }

                                events.add(
                                    MatchEvent(
                                        minute = minute,
                                        type = MatchEventType.GOAL,
                                        side = attackingCtx.side,
                                        teamId = attackingCtx.team.clubId,
                                        playerId = shooter.id,
                                        secondaryPlayerId = assister?.id,
                                        description = "GOAL! ${shooter.name}" + (if (assister != null) " (assist: ${assister.name})" else ""),
                                    ),
                                )
                            } else {
                                // SAVE!
                                gk?.let { keeper ->
                                    val gkStats = defendingCtx.playerStats(keeper.id)
                                    defendingCtx.recordPlayerStat(gkStats.copy(saves = gkStats.saves + 1))
                                }

                                events.add(
                                    MatchEvent(
                                        minute = minute,
                                        type = MatchEventType.SAVE,
                                        side = defendingCtx.side,
                                        teamId = defendingCtx.team.clubId,
                                        playerId = gk?.id,
                                        description = "Save by goalkeeper ${gk?.name ?: "GK"}",
                                    ),
                                )
                                events.add(
                                    MatchEvent(
                                        minute = minute,
                                        type = MatchEventType.SHOT_SAVED,
                                        side = attackingCtx.side,
                                        teamId = attackingCtx.team.clubId,
                                        playerId = shooter.id,
                                    ),
                                )

                                // Check for Corner
                                if (random.nextDouble() < 0.35) {
                                    attackingCtx.corners++
                                    events.add(
                                        MatchEvent(
                                            minute = minute,
                                            type = MatchEventType.CORNER,
                                            side = attackingCtx.side,
                                            teamId = attackingCtx.team.clubId,
                                            description = "Corner awarded to ${attackingCtx.side}",
                                        ),
                                    )
                                }
                            }
                        } else {
                            // Missed
                            events.add(
                                MatchEvent(
                                    minute = minute,
                                    type = MatchEventType.SHOT_MISSED,
                                    side = attackingCtx.side,
                                    teamId = attackingCtx.team.clubId,
                                    playerId = shooter.id,
                                    description = "Shot missed by ${shooter.name}",
                                ),
                            )
                        }
                    }
                }
            }

            // G. In-match Injury Check
            checkInMatchInjury(homeCtx, minute, events, substitutions)
            checkInMatchInjury(awayCtx, minute, events, substitutions)

            // H. Increment minutes played for all active players
            homeCtx.activePlayers.forEach { p ->
                val cur = homeCtx.playerStats(p.id)
                homeCtx.recordPlayerStat(cur.copy(minutesPlayed = cur.minutesPlayed + 1))
            }
            awayCtx.activePlayers.forEach { p ->
                val cur = awayCtx.playerStats(p.id)
                awayCtx.recordPlayerStat(cur.copy(minutesPlayed = cur.minutesPlayed + 1))
            }
        }

        // 4. Compile final team & match statistics
        val homeTeamStats = TeamStats(
            possessions = homeCtx.possessions,
            shots = homeCtx.shots,
            shotsOnTarget = homeCtx.shotsOnTarget,
            goals = homeCtx.goals,
            corners = homeCtx.corners,
            fouls = homeCtx.fouls,
            yellowCards = homeCtx.yellowCards,
            redCards = homeCtx.redCards,
        )
        val awayTeamStats = TeamStats(
            possessions = awayCtx.possessions,
            shots = awayCtx.shots,
            shotsOnTarget = awayCtx.shotsOnTarget,
            goals = awayCtx.goals,
            corners = awayCtx.corners,
            fouls = awayCtx.fouls,
            yellowCards = awayCtx.yellowCards,
            redCards = awayCtx.redCards,
        )

        val matchStats = MatchStats(
            ticks = MINUTES_PER_MATCH,
            home = homeTeamStats,
            away = awayTeamStats,
        )

        // 5. Combine player match stats
        val combinedPlayerStats = (homeCtx.allPlayerStats() + awayCtx.allPlayerStats()).associateBy { it.playerId }

        // 6. Calculate position-aware player ratings (1.0..10.0)
        val playerRatings = mutableMapOf<Long, Double>()
        val allParticipants = homeCtx.allKnownPlayers() + awayCtx.allKnownPlayers()

        for (player in allParticipants) {
            val stats = combinedPlayerStats[player.id] ?: continue
            if (stats.minutesPlayed > 0) {
                val isHome = player.id in homeCtx.allKnownPlayerIds()
                val goalsConceded = if (isHome) awayCtx.goals else homeCtx.goals
                val teamLost = if (isHome) homeCtx.goals < awayCtx.goals else awayCtx.goals < homeCtx.goals
                val rating = calculateRating(player, stats, goalsConceded, teamLost)
                playerRatings[player.id] = rating
            }
        }

        val result = MatchResult(
            homeClubId = home.clubId,
            awayClubId = away.clubId,
            homeScore = homeCtx.goals,
            awayScore = awayCtx.goals,
            events = events,
            stats = matchStats,
            durationMinutes = MINUTES_PER_MATCH,
            playerStats = combinedPlayerStats,
            playerRatings = playerRatings,
            substitutions = substitutions,
        )

        if (logger.isEnabled) {
            logger.onMatchEnd(result)
        }

        return result
    }

    private fun processSubstitutions(
        ctx: TeamMatchContext,
        minute: Int,
        events: MutableList<MatchEvent>,
        substitutions: MutableList<SubstitutionRecord>,
    ) {
        if (ctx.substitutionsCount >= MAX_SUBS_PER_MATCH || ctx.benchPlayers.isEmpty()) return

        // 1. Check for injured player needing sub
        val injuredActive = ctx.activePlayers.firstOrNull { ctx.playerStats(it.id).isInjured }
        if (injuredActive != null) {
            executeSubstitution(ctx, injuredActive, minute, "Injury", events, substitutions)
            return
        }

        // 2. Check for severe fatigue after minute 60
        if (minute >= 60 && ctx.substitutionsCount < MAX_SUBS_PER_MATCH) {
            val fatigued = ctx.activePlayers.firstOrNull {
                ctx.stamina(it.id) < 35.0 && Position.GK !in it.naturalPositions
            }
            if (fatigued != null) {
                executeSubstitution(ctx, fatigued, minute, "Fatigue", events, substitutions)
                return
            }
        }

        // 3. Check for tactical underperformance after minute 65
        if (minute >= 65 && ctx.substitutionsCount < MAX_SUBS_PER_MATCH) {
            val underperforming = ctx.activePlayers.firstOrNull {
                Position.GK !in it.naturalPositions &&
                    ctx.playerStats(it.id).goals == 0 &&
                    ctx.playerStats(it.id).assists == 0 &&
                    ctx.stamina(it.id) < 55.0
            }
            if (underperforming != null && ctx.benchPlayers.isNotEmpty()) {
                executeSubstitution(ctx, underperforming, minute, "Tactical", events, substitutions)
            }
        }
    }

    private fun executeSubstitution(
        ctx: TeamMatchContext,
        playerOut: Player,
        minute: Int,
        reason: String,
        events: MutableList<MatchEvent>,
        substitutions: MutableList<SubstitutionRecord>,
    ): Boolean {
        val candidateSub = ctx.benchPlayers.firstOrNull {
            !ctx.playerStats(it.id).isInjured &&
            !ctx.playerStats(it.id).isRedCarded &&
            !ctx.playerStats(it.id).isSubstitutedIn &&
            it.id !in ctx.subbedOut
        } ?: return false
        ctx.activePlayers.remove(playerOut)
        ctx.benchPlayers.remove(candidateSub)
        ctx.activePlayers.add(candidateSub)
        ctx.subbedOut.add(playerOut.id)
        ctx.substitutionsCount++

        val outStats = ctx.playerStats(playerOut.id).copy(isSubstitutedOut = true)
        val inStats = ctx.playerStats(candidateSub.id).copy(isSubstitutedIn = true)
        ctx.recordPlayerStat(outStats)
        ctx.recordPlayerStat(inStats)

        val record = SubstitutionRecord(
            minute = minute,
            teamId = ctx.team.clubId,
            playerOutId = playerOut.id,
            playerInId = candidateSub.id,
            reason = reason,
        )
        substitutions.add(record)

        events.add(
            MatchEvent(
                minute = minute,
                type = MatchEventType.SUBSTITUTION,
                side = ctx.side,
                teamId = ctx.team.clubId,
                playerId = playerOut.id,
                secondaryPlayerId = candidateSub.id,
                description = "Substitution ($reason): ${playerOut.name} OFF, ${candidateSub.name} ON",
            ),
        )
        return true
    }
    private fun checkInMatchInjury(
        ctx: TeamMatchContext,
        minute: Int,
        events: MutableList<MatchEvent>,
        substitutions: MutableList<SubstitutionRecord>,
    ) {
        if (ctx.activePlayers.isEmpty()) return
        // ~0.003 chance per team per minute (about 1 injury every ~4 matches across both teams)
        if (random.nextDouble() < 0.003) {
            val injured = ctx.activePlayers.random(random)
            val st = ctx.playerStats(injured.id).copy(isInjured = true)
            ctx.recordPlayerStat(st)

            events.add(
                MatchEvent(
                    minute = minute,
                    type = MatchEventType.INJURY,
                    side = ctx.side,
                    teamId = ctx.team.clubId,
                    playerId = injured.id,
                    description = "Injury to ${injured.name}",
                ),
            )

            // Immediate substitution if possible, otherwise remove injured player from field
            val subbed = if (ctx.substitutionsCount < MAX_SUBS_PER_MATCH && ctx.benchPlayers.isNotEmpty()) {
                executeSubstitution(ctx, injured, minute, "Injury", events, substitutions)
            } else false

            if (!subbed) {
                ctx.activePlayers.remove(injured)
            }
        }
    }
    private fun calculateRating(
        player: Player,
        stats: PlayerMatchStats,
        goalsConceded: Int,
        teamLost: Boolean,
    ): Double {
        var rating = 6.2

        // Goals and assists
        rating += stats.goals * 1.1
        rating += stats.assists * 0.6

        // Position-specific modifiers
        val primaryPos = player.bestPosition()
        when (primaryPos.group) {
            PositionGroup.GOALKEEPER -> {
                rating += stats.saves * 0.45
                rating -= goalsConceded * 0.40
                if (goalsConceded == 0 && stats.minutesPlayed >= 45) rating += 0.8
            }
            PositionGroup.DEFENDER -> {
                rating += stats.defensiveContributions * 0.25
                rating -= goalsConceded * 0.25
                if (goalsConceded == 0 && stats.minutesPlayed >= 45) rating += 0.6
                rating += stats.passingContributions * 0.1
            }
            PositionGroup.MIDFIELDER -> {
                rating += stats.passingContributions * 0.25
                rating += stats.attackingContributions * 0.25
                rating += stats.defensiveContributions * 0.15
                rating += stats.shotsOnTarget * 0.2
            }
            PositionGroup.ATTACKER -> {
                rating += stats.shotsOnTarget * 0.35
                rating += stats.attackingContributions * 0.25
                rating += stats.shots * 0.10
            }
        }

        // Cards & Disciplinary penalties
        if (stats.yellowCards > 0) rating -= 0.4 * stats.yellowCards
        if (stats.redCards > 0) rating -= 1.8 * stats.redCards
        rating -= stats.fouls * 0.1

        // Team loss penalty
        if (teamLost && goalsConceded >= 3) {
            rating -= 0.4
        }

        return (rating * 10).roundToInt() / 10.0.coerceIn(1.0, 10.0)
    }

    private fun generateSyntheticStarters(clubId: Long, attack: Int, defense: Int, tactics: Tactics): List<Player> {
        return tactics.formation.slots.mapIndexed { index, pos ->
            val pId = clubId * 1000L + (index + 1)
            createSyntheticPlayer(pId, "Player $pId", pos, attack, defense, isStarter = true)
        }
    }

    private fun generateSyntheticBench(clubId: Long, attack: Int, defense: Int): List<Player> {
        val benchPositions = listOf(Position.GK, Position.CB, Position.CM, Position.LW, Position.ST)
        return benchPositions.mapIndexed { index, pos ->
            val pId = clubId * 1000L + (12 + index)
            createSyntheticPlayer(pId, "Sub $pId", pos, attack, defense, isStarter = false)
        }
    }

    private fun createSyntheticPlayer(
        id: Long,
        name: String,
        position: Position,
        attack: Int,
        defense: Int,
        isStarter: Boolean,
    ): Player {
        val quality = if (position.group == PositionGroup.DEFENDER || position.group == PositionGroup.GOALKEEPER) defense else attack
        val attrs = mapOf(
            Attribute.PACE to quality,
            Attribute.SHOOTING to if (position.group == PositionGroup.ATTACKER) quality + 5 else quality - 10,
            Attribute.PASSING to quality,
            Attribute.DRIBBLING to quality,
            Attribute.DEFENDING to if (position.group == PositionGroup.DEFENDER) quality + 5 else quality - 10,
            Attribute.PHYSICAL to quality,
            Attribute.GOALKEEPER to if (position == Position.GK) quality + 10 else 10,
        )
        return Player(
            id = id,
            name = name,
            age = 24,
            nationality = "ID",
            naturalPositions = listOf(position),
            attributes = PlayerAttributes(attrs),
            contract = Contract(weeklyWage = 2000L, expiresOn = LocalDate.of(2030, 6, 30)),
        )
    }

    companion object {
        const val MINUTES_PER_MATCH = 90
        const val BASE_CHANCE_PER_MINUTE = 0.16
        const val HOME_ADVANTAGE = 1.05
        const val MAX_SUBS_PER_MATCH = 5

        /** Backward-compatible ticks constants. */
        const val TICKS_PER_MATCH = 90
        const val MINUTES_PER_TICK = 1
    }
}

/** In-match tracking context for a single team. */
private class TeamMatchContext(
    val team: Team,
    val side: Side,
    val activePlayers: MutableList<Player>,
    val benchPlayers: MutableList<Player>,
) {
    var possessions: Int = 0
    var shots: Int = 0
    var shotsOnTarget: Int = 0
    var goals: Int = 0
    var corners: Int = 0
    var fouls: Int = 0
    var yellowCards: Int = 0
    var redCards: Int = 0
    var substitutionsCount: Int = 0

    val subbedOut: MutableSet<Long> = mutableSetOf()
    val yellowCarded: MutableSet<Long> = mutableSetOf()
    private val staminaMap: MutableMap<Long, Double> = (activePlayers + benchPlayers).associate { it.id to it.fitness.toDouble() }.toMutableMap()
    private val statsMap: MutableMap<Long, PlayerMatchStats> = mutableMapOf()

    init {
        activePlayers.forEach { p ->
            statsMap[p.id] = PlayerMatchStats(playerId = p.id, teamId = team.clubId, isStarter = true)
        }
        benchPlayers.forEach { p ->
            statsMap[p.id] = PlayerMatchStats(playerId = p.id, teamId = team.clubId, isStarter = false)
        }
    }

    fun stamina(playerId: Long): Double = staminaMap[playerId] ?: 100.0

    fun decayStamina() {
        val rate = when (team.tactics.pressing) {
            Pressing.HIGH -> 0.45
            Pressing.MEDIUM -> 0.35
            Pressing.LOW -> 0.25
        } * when (team.tactics.tempo) {
            Tempo.FAST -> 1.20
            Tempo.NORMAL -> 1.00
            Tempo.SLOW -> 0.85
        }
        activePlayers.forEach { p ->
            staminaMap[p.id] = (stamina(p.id) - rate).coerceAtLeast(10.0)
        }
    }

    fun playerStats(playerId: Long): PlayerMatchStats =
        statsMap.getOrPut(playerId) { PlayerMatchStats(playerId = playerId, teamId = team.clubId) }

    fun recordPlayerStat(stat: PlayerMatchStats) {
        statsMap[stat.playerId] = stat
    }

    fun allPlayerStats(): List<PlayerMatchStats> = statsMap.values.toList()

    fun allKnownPlayers(): List<Player> = (activePlayers + benchPlayers).distinctBy { it.id }

    fun allKnownPlayerIds(): Set<Long> = allKnownPlayers().map { it.id }.toSet()

    fun ejectPlayer(playerId: Long) {
        activePlayers.removeAll { it.id == playerId }
    }

    fun currentAttackPower(): Double {
        val playerRatio = (activePlayers.size / 11.0).coerceIn(0.5, 1.0)
        return team.effectiveAttack() * playerRatio
    }

    fun activeGoalkeeper(): Player? =
        activePlayers.firstOrNull { Position.GK in it.naturalPositions } ?: activePlayers.firstOrNull()

    fun pickShooter(random: RandomSource): Player? {
        if (activePlayers.isEmpty()) return null
        val candidates = activePlayers.filter { Position.GK !in it.naturalPositions }.ifEmpty { activePlayers }
        val weights = candidates.map { p ->
            when (p.bestPosition().group) {
                PositionGroup.ATTACKER -> 5
                PositionGroup.MIDFIELDER -> 3
                PositionGroup.DEFENDER -> 1
                PositionGroup.GOALKEEPER -> 1
            }
        }
        val total = weights.sum()
        var roll = random.nextInt(0, total)
        for ((idx, w) in weights.withIndex()) {
            roll -= w
            if (roll < 0) return candidates[idx]
        }
        return candidates.last()
    }

    fun pickAssister(scorerId: Long, random: RandomSource): Player? {
        val candidates = activePlayers.filter { it.id != scorerId && Position.GK !in it.naturalPositions }
        if (candidates.isEmpty() || random.nextDouble() > 0.65) return null
        return candidates.random(random)
    }

    fun pickDefenderForAction(random: RandomSource): Player? {
        if (activePlayers.isEmpty()) return null
        val defenders = activePlayers.filter { it.bestPosition().group == PositionGroup.DEFENDER || it.bestPosition().group == PositionGroup.MIDFIELDER }
        return if (defenders.isNotEmpty()) defenders.random(random) else activePlayers.random(random)
    }
}

private fun <T> List<T>.random(random: RandomSource): T =
    this[random.nextInt(0, size)]
