package com.footballmanager.usecase

import com.footballmanager.calculator.PlayerDevelopmentCalculator
import com.footballmanager.model.Facilities
import com.footballmanager.model.Player
import com.footballmanager.model.TrainingFocus
import com.footballmanager.model.TrainingIntensity
import com.footballmanager.simulation.season.SeasonState

class PlayerDevelopmentUseCase {

    /**
     * Updates an individual player's training regime.
     */
    fun setPlayerTraining(
        season: SeasonState,
        playerId: Long,
        focus: TrainingFocus,
        intensity: TrainingIntensity = TrainingIntensity.NORMAL,
    ): SeasonState {
        val player = season.players[playerId] ?: return season
        val updatedPlayer = player.copy(
            trainingFocus = focus,
            trainingIntensity = intensity,
        )
        return season.copy(
            players = season.players + (playerId to updatedPlayer),
        )
    }

    /**
     * Updates the training focus and intensity for an entire squad.
     */
    fun setClubSquadTraining(
        season: SeasonState,
        clubId: Long,
        focus: TrainingFocus,
        intensity: TrainingIntensity = TrainingIntensity.NORMAL,
    ): SeasonState {
        val club = season.clubs[clubId] ?: return season
        val updatedPlayers = season.players.toMutableMap()
        for (playerId in club.squad.playerIds) {
            val player = updatedPlayers[playerId] ?: continue
            // Goalkeepers retain GOALKEEPING focus unless explicitly changed
            val targetFocus = if (player.naturalPositions.any { it.name == "GK" } && focus != TrainingFocus.BALANCED) {
                TrainingFocus.GOALKEEPING
            } else {
                focus
            }
            updatedPlayers[playerId] = player.copy(
                trainingFocus = targetFocus,
                trainingIntensity = intensity,
            )
        }
        return season.copy(players = updatedPlayers)
    }

    /**
     * Applies annual player development across all clubs and players in the season.
     */
    fun executeAnnualDevelopment(season: SeasonState): SeasonState {
        val updatedPlayers = season.players.mapValues { (_, player) ->
            val club = season.clubs.values.firstOrNull { it.squad.contains(player.id) }
            val facilities = club?.facilities ?: Facilities()
            val stats = season.playerStats[player.id]
            PlayerDevelopmentCalculator.applyDevelopment(
                player = player,
                facilities = facilities,
                seasonStats = stats,
            )
        }
        return season.copy(players = updatedPlayers)
    }
}
