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
        require(substitutes.distinct().size == substitutes.size) { "Substitutes must be unique players" }
        require(starters.intersect(substitutes.toSet()).isEmpty()) { "Player cannot be both starter and substitute" }
    }

    companion object {
        private val selectLineupUseCase = SelectLineupUseCase()

        fun autoSelect(
            players: List<Player>,
            tactics: Tactics,
            allowEmergencyGk: Boolean = true,
            clubId: Long = 0L,
        ): Lineup =
            selectLineupUseCase(players, tactics, allowEmergencyGk, clubId)
    }
}
