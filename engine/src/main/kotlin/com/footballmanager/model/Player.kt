package com.footballmanager.model

import com.footballmanager.calculator.PlayerCalculator
import com.footballmanager.serialization.LocalDateSerializer
import java.time.LocalDate
import kotlinx.serialization.Serializable

@Serializable
enum class SquadStatus {
    KEY_PLAYER,
    FIRST_TEAM,
    ROTATION,
    BACKUP,
    YOUTH,
}

@Serializable
data class Contract(
    val weeklyWage: Long = 0L,
    @Serializable(with = LocalDateSerializer::class)
    val expiresOn: LocalDate,
    val squadStatus: SquadStatus = SquadStatus.ROTATION,
)

@Serializable
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
    /** Optional external graphics id (e.g. a community facepack id). */
    val graphicsId: Long? = null,
) {
    init {
        require(name.isNotBlank()) { "player name must not be blank" }
        require(age > 0) { "player age must be positive" }
        require(naturalPositions.isNotEmpty()) { "player must have at least one natural position" }
    }

    /** Overall rating when played in [position]. */
    fun overall(position: Position): Int = attributes.overall(position)

    /** Overall rating when played in [position], modified by fitness and morale. */
    fun effectiveOverall(position: Position): Int =
        PlayerCalculator.calculateEffectiveOverall(this, position)

    /** The natural position where this player rates highest. */
    fun bestPosition(): Position =
        PlayerCalculator.findBestPosition(this)

    /** Overall rating at the player's best natural position. */
    fun bestOverall(): Int =
        PlayerCalculator.calculateBestOverall(this)
}
