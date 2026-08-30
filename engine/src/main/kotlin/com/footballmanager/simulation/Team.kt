package com.footballmanager.simulation

import com.footballmanager.model.MAX_ATTRIBUTE
import com.footballmanager.model.MIN_ATTRIBUTE
import com.footballmanager.model.Player
import com.footballmanager.usecase.CalculateTeamStrengthUseCase
import kotlin.math.roundToInt
import kotlinx.serialization.Serializable

@Serializable
data class Team(
    val clubId: Long,
    val attack: Int,
    val defense: Int,
    val tactics: Tactics = Tactics(),
) {
    init {
        require(attack in MIN_ATTRIBUTE..MAX_ATTRIBUTE) { "attack out of range: $attack" }
        require(defense in MIN_ATTRIBUTE..MAX_ATTRIBUTE) { "defense out of range: $defense" }
    }

    fun effectiveAttack(): Int =
        (attack * tactics.attackModifier).roundToInt().coerceIn(MIN_ATTRIBUTE, MAX_ATTRIBUTE)

    fun effectiveDefense(): Int =
        (defense * tactics.defenseModifier).roundToInt().coerceIn(MIN_ATTRIBUTE, MAX_ATTRIBUTE)

    companion object {
        private val calculateTeamStrengthUseCase = CalculateTeamStrengthUseCase()

        fun fromLineup(clubId: Long, starters: List<Player>, tactics: Tactics = Tactics()): Team =
            calculateTeamStrengthUseCase.buildTeam(clubId, starters, tactics)

        fun fromSquad(clubId: Long, players: List<Player>, tactics: Tactics = Tactics()): Team =
            calculateTeamStrengthUseCase.buildTeamFromSquad(clubId, players, tactics)
    }
}
