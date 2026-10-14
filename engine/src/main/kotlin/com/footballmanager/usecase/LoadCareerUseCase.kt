package com.footballmanager.usecase

import com.footballmanager.model.Game
import com.footballmanager.serialization.CorruptSaveException
import com.footballmanager.serialization.SaveGameDto
import com.footballmanager.serialization.SaveGameMapper
import com.footballmanager.serialization.SaveSlot
import com.footballmanager.serialization.SaveSlotMetadata
import com.footballmanager.serialization.SaveValidator
import com.footballmanager.serialization.gameJson
import java.io.File

class LoadCareerUseCase(
    private val baseDirectory: File = File("."),
) {
    fun execute(slot: SaveSlot = SaveSlot.MANUAL): Game {
        val targetFile = File(baseDirectory, slot.fileName)
        return executeFromFile(targetFile)
    }

    fun executeFromFile(file: File): Game {
        if (!file.exists()) {
            throw IllegalArgumentException("Save file does not exist: ${file.absolutePath}")
        }

        val text = file.readText()
        val dto: SaveGameDto = try {
            gameJson.decodeFromString(SaveGameDto.serializer(), text)
        } catch (e: Exception) {
            if (e is com.footballmanager.serialization.SaveException) throw e
            throw CorruptSaveException("Malformed save JSON: ${e.message}", e)
        }

        SaveValidator.validate(dto)
        return SaveGameMapper.toDomain(dto)
    }

    fun getSlotMetadata(slot: SaveSlot): SaveSlotMetadata {
        val file = File(baseDirectory, slot.fileName)
        if (!file.exists()) {
            return SaveSlotMetadata(slot = slot, exists = false)
        }
        return try {
            val text = file.readText()
            val dto = gameJson.decodeFromString(SaveGameDto.serializer(), text)
            val humanClubName = dto.clubs.firstOrNull { it.id == dto.currentSeason?.humanClubId }?.name
                ?: dto.clubs.firstOrNull()?.name
            SaveSlotMetadata(
                slot = slot,
                exists = true,
                managerName = dto.currentSeason?.managerProfile?.name ?: dto.name,
                clubName = humanClubName,
                inGameDate = dto.currentDate.toString(),
                roundInfo = "Round ${dto.currentSeason?.round ?: 1}",
                lastModified = file.lastModified(),
            )
        } catch (_: Exception) {
            SaveSlotMetadata(slot = slot, exists = true, managerName = "Corrupt save", clubName = "Unknown", lastModified = file.lastModified())
        }
    }

    fun getAllSlotsMetadata(): List<SaveSlotMetadata> =
        SaveSlot.userSlots.map { getSlotMetadata(it) }
}
