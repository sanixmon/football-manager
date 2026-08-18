package com.footballmanager.graphics

import com.footballmanager.model.Club
import com.footballmanager.model.Contract
import com.footballmanager.model.Player
import com.footballmanager.model.PlayerAttributes
import com.footballmanager.model.Position
import java.nio.file.Files
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GraphicsPackTest {

    @Test
    fun `resolves logos, kits and faces by id`() {
        val root = Files.createTempDirectory("graphics-pack")

        Files.createDirectories(root.resolve("logos"))
        Files.write(root.resolve("logos/7.png"), byteArrayOf())
        Files.createDirectories(root.resolve("kits/7"))
        Files.write(root.resolve("kits/7/home.png"), byteArrayOf())
        Files.write(root.resolve("kits/7/away.png"), byteArrayOf())
        Files.createDirectories(root.resolve("faces"))
        Files.write(root.resolve("faces/42.png"), byteArrayOf())

        val pack = GraphicsPack(root)

        assertEquals(root.resolve("logos/7.png"), pack.logoPath(7L))
        assertNull(pack.logoPath(8L))

        assertEquals(root.resolve("kits/7/home.png"), pack.kitPath(7L, KitSide.HOME))
        assertEquals(root.resolve("kits/7/away.png"), pack.kitPath(7L, KitSide.AWAY))
        assertNull(pack.kitPath(7L, KitSide.THIRD))
        assertNull(pack.kitPath(8L, KitSide.HOME))

        assertEquals(root.resolve("faces/42.png"), pack.facePath(42L))
        assertNull(pack.facePath(43L))
    }

    @Test
    fun `missing or non-numeric files are ignored`() {
        val root = Files.createTempDirectory("graphics-ignore")
        Files.createDirectories(root.resolve("logos"))
        Files.write(root.resolve("logos/abc.png"), byteArrayOf())
        Files.write(root.resolve("logos/readme.txt"), byteArrayOf())

        val pack = GraphicsPack(root)

        assertNull(pack.logoPath(1L))
    }

    @Test
    fun `club and player lookups prefer graphics id then fall back to id`() {
        val root = Files.createTempDirectory("graphics-key")
        Files.createDirectories(root.resolve("logos"))
        Files.write(root.resolve("logos/680.png"), byteArrayOf())
        Files.createDirectories(root.resolve("faces"))
        Files.write(root.resolve("faces/42.png"), byteArrayOf())

        val pack = GraphicsPack(root)

        val mappedClub = Club(1, "Mapped", "M", 1, graphicsId = 680)
        assertEquals(root.resolve("logos/680.png"), pack.logoPath(mappedClub))

        val plainClub = Club(2, "Plain", "P", 1)
        assertNull(pack.logoPath(plainClub))

        val mappedPlayer = testPlayer(1, graphicsId = 42)
        assertEquals(root.resolve("faces/42.png"), pack.facePath(mappedPlayer))

        val plainPlayer = testPlayer(2)
        assertNull(pack.facePath(plainPlayer))
    }

    private fun testPlayer(id: Long, graphicsId: Long? = null): Player = Player(
        id = id,
        name = "Player $id",
        age = 20,
        nationality = "ID",
        naturalPositions = listOf(Position.ST),
        attributes = PlayerAttributes.uniform(50),
        contract = Contract(expiresOn = LocalDate.of(2030, 6, 30)),
        graphicsId = graphicsId,
    )

    @Test
    fun `empty root resolves nothing`() {
        val pack = GraphicsPack(Files.createTempDirectory("graphics-empty"))
        assertNull(pack.logoPath(1L))
        assertNull(pack.facePath(1L))
        assertNull(pack.kitPath(1L, KitSide.HOME))
    }
}
