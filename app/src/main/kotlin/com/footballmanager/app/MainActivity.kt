package com.footballmanager.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.footballmanager.app.ui.navigation.NavigationTab
import com.footballmanager.app.ui.screens.HomeScreen
import com.footballmanager.app.ui.screens.MatchdayScreen
import com.footballmanager.app.ui.screens.StandingsScreen
import com.footballmanager.app.ui.screens.TacticsScreen
import com.footballmanager.app.ui.theme.FootballManagerTheme
import com.footballmanager.app.ui.theme.StadiumEmerald
import com.footballmanager.app.ui.theme.SurfaceSlate
import com.footballmanager.app.ui.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FootballManagerTheme {
                val state by viewModel.uiState.collectAsState()
                var currentTab by remember { mutableStateOf(NavigationTab.HOME) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(containerColor = SurfaceSlate) {
                            NavigationTab.entries.forEach { tab ->
                                NavigationBarItem(
                                    selected = currentTab == tab,
                                    onClick = { currentTab = tab },
                                    icon = { Icon(tab.icon, contentDescription = tab.label) },
                                    label = { Text(tab.label) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        selectedTextColor = StadiumEmerald,
                                        indicatorColor = StadiumEmerald,
                                    ),
                                )
                            }
                        }
                    },
                ) { padding ->
                    when (currentTab) {
                        NavigationTab.HOME -> HomeScreen(
                            state = state,
                            onNavigateToMatchday = { currentTab = NavigationTab.MATCHDAY },
                            modifier = Modifier.padding(padding),
                        )
                        NavigationTab.TACTICS -> TacticsScreen(
                            state = state,
                            onFormationSelected = viewModel::updateFormation,
                            onMentalitySelected = viewModel::updateMentality,
                            onAutoSelect = viewModel::autoSelectBestXI,
                            onStarterClick = viewModel::onStarterSelected,
                            onBenchClick = viewModel::swapWithBench,
                            modifier = Modifier.padding(padding),
                        )
                        NavigationTab.STANDINGS -> StandingsScreen(
                            state = state,
                            modifier = Modifier.padding(padding),
                        )
                        NavigationTab.MATCHDAY -> MatchdayScreen(
                            state = state,
                            onSimulateMatchday = viewModel::playNextMatchday,
                            modifier = Modifier.padding(padding),
                        )
                    }
                }
            }
        }
    }
}
