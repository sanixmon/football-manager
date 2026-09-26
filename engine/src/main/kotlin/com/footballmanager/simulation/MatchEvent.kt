package com.footballmanager.simulation

import kotlinx.serialization.Serializable

@Serializable
enum class Side { HOME, AWAY }

@Serializable
enum class MatchEventType {
    POSSESSION,
    ATTACK,
    SHOT,
    SHOT_ON_TARGET,
    GOAL,
    SAVE,
    CORNER,
    FOUL,
    YELLOW_CARD,
    RED_CARD,
    INJURY,
    SUBSTITUTION,
    // Backward compatibility aliases
    SHOT_SAVED,
    SHOT_MISSED,
}

@Serializable
data class MatchEvent(
    val minute: Int,
    val type: MatchEventType,
    val side: Side,
    val teamId: Long = 0L,
    val playerId: Long? = null,
    val secondaryPlayerId: Long? = null,
    val description: String = "",
)
