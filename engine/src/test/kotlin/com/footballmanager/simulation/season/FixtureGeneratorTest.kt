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
    fun `4 teams produce 12 fixtures`() {
        assertEquals(12, FixtureGenerator.generate(teams(4), start).size)
    }

    @Test
    fun `10 teams produce 90 fixtures`() {
        assertEquals(90, FixtureGenerator.generate(teams(10), start).size)
    }

    @Test
    fun `odd team counts are handled with a bye`() {
        assertEquals(6, FixtureGenerator.generate(teams(3), start).size)
    }

    @Test
    fun `no team plays itself`() {
        val fixtures = FixtureGenerator.generate(teams(10), start)
        assertTrue(fixtures.all { it.home.clubId != it.away.clubId })
    }

    @Test
    fun `every pair of teams meets exactly twice, once at each venue`() {
        val n = 8
        val fixtures = FixtureGenerator.generate(teams(n), start)
        assertEquals(n * (n - 1), fixtures.size)

        // each ordered (home, away) pair appears exactly once
        val ordered = fixtures.map { it.home.clubId to it.away.clubId }
        assertEquals(ordered.size, ordered.distinct().size, "a venue pairing repeats")

        // each unordered pair therefore appears exactly twice
        val unordered = fixtures.map { setOf(it.home.clubId, it.away.clubId) }
        assertTrue(
            unordered.groupingBy { it }.eachCount().values.all { it == 2 },
            "every pair must meet exactly twice",
        )
    }

    @Test
    fun `each team plays the same number of home and away games`() {
        val n = 6
        val fixtures = FixtureGenerator.generate(teams(n), start)
        val homeCounts = fixtures.groupingBy { it.home.clubId }.eachCount()
        val awayCounts = fixtures.groupingBy { it.away.clubId }.eachCount()
        for (team in teams(n)) {
            assertEquals(n - 1, homeCounts[team.clubId], "home count for club ${team.clubId}")
            assertEquals(n - 1, awayCounts[team.clubId], "away count for club ${team.clubId}")
        }
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
