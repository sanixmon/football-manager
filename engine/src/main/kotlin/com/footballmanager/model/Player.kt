package com.footballmanager.model

import java.time.LocalDate

enum class SquadStatus {
    KEY_PLAYER,
    FIRST_TEAM,
    ROTATION,
    BACKUP,
    YOUTH,
}

data class Contract(
    val weeklyWage: Long = 0L,
    val expiresOn: LocalDate,
    val squadStatus: SquadStatus = SquadStatus.ROTATION,
)

data class Player(
    val id: Long,
    val name: String,
    val age: Int,
    val nationality: String,
    val naturalPositions: List<Position>,
    val attributes: PlayerAttributes,
    val contract: Contract,
    val fitness: Int = 100,
    val morale: Int = 50,
) {
    init {
        require(name.isNotBlank()) { "player name must not be blank" }
        require(age > 0) { "player age must be positive" }
        require(naturalPositions.isNotEmpty()) { "player must have at least one natural position" }
    }

    /** Overall rating when played in [position]. */
    fun overall(position: Position): Int = attributes.overall(position)

    /** The natural position where this player rates highest. */
    fun bestPosition(): Position =
        naturalPositions.maxByOrNull { overall(it) }
            ?: error("player must have at least one natural position")

    /** Overall rating at the player's best natural position. */
    fun bestOverall(): Int = overall(bestPosition())
}
