package com.footballmanager

import kotlin.test.Test
import kotlin.test.assertEquals

class EngineTest {

    @Test
    fun `engine reports its identity`() {
        assertEquals("football-manager", Engine.NAME)
        assertEquals("0.1.0", Engine.VERSION)
    }
}
