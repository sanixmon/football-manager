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
import kotlinx.coroutines.launch

@Composable
fun SquadScreen(
    state: GameUiState,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var activeNavSection by remember { mutableStateOf(FmNavSection.SQUAD) }
    var activeTab by remember { mutableStateOf(FmSquadTab.OVERVIEW) }
    var selectedPlayer by remember { mutableStateOf<Player?>(null) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val squad = state.humanSquad
    val totalWage = squad.sumOf { it.contract.weeklyWage }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(FmDarkBg),
    ) {
        val isTablet = maxWidth >= 650.dp

        if (isTablet) {
            // Tablet / Landscape Layout: Permanent Navigation Rail + Side-by-side Table & Detail Panel
            Row(modifier = Modifier.fillMaxSize()) {
                // 1. Permanent Navigation Rail
                FmNavigationRail(
                    currentSection = activeNavSection,
                    onSectionSelected = { activeNavSection = it },
                )

                // 2. Main Work Area
                Column(modifier = Modifier.weight(1f)) {
                    // Top App Bar
                    FmTopAppBar(
                        clubName = state.humanClub.name,
                        breadcrumb = "Overview > ${activeNavSection.title} > ${activeTab.label}",
                        currentDateText = "${state.currentSeason.currentDate}",
                        onContinueClick = onContinueClick,
                    )

                    // Tab Row
                    FmTabRow(
                        selectedTab = activeTab,
                        onTabSelected = { activeTab = it },
                    )

                    // Content Area with Split View when Player is Selected
                    Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        // Data Table
                        FmSquadTable(
                            players = squad,
                            selectedPlayerId = selectedPlayer?.id,
                            onPlayerClick = { clicked ->
                                selectedPlayer = if (selectedPlayer?.id == clicked.id) null else clicked
                            },
                            modifier = Modifier.weight(1f),
                        )

                        // Slide-In Tablet Right Detail Panel
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

                    // Bottom Footer Bar
                    FmFooterBar(
                        playerCount = squad.size,
                        totalWage = totalWage,
                    )
                }
            }
        } else {
            // Phone / Compact Layout: Modal Navigation Drawer + Full-Width Table + Modal Bottom Sheet
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    FmNavigationDrawerContent(
                        currentSection = activeNavSection,
                        onSectionSelected = { section ->
                            activeNavSection = section
                            coroutineScope.launch { drawerState.close() }
                        },
                    )
                },
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Top App Bar with Hamburger Toggle
                    FmTopAppBar(
                        clubName = state.humanClub.name,
                        breadcrumb = "Overview > ${activeNavSection.title}",
                        currentDateText = "${state.currentSeason.currentDate}",
                        onMenuClick = {
                            coroutineScope.launch {
                                if (drawerState.isClosed) drawerState.open() else drawerState.close()
                            }
                        },
                        onContinueClick = onContinueClick,
                    )

                    // Secondary Tab Row
                    FmTabRow(
                        selectedTab = activeTab,
                        onTabSelected = { activeTab = it },
                    )

                    // Main Squad Data Table
                    FmSquadTable(
                        players = squad,
                        selectedPlayerId = selectedPlayer?.id,
                        onPlayerClick = { clicked ->
                            selectedPlayer = clicked
                        },
                        modifier = Modifier.weight(1f),
                    )

                    // Footer Status Bar
                    FmFooterBar(
                        playerCount = squad.size,
                        totalWage = totalWage,
                    )

                    // Phone Modal Bottom Sheet for Player Details
                    if (selectedPlayer != null) {
                        PlayerDetailBottomSheet(
                            player = selectedPlayer,
                            onDismiss = { selectedPlayer = null },
                        )
                    }
                }
            }
        }
    }
}
