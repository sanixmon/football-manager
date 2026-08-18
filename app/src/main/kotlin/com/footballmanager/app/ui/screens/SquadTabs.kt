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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material.icons.outlined.TrendingFlat
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.footballmanager.app.ui.theme.FmAccentBlue
import com.footballmanager.app.ui.theme.FmAccentCyan
import com.footballmanager.app.ui.theme.FmBorder
import com.footballmanager.app.ui.theme.FmDarkBg
import com.footballmanager.app.ui.theme.FmPosAtt
import com.footballmanager.app.ui.theme.FmPosDef
import com.footballmanager.app.ui.theme.FmPosGk
import com.footballmanager.app.ui.theme.FmPosMid
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
import com.footballmanager.model.Player
import com.footballmanager.model.PositionGroup
import com.footballmanager.model.SquadStatus

// ── Report Tab: Individual player season report scores ────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SquadReportTab(
    players: List<Player>,
    selectedPlayerId: Long?,
    onPlayerClick: (Player) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize().background(FmDarkBg)) {
        stickyHeader {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .background(Color(0xFF181B20))
                    .border(1.dp, FmBorder)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("PLAYER", style = MaterialTheme.typography.labelSmall, color = FmTextSecondary, fontSize = 10.sp, modifier = Modifier.weight(2f))
                Text("OVERALL", style = MaterialTheme.typography.labelSmall, color = FmTextSecondary, fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.width(56.dp))
                Text("FITNESS", style = MaterialTheme.typography.labelSmall, color = FmTextSecondary, fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.width(60.dp))
                Text("MORALE", style = MaterialTheme.typography.labelSmall, color = FmTextSecondary, fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.width(60.dp))
                Text("STATUS", style = MaterialTheme.typography.labelSmall, color = FmTextSecondary, fontSize = 10.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
            }
        }
        itemsIndexed(players.sortedByDescending { it.bestOverall() }) { index, player ->
            val isSelected = player.id == selectedPlayerId
            val bg = when {
                isSelected -> FmSurfaceSelected
                index % 2 == 0 -> FmSurface
                else -> FmSurfaceAlt
            }
            val rating = player.bestOverall()
            val ratingColor = when {
                rating >= 70 -> FmRatingHigh
                rating >= 60 -> FmRatingMed
                else -> FmRatingLow
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .background(bg)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Player name
                Column(modifier = Modifier.weight(2f)) {
                    Text(
                        text = player.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = FmTextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = player.bestPosition().name,
                        style = MaterialTheme.typography.bodySmall,
                        color = FmTextMuted,
                        fontSize = 10.sp,
                    )
                }

                // Overall rating circle
                Box(modifier = Modifier.width(56.dp), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier.size(26.dp).clip(CircleShape).background(ratingColor),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("$rating", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }

                // Fitness bar
                Column(modifier = Modifier.width(60.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${player.fitness}%",
                        fontSize = 10.sp,
                        color = if (player.fitness >= 85) FmRatingHigh else if (player.fitness >= 60) FmRatingMed else FmRatingLow,
                    )
                    LinearProgressIndicator(
                        progress = { player.fitness / 100f },
                        modifier = Modifier.width(44.dp).height(3.dp).clip(RoundedCornerShape(1.5.dp)),
                        color = if (player.fitness >= 85) FmRatingHigh else if (player.fitness >= 60) FmRatingMed else FmRatingLow,
                        trackColor = FmBorder,
                    )
                }

                // Morale bar
                Column(modifier = Modifier.width(60.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${player.morale}%",
                        fontSize = 10.sp,
                        color = if (player.morale >= 70) FmRatingHigh else if (player.morale >= 40) FmRatingMed else FmRatingLow,
                    )
                    LinearProgressIndicator(
                        progress = { player.morale / 100f },
                        modifier = Modifier.width(44.dp).height(3.dp).clip(RoundedCornerShape(1.5.dp)),
                        color = if (player.morale >= 70) FmRatingHigh else if (player.morale >= 40) FmRatingMed else FmRatingLow,
                        trackColor = FmBorder,
                    )
                }

                // Squad Status label
                val (statusColor, statusText) = when (player.contract.squadStatus) {
                    SquadStatus.KEY_PLAYER -> FmRatingHigh to "KEY"
                    SquadStatus.FIRST_TEAM -> FmAccentCyan to "FIRST"
                    SquadStatus.ROTATION -> FmAccentBlue to "ROT"
                    SquadStatus.BACKUP -> FmTextSecondary to "BACK"
                    SquadStatus.YOUTH -> FmTextMuted to "YOUTH"
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(statusText, style = MaterialTheme.typography.labelSmall, color = statusColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            HorizontalDivider(color = FmBorder.copy(alpha = 0.4f), thickness = 0.5.dp)
        }
    }
}

// ── Dynamics Tab: Group cohesion, morale by position group ───────────────────
@Composable
fun SquadDynamicsTab(players: List<Player>) {
    val groups = listOf(
        "Goalkeepers" to PositionGroup.GOALKEEPER,
        "Defenders" to PositionGroup.DEFENDER,
        "Midfielders" to PositionGroup.MIDFIELDER,
        "Attackers" to PositionGroup.ATTACKER,
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(FmDarkBg).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("TEAM DYNAMICS", style = MaterialTheme.typography.labelSmall, color = FmAccentBlue, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))

            val totalMorale = if (players.isEmpty()) 0 else players.map { it.morale }.average().toInt()
            val totalFitness = if (players.isEmpty()) 0 else players.map { it.fitness }.average().toInt()

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DynamicsStatCard("Team Morale", totalMorale, Modifier.weight(1f))
                DynamicsStatCard("Avg Fitness", totalFitness, Modifier.weight(1f))
            }
        }

        groups.forEach { (label, group) ->
            val groupPlayers = players.filter { p -> p.naturalPositions.any { it.group == group } }
            if (groupPlayers.isNotEmpty()) {
                item {
                    val avgMorale = groupPlayers.map { it.morale }.average().toInt()
                    val groupColor = when (group) {
                        PositionGroup.GOALKEEPER -> FmPosGk
                        PositionGroup.DEFENDER -> FmPosDef
                        PositionGroup.MIDFIELDER -> FmPosMid
                        PositionGroup.ATTACKER -> FmPosAtt
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(FmSurface)
                            .border(1.dp, FmBorder, RoundedCornerShape(6.dp))
                            .padding(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(groupColor),
                                )
                                Text(label, style = MaterialTheme.typography.titleSmall, color = FmTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Text("${groupPlayers.size} players", style = MaterialTheme.typography.bodySmall, color = FmTextMuted)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Cohesion", style = MaterialTheme.typography.bodySmall, color = FmTextSecondary)
                            val moraleColor = if (avgMorale >= 70) FmRatingHigh else if (avgMorale >= 40) FmRatingMed else FmRatingLow
                            Text("$avgMorale / 100", style = MaterialTheme.typography.bodySmall, color = moraleColor, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        val moraleColor = if (avgMorale >= 70) FmRatingHigh else if (avgMorale >= 40) FmRatingMed else FmRatingLow
                        LinearProgressIndicator(
                            progress = { avgMorale / 100f },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                            color = moraleColor,
                            trackColor = FmBorder,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // Player name list
                        groupPlayers.forEach { p ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                val morTrend = when {
                                    p.morale >= 70 -> Icons.Outlined.TrendingUp to FmRatingHigh
                                    p.morale >= 40 -> Icons.Outlined.TrendingFlat to FmRatingMed
                                    else -> Icons.Outlined.TrendingDown to FmRatingLow
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(morTrend.first, contentDescription = null, tint = morTrend.second, modifier = Modifier.size(14.dp))
                                    Text(p.name, style = MaterialTheme.typography.bodySmall, color = FmTextPrimary, fontSize = 11.sp)
                                }
                                Text("${p.morale}%", style = MaterialTheme.typography.bodySmall, color = morTrend.second, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DynamicsStatCard(label: String, value: Int, modifier: Modifier = Modifier) {
    val valueColor = when {
        value >= 70 -> FmRatingHigh
        value >= 40 -> FmRatingMed
        else -> FmRatingLow
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(FmSurface)
            .border(1.dp, FmBorder, RoundedCornerShape(6.dp))
            .padding(12.dp),
    ) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = FmTextMuted, fontSize = 10.sp, letterSpacing = 0.5.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text("$value", style = MaterialTheme.typography.headlineLarge, color = valueColor, fontWeight = FontWeight.Black, fontSize = 28.sp)
        Text("/ 100", style = MaterialTheme.typography.bodySmall, color = FmTextMuted)
    }
}

// ── Stats Tab: Attribute category summary per player ─────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SquadStatsTab(
    players: List<Player>,
    selectedPlayerId: Long?,
    onPlayerClick: (Player) -> Unit,
) {
    val categories = listOf("Technical" to listOf("PASSING", "DRIBBLING", "FINISHING", "TACKLING"),
                            "Physical"  to listOf("PACE", "STAMINA", "STRENGTH", "AGILITY"),
                            "Mental"    to listOf("COMPOSURE", "POSITIONING", "VISION", "DECISION_MAKING"))

    LazyColumn(modifier = Modifier.fillMaxSize().background(FmDarkBg)) {
        stickyHeader {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .background(Color(0xFF181B20))
                    .border(1.dp, FmBorder)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("PLAYER", style = MaterialTheme.typography.labelSmall, color = FmTextSecondary, fontSize = 10.sp, modifier = Modifier.weight(2f))
                Text("TEC", style = MaterialTheme.typography.labelSmall, color = FmTextSecondary, fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.width(40.dp))
                Text("PHY", style = MaterialTheme.typography.labelSmall, color = FmTextSecondary, fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.width(40.dp))
                Text("MEN", style = MaterialTheme.typography.labelSmall, color = FmTextSecondary, fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.width(40.dp))
                Text("OVR", style = MaterialTheme.typography.labelSmall, color = FmTextSecondary, fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.width(44.dp))
            }
        }
        itemsIndexed(players.sortedByDescending { it.bestOverall() }) { index, player ->
            val isSelected = player.id == selectedPlayerId
            val bg = when {
                isSelected -> FmSurfaceSelected
                index % 2 == 0 -> FmSurface
                else -> FmSurfaceAlt
            }

            // Compute category averages from attributes map
            val attrMap = player.attributes.toMap()
            val tecAvg = attrMap.entries.filter { it.key.name in listOf("PASSING","DRIBBLING","FINISHING","TACKLING","CROSSING","FIRST_TOUCH") }.map { it.value }.average().toInt()
            val phyAvg = attrMap.entries.filter { it.key.name in listOf("PACE","STAMINA","STRENGTH","AGILITY","ACCELERATION") }.map { it.value }.average().toInt()
            val menAvg = attrMap.entries.filter { it.key.name in listOf("COMPOSURE","POSITIONING","VISION","DECISION_MAKING","WORK_RATE","LEADERSHIP") }.map { it.value }.average().toInt()
            val overall = player.bestOverall()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .background(bg)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(2f)) {
                    Text(
                        player.name, style = MaterialTheme.typography.bodyMedium, color = FmTextPrimary,
                        fontWeight = FontWeight.SemiBold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                    Text(player.bestPosition().name, style = MaterialTheme.typography.bodySmall, color = FmTextMuted, fontSize = 10.sp)
                }
                CategoryStatBox(tecAvg, Modifier.width(40.dp))
                CategoryStatBox(phyAvg, Modifier.width(40.dp))
                CategoryStatBox(menAvg, Modifier.width(40.dp))
                // Overall
                Box(modifier = Modifier.width(44.dp), contentAlignment = Alignment.Center) {
                    val ovrColor = if (overall >= 70) FmRatingHigh else if (overall >= 60) FmRatingMed else FmRatingLow
                    Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(ovrColor), contentAlignment = Alignment.Center) {
                        Text("$overall", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
            }
            HorizontalDivider(color = FmBorder.copy(alpha = 0.4f), thickness = 0.5.dp)
        }
    }
}

@Composable
private fun CategoryStatBox(value: Int, modifier: Modifier = Modifier) {
    val color = when {
        value >= 70 -> FmRatingHigh
        value >= 55 -> FmRatingMed
        else -> FmRatingLow
    }
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text("$value", style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold, fontSize = 11.sp)
    }
}

// ── Contracts Tab: Salary + expiry + status ───────────────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SquadContractsTab(
    players: List<Player>,
    selectedPlayerId: Long?,
    onPlayerClick: (Player) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize().background(FmDarkBg)) {
        stickyHeader {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .background(Color(0xFF181B20))
                    .border(1.dp, FmBorder)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("PLAYER", style = MaterialTheme.typography.labelSmall, color = FmTextSecondary, fontSize = 10.sp, modifier = Modifier.weight(2f))
                Text("STATUS", style = MaterialTheme.typography.labelSmall, color = FmTextSecondary, fontSize = 10.sp, modifier = Modifier.weight(1f))
                Text("WAGE /w", style = MaterialTheme.typography.labelSmall, color = FmTextSecondary, fontSize = 10.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                Text("EXPIRES", style = MaterialTheme.typography.labelSmall, color = FmTextSecondary, fontSize = 10.sp, textAlign = TextAlign.End, modifier = Modifier.width(72.dp))
            }
        }
        itemsIndexed(players.sortedByDescending { it.contract.weeklyWage }) { index, player ->
            val isSelected = player.id == selectedPlayerId
            val bg = when {
                isSelected -> FmSurfaceSelected
                index % 2 == 0 -> FmSurface
                else -> FmSurfaceAlt
            }

            val expiresStr = player.contract.expiresOn.toString()
            val yearsLeft = player.contract.expiresOn.year - 2026
            val expiresColor = when {
                yearsLeft <= 0 -> FmRatingLow
                yearsLeft == 1 -> FmRatingMed
                else -> FmTextPrimary
            }

            val (statusColor, statusText) = when (player.contract.squadStatus) {
                SquadStatus.KEY_PLAYER -> FmRatingHigh to "Key Player"
                SquadStatus.FIRST_TEAM -> FmAccentCyan to "First Team"
                SquadStatus.ROTATION -> FmAccentBlue to "Rotation"
                SquadStatus.BACKUP -> FmTextSecondary to "Backup"
                SquadStatus.YOUTH -> FmTextMuted to "Youth"
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .background(bg)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(2f)) {
                    Text(
                        player.name, style = MaterialTheme.typography.bodyMedium, color = FmTextPrimary,
                        fontWeight = FontWeight.SemiBold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                    Text(player.naturalPositions.firstOrNull()?.name ?: "", style = MaterialTheme.typography.bodySmall, color = FmTextMuted, fontSize = 10.sp)
                }
                Text(statusText, style = MaterialTheme.typography.bodySmall, color = statusColor, fontSize = 10.sp, modifier = Modifier.weight(1f))
                Text(
                    "Rp ${(player.contract.weeklyWage / 1_000_000).coerceAtLeast(5)}M",
                    style = MaterialTheme.typography.bodySmall, color = FmTextPrimary, fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End, modifier = Modifier.weight(1f),
                )
                Text(
                    expiresStr.take(7),
                    style = MaterialTheme.typography.bodySmall, color = expiresColor, fontSize = 10.sp,
                    textAlign = TextAlign.End, modifier = Modifier.width(72.dp),
                )
            }
            HorizontalDivider(color = FmBorder.copy(alpha = 0.4f), thickness = 0.5.dp)
        }
    }
}
