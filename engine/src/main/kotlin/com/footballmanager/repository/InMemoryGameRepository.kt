package com.footballmanager.repository

import com.footballmanager.model.Game

class InMemoryGameRepository(
    private var currentGame: Game,
) : GameRepository {

    override fun exists(): Boolean = true

    override fun getGame(): Game = currentGame

    override fun reload(): Game = currentGame

    override fun saveGame(game: Game) {
        this.currentGame = game
    }
}
