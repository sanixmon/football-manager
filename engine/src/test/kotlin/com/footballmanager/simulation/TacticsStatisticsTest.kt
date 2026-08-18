package com.footballmanager.simulation

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertTrue

class TacticsStatisticsTest {

    private val matches = 10_000

    @Test
    fun `attacking mentality scores and concedes more than defensive mentality`() {
        val engine = MatchEngine(KotlinRandomSource(Random(2026)))
        val balanced = Team(3, 70, 70)

        val attacking = averageGoals(
            engine,
            subject = Team(1, 70, 70, Tactics(Formation.FOUR_FOUR_TWO, Mentality.ATTACKING)),
            opponent = balanced,
        )
        val defensive = averageGoals(
            engine,
            subject = Team(2, 70, 70, Tactics(Formation.FOUR_FOUR_TWO, Mentality.DEFENSIVE)),
            opponent = balanced,
        )

        println(
            "MENTALITY attacking scored=%.3f conceded=%.3f | defensive scored=%.3f conceded=%.3f"
                .format(attacking.scored, attacking.conceded, defensive.scored, defensive.conceded),
        )

        assertTrue(attacking.scored > defensive.scored, "attacking should score more")
        assertTrue(attacking.conceded > defensive.conceded, "attacking should concede more")
    }

    @Test
    fun `attacking formation scores and concedes more than defensive formation`() {
        val engine = MatchEngine(KotlinRandomSource(Random(7)))
        val balanced = Team(3, 70, 70)

        val attacking = averageGoals(
            engine,
            subject = Team(1, 70, 70, Tactics(Formation.FOUR_THREE_THREE, Mentality.BALANCED)),
            opponent = balanced,
        )
        val defensive = averageGoals(
            engine,
            subject = Team(2, 70, 70, Tactics(Formation.FIVE_THREE_TWO, Mentality.BALANCED)),
            opponent = balanced,
        )

        println(
            "FORMATION 4-3-3 scored=%.3f conceded=%.3f | 5-3-2 scored=%.3f conceded=%.3f"
                .format(attacking.scored, attacking.conceded, defensive.scored, defensive.conceded),
        )

        assertTrue(attacking.scored > defensive.scored, "4-3-3 should score more")
        assertTrue(attacking.conceded > defensive.conceded, "4-3-3 should concede more")
    }

    /**
     * Averages goals scored/conceded by [subject] when it is home against a fixed
     * [opponent]. Comparing two subjects against the same opponent isolates the
     * tactic effect (a head-to-head of symmetric modifiers would cancel out).
     */
    private fun averageGoals(engine: MatchEngine, subject: Team, opponent: Team): GoalAverages {
        var scored = 0
        var conceded = 0
        repeat(matches) {
            val result = engine.simulate(subject, opponent)
            scored += result.homeScore
            conceded += result.awayScore
        }
        return GoalAverages(scored.toDouble() / matches, conceded.toDouble() / matches)
    }

    private data class GoalAverages(val scored: Double, val conceded: Double)
}
