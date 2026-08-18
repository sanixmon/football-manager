package com.footballmanager.simulation.season

import com.footballmanager.simulation.Team

/** A team's row in the league table. */
data class StandingEntry(
    val team: Team,
    val played: Int = 0,
    val won: Int = 0,
    val drawn: Int = 0,
    val lost: Int = 0,
    val goalsFor: Int = 0,
    val goalsAgainst: Int = 0,
) {
    val points: Int get() = won * 3 + drawn

    val goalDifference: Int get() = goalsFor - goalsAgainst

    /** Returns a new entry reflecting a [scored]-[conceded] result. */
    fun record(scored: Int, conceded: Int): StandingEntry = copy(
        played = played + 1,
        won = won + if (scored > conceded) 1 else 0,
        drawn = drawn + if (scored == conceded) 1 else 0,
        lost = lost + if (scored < conceded) 1 else 0,
        goalsFor = goalsFor + scored,
        goalsAgainst = goalsAgainst + conceded,
    )
}
