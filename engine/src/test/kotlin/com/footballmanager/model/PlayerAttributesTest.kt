package com.footballmanager.model

import kotlin.test.Test
import kotlin.test.assertEquals

class PlayerAttributesTest {

    @Test
    fun `values are clamped to the allowed range`() {
        val attributes = PlayerAttributes(
            mapOf(
                Attribute.FINISHING to 150,
                Attribute.TACKLING to -5,
            ),
        )
        assertEquals(MAX_ATTRIBUTE, attributes[Attribute.FINISHING])
        assertEquals(MIN_ATTRIBUTE, attributes[Attribute.TACKLING])
    }

    @Test
    fun `omitted attributes default to the minimum`() {
        val attributes = PlayerAttributes(mapOf(Attribute.FINISHING to 80))
        assertEquals(MIN_ATTRIBUTE, attributes[Attribute.PASSING])
    }

    @Test
    fun `uniform builds attributes with a single value`() {
        val attributes = PlayerAttributes.uniform(70)
        assertEquals(70, attributes[Attribute.FINISHING])
        assertEquals(70, attributes[Attribute.LEADERSHIP])
    }

    @Test
    fun `attribute maps are equal by content`() {
        val a = PlayerAttributes(mapOf(Attribute.FINISHING to 80, Attribute.PACE to 90))
        val b = PlayerAttributes(mapOf(Attribute.PACE to 90, Attribute.FINISHING to 80))
        assertEquals(a, b)
    }
}
