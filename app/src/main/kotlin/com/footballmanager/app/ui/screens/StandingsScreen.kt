package com.footballmanager.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    var showPreviousSeason by remember { mutableStateOf(false) }
    val lastSeason = state.game.lastSeason
    val entries = if (showPreviousSeason && lastSeason != null) lastSeason.standings.entries else state.currentSeason.standings.entries
    val humanPos = entries.indexOfFirst { it.team.clubId == state.humanClubId }

    val userResults = state.currentSeason.results
        .filter { it.homeClubId == state.humanClubId || it.awayClubId == state.humanClubId }
        .takeLast(5)
    val upcoming = state.currentSeason.fixtures
        .filter {
            (it.home.clubId == state.humanClubId || it.away.clubId == state.humanClubId) &&
                it.date >= state.currentSeason.currentDate
        }
        .take(3)

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
                if (lastSeason != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (!showPreviousSeason) FmAccentBlue else FmSurfaceAlt)
                                .border(1.dp, if (!showPreviousSeason) FmAccentCyan else FmBorder, RoundedCornerShape(3.dp))
                                .clickable { showPreviousSeason = false }
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        ) {
                            Text("CURRENT SEASON", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = if (!showPreviousSeason) Color.White else FmTextSecondary)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (showPreviousSeason) FmAccentBlue else FmSurfaceAlt)
                                .border(1.dp, if (showPreviousSeason) FmAccentCyan else FmBorder, RoundedCornerShape(3.dp))
                                .clickable { showPreviousSeason = true }
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        ) {
                            val champName = state.game.club(lastSeason.champion.team.clubId).name
                            Text("PREVIOUS SEASON (Champ: $champName)", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = if (showPreviousSeason) Color.White else FmTextSecondary)
                        }
                    }
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

        // Your-club context for the current season only.
        if (!showPreviousSeason) {
            item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(FmSurface)
                    .border(1.dp, FmBorder, RoundedCornerShape(6.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("YOUR SEASON", style = MaterialTheme.typography.labelSmall, color = FmAccentBlue, fontSize = 10.sp, letterSpacing = 1.sp)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (userResults.isEmpty()) {
                            Text("No matches yet", fontSize = 11.sp, color = FmTextMuted)
                        } else {
                            userResults.forEach { r ->
                                val isHome = r.homeClubId == state.humanClubId
                                val win = (isHome && r.isHomeWin) || (!isHome && r.isAwayWin)
                                val (text, bg) = when {
                                    win -> "W" to FmRatingHigh
                                    r.isDraw -> "D" to FmRatingMed
                                    else -> "L" to FmRatingLow
                                }
                                Box(
                                    modifier = Modifier.size(18.dp).clip(CircleShape).background(bg),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(text, color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
                if (upcoming.isEmpty()) {
                    Text("No further fixtures scheduled.", fontSize = 11.sp, color = FmTextMuted)
                } else {
                    upcoming.forEach { fx ->
                        val isHome = fx.home.clubId == state.humanClubId
                        val oppId = if (isHome) fx.away.clubId else fx.home.clubId
                        Text(
                            "${if (isHome) "H" else "A"} vs ${state.game.club(oppId).shortName} • Round ${fx.round} • ${fx.date}",
                            fontSize = 11.sp,
                            color = FmTextSecondary,
                        )
                    }
                }
            }
        }

        }

        itemsIndexed(entries) { index, entry ->
            val isHuman = entry.team.clubId == state.humanClubId
            val position = index + 1

            // Zone treatment is deliberately neutral: leader tint + your-club
            // highlight only. The engine defines no continental/relegation rules.
            val rowBg = if (isHuman) FmSurfaceSelected.copy(alpha = 0.8f)
            else if (position == 1) FmRatingHigh.copy(alpha = 0.10f)
            else if (index % 2 == 0) FmSurface else FmSurfaceAlt

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
                        position == 1 -> FmRatingHigh
                        position <= 4 -> FmAccentBlue
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

        // Table key at the bottom (no invented competition rules).
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
                Text("TABLE KEY", style = MaterialTheme.typography.labelSmall, color = FmTextMuted, fontSize = 10.sp, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(4.dp))
                ZoneLegend("League leader", FmRatingHigh)
                ZoneLegend("Top 4", FmAccentBlue)
                ZoneLegend("Your club (highlighted row)", FmContinueGreen)
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
