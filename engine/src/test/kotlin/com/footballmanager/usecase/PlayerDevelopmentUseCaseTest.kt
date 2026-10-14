package com.footballmanager.usecase

import com.footballmanager.model.Club
import com.footballmanager.model.League
import com.footballmanager.model.Squad
import com.footballmanager.model.TrainingFocus
import com.footballmanager.model.TrainingIntensity
import com.footballmanager.seed.SeedData
import com.footballmanager.simulation.season.SeasonRunner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PlayerDevelopmentUseCaseTest {

    @Test
    fun `setPlayerTraining updates focus and intensity immutably`() {
        val game = SeedData.game()
        val runner = SeasonRunner()
        val season = runner.start(
            league = game.competitions.values.first() as League,
            teams = SeedData.teams(game),
            startDate = game.currentDate,
            humanClubId = 1L,
            clubs = game.clubs,
            players = game.players,
        )

        val targetPlayerId = season.clubs[1L]!!.squad.playerIds.first()
        val useCase = PlayerDevelopmentUseCase()

        val updated = useCase.setPlayerTraining(
            season = season,
            playerId = targetPlayerId,
            focus = TrainingFocus.PHYSICAL,
            intensity = TrainingIntensity.INTENSIVE,
        )

        val updatedPlayer = updated.players[targetPlayerId]
        assertNotNull(updatedPlayer)
        assertEquals(TrainingFocus.PHYSICAL, updatedPlayer.trainingFocus)
        assertEquals(TrainingIntensity.INTENSIVE, updatedPlayer.trainingIntensity)

        // Original season must remain unchanged (immutability)
        val originalPlayer = season.players[targetPlayerId]
        assertNotNull(originalPlayer)
        assertEquals(TrainingFocus.BALANCED, originalPlayer.trainingFocus)
    }

    @Test
    fun `setClubSquadTraining updates all outfield players while preserving goalkeeper focus`() {
        val game = SeedData.game()
        val runner = SeasonRunner()
        val season = runner.start(
            league = game.competitions.values.first() as League,
            teams = SeedData.teams(game),
            startDate = game.currentDate,
            humanClubId = 1L,
            clubs = game.clubs,
            players = game.players,
        )

        val clubId = 1L
        val useCase = PlayerDevelopmentUseCase()

        val updated = useCase.setClubSquadTraining(
            season = season,
            clubId = clubId,
            focus = TrainingFocus.ATTACKING,
            intensity = TrainingIntensity.INTENSIVE,
        )

        val squadPlayerIds = updated.clubs[clubId]!!.squad.playerIds
        for (pId in squadPlayerIds) {
            val player = updated.players[pId]!!
            if (player.naturalPositions.any { it.name == "GK" }) {
                assertEquals(TrainingFocus.GOALKEEPING, player.trainingFocus)
            } else {
                assertEquals(TrainingFocus.ATTACKING, player.trainingFocus)
            }
            assertEquals(TrainingIntensity.INTENSIVE, player.trainingIntensity)
        }
    }

    @Test
    fun `executeAnnualDevelopment applies development across all clubs without breaking invariants`() {
        val game = SeedData.game()
        val runner = SeasonRunner()
        val season = runner.start(
            league = game.competitions.values.first() as League,
            teams = SeedData.teams(game),
            startDate = game.currentDate,
            humanClubId = 1L,
            clubs = game.clubs,
            players = game.players,
        )

        val useCase = PlayerDevelopmentUseCase()
        val progressed = useCase.executeAnnualDevelopment(season)

        assertEquals(season.players.size, progressed.players.size)
        progressed.players.values.forEach { player ->
            assertTrue(player.bestOverall() in 1..100)
        }
    }
}
