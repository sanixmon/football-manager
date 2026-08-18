package com.footballmanager.simulation

import com.footballmanager.model.Position
import kotlinx.serialization.Serializable

/** Team shape. Modifiers shift [Team] attack/defense, not player ratings. */
@Serializable
enum class Formation(
    val label: String,
    val attackModifier: Double,
    val defenseModifier: Double,
) {
    FOUR_FOUR_TWO("4-4-2", 1.00, 1.00),
    FOUR_THREE_THREE("4-3-3", 1.10, 0.90),
    FIVE_THREE_TWO("5-3-2", 0.90, 1.10);

    val slots: List<Position> get() = when (this) {
        FOUR_FOUR_TWO -> listOf(
            Position.GK,
            Position.LB,
            Position.CB,
            Position.CB,
            Position.RB,
            Position.LM,
            Position.CM,
            Position.CM,
            Position.RM,
            Position.ST,
            Position.ST,
        )
        FOUR_THREE_THREE -> listOf(
            Position.GK,
            Position.LB,
            Position.CB,
            Position.CB,
            Position.RB,
            Position.CDM,
            Position.CM,
            Position.CAM,
            Position.LW,
            Position.ST,
            Position.RW,
        )
        FIVE_THREE_TWO -> listOf(
            Position.GK,
            Position.LWB,
            Position.CB,
            Position.CB,
            Position.CB,
            Position.RWB,
            Position.CM,
            Position.CM,
            Position.CM,
            Position.ST,
            Position.ST,
        )
    }
}

/** Managerial intent. Stacks with [Formation] (multiplied together). */
@Serializable
enum class Mentality(
    val label: String,
    val attackModifier: Double,
    val defenseModifier: Double,
) {
    DEFENSIVE("Defensive", 0.85, 1.15),
    BALANCED("Balanced", 1.0, 1.0),
    ATTACKING("Attacking", 1.15, 0.85),
}

/**
 * A static per-match tactical setup: formation and mentality, applied together
 * as multipliers on a team's attack and defense.
 */
@Serializable
data class Tactics(
    val formation: Formation = Formation.FOUR_FOUR_TWO,
    val mentality: Mentality = Mentality.BALANCED,
) {
    /** Combined attack modifier (formation × mentality). */
    val attackModifier: Double get() = formation.attackModifier * mentality.attackModifier

    /** Combined defense modifier (formation × mentality). */
    val defenseModifier: Double get() = formation.defenseModifier * mentality.defenseModifier
}
