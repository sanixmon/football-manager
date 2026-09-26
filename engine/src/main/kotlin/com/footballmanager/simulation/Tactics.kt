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
    FOUR_TWO_THREE_ONE("4-2-3-1", 1.05, 0.95),
    THREE_FIVE_TWO("3-5-2", 1.00, 1.05);

    val slots: List<Position> get() = when (this) {
        FOUR_FOUR_TWO -> listOf(
            Position.GK,
            Position.LB,
            Position.CB,
            Position.CB,
            Position.RB,
            Position.LW,
            Position.CM,
            Position.CM,
            Position.RW,
            Position.ST,
            Position.ST,
        )
        FOUR_THREE_THREE -> listOf(
            Position.GK,
            Position.LB,
            Position.CB,
            Position.CB,
            Position.RB,
            Position.DM,
            Position.CM,
            Position.AM,
            Position.LW,
            Position.ST,
            Position.RW,
        )
        FOUR_TWO_THREE_ONE -> listOf(
            Position.GK,
            Position.LB,
            Position.CB,
            Position.CB,
            Position.RB,
            Position.DM,
            Position.CM,
            Position.LW,
            Position.AM,
            Position.RW,
            Position.ST,
        )
        THREE_FIVE_TWO -> listOf(
            Position.GK,
            Position.CB,
            Position.CB,
            Position.CB,
            Position.LB,
            Position.DM,
            Position.CM,
            Position.AM,
            Position.RB,
            Position.ST,
            Position.ST,
        )
    }
}

/** Managerial intent on risk vs security. */
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

/** Pace of play. */
@Serializable
enum class Tempo(
    val label: String,
    val possessionMultiplier: Double,
    val fatigueMultiplier: Double,
    val chanceMultiplier: Double,
) {
    SLOW("Slow", 1.05, 0.90, 0.90),
    NORMAL("Normal", 1.0, 1.0, 1.0),
    FAST("Fast", 0.95, 1.15, 1.10),
}

/** Defensive engagement line and pressure. */
@Serializable
enum class Pressing(
    val label: String,
    val ballWinningModifier: Double,
    val fatigueMultiplier: Double,
    val counterVulnerability: Double,
) {
    LOW("Low", 0.90, 0.85, 0.90),
    MEDIUM("Medium", 1.0, 1.0, 1.0),
    HIGH("High", 1.15, 1.25, 1.15),
}

/** Distribution style. */
@Serializable
enum class Passing(
    val label: String,
    val possessionModifier: Double,
    val directnessModifier: Double,
) {
    SHORT("Short", 1.05, 0.90),
    MIXED("Mixed", 1.0, 1.0),
    DIRECT("Direct", 0.92, 1.15),
}

/**
 * A per-match tactical setup: formation and four tactical axes.
 */
@Serializable
data class Tactics(
    val formation: Formation = Formation.FOUR_FOUR_TWO,
    val mentality: Mentality = Mentality.BALANCED,
    val tempo: Tempo = Tempo.NORMAL,
    val pressing: Pressing = Pressing.MEDIUM,
    val passing: Passing = Passing.MIXED,
) {
    /** Combined attack modifier (formation × mentality). */
    val attackModifier: Double get() = formation.attackModifier * mentality.attackModifier

    /** Combined defense modifier (formation × mentality). */
    val defenseModifier: Double get() = formation.defenseModifier * mentality.defenseModifier
}
