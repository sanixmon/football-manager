package com.footballmanager.app.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.footballmanager.app.ui.components.FmTabRow
import com.footballmanager.app.ui.components.SaveLoadDialog
import com.footballmanager.app.ui.components.SaveLoadMode
import com.footballmanager.app.ui.theme.FmAccentBlue
import com.footballmanager.app.ui.theme.FmAccentCyan
import com.footballmanager.app.ui.theme.FmBorder
import com.footballmanager.app.ui.theme.FmRatingHigh
import com.footballmanager.app.ui.theme.FmSurface
import com.footballmanager.app.ui.theme.FmSurfaceAlt
import com.footballmanager.app.ui.theme.FmTextMuted
import com.footballmanager.app.ui.theme.FmTextSecondary
import com.footballmanager.model.TrainingFocus
import com.footballmanager.model.TrainingIntensity
import com.footballmanager.serialization.SaveSlot
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
    onUpdatePlayerTraining: (playerId: Long, focus: TrainingFocus, intensity: TrainingIntensity) -> Unit = { _, _, _ -> },
    onUpdateClubTraining: (clubId: Long, focus: TrainingFocus, intensity: TrainingIntensity) -> Unit = { _, _, _ -> },
    onSelectSaveSlot: (SaveSlot) -> Unit = {},
    onDismissSaveLoadDialog: () -> Unit = {},
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
                    Column(modifier = contentModifier.fillMaxSize()) {
                        FmTabRow(
                            selectedTab = state.activeSquadTab,
                            onTabSelected = onTabSelected,
                        )

                        if (state.activeSquadTab == FmSquadTab.TRAINING) {
                            // Team Training Facilities & Quick Focus Strip
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(FmSurface)
                                    .border(1.dp, FmBorder)
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("FACILITIES:", fontSize = 9.5.sp, color = FmTextMuted, fontWeight = FontWeight.Bold)
                                    Text("Level ${state.humanClub.facilities.trainingLevel}/5", fontSize = 10.sp, color = FmRatingHigh, fontWeight = FontWeight.Bold)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("TEAM FOCUS:", fontSize = 9.sp, color = FmTextMuted)
                                    listOf(TrainingFocus.BALANCED, TrainingFocus.ATTACKING, TrainingFocus.DEFENDING, TrainingFocus.PHYSICAL, TrainingFocus.TECHNICAL).forEach { f ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(FmSurfaceAlt)
                                                .border(1.dp, FmBorder, RoundedCornerShape(3.dp))
                                                .clickable { onUpdateClubTraining(state.humanClubId, f, TrainingIntensity.NORMAL) }
                                                .padding(horizontal = 6.dp, vertical = 2.dp),
                                        ) {
                                            Text(f.name.take(3), fontSize = 8.5.sp, color = FmTextSecondary)
                                        }
                                    }
                                }
                            }
                        }

                        if (isExpanded) {
                            // ── Landscape 70/30 Split Pane: Table 70% | Persistent Inspector 30% ──
                            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
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
                                        onUpdateTraining = { focus, intensity ->
                                            onUpdatePlayerTraining(selectedPlayer.id, focus, intensity)
                                        },
                                        modifier = Modifier.weight(0.32f).fillMaxHeight(),
                                    )
                                }
                            }
                        } else {
                            // ── Portrait Compact: Full-Width Table + Tap opens Detail Sheet ────
                            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
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
                                        onUpdateTraining = { focus, intensity ->
                                            onUpdatePlayerTraining(selectedPlayer.id, focus, intensity)
                                        },
                                    )
                                }
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

        if (state.showSaveDialog) {
            SaveLoadDialog(
                mode = SaveLoadMode.SAVE,
                slots = state.availableSaveSlots,
                onSelectSlot = onSelectSaveSlot,
                onDismiss = onDismissSaveLoadDialog,
            )
        }

        if (state.showLoadDialog) {
            SaveLoadDialog(
                mode = SaveLoadMode.LOAD,
                slots = state.availableSaveSlots,
                onSelectSlot = onSelectSaveSlot,
                onDismiss = onDismissSaveLoadDialog,
            )
        }
    }
}
