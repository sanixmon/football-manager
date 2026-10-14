package com.footballmanager.seed

import com.footballmanager.mod.ModLoader
import com.footballmanager.model.Position
import com.footballmanager.simulation.season.SeasonRunner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LaLigaDataTest {

    @Test
    fun `laliga data creates 10 authentic clubs and 18-player squads`() {
        val game = LaLigaData.game()

        assertEquals("LaLiga EA Sports 2026/27", game.name)
        assertEquals(10, game.clubs.size)
        assertEquals(180, game.players.size) // 10 x 18

        val realMadrid = game.clubs.values.firstOrNull { it.shortName == "RMA" }
        assertNotNull(realMadrid)
        assertEquals("Real Madrid CF", realMadrid.name)

        val rmaPlayers = game.squad(realMadrid.id)
        assertEquals(18, rmaPlayers.size)
        assertTrue(rmaPlayers.any { it.name == "Kylian Mbappé" && it.bestPosition() == Position.ST })
        assertTrue(rmaPlayers.any { it.name == "Vinícius Júnior" && it.bestPosition() == Position.LW })
        assertTrue(rmaPlayers.any { it.name == "Jude Bellingham" && it.bestPosition() == Position.AM })
        assertTrue(rmaPlayers.any { it.name == "Thibaut Courtois" && it.bestPosition() == Position.GK })

        val barcelona = game.clubs.values.firstOrNull { it.shortName == "FCB" }
        assertNotNull(barcelona)
        val fcbPlayers = game.squad(barcelona.id)
        assertTrue(fcbPlayers.any { it.name == "Lamine Yamal" && it.bestPosition() == Position.RW })
        assertTrue(fcbPlayers.any { it.name == "Dani Olmo" && it.bestPosition() == Position.AM })
        assertTrue(fcbPlayers.any { it.name == "Robert Lewandowski" && it.bestPosition() == Position.ST })

        val atletico = game.clubs.values.firstOrNull { it.shortName == "ATM" }
        assertNotNull(atletico)
        val atmPlayers = game.squad(atletico.id)
        assertTrue(atmPlayers.any { it.name == "Julián Álvarez" && it.bestPosition() == Position.ST })
        assertTrue(atmPlayers.any { it.name == "Conor Gallagher" && it.bestPosition() == Position.CM })
    }

    @Test
    fun `laliga mod json loads cleanly through ModLoader`() {
        val game = ModLoader.loadFromResource("mod/laliga.json")

        assertEquals("LaLiga EA Sports 2026/2027", game.name)
        assertEquals(10, game.clubs.size)
        assertEquals(180, game.players.size)

        val rma = game.clubs.values.first { it.shortName == "RMA" }
        assertEquals(120_000_000L, rma.finance.transferBudget)
        val mbappe = game.players.values.first { it.name == "Kylian Mbappé" }
        assertEquals(94, mbappe.potential)
    }

    @Test
    fun `laliga season simulates completely through SeasonRunner without issues`() {
        val game = LaLigaData.game()
        val runner = SeasonRunner()
        val league = game.competitions.values.first() as com.footballmanager.model.League
        val teams = LaLigaData.teams(game)

        var season = runner.start(
            league = league,
            teams = teams,
            startDate = game.currentDate,
            humanClubId = 1L,
            clubs = game.clubs,
            players = game.players,
        )

        while (!season.isFinished) {
            season = runner.playNextMatchday(season)
        }

        assertTrue(season.isFinished)
        assertEquals(90, season.results.size)
        val champion = season.standings.champion
        assertNotNull(champion)
        assertTrue(champion.points >= 30, "Champion should have 30+ points in 18 matchdays")
    }
}
