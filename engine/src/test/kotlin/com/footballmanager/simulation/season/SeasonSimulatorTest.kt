package com.footballmanager.simulation.season

import com.footballmanager.model.League
import com.footballmanager.simulation.KotlinRandomSource
import com.footballmanager.simulation.MatchEngine
import com.footballmanager.simulation.Team
import java.time.LocalDate
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonSimulatorTest {

    private val league = League(id = 1, name = "Test League")
    private val start = LocalDate.of(2026, 8, 1)

    private fun teams(n: Int): List<Team> = (1..n).map { Team(it.toLong(), 60, 60) }

    private fun season(seed: Int, teams: List<Team>): SeasonResult =
        SeasonSimulator(MatchEngine(KotlinRandomSource(Random(seed)))).simulate(league, teams, start)

    @Test
    fun `every fixture is played exactly once`() {
        val result = season(1, teams(10))
        assertEquals(90, result.fixtures.size)
        assertEquals(result.fixtures.size, result.results.size)
    }

    @Test
    fun `results align with their fixtures`() {
        val result = season(8, teams(6))
        for ((fixture, match) in result.fixtures.zip(result.results)) {
            assertEquals(fixture.home.clubId, match.homeClubId)
            assertEquals(fixture.away.clubId, match.awayClubId)
        }
    }

    @Test
    fun `played equals wins plus draws plus losses`() {
        val result = season(2, teams(10))
        for (entry in result.standings.entries) {
            assertEquals(entry.played, entry.won + entry.drawn + entry.lost)
            assertEquals(18, entry.played, "each of 10 teams plays 18 matches")
        }
    }

    @Test
    fun `total played matches are consistent with the fixture count`() {
        val result = season(12, teams(10))
        assertEquals(2 * result.fixtures.size, result.standings.entries.sumOf { it.played })
    }

    @Test
    fun `goal difference equals goals for minus goals against`() {
        val result = season(3, teams(10))
        for (entry in result.standings.entries) {
            assertEquals(entry.goalsFor - entry.goalsAgainst, entry.goalDifference)
        }
    }

    @Test
    fun `a win is worth 3 points`() {
        val entry = StandingEntry(Team(1, 60, 60)).record(scored = 2, conceded = 1)
        assertEquals(3, entry.points)
        assertEquals(1, entry.won)
    }

    @Test
    fun `a draw is worth 1 point`() {
        val entry = StandingEntry(Team(1, 60, 60)).record(scored = 1, conceded = 1)
        assertEquals(1, entry.points)
        assertEquals(1, entry.drawn)
    }

    @Test
    fun `a loss is worth 0 points`() {
        val entry = StandingEntry(Team(1, 60, 60)).record(scored = 0, conceded = 1)
        assertEquals(0, entry.points)
        assertEquals(1, entry.lost)
    }

    @Test
    fun `points accumulate correctly`() {
        var entry = StandingEntry(Team(1, 60, 60))
        entry = entry.record(2, 0) // win  +3
        entry = entry.record(1, 1) // draw +1
        entry = entry.record(0, 3) // loss +0
        assertEquals(3, entry.played)
        assertEquals(4, entry.points)
        assertEquals(1, entry.won)
        assertEquals(1, entry.drawn)
        assertEquals(1, entry.lost)
    }

    @Test
    fun `standings are sorted by points then goal difference then goals for`() {
        val result = season(4, teams(10))
        val entries = result.standings.entries
        assertEquals(entries.sortedWith(Standings.comparator), entries)
    }

    @Test
    fun `the champion is the first entry with the most points`() {
        val result = season(5, teams(8))
        val first = result.standings.entries.first()
        assertEquals(first, result.champion)
        assertEquals(result.standings.entries.maxOf { it.points }, first.points)
    }

    @Test
    fun `goals for and against are consistent with the played results`() {
        val result = season(6, teams(6))
        val goalsFor = mutableMapOf<Long, Int>()
        val goalsAgainst = mutableMapOf<Long, Int>()
        for ((fixture, match) in result.fixtures.zip(result.results)) {
            goalsFor.merge(fixture.home.clubId, match.homeScore, Int::plus)
            goalsAgainst.merge(fixture.home.clubId, match.awayScore, Int::plus)
            goalsFor.merge(fixture.away.clubId, match.awayScore, Int::plus)
            goalsAgainst.merge(fixture.away.clubId, match.homeScore, Int::plus)
        }
        for (entry in result.standings.entries) {
            assertEquals(goalsFor.getValue(entry.team.clubId), entry.goalsFor)
            assertEquals(goalsAgainst.getValue(entry.team.clubId), entry.goalsAgainst)
        }
    }

    @Test
    fun `a season always produces a champion`() {
        val result = season(7, teams(10))
        assertTrue(result.champion.played > 0)
    }

    @Test
    fun `deterministic random source produces the same season`() {
        val a = SeasonSimulator(MatchEngine(KotlinRandomSource(Random(99))))
            .simulate(league, teams(10), start)
        val b = SeasonSimulator(MatchEngine(KotlinRandomSource(Random(99))))
            .simulate(league, teams(10), start)
        assertEquals(a, b)
    }
}
