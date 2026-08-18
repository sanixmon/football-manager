package com.footballmanager.simulation

import kotlinx.serialization.Serializable

@Serializable
data class MatchResult(
    val homeClubId: Long,
    val awayClubId: Long,
    val homeScore: Int,
    val awayScore: Int,
    val events: List<MatchEvent>,
    val stats: MatchStats,
) {
    val isHomeWin: Boolean get() = homeScore > awayScore
    val isAwayWin: Boolean get() = awayScore > homeScore
    val isDraw: Boolean get() = homeScore == awayScore

    val winner: Side?
        get() = when {
            homeScore > awayScore -> Side.HOME
            awayScore > homeScore -> Side.AWAY
            else -> null
        }
}
