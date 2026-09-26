package com.footballmanager.model

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OverallTest {

    private val strikerAttributes = PlayerAttributes(
        mapOf(
            Attribute.SHOOTING to 90,
            Attribute.PACE to 85,
            Attribute.DRIBBLING to 80,
            Attribute.PHYSICAL to 75,
            Attribute.PASSING to 70,
            Attribute.DEFENDING to 25,
            Attribute.GOALKEEPER to 10,
        ),
    )

    private fun player(attributes: PlayerAttributes, positions: List<Position>) = Player(
        id = 1,
        name = "Test Player",
        age = 24,
        nationality = "ID",
        naturalPositions = positions,
        attributes = attributes,
        contract = Contract(expiresOn = LocalDate.of(2030, 6, 30)),
    )

    @Test
    fun `striker rates higher at ST than CB`() {
        assertTrue(strikerAttributes.overall(Position.ST) > strikerAttributes.overall(Position.CB))
    }

    @Test
    fun `bestPosition picks the strongest natural position`() {
        val striker = player(strikerAttributes, listOf(Position.ST, Position.CB))
        assertEquals(Position.ST, striker.bestPosition())
    }

    @Test
    fun `overall stays within bounds for any position`() {
        for (position in Position.entries) {
            val value = strikerAttributes.overall(position)
            assertTrue(value in MIN_ATTRIBUTE..MAX_ATTRIBUTE, "out of range at $position: $value")
        }
    }
}
