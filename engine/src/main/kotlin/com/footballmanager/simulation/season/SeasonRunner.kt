package com.footballmanager.simulation.season

import com.footballmanager.model.Club
import com.footballmanager.model.League
import com.footballmanager.model.Player
import com.footballmanager.simulation.Lineup
import com.footballmanager.simulation.MatchEngine
import com.footballmanager.simulation.Team
import com.footballmanager.usecase.ApplyMatchResultToWorldUseCase
import java.time.LocalDate

/**
 * Drives a season one matchday at a time instead of simulating it all at once.
 *
 * [start] builds the initial [SeasonState]; each [playNextMatchday] call plays
 * the next round's fixtures through [MatchEngine] and advances the standings.
 * When [players] and [clubs] are tracked in state, starting lineups are resolved,
 * matchday team strength reflects player condition, and fitness and morale evolve
 * across rounds.
 */
class SeasonRunner(
    private val engine: MatchEngine = MatchEngine(),
    private val applyMatchResultToWorldUseCase: ApplyMatchResultToWorldUseCase = ApplyMatchResultToWorldUseCase(),
) {
    fun start(
        league: League,
        teams: List<Team>,
        startDate: LocalDate,
        humanClubId: Long? = null,
        clubs: Map<Long, Club> = emptyMap(),
        players: Map<Long, Player> = emptyMap(),
        lineups: Map<Long, Lineup> = emptyMap(),
    ): SeasonState {
        val fixtures = FixtureGenerator.generate(teams, startDate)
        return SeasonState(
            league = league,
            teams = teams,
            fixtures = fixtures,
            standings = Standings(teams.map { StandingEntry(it) }.sortedWith(Standings.comparator)),
            currentDate = startDate,
            humanClubId = humanClubId,
            clubs = clubs,
            players = players,
            lineups = lineups,
        )
    }

    /**
     * Plays every fixture of the next matchday and returns the advanced state.
     * Teams are resolved by club id; when player condition is tracked, starting
     * lineups and post-match fitness/morale adjustments take effect for that matchday.
     */
    fun playNextMatchday(state: SeasonState): SeasonState {
        require(!state.isFinished) { "season already finished" }

        val start = state.nextFixtureIndex
        val round = state.fixtures[start].round
        var end = start
        while (end < state.fixtures.size && state.fixtures[end].round == round) end++

        val teamOf = state.teams.associateBy { it.clubId }
        val matchdayFixtures = state.fixtures.subList(start, end)
        val activeLineups = mutableMapOf<Long, Lineup>()

        fun resolveTeam(clubId: Long): Team {
            val baseTeam = teamOf.getValue(clubId)
            if (state.players.isEmpty()) return baseTeam

            val clubPlayerIds = state.clubs[clubId]?.squad?.playerIds
                ?: state.players.values.filter { it.contract.squadStatus != null }.map { it.id }
            val squadPlayers = clubPlayerIds.mapNotNull { state.players[it] }

            if (squadPlayers.size < 11) return baseTeam

            val lineup = state.lineups[clubId] ?: Lineup.autoSelect(squadPlayers, baseTeam.tactics, clubId = clubId)
            activeLineups[clubId] = lineup
            val starters = lineup.starters.map { state.players[it] ?: com.footballmanager.model.EmergencyGoalkeeper.create(clubId, it) }
            val subs = lineup.substitutes.mapNotNull { state.players[it] }
            return Team.fromLineup(clubId, starters, baseTeam.tactics, substitutes = subs)
        }

        val results = matchdayFixtures.map { fixture ->
            val homeTeam = resolveTeam(fixture.home.clubId)
            val awayTeam = resolveTeam(fixture.away.clubId)
            engine.simulate(homeTeam, awayTeam)
        }

        var progressedState = state.copy(
            results = state.results + results,
            standings = state.standings.withResults(matchdayFixtures, results),
            nextFixtureIndex = end,
            currentDate = matchdayFixtures.last().date,
        )

        if (state.players.isNotEmpty()) {
            for (result in results) {
                progressedState = applyMatchResultToWorldUseCase.execute(progressedState, result)
            }
        }

        return progressedState
    }
}
