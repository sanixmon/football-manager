package com.footballmanager.calculator

import com.footballmanager.model.Attribute
import com.footballmanager.model.Contract
import com.footballmanager.model.MAX_ATTRIBUTE
import com.footballmanager.model.MIN_ATTRIBUTE
import com.footballmanager.model.Player
import com.footballmanager.model.PlayerAttributes
import com.footballmanager.model.Position
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlayerCalculatorTest {

    private fun createPlayer(fitness: Int = 100, morale: Int = 50): Player {
        val attributes = PlayerAttributes(Attribute.entries.associateWith { 80 })
        return Player(
            id = 1L,
            name = "Test Striker",
            age = 24,
            nationality = "ID",
            naturalPositions = listOf(Position.ST, Position.CAM),
            attributes = attributes,
            contract = Contract(expiresOn = LocalDate.of(2030, 6, 30)),
            fitness = fitness,
            morale = morale,
        )
    }

    @Test
    fun `calculateEffectiveOverall scales with fitness and morale correctly`() {
        val fullFit = createPlayer(fitness = 100, morale = 50)
        val calculated = PlayerCalculator.calculateEffectiveOverall(fullFit, Position.ST)
        assertEquals(80, calculated)

        val exhausted = createPlayer(fitness = 0, morale = 50)
        val exhaustedRating = PlayerCalculator.calculateEffectiveOverall(exhausted, Position.ST)
        assertEquals(56, exhaustedRating)

        val motivated = createPlayer(fitness = 100, morale = 100)
        val motivatedRating = PlayerCalculator.calculateEffectiveOverall(motivated, Position.ST)
        assertEquals(88, motivatedRating)
    }

    @Test
    fun `findBestPosition returns highest rating natural position`() {
        val player = createPlayer()
        val bestPos = PlayerCalculator.findBestPosition(player)
        assertTrue(bestPos in player.naturalPositions)
    }

    @Test
    fun `calculateBestOverall returns overall at best position`() {
        val player = createPlayer()
        val bestOverall = PlayerCalculator.calculateBestOverall(player)
        assertTrue(bestOverall in MIN_ATTRIBUTE..MAX_ATTRIBUTE)
    }
}
