package com.footballmanager.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.footballmanager.app.di.DefaultAppContainer
import com.footballmanager.app.ui.screens.SquadScreen
import com.footballmanager.app.ui.theme.FootballManagerTheme
import com.footballmanager.app.ui.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels {
        (application as? FootballManagerApp)?.container?.gameViewModelFactory
            ?: DefaultAppContainer().gameViewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Immersive sticky fullscreen: hide system navigation and status bars
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())

        setContent {
            FootballManagerTheme {
                val state by viewModel.uiState.collectAsState()

                when (state.currentScreen) {
                    com.footballmanager.app.ui.viewmodel.AppScreen.MAIN_MENU -> {
                        com.footballmanager.app.ui.screens.MainMenuScreen(
                            state = state,
                            onContinueGame = { viewModel.goToAppScreen(com.footballmanager.app.ui.viewmodel.AppScreen.IN_GAME) },
                            onNewGame = { viewModel.goToAppScreen(com.footballmanager.app.ui.viewmodel.AppScreen.NEW_GAME_WIZARD) },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    com.footballmanager.app.ui.viewmodel.AppScreen.NEW_GAME_WIZARD -> {
                        com.footballmanager.app.ui.screens.NewGameWizardScreen(
                            clubs = state.game.clubs.values.toList(),
                            onBack = { viewModel.goToAppScreen(com.footballmanager.app.ui.viewmodel.AppScreen.MAIN_MENU) },
                            onStartCareer = { name, nationality, clubId ->
                                viewModel.startNewCareer(name, nationality, clubId)
                            },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    com.footballmanager.app.ui.viewmodel.AppScreen.IN_GAME -> {
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
                            onSubmitBid = viewModel::submitTransferBid,
                            onOfferContract = viewModel::offerContractTerms,
                            onCompleteDeal = viewModel::completeTransferDeal,
                            onCancelDeal = viewModel::cancelTransferDeal,
                            onRolloverSeason = viewModel::rolloverToNextSeason,
                            onMainMenuClick = { viewModel.goToAppScreen(com.footballmanager.app.ui.viewmodel.AppScreen.MAIN_MENU) },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}
