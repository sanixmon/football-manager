package com.footballmanager.usecase

import com.footballmanager.simulation.FakeRandomSource
import com.footballmanager.simulation.Team
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SimulateMatchUseCaseTest {

    @Test
    fun `SimulateMatchUseCase uses injected RandomSource when seed is null`() {
        val fakeRandom = FakeRandomSource(values = listOf(0.1, 0.2, 0.3))
        val useCase = SimulateMatchUseCase(randomSource = fakeRandom)

        val home = Team(clubId = 1L, attack = 80, defense = 75)
        val away = Team(clubId = 2L, attack = 70, defense = 70)

        val result = useCase(MatchRequest(homeTeam = home, awayTeam = away))
        assertNotNull(result)
        assertEquals(1L, result.homeClubId)
        assertEquals(2L, result.awayClubId)
    }

    @Test
    fun `SimulateMatchUseCase honors explicit seed override`() {
        val useCase = SimulateMatchUseCase()

        val home = Team(clubId = 1L, attack = 80, defense = 75)
        val away = Team(clubId = 2L, attack = 70, defense = 70)

        val result1 = useCase(MatchRequest(homeTeam = home, awayTeam = away, seed = 42L))
        val result2 = useCase(MatchRequest(homeTeam = home, awayTeam = away, seed = 42L))

        assertEquals(result1.homeScore, result2.homeScore)
        assertEquals(result1.awayScore, result2.awayScore)
        assertEquals(result1.events.size, result2.events.size)
    }

    @Test
    fun `simulateBatch simulates multiple fixtures`() {
        val useCase = SimulateMatchUseCase()
        val t1 = Team(clubId = 1L, attack = 80, defense = 75)
        val t2 = Team(clubId = 2L, attack = 70, defense = 70)
        val t3 = Team(clubId = 3L, attack = 65, defense = 65)

        val requests = listOf(
            MatchRequest(homeTeam = t1, awayTeam = t2, seed = 1L),
            MatchRequest(homeTeam = t2, awayTeam = t3, seed = 2L),
        )

        val results = useCase.simulateBatch(requests)
        assertEquals(2, results.size)
        assertEquals(1L, results[0].homeClubId)
        assertEquals(2L, results[1].homeClubId)
    }
}

