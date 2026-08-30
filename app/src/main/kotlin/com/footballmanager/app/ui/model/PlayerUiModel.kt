package com.footballmanager.app.ui.model

import com.footballmanager.calculator.PlayerCalculator
import com.footballmanager.model.Player
import com.footballmanager.model.Position

data class PlayerUiModel(
    val id: Long,
    val displayName: String,
    val shortName: String,
    val positionName: String,
    val baseOverall: Int,
    val effectiveOverall: Int,
    val fitness: Int,
    val morale: Int,
    val valueFormatted: String,
)

fun Player.toUiModel(targetPosition: Position? = null): PlayerUiModel {
    val pos = targetPosition ?: bestPosition()
    val eff = PlayerCalculator.calculateEffectiveOverall(this, pos)
    return PlayerUiModel(
        id = id,
        displayName = name,
        shortName = name.split(" ").lastOrNull() ?: name,
        positionName = pos.name,
        baseOverall = overall(pos),
        effectiveOverall = eff,
        fitness = fitness,
        morale = morale,
        valueFormatted = "${overall(pos) * 100_000L}",
    )
}
