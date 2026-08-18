package com.footballmanager.simulation

import com.footballmanager.model.MAX_ATTRIBUTE
import com.footballmanager.model.MIN_ATTRIBUTE
import com.footballmanager.model.Player
import com.footballmanager.model.PositionGroup
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
        private const val MID_RATING = 50

        fun fromLineup(clubId: Long, starters: List<Player>, tactics: Tactics = Tactics()): Team {
            require(starters.size == 11) { "Starting XI must have exactly 11 players (got ${starters.size})" }
            val slots = tactics.formation.slots
            val assigned = starters.zip(slots)

            fun groupRatings(group: PositionGroup): List<Int> =
                assigned.filter { (_, slot) -> slot.group == group }
                    .map { (player, slot) -> player.effectiveOverall(slot) }

            fun average(values: List<Int>): Double =
                if (values.isEmpty()) MID_RATING.toDouble() else values.average()

            val attack = (0.7 * average(groupRatings(PositionGroup.ATTACKER)) +
                0.3 * average(groupRatings(PositionGroup.MIDFIELDER)))
                .roundToInt().coerceIn(MIN_ATTRIBUTE, MAX_ATTRIBUTE)

            val defense = (0.6 * average(groupRatings(PositionGroup.DEFENDER)) +
                0.2 * average(groupRatings(PositionGroup.GOALKEEPER)) +
                0.2 * average(groupRatings(PositionGroup.MIDFIELDER)))
                .roundToInt().coerceIn(MIN_ATTRIBUTE, MAX_ATTRIBUTE)

            return Team(clubId, attack, defense, tactics)
        }

        fun fromSquad(clubId: Long, players: List<Player>, tactics: Tactics = Tactics()): Team {
            val lineup = Lineup.autoSelect(players, tactics)
            val playerMap = players.associateBy { it.id }
            val starters = lineup.starters.map { playerMap.getValue(it) }
            return fromLineup(clubId, starters, tactics)
        }
    }
}
