package com.footballmanager.model

import java.time.LocalDate

/**
 * Root aggregate of a save game: the clubs, players, competitions and fixture
 * calendar the simulation engine operates on. Entities reference each other by
 * id (rather than object references) so this maps cleanly onto persistence later.
 */
data class Game(
    val name: String,
    val currentDate: LocalDate,
    val clubs: Map<Long, Club> = emptyMap(),
    val players: Map<Long, Player> = emptyMap(),
    val competitions: Map<Long, Competition> = emptyMap(),
    val calendar: Calendar = Calendar(),
) {
    fun club(id: Long): Club = clubs.getValue(id)

    fun player(id: Long): Player = players.getValue(id)

    fun competition(id: Long): Competition = competitions.getValue(id)

    /** Players currently registered to [clubId], in squad order. */
    fun squad(clubId: Long): List<Player> =
        club(clubId).squad.playerIds.map { player(it) }
}
