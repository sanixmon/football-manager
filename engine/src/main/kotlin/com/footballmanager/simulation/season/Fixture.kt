package com.footballmanager.simulation.season

import com.footballmanager.serialization.LocalDateSerializer
import com.footballmanager.simulation.Team
import java.time.LocalDate
import kotlinx.serialization.Serializable

/** A scheduled league match in a season. */
@Serializable
data class Fixture(
    val round: Int,
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,
    val home: Team,
    val away: Team,
)
