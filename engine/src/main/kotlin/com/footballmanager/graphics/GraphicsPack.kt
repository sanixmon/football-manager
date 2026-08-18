package com.footballmanager.graphics

import com.footballmanager.model.Club
import com.footballmanager.model.Player
import java.nio.file.Files
import java.nio.file.Path

/** The kit variants a club can have. */
enum class KitSide(val folder: String) {
    HOME("home"),
    AWAY("away"),
    THIRD("third"),
}

/**
 * Resolves community graphics packs (logos, kits, faces) stored as PNGs keyed
 * by club/player id, following the convention:
 *
 * ```
 * <root>/logos/<clubId>.png
 * <root>/kits/<clubId>/{home,away,third}.png
 * <root>/faces/<playerId>.png
 * ```
 *
 * Non-numeric or missing files are ignored and lookups return null when no
 * asset exists. This is the engine-side mapping a future Compose layer will use
 * to render images, so it stays independent of any UI toolkit.
 */
class GraphicsPack(private val root: Path) {

    private val logos: Set<Long> = scanIds(root.resolve("logos"))
    private val faces: Set<Long> = scanIds(root.resolve("faces"))
    private val kits: Map<Long, Map<KitSide, Path>> = scanKits(root.resolve("kits"))

    fun logoPath(clubId: Long): Path? =
        root.resolve("logos").resolve("$clubId.png").takeIf { clubId in logos }

    fun facePath(playerId: Long): Path? =
        root.resolve("faces").resolve("$playerId.png").takeIf { playerId in faces }

    fun kitPath(clubId: Long, side: KitSide): Path? = kits[clubId]?.get(side)

    /** Resolves a club's logo, preferring [Club.graphicsId] then falling back to its id. */
    fun logoPath(club: Club): Path? = logoPath(club.graphicsId ?: club.id)

    /** Resolves a player's face, preferring [Player.graphicsId] then falling back to its id. */
    fun facePath(player: Player): Path? = facePath(player.graphicsId ?: player.id)

    /** Resolves a club's kit, preferring [Club.graphicsId] then falling back to its id. */
    fun kitPath(club: Club, side: KitSide): Path? = kitPath(club.graphicsId ?: club.id, side)

    private fun scanIds(dir: Path): Set<Long> {
        if (!Files.isDirectory(dir)) return emptySet()
        val ids = mutableSetOf<Long>()
        Files.newDirectoryStream(dir).use { stream ->
            for (path in stream) {
                val name = path.fileName.toString()
                if (name.endsWith(".png", ignoreCase = true)) {
                    name.substringBeforeLast('.').toLongOrNull()?.let(ids::add)
                }
            }
        }
        return ids
    }

    private fun scanKits(dir: Path): Map<Long, Map<KitSide, Path>> {
        if (!Files.isDirectory(dir)) return emptyMap()
        val result = mutableMapOf<Long, MutableMap<KitSide, Path>>()
        Files.newDirectoryStream(dir).use { stream ->
            for (clubDir in stream) {
                if (!Files.isDirectory(clubDir)) continue
                val clubId = clubDir.fileName.toString().toLongOrNull() ?: continue
                val sides = mutableMapOf<KitSide, Path>()
                for (side in KitSide.entries) {
                    val file = clubDir.resolve("${side.folder}.png")
                    if (Files.isRegularFile(file)) sides[side] = file
                }
                if (sides.isNotEmpty()) result[clubId] = sides
            }
        }
        return result
    }
}
