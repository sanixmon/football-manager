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
    DM(PositionGroup.MIDFIELDER),
    CM(PositionGroup.MIDFIELDER),
    AM(PositionGroup.MIDFIELDER),
    LW(PositionGroup.ATTACKER),
    RW(PositionGroup.ATTACKER),
    ST(PositionGroup.ATTACKER),
}

/**
 * Relative importance of each attribute per position, used to derive the
 * position-specific overall rating. Attributes absent from a position's map
 * contribute nothing to that position's rating.
 */
object PositionWeights {

    val weights: Map<Position, Map<Attribute, Int>> = mapOf(
        Position.GK to mapOf(
            Attribute.GOALKEEPER to 10,
            Attribute.PHYSICAL to 3,
            Attribute.PASSING to 2,
            Attribute.DEFENDING to 1,
        ),
        Position.CB to mapOf(
            Attribute.DEFENDING to 8,
            Attribute.PHYSICAL to 6,
            Attribute.PACE to 3,
            Attribute.PASSING to 2,
        ),
        Position.LB to mapOf(
            Attribute.DEFENDING to 6,
            Attribute.PACE to 5,
            Attribute.PHYSICAL to 4,
            Attribute.PASSING to 3,
            Attribute.DRIBBLING to 2,
        ),
        Position.RB to mapOf(
            Attribute.DEFENDING to 6,
            Attribute.PACE to 5,
            Attribute.PHYSICAL to 4,
            Attribute.PASSING to 3,
            Attribute.DRIBBLING to 2,
        ),
        Position.DM to mapOf(
            Attribute.DEFENDING to 7,
            Attribute.PASSING to 5,
            Attribute.PHYSICAL to 5,
            Attribute.PACE to 2,
            Attribute.DRIBBLING to 2,
        ),
        Position.CM to mapOf(
            Attribute.PASSING to 7,
            Attribute.DRIBBLING to 5,
            Attribute.PHYSICAL to 4,
            Attribute.DEFENDING to 4,
            Attribute.PACE to 3,
            Attribute.SHOOTING to 3,
        ),
        Position.AM to mapOf(
            Attribute.PASSING to 7,
            Attribute.DRIBBLING to 7,
            Attribute.SHOOTING to 5,
            Attribute.PACE to 4,
            Attribute.PHYSICAL to 2,
        ),
        Position.LW to mapOf(
            Attribute.PACE to 7,
            Attribute.DRIBBLING to 7,
            Attribute.PASSING to 4,
            Attribute.SHOOTING to 4,
            Attribute.PHYSICAL to 2,
        ),
        Position.RW to mapOf(
            Attribute.PACE to 7,
            Attribute.DRIBBLING to 7,
            Attribute.PASSING to 4,
            Attribute.SHOOTING to 4,
            Attribute.PHYSICAL to 2,
        ),
        Position.ST to mapOf(
            Attribute.SHOOTING to 8,
            Attribute.PACE to 5,
            Attribute.PHYSICAL to 4,
            Attribute.DRIBBLING to 4,
            Attribute.PASSING to 2,
        ),
    )

    val totalWeights: Map<Position, Int> = weights.mapValues { (_, attrs) ->
        attrs.values.sum()
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
