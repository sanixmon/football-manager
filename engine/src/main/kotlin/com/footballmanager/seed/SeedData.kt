package com.footballmanager.seed

import com.footballmanager.model.Attribute
import com.footballmanager.model.Club
import com.footballmanager.model.Contract
import com.footballmanager.model.Game
import com.footballmanager.model.League
import com.footballmanager.model.MAX_ATTRIBUTE
import com.footballmanager.model.MIN_ATTRIBUTE
import com.footballmanager.model.Player
import com.footballmanager.model.PlayerAttributes
import com.footballmanager.model.Position
import com.footballmanager.model.PositionWeights
import com.footballmanager.model.Squad
import com.footballmanager.simulation.Team
import java.time.LocalDate
import kotlin.random.Random

/**
 * Deterministic seed data: 10 clubs, each with an 18-player squad, and a single
 * league. Player attributes are generated per position from [PositionWeights]
 * (key attributes boosted, non-key lowered) and scaled by club quality, so
 * stronger clubs are clearly better without making the season fully predictable.
 */
object SeedData {

    const val LEAGUE_ID = 1L
    const val LEAGUE_NAME = "Liga Nusantara"
    val START_DATE: LocalDate = LocalDate.of(2026, 8, 1)

    private const val SEED = 20260818L

    private data class ClubSpec(val name: String, val shortName: String, val quality: Int)

    private val clubSpecs = listOf(
        ClubSpec("Jakarta Raya", "JKT", 82),
        ClubSpec("Bandung Sakti", "BDG", 80),
        ClubSpec("Surabaya Timur", "SBY", 78),
        ClubSpec("Medan Utara", "MDN", 75),
        ClubSpec("Makassar Selatan", "MKS", 71),
        ClubSpec("Semarang City", "SMG", 68),
        ClubSpec("Palembang United", "PLM", 64),
        ClubSpec("Denpasar Bali", "DPS", 60),
        ClubSpec("Balikpapan Oilers", "BPP", 56),
        ClubSpec("Pontianak Rovers", "PTK", 52),
    )

    /** 18-player squad: 2 GK, 6 DF, 5 MF, 5 FW. */
    private val squadTemplate = listOf(
        Position.GK, Position.GK,
        Position.CB, Position.CB, Position.CB, Position.CB,
        Position.LB, Position.RB,
        Position.CDM, Position.CM, Position.CM, Position.CM,
        Position.CAM,
        Position.LW, Position.RW,
        Position.ST, Position.ST, Position.ST,
    )

    private val firstNames = listOf(
        "Adam", "Bima", "Cahyo", "Dimas", "Eko", "Fajar", "Galih", "Hendra",
        "Iqbal", "Joko", "Krisna", "Lukman", "Mario", "Nanda", "Oki", "Putra",
    )

    private val lastNames = listOf(
        "Pratama", "Saputra", "Wijaya", "Kurniawan", "Santoso", "Hidayat",
        "Nugroho", "Ramadhan", "Firmansyah", "Gunawan", "Siregar", "Wibowo",
        "Setiawan", "Mahendra", "Kusuma", "Pangestu",
    )

    /** Builds the full [Game] aggregate deterministically. */
    fun game(): Game {
        val random = Random(SEED)
        var nextPlayerId = 1L
        val clubs = linkedMapOf<Long, Club>()
        val players = linkedMapOf<Long, Player>()

        for ((index, spec) in clubSpecs.withIndex()) {
            val clubId = (index + 1).toLong()
            val playerIds = squadTemplate.map { position ->
                val playerId = nextPlayerId++
                players[playerId] = player(
                    id = playerId,
                    name = name(random),
                    position = position,
                    quality = spec.quality,
                    random = random,
                )
                playerId
            }
            clubs[clubId] = Club(
                id = clubId,
                name = spec.name,
                shortName = spec.shortName,
                leagueId = LEAGUE_ID,
                squad = Squad(clubId, playerIds),
            )
        }

        val league = League(LEAGUE_ID, LEAGUE_NAME, clubs.keys.toList())
        return Game(
            name = "Demo Save",
            currentDate = START_DATE,
            clubs = clubs,
            players = players,
            competitions = mapOf(LEAGUE_ID to league),
        )
    }

    /** Converts every club's squad into a [Team] via [Team.fromSquad], ordered by club id. */
    fun teams(game: Game): List<Team> =
        game.clubs.values.sortedBy { it.id }.map { club ->
            Team.fromSquad(club.id, game.squad(club.id))
        }

    private fun player(
        id: Long,
        name: String,
        position: Position,
        quality: Int,
        random: Random,
    ): Player = Player(
        id = id,
        name = name,
        age = 18 + random.nextInt(0, 16),
        nationality = "ID",
        naturalPositions = listOf(position),
        attributes = attributes(position, quality, random),
        contract = Contract(expiresOn = LocalDate.of(2030, 6, 30)),
    )

    /**
     * Generates attributes for [position] by boosting key attributes (from
     * [PositionWeights]) above [quality] and lowering non-key ones, plus jitter.
     */
    private fun attributes(position: Position, quality: Int, random: Random): PlayerAttributes {
        val weights = PositionWeights.weights.getValue(position)
        val values = Attribute.entries.associateWith { attribute ->
            val weight = weights[attribute] ?: 0
            (quality + (weight - 2) * 5 + random.nextInt(-6, 7))
                .coerceIn(MIN_ATTRIBUTE, MAX_ATTRIBUTE)
        }
        return PlayerAttributes(values)
    }

    private fun name(random: Random): String =
        "${firstNames[random.nextInt(firstNames.size)]} ${lastNames[random.nextInt(lastNames.size)]}"
}
