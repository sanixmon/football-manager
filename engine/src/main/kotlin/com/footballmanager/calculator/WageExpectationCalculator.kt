package com.footballmanager.calculator

import com.footballmanager.model.Player
import com.footballmanager.model.SquadStatus
import kotlin.math.roundToLong

object WageExpectationCalculator {

    fun calculateExpectedWage(
        player: Player,
        promisedStatus: SquadStatus = SquadStatus.ROTATION,
    ): Long {
        val overall = player.bestOverall()
        val baseWage = when {
            overall < 60 -> 250L + ((overall - 1) * 10L)
            overall < 70 -> 800L + ((overall - 60) * 70L)
            overall < 80 -> 1_500L + ((overall - 70) * 650L)
            overall < 90 -> 8_000L + ((overall - 80) * 1_700L)
            else -> 25_000L + ((overall - 90) * 5_000L)
        }

        val statusMultiplier = when (promisedStatus) {
            SquadStatus.KEY_PLAYER -> 1.25
            SquadStatus.FIRST_TEAM -> 1.05
            SquadStatus.ROTATION -> 0.90
            SquadStatus.BACKUP -> 0.70
            SquadStatus.YOUTH -> 0.50
        }

        return (baseWage * statusMultiplier).roundToLong().coerceAtLeast(100L)
    }
}
