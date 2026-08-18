package com.footballmanager.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A competition: a league or a cup. Club ids reference [Game.clubs]. */
@Serializable
sealed interface Competition {
    val id: Long
    val name: String
    val clubIds: List<Long>
}

@Serializable
@SerialName("league")
data class League(
    override val id: Long,
    override val name: String,
    override val clubIds: List<Long> = emptyList(),
) : Competition

@Serializable
@SerialName("cup")
data class Cup(
    override val id: Long,
    override val name: String,
    override val clubIds: List<Long> = emptyList(),
) : Competition
