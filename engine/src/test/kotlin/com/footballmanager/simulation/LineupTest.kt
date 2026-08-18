package com.footballmanager.simulation

import com.footballmanager.model.Position
import com.footballmanager.seed.SeedData
import com.footballmanager.serialization.gameJson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class LineupTest {

    @Test
    fun `lineup requires exactly 11 unique starters`() {
        assertFailsWith<IllegalArgumentException> {
            Lineup(starters = (1L..10L).toList())
        }
        assertFailsWith<IllegalArgumentException> {
            Lineup(starters = listOf(1L, 1L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L))
        }
    }

    @Test
    fun `autoSelect assigns 11 unique starters and puts rest on bench`() {
        val game = SeedData.game()
        val squad = game.squad(1L)
        val tactics = Tactics(Formation.FOUR_THREE_THREE, Mentality.ATTACKING)

        val lineup = Lineup.autoSelect(squad, tactics)

        assertEquals(11, lineup.starters.size)
        assertEquals(11, lineup.starters.distinct().size)
        assertEquals(7, lineup.substitutes.size)
        assertEquals(squad.size, (lineup.starters + lineup.substitutes).distinct().size)
    }

    @Test
    fun `autoSelect picks goalkeeper for the GK slot`() {
        val game = SeedData.game()
        val squad = game.squad(1L)
        val tactics = Tactics(Formation.FOUR_FOUR_TWO)

        val lineup = Lineup.autoSelect(squad, tactics)
        val gk = game.player(lineup.starters.first())

        assertTrue(Position.GK in gk.naturalPositions)
    }

    @Test
    fun `autoSelect throws when squad has fewer than 11 players`() {
        val game = SeedData.game()
        val smallSquad = game.squad(1L).take(10)

        assertFailsWith<IllegalArgumentException> {
            Lineup.autoSelect(smallSquad, Tactics())
        }
    }

    @Test
    fun `lineup serializes and deserializes correctly`() {
        val lineup = Lineup(
            starters = (1L..11L).toList(),
            substitutes = listOf(12L, 13L, 14L),
        )
        val json = gameJson.encodeToString(Lineup.serializer(), lineup)
        val decoded = gameJson.decodeFromString(Lineup.serializer(), json)

        assertEquals(lineup, decoded)
    }
}
