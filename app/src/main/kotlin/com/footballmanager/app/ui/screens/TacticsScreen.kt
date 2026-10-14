package com.footballmanager.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.footballmanager.app.ui.components.FmSectionHeader
import com.footballmanager.app.ui.components.PitchCanvas
import com.footballmanager.app.ui.theme.FmAccentBlue
import com.footballmanager.app.ui.theme.FmAccentCyan
import com.footballmanager.app.ui.theme.FmBorder
import com.footballmanager.app.ui.theme.FmDarkBg
import com.footballmanager.app.ui.theme.FmRatingHigh
import com.footballmanager.app.ui.theme.FmRatingLow
import com.footballmanager.app.ui.theme.FmSurface
import com.footballmanager.app.ui.theme.FmSurfaceAlt
import com.footballmanager.app.ui.theme.FmSurfaceSelected
import com.footballmanager.app.ui.theme.FmTextMuted
import com.footballmanager.app.ui.theme.FmTextPrimary
import com.footballmanager.app.ui.theme.FmTextSecondary
import com.footballmanager.app.ui.viewmodel.GameUiState
import com.footballmanager.model.Player
import com.footballmanager.simulation.Formation
import com.footballmanager.simulation.Mentality
import com.footballmanager.simulation.Passing
import com.footballmanager.simulation.Pressing
import com.footballmanager.simulation.Tempo

// Landscape-only 3-column workspace: shape | pitch + bench | tactical axes.
@Composable
fun TacticsScreen(
    state: GameUiState,
    onFormationSelected: (Formation) -> Unit,
    onMentalitySelected: (Mentality) -> Unit,
    onAutoSelect: () -> Unit,
    onStarterClick: (Long) -> Unit,
    onBenchClick: (Long) -> Unit,
    onTempoSelected: (Tempo) -> Unit = {},
    onPressingSelected: (Pressing) -> Unit = {},
    onPassingSelected: (Passing) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val currentFormation = state.humanTeam.tactics.formation
    val currentMentality = state.humanTeam.tactics.mentality
    val currentTempo = state.humanTeam.tactics.tempo
    val currentPressing = state.humanTeam.tactics.pressing
    val currentPassing = state.humanTeam.tactics.passing
    val hasSelection = state.selectedStarterPlayerId != null

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(FmDarkBg),
    ) {
        // Column 1: Formation selector (120dp for 48dp touch targets).
        Column(
            modifier = Modifier
                .width(120.dp)
                .fillMaxHeight()
                .background(FmSurface)
                .border(1.dp, FmBorder)
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FmSectionHeader(title = "SHAPE")
                Formation.entries.forEach { f ->
                    val isSelected = currentFormation == f
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isSelected) FmAccentBlue else FmSurfaceAlt)
                            .border(1.dp, if (isSelected) FmAccentCyan else FmBorder, RoundedCornerShape(3.dp))
                            .clickable { onFormationSelected(f) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = f.label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else FmTextSecondary,
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(3.dp))
                    .background(FmAccentBlue.copy(alpha = 0.15f))
                    .border(1.dp, FmAccentBlue, RoundedCornerShape(3.dp))
                    .clickable { onAutoSelect() }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Outlined.AutoFixHigh, contentDescription = null, tint = FmAccentCyan, modifier = Modifier.size(14.dp))
                    Text("AUTO XI", style = MaterialTheme.typography.labelSmall, color = FmAccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Column 2: pitch + swap hint + bench (priority space).
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(horizontal = 6.dp, vertical = 4.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (hasSelection) FmSurfaceSelected else Color(0xFF11151C))
                    .border(1.dp, if (hasSelection) FmAccentCyan else FmBorder, RoundedCornerShape(3.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(Icons.Outlined.SwapHoriz, contentDescription = null, tint = if (hasSelection) FmAccentCyan else FmTextMuted, modifier = Modifier.size(14.dp))
                Text(
                    if (hasSelection) "Starter selected — tap a bench player to swap" else "Tap a starter, then tap a bench player to swap",
                    fontSize = 11.sp,
                    color = if (hasSelection) FmTextPrimary else FmTextMuted,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))

            PitchCanvas(
                starters = state.starters,
                formation = currentFormation,
                selectedPlayerId = state.selectedStarterPlayerId,
                onPlayerClick = onStarterClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )

            Spacer(modifier = Modifier.height(4.dp))

            BenchTray(
                substitutes = state.substitutes,
                hasStarterSelected = hasSelection,
                onBenchClick = onBenchClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
            )
        }

        // Column 3: tactical axes + validation + team rating (180dp, scrollable for short height).
        LazyColumn(
            modifier = Modifier
                .width(180.dp)
                .fillMaxHeight()
                .background(FmSurface)
                .border(1.dp, FmBorder)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                FmSectionHeader(title = "TACTICS")
            }
            state.lineupValidationError?.let { error ->
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(FmRatingLow.copy(alpha = 0.2f))
                            .border(1.dp, FmRatingLow, RoundedCornerShape(3.dp))
                            .padding(6.dp),
                    ) {
                        Text(error, color = FmRatingLow, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            item {
                TacticalAxisSelector("MENTALITY", Mentality.entries.map { it.name.take(3) }, Mentality.entries.indexOf(currentMentality)) {
                    onMentalitySelected(Mentality.entries[it])
                }
            }
            item {
                TacticalAxisSelector("TEMPO", Tempo.entries.map { it.label.take(4) }, Tempo.entries.indexOf(currentTempo)) {
                    onTempoSelected(Tempo.entries[it])
                }
            }
            item {
                TacticalAxisSelector("PRESSING", Pressing.entries.map { it.label.take(4) }, Pressing.entries.indexOf(currentPressing)) {
                    onPressingSelected(Pressing.entries[it])
                }
            }
            item {
                TacticalAxisSelector("PASSING", Passing.entries.map { it.label.take(4) }, Passing.entries.indexOf(currentPassing)) {
                    onPassingSelected(Passing.entries[it])
                }
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF0F141C))
                        .border(1.dp, FmAccentBlue.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(6.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Outlined.Shield, contentDescription = null, tint = FmAccentCyan, modifier = Modifier.size(12.dp))
                        Text("TEAM RATING", style = MaterialTheme.typography.labelSmall, color = FmAccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(2.dp))
                                .background(FmAccentBlue.copy(alpha = 0.15f))
                                .border(1.dp, FmAccentBlue.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("ATK ${state.humanTeam.effectiveAttack()}", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Black)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(2.dp))
                                .background(FmAccentBlue.copy(alpha = 0.15f))
                                .border(1.dp, FmAccentBlue.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("DEF ${state.humanTeam.effectiveDefense()}", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Black)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Attack/defense are team aggregates, not player attributes.",
                        fontSize = 11.sp,
                        color = FmTextMuted,
                    )
                }
            }
        }
    }
}

@Composable
private fun TacticalAxisSelector(
    label: String,
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = FmTextMuted, fontSize = 11.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            options.forEachIndexed { idx, opt ->
                val isSelected = idx == selectedIndex
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (isSelected) FmAccentBlue else FmSurfaceAlt)
                        .border(1.dp, if (isSelected) FmAccentCyan else FmBorder, RoundedCornerShape(2.dp))
                        .clickable { onOptionSelected(idx) }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                ) {
                    Text(opt, fontSize = 11.sp, color = if (isSelected) Color.White else FmTextSecondary, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
    }
}

@Composable
private fun BenchTray(
    substitutes: List<Player>,
    hasStarterSelected: Boolean,
    onBenchClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(Color(0xFF11151C))
            .border(1.dp, if (hasStarterSelected) FmAccentCyan else FmBorder)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("BENCH:", style = MaterialTheme.typography.labelSmall, color = if (hasStarterSelected) FmAccentCyan else FmTextMuted, fontSize = 11.sp, modifier = Modifier.padding(end = 4.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(substitutes, key = { it.id }) { sub ->
                val rating = sub.bestOverall()
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (hasStarterSelected) FmSurfaceSelected else FmSurfaceAlt)
                        .border(1.dp, if (hasStarterSelected) FmAccentCyan else FmBorder, RoundedCornerShape(3.dp))
                        .clickable { onBenchClick(sub.id) }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(sub.bestPosition().name, fontSize = 11.sp, color = FmAccentCyan, fontWeight = FontWeight.Bold)
                    Text(
                        text = sub.name,
                        fontSize = 11.sp,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    )
                    Text("$rating", fontSize = 11.sp, color = if (rating >= 75) FmRatingHigh else FmTextSecondary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
