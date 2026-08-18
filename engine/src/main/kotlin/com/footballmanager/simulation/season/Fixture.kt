package com.footballmanager.simulation.season

import com.footballmanager.simulation.Team
import java.time.LocalDate

/** A scheduled league match in a season. */
data class Fixture(
    val round: Int,
    val date: LocalDate,
    val home: Team,
    val away: Team,
)
