package com.footballmanager.simulation

import com.footballmanager.model.Attribute
import com.footballmanager.model.Contract
import com.footballmanager.model.Player
import com.footballmanager.model.PlayerAttributes
import com.footballmanager.model.Position
import com.footballmanager.seed.SeedData
import java.time.LocalDate
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
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
    fun `match duration always reaches minute 90 and all events are within 1 to 90`() {
        val result = MatchEngine(KotlinRandomSource(Random(2))).simulate(
            Team(1, 75, 75),
            Team(2, 70, 70),
        )
        assertEquals(90, result.durationMinutes)
        assertEquals(90, result.stats.durationMinutes)
        for (event in result.events) {
            assertTrue(event.minute in 1..90, "Event minute ${event.minute} outside 1..90")
        }
    }

    @Test
    fun `scores are never negative and within valid bounds`() {
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
                assertTrue(result.homeScore >= 0)
                assertTrue(result.awayScore >= 0)
                assertTrue(result.homeScore <= 90)
                assertTrue(result.awayScore <= 90)
            }
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
    fun `stats are consistent with the scoreline and invariants hold`() {
        val engine = MatchEngine(KotlinRandomSource(Random(6)))
        val result = engine.simulate(Team(1, 75, 70), Team(2, 60, 65))

        assertEquals(result.homeScore, result.stats.home.goals)
        assertEquals(result.awayScore, result.stats.away.goals)
        assertTrue(result.stats.home.shotsOnTarget <= result.stats.home.shots)
        assertTrue(result.stats.away.shotsOnTarget <= result.stats.away.shots)
        assertTrue(result.stats.home.goals <= result.stats.home.shotsOnTarget)
        assertTrue(result.stats.away.goals <= result.stats.away.shotsOnTarget)
        assertEquals(1.0, result.stats.homePossession + result.stats.awayPossession, 1e-9)

        // Player totals match team totals
        val homePlayerStats = result.playerStats.values.filter { it.teamId == result.homeClubId }
        val awayPlayerStats = result.playerStats.values.filter { it.teamId == result.awayClubId }

        assertEquals(result.stats.home.goals, homePlayerStats.sumOf { it.goals })
        assertEquals(result.stats.away.goals, awayPlayerStats.sumOf { it.goals })
        assertEquals(result.stats.home.shots, homePlayerStats.sumOf { it.shots })
        assertEquals(result.stats.away.shots, awayPlayerStats.sumOf { it.shots })
        assertEquals(result.stats.home.shotsOnTarget, homePlayerStats.sumOf { it.shotsOnTarget })
        assertEquals(result.stats.away.shotsOnTarget, awayPlayerStats.sumOf { it.shotsOnTarget })
        assertEquals(result.stats.home.yellowCards, homePlayerStats.sumOf { it.yellowCards })
        assertEquals(result.stats.away.yellowCards, awayPlayerStats.sumOf { it.yellowCards })
        assertEquals(result.stats.home.redCards, homePlayerStats.sumOf { it.redCards })
        assertEquals(result.stats.away.redCards, awayPlayerStats.sumOf { it.redCards })
    }

    @Test
    fun `determinism - same seed produces identical MatchResult`() {
        val home = Team(1, 72, 68)
        val away = Team(2, 65, 70)
        val first = MatchEngine(KotlinRandomSource(Random(42))).simulate(home, away)
        val second = MatchEngine(KotlinRandomSource(Random(42))).simulate(home, away)
        assertEquals(first, second)
        assertEquals(first.homeScore, second.homeScore)
        assertEquals(first.awayScore, second.awayScore)
        assertEquals(first.events, second.events)
        assertEquals(first.playerRatings, second.playerRatings)
        assertEquals(first.substitutions, second.substitutions)
    }

    @Test
    fun `determinism - different seeds produce different outcomes`() {
        val home = Team(1, 75, 70)
        val away = Team(2, 70, 75)
        val resultA = MatchEngine(KotlinRandomSource(Random(101))).simulate(home, away)
        val resultB = MatchEngine(KotlinRandomSource(Random(999))).simulate(home, away)

        val areEventsDifferent = resultA.events != resultB.events
        assertTrue(areEventsDifferent, "Different seeds should produce different match event sequences")
    }

    @Test
    fun `player attribution - all events identify valid participating players on the correct teams`() {
        val game = SeedData.game()
        val homeSquad = game.squad(1L)
        val awaySquad = game.squad(2L)
        val homeTeam = Team.fromSquad(1L, homeSquad)
        val awayTeam = Team.fromSquad(2L, awaySquad)

        val engine = MatchEngine(KotlinRandomSource(Random(88)))
        val result = engine.simulate(homeTeam, awayTeam)

        val homePlayerIds = homeSquad.map { it.id }.toSet()
        val awayPlayerIds = awaySquad.map { it.id }.toSet()

        for (event in result.events) {
            when (event.type) {
                MatchEventType.GOAL -> {
                    assertNotNull(event.playerId, "Goal must have a scorer")
                    val expectedTeamIds = if (event.side == Side.HOME) homePlayerIds else awayPlayerIds
                    assertTrue(event.playerId in expectedTeamIds, "Scorer must belong to scoring team")
                    if (event.secondaryPlayerId != null) {
                        assertTrue(event.secondaryPlayerId in expectedTeamIds, "Assister must belong to scoring team")
                    }
                }
                MatchEventType.SHOT, MatchEventType.SHOT_ON_TARGET -> {
                    assertNotNull(event.playerId, "Shot must have a shooter")
                    val expectedTeamIds = if (event.side == Side.HOME) homePlayerIds else awayPlayerIds
                    assertTrue(event.playerId in expectedTeamIds, "Shooter must belong to attacking team")
                }
                MatchEventType.SAVE -> {
                    assertNotNull(event.playerId, "Save must have a goalkeeper")
                    val expectedTeamIds = if (event.side == Side.HOME) homePlayerIds else awayPlayerIds
                    assertTrue(event.playerId in expectedTeamIds, "Goalkeeper must belong to defending team")
                }
                MatchEventType.YELLOW_CARD, MatchEventType.RED_CARD -> {
                    assertNotNull(event.playerId, "Card must identify player")
                    val expectedTeamIds = if (event.side == Side.HOME) homePlayerIds else awayPlayerIds
                    assertTrue(event.playerId in expectedTeamIds, "Carded player must belong to team")
                }
                MatchEventType.SUBSTITUTION -> {
                    assertNotNull(event.playerId, "Sub-out must identify player")
                    assertNotNull(event.secondaryPlayerId, "Sub-in must identify player")
                    val expectedTeamIds = if (event.side == Side.HOME) homePlayerIds else awayPlayerIds
                    assertTrue(event.playerId in expectedTeamIds)
                    assertTrue(event.secondaryPlayerId in expectedTeamIds)
                }
                else -> Unit
            }
        }
    }

    @Test
    fun `player ratings - all participants receive ratings between 1 and 10 and unused bench has no rating`() {
        val game = SeedData.game()
        val homeTeam = Team.fromSquad(1L, game.squad(1L))
        val awayTeam = Team.fromSquad(2L, game.squad(2L))

        val engine = MatchEngine(KotlinRandomSource(Random(42)))
        val result = engine.simulate(homeTeam, awayTeam)

        assertTrue(result.playerRatings.isNotEmpty())
        for ((playerId, rating) in result.playerRatings) {
            val stats = result.playerStats.getValue(playerId)
            assertTrue(stats.minutesPlayed > 0, "Only players who played minutes should have a rating")
            assertTrue(rating in 1.0..10.0, "Rating $rating outside 1.0..10.0")
        }

        // Check unused bench players have no rating
        val unusedBench = result.playerStats.values.filter { it.minutesPlayed == 0 }
        for (unused in unusedBench) {
            assertNull(result.playerRatings[unused.playerId], "Unused substitute must not have a rating")
        }
    }

    @Test
    fun `substitutions obey invariants - correct roster transitions and no duplicate entries`() {
        val game = SeedData.game()
        val homeTeam = Team.fromSquad(1L, game.squad(1L))
        val awayTeam = Team.fromSquad(2L, game.squad(2L))

        val engine = MatchEngine(KotlinRandomSource(Random(2026)))
        val result = engine.simulate(homeTeam, awayTeam)

        val enteredPlayers = mutableSetOf<Long>()
        val exitedPlayers = mutableSetOf<Long>()

        for (sub in result.substitutions) {
            // Player leaving cannot have already left
            assertTrue(sub.playerOutId !in exitedPlayers, "Player cannot leave twice")
            // Player entering cannot have already entered
            assertTrue(sub.playerInId !in enteredPlayers, "Player cannot enter twice")

            exitedPlayers.add(sub.playerOutId)
            enteredPlayers.add(sub.playerInId)

            val outStats = result.playerStats.getValue(sub.playerOutId)
            val inStats = result.playerStats.getValue(sub.playerInId)
            assertTrue(outStats.isSubstitutedOut)
            assertTrue(inStats.isSubstitutedIn)
        }
    }

    @Test
    fun `tactical influence - attacking mentality produces more shots than defensive mentality`() {
        val engine = MatchEngine(KotlinRandomSource(Random(12345)))
        val opponent = Team(3, 70, 70)

        var attackingShots = 0
        var defensiveShots = 0
        val runs = 50

        for (i in 0 until runs) {
            val resAtt = MatchEngine(KotlinRandomSource(Random(i.toLong()))).simulate(
                Team(1, 70, 70, Tactics(mentality = Mentality.ATTACKING)),
                opponent,
            )
            val resDef = MatchEngine(KotlinRandomSource(Random(i.toLong()))).simulate(
                Team(2, 70, 70, Tactics(mentality = Mentality.DEFENSIVE)),
                opponent,
            )
            attackingShots += resAtt.stats.home.shots
            defensiveShots += resDef.stats.home.shots
        }

        assertTrue(attackingShots > defensiveShots, "Attacking mentality should produce more shots ($attackingShots) than defensive ($defensiveShots)")
    }

    @Test
    fun `tactical influence - high pressing causes more fouls than low pressing`() {
        val opponent = Team(3, 70, 70)
        var highPressFouls = 0
        var lowPressFouls = 0
        val runs = 50

        for (i in 0 until runs) {
            val resHigh = MatchEngine(KotlinRandomSource(Random(i.toLong()))).simulate(
                Team(1, 70, 70, Tactics(pressing = Pressing.HIGH)),
                opponent,
            )
            val resLow = MatchEngine(KotlinRandomSource(Random(i.toLong()))).simulate(
                Team(2, 70, 70, Tactics(pressing = Pressing.LOW)),
                opponent,
            )
            highPressFouls += resHigh.stats.home.fouls
            lowPressFouls += resLow.stats.home.fouls
        }

        assertTrue(highPressFouls > lowPressFouls, "High pressing should cause more fouls ($highPressFouls) than low pressing ($lowPressFouls)")
    }

    @Test
    fun `a squad with fewer than 11 players throws in selectLineup`() {
        assertFailsWith<IllegalArgumentException> {
            Team.fromSquad(clubId = 1, players = emptyList())
        }
    }

    @Test
    fun `injured player does not remain on field when bench substitute is unavailable`() {
        val game = SeedData.game()
        val homeSquad = game.squad(1L).take(11) // exactly 11 players, NO bench
        val awaySquad = game.squad(2L)
        val homeTeam = Team.fromLineup(1L, homeSquad, substitutes = emptyList())
        val awayTeam = Team.fromSquad(2L, awaySquad)

        // Run simulations until an injury occurs to verify they leave the pitch
        var injuryFound = false
        for (seed in 1L..100L) {
            val res = MatchEngine(KotlinRandomSource(Random(seed))).simulate(homeTeam, awayTeam)
            val injuryEvent = res.events.firstOrNull { it.type == MatchEventType.INJURY && it.teamId == 1L }
            if (injuryEvent != null) {
                injuryFound = true
                val injuredId = requireNotNull(injuryEvent.playerId)
                val pStats = res.playerStats.getValue(injuredId)
                assertTrue(pStats.isInjured)
                // Injured player must not have played full 90 minutes if injured earlier
                if (injuryEvent.minute < 85) {
                    assertTrue(pStats.minutesPlayed < 90, "Injured player must leave pitch and not accumulate 90 mins")
                }
                break
            }
        }
        assertTrue(injuryFound, "Should trigger at least one injury across 100 seeded runs")
    }

    @Test
    fun `foul counts match player fouls under all circumstances`() {
        val game = SeedData.game()
        val homeTeam = Team.fromSquad(1L, game.squad(1L))
        val awayTeam = Team.fromSquad(2L, game.squad(2L))

        for (seed in 1L..20L) {
            val res = MatchEngine(KotlinRandomSource(Random(seed))).simulate(homeTeam, awayTeam)
            val homePlayerFouls = res.playerStats.values.filter { it.teamId == 1L }.sumOf { it.fouls }
            val awayPlayerFouls = res.playerStats.values.filter { it.teamId == 2L }.sumOf { it.fouls }
            assertEquals(res.stats.home.fouls, homePlayerFouls, "Home team fouls must equal sum of player fouls")
            assertEquals(res.stats.away.fouls, awayPlayerFouls, "Away team fouls must equal sum of player fouls")
        }
    }
}
