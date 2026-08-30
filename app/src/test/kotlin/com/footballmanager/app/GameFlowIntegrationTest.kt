package com.footballmanager.app

import com.footballmanager.app.di.DefaultAppContainer
import com.footballmanager.seed.SeedData
import com.footballmanager.simulation.Formation
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GameFlowIntegrationTest {

    @Test
    fun `AppContainer initializes GameViewModel and executes full matchday flow`() {
        val game = SeedData.game()
        val container = DefaultAppContainer(defaultGameProvider = { game })

        val vm = container.gameViewModelFactory.create(com.footballmanager.app.ui.viewmodel.GameViewModel::class.java)
        val state = vm.uiState.value

        assertNotNull(state)
        assertEquals(1L, state.humanClubId)
        assertEquals(11, state.starters.size)

        // Change formation
        vm.updateFormation(Formation.FOUR_THREE_THREE)
        assertEquals(Formation.FOUR_THREE_THREE, vm.uiState.value.humanTeam.tactics.formation)

        // Advance matchday
        vm.playNextMatchday()
        val nextState = vm.uiState.value
        assertTrue(nextState.currentSeason.results.isNotEmpty())
        assertEquals(2, nextState.currentSeason.nextMatchday)
        assertNotNull(nextState.lastMatchResult)
    }

    @Test
    fun `DefaultAppContainer with saveFile persists progress across ViewModel restarts`() {
        val tempSaveFile = File.createTempFile("test_app_save", ".json")
        tempSaveFile.deleteOnExit()
        tempSaveFile.delete()

        // 1st Session: play matchday
        val container1 = DefaultAppContainer(saveFile = tempSaveFile, defaultGameProvider = { SeedData.game() })
        val vm1 = container1.gameViewModelFactory.create(com.footballmanager.app.ui.viewmodel.GameViewModel::class.java)
        vm1.playNextMatchday()
        assertTrue(vm1.uiState.value.currentSeason.results.isNotEmpty())
        assertTrue(tempSaveFile.exists())

        // 2nd Session: reload from disk
        val container2 = DefaultAppContainer(saveFile = tempSaveFile, defaultGameProvider = { SeedData.game() })
        val vm2 = container2.gameViewModelFactory.create(com.footballmanager.app.ui.viewmodel.GameViewModel::class.java)
        assertEquals(vm1.uiState.value.currentSeason.results.size, vm2.uiState.value.currentSeason.results.size)
        assertEquals(vm1.uiState.value.lastMatchResult, vm2.uiState.value.lastMatchResult)
    }
}
