package com.footballmanager.usecase

import com.footballmanager.simulation.FakeRandomSource
import com.footballmanager.simulation.Team
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SimulateMatchUseCaseTest {

    @Test
    fun `SimulateMatchUseCase simulates match and produces valid MatchResult`() {
        val fakeRandom = FakeRandomSource(values = listOf(0.1, 0.2, 0.3))
        val useCase = SimulateMatchUseCase(randomSource = fakeRandom)

        val home = Team(clubId = 1L, attack = 80, defense = 75)
        val away = Team(clubId = 2L, attack = 70, defense = 70)

        val result = useCase(MatchRequest(homeTeam = home, awayTeam = away, seed = 12345L))
        assertNotNull(result)
        assertEquals(1L, result.homeClubId)
        assertEquals(2L, result.awayClubId)
    }
}
