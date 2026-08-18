package com.footballmanager.model

/** A competition: a league or a cup. Club ids reference [Game.clubs]. */
sealed interface Competition {
    val id: Long
    val name: String
    val clubIds: List<Long>
}

data class League(
    override val id: Long,
    override val name: String,
    override val clubIds: List<Long> = emptyList(),
) : Competition

data class Cup(
    override val id: Long,
    override val name: String,
    override val clubIds: List<Long> = emptyList(),
) : Competition
