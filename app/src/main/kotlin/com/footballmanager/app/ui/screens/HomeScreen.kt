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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
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
import com.footballmanager.app.ui.theme.FmContinueGreen
import com.footballmanager.app.ui.theme.FmDarkBg
import com.footballmanager.app.ui.theme.FmRatingHigh
import com.footballmanager.app.ui.theme.FmRatingLow
import com.footballmanager.app.ui.theme.FmRatingMed
import com.footballmanager.app.ui.theme.FmSurface
import com.footballmanager.app.ui.theme.FmSurfaceSelected
import com.footballmanager.app.ui.theme.FmTextMuted
import com.footballmanager.app.ui.theme.FmTextPrimary
import com.footballmanager.app.ui.theme.FmTextSecondary
import com.footballmanager.app.ui.viewmodel.GameUiState

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FmDarkBg)
            .verticalScroll(rememberScrollState()),
    ) {
        // ─── Status Notification Banner ───────────────────────────────────
        state.saveStatusMessage?.let { msg ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FmAccentBlue.copy(alpha = 0.2f))
                    .border(1.dp, FmAccentBlue)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text(msg, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // ─── Club Hero Strip ──────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(FmSurface)
                .border(1.dp, FmBorder)
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(FmAccentBlue.copy(alpha = 0.2f))
                            .border(1.dp, FmAccentBlue.copy(alpha = 0.6f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.Shield, contentDescription = null, tint = FmAccentCyan, modifier = Modifier.size(26.dp))
                    }
                    Column {
                        Text(club.name, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Manager • Liga Nusantara", style = MaterialTheme.typography.bodySmall, color = FmTextSecondary, fontSize = 11.sp)
                    }
                }
                // League position badge
                Column(horizontalAlignment = Alignment.End) {
                    Text("POSITION", style = MaterialTheme.typography.labelSmall, color = FmTextMuted, fontSize = 9.sp)
                    Text(
                        "#$myPosition",
                        style = MaterialTheme.typography.headlineLarge,
                        color = if (myPosition <= 3) FmRatingHigh else if (myPosition <= 6) FmAccentCyan else FmTextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = FmBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Key stats strip
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                myEntry?.let { entry ->
                    KeyStat("Played", "${entry.played}")
                    KeyStat("Won", "${entry.won}", FmRatingHigh)
                    KeyStat("Drawn", "${entry.drawn}", FmRatingMed)
                    KeyStat("Lost", "${entry.lost}", FmRatingLow)
                    KeyStat("Points", "${entry.points}", FmContinueGreen)
                } ?: KeyStat("Season", "Starting", FmAccentBlue)
            }
        }

        Spacer(modifier = Modifier.height(1.dp))

        // ─── Next Match Hero Card ─────────────────────────────────────────
        if (nextFixture != null && !season.isFinished) {
            val opponentId = if (nextFixture.home.clubId == club.id) nextFixture.away.clubId else nextFixture.home.clubId
            val isHome = nextFixture.home.clubId == club.id
            val opponent = state.game.club(opponentId)
            val isToday = state.isMatchdayToday

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isToday) FmSurfaceSelected else FmSurface)
                    .border(1.dp, if (isToday) FmContinueGreen else FmBorder)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "NEXT MATCH — ROUND $nextMatchday",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isToday) FmContinueGreen else FmAccentBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp,
                    )
                    Text(
                        if (isHome) "HOME" else "AWAY",
                        style = MaterialTheme.typography.labelSmall,
                        color = FmTextSecondary,
                        fontSize = 10.sp,
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            text = if (isHome) "${club.name}  vs  ${opponent.name}" else "${opponent.name}  vs  ${club.name}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp,
                        )
                        Text(
                            text = "Fixture Date: ${nextFixture.date}",
                            style = MaterialTheme.typography.bodySmall,
                            color = FmTextSecondary,
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
                        Text("MATCH", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(1.dp))

        // ─── Squad Availability & Condition ───────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(FmSurface)
                .border(1.dp, FmBorder)
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Text("SQUAD AVAILABILITY & CONDITION", style = MaterialTheme.typography.labelSmall, color = FmAccentBlue, fontSize = 10.sp, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                KeyStat("Available", "$availableCount", FmContinueGreen)
                KeyStat("Injured", "$injuredCount", if (injuredCount > 0) FmRatingLow else FmTextMuted)
                KeyStat("Suspended", "$suspendedCount", if (suspendedCount > 0) FmRatingLow else FmTextMuted)
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = FmBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            ConditionGauge("Average Fitness", avgFitness, Icons.Outlined.FitnessCenter)
            Spacer(modifier = Modifier.height(8.dp))
            ConditionGauge("Average Morale", avgMorale, Icons.Outlined.TrendingUp)
        }

        Spacer(modifier = Modifier.height(1.dp))

        // ─── Club Finances ────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(FmSurface)
                .border(1.dp, FmBorder)
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Text("FINANCES", style = MaterialTheme.typography.labelSmall, color = FmAccentBlue, fontSize = 10.sp, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Club Balance", style = MaterialTheme.typography.bodySmall, color = FmTextMuted, fontSize = 11.sp)
                    Text("$${"%,d".format(state.humanFinance.balance)}", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Transfer Budget", style = MaterialTheme.typography.bodySmall, color = FmTextMuted, fontSize = 11.sp)
                    Text("$${"%,d".format(state.humanFinance.transferBudget)}", style = MaterialTheme.typography.titleMedium, color = FmContinueGreen, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(1.dp))

        // ─── Quick Actions & Save/Load ─────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(FmSurface)
                .border(1.dp, FmBorder)
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Text("QUICK NAVIGATION & SAVE", style = MaterialTheme.typography.labelSmall, color = FmAccentBlue, fontSize = 10.sp, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(onClick = { onNavigateToSection(FmNavSection.SQUAD) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(4.dp)) {
                    Text("Squad", fontSize = 11.sp, color = FmTextPrimary)
                }
                OutlinedButton(onClick = { onNavigateToSection(FmNavSection.TACTICS) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(4.dp)) {
                    Text("Tactics", fontSize = 11.sp, color = FmTextPrimary)
                }
                OutlinedButton(onClick = { onNavigateToSection(FmNavSection.LEAGUE) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(4.dp)) {
                    Text("League", fontSize = 11.sp, color = FmTextPrimary)
                }
                OutlinedButton(onClick = { onNavigateToSection(FmNavSection.TRANSFERS) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(4.dp)) {
                    Text("Transfers", fontSize = 11.sp, color = FmTextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onSaveCareer,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = FmAccentBlue),
                    shape = RoundedCornerShape(4.dp),
                ) {
                    Icon(Icons.Outlined.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Career", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onLoadCareer,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(4.dp),
                ) {
                    Icon(Icons.Outlined.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Load Career", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FmTextPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun KeyStat(label: String, value: String, color: Color = FmTextSecondary) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(label, style = MaterialTheme.typography.labelSmall, color = FmTextMuted, fontSize = 10.sp)
    }
}

@Composable
private fun ConditionGauge(label: String, value: Int, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(icon, contentDescription = null, tint = FmAccentBlue, modifier = Modifier.size(14.dp))
                Text(label, style = MaterialTheme.typography.bodySmall, color = FmTextPrimary, fontSize = 11.sp)
            }
            Text(
                "$value%",
                style = MaterialTheme.typography.labelSmall,
                color = if (value >= 75) FmRatingHigh else if (value >= 50) FmRatingMed else FmRatingLow,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
            )
        }
    }
}
