package com.footballmanager.app.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.footballmanager.app.ui.components.FmEmptyState
import com.footballmanager.app.ui.components.FmNavSection
import com.footballmanager.app.ui.components.FmNavigationRail
import com.footballmanager.app.ui.components.FmSquadTab
import com.footballmanager.app.ui.components.FmSquadTable
import com.footballmanager.app.ui.components.FmTopAppBar
import com.footballmanager.app.ui.components.PlayerDetailContent
import com.footballmanager.app.ui.theme.FmContinueGreen
import com.footballmanager.app.ui.theme.FmDarkBg
import com.footballmanager.app.ui.viewmodel.GameUiState
import com.footballmanager.model.League
import com.footballmanager.simulation.Formation
import com.footballmanager.simulation.Mentality
import com.footballmanager.simulation.Passing
import com.footballmanager.simulation.Pressing
import com.footballmanager.simulation.Tempo

// Landscape-only shell: left rail + slim top bar + content. No portrait drawer/bottom bar.
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
    onDismissSaveStatus: () -> Unit = {},
    onRolloverSeason: () -> Unit = {},
    onUpdatePlayerTraining: (playerId: Long, focus: TrainingFocus, intensity: TrainingIntensity) -> Unit = { _, _, _ -> },
    onUpdateClubTraining: (clubId: Long, focus: TrainingFocus, intensity: TrainingIntensity) -> Unit = { _, _, _ -> },
    onSelectSaveSlot: (SaveSlot) -> Unit = {},
    onDismissSaveLoadDialog: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val squad = state.humanSquad
    val activeNavSection = state.activeNavSection

    var selectedPlayerId by remember(squad) {
        mutableStateOf<Long?>(squad.firstOrNull()?.id)
    }
    val selectedPlayer = selectedPlayerId?.let { id -> squad.firstOrNull { it.id == id } }

    val myPosition = state.currentSeason.standings.entries.indexOfFirst { it.team.clubId == state.humanClubId } + 1
    val myPoints = state.currentSeason.standings.entries.firstOrNull { it.team.clubId == state.humanClubId }?.points ?: 0
    val leagueName = state.game.competitions.values.filterIsInstance<League>().firstOrNull()?.name.orEmpty()

    val isSeasonFinished = state.currentSeason.isFinished
    val isMatchToday = state.isMatchdayToday
    val daysLeft = state.daysUntilNextMatch
    val buttonText = when {
        isSeasonFinished -> "NEW SEASON"
        isMatchToday -> "PLAY MATCH"
        else -> if (daysLeft > 0) "CONTINUE (${daysLeft}d)" else "CONTINUE"
    }
    val buttonColor = when {
        isSeasonFinished -> Color(0xFFF59E0B)
        isMatchToday -> FmContinueGreen
        else -> Color(0xFF2563EB)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FmDarkBg),
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            FmNavigationRail(
                currentSection = activeNavSection,
                onSectionSelected = onNavSection,
            )

            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                FmTopAppBar(
                    clubName = state.humanClub.name,
                    currentDateText = "${state.currentSeason.currentDate}",
                    breadcrumb = if (leagueName.isNotBlank()) "$leagueName • ${activeNavSection.title}" else activeNavSection.title,
                    leaguePosition = myPosition,
                    leaguePoints = myPoints,
                    balanceText = "$${"%,d".format(state.humanFinance.balance)}",
                    continueButtonText = buttonText,
                    continueButtonColor = buttonColor,
                    onMenuClick = null,
                    onContinueClick = onContinueClick,
                )

                when (activeNavSection) {
                    FmNavSection.DASHBOARD -> HomeScreen(
                        state = state,
                        onNavigateToMatchday = { onNavSection(FmNavSection.MATCH) },
                        onNavigateToSection = onNavSection,
                        onSaveCareer = onSaveCareer,
                        onLoadCareer = onLoadCareer,
                        onRolloverSeason = onRolloverSeason,
                        onDismissSaveStatus = onDismissSaveStatus,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                    )

                    FmNavSection.SQUAD -> {
                        Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            // Only OVERVIEW and TRAINING map to real features; the other
                            // squad tabs are intentionally not offered (see audit doc).
                            FmTabRow(
                                tabs = listOf(FmSquadTab.OVERVIEW, FmSquadTab.TRAINING),
                                selectedTab = state.activeSquadTab,
                                onTabSelected = onTabSelected,
                            )

                            if (state.activeSquadTab == FmSquadTab.TRAINING) {
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
                                        Text("FACILITIES:", fontSize = 11.sp, color = FmTextMuted, fontWeight = FontWeight.Bold)
                                        Text("Level ${state.humanClub.facilities.trainingLevel}/5", fontSize = 11.sp, color = FmRatingHigh, fontWeight = FontWeight.Bold)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("TEAM FOCUS:", fontSize = 11.sp, color = FmTextMuted)
                                        listOf(TrainingFocus.BALANCED, TrainingFocus.ATTACKING, TrainingFocus.DEFENDING, TrainingFocus.PHYSICAL, TrainingFocus.TECHNICAL).forEach { f ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(FmSurfaceAlt)
                                                    .border(1.dp, FmBorder, RoundedCornerShape(3.dp))
                                                    .clickable { onUpdateClubTraining(state.humanClubId, f, TrainingIntensity.NORMAL) }
                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                            ) {
                                                Text(f.name.take(3), fontSize = 11.sp, color = FmTextSecondary)
                                            }
                                        }
                                    }
                                }
                            }

                            // Landscape 68/32 split: table | persistent inspector.
                            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                FmSquadTable(
                                    players = squad,
                                    selectedPlayerId = selectedPlayer?.id,
                                    onPlayerClick = { clicked ->
                                        selectedPlayerId = clicked.id
                                    },
                                    modifier = Modifier.weight(0.68f).fillMaxHeight(),
                                    starterIds = state.humanLineup.starters.toSet(),
                                )

                                if (selectedPlayer != null) {
                                    PlayerDetailContent(
                                        player = selectedPlayer,
                                        onClose = { selectedPlayerId = null },
                                        onUpdateTraining = { focus, intensity ->
                                            onUpdatePlayerTraining(selectedPlayer.id, focus, intensity)
                                        },
                                        modifier = Modifier.weight(0.32f).fillMaxHeight(),
                                    )
                                } else {
                                    FmEmptyState(
                                        title = "No player selected",
                                        detail = "Select a player from the table to inspect attributes, contract and training.",
                                        icon = Icons.Outlined.Person,
                                        modifier = Modifier.weight(0.32f).fillMaxHeight(),
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
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                    )

                    FmNavSection.MATCH -> MatchdayScreen(
                        state = state,
                        onSimulateMatchday = onContinueClick,
                        onOpenTactics = { onNavSection(FmNavSection.TACTICS) },
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                    )

                    FmNavSection.LEAGUE -> StandingsScreen(
                        state = state,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                    )

                    FmNavSection.TRANSFERS -> TransfersScreen(
                        state = state,
                        onDirectTransfer = onDirectTransfer,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
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
