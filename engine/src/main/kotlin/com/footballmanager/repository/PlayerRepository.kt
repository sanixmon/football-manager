package com.footballmanager.repository

import com.footballmanager.model.Player

interface PlayerRepository {
    fun findById(id: Long): Player?
    fun findAll(): List<Player>
    fun save(player: Player)
    fun saveAll(players: List<Player>)
    fun delete(id: Long)
}

class InMemoryPlayerRepository(
    initialPlayers: List<Player> = emptyList(),
) : PlayerRepository {
    private val store = mutableMapOf<Long, Player>().apply {
        initialPlayers.forEach { put(it.id, it) }
    }

    override fun findById(id: Long): Player? = store[id]

    override fun findAll(): List<Player> = store.values.toList()

    override fun save(player: Player) {
        store[player.id] = player
    }

    override fun saveAll(players: List<Player>) {
        players.forEach { store[it.id] = it }
    }

    override fun delete(id: Long) {
        store.remove(id)
    }
}
