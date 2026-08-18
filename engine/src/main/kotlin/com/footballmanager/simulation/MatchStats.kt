package com.footballmanager.simulation

import kotlinx.serialization.Serializable

@Serializable
data class TeamStats(
    val possessions: Int = 0,
    val shots: Int = 0,
    val shotsOnTarget: Int = 0,
    val goals: Int = 0,
)

@Serializable
data class MatchStats(
    val ticks: Int,
    val home: TeamStats,
    val away: TeamStats,
) {
    /** Share of ticks where the home side had possession (0.0..1.0). */
    val homePossession: Double
        get() = if (ticks == 0) 0.0 else home.possessions.toDouble() / ticks

    /** Share of ticks where the away side had possession (0.0..1.0). */
    val awayPossession: Double
        get() = if (ticks == 0) 0.0 else away.possessions.toDouble() / ticks
}
