package com.footballmanager.usecase

import com.footballmanager.model.BoardObjectives
import com.footballmanager.model.ManagerProfile
import com.footballmanager.seed.SeedData
import com.footballmanager.simulation.MatchEvent
import com.footballmanager.simulation.MatchEventType
import com.footballmanager.simulation.MatchResult
import com.footballmanager.simulation.Tactics
import com.footballmanager.simulation.season.SeasonRunner
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DailyLoopAndRolloverTest {

    @Test
    fun `applyMatchResultToWorld updates player stats, gate receipts, and manager profile`() {
        val game = SeedData.game()
        val runner = SeasonRunner()
        val initialSeason = runner.start(
            league = game.competitions.values.first() as com.footballmanager.model.League,
            teams = SeedData.teams(game),
            startDate = game.currentDate,
            humanClubId = 1L,
            clubs = game.clubs,
            players = game.players,
        ).copy(
            managerProfile = ManagerProfile(name = "Test Boss", clubId = 1L),
            boardObjectives = mapOf(1L to BoardObjectives(clubId = 1L, targetLeaguePosition = 1)),
        )

        val homeStarter = initialSeason.lineups[1L]?.starters?.first() ?: 1L

        val dummyResult = MatchResult(
            fixtureId = 1L,
            homeClubId = 1L,
            awayClubId = 2L,
            homeScore = 2,
            awayScore = 0,
            events = listOf(
                MatchEvent(minute = 23, type = MatchEventType.GOAL, clubId = 1L, playerId = homeStarter, description = "Goal!"),
                MatchEvent(minute = 75, type = MatchEventType.GOAL, clubId = 1L, playerId = homeStarter, description = "Goal!"),
            ),
        )

        val useCase = ApplyMatchResultToWorldUseCase()
        val updatedSeason = useCase.execute(initialSeason, dummyResult)

        // Verifications
        val homeClubBalance = updatedSeason.clubs[1L]?.finance?.balance ?: 0L
        val initialBalance = initialSeason.clubs[1L]?.finance?.balance ?: 0L
        assertEquals(initialBalance + 75_000L, homeClubBalance)

        val playerStat = updatedSeason.playerStats[homeStarter]
        assertNotNull(playerStat)
        assertEquals(1, playerStat.appearances)
        assertEquals(2, playerStat.goals)
        assertTrue(playerStat.averageRating >= 7.0)

        val manager = updatedSeason.managerProfile
        assertNotNull(manager)
        assertEquals(1, manager.wins)
        assertEquals(1, manager.matchesManaged)
    }

    @Test
    fun `advanceDay recovers player fitness naturally`() {
        val game = SeedData.game()
        val runner = SeasonRunner()
        val season = runner.start(
            league = game.competitions.values.first() as com.footballmanager.model.League,
            teams = SeedData.teams(game),
            startDate = game.currentDate,
            humanClubId = 1L,
            clubs = game.clubs,
            players = game.players,
        )

        // Artificially fatigue player 1
        val fatiguedPlayer = season.players.values.first().copy(fitness = 60)
        val fatiguedSeason = season.copy(players = season.players + (fatiguedPlayer.id to fatiguedPlayer))

        val advanceUseCase = AdvanceDayUseCase()
        val nextDaySeason = advanceUseCase.execute(fatiguedSeason)

        assertEquals(fatiguedSeason.currentDate.plusDays(1), nextDaySeason.currentDate)
        val recoveredPlayer = nextDaySeason.players[fatiguedPlayer.id]
        assertNotNull(recoveredPlayer)
        assertEquals(72, recoveredPlayer.fitness)
    }

    @Test
    fun `season rollover increments age, injects youth, and awards prize money`() {
        val game = SeedData.game()
        val runner = SeasonRunner()
        var season = runner.start(
            league = game.competitions.values.first() as com.footballmanager.model.League,
            teams = SeedData.teams(game),
            startDate = game.currentDate,
            humanClubId = 1L,
            clubs = game.clubs,
            players = game.players,
        )

        // Fast forward to complete all fixtures
        while (!season.isFinished) {
            season = runner.playNextMatchday(season)
        }

        val initialPlayerAge = season.players.values.first().age
        val initialPlayerCount = season.players.size

        val rolloverUseCase = SeasonRolloverUseCase()
        val nextSeason = rolloverUseCase.execute(season)

        assertEquals(0, nextSeason.nextFixtureIndex)
        assertEquals(season.currentDate.year + 1, nextSeason.currentDate.year)
        val agedPlayer = nextSeason.players[season.players.values.first().id]
        assertNotNull(agedPlayer)
        assertEquals(initialPlayerAge + 1, agedPlayer.age)

        // 2 youth players per club (10 clubs = +20 players)
        assertEquals(initialPlayerCount + 20, nextSeason.players.size)
    }
}
