package com.footballmanager.simulation

import com.footballmanager.model.Player
import com.footballmanager.usecase.SelectLineupUseCase
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
        private val selectLineupUseCase = SelectLineupUseCase()

        fun autoSelect(players: List<Player>, tactics: Tactics): Lineup =
            selectLineupUseCase(players, tactics)
    }
}
