package com.footballmanager.simulation.season

import com.footballmanager.simulation.Team
import java.time.LocalDate

/**
 * Generates a double round-robin schedule: every team plays every other team
 * twice, once home and once away. The first leg uses the circle method; the
 * second leg is the first leg with home/away reversed, so each club visits
 * exactly the opponents it hosted in the first leg.
 *
 * An odd number of teams is handled with a bye in each leg, so every team gets
 * the same number of byes overall.
 */
object FixtureGenerator {

    const val DAYS_PER_ROUND = 7

    fun generate(teams: List<Team>, startDate: LocalDate): List<Fixture> {
        require(teams.size >= 2) { "a season needs at least two teams" }
        require(teams.map { it.clubId }.distinct().size == teams.size) {
            "teams must have distinct club ids"
        }

        // Pad with a null "bye" so the circle method works for odd team counts.
        val padded: List<Team?> = if (teams.size % 2 == 1) teams + listOf<Team?>(null) else teams
        val roundsPerLeg = padded.size - 1

        val firstLeg = buildLeg(padded, startDate)
        val secondLeg = firstLeg.map { fixture ->
            Fixture(
                round = fixture.round + roundsPerLeg,
                date = fixture.date.plusDays(roundsPerLeg.toLong() * DAYS_PER_ROUND),
                home = fixture.away,
                away = fixture.home,
            )
        }
        return firstLeg + secondLeg
    }

    private fun buildLeg(padded: List<Team?>, startDate: LocalDate): List<Fixture> {
        val count = padded.size
        val circle = padded.toMutableList()
        val fixtures = mutableListOf<Fixture>()
        for (round in 1..(count - 1)) {
            for (i in 0 until count / 2) {
                val first = circle[i]
                val second = circle[count - 1 - i]
                if (first == null || second == null) continue

                val home: Team
                val away: Team
                if (round % 2 == 1) {
                    home = first
                    away = second
                } else {
                    home = second
                    away = first
                }

                fixtures += Fixture(
                    round = round,
                    date = startDate.plusDays((round - 1).toLong() * DAYS_PER_ROUND),
                    home = home,
                    away = away,
                )
            }
            rotate(circle)
        }
        return fixtures
    }

    private fun rotate(circle: MutableList<Team?>) {
        // Keep the first element fixed; rotate the rest cyclically by one.
        val last = circle[circle.size - 1]
        for (i in circle.size - 1 downTo 2) {
            circle[i] = circle[i - 1]
        }
        circle[1] = last
    }
}
