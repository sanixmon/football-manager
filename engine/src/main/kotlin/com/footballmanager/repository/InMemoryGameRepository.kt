package com.footballmanager.repository

import com.footballmanager.model.Game
import java.util.concurrent.atomic.AtomicReference

class InMemoryGameRepository(
    initialGame: Game,
) : GameRepository {
    private val currentGame = AtomicReference(initialGame)

    override fun exists(): Boolean = true

    override fun getGame(): Game = currentGame.get()

    override fun reload(): Game = currentGame.get()

    override fun saveGame(game: Game) {
        currentGame.set(game)
    }
}
