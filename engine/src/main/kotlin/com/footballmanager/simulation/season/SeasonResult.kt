package com.footballmanager.simulation.season

import com.footballmanager.model.League
import com.footballmanager.simulation.MatchResult
import kotlinx.serialization.Serializable

/**
 * The outcome of a simulated season: the fixtures in round order, the
 * [MatchResult] for each fixture (aligned by index), and the final [Standings].
 */
@Serializable
data class SeasonResult(
    val league: League,
    val fixtures: List<Fixture>,
    val results: List<MatchResult>,
    val standings: Standings,
) {
    init {
        require(fixtures.size == results.size) {
            "fixtures and results must be aligned (${fixtures.size} vs ${results.size})"
        }
    }

    val champion: StandingEntry get() = standings.champion
}
