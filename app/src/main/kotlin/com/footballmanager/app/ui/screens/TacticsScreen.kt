package com.footballmanager.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.HorizontalDivider
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
import com.footballmanager.app.ui.components.PitchCanvas
import com.footballmanager.app.ui.theme.FmAccentBlue
import com.footballmanager.app.ui.theme.FmAccentCyan
import com.footballmanager.app.ui.theme.FmBorder
import com.footballmanager.app.ui.theme.FmContinueGreen
import com.footballmanager.app.ui.theme.FmDarkBg
import com.footballmanager.app.ui.theme.FmRatingHigh
import com.footballmanager.app.ui.theme.FmRatingLow
import com.footballmanager.app.ui.theme.FmRatingMed
import com.footballmanager.app.ui.theme.FmSurface
import com.footballmanager.app.ui.theme.FmSurfaceAlt
import com.footballmanager.app.ui.theme.FmSurfaceSelected
import com.footballmanager.app.ui.theme.FmTextMuted
import com.footballmanager.app.ui.theme.FmTextPrimary
import com.footballmanager.app.ui.theme.FmTextSecondary
import com.footballmanager.app.ui.viewmodel.GameUiState
import com.footballmanager.model.Player
import com.footballmanager.model.PositionGroup
import com.footballmanager.simulation.Formation
import com.footballmanager.simulation.Mentality
import com.footballmanager.simulation.Passing
import com.footballmanager.simulation.Pressing
import com.footballmanager.simulation.Tempo

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

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(FmDarkBg),
    ) {
        val availableWidth = maxWidth
        val isExpanded = availableWidth >= 600.dp

        if (isExpanded) {
            // ─── EXPANDED MODE (>= 600dp): 3-Column Football Workspace ────────
            Row(modifier = Modifier.fillMaxSize()) {
                // Column 1: Formation Selector (~105dp)
                Column(
                    modifier = Modifier
                        .width(105.dp)
                        .fillMaxHeight()
                        .background(FmSurface)
                        .border(1.dp, FmBorder)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("SHAPE", style = MaterialTheme.typography.labelSmall, color = FmAccentBlue, fontSize = 9.sp, letterSpacing = 0.5.sp)
                        Formation.entries.forEach { f ->
                            val isSelected = currentFormation == f
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (isSelected) FmAccentBlue else FmSurfaceAlt)
                                    .border(1.dp, if (isSelected) FmAccentCyan else FmBorder, RoundedCornerShape(3.dp))
                                    .clickable { onFormationSelected(f) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = f.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else FmTextSecondary,
                                )
                            }
                        }
                    }

                    // Auto Select CTA
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(3.dp))
                            .background(FmAccentBlue.copy(alpha = 0.15f))
                            .border(1.dp, FmAccentBlue, RoundedCornerShape(3.dp))
                            .clickable { onAutoSelect() }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Outlined.AutoFixHigh, contentDescription = null, tint = FmAccentCyan, modifier = Modifier.size(13.dp))
                            Text("AUTO XI", style = MaterialTheme.typography.labelSmall, color = FmAccentCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Column 2: Center Pitch & Bench Tray (weight 1f, priority space)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                ) {
                    // Pitch Canvas
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

                    // Bench Tray (Compact Horizontal List)
                    BenchTray(
                        substitutes = state.substitutes,
                        hasStarterSelected = state.selectedStarterPlayerId != null,
                        onBenchClick = onBenchClick,
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                    )
                }

                // Column 3: Tactical Axes & Lineup Validation (~165dp)
                Column(
                    modifier = Modifier
                        .width(165.dp)
                        .fillMaxHeight()
                        .background(FmSurface)
                        .border(1.dp, FmBorder)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("TACTICAL SYSTEM", style = MaterialTheme.typography.labelSmall, color = FmAccentBlue, fontSize = 9.sp, letterSpacing = 0.5.sp)

                        // Validation Banner if any
                        state.lineupValidationError?.let { error ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(FmRatingLow.copy(alpha = 0.2f))
                                    .border(1.dp, FmRatingLow, RoundedCornerShape(3.dp))
                                    .padding(4.dp),
                            ) {
                                Text(error, color = FmRatingLow, fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        // Mentality
                        TacticalAxisSelector("MENTALITY", Mentality.entries.map { it.name.take(3) }, Mentality.entries.indexOf(currentMentality)) {
                            onMentalitySelected(Mentality.entries[it])
                        }

                        // Tempo
                        TacticalAxisSelector("TEMPO", Tempo.entries.map { it.label.take(4) }, Tempo.entries.indexOf(currentTempo)) {
                            onTempoSelected(Tempo.entries[it])
                        }

                        // Pressing
                        TacticalAxisSelector("PRESSING", Pressing.entries.map { it.label.take(4) }, Pressing.entries.indexOf(currentPressing)) {
                            onPressingSelected(Pressing.entries[it])
                        }

                        // Passing
                        TacticalAxisSelector("PASSING", Passing.entries.map { it.label.take(4) }, Passing.entries.indexOf(currentPassing)) {
                            onPassingSelected(Passing.entries[it])
                        }
                    }

                    // Team Power Readout
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(3.dp))
                            .background(FmSurfaceAlt)
                            .border(1.dp, FmBorder, RoundedCornerShape(3.dp))
                            .padding(6.dp),
                    ) {
                        Text("TEAM RATINGS", style = MaterialTheme.typography.labelSmall, color = FmTextMuted, fontSize = 8.5.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Attack: ${state.humanTeam.effectiveAttack()}", fontSize = 10.sp, color = FmRatingHigh, fontWeight = FontWeight.Bold)
                            Text("Defense: ${state.humanTeam.effectiveDefense()}", fontSize = 10.sp, color = FmAccentCyan, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // ─── COMPACT MODE (< 600dp): Vertical Stacking ───────────────────
            Column(modifier = Modifier.fillMaxSize()) {
                if (state.lineupValidationError != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(FmRatingLow.copy(alpha = 0.2f))
                            .border(1.dp, FmRatingLow)
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                    ) {
                        Text("⚠️ ${state.lineupValidationError}", color = FmRatingLow, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Top Formations Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FmSurface)
                        .border(1.dp, FmBorder)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Formation.entries.forEach { f ->
                            val isSelected = currentFormation == f
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (isSelected) FmAccentBlue else FmSurfaceAlt)
                                    .border(1.dp, if (isSelected) FmAccentCyan else FmBorder, RoundedCornerShape(3.dp))
                                    .clickable { onFormationSelected(f) }
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                            ) {
                                Text(f.label, fontSize = 10.5.sp, color = if (isSelected) Color.White else FmTextSecondary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(FmAccentBlue.copy(alpha = 0.2f))
                            .clickable { onAutoSelect() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text("AUTO XI", style = MaterialTheme.typography.labelSmall, color = FmAccentCyan, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Center Pitch Canvas
                PitchCanvas(
                    starters = state.starters,
                    formation = currentFormation,
                    selectedPlayerId = state.selectedStarterPlayerId,
                    onPlayerClick = onStarterClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.55f),
                )

                // Bench Tray
                BenchTray(
                    substitutes = state.substitutes,
                    hasStarterSelected = state.selectedStarterPlayerId != null,
                    onBenchClick = onBenchClick,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                )

                // Tactical Axes Below
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.45f)
                        .background(FmSurface)
                        .border(1.dp, FmBorder)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.SpaceAround,
                ) {
                    TacticalAxisSelector("MENTALITY", Mentality.entries.map { it.name }, Mentality.entries.indexOf(currentMentality)) {
                        onMentalitySelected(Mentality.entries[it])
                    }
                    TacticalAxisSelector("TEMPO", Tempo.entries.map { it.label }, Tempo.entries.indexOf(currentTempo)) {
                        onTempoSelected(Tempo.entries[it])
                    }
                    TacticalAxisSelector("PRESSING", Pressing.entries.map { it.label }, Pressing.entries.indexOf(currentPressing)) {
                        onPressingSelected(Pressing.entries[it])
                    }
                    TacticalAxisSelector("PASSING", Passing.entries.map { it.label }, Passing.entries.indexOf(currentPassing)) {
                        onPassingSelected(Passing.entries[it])
                    }
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = FmTextMuted, fontSize = 8.5.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            options.forEachIndexed { idx, opt ->
                val isSelected = idx == selectedIndex
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (isSelected) FmAccentBlue else FmSurfaceAlt)
                        .border(1.dp, if (isSelected) FmAccentCyan else FmBorder, RoundedCornerShape(2.dp))
                        .clickable { onOptionSelected(idx) }
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                ) {
                    Text(opt, fontSize = 8.5.sp, color = if (isSelected) Color.White else FmTextSecondary, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
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
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("BENCH:", style = MaterialTheme.typography.labelSmall, color = if (hasStarterSelected) FmAccentCyan else FmTextMuted, fontSize = 9.sp, modifier = Modifier.padding(end = 4.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            items(substitutes, key = { it.id }) { sub ->
                val rating = sub.bestOverall()
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (hasStarterSelected) FmSurfaceSelected else FmSurfaceAlt)
                        .border(1.dp, if (hasStarterSelected) FmAccentCyan else FmBorder, RoundedCornerShape(3.dp))
                        .clickable { onBenchClick(sub.id) }
                        .padding(horizontal = 5.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(sub.bestPosition().name, fontSize = 8.5.sp, color = FmAccentCyan, fontWeight = FontWeight.Bold)
                    Text(sub.name.take(10), fontSize = 9.5.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text("$rating", fontSize = 8.5.sp, color = if (rating >= 75) FmRatingHigh else FmRatingMed, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
