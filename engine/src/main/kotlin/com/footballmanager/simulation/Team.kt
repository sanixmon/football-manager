package com.footballmanager.simulation

import com.footballmanager.model.MAX_ATTRIBUTE
import com.footballmanager.model.MIN_ATTRIBUTE
import com.footballmanager.model.Player
import com.footballmanager.model.PositionGroup
import kotlin.math.roundToInt

/**
 * A team's match-relevant strength on the same 1..100 scale as attributes,
 * plus optional [tactics] that modify attack/defense for a match.
 */
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

    /** Attack rating after applying the formation and mentality modifiers. */
    fun effectiveAttack(): Int =
        (attack * tactics.attackModifier).roundToInt().coerceIn(MIN_ATTRIBUTE, MAX_ATTRIBUTE)

    /** Defense rating after applying the formation and mentality modifiers. */
    fun effectiveDefense(): Int =
        (defense * tactics.defenseModifier).roundToInt().coerceIn(MIN_ATTRIBUTE, MAX_ATTRIBUTE)

    companion object {
        private const val MID_RATING = 50

        /**
         * Derives a [Team] from a squad by averaging the best overall of each
         * positional group. Attackers drive attack, defenders + keeper drive
         * defense, and midfielders contribute to both.
         */
        fun fromSquad(clubId: Long, players: List<Player>, tactics: Tactics = Tactics()): Team {
            fun bestOf(group: PositionGroup): List<Int> =
                players.filter { it.bestPosition().group == group }.map { it.bestOverall() }

            fun average(values: List<Int>): Double =
                if (values.isEmpty()) MID_RATING.toDouble() else values.average()

            val attack = (0.7 * average(bestOf(PositionGroup.ATTACKER)) +
                0.3 * average(bestOf(PositionGroup.MIDFIELDER)))
                .roundToInt().coerceIn(MIN_ATTRIBUTE, MAX_ATTRIBUTE)

            val defense = (0.6 * average(bestOf(PositionGroup.DEFENDER)) +
                0.2 * average(bestOf(PositionGroup.GOALKEEPER)) +
                0.2 * average(bestOf(PositionGroup.MIDFIELDER)))
                .roundToInt().coerceIn(MIN_ATTRIBUTE, MAX_ATTRIBUTE)

            return Team(clubId, attack, defense, tactics)
        }
    }
}
