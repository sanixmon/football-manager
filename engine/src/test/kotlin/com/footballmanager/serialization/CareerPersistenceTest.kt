package com.footballmanager.serialization

import com.footballmanager.model.Attribute
import com.footballmanager.model.Club
import com.footballmanager.model.Contract
import com.footballmanager.model.Game
import com.footballmanager.model.InjuryStatus
import com.footballmanager.model.League
import com.footballmanager.model.Player
import com.footballmanager.model.PlayerAttributes
import com.footballmanager.model.Position
import com.footballmanager.model.Squad
import com.footballmanager.model.SuspensionStatus
import com.footballmanager.seed.SeedData
import com.footballmanager.simulation.Formation
import com.footballmanager.simulation.KotlinRandomSource
import com.footballmanager.simulation.Lineup
import com.footballmanager.simulation.MatchEngine
import com.footballmanager.simulation.Tactics
import com.footballmanager.simulation.Team
import com.footballmanager.simulation.season.SeasonRunner
import com.footballmanager.simulation.season.SeasonState
import com.footballmanager.usecase.DirectTransferRequest
import com.footballmanager.usecase.DirectTransferUseCase
import com.footballmanager.usecase.LoadCareerUseCase
import com.footballmanager.usecase.SaveCareerUseCase
import java.io.File
import java.nio.file.Files
import java.time.LocalDate
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CareerPersistenceTest {

    private fun tempDir(): File =
        Files.createTempDirectory("career-save-test").toFile()

    @Test
    fun `canonical state serializes and deserializes cleanly via SaveCareerUseCase and LoadCareerUseCase`() {
        val dir = tempDir()
        val saveUseCase = SaveCareerUseCase(dir)
        val loadUseCase = LoadCareerUseCase(dir)

        val game = SeedData.game()
        val savedFile = saveUseCase.execute(game, SaveSlot.MANUAL)

        assertTrue(savedFile.exists())
        assertEquals("manual_save.json", savedFile.name)

        val loadedGame = loadUseCase.execute(SaveSlot.MANUAL)

        assertEquals(game.name, loadedGame.name)
        assertEquals(game.currentDate, loadedGame.currentDate)
        assertEquals(game.clubs.keys, loadedGame.clubs.keys)
        assertEquals(game.players.keys, loadedGame.players.keys)

        // Verify player canonical attributes and form preserved
        val pOriginal = game.players.values.first()
        val pLoaded = loadedGame.players.getValue(pOriginal.id)
        assertEquals(pOriginal.attributes.toMap(), pLoaded.attributes.toMap())
        assertEquals(pOriginal.form, pLoaded.form)
        assertEquals(pOriginal.potential, pLoaded.potential)
    }

    @Test
    fun `unsupported save version is rejected with UnsupportedSaveVersionException`() {
        val dir = tempDir()
        val file = File(dir, "manual_save.json")
        val invalidVersionJson = """
        {
            "saveVersion": 999,
            "name": "Future Save",
            "currentDate": "2026-08-01",
            "clubs": [],
            "players": [],
            "league": { "id": 1, "name": "L", "clubIds": [] }
        }
        """.trimIndent()
        file.writeText(invalidVersionJson)

        val loadUseCase = LoadCareerUseCase(dir)
        val ex = assertFailsWith<UnsupportedSaveVersionException> {
            loadUseCase.execute(SaveSlot.MANUAL)
        }
        assertEquals(999, ex.version)
    }

    @Test
    fun `corrupt malformed JSON is rejected with CorruptSaveException`() {
        val dir = tempDir()
        val file = File(dir, "manual_save.json")
        file.writeText("{ this is definitely not valid json }")

        val loadUseCase = LoadCareerUseCase(dir)
        assertFailsWith<CorruptSaveException> {
            loadUseCase.execute(SaveSlot.MANUAL)
        }
    }

    @Test
    fun `duplicate player ID is rejected with CorruptSaveException`() {
        val dir = tempDir()
        val file = File(dir, "manual_save.json")
        val duplicatePlayerJson = """
        {
            "saveVersion": 1,
            "name": "Corrupt Save",
            "currentDate": "2026-08-01",
            "clubs": [],
            "players": [
                {
                    "id": 10, "name": "P1", "age": 20, "nationality": "ID", "naturalPositions": ["ST"],
                    "attributes": { "SHOOTING": 70 }, "weeklyWage": 1000, "contractExpiresOn": "2030-06-30"
                },
                {
                    "id": 10, "name": "P2 Duplicate", "age": 22, "nationality": "ID", "naturalPositions": ["CB"],
                    "attributes": { "DEFENDING": 70 }, "weeklyWage": 1000, "contractExpiresOn": "2030-06-30"
                }
            ],
            "league": { "id": 1, "name": "L", "clubIds": [] }
        }
        """.trimIndent()
        file.writeText(duplicatePlayerJson)

        val loadUseCase = LoadCareerUseCase(dir)
        val ex = assertFailsWith<CorruptSaveException> {
            loadUseCase.execute(SaveSlot.MANUAL)
        }
        assertTrue(ex.message?.contains("Duplicate player ID") == true)
    }

    @Test
    fun `player registered to multiple clubs is rejected with CorruptSaveException`() {
        val dir = tempDir()
        val file = File(dir, "manual_save.json")
        val multiClubJson = """
        {
            "saveVersion": 1,
            "name": "Corrupt Save",
            "currentDate": "2026-08-01",
            "clubs": [
                { "id": 1, "name": "C1", "shortName": "C1", "leagueId": 1, "balance": 1000, "transferBudget": 1000, "weeklyWageBudget": 100, "playerIds": [10] },
                { "id": 2, "name": "C2", "shortName": "C2", "leagueId": 1, "balance": 1000, "transferBudget": 1000, "weeklyWageBudget": 100, "playerIds": [10] }
            ],
            "players": [
                {
                    "id": 10, "name": "P1", "age": 20, "nationality": "ID", "naturalPositions": ["ST"],
                    "attributes": { "SHOOTING": 70 }, "weeklyWage": 1000, "contractExpiresOn": "2030-06-30"
                }
            ],
            "league": { "id": 1, "name": "L", "clubIds": [1, 2] }
        }
        """.trimIndent()
        file.writeText(multiClubJson)

        val loadUseCase = LoadCareerUseCase(dir)
        val ex = assertFailsWith<CorruptSaveException> {
            loadUseCase.execute(SaveSlot.MANUAL)
        }
        assertTrue(ex.message?.contains("multiple clubs") == true)
    }

    @Test
    fun `round-trip preserves a complex mid-season career state with injuries, suspensions, and transfers`() {
        val dir = tempDir()
        val saveUseCase = SaveCareerUseCase(dir)
        val loadUseCase = LoadCareerUseCase(dir)

        val game = SeedData.game()
        val league = game.competitions.values.first() as League
        val teams = SeedData.teams(game)
        val runner = SeasonRunner(MatchEngine(KotlinRandomSource(Random(42))))

        var season = runner.start(
            league = league,
            teams = teams,
            startDate = game.currentDate,
            humanClubId = 1L,
            clubs = game.clubs,
            players = game.players,
        )

        // 1. Play 2 matchdays
        season = runner.playNextMatchday(season)
        season = runner.playNextMatchday(season)

        // 2. Perform a direct transfer
        val sourceClubId = 1L
        val destClubId = 2L
        val transferredPlayerId = season.clubs[sourceClubId]!!.squad.playerIds.first()
        season = DirectTransferUseCase().execute(
            season = season,
            request = DirectTransferRequest(
                playerId = transferredPlayerId,
                fromClubId = sourceClubId,
                toClubId = destClubId,
                transferFee = 150_000L,
            ),
        )

        // 3. Mark an injury and a suspension
        val injuredPlayerId = season.clubs[sourceClubId]!!.squad.playerIds[1]
        val suspendedPlayerId = season.clubs[destClubId]!!.squad.playerIds.first()

        val updatedPlayers = season.players.toMutableMap()
        updatedPlayers[injuredPlayerId] = updatedPlayers[injuredPlayerId]!!.copy(
            injuryStatus = InjuryStatus(description = "Torn Hamstring", daysRemaining = 21),
            form = 88,
        )
        updatedPlayers[suspendedPlayerId] = updatedPlayers[suspendedPlayerId]!!.copy(
            suspensionStatus = SuspensionStatus(matchesRemaining = 2, reason = "Red card ban"),
            form = 45,
        )
        season = season.copy(players = updatedPlayers)

        val fullGame = game.copy(
            clubs = season.clubs,
            players = season.players,
            currentSeason = season,
        )

        // Save
        saveUseCase.execute(fullGame, SaveSlot.AUTOSAVE)

        // Load
        val loadedGame = loadUseCase.execute(SaveSlot.AUTOSAVE)
        val loadedSeason = requireNotNull(loadedGame.currentSeason)

        // Verify Canonical Assertions
        assertEquals(season.nextFixtureIndex, loadedSeason.nextFixtureIndex)
        assertEquals(season.results.size, loadedSeason.results.size)
        assertEquals(season.standings.entries.size, loadedSeason.standings.entries.size)

        // Verify Transfer
        assertFalse(transferredPlayerId in loadedSeason.clubs[sourceClubId]!!.squad.playerIds)
        assertTrue(transferredPlayerId in loadedSeason.clubs[destClubId]!!.squad.playerIds)

        // Verify Injury & Suspension
        val loadedInjured = loadedSeason.players.getValue(injuredPlayerId)
        assertTrue(loadedInjured.isInjured)
        assertEquals(21, loadedInjured.injuryStatus?.daysRemaining)
        assertEquals("Torn Hamstring", loadedInjured.injuryStatus?.description)
        assertEquals(88, loadedInjured.form)

        val loadedSuspended = loadedSeason.players.getValue(suspendedPlayerId)
        assertTrue(loadedSuspended.isSuspended)
        assertEquals(2, loadedSuspended.suspensionStatus?.matchesRemaining)
        assertEquals("Red card ban", loadedSuspended.suspensionStatus?.reason)
        assertEquals(45, loadedSuspended.form)

        // Verify Derived Values Recalculated
        // Standings points and goal difference match
        for (i in season.standings.entries.indices) {
            val originalEntry = season.standings.entries[i]
            val loadedEntry = loadedSeason.standings.entries[i]
            assertEquals(originalEntry.team.clubId, loadedEntry.team.clubId)
            assertEquals(originalEntry.points, loadedEntry.points)
            assertEquals(originalEntry.goalDifference, loadedEntry.goalDifference)
        }
    }

    @Test
    fun `deterministic continuation - save and load produces equivalent progression to uninterrupted career`() {
        val dir = tempDir()
        val saveUseCase = SaveCareerUseCase(dir)
        val loadUseCase = LoadCareerUseCase(dir)

        val game = SeedData.game()
        val league = game.competitions.values.first() as League
        val teams = SeedData.teams(game)
        val runner = SeasonRunner(MatchEngine(KotlinRandomSource(Random(99))))

        var uninterrupted = runner.start(
            league = league,
            teams = teams,
            startDate = game.currentDate,
            humanClubId = 1L,
            clubs = game.clubs,
            players = game.players,
        )

        // Progress 3 rounds
        repeat(3) { uninterrupted = runner.playNextMatchday(uninterrupted) }

        // Save at round 3
        val gameToSave = game.copy(
            clubs = uninterrupted.clubs,
            players = uninterrupted.players,
            currentSeason = uninterrupted,
        )
        saveUseCase.execute(gameToSave, SaveSlot.MANUAL)

        // Branch 1: Uninterrupted plays round 4 with continuation runner
        val continuationRunner1 = SeasonRunner(MatchEngine(KotlinRandomSource(Random(12345))))
        val uninterruptedAfterRound4 = continuationRunner1.playNextMatchday(uninterrupted)

        // Branch 2: Loaded plays round 4 with identical continuation runner
        val loadedGame = loadUseCase.execute(SaveSlot.MANUAL)
        val loadedSeason = requireNotNull(loadedGame.currentSeason)
        val continuationRunner2 = SeasonRunner(MatchEngine(KotlinRandomSource(Random(12345))))
        val loadedAfterRound4 = continuationRunner2.playNextMatchday(loadedSeason)

        // Compare: Next fixture index, scores, standings
        assertEquals(uninterruptedAfterRound4.nextFixtureIndex, loadedAfterRound4.nextFixtureIndex)
        assertEquals(uninterruptedAfterRound4.results.size, loadedAfterRound4.results.size)

        for (i in uninterruptedAfterRound4.results.indices) {
            val res1 = uninterruptedAfterRound4.results[i]
            val res2 = loadedAfterRound4.results[i]
            assertEquals(res1.homeClubId, res2.homeClubId)
            assertEquals(res1.awayClubId, res2.awayClubId)
            assertEquals(res1.homeScore, res2.homeScore)
            assertEquals(res1.awayScore, res2.awayScore)
        }

        assertEquals(
            uninterruptedAfterRound4.standings.entries.map { it.points },
            loadedAfterRound4.standings.entries.map { it.points },
        )
    }

    @Test
    fun `atomic save guarantees target file is never left half-written`() {
        val dir = tempDir()
        val saveUseCase = SaveCareerUseCase(dir)
        val game = SeedData.game()

        val saveFile = saveUseCase.execute(game, SaveSlot.MANUAL)
        assertTrue(saveFile.exists())
        val originalContent = saveFile.readText()

        // Verify that no stray temp file remains after successful atomic write
        val tempFile = File(dir, "${SaveSlot.MANUAL.fileName}.tmp")
        assertFalse(tempFile.exists(), "Temporary save file should be moved/cleaned")

        // Overwrite again
        saveUseCase.execute(game, SaveSlot.MANUAL)
        assertTrue(saveFile.exists())
        assertEquals(originalContent, saveFile.readText())
    }

    @Test
    fun `emergency goalkeeper is not persisted as registered club player`() {
        val dir = tempDir()
        val saveUseCase = SaveCareerUseCase(dir)
        val loadUseCase = LoadCareerUseCase(dir)

        val game = SeedData.game()
        // Inject emergency GK with negative ID into club 1 squad
        val club1 = game.club(1L)
        val clubWithEmergencyGk = club1.copy(squad = Squad(1L, club1.squad.playerIds + listOf(-1L)))
        val gameWithEmergency = game.copy(clubs = game.clubs + (1L to clubWithEmergencyGk))

        saveUseCase.execute(gameWithEmergency, SaveSlot.MANUAL)

        val loaded = loadUseCase.execute(SaveSlot.MANUAL)
        val loadedClub1 = loaded.club(1L)

        // Emergency GK must NOT be stored in canonical squad
        assertFalse(-1L in loadedClub1.squad.playerIds)
        assertNull(loaded.players[-1L])
    }
}
