package com.footballmanager.simulation.season

import com.footballmanager.model.League
import com.footballmanager.seed.SeedData
import com.footballmanager.simulation.Lineup
import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonRunnerConditionTest {

    @Test
    fun `playing a matchday depletes starter fitness and recovers bench fitness`() {
        val game = SeedData.game()
        val league = game.competitions.getValue(SeedData.LEAGUE_ID) as League
        val teams = SeedData.teams(game)
        val runner = SeasonRunner()

        val initialState = runner.start(
            league = league,
            teams = teams,
            startDate = SeedData.START_DATE,
            humanClubId = 1L,
            clubs = game.clubs,
            players = game.players,
        )

        val nextState = runner.playNextMatchday(initialState)

        val club1Squad = game.squad(1L)
        val club1Lineup = Lineup.autoSelect(club1Squad, teams.first { it.clubId == 1L }.tactics)
        val starterId = club1Lineup.starters.first()
        val benchId = club1Lineup.substitutes.first()

        val updatedStarter = nextState.players.getValue(starterId)
        val updatedBench = nextState.players.getValue(benchId)

        assertEquals(88, updatedStarter.fitness) // 100 - 12 = 88
        assertEquals(100, updatedBench.fitness)  // 100 + 18 capped at 100
    }

    @Test
    fun `custom lineup is respected during matchday execution`() {
        val game = SeedData.game()
        val league = game.competitions.getValue(SeedData.LEAGUE_ID) as League
        val teams = SeedData.teams(game)
        val runner = SeasonRunner()

        var state = runner.start(
            league = league,
            teams = teams,
            startDate = SeedData.START_DATE,
            humanClubId = 1L,
            clubs = game.clubs,
            players = game.players,
        )

        val squad = game.squad(1L)
        // Take the last 11 players as custom starters
        val customStarters = squad.takeLast(11).map { it.id }
        val customLineup = Lineup(starters = customStarters, substitutes = squad.dropLast(11).map { it.id })

        state = state.setLineup(1L, customLineup)
        state = runner.playNextMatchday(state)

        for (starterId in customStarters) {
            assertEquals(88, state.players.getValue(starterId).fitness)
        }
    }
}
