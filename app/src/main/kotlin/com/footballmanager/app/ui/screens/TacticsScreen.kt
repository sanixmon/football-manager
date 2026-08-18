package com.footballmanager.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.footballmanager.app.ui.components.PitchCanvas
import com.footballmanager.app.ui.theme.ElectricBlue
import com.footballmanager.app.ui.theme.StadiumEmerald
import com.footballmanager.app.ui.theme.SurfaceSlate
import com.footballmanager.app.ui.viewmodel.GameUiState
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Formation & Mentality Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Formation.entries.forEach { f ->
                    FilterChip(
                        selected = state.humanTeam.tactics.formation == f,
                        onClick = { onFormationSelected(f) },
                        label = { Text(f.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StadiumEmerald,
                            selectedLabelColor = Color.Black,
                        ),
                    )
                }
            }
            Button(
                onClick = onAutoSelect,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                shape = RoundedCornerShape(8.dp),
            ) {
                Icon(Icons.Outlined.AutoFixHigh, contentDescription = "Auto Pick")
            }
        }

        // Pitch Visualizer
        PitchCanvas(
            starters = state.starters,
            formation = state.humanTeam.tactics.formation,
            selectedPlayerId = state.selectedStarterPlayerId,
            onPlayerClick = onStarterClick,
        )

        // Bench Substitutes
        Text(
            text = if (state.selectedStarterPlayerId != null) "Tap a substitute to swap" else "Substitutes",
            style = MaterialTheme.typography.titleMedium,
            color = if (state.selectedStarterPlayerId != null) StadiumEmerald else Color.White,
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(state.substitutes) { player ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onBenchClick(player.id) },
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(text = player.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "${player.bestPosition()} • Fit: ${player.fitness}% • Mor: ${player.morale}",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        Text(
                            text = "${player.bestOverall()}",
                            style = MaterialTheme.typography.titleLarge,
                            color = StadiumEmerald,
                        )
                    }
                }
            }
        }
    }
}
