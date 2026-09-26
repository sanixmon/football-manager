package com.footballmanager.usecase

import com.footballmanager.simulation.season.SeasonState
import java.time.LocalDate

class AdvanceDayUseCase {

    fun execute(season: SeasonState): SeasonState {
        val nextDate = season.currentDate.plusDays(1)

        val updatedPlayers = season.players.mapValues { (_, player) ->
            var p = player
            // Decrement injury days if any
            if (p.injuryStatus != null) {
                val rem = (p.injuryStatus!!.daysRemaining - 1).coerceAtLeast(0)
                p = if (rem <= 0) p.copy(injuryStatus = null) else p.copy(injuryStatus = p.injuryStatus!!.copy(daysRemaining = rem))
            }
            // Natural daily recovery (+12% fitness for resting players up to 100%)
            if (p.fitness < 100) {
                p = p.copy(fitness = (p.fitness + 12).coerceAtMost(100))
            }
            p
        }

        return season.copy(
            currentDate = nextDate,
            players = updatedPlayers,
        )
    }
}
