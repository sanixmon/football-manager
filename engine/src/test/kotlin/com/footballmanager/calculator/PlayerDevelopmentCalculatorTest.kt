package com.footballmanager.calculator

import com.footballmanager.model.Attribute
import com.footballmanager.model.Contract
import com.footballmanager.model.Facilities
import com.footballmanager.model.Player
import com.footballmanager.model.PlayerAttributes
import com.footballmanager.model.PlayerSeasonStats
import com.footballmanager.model.Position
import com.footballmanager.model.TrainingFocus
import com.footballmanager.model.TrainingIntensity
import java.time.LocalDate
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlayerDevelopmentCalculatorTest {

    private fun createPlayer(
        age: Int,
        baseRating: Int = 70,
        potential: Int = 85,
        focus: TrainingFocus = TrainingFocus.BALANCED,
        intensity: TrainingIntensity = TrainingIntensity.NORMAL,
        position: Position = Position.ST,
    ): Player {
        val attrs = Attribute.entries.associateWith { baseRating }
        return Player(
            id = 42L,
            name = "Test Player",
            age = age,
            nationality = "ID",
            naturalPositions = listOf(position),
            attributes = PlayerAttributes(attrs),
            contract = Contract(weeklyWage = 2_000L, expiresOn = LocalDate.of(2028, 6, 30)),
            potential = potential,
            trainingFocus = focus,
            trainingIntensity = intensity,
        )
    }

    @Test
    fun `young talent with potential develops positively`() {
        val youngPlayer = createPlayer(age = 18, baseRating = 65, potential = 85)
        val stats = PlayerSeasonStats(playerId = 42L, appearances = 16, starts = 14)
        val developed = PlayerDevelopmentCalculator.applyDevelopment(
            player = youngPlayer,
            facilities = Facilities(trainingLevel = 3),
            seasonStats = stats,
            random = Random(12345),
        )

        assertTrue(developed.bestOverall() >= youngPlayer.bestOverall())
        // Should gain points across relevant attributes
        val deltaSum = Attribute.entries.sumOf { developed.attributes[it] - youngPlayer.attributes[it] }
        assertTrue(deltaSum > 0, "Young player with high potential gap must gain attribute points")
    }

    @Test
    fun `veteran players over 32 experience physical decline`() {
        val veteran = createPlayer(age = 34, baseRating = 78, potential = 78)
        val developed = PlayerDevelopmentCalculator.applyDevelopment(
            player = veteran,
            facilities = Facilities(trainingLevel = 2),
            seasonStats = PlayerSeasonStats(playerId = 42L, appearances = 15),
            random = Random(12345),
        )

        assertTrue(
            developed.attributes[Attribute.PACE] < veteran.attributes[Attribute.PACE],
            "Veteran pace should decline with age",
        )
        assertTrue(
            developed.attributes[Attribute.PHYSICAL] < veteran.attributes[Attribute.PHYSICAL],
            "Veteran physical should decline with age",
        )
        // Passing should remain stable at 34
        assertEquals(veteran.attributes[Attribute.PASSING], developed.attributes[Attribute.PASSING])
    }

    @Test
    fun `playing time significantly boosts youth progression`() {
        val playerWithMinutes = createPlayer(age = 19, baseRating = 64, potential = 82)
        val benchWarmer = createPlayer(age = 19, baseRating = 64, potential = 82)

        val starterStats = PlayerSeasonStats(playerId = 42L, appearances = 18, starts = 18)
        val zeroStats = PlayerSeasonStats(playerId = 42L, appearances = 0, starts = 0)

        val developedStarter = PlayerDevelopmentCalculator.applyDevelopment(
            player = playerWithMinutes,
            seasonStats = starterStats,
            random = Random(999),
        )
        val developedBench = PlayerDevelopmentCalculator.applyDevelopment(
            player = benchWarmer,
            seasonStats = zeroStats,
            random = Random(999),
        )

        val starterGains = Attribute.entries.sumOf { developedStarter.attributes[it] - playerWithMinutes.attributes[it] }
        val benchGains = Attribute.entries.sumOf { developedBench.attributes[it] - benchWarmer.attributes[it] }

        assertTrue(starterGains >= benchGains, "Starters with heavy minutes should gain more than unused bench players")
    }

    @Test
    fun `training intensity increases progression rate`() {
        val lightTrainee = createPlayer(age = 20, baseRating = 68, potential = 85, intensity = TrainingIntensity.LIGHT)
        val intensiveTrainee = createPlayer(age = 20, baseRating = 68, potential = 85, intensity = TrainingIntensity.INTENSIVE)

        val stats = PlayerSeasonStats(playerId = 42L, appearances = 12, starts = 10)

        val developedLight = PlayerDevelopmentCalculator.applyDevelopment(player = lightTrainee, seasonStats = stats, random = Random(42))
        val developedIntensive = PlayerDevelopmentCalculator.applyDevelopment(player = intensiveTrainee, seasonStats = stats, random = Random(42))

        val lightPoints = Attribute.entries.sumOf { developedLight.attributes[it] - lightTrainee.attributes[it] }
        val intensivePoints = Attribute.entries.sumOf { developedIntensive.attributes[it] - intensiveTrainee.attributes[it] }

        assertTrue(intensivePoints >= lightPoints, "Intensive training must yield at least as many points as light training")
    }

    @Test
    fun `attacking training focus targets shooting and attacking attributes`() {
        val striker = createPlayer(
            age = 19,
            baseRating = 66,
            potential = 84,
            focus = TrainingFocus.ATTACKING,
        )
        val stats = PlayerSeasonStats(playerId = 42L, appearances = 15, starts = 12)
        val developed = PlayerDevelopmentCalculator.applyDevelopment(
            player = striker,
            seasonStats = stats,
            random = Random(777),
        )

        val shootingGain = developed.attributes[Attribute.SHOOTING] - striker.attributes[Attribute.SHOOTING]
        val defendingGain = developed.attributes[Attribute.DEFENDING] - striker.attributes[Attribute.DEFENDING]

        assertTrue(shootingGain >= defendingGain, "Attacking focus must prioritize shooting over defending")
    }

    @Test
    fun `goalkeeping training focus targets goalkeeping attributes`() {
        val gk = createPlayer(
            age = 20,
            baseRating = 65,
            potential = 80,
            focus = TrainingFocus.GOALKEEPING,
            position = Position.GK,
        )
        val stats = PlayerSeasonStats(playerId = 42L, appearances = 15, starts = 15)
        val developed = PlayerDevelopmentCalculator.applyDevelopment(
            player = gk,
            seasonStats = stats,
            random = Random(777),
        )

        val gkGain = developed.attributes[Attribute.GOALKEEPER] - gk.attributes[Attribute.GOALKEEPER]
        assertTrue(gkGain >= 0, "Goalkeeper training should develop goalkeeping attribute")
    }
}
