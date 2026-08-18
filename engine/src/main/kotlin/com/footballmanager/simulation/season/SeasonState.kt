package com.footballmanager.simulation.season

import com.footballmanager.model.League
import com.footballmanager.serialization.LocalDateSerializer
import com.footballmanager.simulation.MatchResult
import com.footballmanager.simulation.Tactics
import com.footballmanager.simulation.Team
import java.time.LocalDate
import kotlinx.serialization.Serializable

/**
 * A resumable snapshot of a season in progress.
 *
 * [fixtures] is the full schedule in round order and [nextFixtureIndex] counts
 * how many of them have already been played, so [results] and [standings] always
 * reflect exactly that prefix. [teams] is the source of truth for match
 * simulation, so tactics changes ([setTactics]) flow into the next matchday.
 */
@Serializable
data class SeasonState(
    val league: League,
    val teams: List<Team>,
    val fixtures: List<Fixture>,
    val nextFixtureIndex: Int = 0,
    val results: List<MatchResult> = emptyList(),
    val standings: Standings,
    @Serializable(with = LocalDateSerializer::class)
    val currentDate: LocalDate,
    val humanClubId: Long? = null,
) {
    init {
        require(nextFixtureIndex in 0..fixtures.size) { "nextFixtureIndex out of range" }
        require(results.size == nextFixtureIndex) {
            "results (${results.size}) must match played fixtures ($nextFixtureIndex)"
        }
    }

    val isFinished: Boolean get() = nextFixtureIndex >= fixtures.size

    val remainingFixtures: Int get() = fixtures.size - nextFixtureIndex

    /** Round number of the next matchday to play, or null once finished. */
    val nextMatchday: Int? get() = fixtures.getOrNull(nextFixtureIndex)?.round

    /** Returns a copy with [tactics] applied to the given club's team and standing row. */
    fun setTactics(clubId: Long, tactics: Tactics): SeasonState {
        val updated = teams.firstOrNull { it.clubId == clubId }?.copy(tactics = tactics) ?: return this
        return copy(
            teams = teams.map { if (it.clubId == clubId) updated else it },
            standings = standings.copy(
                entries = standings.entries.map {
                    if (it.team.clubId == clubId) it.copy(team = updated) else it
                },
            ),
        )
    }
}
