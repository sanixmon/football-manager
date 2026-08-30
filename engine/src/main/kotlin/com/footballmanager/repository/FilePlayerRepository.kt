package com.footballmanager.repository

import com.footballmanager.model.Player

class FilePlayerRepository(
    private val gameRepository: GameRepository,
) : PlayerRepository {

    override fun findById(id: Long): Player? =
        gameRepository.getGame().players[id]

    override fun findAll(): List<Player> =
        gameRepository.getGame().players.values.toList()

    override fun save(player: Player) {
        val currentGame = gameRepository.getGame()
        val updatedPlayers = currentGame.players + (player.id to player)
        gameRepository.saveGame(currentGame.copy(players = updatedPlayers))
    }

    override fun saveAll(players: List<Player>) {
        val currentGame = gameRepository.getGame()
        val updatedPlayers = currentGame.players + players.associateBy { it.id }
        gameRepository.saveGame(currentGame.copy(players = updatedPlayers))
    }

    override fun delete(id: Long) {
        val currentGame = gameRepository.getGame()
        val updatedPlayers = currentGame.players - id
        gameRepository.saveGame(currentGame.copy(players = updatedPlayers))
    }
}
