package com.footballmanager.model

import kotlinx.serialization.Serializable

@Serializable
data class BoardObjectives(
    val clubId: Long,
    val targetLeaguePosition: Int, // e.g. 1 for title, 3 for continental, 6 for top half, 10 for avoid relegation
    val boardConfidence: Int = 75, // 0 to 100
) {
    val statusDescription: String
        get() = when {
            boardConfidence >= 80 -> "Delighted"
            boardConfidence >= 65 -> "Satisfied"
            boardConfidence >= 45 -> "Under Review"
            else -> "In Jeopardy"
        }

    fun withMatchEvaluation(currentPosition: Int, isWin: Boolean, isLoss: Boolean): BoardObjectives {
        val positionDelta = targetLeaguePosition - currentPosition
        val deltaConfidence = when {
            isWin && positionDelta >= 0 -> +3
            isWin -> +2
            isLoss && positionDelta < -2 -> -4
            isLoss -> -2
            else -> 0
        }
        return copy(boardConfidence = (boardConfidence + deltaConfidence).coerceIn(0, 100))
    }
}
