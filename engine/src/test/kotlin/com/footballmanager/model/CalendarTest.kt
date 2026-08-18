package com.footballmanager.model

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CalendarTest {

    private val early = LocalDate.of(2026, 8, 20)
    private val late = LocalDate.of(2026, 8, 27)

    @Test
    fun `fixtures are sorted by date`() {
        val calendar = Calendar(
            listOf(
                Fixture(1, competitionId = 100, date = late, homeClubId = 1, awayClubId = 2),
                Fixture(2, competitionId = 100, date = early, homeClubId = 1, awayClubId = 3),
            ),
        )
        assertEquals(listOf(2L, 1L), calendar.fixtures().map { it.id })
    }

    @Test
    fun `fixtures are filterable by club and date`() {
        val calendar = Calendar(
            listOf(
                Fixture(1, competitionId = 100, date = late, homeClubId = 1, awayClubId = 2),
                Fixture(2, competitionId = 100, date = early, homeClubId = 3, awayClubId = 4),
            ),
        )
        assertEquals(listOf(1L), calendar.fixturesFor(1).map { it.id })
        assertEquals(listOf(2L), calendar.fixturesOn(early).map { it.id })
    }

    @Test
    fun `nextFixture returns the earliest upcoming match or null`() {
        val matchday1 = Fixture(1, competitionId = 100, date = early, homeClubId = 1, awayClubId = 2)
        val matchday2 = Fixture(2, competitionId = 100, date = late, homeClubId = 3, awayClubId = 1)
        val calendar = Calendar(listOf(matchday1, matchday2))

        assertEquals(matchday2, calendar.nextFixture(1, early.plusDays(1)))
        assertNull(calendar.nextFixture(1, late.plusDays(1)))
    }
}
