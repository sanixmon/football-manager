package com.footballmanager.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.footballmanager.app.ui.theme.FmTextMuted
import com.footballmanager.app.ui.theme.FmTextPrimary
import com.footballmanager.app.ui.theme.FmTextSecondary
import com.footballmanager.app.ui.viewmodel.GameUiState
import com.footballmanager.simulation.MatchEventType

// Landscape-only match center: scoreboard strip + 50/50 timeline | stats+ratings.
// Shows a pre-match preview when no result exists yet (first matchday).
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
    val nextFixture = season.fixtures.getOrNull(season.nextFixtureIndex)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FmDarkBg),
    ) {
        if (result != null) {
            val homeClub = state.game.club(result.homeClubId)
            val awayClub = state.game.club(result.awayClubId)
            val isUserInvolved = result.homeClubId == humanClubId || result.awayClubId == humanClubId
            val userWon = (result.homeClubId == humanClubId && result.isHomeWin) || (result.awayClubId == humanClubId && result.isAwayWin)
            val accentColor = if (isUserInvolved) (if (userWon) FmRatingHigh else if (result.isDraw) FmRatingMed else FmRatingLow) else FmAccentBlue

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .background(Color(0xFF141820))
                    .border(1.dp, if (isUserInvolved) accentColor else FmBorder)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("FULL TIME", style = MaterialTheme.typography.labelSmall, color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("${homeClub.name} vs ${awayClub.name}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }

                Text("${result.homeScore}  –  ${result.awayScore}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(FmContinueGreen)
                        .clickable { onSimulateMatchday() }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    Text("CONTINUE >", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .weight(0.50f)
                        .fillMaxHeight()
                        .background(FmSurface)
                        .border(1.dp, FmBorder),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF11151C))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    ) {
                        Text("MATCH TIMELINE", style = MaterialTheme.typography.labelSmall, color = FmAccentBlue, fontSize = 11.sp, letterSpacing = 0.5.sp)
                    }
                    HorizontalDivider(color = FmBorder, thickness = 1.dp)

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        items(result.events.filter { it.type != MatchEventType.POSSESSION }) { event ->
                            EventTimelineRow(event)
                            HorizontalDivider(color = FmBorder.copy(alpha = 0.3f), thickness = 0.5.dp)
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(0.50f)
                        .fillMaxHeight()
                        .background(FmSurface)
                        .border(1.dp, FmBorder),
                ) {
                    Column(modifier = Modifier.weight(0.42f).fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF11151C))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        ) {
                            Text("MATCH STATISTICS", style = MaterialTheme.typography.labelSmall, color = FmAccentBlue, fontSize = 11.sp, letterSpacing = 0.5.sp)
                        }
                        HorizontalDivider(color = FmBorder, thickness = 1.dp)

                        val stats = result.stats
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.SpaceAround,
                        ) {
                            DenseStatRow("${(stats.homePossession * 100).toInt()}%", "Possession", "${(stats.awayPossession * 100).toInt()}%")
                            DenseStatRow("${stats.home.shots}", "Total Shots", "${stats.away.shots}")
                            DenseStatRow("${stats.home.shotsOnTarget}", "Shots on Target", "${stats.away.shotsOnTarget}")
                            DenseStatRow("${stats.home.corners}", "Corners", "${stats.away.corners}")
                            DenseStatRow("${stats.home.fouls}", "Fouls", "${stats.away.fouls}")
                            DenseStatRow("${stats.home.yellowCards}", "Yellow Cards", "${stats.away.yellowCards}")
                        }
                    }

                    HorizontalDivider(color = FmBorder, thickness = 1.dp)

                    Column(modifier = Modifier.weight(0.58f).fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF11151C))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        ) {
                            Text("PLAYER RATINGS", style = MaterialTheme.typography.labelSmall, color = FmAccentBlue, fontSize = 11.sp, letterSpacing = 0.5.sp)
                        }
                        HorizontalDivider(color = FmBorder, thickness = 1.dp)

                        val participatingPlayers = result.playerRatings.entries
                            .mapNotNull { (pId, rating) ->
                                state.currentSeason.players[pId]?.let { p -> Triple(p, rating, result.playerStats[pId]) }
                            }
                            .sortedByDescending { it.second }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        ) {
                            items(participatingPlayers) { (player, rating, pStats) ->
                                val isUserPlayer = player.id in state.humanSquad.map { it.id }
                                DenseRatingRow(player, rating, pStats, isUserPlayer)
                                HorizontalDivider(color = FmBorder.copy(alpha = 0.3f), thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        } else {
            // Pre-match preview: no result yet (start of career or after rollover).
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(FmSurface)
                        .border(1.dp, FmBorder, RoundedCornerShape(6.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (season.isFinished) {
                        Text("SEASON COMPLETE", color = FmRatingHigh, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Press NEW SEASON in the top bar to start the next campaign.", color = FmTextSecondary, fontSize = 12.sp, textAlign = TextAlign.Center)
                    } else if (nextFixture != null) {
                        val opponentId = if (nextFixture.home.clubId == humanClubId) nextFixture.away.clubId else nextFixture.home.clubId
                        val opponent = state.game.club(opponentId)
                        val isHome = nextFixture.home.clubId == humanClubId
                        Text("ROUND $nextMatchday • ${if (isHome) "HOME" else "AWAY"} • ${nextFixture.date}", color = FmAccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(
                            if (isHome) "${state.humanClub.name} vs ${opponent.name}" else "${opponent.name} vs ${state.humanClub.name}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                        )
                        state.lineupValidationError?.let {
                            Text(it, color = FmRatingLow, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(FmContinueGreen)
                                .clickable { onSimulateMatchday() }
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                        ) {
                            Text("SIMULATE MATCHDAY", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    } else {
                        Text("No fixtures scheduled.", color = FmTextMuted, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun EventTimelineRow(event: com.footballmanager.simulation.MatchEvent) {
    val (icon, iconColor) = when (event.type) {
        MatchEventType.GOAL -> Icons.Outlined.SportsSoccer to FmRatingHigh
        MatchEventType.SHOT, MatchEventType.SHOT_ON_TARGET -> Icons.Outlined.SportsSoccer to FmAccentBlue
        MatchEventType.SAVE, MatchEventType.SHOT_SAVED -> Icons.Outlined.Shield to FmAccentBlue
        MatchEventType.SHOT_MISSED -> Icons.Outlined.Close to FmRatingLow
        MatchEventType.YELLOW_CARD -> Icons.Outlined.PlayArrow to FmRatingMed
        MatchEventType.RED_CARD -> Icons.Outlined.Close to FmRatingLow
        MatchEventType.INJURY -> Icons.Outlined.Close to FmRatingLow
        MatchEventType.SUBSTITUTION -> Icons.Outlined.ChevronRight to FmAccentCyan
        MatchEventType.CORNER -> Icons.Outlined.PlayArrow to FmAccentBlue
        MatchEventType.FOUL -> Icons.Outlined.Close to FmTextMuted
        else -> Icons.Outlined.PlayArrow to FmTextMuted
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("${event.minute}'", style = MaterialTheme.typography.labelSmall, color = FmAccentBlue, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(30.dp))
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
        Text(
            text = if (event.description.isNotBlank()) event.description else "${event.side}: ${event.type.name.replace('_', ' ')}",
            style = MaterialTheme.typography.bodySmall,
            color = FmTextPrimary,
            fontSize = 12.sp,
            maxLines = 1,
        )
    }
}

@Composable
private fun DenseStatRow(homeValue: String, label: String, awayValue: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(homeValue, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.width(44.dp))
        Text(label, fontSize = 11.sp, color = FmTextMuted, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
        Text(awayValue, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.width(44.dp))
    }
}

@Composable
private fun DenseRatingRow(
    player: com.footballmanager.model.Player,
    rating: Double,
    pStats: com.footballmanager.simulation.PlayerMatchStats?,
    isUserPlayer: Boolean,
) {
    val ratingColor = when {
        rating >= 7.5 -> FmRatingHigh
        rating >= 6.5 -> FmRatingMed
        else -> FmRatingLow
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(if (isUserPlayer) com.footballmanager.app.ui.theme.FmSurfaceSelected else Color.Transparent)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(player.bestPosition().name, style = MaterialTheme.typography.labelSmall, color = FmAccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(player.name, style = MaterialTheme.typography.bodySmall, color = if (isUserPlayer) Color.White else FmTextPrimary, fontSize = 12.sp, maxLines = 1)
            pStats?.let { st ->
                if (st.goals > 0) Text("G:${st.goals}", fontSize = 11.sp, color = FmRatingHigh, fontWeight = FontWeight.Bold)
                if (st.assists > 0) Text("A:${st.assists}", fontSize = 11.sp, color = FmAccentCyan, fontWeight = FontWeight.Bold)
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(2.dp))
                .background(ratingColor.copy(alpha = 0.2f))
                .padding(horizontal = 6.dp, vertical = 2.dp),
        ) {
            Text("$rating", style = MaterialTheme.typography.labelSmall, color = ratingColor, fontWeight = FontWeight.Black, fontSize = 11.sp)
        }
    }
}

@Composable
private fun MatchStatChip(homeVal: String, label: String, awayVal: String) {
    Spacer(modifier = Modifier.width(4.dp))
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(homeVal, style = MaterialTheme.typography.titleSmall, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("-", style = MaterialTheme.typography.titleSmall, color = FmTextMuted, fontSize = 12.sp)
            Text(awayVal, style = MaterialTheme.typography.titleSmall, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = FmTextMuted, fontSize = 11.sp)
    }
}
