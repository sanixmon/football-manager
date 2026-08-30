package com.footballmanager.usecase

import com.footballmanager.model.Attribute
import com.footballmanager.model.Contract
import com.footballmanager.model.Player
import com.footballmanager.model.PlayerAttributes
import com.footballmanager.model.Position
import com.footballmanager.simulation.Formation
import com.footballmanager.simulation.Tactics
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SelectLineupUseCaseTest {

    private val selectLineupUseCase = SelectLineupUseCase()

    private fun createSquad(): List<Player> {
        val positions = listOf(
            Position.GK, Position.LB, Position.CB, Position.CB, Position.RB,
            Position.LM, Position.CM, Position.CM, Position.RM,
            Position.ST, Position.ST, Position.GK, Position.CB, Position.CM, Position.ST
        )
        return positions.mapIndexed { index, pos ->
            Player(
                id = (index + 1).toLong(),
                name = "Player ${index + 1}",
                age = 22 + (index % 10),
                nationality = "ID",
                naturalPositions = listOf(pos),
                attributes = PlayerAttributes(Attribute.entries.associateWith { 70 + index }),
                contract = Contract(expiresOn = LocalDate.of(2030, 6, 30)),
            )
        }
    }

    @Test
    fun `invoking SelectLineupUseCase selects exactly 11 starters and bench substitutes`() {
        val squad = createSquad()
        val tactics = Tactics(formation = Formation.FOUR_FOUR_TWO)
        val lineup = selectLineupUseCase(squad, tactics)

        assertEquals(11, lineup.starters.size)
        assertEquals(4, lineup.substitutes.size)
        assertEquals(15, lineup.starters.size + lineup.substitutes.size)
        assertTrue(lineup.starters.distinct().size == 11)
    }
}
