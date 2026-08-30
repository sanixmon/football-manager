package com.footballmanager.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.dp
import com.footballmanager.app.ui.components.FmFooterBar
import com.footballmanager.app.ui.components.FmNavSection
import com.footballmanager.app.ui.components.FmNavigationDrawerContent
import com.footballmanager.app.ui.components.FmNavigationRail
import com.footballmanager.app.ui.components.FmSquadTab
import com.footballmanager.app.ui.components.FmSquadTable
import com.footballmanager.app.ui.components.FmTabRow
import com.footballmanager.app.ui.components.FmTopAppBar
import com.footballmanager.app.ui.components.PlayerDetailBottomSheet
import com.footballmanager.app.ui.components.PlayerDetailContent
import com.footballmanager.app.ui.theme.FmDarkBg
import com.footballmanager.app.ui.viewmodel.GameUiState
import com.footballmanager.model.Player
import com.footballmanager.simulation.Formation
import com.footballmanager.simulation.Mentality
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
    onSubmitBid: (playerId: Long, fee: Long) -> Unit = { _, _ -> },
    onOfferContract: (bidId: Long, weeklyWage: Long, years: Int, squadStatus: com.footballmanager.model.SquadStatus) -> Unit = { _, _, _, _ -> },
    onCompleteDeal: (bidId: Long) -> Unit = {},
    onCancelDeal: (bidId: Long) -> Unit = {},
    onRolloverSeason: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var selectedPlayer by remember { mutableStateOf<Player?>(null) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val squad = state.humanSquad
    val totalWage = squad.sumOf { it.contract.weeklyWage }
    val activeNavSection = state.activeNavSection
    val activeTab = state.activeSquadTab

    // Determine current breadcrumb + tab options based on nav section
    val breadcrumbSection = activeNavSection.title
    val showSquadTabs = activeNavSection == FmNavSection.SQUAD
    val showTacticsTabs = activeNavSection == FmNavSection.TACTICS

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(FmDarkBg),
    ) {
        val isTablet = maxWidth >= 650.dp

        @Composable
        fun MainContent() {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top App Bar
                FmTopAppBar(
                    clubName = state.humanClub.name,
                    breadcrumb = when (activeNavSection) {
                        FmNavSection.SQUAD -> "Overview > Squad > ${activeTab.label}"
                        FmNavSection.TACTICS -> "Overview > Tactics"
                        FmNavSection.SCHEDULE -> "Overview > Schedule"
                        FmNavSection.COMPETITIONS -> "Overview > League"
                        FmNavSection.INBOX -> "Inbox"
                        else -> "Overview > ${activeNavSection.title}"
                    },
                    currentDateText = "${state.currentSeason.currentDate}",
                    onMenuClick = if (!isTablet) ({
                        coroutineScope.launch {
                            if (drawerState.isClosed) drawerState.open() else drawerState.close()
                        }
                        Unit
                    }) else null,
                    onContinueClick = onContinueClick,
                )

                // Tab Row — only for Squad section
                if (showSquadTabs) {
                    FmTabRow(
                        selectedTab = activeTab,
                        onTabSelected = onTabSelected,
                    )
                }

                // Content Area
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    // Main Content body
                    val bodyModifier = Modifier.weight(1f)

                    when (activeNavSection) {
                        FmNavSection.SQUAD -> when (activeTab) {
                            FmSquadTab.OVERVIEW -> FmSquadTable(
                                players = squad,
                                selectedPlayerId = selectedPlayer?.id,
                                onPlayerClick = { clicked ->
                                    selectedPlayer = if (!isTablet || selectedPlayer?.id == clicked.id) {
                                        if (selectedPlayer?.id == clicked.id) null else clicked
                                    } else {
                                        clicked
                                    }
                                },
                                modifier = bodyModifier,
                            )
                            FmSquadTab.REPORT -> SquadReportTab(
                                players = squad,
                                selectedPlayerId = selectedPlayer?.id,
                                onPlayerClick = { selectedPlayer = it },
                            )
                            FmSquadTab.DYNAMICS -> SquadDynamicsTab(players = squad)
                            FmSquadTab.STATS -> SquadStatsTab(
                                players = squad,
                                playerStats = state.currentSeason.playerStats,
                                selectedPlayerId = selectedPlayer?.id,
                                onPlayerClick = { selectedPlayer = it },
                            )
                            FmSquadTab.CONTRACTS -> SquadContractsTab(
                                players = squad,
                                selectedPlayerId = selectedPlayer?.id,
                                onPlayerClick = { selectedPlayer = it },
                            )
                        }

                        FmNavSection.TACTICS -> TacticsScreen(
                            state = state,
                            onFormationSelected = onFormationSelected,
                            onMentalitySelected = onMentalitySelected,
                            onAutoSelect = onAutoSelect,
                            onStarterClick = onStarterClick,
                            onBenchClick = onBenchClick,
                            modifier = bodyModifier,
                        )

                        FmNavSection.COMPETITIONS -> StandingsScreen(
                            state = state,
                            modifier = bodyModifier,
                        )

                        FmNavSection.SCHEDULE -> MatchdayScreen(
                            state = state,
                            onSimulateMatchday = onContinueClick,
                            modifier = bodyModifier,
                        )

                        FmNavSection.HOME -> HomeScreen(
                            state = state,
                            onNavigateToMatchday = { onNavSection(FmNavSection.SCHEDULE) },
                            onRolloverSeason = onRolloverSeason,
                            modifier = bodyModifier,
                        )

                        FmNavSection.INBOX -> InboxScreen(
                            state = state,
                            modifier = bodyModifier,
                        )

                        FmNavSection.SCOUTING -> ScoutingScreen(
                            state = state,
                            onSubmitBid = onSubmitBid,
                            modifier = bodyModifier,
                        )

                        FmNavSection.TRANSFERS -> TransfersScreen(
                            state = state,
                            onOfferContract = onOfferContract,
                            onCompleteDeal = onCompleteDeal,
                            onCancelDeal = onCancelDeal,
                            modifier = bodyModifier,
                        )

                        FmNavSection.FINANCES -> FinancesScreen(
                            state = state,
                            modifier = bodyModifier,
                        )

                        else -> StandingsScreen(
                            state = state,
                            modifier = bodyModifier,
                        )
                    }

                    // Slide-In Tablet Right Detail Panel (only for squad overview/report/stats/contracts)
                    if (isTablet && activeNavSection == FmNavSection.SQUAD) {
                        AnimatedVisibility(
                            visible = selectedPlayer != null,
                            enter = slideInHorizontally(initialOffsetX = { it }),
                            exit = slideOutHorizontally(targetOffsetX = { it }),
                        ) {
                            selectedPlayer?.let { player ->
                                PlayerDetailContent(
                                    player = player,
                                    onClose = { selectedPlayer = null },
                                    modifier = Modifier.width(360.dp),
                                )
                            }
                        }
                    }
                }

                // Footer Bar
                FmFooterBar(
                    playerCount = squad.size,
                    totalWage = totalWage,
                )
            }
        }

        if (isTablet) {
            Row(modifier = Modifier.fillMaxSize()) {
                FmNavigationRail(
                    currentSection = activeNavSection,
                    onSectionSelected = onNavSection,
                )
                MainContent()
            }
        } else {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    FmNavigationDrawerContent(
                        currentSection = activeNavSection,
                        onSectionSelected = { section ->
                            onNavSection(section)
                            coroutineScope.launch { drawerState.close() }
                        },
                    )
                },
            ) {
                MainContent()
            }

            // Phone Modal Bottom Sheet for Player Details (Squad section only)
            if (activeNavSection == FmNavSection.SQUAD && selectedPlayer != null) {
                PlayerDetailBottomSheet(
                    player = selectedPlayer,
                    onDismiss = { selectedPlayer = null },
                )
            }
        }
    }
}
