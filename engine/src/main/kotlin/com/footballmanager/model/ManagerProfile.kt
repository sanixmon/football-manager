package com.footballmanager.model

import kotlinx.serialization.Serializable

@Serializable
data class ManagerProfile(
    val name: String,
    val nationality: String = "ID",
    val clubId: Long,
    val reputation: Int = 50,
    val trophiesWon: Int = 0,
    val matchesManaged: Int = 0,
    val wins: Int = 0,
    val draws: Int = 0,
    val losses: Int = 0,
) {
    val winRatePercentage: Int
        get() = if (matchesManaged > 0) ((wins.toDouble() / matchesManaged) * 100).toInt() else 0

    fun withMatchResult(isWin: Boolean, isDraw: Boolean): ManagerProfile = copy(
        matchesManaged = matchesManaged + 1,
        wins = if (isWin) wins + 1 else wins,
        draws = if (isDraw) draws + 1 else draws,
        losses = if (!isWin && !isDraw) losses + 1 else losses,
        reputation = (reputation + (if (isWin) 2 else if (isDraw) 0 else -1)).coerceIn(1, 100),
    )
}
