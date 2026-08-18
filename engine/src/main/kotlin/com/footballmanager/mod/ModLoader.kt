package com.footballmanager.mod

import com.footballmanager.model.Attribute
import com.footballmanager.model.Club
import com.footballmanager.model.Contract
import com.footballmanager.model.Game
import com.footballmanager.model.League
import com.footballmanager.model.Player
import com.footballmanager.model.PlayerAttributes
import com.footballmanager.model.Position
import com.footballmanager.model.Squad
import com.footballmanager.simulation.Formation
import com.footballmanager.simulation.Mentality
import com.footballmanager.simulation.Tactics
import java.io.File
import java.time.LocalDate
import kotlinx.serialization.json.Json

/**
 * Turns a [ModFile] (or its JSON) into a populated [Game].
 *
 * The loader assigns league/club/player ids in file order and parses the
 * string-based enums (position, attribute, formation, mentality) leniently by
 * label or name. Unknown strings fail fast with a clear message so mod authors
 * catch typos early.
 */
object ModLoader {

    private val json = Json { ignoreUnknownKeys = true }

    fun loadFromJson(text: String): Game = load(json.decodeFromString(ModFile.serializer(), text))

    fun loadFromFile(path: String): Game = loadFromJson(File(path).readText())

    fun loadFromResource(path: String): Game =
        loadFromJson(
            requireNotNull(ModLoader::class.java.classLoader.getResourceAsStream(path)) {
                "resource not found: $path"
            }.bufferedReader().use { it.readText() },
        )

    fun load(modFile: ModFile): Game {
        val leagueId = 1L
        var nextClubId = 1L
        var nextPlayerId = 1L
        val clubs = linkedMapOf<Long, Club>()
        val players = linkedMapOf<Long, Player>()

        for (modClub in modFile.clubs) {
            val clubId = nextClubId++
            val playerIds = modClub.players.map { modPlayer ->
                val playerId = nextPlayerId++
                players[playerId] = toPlayer(modPlayer, playerId)
                playerId
            }
            clubs[clubId] = Club(
                id = clubId,
                name = modClub.name,
                shortName = modClub.shortName,
                leagueId = leagueId,
                squad = Squad(clubId, playerIds),
                defaultTactics = Tactics(parseFormation(modClub.formation), parseMentality(modClub.mentality)),
                graphicsId = modClub.graphicsId,
            )
        }

        val league = League(leagueId, modFile.league.name, clubs.keys.toList())
        return Game(
            name = modFile.name,
            currentDate = LocalDate.parse(modFile.startDate),
            clubs = clubs,
            players = players,
            competitions = mapOf(leagueId to league),
        )
    }

    private fun toPlayer(modPlayer: ModPlayer, id: Long): Player = Player(
        id = id,
        name = modPlayer.name,
        age = modPlayer.age,
        nationality = modPlayer.nationality,
        naturalPositions = listOf(parsePosition(modPlayer.position)),
        attributes = PlayerAttributes(
            modPlayer.attributes.entries.associate { (key, value) -> parseAttribute(key) to value },
        ),
        contract = Contract(expiresOn = LocalDate.of(2030, 6, 30)),
        graphicsId = modPlayer.graphicsId,
    )

    private fun parsePosition(input: String): Position =
        Position.entries.firstOrNull { it.name.equals(input, ignoreCase = true) }
            ?: error("unknown position: '$input'")

    private fun parseAttribute(input: String): Attribute =
        Attribute.entries.firstOrNull { it.name.equals(input, ignoreCase = true) }
            ?: error("unknown attribute: '$input'")

    private fun parseFormation(input: String): Formation =
        Formation.entries.firstOrNull { it.label.equals(input, ignoreCase = true) }
            ?: Formation.entries.firstOrNull { it.name.equals(input, ignoreCase = true) }
            ?: error("unknown formation: '$input'")

    private fun parseMentality(input: String): Mentality =
        Mentality.entries.firstOrNull { it.label.equals(input, ignoreCase = true) }
            ?: Mentality.entries.firstOrNull { it.name.equals(input, ignoreCase = true) }
            ?: error("unknown mentality: '$input'")
}
