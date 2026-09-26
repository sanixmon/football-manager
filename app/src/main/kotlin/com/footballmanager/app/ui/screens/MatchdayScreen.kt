package com.footballmanager.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
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
import com.footballmanager.app.ui.theme.FmSurfaceAlt
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

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(FmDarkBg),
    ) {
        val availableWidth = maxWidth
        val isExpanded = availableWidth >= 600.dp

        if (isExpanded && result != null) {
            // ─── EXPANDED MODE (>= 600dp): Split Screen Match Center ──────────
            Column(modifier = Modifier.fillMaxSize()) {
                val homeClub = state.game.club(result.homeClubId)
                val awayClub = state.game.club(result.awayClubId)
                val isUserInvolved = result.homeClubId == humanClubId || result.awayClubId == humanClubId
                val userWon = (result.homeClubId == humanClubId && result.isHomeWin) || (result.awayClubId == humanClubId && result.isAwayWin)
                val accentColor = if (isUserInvolved) (if (userWon) FmRatingHigh else if (result.isDraw) FmRatingMed else FmRatingLow) else FmAccentBlue

                // 1. Compact Scoreboard Strip (38dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .background(Color(0xFF141820))
                        .border(1.dp, if (isUserInvolved) accentColor else FmBorder)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("FULL TIME", style = MaterialTheme.typography.labelSmall, color = accentColor, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                        Text("${homeClub.name} vs ${awayClub.name}", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }

                    Text("${result.homeScore}  –  ${result.awayScore}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)

                    // Action CTA button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(FmContinueGreen)
                            .clickable { onSimulateMatchday() }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                    ) {
                        Text("CONTINUE >", color = Color.Black, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // 2. 50/50 Split Body: Left Timeline | Right Stats + Ratings
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    // Left Pane (50%): Chronological Incidents Timeline
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
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                        ) {
                            Text("MATCH TIMELINE", style = MaterialTheme.typography.labelSmall, color = FmAccentBlue, fontSize = 9.sp, letterSpacing = 0.5.sp)
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

                    // Right Pane (50%): Stacked Stats & Ratings
                    Column(
                        modifier = Modifier
                            .weight(0.50f)
                            .fillMaxHeight()
                            .background(FmSurface)
                            .border(1.dp, FmBorder),
                    ) {
                        // Top Half: Stats
                        Column(modifier = Modifier.weight(0.42f).fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF11151C))
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                            ) {
                                Text("MATCH STATISTICS", style = MaterialTheme.typography.labelSmall, color = FmAccentBlue, fontSize = 9.sp, letterSpacing = 0.5.sp)
                            }
                            HorizontalDivider(color = FmBorder, thickness = 1.dp)

                            val stats = result.stats
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
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

                        // Bottom Half: Player Ratings
                        Column(modifier = Modifier.weight(0.58f).fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF11151C))
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                            ) {
                                Text("PLAYER RATINGS", style = MaterialTheme.typography.labelSmall, color = FmAccentBlue, fontSize = 9.sp, letterSpacing = 0.5.sp)
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
            }
        } else {
            // ─── COMPACT MODE (< 600dp): Vertical Stacking ───────────────────
            LazyColumn(modifier = Modifier.fillMaxSize().background(FmDarkBg)) {
                stickyHeader {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF141820))
                            .border(1.dp, FmBorder),
                    ) {
                        if (!season.isFinished) {
                            val nextFixture = season.fixtures.getOrNull(season.nextFixtureIndex)
                            if (nextFixture != null) {
                                val opponentId = if (nextFixture.home.clubId == humanClubId) nextFixture.away.clubId else nextFixture.home.clubId
                                val opponent = state.game.club(opponentId)
                                val isHome = nextFixture.home.clubId == humanClubId

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSimulateMatchday() }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Column {
                                        Text("ROUND $nextMatchday", style = MaterialTheme.typography.labelSmall, color = FmAccentBlue, fontSize = 9.sp)
                                        Text(
                                            if (isHome) "${state.humanClub.name} vs ${opponent.name}" else "${opponent.name} vs ${state.humanClub.name}",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(FmContinueGreen)
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                    ) {
                                        Text("SIMULATE", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                        HorizontalDivider(color = FmBorder, thickness = 1.dp)
                    }
                }

                if (result != null) {
                    item {
                        val homeClub = state.game.club(result.homeClubId)
                        val awayClub = state.game.club(result.awayClubId)
                        val stats = result.stats

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(FmSurface)
                                .border(1.dp, FmBorder, RoundedCornerShape(4.dp))
                                .padding(10.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(homeClub.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                Text("${result.homeScore}  –  ${result.awayScore}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
                                Text(awayClub.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = FmBorder, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                                MatchStatChip("${stats.home.shots}", "Shots", "${stats.away.shots}")
                                MatchStatChip("${stats.home.shotsOnTarget}", "Target", "${stats.away.shotsOnTarget}")
                                MatchStatChip("${(stats.homePossession * 100).toInt()}%", "Poss", "${(stats.awayPossession * 100).toInt()}%")
                                MatchStatChip("${stats.home.corners}", "Corners", "${stats.away.corners}")
                            }
                        }
                    }

                    // Incidents Header
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF11151C))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            Text("MATCH TIMELINE", style = MaterialTheme.typography.labelSmall, color = FmAccentBlue, fontSize = 9.sp)
                        }
                    }

                    items(result.events.filter { it.type != MatchEventType.POSSESSION }) { event ->
                        EventTimelineRow(event)
                        HorizontalDivider(color = FmBorder.copy(alpha = 0.3f), thickness = 0.5.dp)
                    }

                    // Ratings Header
                    if (result.playerRatings.isNotEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF11151C))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                            ) {
                                Text("PLAYER RATINGS", style = MaterialTheme.typography.labelSmall, color = FmAccentBlue, fontSize = 9.sp)
                            }
                        }

                        val participatingPlayers = result.playerRatings.entries
                            .mapNotNull { (pId, rating) ->
                                state.currentSeason.players[pId]?.let { p -> Triple(p, rating, result.playerStats[pId]) }
                            }
                            .sortedByDescending { it.second }

                        items(participatingPlayers) { (player, rating, pStats) ->
                            val isUserPlayer = player.id in state.humanSquad.map { it.id }
                            DenseRatingRow(player, rating, pStats, isUserPlayer)
                            HorizontalDivider(color = FmBorder.copy(alpha = 0.3f), thickness = 0.5.dp)
                        }
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
            .height(30.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("${event.minute}'", style = MaterialTheme.typography.labelSmall, color = FmAccentBlue, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.width(26.dp))
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(14.dp))
        Text(
            text = if (event.description.isNotBlank()) event.description else "${event.side}: ${event.type.name.replace('_', ' ')}",
            style = MaterialTheme.typography.bodySmall,
            color = FmTextPrimary,
            fontSize = 11.sp,
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
        Text(homeValue, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp))
        Text(label, fontSize = 10.sp, color = FmTextMuted, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
        Text(awayValue, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.width(36.dp))
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
            .height(32.dp)
            .background(if (isUserPlayer) FmSurfaceSelected else Color.Transparent)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(player.bestPosition().name, style = MaterialTheme.typography.labelSmall, color = FmAccentCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text(player.name, style = MaterialTheme.typography.bodySmall, color = if (isUserPlayer) Color.White else FmTextPrimary, fontSize = 11.sp, maxLines = 1)
            pStats?.let { st ->
                if (st.goals > 0) Text("G:${st.goals}", fontSize = 9.5.sp, color = FmRatingHigh, fontWeight = FontWeight.Bold)
                if (st.assists > 0) Text("A:${st.assists}", fontSize = 9.5.sp, color = FmAccentCyan, fontWeight = FontWeight.Bold)
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(2.dp))
                .background(ratingColor.copy(alpha = 0.2f))
                .padding(horizontal = 5.dp, vertical = 1.dp),
        ) {
            Text("$rating", style = MaterialTheme.typography.labelSmall, color = ratingColor, fontWeight = FontWeight.Black, fontSize = 10.sp)
        }
    }
}

@Composable
private fun MatchStatChip(homeVal: String, label: String, awayVal: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(homeVal, style = MaterialTheme.typography.titleSmall, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("-", style = MaterialTheme.typography.titleSmall, color = FmTextMuted, fontSize = 11.sp)
            Text(awayVal, style = MaterialTheme.typography.titleSmall, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = FmTextMuted, fontSize = 9.sp)
    }
}
