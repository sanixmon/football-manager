package com.footballmanager.simulation

import com.footballmanager.model.EmergencyGoalkeeper
import com.footballmanager.model.MAX_ATTRIBUTE
import com.footballmanager.model.MIN_ATTRIBUTE
import com.footballmanager.model.Player
import com.footballmanager.usecase.CalculateTeamStrengthUseCase
import com.footballmanager.usecase.SelectLineupUseCase
import kotlin.math.roundToInt
import kotlinx.serialization.Serializable

@Serializable
data class Team(
    val clubId: Long,
    val attack: Int,
    val defense: Int,
    val tactics: Tactics = Tactics(),
    val starters: List<Player> = emptyList(),
    val substitutes: List<Player> = emptyList(),
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
        private val selectLineupUseCase = SelectLineupUseCase()

        fun fromLineup(
            clubId: Long,
            starters: List<Player>,
            tactics: Tactics = Tactics(),
            substitutes: List<Player> = emptyList(),
        ): Team {
            val baseTeam = calculateTeamStrengthUseCase.buildTeam(clubId, starters, tactics)
            return baseTeam.copy(starters = starters, substitutes = substitutes)
        }

        fun fromSquad(
            clubId: Long,
            players: List<Player>,
            tactics: Tactics = Tactics(),
        ): Team {
            val lineup = selectLineupUseCase(players, tactics, clubId = clubId)
            val emergencyGk = if (lineup.starters.isNotEmpty() && lineup.starters.first() < 0) {
                listOf(EmergencyGoalkeeper.create(clubId, lineup.starters.first()))
            } else {
                emptyList()
            }
            val playerMap = (players + emergencyGk).associateBy { it.id }
            val starters = lineup.starters.map { playerMap.getValue(it) }
            val subs = lineup.substitutes.mapNotNull { playerMap[it] }
            return fromLineup(clubId, starters, tactics, substitutes = subs)
        }
    }
}
