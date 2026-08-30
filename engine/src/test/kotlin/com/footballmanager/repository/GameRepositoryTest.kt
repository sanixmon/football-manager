package com.footballmanager.repository

import com.footballmanager.seed.SeedData
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GameRepositoryTest {

    @Test
    fun `JsonFileGameRepository saves, reloads, and caches game state`() {
        val tempFile = File.createTempFile("test-game-save", ".json")
        tempFile.deleteOnExit()
        tempFile.delete()

        val defaultGame = SeedData.game()
        val repo = JsonFileGameRepository(tempFile) { defaultGame }

        assertTrue(!repo.exists())
        val initialGame = repo.getGame()
        assertTrue(repo.exists())
        assertEquals(defaultGame.name, initialGame.name)

        val modifiedGame = initialGame.copy(name = "Persisted League Save")
        repo.saveGame(modifiedGame)

        val reloaded = repo.reload()
        assertEquals("Persisted League Save", reloaded.name)
    }

    @Test
    fun `FilePlayerRepository delegates updates to GameRepository`() {
        val game = SeedData.game()
        val gameRepo = InMemoryGameRepository(game)
        val playerRepo = FilePlayerRepository(gameRepo)

        assertEquals(180, playerRepo.findAll().size)
        val firstPlayer = playerRepo.findById(1L)
        assertTrue(firstPlayer != null)

        val modifiedPlayer = firstPlayer.copy(name = "Super Striker")
        playerRepo.save(modifiedPlayer)

        assertEquals("Super Striker", playerRepo.findById(1L)?.name)
        assertEquals("Super Striker", gameRepo.getGame().player(1L).name)
    }
}
