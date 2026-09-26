package com.footballmanager.usecase

import com.footballmanager.calculator.PlayerCalculator
import com.footballmanager.model.EmergencyGoalkeeper
import com.footballmanager.model.Player
import com.footballmanager.model.Position
import com.footballmanager.simulation.Lineup
import com.footballmanager.simulation.LineupValidator
import com.footballmanager.simulation.Tactics

class SelectLineupUseCase {
    operator fun invoke(
        players: List<Player>,
        tactics: Tactics,
        allowEmergencyGk: Boolean = true,
        clubId: Long = 0L,
    ): Lineup {
        // 1. Filter out unavailable players (injured or suspended)
        val available = players.filter { it.isAvailable }.toMutableList()

        // 2. Select starting GK
        val availableGks = available.filter { Position.GK in it.naturalPositions }
        val startingGk: Player = if (availableGks.isNotEmpty()) {
            availableGks.maxWithOrNull(
                compareBy<Player> { PlayerCalculator.calculateEffectiveOverall(it, Position.GK) }
                    .thenBy { it.id },
            )!!
        } else if (allowEmergencyGk) {
            val emergencyGk = EmergencyGoalkeeper.create(clubId = clubId)
            available.add(emergencyGk)
            emergencyGk
        } else {
            throw IllegalStateException("No eligible goalkeeper available in squad")
        }

        available.remove(startingGk)
        val selectedStarters = mutableListOf(startingGk.id)

        // 3. Select remaining 10 outfield starters according to formation slots
        val outfieldSlots = tactics.formation.slots.drop(1)
        for (slot in outfieldSlots) {
            val best = available.maxWithOrNull(
                compareBy<Player> { slot in it.naturalPositions }
                    .thenBy { PlayerCalculator.calculateEffectiveOverall(it, slot) }
                    .thenBy { it.id },
            ) ?: throw IllegalArgumentException("Squad has fewer than 11 eligible players (needed 11, only have ${selectedStarters.size})")

            selectedStarters.add(best.id)
            available.remove(best)
        }

        // 4. Bench substitutes from remaining eligible players sorted deterministically
        val bench = available
            .sortedWith(compareByDescending<Player> { PlayerCalculator.calculateBestOverall(it) }.thenBy { it.id })
            .map { it.id }

        val lineup = Lineup(starters = selectedStarters, substitutes = bench)

        val playerLookup = (players + if (startingGk.id < 0) listOf(startingGk) else emptyList()).associateBy { it.id }
        LineupValidator.requireValid(lineup, playerLookup)

        return lineup
    }
}
