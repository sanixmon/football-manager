package com.footballmanager.calculator

import com.footballmanager.model.Attribute
import com.footballmanager.model.Contract
import com.footballmanager.model.Player
import com.footballmanager.model.PlayerAttributes
import com.footballmanager.model.Position
import com.footballmanager.model.TransferListingStatus
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransferValuationCalculatorTest {

    private fun createPlayer(age: Int, rating: Int, contractYears: Long = 2): Player {
        val attributes = PlayerAttributes(Attribute.entries.associateWith { rating })
        return Player(
            id = 1L,
            name = "Valuation Test Player",
            age = age,
            nationality = "ID",
            naturalPositions = listOf(Position.ST),
            attributes = attributes,
            contract = Contract(weeklyWage = 5000L, expiresOn = LocalDate.of(2028, 6, 30)),
        )
    }

    @Test
    fun `young high rating player has high market value`() {
        val youngStar = createPlayer(age = 19, rating = 82)
        val value = TransferValuationCalculator.calculateMarketValue(
            youngStar,
            currentDate = LocalDate.of(2026, 7, 1)
        )
        assertTrue(value > 10_000_000L, "Young star value should be > 10M, got $value")
    }

    @Test
    fun `transfer listed status applies discount`() {
        val player = createPlayer(age = 26, rating = 75)
        val normalValue = TransferValuationCalculator.calculateMarketValue(
            player,
            listingStatus = TransferListingStatus.NONE,
            currentDate = LocalDate.of(2026, 7, 1)
        )
        val listedValue = TransferValuationCalculator.calculateMarketValue(
            player,
            listingStatus = TransferListingStatus.TRANSFER_LISTED,
            currentDate = LocalDate.of(2026, 7, 1)
        )
        assertTrue(listedValue < normalValue)
        assertEquals((normalValue * 0.80).toLong(), listedValue)
    }

    @Test
    fun `expiring contract reduces player valuation`() {
        val player = createPlayer(age = 28, rating = 78)
        val longContractValue = TransferValuationCalculator.calculateMarketValue(
            player,
            currentDate = LocalDate.of(2026, 7, 1)
        )
        val expiringValue = TransferValuationCalculator.calculateMarketValue(
            player,
            currentDate = LocalDate.of(2028, 3, 1) // 3 months remaining
        )
        assertTrue(expiringValue < longContractValue)
    }
}
