package com.footballmanager.model

/** Attributes and overall ratings use a 1..100 scale. */
const val MIN_ATTRIBUTE = 1
const val MAX_ATTRIBUTE = 100

enum class AttributeCategory {
    TECHNICAL,
    PHYSICAL,
    MENTAL,
}

enum class Attribute(val category: AttributeCategory) {
    // Technical
    PASSING(AttributeCategory.TECHNICAL),
    FIRST_TOUCH(AttributeCategory.TECHNICAL),
    FINISHING(AttributeCategory.TECHNICAL),
    DRIBBLING(AttributeCategory.TECHNICAL),
    CROSSING(AttributeCategory.TECHNICAL),
    TACKLING(AttributeCategory.TECHNICAL),

    // Physical
    PACE(AttributeCategory.PHYSICAL),
    ACCELERATION(AttributeCategory.PHYSICAL),
    STRENGTH(AttributeCategory.PHYSICAL),
    STAMINA(AttributeCategory.PHYSICAL),
    AGILITY(AttributeCategory.PHYSICAL),

    // Mental
    DECISION_MAKING(AttributeCategory.MENTAL),
    COMPOSURE(AttributeCategory.MENTAL),
    POSITIONING(AttributeCategory.MENTAL),
    VISION(AttributeCategory.MENTAL),
    WORK_RATE(AttributeCategory.MENTAL),
    LEADERSHIP(AttributeCategory.MENTAL),
}

/**
 * A player's set of attributes.
 *
 * Any attribute omitted from the map defaults to [MIN_ATTRIBUTE], and every
 * value is clamped into the [MIN_ATTRIBUTE]..[MAX_ATTRIBUTE] range.
 */
class PlayerAttributes(values: Map<Attribute, Int>) {

    private val normalized: Map<Attribute, Int> = Attribute.entries.associateWith { attribute ->
        (values[attribute] ?: MIN_ATTRIBUTE).coerceIn(MIN_ATTRIBUTE, MAX_ATTRIBUTE)
    }

    operator fun get(attribute: Attribute): Int = normalized.getValue(attribute)

    fun toMap(): Map<Attribute, Int> = normalized

    override fun equals(other: Any?): Boolean =
        other is PlayerAttributes && other.normalized == normalized

    override fun hashCode(): Int = normalized.hashCode()

    override fun toString(): String = "PlayerAttributes($normalized)"

    companion object {
        /** Attributes where every value is [value]. */
        fun uniform(value: Int): PlayerAttributes =
            PlayerAttributes(Attribute.entries.associateWith { value })
    }
}
