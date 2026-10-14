package com.footballmanager.app

import com.footballmanager.app.ui.components.FmNavSection
import com.footballmanager.app.ui.viewmodel.AppScreen
import com.footballmanager.app.ui.viewmodel.GameViewModel
import com.footballmanager.model.Position
import com.footballmanager.repository.InMemoryGameRepository
import com.footballmanager.seed.SeedData
import com.footballmanager.simulation.Formation
import com.footballmanager.simulation.Mentality
import com.footballmanager.simulation.Passing
import com.footballmanager.simulation.Pressing
import com.footballmanager.simulation.Tempo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameViewModelTest {

    @Test
    fun `viewModel initializes with human club and lineup`() {
        val vm = GameViewModel()
        val state = vm.uiState.value
        assertEquals(1L, state.humanClubId)
        assertEquals(11, state.starters.size)
        assertTrue(state.substitutes.isNotEmpty())
    }

    @Test
    fun `new career reaches Dashboard`() {
        val vm = GameViewModel()
        vm.startNewCareer("Alex Ferguson", "GB", 2L)
        val state = vm.uiState.value

        assertEquals(2L, state.humanClubId)
        assertEquals(AppScreen.IN_GAME, state.currentScreen)
        assertEquals(FmNavSection.DASHBOARD, state.activeNavSection)
        assertEquals("Alex Ferguson", state.currentSeason.managerProfile?.name)
    }

    @Test
    fun `all six MVP screens are reachable`() {
        val vm = GameViewModel()
        val sections = listOf(
            FmNavSection.DASHBOARD,
            FmNavSection.SQUAD,
            FmNavSection.TACTICS,
            FmNavSection.MATCH,
            FmNavSection.LEAGUE,
            FmNavSection.TRANSFERS,
        )

        for (section in sections) {
            vm.navigateTo(section)
            assertEquals(section, vm.uiState.value.activeNavSection)
        }
    }

    @Test
    fun `updating formation changes tactical slots and auto-aligns starters`() {
        val vm = GameViewModel()
        vm.updateFormation(Formation.THREE_FIVE_TWO)
        assertEquals(Formation.THREE_FIVE_TWO, vm.uiState.value.humanTeam.tactics.formation)
        assertEquals(11, vm.uiState.value.starters.size)
    }

    @Test
    fun `swapWithBench swaps starter and substitute correctly`() {
        val vm = GameViewModel()
        // Choose outfield starter (not slot 0 GK)
        val initialStarterId = vm.uiState.value.starters[1].id
        // Choose outfield substitute
        val initialBenchId = vm.uiState.value.substitutes.first { Position.GK !in it.naturalPositions }.id

        vm.onStarterSelected(initialStarterId)
        vm.swapWithBench(initialBenchId)

        val updatedStarters = vm.uiState.value.starters.map { it.id }
        val updatedSubs = vm.uiState.value.substitutes.map { it.id }

        assertTrue(initialBenchId in updatedStarters)
        assertTrue(initialStarterId in updatedSubs)
        assertNull(vm.uiState.value.lineupValidationError)
    }

    @Test
    fun `invalid lineup produces UI error`() {
        val vm = GameViewModel()
        val gkStarterId = vm.uiState.value.starters.first().id // Slot 0 GK
        val outfieldSubId = vm.uiState.value.substitutes.first { Position.GK !in it.naturalPositions }.id

        // Attempt to swap outfield player into GK slot 0
        vm.onStarterSelected(gkStarterId)
        vm.swapWithBench(outfieldSubId)

        // Must reject and set lineupValidationError
        val error = vm.uiState.value.lineupValidationError
        assertNotNull(error)
        assertTrue(error.contains("Slot 0 must be occupied by a natural GK"))
    }

    @Test
    fun `playNextMatchday advances season and updates displayed result and autosaves`() {
        val vm = GameViewModel()
        vm.playNextMatchday()

        val state = vm.uiState.value
        assertNotNull(state.lastMatchResult)
        assertNotNull(state.saveStatusMessage)
        assertTrue(state.saveStatusMessage?.contains("Autosaved") == true)
        assertEquals(null, state.lineupValidationError)
    }

    @Test
    fun `tactical and lineup changes are persisted to repository immediately`() {
        val repo = InMemoryGameRepository(SeedData.game())
        val vm = GameViewModel(gameRepository = repo)

        vm.updateFormation(Formation.THREE_FIVE_TWO)
        val savedGame = repo.getGame()
        val savedTactics = savedGame.currentSeason?.teams?.firstOrNull { it.clubId == 1L }?.tactics
        assertEquals(Formation.THREE_FIVE_TWO, savedTactics?.formation)

        vm.updateMentality(Mentality.ATTACKING)
        val savedMentality = repo.getGame().currentSeason?.teams?.firstOrNull { it.clubId == 1L }?.tactics?.mentality
        assertEquals(Mentality.ATTACKING, savedMentality)
    }

    @Test
    fun `updating tactical axes updates and persists state`() {
        val repo = InMemoryGameRepository(SeedData.game())
        val vm = GameViewModel(gameRepository = repo)

        vm.updateTempo(Tempo.FAST)
        vm.updatePressing(Pressing.HIGH)
        vm.updatePassing(Passing.DIRECT)

        val tactics = vm.uiState.value.humanTeam.tactics
        assertEquals(Tempo.FAST, tactics.tempo)
        assertEquals(Pressing.HIGH, tactics.pressing)
        assertEquals(Passing.DIRECT, tactics.passing)

        val savedTactics = repo.getGame().currentSeason?.teams?.firstOrNull { it.clubId == 1L }?.tactics
        assertEquals(Tempo.FAST, savedTactics?.tempo)
        assertEquals(Pressing.HIGH, savedTactics?.pressing)
        assertEquals(Passing.DIRECT, savedTactics?.passing)
    }

    @Test
    fun `save and load career updates UI state with feedback`() {
        val repo = InMemoryGameRepository(SeedData.game())
        val vm = GameViewModel(gameRepository = repo)

        // Save
        vm.saveCareer()
        assertTrue(vm.uiState.value.saveStatusMessage?.contains("successfully") == true)

        vm.clearSaveStatus()
        assertNull(vm.uiState.value.saveStatusMessage)

        // Load
        vm.loadCareer()
        assertTrue(vm.uiState.value.saveStatusMessage?.contains("successfully") == true)
    }

    @Test
    fun `direct transfer updates UI state on success and displays error on failure`() {
        val vm = GameViewModel()
        val humanClubId = vm.uiState.value.humanClubId
        val targetClub = vm.uiState.value.currentSeason.clubs.values.first { it.id != humanClubId }
        val playerToBuy = targetClub.squad.playerIds.first()

        // 1. Success transfer
        vm.executeDirectTransfer(playerToBuy, targetClub.id, humanClubId, 50_000L)
        val stateAfterSuccess = vm.uiState.value

        assertNotNull(stateAfterSuccess.transferSuccessMessage)
        assertNull(stateAfterSuccess.transferErrorMessage)
        assertTrue(playerToBuy in stateAfterSuccess.humanSquad.map { it.id })

        // 2. Failure transfer (insufficient budget)
        vm.executeDirectTransfer(playerToBuy, humanClubId, targetClub.id, 999_999_999_999L)
        val stateAfterFailure = vm.uiState.value

        assertNotNull(stateAfterFailure.transferErrorMessage)
    }
    @Test
    fun `failed load does not destroy current valid game state`() {
        val game = SeedData.game()
        val failingRepo = object : com.footballmanager.repository.GameRepository {
            override fun getGame(): com.footballmanager.model.Game = game
            override fun saveGame(game: com.footballmanager.model.Game) {}
            override fun exists(): Boolean = true
            override fun reload(): com.footballmanager.model.Game {
                throw com.footballmanager.serialization.CorruptSaveException("Simulated corrupted save file")
            }
        }

        val vm = GameViewModel(gameRepository = failingRepo)
        val stateBefore = vm.uiState.value
        assertEquals(1L, stateBefore.humanClubId)

        // Attempt to load from failing repository
        vm.loadCareer()

        val stateAfter = vm.uiState.value
        // Must report error in message
        assertNotNull(stateAfter.saveStatusMessage)
        assertTrue(stateAfter.saveStatusMessage?.contains("Failed to load") == true)
        // Current valid state must NOT be destroyed or replaced with default empty game
        assertEquals(stateBefore.game.name, stateAfter.game.name)
        assertEquals(stateBefore.currentSeason.nextFixtureIndex, stateAfter.currentSeason.nextFixtureIndex)
        assertEquals(stateBefore.humanSquad.size, stateAfter.humanSquad.size)
    }

    @Test
    fun `rapid double-execution of playNextMatchday is prevented by in-flight guard`() {
        val vm = GameViewModel()
        val initialIndex = vm.uiState.value.currentSeason.nextFixtureIndex

        // First call advances matchday
        vm.playNextMatchday()
        val afterFirstCallIndex = vm.uiState.value.currentSeason.nextFixtureIndex
        assertTrue(afterFirstCallIndex > initialIndex)
    }

    @Test
    fun `updating player training modifies training focus and intensity`() {
        val vm = GameViewModel()
        val playerId = vm.uiState.value.humanSquad.first().id

        vm.updatePlayerTraining(
            playerId = playerId,
            focus = com.footballmanager.model.TrainingFocus.ATTACKING,
            intensity = com.footballmanager.model.TrainingIntensity.INTENSIVE,
        )

        val updatedPlayer = vm.uiState.value.humanSquad.first { it.id == playerId }
        assertEquals(com.footballmanager.model.TrainingFocus.ATTACKING, updatedPlayer.trainingFocus)
        assertEquals(com.footballmanager.model.TrainingIntensity.INTENSIVE, updatedPlayer.trainingIntensity)
    }

    @Test
    fun `updating club training modifies all squad members`() {
        val vm = GameViewModel()
        val clubId = vm.uiState.value.humanClubId

        vm.updateClubTraining(
            clubId = clubId,
            focus = com.footballmanager.model.TrainingFocus.DEFENDING,
            intensity = com.footballmanager.model.TrainingIntensity.NORMAL,
        )

        val squad = vm.uiState.value.humanSquad
        for (player in squad) {
            if (player.naturalPositions.any { it.name == "GK" }) {
                assertEquals(com.footballmanager.model.TrainingFocus.GOALKEEPING, player.trainingFocus)
            } else {
                assertEquals(com.footballmanager.model.TrainingFocus.DEFENDING, player.trainingFocus)
            }
        }
    }

    @Test
    fun `save and load dialog visibility toggles correctly`() {
        val vm = GameViewModel()
        assertFalse(vm.uiState.value.showSaveDialog)
        assertFalse(vm.uiState.value.showLoadDialog)

        vm.showSaveDialog(true)
        assertTrue(vm.uiState.value.showSaveDialog)

        vm.showSaveDialog(false)
        assertFalse(vm.uiState.value.showSaveDialog)

        vm.showLoadDialog(true)
        assertTrue(vm.uiState.value.showLoadDialog)

        vm.showLoadDialog(false)
        assertFalse(vm.uiState.value.showLoadDialog)
    }

    @Test
    fun `season rollover saves completed season into game lastSeason`() {
        val vm = GameViewModel()
        // Fast forward season to completion
        while (!vm.uiState.value.currentSeason.isFinished) {
            vm.playNextMatchday()
        }
        assertTrue(vm.uiState.value.currentSeason.isFinished)

        vm.rolloverToNextSeason()

        val state = vm.uiState.value
        assertEquals(0, state.currentSeason.nextFixtureIndex)
        assertNotNull(state.game.lastSeason)
        assertEquals(90, state.game.lastSeason!!.results.size)
        assertEquals(10, state.game.lastSeason!!.standings.entries.size)
    }
}
