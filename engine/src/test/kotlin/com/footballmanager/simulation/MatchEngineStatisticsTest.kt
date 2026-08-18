package com.footballmanager.simulation

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertTrue

class MatchEngineStatisticsTest {

    private val matches = 10_000

    @Test
    fun `a stronger team wins more often than a weaker team`() {
        val engine = MatchEngine(KotlinRandomSource(Random(1234)))
        val strong = Team(1, attack = 80, defense = 80)
        val weak = Team(2, attack = 55, defense = 55)

        var strongWins = 0
        var weakWins = 0
        repeat(matches) { i ->
            val strongAtHome = i % 2 == 0
            val result = if (strongAtHome) {
                engine.simulate(strong, weak)
            } else {
                engine.simulate(weak, strong)
            }
            val strongWon = if (strongAtHome) result.isHomeWin else result.isAwayWin
            val weakWon = if (strongAtHome) result.isAwayWin else result.isHomeWin
            if (strongWon) strongWins++
            if (weakWon) weakWins++
        }

        assertTrue(strongWins > weakWins, "strong=$strongWins weak=$weakWins")
    }

    @Test
    fun `home advantage tilts results toward the home side`() {
        val engine = MatchEngine(KotlinRandomSource(Random(99)))
        val a = Team(1, 70, 70)
        val b = Team(2, 70, 70)

        var homeWins = 0
        var awayWins = 0
        repeat(matches) { i ->
            val result = if (i % 2 == 0) engine.simulate(a, b) else engine.simulate(b, a)
            if (result.isHomeWin) homeWins++
            if (result.isAwayWin) awayWins++
        }

        assertTrue(homeWins > awayWins, "home=$homeWins away=$awayWins")
    }

    @Test
    fun `draws are possible between evenly matched teams`() {
        val engine = MatchEngine(KotlinRandomSource(Random(7)))
        val a = Team(1, 70, 70)
        val b = Team(2, 70, 70)

        var draws = 0
        repeat(matches) {
            if (engine.simulate(a, b).isDraw) draws++
        }

        assertTrue(draws > 0, "no draws in $matches matches")
    }
}
