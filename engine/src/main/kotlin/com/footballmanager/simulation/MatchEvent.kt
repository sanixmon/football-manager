package com.footballmanager.simulation

import kotlinx.serialization.Serializable

@Serializable
enum class Side { HOME, AWAY }

@Serializable
enum class MatchEventType {
    GOAL,
    SHOT_SAVED,
    SHOT_MISSED,
}

@Serializable
data class MatchEvent(
    val minute: Int,
    val type: MatchEventType,
    val side: Side,
)
