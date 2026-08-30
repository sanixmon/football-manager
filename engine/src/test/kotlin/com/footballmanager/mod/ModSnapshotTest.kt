package com.footballmanager.mod

import com.footballmanager.model.Attribute
import com.footballmanager.model.Position
import com.footballmanager.repository.InMemoryPlayerRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ModSnapshotTest {

    @Test
    fun `sample-mod resource matches golden schema snapshot deterministically`() {
        val repo = InMemoryPlayerRepository()
        val game = ModLoader.loadFromResource("mod/sample-mod.json", repo)

        assertEquals("Contoh Liga Mod", game.name)
        assertEquals(3, game.clubs.size)
        assertEquals(6, repo.findAll().size)

        // Validate Club Alpha snapshot
        val alpha = game.club(1L)
        assertEquals("Klub Alpha", alpha.name)
        assertEquals("ALP", alpha.shortName)
        assertEquals(680L, alpha.graphicsId)
        assertEquals(3, alpha.squad.size)

        // Validate Player Budi Santoso snapshot
        val budi = repo.findById(1L)
        assertNotNull(budi)
        assertEquals("Budi Santoso", budi.name)
        assertEquals(26, budi.age)
        assertEquals(Position.ST, budi.naturalPositions.first())
        assertEquals(98012345L, budi.graphicsId)
        assertEquals(88, budi.attributes[Attribute.FINISHING])
        assertEquals(85, budi.attributes[Attribute.PACE])
        assertEquals(80, budi.attributes[Attribute.COMPOSURE])
    }
}
