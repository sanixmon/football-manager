package com.footballmanager.integration

import com.footballmanager.mod.ModLoader
import com.footballmanager.model.Game
import com.footballmanager.repository.InMemoryPlayerRepository
import com.footballmanager.repository.PlayerRepository
import com.footballmanager.seed.SeedData
import com.footballmanager.simulation.Formation
import com.footballmanager.simulation.Tactics
import com.footballmanager.simulation.season.SeasonRunner
import com.footballmanager.usecase.CalculateTeamStrengthUseCase
import com.footballmanager.usecase.MatchRequest
import com.footballmanager.usecase.SelectLineupUseCase
import com.footballmanager.usecase.SimulateMatchUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class EngineIntegrationTest {

    @Test
    fun `full simulation loop from repository to match simulation works seamlessly`() {
        val game: Game = SeedData.game()
        val playerRepo: PlayerRepository = InMemoryPlayerRepository(game.players.values.toList())

        assertEquals(180, playerRepo.findAll().size)

        val club1 = game.club(1L)
        val club2 = game.club(2L)

        val squad1 = club1.squad.playerIds.mapNotNull { playerRepo.findById(it) }
        val squad2 = club2.squad.playerIds.mapNotNull { playerRepo.findById(it) }

        val selectLineupUseCase = SelectLineupUseCase()
        val calculateTeamStrengthUseCase = CalculateTeamStrengthUseCase(selectLineupUseCase)
        val simulateMatchUseCase = SimulateMatchUseCase()

        val team1 = calculateTeamStrengthUseCase.buildTeamFromSquad(club1.id, squad1, Tactics(formation = Formation.FOUR_FOUR_TWO))
        val team2 = calculateTeamStrengthUseCase.buildTeamFromSquad(club2.id, squad2, Tactics(formation = Formation.FOUR_THREE_THREE))

        val matchResult = simulateMatchUseCase(MatchRequest(homeTeam = team1, awayTeam = team2, seed = 42L))

        assertNotNull(matchResult)
        assertEquals(club1.id, matchResult.homeClubId)
        assertEquals(club2.id, matchResult.awayClubId)
        assertTrue(matchResult.homeScore >= 0)
        assertTrue(matchResult.awayScore >= 0)
    }

    @Test
    fun `mod loading with player repository initializes game state properly`() {
        val jsonMod = """
        {
            "name": "Integration Test League",
            "startDate": "2026-08-01",
            "league": { "name": "Super League" },
            "clubs": [
                {
                    "name": "Club Alpha",
                    "shortName": "ALP",
                    "formation": "4-4-2",
                    "players": [
                        { "name": "Player A1", "position": "GK", "age": 25, "attributes": { "POSITIONING": 80 } },
                        { "name": "Player A2", "position": "CB", "age": 24, "attributes": { "TACKLING": 75 } },
                        { "name": "Player A3", "position": "CB", "age": 26, "attributes": { "TACKLING": 78 } },
                        { "name": "Player A4", "position": "LB", "age": 22, "attributes": { "PACE": 82 } },
                        { "name": "Player A5", "position": "RB", "age": 23, "attributes": { "PACE": 80 } },
                        { "name": "Player A6", "position": "CM", "age": 27, "attributes": { "PASSING": 85 } },
                        { "name": "Player A7", "position": "CM", "age": 28, "attributes": { "PASSING": 83 } },
                        { "name": "Player A8", "position": "LM", "age": 21, "attributes": { "DRIBBLING": 84 } },
                        { "name": "Player A9", "position": "RM", "age": 22, "attributes": { "DRIBBLING": 81 } },
                        { "name": "Player A10", "position": "ST", "age": 29, "attributes": { "FINISHING": 88 } },
                        { "name": "Player A11", "position": "ST", "age": 20, "attributes": { "FINISHING": 79 } }
                    ]
                }
            ]
        }
        """.trimIndent()

        val repo = InMemoryPlayerRepository()
        val game = ModLoader.loadFromJson(jsonMod, repo)

        assertEquals("Integration Test League", game.name)
        assertEquals(11, repo.findAll().size)
        val club = game.club(1L)
        assertEquals("Club Alpha", club.name)
        assertEquals(11, club.squad.playerIds.size)
    }
}
