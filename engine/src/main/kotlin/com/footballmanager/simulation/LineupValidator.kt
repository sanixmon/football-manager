package com.footballmanager.simulation

import com.footballmanager.model.Player
import com.footballmanager.model.Position

sealed class LineupValidationResult {
    data object Valid : LineupValidationResult()
    data class Invalid(val errors: List<String>) : LineupValidationResult() {
        val message: String get() = errors.joinToString("; ")
    }
}

object LineupValidator {

    fun validate(
        lineup: Lineup,
        players: Map<Long, Player>,
    ): LineupValidationResult {
        val errors = mutableListOf<String>()

        // 1. Exactly 11 starters
        if (lineup.starters.size != 11) {
            errors.add("Lineup must have exactly 11 starters (got ${lineup.starters.size})")
        }

        // 2. Starters must be unique
        if (lineup.starters.distinct().size != lineup.starters.size) {
            errors.add("Starters must be unique players")
        }

        // 3. Substitutes must be unique
        if (lineup.substitutes.distinct().size != lineup.substitutes.size) {
            errors.add("Substitutes must be unique players")
        }

        // 4. Overlap between starters and substitutes
        val overlap = lineup.starters.intersect(lineup.substitutes.toSet())
        if (overlap.isNotEmpty()) {
            errors.add("Players cannot be both starter and substitute: $overlap")
        }

        // 5. Starters checks
        for ((index, playerId) in lineup.starters.withIndex()) {
            val player = players[playerId]
            if (player == null) {
                errors.add("Starter with id $playerId not found in player map")
                continue
            }

            // Slot 0 must be a natural GK
            if (index == 0 && Position.GK !in player.naturalPositions) {
                errors.add("Slot 0 must be occupied by a natural GK (got ${player.name} with positions ${player.naturalPositions})")
            }

            // Injured starter
            if (player.isInjured) {
                errors.add("Injured player ${player.name} (id=$playerId) cannot start")
            }

            // Suspended starter
            if (player.isSuspended) {
                errors.add("Suspended player ${player.name} (id=$playerId) cannot start")
            }
        }

        // 6. Substitutes checks
        for (subId in lineup.substitutes) {
            val sub = players[subId]
            if (sub == null) {
                errors.add("Substitute with id $subId not found in player map")
                continue
            }
            if (sub.isInjured) {
                errors.add("Injured player ${sub.name} (id=$subId) cannot be on the bench")
            }
            if (sub.isSuspended) {
                errors.add("Suspended player ${sub.name} (id=$subId) cannot be on the bench")
            }
        }

        return if (errors.isEmpty()) LineupValidationResult.Valid else LineupValidationResult.Invalid(errors)
    }

    fun requireValid(lineup: Lineup, players: Map<Long, Player>) {
        when (val result = validate(lineup, players)) {
            is LineupValidationResult.Valid -> Unit
            is LineupValidationResult.Invalid -> throw IllegalArgumentException("Invalid lineup: ${result.message}")
        }
    }
}
