package com.footballmanager.usecase

import com.footballmanager.logging.MatchLogger
import com.footballmanager.logging.NoOpMatchLogger
import com.footballmanager.simulation.MatchEngine
import com.footballmanager.simulation.MatchResult
import com.footballmanager.simulation.RandomSource
import com.footballmanager.simulation.Team

data class MatchRequest(
    val homeTeam: Team,
    val awayTeam: Team,
    val seed: Long? = null,
)

class SimulateMatchUseCase(
    private val randomSource: RandomSource? = null,
    private val logger: MatchLogger = NoOpMatchLogger,
) {
    operator fun invoke(request: MatchRequest): MatchResult {
        val engine = if (request.seed != null) {
            MatchEngine(request.seed, logger = logger)
        } else if (randomSource != null) {
            MatchEngine(randomSource, logger = logger)
        } else {
            MatchEngine(logger = logger)
        }
        return engine.simulate(request.homeTeam, request.awayTeam)
    }

    /** Batch simulation of multiple fixtures in a round or tournament. */
    fun simulateBatch(requests: List<MatchRequest>): List<MatchResult> =
        requests.map { invoke(it) }
}
