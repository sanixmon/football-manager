package com.footballmanager.calculator

import com.footballmanager.model.Attribute
import com.footballmanager.model.Contract
import com.footballmanager.model.Player
import com.footballmanager.model.PlayerAttributes
import com.footballmanager.model.Position
import com.footballmanager.model.SquadStatus
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertTrue

class WageExpectationCalculatorTest {

    private fun createPlayer(rating: Int): Player {
        val attributes = PlayerAttributes(Attribute.entries.associateWith { rating })
        return Player(
            id = 1L,
            name = "Wage Test Player",
            age = 25,
            nationality = "ID",
            naturalPositions = listOf(Position.CM),
            attributes = attributes,
            contract = Contract(weeklyWage = 2000L, expiresOn = LocalDate.of(2028, 6, 30)),
        )
    }

    @Test
    fun `higher rating players expect higher wage`() {
        val averagePlayer = createPlayer(70)
        val starPlayer = createPlayer(85)

        val wageAvg = WageExpectationCalculator.calculateExpectedWage(averagePlayer)
        val wageStar = WageExpectationCalculator.calculateExpectedWage(starPlayer)

        assertTrue(wageStar > wageAvg * 3, "Star wage ($wageStar) should be much higher than average ($wageAvg)")
    }

    @Test
    fun `key player status commands higher wage than backup`() {
        val player = createPlayer(78)

        val keyWage = WageExpectationCalculator.calculateExpectedWage(player, SquadStatus.KEY_PLAYER)
        val backupWage = WageExpectationCalculator.calculateExpectedWage(player, SquadStatus.BACKUP)

        assertTrue(keyWage > backupWage)
    }
}
