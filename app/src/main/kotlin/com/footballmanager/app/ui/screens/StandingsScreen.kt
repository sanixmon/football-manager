package com.footballmanager.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.footballmanager.app.ui.theme.StadiumEmerald
import com.footballmanager.app.ui.theme.SurfaceSlate
import com.footballmanager.app.ui.viewmodel.GameUiState

@Composable
fun StandingsScreen(state: GameUiState, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(12.dp)) {
        Text(
            text = "LEAGUE TABLE",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("#", modifier = Modifier.width(24.dp), style = MaterialTheme.typography.labelSmall)
            Text("Club", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall)
            Text("P", modifier = Modifier.width(28.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
            Text("GD", modifier = Modifier.width(32.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
            Text("Pts", modifier = Modifier.width(36.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall)
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            itemsIndexed(state.currentSeason.standings.entries) { index, entry ->
                val club = state.game.club(entry.team.clubId)
                val isHuman = entry.team.clubId == state.humanClubId

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isHuman) StadiumEmerald.copy(alpha = 0.2f) else SurfaceSlate,
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("${index + 1}", modifier = Modifier.width(24.dp), color = if (isHuman) StadiumEmerald else Color.White)
                        Text(club.name, modifier = Modifier.weight(1f), color = if (isHuman) StadiumEmerald else Color.White)
                        Text("${entry.played}", modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
                        Text("${entry.goalDifference}", modifier = Modifier.width(32.dp), textAlign = TextAlign.Center)
                        Text("${entry.points}", modifier = Modifier.width(36.dp), textAlign = TextAlign.End, color = StadiumEmerald)
                    }
                }
            }
        }
    }
}
