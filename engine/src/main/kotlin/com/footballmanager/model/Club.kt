package com.footballmanager.model

import kotlinx.serialization.Serializable

/** Money is represented as integer units of the base currency. */
@Serializable
data class Finance(
    val balance: Long = 0L,
    val transferBudget: Long = 0L,
    val weeklyWageBudget: Long = 0L,
)

@Serializable
data class Facilities(
    val trainingLevel: Int = 1,
    val youthLevel: Int = 1,
    val stadiumCapacity: Int = 0,
)

/** The players registered to a club, referenced by player id. */
@Serializable
data class Squad(
    val clubId: Long,
    val playerIds: List<Long> = emptyList(),
) {
    val size: Int get() = playerIds.size

    fun contains(playerId: Long): Boolean = playerId in playerIds
}

@Serializable
data class Club(
    val id: Long,
    val name: String,
    val shortName: String,
    val leagueId: Long,
    val finance: Finance = Finance(),
    val facilities: Facilities = Facilities(),
    val squad: Squad = Squad(id),
)
