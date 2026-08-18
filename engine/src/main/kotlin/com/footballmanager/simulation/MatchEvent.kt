package com.footballmanager.simulation

enum class Side { HOME, AWAY }

enum class MatchEventType {
    GOAL,
    SHOT_SAVED,
    SHOT_MISSED,
}

data class MatchEvent(
    val minute: Int,
    val type: MatchEventType,
    val side: Side,
)
