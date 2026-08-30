package com.footballmanager.calculator

import com.footballmanager.model.MAX_ATTRIBUTE
import com.footballmanager.model.MIN_ATTRIBUTE
import com.footballmanager.model.Player
import com.footballmanager.model.Position
import kotlin.math.roundToInt

/**
 * Pure calculation engine for Player ratings, fitness/morale scaling, and positional optimization.
 */
object PlayerCalculator {

    /** Overall rating when played in [position], modified by fitness and morale. */
    fun calculateEffectiveOverall(player: Player, position: Position): Int {
        val base = player.overall(position)
        val fitnessFactor = 0.70 + 0.30 * (player.fitness.coerceIn(0, 100) / 100.0)
        val moraleFactor = 0.90 + 0.20 * (player.morale.coerceIn(0, 100) / 100.0)
        return (base * fitnessFactor * moraleFactor).roundToInt().coerceIn(MIN_ATTRIBUTE, MAX_ATTRIBUTE)
    }

    /** The natural position where this player rates highest based on attributes. */
    fun findBestPosition(player: Player): Position {
        return player.naturalPositions.maxByOrNull { player.overall(it) }
            ?: error("player must have at least one natural position")
    }

    /** Overall rating at the player's best natural position. */
    fun calculateBestOverall(player: Player): Int {
        return player.overall(findBestPosition(player))
    }
}
