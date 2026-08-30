package com.footballmanager.calculator

import com.footballmanager.model.Player
import com.footballmanager.model.TransferListingStatus
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToLong

object TransferValuationCalculator {

    fun calculateMarketValue(
        player: Player,
        listingStatus: TransferListingStatus = TransferListingStatus.NONE,
        currentDate: LocalDate = player.contract.expiresOn.minusYears(1),
    ): Long {
        if (listingStatus == TransferListingStatus.NOT_FOR_SALE) {
            return 0L
        }

        val overall = player.bestOverall()
        val baseValue = when {
            overall < 60 -> 50_000L + ((overall - 1) * 3_000L)
            overall < 70 -> 250_000L + ((overall - 60) * 125_000L)
            overall < 80 -> 1_500_000L + ((overall - 70) * 650_000L)
            overall < 90 -> 8_000_000L + ((overall - 80) * 2_200_000L)
            else -> 30_000_000L + ((overall - 90) * 5_000_000L)
        }

        val ageMultiplier = when {
            player.age < 21 -> 1.35
            player.age in 21..25 -> 1.20
            player.age in 26..29 -> 1.00
            player.age in 30..33 -> 0.70
            else -> 0.40
        }

        val monthsRemaining = ChronoUnit.MONTHS.between(currentDate, player.contract.expiresOn).coerceAtLeast(0)
        val contractMultiplier = when {
            monthsRemaining < 6 -> 0.50
            monthsRemaining in 6..12 -> 0.75
            monthsRemaining in 13..36 -> 1.00
            else -> 1.15
        }

        val statusMultiplier = when (listingStatus) {
            TransferListingStatus.TRANSFER_LISTED -> 0.80
            TransferListingStatus.LOAN_LISTED -> 0.90
            else -> 1.00
        }

        val rawValue = (baseValue * ageMultiplier * contractMultiplier * statusMultiplier).roundToLong()
        return rawValue.coerceAtLeast(10_000L)
    }
}
