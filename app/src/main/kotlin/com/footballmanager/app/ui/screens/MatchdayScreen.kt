package com.footballmanager.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.footballmanager.app.ui.theme.ElectricBlue
import com.footballmanager.app.ui.theme.StadiumEmerald
import com.footballmanager.app.ui.theme.StatusCoral
import com.footballmanager.app.ui.theme.SurfaceSlate
import com.footballmanager.app.ui.viewmodel.GameUiState
import com.footballmanager.simulation.MatchEventType

@Composable
fun MatchdayScreen(
    state: GameUiState,
    onSimulateMatchday: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val result = state.lastMatchResult
    val nextMatchday = state.currentSeason.nextMatchday

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = "MATCHDAY SIMULATION", style = MaterialTheme.typography.headlineMedium)

        if (!state.currentSeason.isFinished) {
            Button(
                onClick = onSimulateMatchday,
                colors = ButtonDefaults.buttonColors(containerColor = StadiumEmerald),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Outlined.PlayArrow, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Simulate Round $nextMatchday", color = Color.Black)
            }
        }

        if (result != null) {
            val homeClub = state.game.club(result.homeClubId)
            val awayClub = state.game.club(result.awayClubId)

            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(text = "FULL TIME", style = MaterialTheme.typography.labelSmall, color = ElectricBlue)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(homeClub.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Text(
                            "${result.homeScore} - ${result.awayScore}",
                            style = MaterialTheme.typography.headlineLarge,
                            color = StadiumEmerald,
                        )
                        Text(awayClub.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    }
                }
            }

            Text(text = "Match Incidents", style = MaterialTheme.typography.titleMedium)
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(result.events) { event ->
                    val (icon, iconColor) = when (event.type) {
                        MatchEventType.GOAL -> Icons.Outlined.SportsSoccer to StadiumEmerald
                        MatchEventType.SHOT_SAVED -> Icons.Outlined.Shield to ElectricBlue
                        MatchEventType.SHOT_MISSED -> Icons.Outlined.Close to StatusCoral
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("${event.minute}'", style = MaterialTheme.typography.labelSmall, color = ElectricBlue)
                        Icon(icon, contentDescription = null, tint = iconColor)
                        Text("${event.side}: ${event.type.name.replace('_', ' ')}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
