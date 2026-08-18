package com.footballmanager.simulation.season

import com.footballmanager.simulation.Team
import java.time.LocalDate

/**
 * Generates a single round-robin schedule where every team plays every other
 * team exactly once, using the circle method. No team plays twice in a round,
 * and an odd number of teams is handled with a bye.
 */
object FixtureGenerator {

    const val DAYS_PER_ROUND = 7

    fun generate(teams: List<Team>, startDate: LocalDate): List<Fixture> {
        require(teams.size >= 2) { "a season needs at least two teams" }
        require(teams.map { it.clubId }.distinct().size == teams.size) {
            "teams must have distinct club ids"
        }

        val fixtures = mutableListOf<Fixture>()
        // Pad with a null "bye" so the circle method works for odd team counts.
        val padded: List<Team?> = if (teams.size % 2 == 1) teams + listOf<Team?>(null) else teams
        val count = padded.size
        val circle = padded.toMutableList()

        val rounds = count - 1
        for (round in 1..rounds) {
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
