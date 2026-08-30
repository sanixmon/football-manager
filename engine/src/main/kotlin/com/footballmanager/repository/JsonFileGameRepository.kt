package com.footballmanager.repository

import com.footballmanager.model.Game
import com.footballmanager.serialization.loadFromFile
import com.footballmanager.serialization.saveToFile
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class JsonFileGameRepository(
    private val saveFile: File,
    private val defaultGameProvider: () -> Game,
) : GameRepository {
    @Volatile
    private var cachedGame: Game? = null

    override fun exists(): Boolean = saveFile.exists()

    override fun getGame(): Game {
        cachedGame?.let { return it }
        return reload()
    }

    override fun reload(): Game {
        val loaded = if (saveFile.exists()) {
            Game.loadFromFile(saveFile.absolutePath)
        } else {
            val fresh = defaultGameProvider()
            saveGame(fresh)
            fresh
        }
        cachedGame = loaded
        return loaded
    }

    override fun saveGame(game: Game) {
        val parent = saveFile.parentFile
        parent?.mkdirs()
        val tempFile = File(parent, "${saveFile.name}.tmp")
        game.saveToFile(tempFile.absolutePath)
        try {
            Files.move(
                tempFile.toPath(),
                saveFile.toPath(),
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING,
            )
        } catch (_: Exception) {
            Files.move(
                tempFile.toPath(),
                saveFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
            )
        }
        cachedGame = game
    }
}
