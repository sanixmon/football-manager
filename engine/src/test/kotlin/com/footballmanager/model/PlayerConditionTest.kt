package com.footballmanager.model

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlayerConditionTest {

    private fun testPlayer(fitness: Int = 100, morale: Int = 50): Player {
        val attributes = PlayerAttributes(Attribute.entries.associateWith { 80 })
        return Player(
            id = 1L,
            name = "Test Player",
            age = 25,
            nationality = "ID",
            naturalPositions = listOf(Position.ST),
            attributes = attributes,
            contract = Contract(expiresOn = LocalDate.of(2030, 6, 30)),
            fitness = fitness,
            morale = morale,
        )
    }

    @Test
    fun `effectiveOverall equals base overall at 100 fitness and 50 morale`() {
        val player = testPlayer(fitness = 100, morale = 50)
        assertEquals(player.overall(Position.ST), player.effectiveOverall(Position.ST))
    }

    @Test
    fun `effectiveOverall drops with lower fitness`() {
        val fullFit = testPlayer(fitness = 100, morale = 50)
        val tired = testPlayer(fitness = 50, morale = 50)
        val exhausted = testPlayer(fitness = 0, morale = 50)

        assertTrue(tired.effectiveOverall(Position.ST) < fullFit.effectiveOverall(Position.ST))
        assertTrue(exhausted.effectiveOverall(Position.ST) < tired.effectiveOverall(Position.ST))
        assertEquals(56, exhausted.effectiveOverall(Position.ST)) // 80 * 0.70 = 56
    }

    @Test
    fun `effectiveOverall increases with high morale and drops with low morale`() {
        val highMorale = testPlayer(fitness = 100, morale = 100)
        val lowMorale = testPlayer(fitness = 100, morale = 0)

        assertEquals(88, highMorale.effectiveOverall(Position.ST)) // 80 * 1.0 * 1.10 = 88
        assertEquals(72, lowMorale.effectiveOverall(Position.ST))  // 80 * 1.0 * 0.90 = 72
    }

    @Test
    fun `effectiveOverall clamps within MIN_ATTRIBUTE and MAX_ATTRIBUTE`() {
        val player = testPlayer(fitness = 100, morale = 100)
        assertTrue(player.effectiveOverall(Position.ST) in MIN_ATTRIBUTE..MAX_ATTRIBUTE)
    }

    @Test
    fun `effectiveOverall handles out of bounds fitness and morale gracefully`() {
        val overclamped = testPlayer(fitness = 150, morale = 150)
        val maxed = testPlayer(fitness = 100, morale = 100)
        assertEquals(maxed.effectiveOverall(Position.ST), overclamped.effectiveOverall(Position.ST))

        val underclamped = testPlayer(fitness = -20, morale = -20)
        val zeroed = testPlayer(fitness = 0, morale = 0)
        assertEquals(zeroed.effectiveOverall(Position.ST), underclamped.effectiveOverall(Position.ST))
    }
}
