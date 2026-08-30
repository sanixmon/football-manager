package com.footballmanager.simulation

import com.footballmanager.logging.MatchLogger
import com.footballmanager.logging.NoOpMatchLogger
import kotlin.random.Random

/**
 * A simple, deterministic, tick-based football match engine.
 *
 * A match is 90 minutes, simulated in 18 ticks of 5 minutes. Each tick:
 *
 *   1. Possession — who attacks, weighted by each side's attack power.
 *   2. Chance     — the attacker creates a scoring chance.
 *   3. Shot       — the chance is converted into a shot.
 *   4. Outcome    — the shot is a goal, a save, or a miss.
 *
 * The home side receives a small [HOME_ADVANTAGE] boost to its attack and
 * defense. Randomness is injected through [RandomSource], so results are
 * reproducible and can be swapped for a seeded or fake source in tests.
 */
class MatchEngine(
    private val random: RandomSource = KotlinRandomSource(),
    private val logger: MatchLogger = NoOpMatchLogger,
) {

    /** Seeded convenience constructor. */
    constructor(seed: Long, logger: MatchLogger = NoOpMatchLogger) : this(KotlinRandomSource(Random(seed)), logger)

    fun simulate(home: Team, away: Team): MatchResult {
        require(home.clubId != away.clubId) { "home and away must be different clubs" }

        if (logger.isEnabled) {
            logger.onMatchStart(home, away)
        }

        val homeAttack = home.effectiveAttack() * HOME_ADVANTAGE
        val homeDefense = home.effectiveDefense() * HOME_ADVANTAGE
        val awayAttack = away.effectiveAttack().toDouble()
        val awayDefense = away.effectiveDefense().toDouble()

        var homeGoals = 0
        var awayGoals = 0
        var homeShots = 0
        var awayShots = 0
        var homeOnTarget = 0
        var awayOnTarget = 0
        var homePossession = 0
        var awayPossession = 0
        val events = mutableListOf<MatchEvent>()

        for (tick in 0 until TICKS_PER_MATCH) {
            // 1. Possession
            val homePossessionProbability = homeAttack / (homeAttack + awayAttack)
            val homeAttacks = random.nextDouble() < homePossessionProbability
            val attackPower = if (homeAttacks) homeAttack else awayAttack
            val defensePower = if (homeAttacks) awayDefense else homeDefense
            val side = if (homeAttacks) Side.HOME else Side.AWAY

            if (homeAttacks) homePossession++ else awayPossession++

            // 2. Chance
            val chanceProbability = (CHANCE_RATE * attackPower / (attackPower + defensePower))
                .coerceIn(MIN_CHANCE, MAX_CHANCE)
            if (random.nextDouble() >= chanceProbability) continue

            // 3. Shot
            if (random.nextDouble() >= SHOT_CONVERSION) continue
            if (homeAttacks) homeShots++ else awayShots++

            // 4. Outcome: goal / save / miss
            val goalProbability = GOAL_RATE * attackPower / (attackPower + defensePower)
            val saveProbability =
                goalProbability + SAVE_RATE * defensePower / (attackPower + defensePower)
            val roll = random.nextDouble()
            val minute = tick * MINUTES_PER_TICK + random.nextInt(0, MINUTES_PER_TICK) + 1

            when {
                roll < goalProbability -> {
                    if (homeAttacks) {
                        homeGoals++
                        homeOnTarget++
                    } else {
                        awayGoals++
                        awayOnTarget++
                    }
                    val event = MatchEvent(minute, MatchEventType.GOAL, side)
                    events += event
                    if (logger.isEnabled) {
                        logger.onTickEvent(tick, event)
                    }
                }
                roll < saveProbability -> {
                    if (homeAttacks) homeOnTarget++ else awayOnTarget++
                    val event = MatchEvent(minute, MatchEventType.SHOT_SAVED, side)
                    events += event
                    if (logger.isEnabled) {
                        logger.onTickEvent(tick, event)
                    }
                }
                else -> {
                    val event = MatchEvent(minute, MatchEventType.SHOT_MISSED, side)
                    events += event
                    if (logger.isEnabled) {
                        logger.onTickEvent(tick, event)
                    }
                }
            }
        }

        val stats = MatchStats(
            ticks = TICKS_PER_MATCH,
            home = TeamStats(homePossession, homeShots, homeOnTarget, homeGoals),
            away = TeamStats(awayPossession, awayShots, awayOnTarget, awayGoals),
        )
        val result = MatchResult(
            homeClubId = home.clubId,
            awayClubId = away.clubId,
            homeScore = homeGoals,
            awayScore = awayGoals,
            events = events,
            stats = stats,
        )
        if (logger.isEnabled) {
            logger.onMatchEnd(result)
        }
        return result
    }

    companion object {
        const val MINUTES_PER_MATCH = 90
        const val MINUTES_PER_TICK = 5
        const val TICKS_PER_MATCH = MINUTES_PER_MATCH / MINUTES_PER_TICK

        /** Multiplier applied to the home side's attack and defense. */
        const val HOME_ADVANTAGE = 1.05

        const val CHANCE_RATE = 0.9
        const val MIN_CHANCE = 0.03
        const val MAX_CHANCE = 0.75
        const val SHOT_CONVERSION = 0.9
        const val GOAL_RATE = 0.5
        const val SAVE_RATE = 0.4
    }
}
