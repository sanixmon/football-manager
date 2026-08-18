package com.footballmanager.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.footballmanager.app.ui.theme.FmSurfaceHover
import com.footballmanager.app.ui.theme.FmSurfaceSelected
import com.footballmanager.app.ui.theme.FmTextMuted
import com.footballmanager.app.ui.theme.FmTextPrimary
import com.footballmanager.app.ui.theme.FmTextSecondary
import com.footballmanager.app.ui.viewmodel.GameUiState
import com.footballmanager.simulation.MatchEventType

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MatchdayScreen(
    state: GameUiState,
    onSimulateMatchday: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val result = state.lastMatchResult
    val nextMatchday = state.currentSeason.nextMatchday
    val season = state.currentSeason
    val humanClubId = state.humanClubId

    // Group remaining fixtures by round for the schedule view
    val playedFixtures = season.fixtures.take(season.nextFixtureIndex)
    val upcomingFixtures = season.fixtures.drop(season.nextFixtureIndex).take(10)

    LazyColumn(modifier = modifier.fillMaxSize().background(FmDarkBg)) {
        // ─── Continue / Simulate Hero ─────────────────────────────────────
        stickyHeader {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF181B20))
                    .border(1.dp, FmBorder),
            ) {
                if (!season.isFinished) {
                    // Next match hero bar
                    val nextFixture = season.fixtures.getOrNull(season.nextFixtureIndex)
                    if (nextFixture != null) {
                        val opponentId = if (nextFixture.home.clubId == humanClubId) nextFixture.away.clubId else nextFixture.home.clubId
                        val opponent = state.game.club(opponentId)
                        val isHome = nextFixture.home.clubId == humanClubId

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(FmSurface)
                                .clickable { onSimulateMatchday() }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text("NEXT MATCH — ROUND $nextMatchday", style = MaterialTheme.typography.labelSmall, color = FmAccentBlue, fontSize = 10.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    if (isHome) "${state.humanClub.name}  vs  ${opponent.name}" else "${opponent.name}  vs  ${state.humanClub.name}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                )
                                Text(
                                    if (isHome) "Home • ${nextFixture.date}" else "Away • ${nextFixture.date}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = FmTextSecondary,
                                )
                            }
                            // Simulate CTA
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(FmContinueGreen)
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(Icons.Outlined.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Text("SIMULATE", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxWidth().background(FmRatingHigh.copy(alpha = 0.2f)).padding(12.dp)) {
                        Text("SEASON COMPLETE", style = MaterialTheme.typography.titleMedium, color = FmRatingHigh, fontWeight = FontWeight.Bold)
                    }
                }
                HorizontalDivider(color = FmBorder, thickness = 1.dp)
            }
        }

        // ─── Last Match Result ─────────────────────────────────────────────
        if (result != null) {
            item {
                val homeClub = state.game.club(result.homeClubId)
                val awayClub = state.game.club(result.awayClubId)
                val isUserInvolved = result.homeClubId == humanClubId || result.awayClubId == humanClubId
                val userWon = (result.homeClubId == humanClubId && result.isHomeWin) || (result.awayClubId == humanClubId && result.isAwayWin)
                val accentColor = if (isUserInvolved) (if (userWon) FmRatingHigh else if (result.isDraw) FmRatingMed else FmRatingLow) else FmAccentBlue

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(FmSurface)
                        .border(1.dp, if (isUserInvolved) accentColor else FmBorder, RoundedCornerShape(6.dp)),
                ) {
                    // Result header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(accentColor.copy(alpha = if (isUserInvolved) 0.15f else 0.05f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("FULL TIME", style = MaterialTheme.typography.labelSmall, color = accentColor, fontSize = 10.sp, letterSpacing = 1.sp)
                        if (isUserInvolved) {
                            Text(
                                if (userWon) "WIN" else if (result.isDraw) "DRAW" else "LOSS",
                                style = MaterialTheme.typography.labelSmall,
                                color = accentColor,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp,
                            )
                        }
                    }

                    // Score display
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                homeClub.name,
                                style = MaterialTheme.typography.titleSmall,
                                color = if (result.homeClubId == humanClubId) Color.White else FmTextPrimary,
                                fontWeight = if (result.homeClubId == humanClubId) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp,
                            )
                            Text("HOME", style = MaterialTheme.typography.bodySmall, color = FmTextMuted, fontSize = 9.sp)
                        }
                        Text(
                            "${result.homeScore}  –  ${result.awayScore}",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp,
                        )
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            Text(
                                awayClub.name,
                                style = MaterialTheme.typography.titleSmall,
                                color = if (result.awayClubId == humanClubId) Color.White else FmTextPrimary,
                                fontWeight = if (result.awayClubId == humanClubId) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp,
                                textAlign = TextAlign.End,
                            )
                            Text("AWAY", style = MaterialTheme.typography.bodySmall, color = FmTextMuted, fontSize = 9.sp, textAlign = TextAlign.End)
                        }
                    }

                    // Match stats row
                    val stats = result.stats
                    HorizontalDivider(color = FmBorder, thickness = 1.dp)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                    ) {
                        MatchStatChip("${stats.home.shots}", "Shots", "${stats.away.shots}")
                        MatchStatChip("${stats.home.shotsOnTarget}", "On Target", "${stats.away.shotsOnTarget}")
                        MatchStatChip(
                            "${(stats.homePossession * 100).toInt()}%",
                            "Possession",
                            "${(stats.awayPossession * 100).toInt()}%",
                        )
                    }
                }
            }

            // Match incidents header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .background(Color(0xFF181B20))
                        .border(width = 1.dp, color = FmBorder)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("MATCH INCIDENTS", style = MaterialTheme.typography.labelSmall, color = FmAccentBlue, fontSize = 10.sp, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("${result.events.count { it.type == MatchEventType.GOAL }} Goals", style = MaterialTheme.typography.labelSmall, color = FmTextMuted, fontSize = 10.sp)
                }
            }

            items(result.events) { event ->
                val (icon, iconColor) = when (event.type) {
                    MatchEventType.GOAL -> Icons.Outlined.SportsSoccer to FmRatingHigh
                    MatchEventType.SHOT_SAVED -> Icons.Outlined.Shield to FmAccentBlue
                    MatchEventType.SHOT_MISSED -> Icons.Outlined.Close to FmRatingLow
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .background(FmSurface)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("${event.minute}'", style = MaterialTheme.typography.labelSmall, color = FmAccentBlue, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(32.dp))
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
                    Text(
                        "${event.side}: ${event.type.name.replace('_', ' ')}",
                        style = MaterialTheme.typography.bodySmall,
                        color = FmTextPrimary,
                        fontSize = 12.sp,
                    )
                }
                HorizontalDivider(color = FmBorder.copy(alpha = 0.3f), thickness = 0.5.dp)
            }
        }

        // ─── Upcoming Fixtures Schedule ───────────────────────────────────
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .background(Color(0xFF181B20))
                    .border(1.dp, FmBorder)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = FmAccentBlue, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("UPCOMING FIXTURES", style = MaterialTheme.typography.labelSmall, color = FmAccentBlue, fontSize = 10.sp, letterSpacing = 1.sp)
            }
        }

        items(upcomingFixtures) { fixture ->
            val isUserFixture = fixture.home.clubId == humanClubId || fixture.away.clubId == humanClubId
            val homeClub = state.game.club(fixture.home.clubId)
            val awayClub = state.game.club(fixture.away.clubId)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .background(if (isUserFixture) FmSurfaceSelected else FmSurface)
                    .border(
                        width = if (isUserFixture) 1.dp else 0.dp,
                        color = if (isUserFixture) FmAccentBlue else Color.Transparent,
                    )
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "R${fixture.round}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isUserFixture) FmAccentCyan else FmTextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(28.dp),
                )
                Text(
                    homeClub.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (fixture.home.clubId == humanClubId) Color.White else FmTextSecondary,
                    fontWeight = if (fixture.home.clubId == humanClubId) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End,
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(FmSurfaceAlt)
                        .border(1.dp, FmBorder, RoundedCornerShape(3.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text("vs", style = MaterialTheme.typography.labelSmall, color = FmTextMuted, fontSize = 10.sp)
                }
                Text(
                    awayClub.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (fixture.away.clubId == humanClubId) Color.White else FmTextSecondary,
                    fontWeight = if (fixture.away.clubId == humanClubId) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f),
                )
                Text("${fixture.date}", style = MaterialTheme.typography.bodySmall, color = FmTextMuted, fontSize = 9.sp)
            }
            HorizontalDivider(color = FmBorder.copy(alpha = 0.3f), thickness = 0.5.dp)
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun MatchStatChip(home: String, label: String, away: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(home, style = MaterialTheme.typography.labelSmall, color = FmTextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(away, style = MaterialTheme.typography.labelSmall, color = FmTextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Text(label, style = MaterialTheme.typography.bodySmall, color = FmTextMuted, fontSize = 9.sp)
    }
}
