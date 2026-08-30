package com.footballmanager.benchmark

import com.footballmanager.model.League
import com.footballmanager.seed.SeedData
import com.footballmanager.simulation.season.SeasonSimulator
import com.footballmanager.usecase.MatchRequest
import com.footballmanager.usecase.SimulateMatchUseCase
import java.time.LocalDate
import kotlin.system.measureTimeMillis
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SimulationPerformanceBenchmarkTest {

    @Test
    fun `SimulateMatchUseCase achieves high throughput on batch simulation`() {
        val game = SeedData.game()
        val teams = SeedData.teams(game)
        val useCase = SimulateMatchUseCase()

        val requests = mutableListOf<MatchRequest>()
        for (i in 0 until 500) {
            val home = teams[i % teams.size]
            val away = teams[(i + 1) % teams.size]
            requests.add(MatchRequest(homeTeam = home, awayTeam = away, seed = i.toLong()))
        }

        // Warmup
        useCase.simulateBatch(requests.take(50))

        // Measure
        val elapsedMs = measureTimeMillis {
            val results = useCase.simulateBatch(requests)
            assertEquals(500, results.size)
        }

        // 500 matches should easily execute in under 1500ms on any modern JVM
        assertTrue(elapsedMs < 1500, "500 match simulations took ${elapsedMs}ms (expected < 1500ms)")
    }

    @Test
    fun `full season simulation of 90 matches completes within performance threshold`() {
        val game = SeedData.game()
        val league = game.competitions.getValue(SeedData.LEAGUE_ID) as League
        val teams = SeedData.teams(game)
        val simulator = SeasonSimulator()

        // Warmup
        simulator.simulate(league, teams, LocalDate.of(2026, 8, 1))

        // Measure 10 full seasons (900 matches + standings calculations)
        val elapsedMs = measureTimeMillis {
            for (i in 0 until 10) {
                val result = simulator.simulate(league, teams, LocalDate.of(2026, 8, 1))
                assertEquals(90, result.fixtures.size)
                assertEquals(10, result.standings.entries.size)
            }
        }

        // 10 full seasons (900 matches) should complete well under 2500ms
        assertTrue(elapsedMs < 2500, "10 full seasons took ${elapsedMs}ms (expected < 2500ms)")
    }
}
