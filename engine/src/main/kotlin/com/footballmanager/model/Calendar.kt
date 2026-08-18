package com.footballmanager.model

import java.time.LocalDate

/** A scheduled match. Result and events are added by the match engine (Phase 2). */
data class Fixture(
    val id: Long,
    val competitionId: Long,
    val date: LocalDate,
    val homeClubId: Long,
    val awayClubId: Long,
)

/** The fixture calendar for a game. Fixtures are kept sorted by date. */
class Calendar(fixtures: List<Fixture> = emptyList()) {

    private val fixtures: List<Fixture> = fixtures.sortedBy { it.date }

    val size: Int get() = fixtures.size

    fun fixtures(): List<Fixture> = fixtures

    fun fixturesOn(date: LocalDate): List<Fixture> =
        fixtures.filter { it.date == date }

    fun fixturesFor(clubId: Long): List<Fixture> =
        fixtures.filter { it.homeClubId == clubId || it.awayClubId == clubId }

    /** The first fixture involving [clubId] on or after [from]. */
    fun nextFixture(clubId: Long, from: LocalDate): Fixture? =
        fixturesFor(clubId).firstOrNull { it.date >= from }
}
