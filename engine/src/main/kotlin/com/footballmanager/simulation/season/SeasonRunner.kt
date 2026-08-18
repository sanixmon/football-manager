package com.footballmanager.simulation.season

import com.footballmanager.model.Club
import com.footballmanager.model.League
import com.footballmanager.model.Player
import com.footballmanager.simulation.Lineup
import com.footballmanager.simulation.MatchEngine
import com.footballmanager.simulation.Team
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

            val lineup = state.lineups[clubId] ?: Lineup.autoSelect(squadPlayers, baseTeam.tactics)
            activeLineups[clubId] = lineup
            val starters = lineup.starters.map { state.players.getValue(it) }
            return Team.fromLineup(clubId, starters, baseTeam.tactics)
        }

        val results = matchdayFixtures.map { fixture ->
            val homeTeam = resolveTeam(fixture.home.clubId)
            val awayTeam = resolveTeam(fixture.away.clubId)
            engine.simulate(homeTeam, awayTeam)
        }

        val updatedPlayers = state.players.toMutableMap()
        if (state.players.isNotEmpty()) {
            for (result in results) {
                updateClubCondition(result.homeClubId, result.homeScore, result.awayScore, activeLineups, updatedPlayers)
                updateClubCondition(result.awayClubId, result.awayScore, result.homeScore, activeLineups, updatedPlayers)
            }
        }

        return state.copy(
            results = state.results + results,
            standings = state.standings.withResults(matchdayFixtures, results),
            nextFixtureIndex = end,
            currentDate = matchdayFixtures.last().date,
            players = updatedPlayers,
        )
    }

    private fun updateClubCondition(
        clubId: Long,
        goalsFor: Int,
        goalsAgainst: Int,
        activeLineups: Map<Long, Lineup>,
        playersMap: MutableMap<Long, Player>,
    ) {
        val lineup = activeLineups[clubId] ?: return

        val (starterMoraleDelta, benchMoraleDelta) = when {
            goalsFor > goalsAgainst -> 8 to 4
            goalsFor < goalsAgainst -> -6 to -3
            else -> 0 to 0
        }

        for (starterId in lineup.starters) {
            playersMap[starterId]?.let { p ->
                val newFitness = (p.fitness - 12).coerceIn(1, 100)
                val newMorale = (p.morale + starterMoraleDelta).coerceIn(1, 100)
                playersMap[starterId] = p.copy(fitness = newFitness, morale = newMorale)
            }
        }

        for (benchId in lineup.substitutes) {
            playersMap[benchId]?.let { p ->
                val newFitness = (p.fitness + 18).coerceIn(1, 100)
                val newMorale = if (starterMoraleDelta == 0) {
                    when {
                        p.morale > 50 -> (p.morale - 1).coerceIn(1, 100)
                        p.morale < 50 -> (p.morale + 1).coerceIn(1, 100)
                        else -> 50
                    }
                } else {
                    (p.morale + benchMoraleDelta).coerceIn(1, 100)
                }
                playersMap[benchId] = p.copy(fitness = newFitness, morale = newMorale)
            }
        }
    }
}
