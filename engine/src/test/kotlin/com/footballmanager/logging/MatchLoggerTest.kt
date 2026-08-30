package com.footballmanager.logging

import com.footballmanager.simulation.Team
import com.footballmanager.usecase.MatchRequest
import com.footballmanager.usecase.SimulateMatchUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MatchLoggerTest {

    @Test
    fun `InMemoryMatchLogger captures match lifecycle and events when enabled`() {
        val logger = InMemoryMatchLogger()
        val useCase = SimulateMatchUseCase(logger = logger)

        val home = Team(clubId = 1L, attack = 85, defense = 80)
        val away = Team(clubId = 2L, attack = 75, defense = 70)

        val result = useCase(MatchRequest(homeTeam = home, awayTeam = away, seed = 42L))

        assertTrue(logger.logs.isNotEmpty())
        assertTrue(logger.logs.first().startsWith("Match started:"))
        assertTrue(logger.logs.last().startsWith("Match ended:"))
    }

    @Test
    fun `disabled logger does not record logs or evaluate lazy supplier`() {
        val disabledLogger = InMemoryMatchLogger(isEnabled = false)
        var supplierEvaluated = false

        disabledLogger.logEvent(LogLevel.DEBUG) {
            supplierEvaluated = true
            "Expensive log message"
        }

        assertTrue(disabledLogger.logs.isEmpty())
        kotlin.test.assertFalse(supplierEvaluated)
    }

    @Test
    fun `enabled logger evaluates lazy supplier`() {
        val logger = InMemoryMatchLogger(isEnabled = true)
        var supplierEvaluated = false

        logger.logEvent(LogLevel.INFO) {
            supplierEvaluated = true
            "Tactical substitution"
        }

        assertTrue(supplierEvaluated)
        assertEquals(1, logger.logs.size)
        assertEquals("[INFO] Tactical substitution", logger.logs.first())
    }
}
