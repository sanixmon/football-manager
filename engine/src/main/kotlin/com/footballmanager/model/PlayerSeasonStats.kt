package com.footballmanager.model

import kotlinx.serialization.Serializable

@Serializable
data class PlayerSeasonStats(
    val playerId: Long,
    val appearances: Int = 0,
    val starts: Int = 0,
    val substituteAppearances: Int = 0,
    val goals: Int = 0,
    val assists: Int = 0,
    val cleanSheets: Int = 0,
    val yellowCards: Int = 0,
    val redCards: Int = 0,
    val totalRatingPoints: Double = 0.0,
) {
    val averageRating: Double
        get() = if (appearances > 0) totalRatingPoints / appearances else 0.0

    fun withMatchAppearance(
        isStarter: Boolean,
        goalsScored: Int = 0,
        assistsMade: Int = 0,
        cleanSheet: Boolean = false,
        matchRating: Double = 6.5,
        yellow: Boolean = false,
        red: Boolean = false,
    ): PlayerSeasonStats = copy(
        appearances = appearances + 1,
        starts = if (isStarter) starts + 1 else starts,
        substituteAppearances = if (!isStarter) substituteAppearances + 1 else substituteAppearances,
        goals = goals + goalsScored,
        assists = assists + assistsMade,
        cleanSheets = if (cleanSheet) cleanSheets + 1 else cleanSheets,
        yellowCards = if (yellow) yellowCards + 1 else yellowCards,
        redCards = if (red) redCards + 1 else redCards,
        totalRatingPoints = totalRatingPoints + matchRating,
    )
}
