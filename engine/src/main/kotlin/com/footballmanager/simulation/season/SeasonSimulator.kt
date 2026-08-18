package com.footballmanager.simulation.season

import com.footballmanager.model.League
import com.footballmanager.simulation.MatchEngine
import com.footballmanager.simulation.Team
import java.time.LocalDate

/**
 * Simulates a full league season: generates fixtures, plays each match through
 * [MatchEngine], and compiles the final standings.
 */
class SeasonSimulator(
    private val engine: MatchEngine = MatchEngine(),
) {
    fun simulate(league: League, teams: List<Team>, startDate: LocalDate): SeasonResult {
        val fixtures = FixtureGenerator.generate(teams, startDate)
        val results = fixtures.map { engine.simulate(it.home, it.away) }
        val standings = Standings.compute(teams, fixtures, results)
        return SeasonResult(league, fixtures, results, standings)
    }
}
