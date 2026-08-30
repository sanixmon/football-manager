package com.footballmanager.app.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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

@Composable
fun HomeScreen(
    state: GameUiState,
    onNavigateToMatchday: () -> Unit,
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

    // Season position
    val standings = season.standings.entries
    val myEntry = standings.firstOrNull { it.team.clubId == state.humanClubId }
    val myPosition = standings.indexOfFirst { it.team.clubId == state.humanClubId } + 1

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FmDarkBg)
            .verticalScroll(rememberScrollState()),
    ) {
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
            val daysLeft = state.daysUntilNextMatch

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
                        fontSize = 10.sp,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isToday) FmContinueGreen else Color(0xFF1E293B))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = if (isToday) "MATCHDAY TODAY" else "IN $daysLeft DAYS",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isToday) Color.Black else FmAccentCyan,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(club.name, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(if (isHome) "HOME" else "AWAY", style = MaterialTheme.typography.bodySmall, color = FmContinueGreen, fontSize = 10.sp)
                    }
                    Text("VS", style = MaterialTheme.typography.labelSmall, color = FmTextMuted, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text(opponent.name, style = MaterialTheme.typography.titleMedium, color = FmTextPrimary, fontSize = 13.sp)
                        Text("${nextFixture.date}", style = MaterialTheme.typography.bodySmall, color = FmTextMuted, fontSize = 10.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isToday) "⚡ Matchday is here! Tap PLAY MATCH in the top bar to simulate."
                           else "⏳ Daily training in progress. Tap CONTINUE in the top bar to advance 1 day and recover stamina.",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isToday) FmContinueGreen else FmTextMuted,
                    fontSize = 11.sp,
                )
            }
        } else if (season.isFinished) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FmRatingHigh.copy(alpha = 0.15f))
                    .border(1.dp, FmRatingHigh, RoundedCornerShape(0.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    "SEASON COMPLETE — Final Position: #$myPosition",
                    style = MaterialTheme.typography.titleMedium,
                    color = FmRatingHigh,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Prize money distributed, young players promoted from academy. Advance to next year's campaign.",
                    style = MaterialTheme.typography.bodySmall,
                    color = FmTextPrimary,
                )
                androidx.compose.material3.Button(
                    onClick = onRolloverSeason,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = FmContinueGreen),
                    shape = RoundedCornerShape(6.dp),
                ) {
                    Text("START NEXT SEASON >", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(1.dp))

        // ─── Squad Condition ──────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(FmSurface)
                .border(1.dp, FmBorder)
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Text("SQUAD CONDITION", style = MaterialTheme.typography.labelSmall, color = FmAccentBlue, fontSize = 10.sp, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(10.dp))

            ConditionGauge("Average Fitness", avgFitness, Icons.Outlined.FitnessCenter)
            Spacer(modifier = Modifier.height(8.dp))
            ConditionGauge("Average Morale", avgMorale, Icons.Outlined.TrendingUp)

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = FmBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("${squad.size} players in squad", style = MaterialTheme.typography.bodySmall, color = FmTextMuted, fontSize = 11.sp)
        }

        Spacer(modifier = Modifier.height(1.dp))

        // ─── Last Result ──────────────────────────────────────────────────
        val lastResult = state.lastMatchResult
        if (lastResult != null) {
            val homeClub = state.game.club(lastResult.homeClubId)
            val awayClub = state.game.club(lastResult.awayClubId)
            val userIsHome = lastResult.homeClubId == state.humanClubId
            val userWon = (userIsHome && lastResult.isHomeWin) || (!userIsHome && lastResult.isAwayWin)
            val resultColor = if (userWon) FmRatingHigh else if (lastResult.isDraw) FmRatingMed else FmRatingLow
            val resultLabel = if (userWon) "WIN" else if (lastResult.isDraw) "DRAW" else "LOSS"

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FmSurface)
                    .border(1.dp, FmBorder),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(resultColor.copy(alpha = 0.12f))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                ) {
                    Text("LAST RESULT — $resultLabel", style = MaterialTheme.typography.labelSmall, color = resultColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(homeClub.name, style = MaterialTheme.typography.bodyMedium, color = FmTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Text("${lastResult.homeScore}  –  ${lastResult.awayScore}", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Black)
                    Text(awayClub.name, style = MaterialTheme.typography.bodyMedium, color = FmTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun KeyStat(label: String, value: String, color: Color = FmTextSecondary) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = color, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, style = MaterialTheme.typography.bodySmall, color = FmTextMuted, fontSize = 10.sp)
    }
}

@Composable
private fun ConditionGauge(label: String, value: Int, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    val color = when {
        value >= 75 -> FmRatingHigh
        value >= 50 -> FmRatingMed
        else -> FmRatingLow
    }
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(label, style = MaterialTheme.typography.bodySmall, color = FmTextSecondary, fontSize = 11.sp)
                Text("$value / 100", style = MaterialTheme.typography.labelSmall, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(3.dp))
            LinearProgressIndicator(
                progress = { value / 100f },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = color,
                trackColor = FmBorder,
            )
        }
    }
}
