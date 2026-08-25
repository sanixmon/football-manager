package com.footballmanager.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StandingsScreen(state: GameUiState, modifier: Modifier = Modifier) {
    val entries = state.currentSeason.standings.entries
    val humanPos = entries.indexOfFirst { it.team.clubId == state.humanClubId }

    LazyColumn(modifier = modifier.fillMaxSize().background(FmDarkBg)) {
        stickyHeader {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF181B20))
                    .border(1.dp, FmBorder)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("LEAGUE TABLE", style = MaterialTheme.typography.labelSmall, color = FmAccentBlue, fontSize = 10.sp, letterSpacing = 1.sp)
                    Text(
                        "You: ${if (humanPos >= 0) "#${humanPos + 1}" else "—"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = FmContinueGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = FmBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(4.dp))
                // Column headers
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("#", style = MaterialTheme.typography.labelSmall, color = FmTextMuted, fontSize = 10.sp, modifier = Modifier.width(26.dp))
                    Text("CLUB", style = MaterialTheme.typography.labelSmall, color = FmTextMuted, fontSize = 10.sp, modifier = Modifier.weight(1f))
                    Text("P", style = MaterialTheme.typography.labelSmall, color = FmTextMuted, fontSize = 10.sp, modifier = Modifier.width(26.dp), textAlign = TextAlign.Center)
                    Text("W", style = MaterialTheme.typography.labelSmall, color = FmTextMuted, fontSize = 10.sp, modifier = Modifier.width(26.dp), textAlign = TextAlign.Center)
                    Text("D", style = MaterialTheme.typography.labelSmall, color = FmTextMuted, fontSize = 10.sp, modifier = Modifier.width(26.dp), textAlign = TextAlign.Center)
                    Text("L", style = MaterialTheme.typography.labelSmall, color = FmTextMuted, fontSize = 10.sp, modifier = Modifier.width(26.dp), textAlign = TextAlign.Center)
                    Text("GF", style = MaterialTheme.typography.labelSmall, color = FmTextMuted, fontSize = 10.sp, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
                    Text("GA", style = MaterialTheme.typography.labelSmall, color = FmTextMuted, fontSize = 10.sp, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
                    Text("GD", style = MaterialTheme.typography.labelSmall, color = FmTextMuted, fontSize = 10.sp, modifier = Modifier.width(32.dp), textAlign = TextAlign.Center)
                    Text("PTS", style = MaterialTheme.typography.labelSmall, color = FmTextMuted, fontSize = 10.sp, modifier = Modifier.width(32.dp), textAlign = TextAlign.End)
                }
            }
        }

        itemsIndexed(entries) { index, entry ->
            val isHuman = entry.team.clubId == state.humanClubId
            val position = index + 1

            // Zone coloring: Top 3 = Champions, 4-6 = European, Bottom 3 = Relegation
            val zoneColor = when {
                position <= 3 -> FmRatingHigh.copy(alpha = 0.08f)
                position <= 6 -> FmAccentBlue.copy(alpha = 0.06f)
                position >= entries.size - 2 -> FmRatingLow.copy(alpha = 0.08f)
                else -> if (index % 2 == 0) FmSurface else FmSurfaceAlt
            }

            val rowBg = if (isHuman) FmSurfaceSelected.copy(alpha = 0.8f) else zoneColor

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(rowBg)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Position number (with zone indicator dot)
                Row(
                    modifier = Modifier.width(26.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val posColor = when {
                        position <= 3 -> FmRatingHigh
                        position <= 6 -> FmAccentBlue
                        position >= entries.size - 2 -> FmRatingLow
                        else -> FmTextMuted
                    }
                    Text(
                        "$position",
                        style = MaterialTheme.typography.labelSmall,
                        color = posColor,
                        fontWeight = if (isHuman) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp,
                    )
                }

                // Club name
                val clubName = state.game.club(entry.team.clubId).name
                Text(
                    clubName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isHuman) Color.White else FmTextPrimary,
                    fontWeight = if (isHuman) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f),
                )

                // Stats columns — direct Text calls (no local composable allowed)
                Text("${entry.played}", style = MaterialTheme.typography.labelSmall, color = FmTextSecondary, fontSize = 11.sp, modifier = Modifier.width(26.dp), textAlign = TextAlign.Center)
                Text("${entry.won}", style = MaterialTheme.typography.labelSmall, color = FmRatingHigh, fontSize = 11.sp, modifier = Modifier.width(26.dp), textAlign = TextAlign.Center)
                Text("${entry.drawn}", style = MaterialTheme.typography.labelSmall, color = FmRatingMed, fontSize = 11.sp, modifier = Modifier.width(26.dp), textAlign = TextAlign.Center)
                Text("${entry.lost}", style = MaterialTheme.typography.labelSmall, color = FmRatingLow, fontSize = 11.sp, modifier = Modifier.width(26.dp), textAlign = TextAlign.Center)
                Text("${entry.goalsFor}", style = MaterialTheme.typography.labelSmall, color = FmTextSecondary, fontSize = 11.sp, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
                Text("${entry.goalsAgainst}", style = MaterialTheme.typography.labelSmall, color = FmTextSecondary, fontSize = 11.sp, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
                val gd = entry.goalDifference
                val gdColor = if (gd > 0) FmRatingHigh else if (gd < 0) FmRatingLow else FmTextSecondary
                Text(
                    if (gd >= 0) "+$gd" else "$gd",
                    style = MaterialTheme.typography.labelSmall,
                    color = gdColor,
                    fontSize = 11.sp,
                    modifier = Modifier.width(32.dp),
                    textAlign = TextAlign.Center,
                )

                // Points (prominent)
                Text(
                    "${entry.points}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isHuman) FmContinueGreen else FmTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(32.dp),
                )
            }

            HorizontalDivider(color = FmBorder.copy(alpha = 0.3f), thickness = 0.5.dp)
        }

        // Zone legend at the bottom
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(FmSurface)
                    .border(1.dp, FmBorder, RoundedCornerShape(6.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text("ZONES", style = MaterialTheme.typography.labelSmall, color = FmTextMuted, fontSize = 10.sp, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(4.dp))
                ZoneLegend("Champions Zone", FmRatingHigh)
                ZoneLegend("European Zone", FmAccentBlue)
                ZoneLegend("Relegation Zone", FmRatingLow)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ZoneLegend(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(label, style = MaterialTheme.typography.bodySmall, color = FmTextSecondary, fontSize = 11.sp)
    }
}
