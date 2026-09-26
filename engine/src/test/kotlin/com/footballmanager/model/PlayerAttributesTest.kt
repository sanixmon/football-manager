package com.footballmanager.model

import kotlin.test.Test
import kotlin.test.assertEquals

class PlayerAttributesTest {

    @Test
    fun `values are clamped to the allowed range`() {
        val attributes = PlayerAttributes(
            mapOf(
                Attribute.SHOOTING to 150,
                Attribute.DEFENDING to -5,
            ),
        )
        assertEquals(MAX_ATTRIBUTE, attributes[Attribute.SHOOTING])
        assertEquals(MIN_ATTRIBUTE, attributes[Attribute.DEFENDING])
    }

    @Test
    fun `omitted attributes default to the minimum`() {
        val attributes = PlayerAttributes(mapOf(Attribute.SHOOTING to 80))
        assertEquals(MIN_ATTRIBUTE, attributes[Attribute.PASSING])
    }

    @Test
    fun `uniform builds attributes with a single value`() {
        val attributes = PlayerAttributes.uniform(70)
        assertEquals(70, attributes[Attribute.SHOOTING])
        assertEquals(70, attributes[Attribute.PHYSICAL])
    }

    @Test
    fun `attribute maps are equal by content`() {
        val a = PlayerAttributes(mapOf(Attribute.SHOOTING to 80, Attribute.PACE to 90))
        val b = PlayerAttributes(mapOf(Attribute.PACE to 90, Attribute.SHOOTING to 80))
        assertEquals(a, b)
    }
}
