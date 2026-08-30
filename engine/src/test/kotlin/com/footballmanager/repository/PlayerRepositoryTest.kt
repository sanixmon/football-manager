package com.footballmanager.repository

import com.footballmanager.model.Attribute
import com.footballmanager.model.Contract
import com.footballmanager.model.Player
import com.footballmanager.model.PlayerAttributes
import com.footballmanager.model.Position
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PlayerRepositoryTest {

    private fun createPlayer(id: Long, name: String): Player = Player(
        id = id,
        name = name,
        age = 22,
        nationality = "ID",
        naturalPositions = listOf(Position.CM),
        attributes = PlayerAttributes(Attribute.entries.associateWith { 75 }),
        contract = Contract(expiresOn = LocalDate.of(2030, 6, 30)),
    )

    @Test
    fun `InMemoryPlayerRepository supports save, findById, findAll, and delete`() {
        val repo = InMemoryPlayerRepository()
        val p1 = createPlayer(1L, "Player One")
        val p2 = createPlayer(2L, "Player Two")

        repo.save(p1)
        repo.save(p2)

        assertEquals(2, repo.findAll().size)
        assertEquals(p1, repo.findById(1L))
        assertEquals(p2, repo.findById(2L))

        repo.delete(1L)
        assertNull(repo.findById(1L))
        assertEquals(1, repo.findAll().size)
    }
}
