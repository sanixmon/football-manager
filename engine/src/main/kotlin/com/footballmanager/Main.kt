package com.footballmanager

import com.footballmanager.model.Game
import com.footballmanager.model.League
import com.footballmanager.seed.SeedData
import com.footballmanager.serialization.loadFromFile
import com.footballmanager.serialization.saveToFile
import com.footballmanager.simulation.KotlinRandomSource
import com.footballmanager.simulation.MatchEngine
import com.footballmanager.simulation.season.SeasonSimulator
import com.footballmanager.simulation.season.StandingEntry
import java.io.File
import kotlin.random.Random

fun main() {
    val game = SeedData.game()
    val league = game.competitions.getValue(SeedData.LEAGUE_ID) as League
    val teams = SeedData.teams(game)

    val result = SeasonSimulator(MatchEngine(KotlinRandomSource(Random(42))))
        .simulate(league, teams, SeedData.START_DATE)

    println("League : ${league.name}")
    println("Clubs  : ${teams.size}")
    println("Rounds : ${result.fixtures.maxOf { it.round }} (${result.fixtures.size} fixtures)")
    println()
    println("--- Standings before save ---")
    printStandings(result.standings.entries, game)
    println()
    val champion = game.club(result.champion.team.clubId)
    println("Champion: ${champion.name} — ${result.champion.points} pts (GD ${result.champion.goalDifference})")

    // ---- attach the completed season to the game, then save ----------------
    val savedGame = game.copy(lastSeason = result)
    val savePath = "demo-save.json"
    savedGame.saveToFile(savePath)

    // ---- load from disk and re-print standings from the LOADED object --------
    val loaded = Game.loadFromFile(savePath)
    val loadedSeason = loaded.lastSeason ?: error("loaded save has no season result")

    println()
    println("Save file   : $savePath (${File(savePath).length()} bytes)")
    println("Load OK     : clubs=${loaded.clubs.size}, players=${loaded.players.size}, competitions=${loaded.competitions.size}, season=yes")
    println()
    println("--- Standings after load (printed from loaded object) ---")
    printStandings(loadedSeason.standings.entries, loaded)
    println()
    val loadedChampion = loaded.club(loadedSeason.champion.team.clubId)
    println("Loaded champion: ${loadedChampion.name} — ${loadedSeason.champion.points} pts (GD ${loadedSeason.champion.goalDifference})")

    // ---- round-trip verification --------------------------------------------
    val identical = loaded.name == savedGame.name &&
        loaded.currentDate == savedGame.currentDate &&
        loaded.clubs == savedGame.clubs &&
        loaded.players == savedGame.players &&
        loaded.competitions == savedGame.competitions &&
        loaded.calendar.fixtures() == savedGame.calendar.fixtures() &&
        loaded.lastSeason == result

    println()
    println("Round-trip  : ${if (identical) "IDENTICAL" else "MISMATCH"}")
}

private fun printStandings(entries: List<StandingEntry>, game: Game) {
    println("%-3s %-18s %-5s %-9s %2s %2s %2s %2s %3s %3s %4s %4s".format(
        "#", "Club", "Form", "Mentality", "P", "W", "D", "L", "GF", "GA", "GD", "Pts",
    ))
    entries.forEachIndexed { index, entry ->
        val club = game.club(entry.team.clubId)
        println("%-3d %-18s %-5s %-9s %2d %2d %2d %2d %3d %3d %4d %4d".format(
            index + 1,
            club.name,
            entry.team.tactics.formation.label,
            entry.team.tactics.mentality.label,
            entry.played,
            entry.won,
            entry.drawn,
            entry.lost,
            entry.goalsFor,
            entry.goalsAgainst,
            entry.goalDifference,
            entry.points,
        ))
    }
}
