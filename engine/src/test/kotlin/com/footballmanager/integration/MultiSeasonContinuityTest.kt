package com.footballmanager.integration

import com.footballmanager.model.Game
import com.footballmanager.model.League
import com.footballmanager.seed.SeedData
import com.footballmanager.serialization.loadFromFile
import com.footballmanager.serialization.saveToFile
import com.footballmanager.simulation.season.SeasonRunner
import com.footballmanager.usecase.SeasonRolloverUseCase
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MultiSeasonContinuityTest {

    @Test
    fun `full season simulation rolls over to season 2 preserving history and serialization integrity`() {
        val game = SeedData.game()
        val runner = SeasonRunner()
        val league = game.competitions.values.first() as League
        val teams = SeedData.teams(game)

        var season = runner.start(
            league = league,
            teams = teams,
            startDate = game.currentDate,
            humanClubId = 1L,
            clubs = game.clubs,
            players = game.players,
        )

        // 1. Simulate all 18 matchdays of Season 1
        while (!season.isFinished) {
            season = runner.playNextMatchday(season)
        }

        assertTrue(season.isFinished)
        assertEquals(90, season.results.size)
        val champion1 = season.standings.champion
        assertNotNull(champion1)

        // 2. Execute Season Rollover
        val rolloverUseCase = SeasonRolloverUseCase()
        val rolloverResult = rolloverUseCase.executeWithResult(season)
        val season2 = rolloverResult.nextSeason
        val completedSeason1Result = rolloverResult.completedSeasonResult

        assertEquals(0, season2.nextFixtureIndex)
        assertEquals(season.currentDate.year + 1, season2.currentDate.year)
        assertEquals(90, season2.fixtures.size)

        // 3. Assemble Season 2 Game state with archived lastSeason
        val gameSeason2 = game.copy(
            clubs = season2.clubs,
            players = season2.players,
            currentSeason = season2,
            currentDate = season2.currentDate,
            lastSeason = completedSeason1Result,
        )

        assertEquals(champion1.team.clubId, gameSeason2.lastSeason?.champion?.team?.clubId)

        // 4. Test Persistence Round-Trip of Multi-Season Game
        val tempFile = File.createTempFile("multiseason_test", ".json")
        try {
            gameSeason2.saveToFile(tempFile.absolutePath)
            val reloaded = Game.loadFromFile(tempFile.absolutePath)

            val currentSeason = reloaded.currentSeason
            assertNotNull(currentSeason)
            assertEquals(0, currentSeason.nextFixtureIndex)
            assertEquals(90, currentSeason.fixtures.size)

            val lastSeason = reloaded.lastSeason
            assertNotNull(lastSeason)
            assertEquals(champion1.team.clubId, lastSeason.champion.team.clubId)
            assertEquals(90, lastSeason.results.size)
            assertEquals(10, lastSeason.standings.entries.size)

            // 5. Play Matchdays in Season 2 from Reloaded Game
            var season2Played = currentSeason
            repeat(3) {
                season2Played = runner.playNextMatchday(season2Played)
            }

            assertEquals(15, season2Played.nextFixtureIndex) // 3 rounds x 5 fixtures = 15
            assertEquals(15, season2Played.results.size)
            assertEquals(10, season2Played.standings.entries.size)

            // Verify standings points match results played
            val totalWins = season2Played.standings.entries.sumOf { it.won }
            val totalLosses = season2Played.standings.entries.sumOf { it.lost }
            assertEquals(totalWins, totalLosses)
        } finally {
            tempFile.delete()
        }
    }
}
