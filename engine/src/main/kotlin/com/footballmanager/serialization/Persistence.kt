package com.footballmanager.serialization

import com.footballmanager.model.Game
import java.io.File
import kotlinx.serialization.json.Json

/** Shared JSON configuration for save/load. */
val gameJson: Json = Json {
    prettyPrint = true
    prettyPrintIndent = "  "
    encodeDefaults = true
    ignoreUnknownKeys = true
}

/** Serialize this [Game] to [path] as pretty-printed JSON. Returns the game for chaining. */
fun Game.saveToFile(path: String): Game {
    File(path).writeText(gameJson.encodeToString(Game.serializer(), this))
    return this
}

/** Deserialize a [Game] previously written by [saveToFile]. */
fun Game.Companion.loadFromFile(path: String): Game {
    val text = File(path).readText()
    return try {
        gameJson.decodeFromString(Game.serializer(), text)
    } catch (_: Exception) {
        val dto = gameJson.decodeFromString(SaveGameDto.serializer(), text)
        SaveValidator.validate(dto)
        SaveGameMapper.toDomain(dto)
    }
}
