package com.footballmanager.simulation.season

import com.footballmanager.model.League
import com.footballmanager.simulation.MatchEngine
import com.footballmanager.simulation.Team
import java.time.LocalDate

/**
 * Drives a season one matchday at a time instead of simulating it all at once.
 *
 * [start] builds the initial [SeasonState]; each [playNextMatchday] call plays
 * the next round's fixtures through [MatchEngine] and advances the standings.
 * Because the whole state lives in [SeasonState] (not in this runner), a human
 * manager can make decisions between matchdays and resume from a saved snapshot.
 */
class SeasonRunner(
    private val engine: MatchEngine = MatchEngine(),
) {
    fun start(
        league: League,
        teams: List<Team>,
        startDate: LocalDate,
        humanClubId: Long? = null,
    ): SeasonState {
        val fixtures = FixtureGenerator.generate(teams, startDate)
        return SeasonState(
            league = league,
            teams = teams,
            fixtures = fixtures,
            standings = Standings(teams.map { StandingEntry(it) }.sortedWith(Standings.comparator)),
            currentDate = startDate,
            humanClubId = humanClubId,
        )
    }

    /**
     * Plays every fixture of the next matchday and returns the advanced state.
     * Teams are resolved by club id from [SeasonState.teams], so any tactics
     * change applied before the call takes effect for that matchday.
     */
    fun playNextMatchday(state: SeasonState): SeasonState {
        require(!state.isFinished) { "season already finished" }

        val start = state.nextFixtureIndex
        val round = state.fixtures[start].round
        var end = start
        while (end < state.fixtures.size && state.fixtures[end].round == round) end++

        val teamOf = state.teams.associateBy { it.clubId }
        val matchdayFixtures = state.fixtures.subList(start, end)
        val results = matchdayFixtures.map { fixture ->
            engine.simulate(
                teamOf.getValue(fixture.home.clubId),
                teamOf.getValue(fixture.away.clubId),
            )
        }

        return state.copy(
            results = state.results + results,
            standings = state.standings.withResults(matchdayFixtures, results),
            nextFixtureIndex = end,
            currentDate = matchdayFixtures.last().date,
        )
    }
}
