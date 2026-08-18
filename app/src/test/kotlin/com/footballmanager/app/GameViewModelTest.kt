package com.footballmanager.app

import com.footballmanager.app.ui.viewmodel.GameViewModel
import com.footballmanager.simulation.Formation
import com.footballmanager.simulation.Mentality
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
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
    fun `updating formation changes tactical slots and auto-aligns starters`() {
        val vm = GameViewModel()
        vm.updateFormation(Formation.FIVE_THREE_TWO)
        assertEquals(Formation.FIVE_THREE_TWO, vm.uiState.value.humanTeam.tactics.formation)
        assertEquals(11, vm.uiState.value.starters.size)
    }

    @Test
    fun `swapWithBench swaps starter and substitute correctly`() {
        val vm = GameViewModel()
        val initialStarterId = vm.uiState.value.starters.first().id
        val initialBenchId = vm.uiState.value.substitutes.first().id

        vm.onStarterSelected(initialStarterId)
        vm.swapWithBench(initialBenchId)

        val updatedStarters = vm.uiState.value.starters.map { it.id }
        val updatedSubs = vm.uiState.value.substitutes.map { it.id }

        assertTrue(initialBenchId in updatedStarters)
        assertTrue(initialStarterId in updatedSubs)
    }

    @Test
    fun `playNextMatchday advances season and depletes starter condition`() {
        val vm = GameViewModel()
        val starterId = vm.uiState.value.starters.first().id
        val initialFitness = vm.uiState.value.starters.first().fitness

        vm.playNextMatchday()

        val updatedStarter = vm.uiState.value.currentSeason.players.getValue(starterId)
        assertEquals(initialFitness - 12, updatedStarter.fitness)
    }
}
