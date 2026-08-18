package com.footballmanager.simulation.season

import com.footballmanager.simulation.MatchResult
import com.footballmanager.simulation.Team

/**
 * A league table ordered by points, then goal difference, then goals scored.
 * Entries are produced by [compute].
 */
data class Standings(
    val entries: List<StandingEntry>,
) {
    val champion: StandingEntry get() = entries.first()

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
