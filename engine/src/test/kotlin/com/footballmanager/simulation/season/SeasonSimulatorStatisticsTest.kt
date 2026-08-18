package com.footballmanager.simulation.season

import com.footballmanager.model.League
import com.footballmanager.simulation.KotlinRandomSource
import com.footballmanager.simulation.MatchEngine
import com.footballmanager.simulation.Team
import java.time.LocalDate
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonSimulatorStatisticsTest {

    @Test
    fun `a strong team tends to finish higher than weaker teams`() {
        val league = League(id = 1, name = "Test League")
        val strong = Team(1, attack = 80, defense = 80)
        val weakTeams = (2..8).map { Team(it.toLong(), attack = 55, defense = 55) }
        val teams = listOf(strong) + weakTeams

        val titleCounts = mutableMapOf<Long, Int>()
        val seasons = 300
        repeat(seasons) { seed ->
            val result = SeasonSimulator(MatchEngine(KotlinRandomSource(Random(seed))))
                .simulate(league, teams, LocalDate.of(2026, 8, 1))
            val championId = result.champion.team.clubId
            titleCounts[championId] = (titleCounts[championId] ?: 0) + 1
        }

        val strongTitles = titleCounts[strong.clubId] ?: 0
        assertTrue(strongTitles > 0, "strong team never won the title")
        for (weak in weakTeams) {
            assertTrue(
                strongTitles > (titleCounts[weak.clubId] ?: 0),
                "strong=$strongTitles weak(${weak.clubId})=${titleCounts[weak.clubId]}",
            )
        }
    }
}
