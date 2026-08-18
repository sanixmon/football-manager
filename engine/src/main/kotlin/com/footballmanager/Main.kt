package com.footballmanager

import com.footballmanager.model.Game
import com.footballmanager.model.League
import com.footballmanager.seed.SeedData
import com.footballmanager.serialization.loadFromFile
import com.footballmanager.serialization.saveToFile
import com.footballmanager.simulation.Formation
import com.footballmanager.simulation.KotlinRandomSource
import com.footballmanager.simulation.MatchEngine
import com.footballmanager.simulation.Mentality
import com.footballmanager.simulation.Tactics
import com.footballmanager.simulation.season.SeasonRunner
import com.footballmanager.simulation.season.StandingEntry
import java.io.File
import kotlin.random.Random

fun main() {
    val game = SeedData.game()
    val league = game.competitions.getValue(SeedData.LEAGUE_ID) as League
    val teams = SeedData.teams(game)

    val humanClubId = 1L
    val humanClub = game.club(humanClubId)
    val runner = SeasonRunner(MatchEngine(KotlinRandomSource(Random(42))))

    println("You manage : ${humanClub.name}")
    println("League     : ${league.name} (${teams.size} clubs)")
    println()

    // Rotate through a few tactical plans before each matchday.
    val plans = listOf(
        Tactics(Formation.FOUR_THREE_THREE, Mentality.ATTACKING),
        Tactics(Formation.FOUR_FOUR_TWO, Mentality.BALANCED),
        Tactics(Formation.FIVE_THREE_TWO, Mentality.DEFENSIVE),
    )

    var state = runner.start(league, teams, SeedData.START_DATE, humanClubId)
    val totalMatchdays = state.fixtures.maxOf { it.round }
    val saveAfterMatchday = 6
    var resumed = false

    while (!state.isFinished) {
        val matchday = state.nextMatchday ?: break
        val plan = plans[(matchday - 1) % plans.size]
        state = state.setTactics(humanClubId, plan)
        state = runner.playNextMatchday(state)

        val mine = state.results.lastOrNull { it.homeClubId == humanClubId || it.awayClubId == humanClubId }
        if (mine != null) {
            val opponentId = if (mine.homeClubId == humanClubId) mine.awayClubId else mine.homeClubId
            val venue = if (mine.homeClubId == humanClubId) "vs" else "@"
            val myGoals = if (mine.homeClubId == humanClubId) mine.homeScore else mine.awayScore
            val oppGoals = if (mine.homeClubId == humanClubId) mine.awayScore else mine.homeScore
            println(
                "MD %2d  %-5s  %s %-18s %d-%d".format(
                    matchday, plan.formation.label, venue, game.club(opponentId).name, myGoals, oppGoals,
                ),
            )
        }

        if (!resumed && matchday == saveAfterMatchday) {
            val savePath = "demo-save.json"
            game.copy(currentSeason = state).saveToFile(savePath)
            val loadedState = Game.loadFromFile(savePath).currentSeason
                ?: error("save has no current season")
            println("--- saved after MD $matchday; round-trip ${if (loadedState == state) "IDENTICAL" else "MISMATCH"} ---")
            state = loadedState
            resumed = true
        }
    }

    // Final save with the completed season attached.
    game.copy(currentSeason = state).saveToFile("demo-save.json")

    println()
    println("Final table after $totalMatchdays matchdays:")
    printStandings(state.standings.entries, game)
    println()
    val champion = game.club(state.standings.champion.team.clubId)
    println("Champion: ${champion.name} — ${state.standings.champion.points} pts (GD ${state.standings.champion.goalDifference})")
    val humanRow = state.standings.entries.first { it.team.clubId == humanClubId }
    val humanPosition = state.standings.entries.indexOfFirst { it.team.clubId == humanClubId } + 1
    println("${humanClub.name} finished ${ordinal(humanPosition)} (${humanRow.points} pts)")
    println("Save file : demo-save.json (${File("demo-save.json").length()} bytes)")
}

private fun ordinal(n: Int): String = when (n % 100) {
    11, 12, 13 -> "${n}th"
    else -> when (n % 10) {
        1 -> "${n}st"
        2 -> "${n}nd"
        3 -> "${n}rd"
        else -> "${n}th"
    }
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
