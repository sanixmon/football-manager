package com.footballmanager.usecase

import com.footballmanager.model.Game
import com.footballmanager.serialization.SaveGameDto
import com.footballmanager.serialization.SaveGameMapper
import com.footballmanager.serialization.SaveSlot
import com.footballmanager.serialization.gameJson
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class SaveCareerUseCase(
    private val baseDirectory: File = File("."),
) {
    fun execute(game: Game, slot: SaveSlot = SaveSlot.MANUAL): File {
        baseDirectory.mkdirs()
        val targetFile = File(baseDirectory, slot.fileName)
        return executeToFile(game, targetFile)
    }

    fun executeToFile(game: Game, targetFile: File): File {
        val parent = targetFile.parentFile ?: baseDirectory
        parent.mkdirs()
        val tempFile = File(parent, "${targetFile.name}.tmp")

        val dto = SaveGameMapper.toDto(game)
        val json = gameJson.encodeToString(SaveGameDto.serializer(), dto)

        tempFile.writeText(json)

        try {
            Files.move(
                tempFile.toPath(),
                targetFile.toPath(),
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING,
            )
        } catch (_: Exception) {
            Files.move(
                tempFile.toPath(),
                targetFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
            )
        }

        return targetFile
    }
}
