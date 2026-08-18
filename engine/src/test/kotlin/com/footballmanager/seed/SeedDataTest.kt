package com.footballmanager.seed

import com.footballmanager.model.League
import com.footballmanager.simulation.KotlinRandomSource
import com.footballmanager.simulation.MatchEngine
import com.footballmanager.simulation.season.SeasonSimulator
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeedDataTest {

    @Test
    fun `seed world runs a full season end to end`() {
        val game = SeedData.game()
        val league = game.competitions.getValue(SeedData.LEAGUE_ID) as League
        val teams = SeedData.teams(game)

        // world shape
        assertTrue(game.clubs.size >= 8, "expected at least 8 clubs, got ${game.clubs.size}")
        assertEquals(game.clubs.size, teams.size, "one Team per club")
        assertEquals(game.clubs.keys.toSet(), league.clubIds.toSet())

        // every club has a squad of 16-20 players
        for (club in game.clubs.values) {
            val size = game.squad(club.id).size
            assertTrue(size in 16..20, "club ${club.name} has $size players")
        }

        // run the season
        val result = SeasonSimulator(MatchEngine(KotlinRandomSource(Random(42))))
            .simulate(league, teams, SeedData.START_DATE)

        // standings: one entry per club, each team played N-1 matches, champion present
        assertEquals(game.clubs.size, result.standings.entries.size)
        val expectedPlayed = teams.size - 1
        for (entry in result.standings.entries) {
            assertEquals(expectedPlayed, entry.played)
        }
        assertEquals(result.standings.entries.first(), result.champion)
    }
}
