package com.footballmanager.simulation

import com.footballmanager.model.Attribute
import com.footballmanager.model.Contract
import com.footballmanager.model.Player
import com.footballmanager.model.PlayerAttributes
import com.footballmanager.model.Position
import java.time.LocalDate
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MatchEngineTest {

    private fun player(id: Long, positions: List<Position>, attributes: PlayerAttributes) = Player(
        id = id,
        name = "Player $id",
        age = 24,
        nationality = "ID",
        naturalPositions = positions,
        attributes = attributes,
        contract = Contract(expiresOn = LocalDate.of(2030, 6, 30)),
    )

    @Test
    fun `result reports the two clubs involved`() {
        val result = MatchEngine(KotlinRandomSource(Random(1))).simulate(
            Team(10, attack = 70, defense = 70),
            Team(20, attack = 65, defense = 65),
        )
        assertEquals(10L, result.homeClubId)
        assertEquals(20L, result.awayClubId)
    }

    @Test
    fun `home and away must be different clubs`() {
        val engine = MatchEngine()
        assertFailsWith<IllegalArgumentException> {
            engine.simulate(Team(1, 70, 70), Team(1, 70, 70))
        }
    }

    @Test
    fun `a match spans 90 minutes in 18 ticks`() {
        val result = MatchEngine(KotlinRandomSource(Random(2))).simulate(
            Team(1, 70, 70),
            Team(2, 70, 70),
        )
        assertEquals(18, MatchEngine.TICKS_PER_MATCH)
        assertEquals(MatchEngine.TICKS_PER_MATCH, result.stats.ticks)
    }

    @Test
    fun `scores are never negative and never exceed the tick count`() {
        val engine = MatchEngine(KotlinRandomSource(Random(3)))
        val teams = listOf(
            Team(1, 40, 40),
            Team(2, 60, 60),
            Team(3, 80, 80),
            Team(4, 90, 20),
        )
        for (home in teams) {
            for (away in teams) {
                if (home.clubId == away.clubId) continue
                val result = engine.simulate(home, away)
                assertTrue(result.homeScore in 0..MatchEngine.TICKS_PER_MATCH)
                assertTrue(result.awayScore in 0..MatchEngine.TICKS_PER_MATCH)
            }
        }
    }

    @Test
    fun `goal events have minutes within 1 to 90`() {
        val engine = MatchEngine(KotlinRandomSource(Random(4)))
        val result = engine.simulate(Team(1, 80, 80), Team(2, 50, 50))
        for (event in result.events.filter { it.type == MatchEventType.GOAL }) {
            assertTrue(event.minute in 1..MatchEngine.MINUTES_PER_MATCH, "bad minute: ${event.minute}")
        }
    }

    @Test
    fun `events are ordered by minute`() {
        val engine = MatchEngine(KotlinRandomSource(Random(5)))
        val result = engine.simulate(Team(1, 70, 70), Team(2, 70, 70))
        val minutes = result.events.map { it.minute }
        assertEquals(minutes.sorted(), minutes)
    }

    @Test
    fun `stats are consistent with the scoreline`() {
        val engine = MatchEngine(KotlinRandomSource(Random(6)))
        val result = engine.simulate(Team(1, 75, 70), Team(2, 60, 65))

        assertEquals(result.homeScore, result.stats.home.goals)
        assertEquals(result.awayScore, result.stats.away.goals)
        assertTrue(result.stats.home.shotsOnTarget <= result.stats.home.shots)
        assertTrue(result.stats.away.shotsOnTarget <= result.stats.away.shots)
        assertTrue(result.stats.home.goals <= result.stats.home.shotsOnTarget)
        assertTrue(result.stats.away.goals <= result.stats.away.shotsOnTarget)
        assertEquals(1.0, result.stats.homePossession + result.stats.awayPossession, 1e-9)
    }

    @Test
    fun `the same seed produces identical results`() {
        val home = Team(1, 72, 68)
        val away = Team(2, 65, 70)
        val first = MatchEngine(KotlinRandomSource(Random(42))).simulate(home, away)
        val second = MatchEngine(KotlinRandomSource(Random(42))).simulate(home, away)
        assertEquals(first, second)
    }

    @Test
    fun `when no chance is ever created the match is goalless`() {
        val engine = MatchEngine(FakeRandomSource(listOf(0.99)))
        val result = engine.simulate(Team(1, 70, 70), Team(2, 70, 70))
        assertEquals(0, result.homeScore)
        assertEquals(0, result.awayScore)
        assertTrue(result.events.isEmpty())
    }

    @Test
    fun `when every chance is converted every tick is a goal`() {
        val engine = MatchEngine(FakeRandomSource(listOf(0.0)))
        val result = engine.simulate(Team(1, 70, 70), Team(2, 70, 70))
        assertEquals(MatchEngine.TICKS_PER_MATCH, result.homeScore)
        assertEquals(0, result.awayScore)
    }

    @Test
    fun `a squad with fewer than 11 players throws`() {
        assertFailsWith<IllegalArgumentException> {
            Team.fromSquad(clubId = 1, players = emptyList())
        }
    }

    @Test
    fun `a striker-heavy squad attacks more than it defends`() {
        val stAttributes = PlayerAttributes(
            mapOf(
                Attribute.FINISHING to 85,
                Attribute.PACE to 85,
                Attribute.ACCELERATION to 85,
                Attribute.POSITIONING to 75,
                Attribute.TACKLING to 20,
            ),
        )
        val squad = (1L..11L).map { player(it, listOf(Position.ST), stAttributes) }
        val team = Team.fromSquad(clubId = 1, players = squad)
        assertTrue(team.attack > team.defense)
    }

    @Test
    fun `a defender-heavy squad defends more than it attacks`() {
        val defAttributes = PlayerAttributes(
            mapOf(
                Attribute.TACKLING to 85,
                Attribute.POSITIONING to 85,
                Attribute.STRENGTH to 85,
                Attribute.FINISHING to 20,
                Attribute.DRIBBLING to 20,
            ),
        )
        val squad = (1L..11L).map { player(it, listOf(Position.CB), defAttributes) }
        val team = Team.fromSquad(clubId = 1, players = squad)
        assertTrue(team.defense > team.attack)
    }
}
