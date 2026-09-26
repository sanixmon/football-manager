package com.footballmanager.simulation

import kotlinx.serialization.Serializable

@Serializable
data class TeamStats(
    val possessions: Int = 0,
    val shots: Int = 0,
    val shotsOnTarget: Int = 0,
    val goals: Int = 0,
    val corners: Int = 0,
    val fouls: Int = 0,
    val yellowCards: Int = 0,
    val redCards: Int = 0,
)

@Serializable
data class PlayerMatchStats(
    val playerId: Long,
    val teamId: Long,
    val minutesPlayed: Int = 0,
    val isStarter: Boolean = false,
    val shots: Int = 0,
    val shotsOnTarget: Int = 0,
    val goals: Int = 0,
    val assists: Int = 0,
    val saves: Int = 0,
    val fouls: Int = 0,
    val yellowCards: Int = 0,
    val redCards: Int = 0,
    val defensiveContributions: Int = 0,
    val passingContributions: Int = 0,
    val attackingContributions: Int = 0,
    val isInjured: Boolean = false,
    val isSubstitutedOut: Boolean = false,
    val isSubstitutedIn: Boolean = false,
    val isRedCarded: Boolean = false,
)

@Serializable
data class SubstitutionRecord(
    val minute: Int,
    val teamId: Long,
    val playerOutId: Long,
    val playerInId: Long,
    val reason: String = "Tactical",
)

@Serializable
data class MatchStats(
    val ticks: Int = 90,
    val home: TeamStats = TeamStats(),
    val away: TeamStats = TeamStats(),
) {
    val durationMinutes: Int get() = ticks

    /** Share of ticks/minutes where the home side had possession (0.0..1.0). */
    val homePossession: Double
        get() {
            val total = home.possessions + away.possessions
            return if (total == 0) 0.5 else home.possessions.toDouble() / total
        }

    /** Share of ticks/minutes where the away side had possession (0.0..1.0). */
    val awayPossession: Double
        get() {
            val total = home.possessions + away.possessions
            return if (total == 0) 0.5 else away.possessions.toDouble() / total
        }
}
