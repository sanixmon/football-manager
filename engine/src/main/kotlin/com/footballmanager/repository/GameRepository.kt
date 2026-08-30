package com.footballmanager.repository

import com.footballmanager.model.Game

interface GameRepository {
    fun getGame(): Game
    fun saveGame(game: Game)
    fun exists(): Boolean
    fun reload(): Game
}
