package com.footballmanager.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.outlined.CheckCircle
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.footballmanager.app.ui.components.FmNavSection
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

// Landscape-only dashboard: club strip on top, then 2-column workspace
// (left: next fixture + league/squad | right: finances + quick actions).
@Composable
fun HomeScreen(
    state: GameUiState,
    onNavigateToMatchday: () -> Unit,
    onNavigateToSection: (FmNavSection) -> Unit = {},
    onSaveCareer: () -> Unit = {},
    onLoadCareer: () -> Unit = {},
    onRolloverSeason: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val club = state.humanClub
    val season = state.currentSeason
    val squad = state.humanSquad
    val nextMatchday = season.nextMatchday
    val nextFixture = season.fixtures.getOrNull(season.nextFixtureIndex)

    val avgFitness = if (squad.isNotEmpty()) squad.map { it.fitness }.average().toInt() else 100
    val avgMorale = if (squad.isNotEmpty()) squad.map { it.morale }.average().toInt() else 50

    val availableCount = squad.count { it.isAvailable }
    val injuredCount = squad.count { it.isInjured }
    val suspendedCount = squad.count { it.isSuspended }

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(com.footballmanager.app.ui.theme.FmSurfaceSelected)
                    .border(1.dp, FmAccentBlue)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = FmAccentCyan, modifier = Modifier.size(16.dp))
                Text(msg, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // Club overview strip (compact for landscape).
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
                        text = "${season.managerProfile?.name ?: "Head Coach"} • Season 2026/27 • ${season.currentDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = FmTextSecondary,
                        fontSize = 11.sp,
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("FORM", style = MaterialTheme.typography.labelSmall, color = FmTextMuted, fontSize = 11.sp)
                if (recent5.isEmpty()) {
                    Text("- - -", style = MaterialTheme.typography.labelSmall, color = FmTextMuted, fontSize = 11.sp)
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

        // Two-column workspace for short landscape height.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Left column: next fixture + league/squad panels.
            Column(
                modifier = Modifier.weight(1.25f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (nextFixture != null && !season.isFinished) {
                    val opponentId = if (nextFixture.home.clubId == club.id) nextFixture.away.clubId else nextFixture.home.clubId
                    val isHome = nextFixture.home.clubId == club.id
                    val opponent = state.game.club(opponentId)
                    val isToday = state.isMatchdayToday

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isToday) com.footballmanager.app.ui.theme.FmSurfaceSelected else FmSurface)
                            .border(1.dp, if (isToday) FmContinueGreen else FmBorder)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("NEXT FIXTURE", style = MaterialTheme.typography.labelSmall, color = FmAccentCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
                                Text("• Round $nextMatchday", style = MaterialTheme.typography.labelSmall, color = FmTextMuted, fontSize = 11.sp)
                            }
                            Text(
                                if (isHome) "HOME • ${nextFixture.date}" else "AWAY • ${nextFixture.date}",
                                style = MaterialTheme.typography.bodySmall,
                                color = FmTextSecondary,
                                fontSize = 11.sp,
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isHome) "${club.name} vs ${opponent.name}" else "${opponent.name} vs ${club.name}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                )
                                Text(
                                    text = "$availableCount available ($injuredCount injured, $suspendedCount suspended)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (injuredCount > 0 || suspendedCount > 0) FmRatingMed else FmTextSecondary,
                                    fontSize = 11.sp,
                                )
                            }

                            Button(
                                onClick = onNavigateToMatchday,
                                colors = ButtonDefaults.buttonColors(containerColor = FmContinueGreen),
                                shape = RoundedCornerShape(4.dp),
                            ) {
                                Icon(Icons.Outlined.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("MATCH CENTER", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                            }
                        }
                    }
                } else if (season.isFinished) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(com.footballmanager.app.ui.theme.FmSurfaceSelected)
                            .border(1.dp, FmRatingHigh)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("SEASON CONCLUDED — #$myPosition", style = MaterialTheme.typography.titleMedium, color = FmRatingHigh, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Button(onClick = onRolloverSeason, colors = ButtonDefaults.buttonColors(containerColor = FmContinueGreen), shape = RoundedCornerShape(4.dp)) {
                            Text("START NEXT CAMPAIGN >", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(FmSurface)
                            .border(1.dp, FmBorder, RoundedCornerShape(4.dp))
                            .padding(10.dp),
                    ) {
                        Text("LEAGUE TABLE", style = MaterialTheme.typography.labelSmall, color = FmAccentBlue, fontSize = 11.sp, letterSpacing = 0.5.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("#$myPosition", style = MaterialTheme.typography.headlineLarge, color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
                            myEntry?.let {
                                Text("${it.points} pts", style = MaterialTheme.typography.titleMedium, color = FmContinueGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                        myEntry?.let {
                            Text("${it.played}P • ${it.won}W ${it.drawn}D ${it.lost}L • GD ${if (it.goalDifference >= 0) "+${it.goalDifference}" else "${it.goalDifference}"}", style = MaterialTheme.typography.bodySmall, color = FmTextSecondary, fontSize = 11.sp)
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(FmSurface)
                            .border(1.dp, FmBorder, RoundedCornerShape(4.dp))
                            .padding(10.dp),
                    ) {
                        Text("SQUAD", style = MaterialTheme.typography.labelSmall, color = FmAccentBlue, fontSize = 11.sp, letterSpacing = 0.5.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            MiniStat("Available", "$availableCount", FmContinueGreen)
                            MiniStat("Injured", "$injuredCount", if (injuredCount > 0) FmRatingLow else FmTextMuted)
                            MiniStat("Suspended", "$suspendedCount", if (suspendedCount > 0) FmRatingLow else FmTextMuted)
                        }
                        Text("Fitness: $avgFitness% • Morale: $avgMorale%", style = MaterialTheme.typography.bodySmall, color = FmTextSecondary, fontSize = 11.sp)
                    }
                }
            }

            // Right column: finances + quick actions.
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
                        .padding(10.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Outlined.AccountBalance, contentDescription = null, tint = FmAccentCyan, modifier = Modifier.size(16.dp))
                        Text("FINANCES", style = MaterialTheme.typography.labelSmall, color = FmAccentBlue, fontSize = 11.sp, letterSpacing = 0.5.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Balance: $${"%,d".format(state.humanFinance.balance)}", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Budget: $${"%,d".format(state.humanFinance.transferBudget)}", fontSize = 12.sp, color = FmContinueGreen, fontWeight = FontWeight.Bold)
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(FmSurface)
                        .border(1.dp, FmBorder, RoundedCornerShape(4.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("QUICK ACTIONS", style = MaterialTheme.typography.labelSmall, color = FmAccentBlue, fontSize = 11.sp, letterSpacing = 0.5.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(onClick = { onNavigateToSection(FmNavSection.SQUAD) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(3.dp)) {
                            Text("Squad", fontSize = 12.sp, color = FmTextPrimary, fontWeight = FontWeight.SemiBold)
                        }
                        OutlinedButton(onClick = { onNavigateToSection(FmNavSection.TACTICS) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(3.dp)) {
                            Text("Tactics", fontSize = 12.sp, color = FmTextPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(onClick = { onNavigateToSection(FmNavSection.LEAGUE) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(3.dp)) {
                            Text("League", fontSize = 12.sp, color = FmTextPrimary, fontWeight = FontWeight.SemiBold)
                        }
                        OutlinedButton(onClick = { onNavigateToSection(FmNavSection.TRANSFERS) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(3.dp)) {
                            Text("Transfers", fontSize = 12.sp, color = FmTextPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Button(
                        onClick = onSaveCareer,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = FmAccentBlue),
                        shape = RoundedCornerShape(3.dp),
                    ) {
                        Icon(Icons.Outlined.Save, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save Career", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onLoadCareer,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(3.dp),
                    ) {
                        Icon(Icons.Outlined.Upload, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Load Career", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FmTextPrimary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun MiniStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(label, style = MaterialTheme.typography.labelSmall, color = FmTextMuted, fontSize = 11.sp)
    }
}
