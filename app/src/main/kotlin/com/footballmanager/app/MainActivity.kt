package com.footballmanager.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.footballmanager.app.ui.screens.SquadScreen
import com.footballmanager.app.ui.theme.FootballManagerTheme
import com.footballmanager.app.ui.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FootballManagerTheme {
                val state by viewModel.uiState.collectAsState()

                SquadScreen(
                    state = state,
                    onContinueClick = viewModel::playNextMatchday,
                    onNavSection = viewModel::navigateTo,
                    onTabSelected = viewModel::selectSquadTab,
                    onFormationSelected = viewModel::updateFormation,
                    onMentalitySelected = viewModel::updateMentality,
                    onAutoSelect = viewModel::autoSelectBestXI,
                    onStarterClick = viewModel::onStarterSelected,
                    onBenchClick = viewModel::swapWithBench,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
