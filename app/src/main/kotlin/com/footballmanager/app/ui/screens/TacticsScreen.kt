package com.footballmanager.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.footballmanager.app.ui.theme.FmSurfaceHover
import com.footballmanager.app.ui.theme.FmSurfaceSelected
import com.footballmanager.app.ui.theme.FmTextMuted
import com.footballmanager.app.ui.theme.FmTextPrimary
import com.footballmanager.app.ui.theme.FmTextSecondary
import com.footballmanager.app.ui.viewmodel.GameUiState
import com.footballmanager.model.Player
import com.footballmanager.model.PositionGroup
import com.footballmanager.simulation.Formation
import com.footballmanager.simulation.Mentality

@Composable
fun TacticsScreen(
    state: GameUiState,
    onFormationSelected: (Formation) -> Unit,
    onMentalitySelected: (Mentality) -> Unit,
    onAutoSelect: () -> Unit,
    onStarterClick: (Long) -> Unit,
    onBenchClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentFormation = state.humanTeam.tactics.formation
    val currentMentality = state.humanTeam.tactics.mentality
    val hasStarSelected = state.selectedStarterPlayerId != null

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FmDarkBg),
    ) {
        // ─── Formation & Mentality Controls ───────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(FmSurface)
                .border(1.dp, FmBorder)
                .padding(horizontal = 10.dp, vertical = 8.dp),
        ) {
            // Formation chips row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("FORMATION", style = MaterialTheme.typography.labelSmall, color = FmAccentBlue, fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Formation.entries.forEach { f ->
                            val isSelected = currentFormation == f
                            Box_chip(
                                label = f.label,
                                isSelected = isSelected,
                                onClick = { onFormationSelected(f) },
                            )
                        }
                    }
                }
                // Auto-select button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(FmAccentBlue.copy(alpha = 0.15f))
                        .border(1.dp, FmAccentBlue, RoundedCornerShape(4.dp))
                        .clickable { onAutoSelect() }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(Icons.Outlined.AutoFixHigh, contentDescription = "Auto", tint = FmAccentBlue, modifier = Modifier.size(16.dp))
                    Text("AUTO", style = MaterialTheme.typography.labelSmall, color = FmAccentBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mentality chips row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("MENTALITY", style = MaterialTheme.typography.labelSmall, color = FmAccentBlue, fontSize = 10.sp)
                Mentality.entries.forEach { m ->
                    val isSelected = currentMentality == m
                    Box_chip(
                        label = m.name.lowercase().replaceFirstChar { it.uppercase() },
                        isSelected = isSelected,
                        onClick = { onMentalitySelected(m) },
                        selectedColor = FmRatingMed,
                    )
                }
            }
        }

        // ─── Pitch Canvas ─────────────────────────────────────────────────
        PitchCanvas(
            starters = state.starters,
            formation = currentFormation,
            selectedPlayerId = state.selectedStarterPlayerId,
            onPlayerClick = onStarterClick,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.55f),
        )

        // ─── Bench / Substitutes ──────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.45f)
                .background(FmDarkBg),
        ) {
            // Bench header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .background(Color(0xFF181B20))
                    .border(1.dp, FmBorder)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                if (hasStarSelected) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Outlined.SwapHoriz, contentDescription = null, tint = FmContinueGreen, modifier = Modifier.size(14.dp))
                        Text("SELECT SUBSTITUTE TO SWAP", style = MaterialTheme.typography.labelSmall, color = FmContinueGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text("BENCH", style = MaterialTheme.typography.labelSmall, color = FmAccentBlue, fontSize = 10.sp, letterSpacing = 1.sp)
                }
                Text("${state.substitutes.size} players", style = MaterialTheme.typography.labelSmall, color = FmTextMuted, fontSize = 10.sp)
            }

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(state.substitutes) { player ->
                    BenchPlayerRow(
                        player = player,
                        isSwapMode = hasStarSelected,
                        onClick = { onBenchClick(player.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun Box_chip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    selectedColor: Color = FmContinueGreen,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(if (isSelected) selectedColor.copy(alpha = 0.2f) else Color(0xFF1E2229))
            .border(1.dp, if (isSelected) selectedColor else FmBorder, RoundedCornerShape(3.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) selectedColor else FmTextSecondary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 11.sp,
        )
    }
}

@Composable
private fun BenchPlayerRow(
    player: Player,
    isSwapMode: Boolean,
    onClick: () -> Unit,
) {
    val posBg = when (player.bestPosition().group) {
        PositionGroup.GOALKEEPER -> FmAccentBlue.copy(alpha = 0.15f) to FmAccentBlue
        PositionGroup.DEFENDER -> FmAccentBlue.copy(alpha = 0.12f) to FmAccentBlue
        PositionGroup.MIDFIELDER -> FmContinueGreen.copy(alpha = 0.12f) to FmContinueGreen
        PositionGroup.ATTACKER -> FmRatingLow.copy(alpha = 0.12f) to FmRatingLow
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(if (isSwapMode) FmSurfaceHover else FmSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Pos badge
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(3.dp))
                .background(posBg.first)
                .border(1.dp, posBg.second.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
                .padding(horizontal = 5.dp, vertical = 2.dp),
        ) {
            Text(player.bestPosition().name, style = MaterialTheme.typography.labelSmall, color = posBg.second, fontSize = 10.sp, fontWeight = FontWeight.Black)
        }

        // Player name
        Text(
            player.name,
            style = MaterialTheme.typography.bodyMedium,
            color = FmTextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        // Rating
        val ovr = player.bestOverall()
        val ovrColor = if (ovr >= 70) FmRatingHigh else if (ovr >= 60) FmRatingMed else FmRatingLow
        Text(
            "$ovr",
            style = MaterialTheme.typography.labelSmall,
            color = ovrColor,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.width(28.dp),
        )

        // Fitness mini bar
        Column(modifier = Modifier.width(48.dp), horizontalAlignment = Alignment.End) {
            Text("${player.fitness}%", style = MaterialTheme.typography.bodySmall, color = FmTextMuted, fontSize = 9.sp)
            LinearProgressIndicator(
                progress = { player.fitness / 100f },
                modifier = Modifier.width(44.dp).height(3.dp).clip(RoundedCornerShape(1.5.dp)),
                color = if (player.fitness >= 85) FmRatingHigh else if (player.fitness >= 60) FmRatingMed else FmRatingLow,
                trackColor = FmBorder,
            )
        }

        if (isSwapMode) {
            Icon(Icons.Outlined.SwapHoriz, contentDescription = "Swap", tint = FmContinueGreen, modifier = Modifier.size(16.dp))
        }
    }
    HorizontalDivider(color = FmBorder.copy(alpha = 0.4f), thickness = 0.5.dp)
}
