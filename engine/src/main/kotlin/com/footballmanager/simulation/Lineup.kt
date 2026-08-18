package com.footballmanager.simulation

import com.footballmanager.model.Player
import kotlinx.serialization.Serializable

@Serializable
data class Lineup(
    val starters: List<Long>,
    val substitutes: List<Long> = emptyList(),
) {
    init {
        require(starters.size == 11) { "Lineup must have exactly 11 starters (got ${starters.size})" }
        require(starters.distinct().size == 11) { "Starters must be unique players" }
    }

    companion object {
        fun autoSelect(players: List<Player>, tactics: Tactics): Lineup {
            require(players.size >= 11) { "Squad must have at least 11 players to select a lineup (got ${players.size})" }
            val available = players.toMutableList()
            val selectedStarters = mutableListOf<Long>()

            for (slot in tactics.formation.slots) {
                val best = available.maxByOrNull { it.effectiveOverall(slot) }
                    ?: error("No available player for slot $slot")
                selectedStarters.add(best.id)
                available.remove(best)
            }

            val bench = available.sortedByDescending { it.bestOverall() }.map { it.id }
            return Lineup(starters = selectedStarters, substitutes = bench)
        }
    }
}
