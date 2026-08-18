package com.footballmanager.simulation.season

import com.footballmanager.simulation.Team
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FixtureGeneratorTest {

    private val start = LocalDate.of(2026, 8, 1)

    private fun teams(n: Int): List<Team> = (1..n).map { Team(it.toLong(), 60, 60) }

    @Test
    fun `4 teams produce 6 fixtures`() {
        assertEquals(6, FixtureGenerator.generate(teams(4), start).size)
    }

    @Test
    fun `10 teams produce 45 fixtures`() {
        assertEquals(45, FixtureGenerator.generate(teams(10), start).size)
    }

    @Test
    fun `odd team counts are handled with a bye`() {
        assertEquals(3, FixtureGenerator.generate(teams(3), start).size)
    }

    @Test
    fun `no team plays itself`() {
        val fixtures = FixtureGenerator.generate(teams(10), start)
        assertTrue(fixtures.all { it.home.clubId != it.away.clubId })
    }

    @Test
    fun `every pair of teams meets exactly once`() {
        val n = 8
        val fixtures = FixtureGenerator.generate(teams(n), start)
        assertEquals(n * (n - 1) / 2, fixtures.size)
        val pairs = fixtures.map { setOf(it.home.clubId, it.away.clubId) }
        assertEquals(pairs.size, pairs.distinct().size, "a pair meets more than once")
    }

    @Test
    fun `no team plays twice in the same round`() {
        val fixtures = FixtureGenerator.generate(teams(10), start)
        for ((round, roundFixtures) in fixtures.groupBy { it.round }) {
            val participants = roundFixtures.flatMap { listOf(it.home.clubId, it.away.clubId) }
            assertEquals(
                participants.size,
                participants.distinct().size,
                "duplicate team in round $round",
            )
        }
    }

    @Test
    fun `every fixture has a date and each round shares one date`() {
        val fixtures = FixtureGenerator.generate(teams(6), start)
        assertTrue(fixtures.all { it.date >= start })
        val datesPerRound = fixtures.groupBy { it.round }.mapValues { (_, roundFixtures) ->
            roundFixtures.map { it.date }.toSet()
        }
        datesPerRound.values.forEach { dates ->
            assertEquals(1, dates.size, "a round must share a single date")
        }
    }
}
