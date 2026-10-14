package com.footballmanager.app.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.footballmanager.app.ui.components.FmNavSection
import com.footballmanager.app.ui.components.FmNoticeBanner
import com.footballmanager.app.ui.components.FmSectionHeader
import com.footballmanager.app.ui.components.FmStatus
import com.footballmanager.app.ui.components.FmStatusBadge
import com.footballmanager.app.ui.theme.FmAccentBlue
import com.footballmanager.app.ui.theme.FmAccentCyan
import com.footballmanager.app.ui.theme.FmBorder
import com.footballmanager.app.ui.theme.FmCardBg
import com.footballmanager.app.ui.theme.FmContinueGreen
import com.footballmanager.app.ui.theme.FmDarkBg
import com.footballmanager.app.ui.theme.FmFormDraw
import com.footballmanager.app.ui.theme.FmFormLoss
import com.footballmanager.app.ui.theme.FmFormWin
import com.footballmanager.app.ui.theme.FmRatingHigh
import com.footballmanager.app.ui.theme.FmRatingLow
import com.footballmanager.app.ui.theme.FmRatingMed
import com.footballmanager.app.ui.theme.FmSurface
import com.footballmanager.app.ui.theme.FmTextMuted
import com.footballmanager.app.ui.theme.FmTextPrimary
import com.footballmanager.app.ui.theme.FmTextSecondary
import com.footballmanager.app.ui.viewmodel.GameUiState
import com.footballmanager.simulation.MatchEventType

// Club command center: answers who/where, what happened, what's next,
// what needs attention. Panels navigate to the relevant section.
@Composable
fun HomeScreen(
    state: GameUiState,
    onNavigateToMatchday: () -> Unit,
    onNavigateToSection: (FmNavSection) -> Unit = {},
    onSaveCareer: () -> Unit = {},
    onLoadCareer: () -> Unit = {},
    onRolloverSeason: () -> Unit = {},
    onDismissSaveStatus: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val club = state.humanClub
    val season = state.currentSeason
    val squad = state.humanSquad
    val nextMatchday = season.nextMatchday
    val nextFixture = season.fixtures.getOrNull(season.nextFixtureIndex)
    val lastResult = state.lastMatchResult

    val avgFitness = if (squad.isNotEmpty()) squad.map { it.fitness }.average().toInt() else 100
    val avgMorale = if (squad.isNotEmpty()) squad.map { it.morale }.average().toInt() else 50

    val injured = squad.filter { it.isInjured }
    val suspended = squad.filter { it.isSuspended }
    val lowFitness = squad.filter { !it.isInjured && it.fitness < 60 }

    val standings = season.standings.entries
    val myEntry = standings.firstOrNull { it.team.clubId == state.humanClubId }
    val myPosition = standings.indexOfFirst { it.team.clubId == state.humanClubId } + 1

    val humanResults = season.results.filter { it.homeClubId == state.humanClubId || it.awayClubId == state.humanClubId }
    val recent5 = humanResults.takeLast(5)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FmDarkBg)
            .verticalScroll(rememberScrollState()),
    ) {
        state.saveStatusMessage?.let { msg ->
            FmNoticeBanner(
                message = msg,
                accent = FmAccentBlue,
                onDismiss = onDismissSaveStatus,
            )
        }

        // Club identity strip.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(FmSurface)
                .border(1.dp, FmBorder)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(FmAccentBlue.copy(alpha = 0.2f))
                        .border(1.dp, FmAccentBlue.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.Shield, contentDescription = null, tint = FmAccentCyan, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text(club.name, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(
                        text = "${season.managerProfile?.name ?: "Head Coach"} • ${season.currentDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = FmTextSecondary,
                        fontSize = 11.sp,
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("FORM", style = MaterialTheme.typography.labelSmall, color = FmTextMuted, fontSize = 11.sp)
                if (recent5.isEmpty()) {
                    Text("No matches yet", style = MaterialTheme.typography.labelSmall, color = FmTextMuted, fontSize = 11.sp)
                } else {
                    recent5.forEach { r ->
                        val isHome = r.homeClubId == state.humanClubId
                        val win = (isHome && r.isHomeWin) || (!isHome && r.isAwayWin)
                        val draw = r.isDraw
                        val (text, bg) = when {
                            win -> "W" to FmFormWin
                            draw -> "D" to FmFormDraw
                            else -> "L" to FmFormLoss
                        }
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(bg),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(text, color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        // Two-column command workspace.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Left: action column — next match, last result, attention.
            Column(
                modifier = Modifier.weight(1.25f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // ── Next match ──
                if (nextFixture != null && !season.isFinished) {
                    val opponentId = if (nextFixture.home.clubId == club.id) nextFixture.away.clubId else nextFixture.home.clubId
                    val isHome = nextFixture.home.clubId == club.id
                    val opponent = state.game.club(opponentId)
                    val isToday = state.isMatchdayToday

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isToday) com.footballmanager.app.ui.theme.FmSurfaceSelected else FmSurface)
                            .border(1.dp, if (isToday) FmContinueGreen else FmBorder, RoundedCornerShape(4.dp))
                            .clickable(onClick = onNavigateToMatchday)
                            .padding(12.dp),
                    ) {
                        FmSectionHeader(
                            title = if (isToday) "MATCHDAY — ROUND $nextMatchday" else "NEXT MATCH — ROUND $nextMatchday",
                            actionLabel = "Match centre",
                            onActionClick = onNavigateToMatchday,
                        )
                        Text(
                            text = if (isHome) "${club.name} vs ${opponent.name}" else "${opponent.name} vs ${club.name}",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp,
                        )
                        Text(
                            text = "${if (isHome) "Home" else "Away"} • ${nextFixture.date}" +
                                (if (!isToday) " • in ${state.daysUntilNextMatch}d" else " • today"),
                            fontSize = 11.sp,
                            color = FmTextSecondary,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onNavigateToMatchday,
                            colors = ButtonDefaults.buttonColors(containerColor = FmContinueGreen),
                            shape = RoundedCornerShape(4.dp),
                        ) {
                            Icon(Icons.Outlined.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isToday) "PLAY MATCH" else "OPEN MATCH CENTRE", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                } else if (season.isFinished) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(com.footballmanager.app.ui.theme.FmSurfaceSelected)
                            .border(1.dp, FmRatingHigh, RoundedCornerShape(4.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FmSectionHeader(title = "SEASON COMPLETE")
                        Text("Final position: #$myPosition", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Button(onClick = onRolloverSeason, colors = ButtonDefaults.buttonColors(containerColor = FmContinueGreen), shape = RoundedCornerShape(4.dp)) {
                            Text("START NEXT SEASON", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                // ── Last result ──
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(FmSurface)
                        .border(1.dp, FmBorder, RoundedCornerShape(4.dp))
                        .padding(12.dp),
                ) {
                    FmSectionHeader(title = "LAST RESULT")
                    if (lastResult != null) {
                        val isHome = lastResult.homeClubId == state.humanClubId
                        val userWon = (isHome && lastResult.isHomeWin) || (!isHome && lastResult.isAwayWin)
                        val homeName = state.game.club(lastResult.homeClubId).name
                        val awayName = state.game.club(lastResult.awayClubId).name
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("$homeName ${lastResult.homeScore} – ${lastResult.awayScore} $awayName", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                val scorers = lastResult.events.filter { it.type == MatchEventType.GOAL }.take(3)
                                if (scorers.isEmpty()) {
                                    Text("No goals", fontSize = 11.sp, color = FmTextMuted)
                                } else {
                                    scorers.forEach { e ->
                                        Text("${e.minute}' ${e.description}", fontSize = 11.sp, color = FmTextSecondary, maxLines = 1)
                                    }
                                }
                            }
                            FmStatusBadge(
                                text = if (userWon) "WIN" else if (lastResult.isDraw) "DRAW" else "LOSS",
                                status = if (userWon) FmStatus.POSITIVE else if (lastResult.isDraw) FmStatus.WARNING else FmStatus.NEGATIVE,
                            )
                        }
                    } else {
                        Text("No matches played yet this season.", fontSize = 12.sp, color = FmTextMuted)
                    }
                }

                // ── Requires attention ──
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(FmSurface)
                        .border(1.dp, FmBorder, RoundedCornerShape(4.dp))
                        .padding(12.dp),
                ) {
                    val issueCount = injured.size + suspended.size + lowFitness.size
                    FmSectionHeader(
                        title = if (issueCount == 0) "SQUAD STATUS" else "REQUIRES ATTENTION ($issueCount)",
                        actionLabel = "Squad",
                        onActionClick = { onNavigateToSection(FmNavSection.SQUAD) },
                    )
                    if (issueCount == 0) {
                        FmStatusBadge(text = "Squad fully available", status = FmStatus.POSITIVE)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            injured.take(3).forEach { p ->
                                AttentionRow(
                                    name = p.name,
                                    detail = "Injured" + (p.injuryStatus?.let { " (${it.daysRemaining}d)" } ?: ""),
                                    status = FmStatus.NEGATIVE,
                                    onClick = { onNavigateToSection(FmNavSection.SQUAD) },
                                )
                            }
                            suspended.take(2).forEach { p ->
                                AttentionRow(
                                    name = p.name,
                                    detail = "Suspended" + (p.suspensionStatus?.let { " (${it.matchesRemaining}m)" } ?: ""),
                                    status = FmStatus.WARNING,
                                    onClick = { onNavigateToSection(FmNavSection.SQUAD) },
                                )
                            }
                            lowFitness.take(2).forEach { p ->
                                AttentionRow(
                                    name = p.name,
                                    detail = "Fitness ${p.fitness}%",
                                    status = FmStatus.WARNING,
                                    onClick = { onNavigateToSection(FmNavSection.SQUAD) },
                                )
                            }
                            if (issueCount > 7) {
                                Text("+${issueCount - 7} more in squad", fontSize = 11.sp, color = FmTextMuted)
                            }
                        }
                    }
                }
            }

            // Right: context column — league snapshot, squad summary, save/load.
            Column(
                modifier = Modifier.weight(0.75f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(FmSurface)
                        .border(1.dp, FmBorder, RoundedCornerShape(4.dp))
                        .padding(12.dp),
                ) {
                    FmSectionHeader(
                        title = "LEAGUE",
                        actionLabel = "Full table",
                        onActionClick = { onNavigateToSection(FmNavSection.LEAGUE) },
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                        Text("#$myPosition", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
                        myEntry?.let {
                            Text("${it.points} pts • GD ${if (it.goalDifference >= 0) "+${it.goalDifference}" else "${it.goalDifference}"}", color = FmContinueGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    val top5 = standings.take(5)
                    top5.forEachIndexed { index, entry ->
                        val isHuman = entry.team.clubId == state.humanClubId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isHuman) com.footballmanager.app.ui.theme.FmSurfaceSelected else Color.Transparent)
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                "${index + 1}. ${state.game.club(entry.team.clubId).shortName}",
                                fontSize = 11.sp,
                                color = if (isHuman) Color.White else FmTextPrimary,
                                fontWeight = if (isHuman) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.weight(1f),
                            )
                            Text("${entry.points}", fontSize = 11.sp, color = FmTextSecondary, textAlign = TextAlign.End)
                        }
                    }
                    if (myPosition > 5 && myEntry != null) {
                        Text("…", fontSize = 11.sp, color = FmTextMuted)
                        Row(modifier = Modifier.fillMaxWidth().background(com.footballmanager.app.ui.theme.FmSurfaceSelected).padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("$myPosition. ${club.shortName}", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("${myEntry.points}", fontSize = 11.sp, color = FmContinueGreen, textAlign = TextAlign.End)
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(FmSurface)
                        .border(1.dp, FmBorder, RoundedCornerShape(4.dp))
                        .clickable { onNavigateToSection(FmNavSection.SQUAD) }
                        .padding(12.dp),
                ) {
                    FmSectionHeader(title = "SQUAD")
                    Text(
                        "${squad.count { it.isAvailable }} available • ${injured.size} injured • ${suspended.size} suspended",
                        fontSize = 11.sp,
                        color = if (injured.isNotEmpty() || suspended.isNotEmpty()) FmRatingMed else FmTextSecondary,
                    )
                    Text("Fitness $avgFitness% • Morale $avgMorale%", fontSize = 11.sp, color = FmTextSecondary)
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(FmSurface)
                        .border(1.dp, FmBorder, RoundedCornerShape(4.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Outlined.AccountBalance, contentDescription = null, tint = FmAccentCyan, modifier = Modifier.size(16.dp))
                        Text("CLUB FINANCES", style = MaterialTheme.typography.labelSmall, color = FmAccentBlue, fontSize = 11.sp)
                    }
                    Text("Balance $${"%,d".format(state.humanFinance.balance)}", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Budget $${"%,d".format(state.humanFinance.transferBudget)}", fontSize = 12.sp, color = FmContinueGreen, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = onSaveCareer,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = FmAccentBlue),
                            shape = RoundedCornerShape(3.dp),
                        ) {
                            Icon(Icons.Outlined.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = onLoadCareer,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(3.dp),
                        ) {
                            Icon(Icons.Outlined.Upload, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Load", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FmTextPrimary)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun AttentionRow(
    name: String,
    detail: String,
    status: FmStatus,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(3.dp))
            .background(FmCardBg)
            .border(1.dp, FmBorder, RoundedCornerShape(3.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 1)
            Text(detail, fontSize = 11.sp, color = FmTextSecondary)
        }
        FmStatusBadge(
            text = when (status) {
                FmStatus.NEGATIVE -> "OUT"
                FmStatus.WARNING -> "RISK"
                else -> "OK"
            },
            status = status,
        )
    }
}
