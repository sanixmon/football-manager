package com.footballmanager.usecase

import com.footballmanager.simulation.season.SeasonState
import java.time.LocalDate

class AdvanceDayUseCase {

    fun execute(season: SeasonState): SeasonState {
        val nextDate = season.currentDate.plusDays(1)

        // Natural daily recovery (+12% fitness for resting players up to 100%)
        val recoveredPlayers = season.players.mapValues { (_, player) ->
            if (player.fitness < 100) {
                player.copy(fitness = (player.fitness + 12).coerceAtMost(100))
            } else {
                player
            }
        }

        return season.copy(
            currentDate = nextDate,
            players = recoveredPlayers,
        )
    }
}
