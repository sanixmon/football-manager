package com.footballmanager.graphics

import java.nio.file.Files
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
    fun `empty root resolves nothing`() {
        val pack = GraphicsPack(Files.createTempDirectory("graphics-empty"))
        assertNull(pack.logoPath(1L))
        assertNull(pack.facePath(1L))
        assertNull(pack.kitPath(1L, KitSide.HOME))
    }
}
