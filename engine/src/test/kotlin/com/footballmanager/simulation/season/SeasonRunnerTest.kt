package com.footballmanager.simulation.season

import com.footballmanager.model.League
import com.footballmanager.seed.SeedData
import com.footballmanager.simulation.Formation
import com.footballmanager.simulation.KotlinRandomSource
import com.footballmanager.simulation.MatchEngine
import com.footballmanager.simulation.Mentality
import com.footballmanager.simulation.Tactics
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SeasonRunnerTest {

    private fun newGame(): Pair<com.footballmanager.model.Game, League> {
        val game = SeedData.game()
        return game to (game.competitions.getValue(SeedData.LEAGUE_ID) as League)
    }

    @Test
    fun `advances one matchday at a time`() {
        val (game, league) = newGame()
        val teams = SeedData.teams(game)
        val runner = SeasonRunner(MatchEngine(KotlinRandomSource(Random(42))))

        val state = runner.start(league, teams, SeedData.START_DATE)
        assertFalse(state.isFinished)
        assertEquals(0, state.nextFixtureIndex)
        assertEquals(1, state.nextMatchday)
        assertEquals(0, state.results.size)

        val fixturesPerRound = state.fixtures.count { it.round == 1 }
        val afterOne = runner.playNextMatchday(state)
        assertEquals(fixturesPerRound, afterOne.results.size)
        assertEquals(fixturesPerRound, afterOne.nextFixtureIndex)
        assertEquals(2, afterOne.nextMatchday)
    }

    @Test
    fun `plays every matchday to completion`() {
        val (game, league) = newGame()
        val teams = SeedData.teams(game)
        val runner = SeasonRunner(MatchEngine(KotlinRandomSource(Random(42))))

        var state = runner.start(league, teams, SeedData.START_DATE)
        var matchdays = 0
        while (!state.isFinished) {
            state = runner.playNextMatchday(state)
            matchdays++
        }

        assertTrue(state.isFinished)
        assertNull(state.nextMatchday)
        assertEquals(state.fixtures.size, state.results.size)
        assertEquals(teams.size, state.standings.entries.size)
        assertEquals(2 * (teams.size - 1), state.standings.entries.first().played)
    }

    @Test
    fun `matchday-by-matchday matches a one-shot season`() {
        val (game, league) = newGame()
        val teams = SeedData.teams(game)

        val oneShot = SeasonSimulator(MatchEngine(KotlinRandomSource(Random(42))))
            .simulate(league, teams, SeedData.START_DATE)

        val runner = SeasonRunner(MatchEngine(KotlinRandomSource(Random(42))))
        var state = runner.start(league, teams, SeedData.START_DATE)
        while (!state.isFinished) state = runner.playNextMatchday(state)

        assertEquals(oneShot.fixtures.size, state.results.size)
        assertEquals(oneShot.standings, state.standings)
    }

    @Test
    fun `setTactics updates the team and its standing row`() {
        val (game, league) = newGame()
        val teams = SeedData.teams(game)
        val runner = SeasonRunner(MatchEngine(KotlinRandomSource(Random(42))))
        val state = runner.start(league, teams, SeedData.START_DATE, humanClubId = 1L)

        val attacking = Tactics(Formation.FOUR_THREE_THREE, Mentality.ATTACKING)
        val updated = state.setTactics(1L, attacking)

        assertEquals(attacking, updated.teams.first { it.clubId == 1L }.tactics)
        assertEquals(attacking, updated.standings.entries.first { it.team.clubId == 1L }.team.tactics)
        // other clubs untouched
        assertEquals(
            state.teams.first { it.clubId == 2L }.tactics,
            updated.teams.first { it.clubId == 2L }.tactics,
        )
    }
}
