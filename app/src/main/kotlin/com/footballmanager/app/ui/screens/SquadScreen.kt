package com.footballmanager.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.footballmanager.app.ui.components.FmBottomNavigationBar
import com.footballmanager.app.ui.components.FmNavSection
import com.footballmanager.app.ui.components.FmNavigationDrawerContent
import com.footballmanager.app.ui.components.FmNavigationRail
import com.footballmanager.app.ui.components.FmSquadTab
import com.footballmanager.app.ui.components.FmSquadTable
import com.footballmanager.app.ui.components.FmTopAppBar
import com.footballmanager.app.ui.components.PlayerDetailBottomSheet
import com.footballmanager.app.ui.components.PlayerDetailContent
import com.footballmanager.app.ui.theme.FmContinueGreen
import com.footballmanager.app.ui.theme.FmDarkBg
import com.footballmanager.app.ui.viewmodel.GameUiState
import com.footballmanager.model.Player
import com.footballmanager.simulation.Formation
import com.footballmanager.simulation.Mentality
import com.footballmanager.simulation.Passing
import com.footballmanager.simulation.Pressing
import com.footballmanager.simulation.Tempo
import kotlinx.coroutines.launch

@Composable
fun SquadScreen(
    state: GameUiState,
    onContinueClick: () -> Unit,
    onNavSection: (FmNavSection) -> Unit,
    onTabSelected: (FmSquadTab) -> Unit,
    onFormationSelected: (Formation) -> Unit,
    onMentalitySelected: (Mentality) -> Unit,
    onAutoSelect: () -> Unit,
    onStarterClick: (Long) -> Unit,
    onBenchClick: (Long) -> Unit,
    onTempoSelected: (Tempo) -> Unit = {},
    onPressingSelected: (Pressing) -> Unit = {},
    onPassingSelected: (Passing) -> Unit = {},
    onDirectTransfer: (playerId: Long, fromClubId: Long, toClubId: Long, fee: Long) -> Unit = { _, _, _, _ -> },
    onSaveCareer: () -> Unit = {},
    onLoadCareer: () -> Unit = {},
    onSubmitBid: (playerId: Long, fee: Long) -> Unit = { _, _ -> },
    onOfferContract: (bidId: Long, weeklyWage: Long, years: Int, squadStatus: com.footballmanager.model.SquadStatus) -> Unit = { _, _, _, _ -> },
    onCompleteDeal: (bidId: Long) -> Unit = {},
    onCancelDeal: (bidId: Long) -> Unit = {},
    onRolloverSeason: () -> Unit = {},
    onMainMenuClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val squad = state.humanSquad
    val activeNavSection = state.activeNavSection

    // Canonical selection: track selectedPlayerId, derive player
    var selectedPlayerId by remember(squad) {
        mutableStateOf(squad.firstOrNull()?.id)
    }
    val selectedPlayer = squad.firstOrNull { it.id == selectedPlayerId } ?: squad.firstOrNull()
    var showMobileDetailSheet by remember { mutableStateOf(false) }

    val myPosition = state.currentSeason.standings.entries.indexOfFirst { it.team.clubId == state.humanClubId } + 1
    val myPoints = state.currentSeason.standings.entries.firstOrNull { it.team.clubId == state.humanClubId }?.points ?: 0

    val isSeasonFinished = state.currentSeason.isFinished
    val isMatchToday = state.isMatchdayToday
    val buttonText = when {
        isSeasonFinished -> "NEW SEASON"
        isMatchToday -> "PLAY MATCH"
        else -> "CONTINUE"
    }
    val buttonColor = when {
        isSeasonFinished -> Color(0xFFF59E0B)
        isMatchToday -> FmContinueGreen
        else -> Color(0xFF2563EB)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(FmDarkBg),
    ) {
        val availableWidth = maxWidth
        // Expanded composition activates when available width >= 600dp regardless of physical orientation
        val isExpanded = availableWidth >= 600.dp

        @Composable
        fun ScreenContent(contentModifier: Modifier) {
            when (activeNavSection) {
                FmNavSection.DASHBOARD -> HomeScreen(
                    state = state,
                    onNavigateToMatchday = { onNavSection(FmNavSection.MATCH) },
                    onNavigateToSection = onNavSection,
                    onSaveCareer = onSaveCareer,
                    onLoadCareer = onLoadCareer,
                    onRolloverSeason = onRolloverSeason,
                    modifier = contentModifier,
                )

                FmNavSection.SQUAD -> {
                    if (isExpanded) {
                        // ── Landscape 70/30 Split Pane: Table 70% | Persistent Inspector 30% ──
                        Row(modifier = contentModifier.fillMaxSize()) {
                            FmSquadTable(
                                players = squad,
                                selectedPlayerId = selectedPlayer?.id,
                                onPlayerClick = { clicked ->
                                    selectedPlayerId = clicked.id
                                },
                                modifier = Modifier.weight(0.68f).fillMaxHeight(),
                            )

                            if (selectedPlayer != null) {
                                PlayerDetailContent(
                                    player = selectedPlayer,
                                    onClose = {},
                                    modifier = Modifier.weight(0.32f).fillMaxHeight(),
                                )
                            }
                        }
                    } else {
                        // ── Portrait Compact: Full-Width Table + Tap opens Detail Sheet ────
                        Box(modifier = contentModifier.fillMaxSize()) {
                            FmSquadTable(
                                players = squad,
                                selectedPlayerId = selectedPlayer?.id,
                                onPlayerClick = { clicked ->
                                    selectedPlayerId = clicked.id
                                    showMobileDetailSheet = true
                                },
                                modifier = Modifier.fillMaxSize(),
                            )

                            if (showMobileDetailSheet && selectedPlayer != null) {
                                PlayerDetailBottomSheet(
                                    player = selectedPlayer,
                                    onDismiss = { showMobileDetailSheet = false },
                                )
                            }
                        }
                    }
                }

                FmNavSection.TACTICS -> TacticsScreen(
                    state = state,
                    onFormationSelected = onFormationSelected,
                    onMentalitySelected = onMentalitySelected,
                    onAutoSelect = onAutoSelect,
                    onStarterClick = onStarterClick,
                    onBenchClick = onBenchClick,
                    onTempoSelected = onTempoSelected,
                    onPressingSelected = onPressingSelected,
                    onPassingSelected = onPassingSelected,
                    modifier = contentModifier,
                )

                FmNavSection.MATCH -> MatchdayScreen(
                    state = state,
                    onSimulateMatchday = onContinueClick,
                    modifier = contentModifier,
                )

                FmNavSection.LEAGUE -> StandingsScreen(
                    state = state,
                    modifier = contentModifier,
                )

                FmNavSection.TRANSFERS -> TransfersScreen(
                    state = state,
                    onDirectTransfer = onDirectTransfer,
                    onOfferContract = onOfferContract,
                    onCompleteDeal = onCompleteDeal,
                    onCancelDeal = onCancelDeal,
                    modifier = contentModifier,
                )
            }
        }

        if (isExpanded) {
            // ─── EXPANDED MODE (>= 600dp): Left Rail + Top Context Bar + Content ───
            Row(modifier = Modifier.fillMaxSize()) {
                FmNavigationRail(
                    currentSection = activeNavSection,
                    onSectionSelected = onNavSection,
                )

                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    FmTopAppBar(
                        clubName = state.humanClub.name,
                        currentDateText = "${state.currentSeason.currentDate}",
                        breadcrumb = "Liga Nusantara • ${activeNavSection.title}",
                        leaguePosition = myPosition,
                        leaguePoints = myPoints,
                        balanceText = "$${"%,d".format(state.humanFinance.balance)}",
                        continueButtonText = buttonText,
                        continueButtonColor = buttonColor,
                        onMenuClick = null,
                        onContinueClick = onContinueClick,
                    )

                    ScreenContent(contentModifier = Modifier.weight(1f).fillMaxWidth())
                }
            }
        } else {
            // ─── COMPACT MODE (< 600dp): Top Context Bar + Content + Bottom Bar ────
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    FmNavigationDrawerContent(
                        currentSection = activeNavSection,
                        onSectionSelected = { section ->
                            onNavSection(section)
                            coroutineScope.launch { drawerState.close() }
                        },
                        onMainMenuClick = onMainMenuClick,
                    )
                },
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    FmTopAppBar(
                        clubName = state.humanClub.name,
                        currentDateText = "${state.currentSeason.currentDate}",
                        breadcrumb = activeNavSection.title,
                        leaguePosition = myPosition,
                        leaguePoints = myPoints,
                        balanceText = "$${"%,d".format(state.humanFinance.balance)}",
                        continueButtonText = buttonText,
                        continueButtonColor = buttonColor,
                        onMenuClick = {
                            coroutineScope.launch {
                                if (drawerState.isClosed) drawerState.open() else drawerState.close()
                            }
                        },
                        onContinueClick = onContinueClick,
                    )

                    ScreenContent(contentModifier = Modifier.weight(1f).fillMaxWidth())

                    FmBottomNavigationBar(
                        currentSection = activeNavSection,
                        onSectionSelected = onNavSection,
                    )
                }
            }
        }
    }
}
