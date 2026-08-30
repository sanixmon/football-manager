package com.footballmanager.usecase

import com.footballmanager.calculator.PlayerCalculator
import com.footballmanager.model.Player
import com.footballmanager.simulation.Lineup
import com.footballmanager.simulation.Tactics

class SelectLineupUseCase {
    operator fun invoke(players: List<Player>, tactics: Tactics): Lineup {
        require(players.size >= 11) { "Squad must have at least 11 players to select a lineup (got ${players.size})" }
        val available = players.toMutableList()
        val selectedStarters = mutableListOf<Long>()

        for (slot in tactics.formation.slots) {
            val best = available.maxByOrNull { PlayerCalculator.calculateEffectiveOverall(it, slot) }
                ?: error("No available player for slot $slot")
            selectedStarters.add(best.id)
            available.remove(best)
        }

        val bench = available.sortedByDescending { PlayerCalculator.calculateBestOverall(it) }.map { it.id }
        return Lineup(starters = selectedStarters, substitutes = bench)
    }
}
