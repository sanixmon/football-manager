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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.footballmanager.app.ui.theme.FmCardBg
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
import com.footballmanager.calculator.TransferValuationCalculator
import com.footballmanager.model.Player
import com.footballmanager.model.SquadStatus

@Composable
fun TransfersScreen(
    state: GameUiState,
    onDirectTransfer: (playerId: Long, fromClubId: Long, toClubId: Long, fee: Long) -> Unit = { _, _, _, _ -> },
    onOfferContract: (bidId: Long, weeklyWage: Long, years: Int, squadStatus: SquadStatus) -> Unit = { _, _, _, _ -> },
    onCompleteDeal: (bidId: Long) -> Unit = {},
    onCancelDeal: (bidId: Long) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var selectedSubTab by remember { mutableIntStateOf(0) }

    val history = state.transferHistory
    val humanClubId = state.humanClubId
    val otherClubs = state.currentSeason.clubs.values.filter { it.id != humanClubId }
    val humanSquad = state.humanSquad

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FmDarkBg),
    ) {
        // ─── Header: Finances ─────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(FmSurface)
                .border(1.dp, FmBorder)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Text("DIRECT TRANSFERS", style = MaterialTheme.typography.labelSmall, color = FmAccentBlue, fontSize = 10.sp, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(6.dp))
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

        // ─── Status Banners ───────────────────────────────────────────────
        state.transferSuccessMessage?.let { msg ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FmContinueGreen.copy(alpha = 0.2f))
                    .border(1.dp, FmContinueGreen)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(msg, color = FmContinueGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        state.transferErrorMessage?.let { err ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FmRatingLow.copy(alpha = 0.2f))
                    .border(1.dp, FmRatingLow)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text("Error: $err", color = FmRatingLow, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // ─── Subtabs ──────────────────────────────────────────────────────
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = FmSurface,
            contentColor = FmTextPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]),
                    color = FmAccentBlue,
                    height = 2.dp,
                )
            },
            divider = { HorizontalDivider(color = FmBorder, thickness = 1.dp) },
        ) {
            listOf("Market (Buy)", "My Squad (Sell)", "History (${history.size})").forEachIndexed { index, title ->
                Tab(
                    selected = selectedSubTab == index,
                    onClick = { selectedSubTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedSubTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedSubTab == index) FmTextPrimary else FmTextMuted,
                        )
                    },
                )
            }
        }

        // ─── Subtab Content ───────────────────────────────────────────────
        when (selectedSubTab) {
            0 -> {
                // Market: Browse other clubs' players to buy
                val marketPlayers = otherClubs.flatMap { club ->
                    club.squad.playerIds.mapNotNull { pId ->
                        state.currentSeason.players[pId]?.let { p -> Pair(p, club) }
                    }
                }.sortedByDescending { it.first.bestOverall() }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(marketPlayers, key = { it.first.id }) { (player, owningClub) ->
                        val fee = (TransferValuationCalculator.calculateMarketValue(player) * 0.9).toLong().coerceAtLeast(50_000L)
                        val canAfford = state.humanFinance.balance >= fee

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(FmCardBg, RoundedCornerShape(6.dp))
                                .border(1.dp, FmBorder, RoundedCornerShape(6.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(player.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(FmAccentBlue.copy(alpha = 0.2f))
                                            .padding(horizontal = 4.dp, vertical = 1.dp),
                                    ) {
                                        Text(player.bestPosition().name, fontSize = 9.sp, color = FmAccentBlue, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("${owningClub.name} • ${player.age} yrs • OVR ${player.bestOverall()} • Form ${player.form}", fontSize = 11.sp, color = FmTextSecondary)
                                Text("Fee: $${"%,d".format(fee)}", fontSize = 11.sp, color = FmAccentCyan, fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = { onDirectTransfer(player.id, owningClub.id, humanClubId, fee) },
                                enabled = canAfford,
                                colors = ButtonDefaults.buttonColors(containerColor = FmContinueGreen),
                                shape = RoundedCornerShape(4.dp),
                            ) {
                                Text(if (canAfford) "BUY" else "NO FUNDS", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            1 -> {
                // Sell: Squad players
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(humanSquad, key = { it.id }) { player ->
                        val fee = (TransferValuationCalculator.calculateMarketValue(player) * 0.9).toLong().coerceAtLeast(50_000L)
                        // Pick first AI club that can afford fee
                        val potentialBuyer = otherClubs.firstOrNull { it.finance.balance >= fee }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(FmCardBg, RoundedCornerShape(6.dp))
                                .border(1.dp, FmBorder, RoundedCornerShape(6.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(player.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("${player.bestPosition().name} • ${player.age} yrs • OVR ${player.bestOverall()} • Form ${player.form}", fontSize = 11.sp, color = FmTextSecondary)
                                Text("Valuation: $${"%,d".format(fee)}", fontSize = 11.sp, color = FmContinueGreen, fontWeight = FontWeight.SemiBold)
                            }

                            if (potentialBuyer != null) {
                                Button(
                                    onClick = { onDirectTransfer(player.id, humanClubId, potentialBuyer.id, fee) },
                                    colors = ButtonDefaults.buttonColors(containerColor = FmAccentBlue),
                                    shape = RoundedCornerShape(4.dp),
                                ) {
                                    Text("SELL TO ${potentialBuyer.shortName}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Text("No Buyers", color = FmTextMuted, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            else -> {
                // History
                if (history.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No completed transfers yet this season.", color = FmTextMuted, fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(history) { rec ->
                            val toClub = state.game.club(rec.toClubId)
                            val fromClubName = rec.fromClubId?.let { state.game.club(it).name } ?: "Free Agent"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(FmCardBg, RoundedCornerShape(6.dp))
                                    .border(1.dp, FmBorder, RoundedCornerShape(6.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column {
                                    Text(rec.playerName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                    Text("$fromClubName -> ${toClub.name}", fontSize = 11.sp, color = FmTextSecondary)
                                }
                                Text("$${"%,d".format(rec.fee)}", color = FmContinueGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
