package com.footballmanager.model

import com.footballmanager.serialization.LocalDateSerializer
import com.footballmanager.simulation.season.SeasonResult
import com.footballmanager.simulation.season.SeasonState
import java.time.LocalDate
import kotlinx.serialization.Serializable

/**
 * Root aggregate of a save game: the clubs, players, competitions and fixture
 * calendar the simulation engine operates on. Entities reference each other by
 * id (rather than object references) so this maps cleanly onto persistence later.
 */
@Serializable
data class Game(
    val name: String,
    @Serializable(with = LocalDateSerializer::class)
    val currentDate: LocalDate,
    val clubs: Map<Long, Club> = emptyMap(),
    val players: Map<Long, Player> = emptyMap(),
    val competitions: Map<Long, Competition> = emptyMap(),
    val calendar: Calendar = Calendar(),
    /** The most recently completed season, if one has been played. */
    val lastSeason: SeasonResult? = null,
    /** The season currently in progress (resumable), if any. */
    val currentSeason: SeasonState? = null,
) {
    fun club(id: Long): Club = clubs.getValue(id)

    fun player(id: Long): Player = players.getValue(id)

    fun competition(id: Long): Competition = competitions.getValue(id)

    /** Players currently registered to [clubId], in squad order. */
    fun squad(clubId: Long): List<Player> =
        club(clubId).squad.playerIds.map { player(it) }

    companion object {}
}
