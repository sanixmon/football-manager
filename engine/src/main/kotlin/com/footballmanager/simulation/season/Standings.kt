package com.footballmanager.simulation.season

import com.footballmanager.simulation.MatchResult
import com.footballmanager.simulation.Team
import kotlinx.serialization.Serializable

/**
 * A league table ordered by points, then goal difference, then goals scored.
 * Entries are produced by [compute].
 */
@Serializable
data class Standings(
    val entries: List<StandingEntry>,
) {
    val champion: StandingEntry get() = entries.first()

    /** Applies a single matchday's results to this table, returning the updated table. */
    fun withResults(fixtures: List<Fixture>, results: List<MatchResult>): Standings {
        require(fixtures.size == results.size) {
            "fixtures and results must be aligned (${fixtures.size} vs ${results.size})"
        }
        val byClub = entries.associateBy { it.team.clubId }.toMutableMap()
        for ((fixture, result) in fixtures.zip(results)) {
            byClub[fixture.home.clubId] =
                byClub.getValue(fixture.home.clubId).record(result.homeScore, result.awayScore)
            byClub[fixture.away.clubId] =
                byClub.getValue(fixture.away.clubId).record(result.awayScore, result.homeScore)
        }
        return Standings(byClub.values.sortedWith(Standings.comparator))
    }

    companion object {
        /** Points (desc) → goal difference (desc) → goals for (desc). */
        val comparator: Comparator<StandingEntry> =
            compareByDescending<StandingEntry> { it.points }
                .thenByDescending { it.goalDifference }
                .thenByDescending { it.goalsFor }

        fun compute(
            teams: List<Team>,
            fixtures: List<Fixture>,
            results: List<MatchResult>,
        ): Standings {
            require(fixtures.size == results.size) {
                "fixtures and results must be aligned (${fixtures.size} vs ${results.size})"
            }
            val entries = teams.associate { it.clubId to StandingEntry(it) }.toMutableMap()
            for ((fixture, result) in fixtures.zip(results)) {
                entries[fixture.home.clubId] =
                    entries.getValue(fixture.home.clubId).record(result.homeScore, result.awayScore)
                entries[fixture.away.clubId] =
                    entries.getValue(fixture.away.clubId).record(result.awayScore, result.homeScore)
            }
            return Standings(entries.values.sortedWith(comparator))
        }
    }
}
