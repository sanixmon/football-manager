package com.footballmanager.mod

import com.footballmanager.model.Attribute
import com.footballmanager.model.Position
import com.footballmanager.simulation.Formation
import com.footballmanager.simulation.Mentality
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ModLoaderTest {

    @Test
    fun `loads a mod file into a game`() {
        val modFile = ModFile(
            name = "Test Mod",
            startDate = "2026-08-01",
            league = ModLeague("Test League"),
            clubs = listOf(
                ModClub(
                    name = "Alpha",
                    shortName = "ALP",
                    formation = "4-3-3",
                    mentality = "Attacking",
                    players = listOf(
                        ModPlayer("Striker", "ST", age = 26, attributes = mapOf("FINISHING" to 90, "PACE" to 85)),
                        ModPlayer("Keeper", "GK", attributes = mapOf("POSITIONING" to 80)),
                    ),
                ),
                ModClub(
                    name = "Beta",
                    shortName = "BET",
                    formation = "5-3-2",
                    mentality = "Defensive",
                    players = listOf(ModPlayer("Defender", "CB", attributes = mapOf("TACKLING" to 88))),
                ),
            ),
        )

        val game = ModLoader.load(modFile)

        assertEquals("Test Mod", game.name)
        assertEquals(2, game.clubs.size)
        assertEquals(3, game.players.size)
        assertEquals("Test League", game.competitions.getValue(1L).name)

        val alpha = game.clubs.getValue(1L)
        assertEquals("Alpha", alpha.name)
        assertEquals(Formation.FOUR_THREE_THREE, alpha.defaultTactics?.formation)
        assertEquals(Mentality.ATTACKING, alpha.defaultTactics?.mentality)
        assertEquals(2, game.squad(alpha.id).size)

        val beta = game.clubs.getValue(2L)
        assertEquals(Formation.FIVE_THREE_TWO, beta.defaultTactics?.formation)
        assertEquals(Mentality.DEFENSIVE, beta.defaultTactics?.mentality)

        // player ids are assigned in file order
        val striker = game.players.getValue(1L)
        assertEquals("Striker", striker.name)
        assertEquals(Position.ST, striker.bestPosition())
        assertEquals(90, striker.attributes[Attribute.FINISHING])
        assertEquals(85, striker.attributes[Attribute.PACE])
        // omitted attributes default to the minimum
        assertEquals(1, striker.attributes[Attribute.TACKLING])
    }

    @Test
    fun `loads the sample mod from resources`() {
        val game = ModLoader.loadFromResource("mod/sample-mod.json")

        assertEquals("Contoh Liga Mod", game.name)
        assertTrue(game.clubs.size >= 2, "sample mod should have at least 2 clubs")
        assertTrue(game.players.size >= 5, "sample mod should have several players")
        assertEquals("Liga Contoh", game.competitions.getValue(1L).name)

        val alpha = game.clubs.getValue(1L)
        assertEquals(Formation.FOUR_THREE_THREE, alpha.defaultTactics?.formation)
    }

    @Test
    fun `unknown enum strings fail fast with a clear message`() {
        val badPosition = ModFile(
            name = "Bad",
            startDate = "2026-08-01",
            league = ModLeague("L"),
            clubs = listOf(
                ModClub(
                    name = "X",
                    shortName = "X",
                    players = listOf(ModPlayer("P", "NOT_A_POSITION")),
                ),
            ),
        )
        val error = runCatching { ModLoader.load(badPosition) }.exceptionOrNull()
        assertEquals("unknown position: 'NOT_A_POSITION'", error?.message)
    }
}
