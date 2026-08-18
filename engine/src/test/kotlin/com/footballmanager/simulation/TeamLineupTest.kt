package com.footballmanager.simulation

import com.footballmanager.seed.SeedData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TeamLineupTest {

    @Test
    fun `team from lineup reflects starting players`() {
        val game = SeedData.game()
        val squad = game.squad(1L)
        val tactics = Tactics(Formation.FOUR_FOUR_TWO)
        val lineup = Lineup.autoSelect(squad, tactics)
        val playerMap = squad.associateBy { it.id }
        val starters = lineup.starters.map { playerMap.getValue(it) }

        val teamFromLineup = Team.fromLineup(1L, starters, tactics)
        val teamFromSquad = Team.fromSquad(1L, squad, tactics)

        assertEquals(teamFromLineup.attack, teamFromSquad.attack)
        assertEquals(teamFromLineup.defense, teamFromSquad.defense)
    }

    @Test
    fun `team strength drops when starters are exhausted`() {
        val game = SeedData.game()
        val squad = game.squad(1L)
        val tactics = Tactics(Formation.FOUR_FOUR_TWO)

        val freshTeam = Team.fromSquad(1L, squad, tactics)
        val tiredSquad = squad.map { it.copy(fitness = 40) }
        val tiredTeam = Team.fromSquad(1L, tiredSquad, tactics)

        assertTrue(tiredTeam.attack < freshTeam.attack, "attack should drop with low fitness")
        assertTrue(tiredTeam.defense < freshTeam.defense, "defense should drop with low fitness")
    }

    @Test
    fun `fromLineup requires exactly 11 players`() {
        val game = SeedData.game()
        val squad = game.squad(1L)
        val starters = squad.take(10)

        assertFailsWith<IllegalArgumentException> {
            Team.fromLineup(1L, starters)
        }
    }

    @Test
    fun `fromSquad throws when squad has fewer than 11 players`() {
        val game = SeedData.game()
        val smallSquad = game.squad(1L).take(10)

        assertFailsWith<IllegalArgumentException> {
            Team.fromSquad(1L, smallSquad)
        }
    }
}
