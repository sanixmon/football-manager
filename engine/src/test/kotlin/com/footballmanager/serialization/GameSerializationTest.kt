package com.footballmanager.serialization

import com.footballmanager.model.Attribute
import com.footballmanager.model.Calendar
import com.footballmanager.model.Club
import com.footballmanager.model.Competition
import com.footballmanager.model.Contract
import com.footballmanager.model.Cup
import com.footballmanager.model.Fixture
import com.footballmanager.model.Game
import com.footballmanager.model.League
import com.footballmanager.model.Player
import com.footballmanager.model.PlayerAttributes
import com.footballmanager.model.Position
import com.footballmanager.model.Squad
import com.footballmanager.model.SquadStatus
import com.footballmanager.seed.SeedData
import com.footballmanager.simulation.KotlinRandomSource
import com.footballmanager.simulation.MatchEngine
import com.footballmanager.simulation.season.SeasonRunner
import com.footballmanager.simulation.season.SeasonSimulator
import java.nio.file.Files
import java.time.LocalDate
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GameSerializationTest {

    @Test
    fun `round trip preserves a small game exactly`() {
        val game = smallGame()
        val path = tempPath("small-save.json")

        game.saveToFile(path)
        val loaded = Game.loadFromFile(path)

        assertEquals(game.name, loaded.name)
        assertEquals(game.currentDate, loaded.currentDate)
        assertEquals(game.clubs, loaded.clubs)
        assertEquals(game.players, loaded.players)
        assertEquals(game.competitions, loaded.competitions)
        assertEquals(game.calendar.fixtures(), loaded.calendar.fixtures())

        // spot-check a player's attributes and contract survived intact
        val original = game.players.getValue(1L)
        val restored = loaded.players.getValue(1L)
        assertEquals(original.attributes.toMap(), restored.attributes.toMap())
        assertEquals(original.contract, restored.contract)
    }

    @Test
    fun `round trip preserves the full seed world`() {
        val game = SeedData.game()
        val path = tempPath("seed-save.json")

        game.saveToFile(path)
        val loaded = Game.loadFromFile(path)

        assertEquals(game.name, loaded.name)
        assertEquals(game.currentDate, loaded.currentDate)
        assertEquals(game.clubs, loaded.clubs)
        assertEquals(game.players, loaded.players)
        assertEquals(game.competitions, loaded.competitions)
    }

    @Test
    fun `round trip preserves the season result exactly`() {
        val game = SeedData.game()
        val league = game.competitions.getValue(SeedData.LEAGUE_ID) as League
        val teams = SeedData.teams(game)

        val result = SeasonSimulator(MatchEngine(KotlinRandomSource(Random(42))))
            .simulate(league, teams, SeedData.START_DATE)

        val saved = game.copy(lastSeason = result)
        val path = tempPath("season-save.json")
        saved.saveToFile(path)
        val loaded = Game.loadFromFile(path)

        val loadedSeason = requireNotNull(loaded.lastSeason)

        // full structural equality (league + fixtures + results + standings)
        assertEquals(result, loadedSeason)
        assertEquals(result.champion, loadedSeason.champion)
        assertEquals(result.standings.entries, loadedSeason.standings.entries)

        // explicit points / GD / order comparison on the loaded table
        val before = result.standings.entries.map {
            Triple(it.team.clubId, it.points, it.goalDifference)
        }
        val after = loadedSeason.standings.entries.map {
            Triple(it.team.clubId, it.points, it.goalDifference)
        }
        assertEquals(before, after)
    }

    @Test
    fun `mid-season save and resume preserves progress`() {
        val game = SeedData.game()
        val league = game.competitions.getValue(SeedData.LEAGUE_ID) as League
        val teams = SeedData.teams(game)

        val runner = SeasonRunner(MatchEngine(KotlinRandomSource(Random(42))))
        var state = runner.start(league, teams, SeedData.START_DATE, humanClubId = 1L)
        repeat(6) { state = runner.playNextMatchday(state) }

        val saved = game.copy(currentSeason = state)
        val path = tempPath("midseason.json")
        saved.saveToFile(path)
        val loaded = Game.loadFromFile(path)
        val resumed = requireNotNull(loaded.currentSeason)

        // full structural equality of the in-progress snapshot
        assertEquals(state, resumed)
        assertEquals(state.results, resumed.results)
        assertEquals(state.standings, resumed.standings)

        // continue to completion from the loaded snapshot
        var finished = resumed
        while (!finished.isFinished) finished = runner.playNextMatchday(finished)
        assertTrue(finished.isFinished)
        assertEquals(teams.size, finished.standings.entries.size)
        assertEquals(2 * (teams.size - 1), finished.standings.entries.first().played)
    }

    @Test
    fun `mid-season save preserves player fitness and lineups`() {
        val game = SeedData.game()
        val league = game.competitions.getValue(SeedData.LEAGUE_ID) as League
        val teams = SeedData.teams(game)
        val runner = SeasonRunner(MatchEngine(KotlinRandomSource(Random(42))))

        var state = runner.start(
            league = league,
            teams = teams,
            startDate = SeedData.START_DATE,
            humanClubId = 1L,
            clubs = game.clubs,
            players = game.players,
        )
        state = runner.playNextMatchday(state)

        val saved = game.copy(players = state.players, currentSeason = state)
        val path = tempPath("condition-save.json")
        saved.saveToFile(path)
        val loaded = Game.loadFromFile(path)
        val resumed = requireNotNull(loaded.currentSeason)

        assertEquals(saved, loaded)
        assertEquals(state.players, resumed.players)
        assertEquals(state.players[1L]?.fitness, resumed.players[1L]?.fitness)
    }

    private fun tempPath(name: String): String =
        Files.createTempDirectory("fm-save").resolve(name).toString()

    private fun smallGame(): Game {
        val date = LocalDate.of(2026, 8, 1)
        val contract = Contract(
            weeklyWage = 50_000,
            expiresOn = LocalDate.of(2030, 6, 30),
            squadStatus = SquadStatus.KEY_PLAYER,
        )

        fun player(id: Long, name: String, position: Position, quality: Int): Player =
            Player(
                id = id,
                name = name,
                age = 25,
                nationality = "ID",
                naturalPositions = listOf(position),
                attributes = PlayerAttributes(
                    Attribute.entries.associateWith { attribute ->
                        (quality + attribute.ordinal).coerceIn(1, 100)
                    },
                ),
                contract = contract,
            )

        val players = mapOf(
            1L to player(1, "Player One", Position.ST, 80),
            2L to player(2, "Player Two", Position.CM, 70),
            3L to player(3, "Player Three", Position.GK, 65),
            4L to player(4, "Player Four", Position.CB, 75),
            5L to player(5, "Player Five", Position.LW, 72),
            6L to player(6, "Player Six", Position.RB, 60),
        )

        val clubs = mapOf(
            1L to Club(1, "Jakarta Raya", "JKT", 1, squad = Squad(1, listOf(1, 2, 3))),
            2L to Club(2, "Bandung Sakti", "BDG", 1, squad = Squad(2, listOf(4, 5, 6))),
        )

        val competitions = mapOf<Long, Competition>(
            1L to League(1, "Liga Nusantara", listOf(1, 2)),
            2L to Cup(2, "Piala Nusantara", listOf(1, 2)),
        )

        val fixtures = listOf(
            Fixture(1, 1, date, 1, 2),
            Fixture(2, 2, date.plusDays(7), 2, 1),
        )

        return Game(
            name = "Test Save",
            currentDate = date,
            clubs = clubs,
            players = players,
            competitions = competitions,
            calendar = Calendar(fixtures),
        )
    }
}
