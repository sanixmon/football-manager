package com.footballmanager.usecase

import com.footballmanager.calculator.PlayerCalculator
import com.footballmanager.model.MAX_ATTRIBUTE
import com.footballmanager.model.MIN_ATTRIBUTE
import com.footballmanager.model.Player
import com.footballmanager.model.PositionGroup
import com.footballmanager.simulation.Tactics
import com.footballmanager.simulation.Team
import kotlin.math.roundToInt

data class TeamStrength(
    val attack: Int,
    val defense: Int,
)

class CalculateTeamStrengthUseCase(
    private val selectLineupUseCase: SelectLineupUseCase = SelectLineupUseCase(),
) {
    private val defaultMidRating = 50

    fun calculateFromStarters(starters: List<Player>, tactics: Tactics = Tactics()): TeamStrength {
        require(starters.size == 11) { "Starting XI must have exactly 11 players (got ${starters.size})" }
        val slots = tactics.formation.slots
        val assigned = starters.zip(slots)

        fun groupRatings(group: PositionGroup): List<Int> =
            assigned.filter { (_, slot) -> slot.group == group }
                .map { (player, slot) -> PlayerCalculator.calculateEffectiveOverall(player, slot) }

        fun average(values: List<Int>): Double =
            if (values.isEmpty()) defaultMidRating.toDouble() else values.average()

        val attack = (0.7 * average(groupRatings(PositionGroup.ATTACKER)) +
            0.3 * average(groupRatings(PositionGroup.MIDFIELDER)))
            .roundToInt().coerceIn(MIN_ATTRIBUTE, MAX_ATTRIBUTE)

        val defense = (0.6 * average(groupRatings(PositionGroup.DEFENDER)) +
            0.2 * average(groupRatings(PositionGroup.GOALKEEPER)) +
            0.2 * average(groupRatings(PositionGroup.MIDFIELDER)))
            .roundToInt().coerceIn(MIN_ATTRIBUTE, MAX_ATTRIBUTE)

        return TeamStrength(attack = attack, defense = defense)
    }

    fun calculateFromSquad(players: List<Player>, tactics: Tactics = Tactics()): TeamStrength {
        val lineup = selectLineupUseCase(players, tactics)
        val playerMap = players.associateBy { it.id }
        val starters = lineup.starters.map { playerMap.getValue(it) }
        return calculateFromStarters(starters, tactics)
    }

    fun buildTeam(clubId: Long, starters: List<Player>, tactics: Tactics = Tactics()): Team {
        val strength = calculateFromStarters(starters, tactics)
        return Team(clubId, strength.attack, strength.defense, tactics)
    }

    fun buildTeamFromSquad(clubId: Long, players: List<Player>, tactics: Tactics = Tactics()): Team {
        val strength = calculateFromSquad(players, tactics)
        return Team(clubId, strength.attack, strength.defense, tactics)
    }
}
