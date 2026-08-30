package com.footballmanager.model

import kotlin.math.roundToInt
import kotlinx.serialization.Serializable

@Serializable
enum class PositionGroup {
    GOALKEEPER,
    DEFENDER,
    MIDFIELDER,
    ATTACKER,
}

@Serializable
enum class Position(val group: PositionGroup) {
    GK(PositionGroup.GOALKEEPER),
    CB(PositionGroup.DEFENDER),
    LB(PositionGroup.DEFENDER),
    RB(PositionGroup.DEFENDER),
    LWB(PositionGroup.DEFENDER),
    RWB(PositionGroup.DEFENDER),
    CDM(PositionGroup.MIDFIELDER),
    CM(PositionGroup.MIDFIELDER),
    CAM(PositionGroup.MIDFIELDER),
    LM(PositionGroup.MIDFIELDER),
    RM(PositionGroup.MIDFIELDER),
    LW(PositionGroup.ATTACKER),
    RW(PositionGroup.ATTACKER),
    ST(PositionGroup.ATTACKER),
}

/**
 * Relative importance of each attribute per position, used to derive the
 * position-specific overall rating. Attributes absent from a position's map
 * contribute nothing to that position's rating.
 *
 * Note: [Position.GK] is approximated from field-player attributes until a
 * dedicated goalkeeping set (reflexes, handling, ...) is introduced.
 */
object PositionWeights {

    private val common = mapOf(
        Attribute.DECISION_MAKING to 2,
        Attribute.COMPOSURE to 2,
        Attribute.WORK_RATE to 2,
    )

    private val fullBack = mapOf(
        Attribute.TACKLING to 4,
        Attribute.CROSSING to 3,
        Attribute.PACE to 3,
        Attribute.ACCELERATION to 3,
        Attribute.STAMINA to 3,
        Attribute.POSITIONING to 3,
    )

    private val winger = mapOf(
        Attribute.DRIBBLING to 4,
        Attribute.PACE to 4,
        Attribute.ACCELERATION to 4,
        Attribute.CROSSING to 3,
        Attribute.FINISHING to 2,
        Attribute.FIRST_TOUCH to 2,
    )

    val weights: Map<Position, Map<Attribute, Int>> = mapOf(
        Position.GK to common + mapOf(
            Attribute.POSITIONING to 5,
            Attribute.AGILITY to 3,
            Attribute.STRENGTH to 2,
            Attribute.LEADERSHIP to 2,
        ),
        Position.CB to common + mapOf(
            Attribute.TACKLING to 5,
            Attribute.POSITIONING to 4,
            Attribute.STRENGTH to 4,
            Attribute.PACE to 2,
            Attribute.ACCELERATION to 2,
            Attribute.LEADERSHIP to 2,
        ),
        Position.LB to common + fullBack,
        Position.RB to common + fullBack,
        Position.LWB to common + fullBack,
        Position.RWB to common + fullBack,
        Position.CDM to common + mapOf(
            Attribute.TACKLING to 4,
            Attribute.POSITIONING to 4,
            Attribute.PASSING to 3,
            Attribute.STAMINA to 3,
            Attribute.STRENGTH to 3,
        ),
        Position.CM to common + mapOf(
            Attribute.PASSING to 4,
            Attribute.VISION to 4,
            Attribute.FIRST_TOUCH to 3,
            Attribute.STAMINA to 3,
            Attribute.POSITIONING to 2,
            Attribute.TACKLING to 1,
        ),
        Position.CAM to common + mapOf(
            Attribute.VISION to 4,
            Attribute.PASSING to 4,
            Attribute.DRIBBLING to 3,
            Attribute.FIRST_TOUCH to 3,
            Attribute.FINISHING to 2,
            Attribute.AGILITY to 2,
        ),
        Position.LM to common + winger,
        Position.RM to common + winger,
        Position.LW to common + winger,
        Position.RW to common + winger,
        Position.ST to common + mapOf(
            Attribute.FINISHING to 5,
            Attribute.PACE to 3,
            Attribute.ACCELERATION to 3,
            Attribute.POSITIONING to 3,
            Attribute.STRENGTH to 2,
            Attribute.FIRST_TOUCH to 2,
            Attribute.DRIBBLING to 1,
        ),
    )

    /** Precomputed sum of attribute weights per position for fast rating lookups. */
    val totalWeights: Map<Position, Int> = weights.mapValues { it.value.values.sum() }

    init {
        require(weights.keys == Position.entries.toSet()) {
            "PositionWeights must define weights for every position"
        }
    }
}

/**
 * Position-specific overall rating (1..100): the weighted average of the
 * player's attributes using [PositionWeights].
 */
fun PlayerAttributes.overall(position: Position): Int {
    val positionWeights = PositionWeights.weights.getValue(position)
    val totalWeight = PositionWeights.totalWeights.getValue(position)
    val weightedSum = positionWeights.entries.sumOf { (attribute, weight) ->
        this[attribute] * weight
    }
    return (weightedSum.toDouble() / totalWeight).roundToInt()
        .coerceIn(MIN_ATTRIBUTE, MAX_ATTRIBUTE)
}
