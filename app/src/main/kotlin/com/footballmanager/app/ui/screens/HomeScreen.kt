package com.footballmanager.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
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
import com.footballmanager.app.ui.components.StatGauge
import com.footballmanager.app.ui.theme.ElectricBlue
import com.footballmanager.app.ui.theme.StadiumEmerald
import com.footballmanager.app.ui.theme.SurfaceSlate
import com.footballmanager.app.ui.viewmodel.GameUiState

@Composable
fun HomeScreen(
    state: GameUiState,
    onNavigateToMatchday: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val club = state.humanClub
    val nextMatchday = state.currentSeason.nextMatchday
    val nextFixture = state.currentSeason.fixtures.getOrNull(state.currentSeason.nextFixtureIndex)

    val avgFitness = if (state.humanSquad.isNotEmpty()) state.humanSquad.map { it.fitness }.average().toInt() else 100
    val avgMorale = if (state.humanSquad.isNotEmpty()) state.humanSquad.map { it.morale }.average().toInt() else 50

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = club.name, style = MaterialTheme.typography.headlineMedium, color = Color.White)
                Text(text = "Manager • Liga Nusantara", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Date: ${state.currentSeason.currentDate}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = StadiumEmerald,
                    )
                    Text(text = "•", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "Round ${nextMatchday ?: 18}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ElectricBlue,
                    )
                }
            }
        }

        // Next Match Hero
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "NEXT MATCH", style = MaterialTheme.typography.labelSmall, color = ElectricBlue)
                Spacer(modifier = Modifier.height(8.dp))
                if (nextFixture != null && !state.currentSeason.isFinished) {
                    val opponentId = if (nextFixture.home.clubId == club.id) nextFixture.away.clubId else nextFixture.home.clubId
                    val isHome = nextFixture.home.clubId == club.id
                    val opponent = state.game.club(opponentId)

                    Text(
                        text = if (isHome) "vs ${opponent.name} (H)" else "@ ${opponent.name} (A)",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onNavigateToMatchday,
                        colors = ButtonDefaults.buttonColors(containerColor = StadiumEmerald),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Outlined.PlayArrow, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Play Matchday $nextMatchday", color = Color.Black)
                    }
                } else {
                    Text(text = "Season Finished", style = MaterialTheme.typography.titleMedium, color = StadiumEmerald)
                }
            }
        }

        // Squad Health Summary
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "SQUAD CONDITION", style = MaterialTheme.typography.labelSmall, color = ElectricBlue)
                Spacer(modifier = Modifier.height(8.dp))
                StatGauge(label = "Average Fitness", value = avgFitness, color = StadiumEmerald)
                StatGauge(label = "Average Morale", value = avgMorale, color = ElectricBlue)
            }
        }
    }
}
