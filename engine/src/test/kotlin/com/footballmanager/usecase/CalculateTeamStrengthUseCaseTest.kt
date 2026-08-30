package com.footballmanager.usecase

import com.footballmanager.model.Attribute
import com.footballmanager.model.Contract
import com.footballmanager.model.Player
import com.footballmanager.model.PlayerAttributes
import com.footballmanager.model.Position
import com.footballmanager.simulation.Tactics
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertTrue

class CalculateTeamStrengthUseCaseTest {

    private val calculateTeamStrengthUseCase = CalculateTeamStrengthUseCase()

    private fun createStarters(rating: Int): List<Player> {
        val positions = listOf(
            Position.GK, Position.LB, Position.CB, Position.CB, Position.RB,
            Position.LM, Position.CM, Position.CM, Position.RM,
            Position.ST, Position.ST
        )
        return positions.mapIndexed { index, pos ->
            Player(
                id = (index + 1).toLong(),
                name = "Starter ${index + 1}",
                age = 25,
                nationality = "ID",
                naturalPositions = listOf(pos),
                attributes = PlayerAttributes(Attribute.entries.associateWith { rating }),
                contract = Contract(expiresOn = LocalDate.of(2030, 6, 30)),
            )
        }
    }

    @Test
    fun `higher rated starters produce higher team attack and defense`() {
        val weakStarters = createStarters(50)
        val strongStarters = createStarters(85)
        val tactics = Tactics()

        val weakStrength = calculateTeamStrengthUseCase.calculateFromStarters(weakStarters, tactics)
        val strongStrength = calculateTeamStrengthUseCase.calculateFromStarters(strongStarters, tactics)

        assertTrue(strongStrength.attack > weakStrength.attack)
        assertTrue(strongStrength.defense > weakStrength.defense)
    }

    @Test
    fun `buildTeam builds Team instance with expected clubId and ratings`() {
        val starters = createStarters(75)
        val team = calculateTeamStrengthUseCase.buildTeam(clubId = 42L, starters = starters)

        assertTrue(team.clubId == 42L)
        assertTrue(team.attack in 1..100)
        assertTrue(team.defense in 1..100)
    }
}
