package com.footballmanager.usecase

import com.footballmanager.model.Club
import com.footballmanager.model.Contract
import com.footballmanager.model.InjuryStatus
import com.footballmanager.model.League
import com.footballmanager.model.Player
import com.footballmanager.model.PlayerAttributes
import com.footballmanager.model.Position
import com.footballmanager.model.Squad
import com.footballmanager.model.SuspensionStatus
import com.footballmanager.seed.SeedData
import com.footballmanager.simulation.KotlinRandomSource
import com.footballmanager.simulation.Lineup
import com.footballmanager.simulation.MatchEngine
import com.footballmanager.simulation.MatchEvent
import com.footballmanager.simulation.MatchEventType
import com.footballmanager.simulation.MatchResult
import com.footballmanager.simulation.MatchStats
import com.footballmanager.simulation.PlayerMatchStats
import com.footballmanager.simulation.Side
import com.footballmanager.simulation.Tactics
import com.footballmanager.simulation.Team
import com.footballmanager.simulation.TeamStats
import com.footballmanager.simulation.season.Fixture
import com.footballmanager.simulation.season.FixtureGenerator
import com.footballmanager.simulation.season.SeasonRunner
import com.footballmanager.simulation.season.SeasonState
import com.footballmanager.simulation.season.Standings
import java.time.LocalDate
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WorldProgressionAndDisciplineTest {

    private fun createPlayer(
        id: Long,
        clubId: Long,
        position: Position = Position.CM,
        form: Int = 60,
        fitness: Int = 100,
        injuryStatus: InjuryStatus? = null,
        suspensionStatus: SuspensionStatus? = null,
    ): Player = Player(
        id = id,
        name = "Player $id",
        age = 24,
        nationality = "ID",
        naturalPositions = listOf(position),
        attributes = PlayerAttributes.uniform(70),
        contract = Contract(expiresOn = LocalDate.of(2030, 6, 30)),
        fitness = fitness,
        form = form,
        injuryStatus = injuryStatus,
        suspensionStatus = suspensionStatus,
    )

    private fun createTestSeason(): SeasonState {
        val game = SeedData.game()
        val league = game.competitions.values.first() as League
        val teams = SeedData.teams(game)
        return SeasonRunner().start(
            league = league,
            teams = teams,
            startDate = game.currentDate,
            humanClubId = 1L,
            clubs = game.clubs,
            players = game.players,
        )
    }

    // ── 1. Injury Progression Tests ──────────────────────────────────────────

    @Test
    fun `in-match injury creates 14-day injury duration and player becomes unavailable`() {
        val season = createTestSeason()
        val targetPlayerId = season.clubs[1L]!!.squad.playerIds.first()
        val targetPlayer = season.players[targetPlayerId]!!
        assertTrue(targetPlayer.isAvailable)

        val matchResult = MatchResult(
            homeClubId = 1L,
            awayClubId = 2L,
            homeScore = 1,
            awayScore = 0,
            events = listOf(
                MatchEvent(minute = 30, type = MatchEventType.INJURY, side = Side.HOME, teamId = 1L, playerId = targetPlayerId),
            ),
            stats = MatchStats(),
            playerStats = mapOf(
                targetPlayerId to PlayerMatchStats(
                    playerId = targetPlayerId,
                    teamId = 1L,
                    minutesPlayed = 30,
                    isStarter = true,
                    isInjured = true,
                ),
            ),
        )

        val updatedSeason = ApplyMatchResultToWorldUseCase().execute(season, matchResult)
        val injuredPlayer = updatedSeason.players[targetPlayerId]!!

        assertTrue(injuredPlayer.isInjured)
        assertFalse(injuredPlayer.isAvailable)
        assertEquals(14, injuredPlayer.injuryStatus?.daysRemaining)
    }

    @Test
    fun `advanceDay decrements injury duration and clears status at zero without going negative`() {
        var season = createTestSeason()
        val playerId = 1L
        val injuredPlayer = season.players[playerId]!!.copy(injuryStatus = InjuryStatus(daysRemaining = 2))
        season = season.copy(players = season.players + (playerId to injuredPlayer))

        val advanceDayUseCase = AdvanceDayUseCase()

        // Day 1: 2 -> 1
        season = advanceDayUseCase.execute(season)
        assertEquals(1, season.players[playerId]?.injuryStatus?.daysRemaining)
        assertTrue(season.players[playerId]?.isInjured == true)

        // Day 2: 1 -> 0 (cleared to null)
        season = advanceDayUseCase.execute(season)
        assertNull(season.players[playerId]?.injuryStatus)
        assertTrue(season.players[playerId]?.isAvailable == true)

        // Day 3: remains null / non-negative
        season = advanceDayUseCase.execute(season)
        assertNull(season.players[playerId]?.injuryStatus)
    }

    // ── 2. Suspension Progression Tests ──────────────────────────────────────

    @Test
    fun `straight red card creates 2 match suspension`() {
        val season = createTestSeason()
        val playerId = season.clubs[1L]!!.squad.playerIds.first()

        val matchResult = MatchResult(
            homeClubId = 1L,
            awayClubId = 2L,
            homeScore = 0,
            awayScore = 0,
            events = listOf(
                MatchEvent(minute = 45, type = MatchEventType.RED_CARD, side = Side.HOME, teamId = 1L, playerId = playerId),
            ),
            stats = MatchStats(),
            playerStats = mapOf(
                playerId to PlayerMatchStats(
                    playerId = playerId,
                    teamId = 1L,
                    minutesPlayed = 45,
                    redCards = 1,
                    isRedCarded = true,
                ),
            ),
        )

        val updatedSeason = ApplyMatchResultToWorldUseCase().execute(season, matchResult)
        val player = updatedSeason.players[playerId]!!

        assertTrue(player.isSuspended)
        assertFalse(player.isAvailable)
        assertEquals(2, player.suspensionStatus?.matchesRemaining)
        assertEquals("Straight red card", player.suspensionStatus?.reason)
    }

    @Test
    fun `two yellows in one match create 1 match suspension without double counting accumulation`() {
        val season = createTestSeason()
        val playerId = season.clubs[1L]!!.squad.playerIds.first()

        val matchResult = MatchResult(
            homeClubId = 1L,
            awayClubId = 2L,
            homeScore = 0,
            awayScore = 0,
            events = listOf(
                MatchEvent(minute = 30, type = MatchEventType.YELLOW_CARD, side = Side.HOME, teamId = 1L, playerId = playerId),
                MatchEvent(minute = 60, type = MatchEventType.RED_CARD, side = Side.HOME, teamId = 1L, playerId = playerId),
            ),
            stats = MatchStats(),
            playerStats = mapOf(
                playerId to PlayerMatchStats(
                    playerId = playerId,
                    teamId = 1L,
                    minutesPlayed = 60,
                    yellowCards = 2,
                    redCards = 1,
                    isRedCarded = true,
                ),
            ),
        )

        val updatedSeason = ApplyMatchResultToWorldUseCase().execute(season, matchResult)
        val player = updatedSeason.players[playerId]!!

        assertTrue(player.isSuspended)
        assertEquals(1, player.suspensionStatus?.matchesRemaining)
        assertEquals("Two yellow cards", player.suspensionStatus?.reason)

        // Unserved yellow accumulation should NOT increment for double-yellow
        val stats = updatedSeason.playerStats[playerId]!!
        assertEquals(0, stats.unservedYellowCards)
    }

    @Test
    fun `accumulating 5 yellow cards triggers 1 match suspension and resets counter`() {
        val season = createTestSeason()
        val playerId = season.clubs[1L]!!.squad.playerIds.first()

        // Give player 4 existing unserved yellow cards
        val existingStats = com.footballmanager.model.PlayerSeasonStats(playerId = playerId, unservedYellowCards = 4)
        val seasonWithStats = season.copy(playerStats = season.playerStats + (playerId to existingStats))

        val matchResult = MatchResult(
            homeClubId = 1L,
            awayClubId = 2L,
            homeScore = 0,
            awayScore = 0,
            events = listOf(
                MatchEvent(minute = 70, type = MatchEventType.YELLOW_CARD, side = Side.HOME, teamId = 1L, playerId = playerId),
            ),
            stats = MatchStats(),
            playerStats = mapOf(
                playerId to PlayerMatchStats(
                    playerId = playerId,
                    teamId = 1L,
                    minutesPlayed = 90,
                    yellowCards = 1,
                ),
            ),
        )

        val updatedSeason = ApplyMatchResultToWorldUseCase().execute(seasonWithStats, matchResult)
        val player = updatedSeason.players[playerId]!!
        val stats = updatedSeason.playerStats[playerId]!!

        assertTrue(player.isSuspended)
        assertEquals(1, player.suspensionStatus?.matchesRemaining)
        assertEquals(0, stats.unservedYellowCards, "Counter must reset after 5 yellows")
    }

    @Test
    fun `suspension decrement semantics - off-by-one prevention`() {
        val season = createTestSeason()
        val suspendedPlayerId = season.clubs[1L]!!.squad.playerIds.first()

        // Match 1: Player gets a straight red card (2 match suspension)
        val matchResult1 = MatchResult(
            homeClubId = 1L,
            awayClubId = 2L,
            homeScore = 0,
            awayScore = 0,
            events = emptyList(),
            stats = MatchStats(),
            playerStats = mapOf(
                suspendedPlayerId to PlayerMatchStats(
                    playerId = suspendedPlayerId,
                    teamId = 1L,
                    minutesPlayed = 50,
                    redCards = 1,
                ),
            ),
        )

        val afterMatch1 = ApplyMatchResultToWorldUseCase().execute(season, matchResult1)
        // Crucial: newly-created suspension must NOT be decremented in Match 1!
        assertEquals(2, afterMatch1.players[suspendedPlayerId]?.suspensionStatus?.matchesRemaining)
        assertFalse(afterMatch1.players[suspendedPlayerId]?.isAvailable == true)

        // Match 2: Player misses Match 2 (not in playerStats)
        val matchResult2 = MatchResult(
            homeClubId = 1L,
            awayClubId = 3L,
            homeScore = 1,
            awayScore = 0,
            events = emptyList(),
            stats = MatchStats(),
            playerStats = mapOf(
                999L to PlayerMatchStats(playerId = 999L, teamId = 1L, minutesPlayed = 90),
            ),
        )

        val afterMatch2 = ApplyMatchResultToWorldUseCase().execute(afterMatch1, matchResult2)
        // Match 2 was missed: matchesRemaining drops 2 -> 1
        assertEquals(1, afterMatch2.players[suspendedPlayerId]?.suspensionStatus?.matchesRemaining)
        assertFalse(afterMatch2.players[suspendedPlayerId]?.isAvailable == true)

        // Match 3: Player misses Match 3
        val matchResult3 = MatchResult(
            homeClubId = 1L,
            awayClubId = 4L,
            homeScore = 2,
            awayScore = 1,
            events = emptyList(),
            stats = MatchStats(),
            playerStats = mapOf(
                999L to PlayerMatchStats(playerId = 999L, teamId = 1L, minutesPlayed = 90),
            ),
        )

        val afterMatch3 = ApplyMatchResultToWorldUseCase().execute(afterMatch2, matchResult3)
        // Match 3 was missed: matchesRemaining drops 1 -> 0 and clears!
        assertNull(afterMatch3.players[suspendedPlayerId]?.suspensionStatus)
        assertTrue(afterMatch3.players[suspendedPlayerId]?.isAvailable == true)
    }

    // ── 3. Form Progression Tests ────────────────────────────────────────────

    @Test
    fun `match performance alters form within bounds and leaves unused players untouched`() {
        val season = createTestSeason()
        val playerGoodId = season.clubs[1L]!!.squad.playerIds[0]
        val playerBadId = season.clubs[1L]!!.squad.playerIds[1]
        val playerUnusedId = season.clubs[1L]!!.squad.playerIds[2]

        val initialGoodForm = season.players[playerGoodId]!!.form
        val initialBadForm = season.players[playerBadId]!!.form
        val initialUnusedForm = season.players[playerUnusedId]!!.form

        val matchResult = MatchResult(
            homeClubId = 1L,
            awayClubId = 2L,
            homeScore = 3,
            awayScore = 0,
            events = emptyList(),
            stats = MatchStats(),
            playerStats = mapOf(
                playerGoodId to PlayerMatchStats(playerId = playerGoodId, teamId = 1L, minutesPlayed = 90, goals = 2),
                playerBadId to PlayerMatchStats(playerId = playerBadId, teamId = 1L, minutesPlayed = 90, yellowCards = 1),
                // playerUnusedId has 0 minutes / not in playerStats
            ),
            playerRatings = mapOf(
                playerGoodId to 8.5,
                playerBadId to 4.8,
            ),
        )

        val updatedSeason = ApplyMatchResultToWorldUseCase().execute(season, matchResult)

        val updatedGood = updatedSeason.players[playerGoodId]!!
        val updatedBad = updatedSeason.players[playerBadId]!!
        val updatedUnused = updatedSeason.players[playerUnusedId]!!

        assertTrue(updatedGood.form > initialGoodForm, "Good performance should improve form")
        assertTrue(updatedBad.form < initialBadForm, "Bad performance should reduce form")
        assertEquals(initialUnusedForm, updatedUnused.form, "Unused player form must remain untouched")
        assertTrue(updatedGood.form in 1..100)
        assertTrue(updatedBad.form in 1..100)
    }

    // ── 4. Standings & Ordering Tests ────────────────────────────────────────

    @Test
    fun `standings order deterministically by points, goal difference, goals for, and clubId fallback`() {
        val teams = listOf(
            Team(1L, 70, 70),
            Team(2L, 70, 70),
            Team(3L, 70, 70),
        )
        val fixtures = listOf(
            Fixture(round = 1, date = LocalDate.of(2026, 8, 1), home = teams[0], away = teams[1]),
            Fixture(round = 1, date = LocalDate.of(2026, 8, 1), home = teams[1], away = teams[2]),
        )
        // Match 1: Club 1 beats Club 2 (3-0) -> Club 1 has 3 pts, +3 GD, 3 GF
        // Match 2: Club 2 beats Club 3 (2-1) -> Club 2 has 3 pts, -2 GD
        val results = listOf<MatchResult>(
            MatchResult(1L, 2L, 3, 0, emptyList(), MatchStats()),
            MatchResult(2L, 3L, 2, 1, emptyList(), MatchStats()),
        )

        val standings = Standings.compute(teams, fixtures, results)

        assertEquals(1L, standings.entries[0].team.clubId) // 3 pts, +3 GD
        assertEquals(2L, standings.entries[1].team.clubId) // 3 pts, -2 GD
        assertEquals(3L, standings.entries[2].team.clubId) // 0 pts, -1 GD

        // Test deterministic tie-breaker when all metrics are identical: clubId asc
        val tiedTeams = listOf(Team(5L, 70, 70), Team(2L, 70, 70))
        val tiedFixtures = listOf(
            Fixture(round = 1, date = LocalDate.of(2026, 8, 1), home = tiedTeams[0], away = tiedTeams[1]),
        )
        val tiedResults = listOf<MatchResult>(
            MatchResult(5L, 2L, 1, 1, emptyList(), MatchStats()),
        )
        val tiedStandings = Standings.compute(tiedTeams, tiedFixtures, tiedResults)
        // Both have 1 pt, 0 GD, 1 GF -> sorted by clubId (2L comes before 5L)
        assertEquals(2L, tiedStandings.entries[0].team.clubId)
        assertEquals(5L, tiedStandings.entries[1].team.clubId)
    }

    // ── 5. Direct Transfers Tests ────────────────────────────────────────────

    @Test
    fun `direct transfer moves player between clubs and updates balances`() {
        val season = createTestSeason()
        val sourceClubId = 1L
        val destClubId = 2L
        val playerToTransfer = season.players[season.clubs[sourceClubId]!!.squad.playerIds.first()]!!

        val sourceInitialBalance = season.clubs[sourceClubId]!!.finance.balance
        val destInitialBalance = season.clubs[destClubId]!!.finance.balance
        val fee = 250_000L

        val transferUseCase = DirectTransferUseCase()
        val updatedSeason = transferUseCase.execute(
            season = season,
            request = DirectTransferRequest(
                playerId = playerToTransfer.id,
                fromClubId = sourceClubId,
                toClubId = destClubId,
                transferFee = fee,
            ),
        )

        val updatedSource = updatedSeason.clubs[sourceClubId]!!
        val updatedDest = updatedSeason.clubs[destClubId]!!

        // Player moved
        assertFalse(playerToTransfer.id in updatedSource.squad.playerIds)
        assertTrue(playerToTransfer.id in updatedDest.squad.playerIds)

        // Finances updated
        assertEquals(sourceInitialBalance + fee, updatedSource.finance.balance)
        assertEquals(destInitialBalance - fee, updatedDest.finance.balance)

        // Exactly one club owns player
        val owningClubs = updatedSeason.clubs.values.filter { playerToTransfer.id in it.squad.playerIds }
        assertEquals(1, owningClubs.size)
        assertEquals(destClubId, owningClubs.first().id)
    }

    @Test
    fun `direct transfer rejects invalid requests`() {
        val season = createTestSeason()
        val transferUseCase = DirectTransferUseCase()

        // 1. Invalid player id
        assertFailsWith<IllegalArgumentException> {
            transferUseCase.execute(season, DirectTransferRequest(999999L, 1L, 2L, 1000L))
        }

        // 2. Source doesn't own player
        val playerFromClub2 = season.clubs[2L]!!.squad.playerIds.first()
        assertFailsWith<IllegalArgumentException> {
            transferUseCase.execute(season, DirectTransferRequest(playerFromClub2, 1L, 3L, 1000L))
        }

        // 3. Player already registered to destination
        val playerFromClub1 = season.clubs[1L]!!.squad.playerIds.first()
        assertFailsWith<IllegalArgumentException> {
            transferUseCase.execute(season, DirectTransferRequest(playerFromClub1, 1L, 1L, 1000L))
        }

        // 4. Insufficient balance
        assertFailsWith<IllegalArgumentException> {
            transferUseCase.execute(season, DirectTransferRequest(playerFromClub1, 1L, 2L, 999_999_999_999L))
        }
    }

    // ── 6. End-to-End Headless World Progression Test ─────────────────────────

    @Test
    fun `end-to-end headless season matchday progression and AI fixtures`() {
        val initialSeason = createTestSeason()
        val runner = SeasonRunner()

        assertEquals(0, initialSeason.nextFixtureIndex)
        assertEquals(0, initialSeason.results.size)
        val initialPlayedCount = initialSeason.standings.entries.sumOf { it.played }
        assertEquals(0, initialPlayedCount)

        // Play Matchday 1
        val afterRound1 = runner.playNextMatchday(initialSeason)

        // 5 fixtures in 10-team league
        assertEquals(5, afterRound1.results.size)
        assertEquals(5, afterRound1.nextFixtureIndex)
        val playedRound1 = afterRound1.standings.entries.sumOf { it.played }
        assertEquals(10, playedRound1) // 5 fixtures * 2 teams = 10 team appearances

        // Results are stored and matchday advanced
        assertNotNull(afterRound1.nextMatchday)
        assertEquals(2, afterRound1.nextMatchday)

        // Cannot replay completed fixtures
        val nextFixture = afterRound1.fixtures[afterRound1.nextFixtureIndex]
        assertEquals(2, nextFixture.round)
    }

    @Test
    fun `straight red preceded by yellow in same match results in 2 match suspension`() {
        val season = createTestSeason()
        val playerId = season.clubs[1L]!!.squad.playerIds.first()

        // Player gets a yellow card, then later in the match receives a straight red card
        val matchResult = MatchResult(
            homeClubId = 1L,
            awayClubId = 2L,
            homeScore = 0,
            awayScore = 0,
            events = listOf(
                MatchEvent(minute = 30, type = MatchEventType.YELLOW_CARD, side = Side.HOME, teamId = 1L, playerId = playerId),
                MatchEvent(minute = 75, type = MatchEventType.RED_CARD, side = Side.HOME, teamId = 1L, playerId = playerId, description = "Violent conduct straight red"),
            ),
            stats = MatchStats(),
            playerStats = mapOf(
                playerId to PlayerMatchStats(
                    playerId = playerId,
                    teamId = 1L,
                    minutesPlayed = 75,
                    yellowCards = 1,
                    redCards = 1,
                    isRedCarded = true,
                ),
            ),
        )

        val updatedSeason = ApplyMatchResultToWorldUseCase().execute(season, matchResult)
        val player = updatedSeason.players[playerId]!!

        assertTrue(player.isSuspended)
        // Must be 2-match ban (straight red), NOT 1-match ban (two yellows)
        assertEquals(2, player.suspensionStatus?.matchesRemaining)
        assertEquals("Straight red card", player.suspensionStatus?.reason)
    }

    @Test
    fun `direct transfer removes player from source club lineup and records transfer history`() {
        val season = createTestSeason()
        val sourceClubId = 1L
        val destClubId = 2L
        val playerToTransfer = season.players[season.clubs[sourceClubId]!!.squad.playerIds.first()]!!

        // Set a lineup for source club with the player as a starter
        val sourceLineup = Lineup.autoSelect(season.clubs[sourceClubId]!!.squad.playerIds.mapNotNull { season.players[it] }, Tactics())
        val seasonWithLineup = season.setLineup(sourceClubId, sourceLineup)
        assertTrue(playerToTransfer.id in seasonWithLineup.lineups[sourceClubId]!!.starters)

        val updatedSeason = DirectTransferUseCase().execute(
            season = seasonWithLineup,
            request = DirectTransferRequest(
                playerId = playerToTransfer.id,
                fromClubId = sourceClubId,
                toClubId = destClubId,
                transferFee = 100_000L,
            ),
        )

        // Lineup must NOT contain transferred player anymore
        val updatedLineup = updatedSeason.lineups[sourceClubId]
        if (updatedLineup != null) {
            assertFalse(playerToTransfer.id in updatedLineup.starters)
            assertFalse(playerToTransfer.id in updatedLineup.substitutes)
        }

        // Transfer history recorded
        assertEquals(1, updatedSeason.transferHistory.size)
        val record = updatedSeason.transferHistory.first()
        assertEquals(playerToTransfer.id, record.playerId)
        assertEquals(sourceClubId, record.fromClubId)
        assertEquals(destClubId, record.toClubId)
        assertEquals(100_000L, record.fee)
    }
}
