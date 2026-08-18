package com.footballmanager

import com.footballmanager.model.Game
import com.footballmanager.model.League
import com.footballmanager.seed.SeedData
import com.footballmanager.simulation.KotlinRandomSource
import com.footballmanager.simulation.MatchEngine
import com.footballmanager.simulation.season.SeasonSimulator
import com.footballmanager.simulation.season.StandingEntry
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
    printStandings(result.standings.entries, game)
    println()
    val champion = game.club(result.champion.team.clubId)
    println("Champion: ${champion.name} — ${result.champion.points} pts (GD ${result.champion.goalDifference})")
}

private fun printStandings(entries: List<StandingEntry>, game: Game) {
    println("%-3s %-20s %2s %2s %2s %2s %3s %3s %4s %4s".format(
        "#", "Club", "P", "W", "D", "L", "GF", "GA", "GD", "Pts",
    ))
    entries.forEachIndexed { index, entry ->
        val club = game.club(entry.team.clubId)
        println("%-3d %-20s %2d %2d %2d %2d %3d %3d %4d %4d".format(
            index + 1,
            club.name,
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
