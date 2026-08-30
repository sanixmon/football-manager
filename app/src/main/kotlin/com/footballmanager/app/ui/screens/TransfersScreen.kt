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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.footballmanager.app.ui.components.ContractOfferDialog
import com.footballmanager.app.ui.theme.FmAccentBlue
import com.footballmanager.app.ui.theme.FmBorder
import com.footballmanager.app.ui.theme.FmCardBg
import com.footballmanager.app.ui.theme.FmContinueGreen
import com.footballmanager.app.ui.theme.FmDarkBg
import com.footballmanager.app.ui.theme.FmSurface
import com.footballmanager.app.ui.theme.FmTextMuted
import com.footballmanager.app.ui.theme.FmTextPrimary
import com.footballmanager.app.ui.theme.FmTextSecondary
import com.footballmanager.app.ui.viewmodel.GameUiState
import com.footballmanager.model.BidStatus
import com.footballmanager.model.Player
import com.footballmanager.model.SquadStatus
import com.footballmanager.model.TransferBid

@Composable
fun TransfersScreen(
    state: GameUiState,
    onOfferContract: (bidId: Long, weeklyWage: Long, years: Int, squadStatus: SquadStatus) -> Unit,
    onCompleteDeal: (bidId: Long) -> Unit,
    onCancelDeal: (bidId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedSubTab by remember { mutableIntStateOf(0) }
    var negotiatingBid by remember { mutableStateOf<TransferBid?>(null) }
    var negotiatingPlayer by remember { mutableStateOf<Player?>(null) }
    var inspectingPlayer by remember { mutableStateOf<Player?>(null) }

    val activeBids = state.activeBids
    val history = state.transferHistory

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FmDarkBg),
    ) {
        // Sub Tabs
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
            listOf("Active Deals (${activeBids.size})", "Transfer History (${history.size})").forEachIndexed { index, title ->
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

        if (selectedSubTab == 0) {
            // Active Deals List
            if (activeBids.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("No active transfer deals. Visit Scouting to bid on players.", color = FmTextMuted, fontSize = 13.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(activeBids, key = { it.id }) { bid ->
                        val player = state.currentSeason.players[bid.playerId]
                        val playerName = player?.name ?: "Player #${bid.playerId}"

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(FmCardBg, RoundedCornerShape(6.dp))
                                .border(1.dp, FmBorder, RoundedCornerShape(6.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { inspectingPlayer = player },
                            ) {
                                Text(playerName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = FmTextPrimary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Offered Fee: $${"%,d".format(bid.feeOffered)} · Status: ${bid.status.name}",
                                    fontSize = 12.sp,
                                    color = when (bid.status) {
                                        BidStatus.ACCEPTED_BY_CLUB, BidStatus.ACCEPTED_BY_PLAYER -> FmContinueGreen
                                        BidStatus.REJECTED_BY_CLUB, BidStatus.REJECTED_BY_PLAYER -> Color(0xFFEF4444)
                                        else -> FmAccentBlue
                                    },
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                when (bid.status) {
                                    BidStatus.ACCEPTED_BY_CLUB, BidStatus.TERMS_OFFERED -> {
                                        Button(
                                            onClick = {
                                                negotiatingBid = bid
                                                negotiatingPlayer = player
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = FmAccentBlue),
                                            shape = RoundedCornerShape(4.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        ) {
                                            Text("Offer Contract", fontSize = 11.sp)
                                        }
                                    }
                                    BidStatus.ACCEPTED_BY_PLAYER -> {
                                        Button(
                                            onClick = { onCompleteDeal(bid.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = FmContinueGreen),
                                            shape = RoundedCornerShape(4.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        ) {
                                            Text("Complete Sign", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                        }
                                    }
                                    else -> {}
                                }

                                OutlinedButton(
                                    onClick = { onCancelDeal(bid.id) },
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                ) {
                                    Text("Cancel", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // History List
            if (history.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("No transfers completed yet this season.", color = FmTextMuted, fontSize = 13.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(history, key = { it.id }) { record ->
                        val toClub = state.game.clubs[record.toClubId]?.name ?: "Club #${record.toClubId}"
                        val fromClub = record.fromClubId?.let { state.game.clubs[it]?.name } ?: "Free Agent"
                        val player = state.currentSeason.players[record.playerId]

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(FmCardBg, RoundedCornerShape(6.dp))
                                .border(1.dp, FmBorder, RoundedCornerShape(6.dp))
                                .clickable { inspectingPlayer = player }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(record.playerName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = FmTextPrimary)
                                Text("$fromClub → $toClub", fontSize = 11.sp, color = FmTextSecondary)
                            }
                            Text("$${"%,d".format(record.fee)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = FmContinueGreen)
                        }
                    }
                }
            }
        }

        if (negotiatingBid != null && negotiatingPlayer != null) {
            ContractOfferDialog(
                player = negotiatingPlayer!!,
                availableWeeklyWageBudget = state.humanFinance.weeklyWageBudget,
                onDismiss = {
                    negotiatingBid = null
                    negotiatingPlayer = null
                },
                onSubmitOffer = { wage, years, status ->
                    onOfferContract(negotiatingBid!!.id, wage, years, status)
                    negotiatingBid = null
                    negotiatingPlayer = null
                },
            )
        }

        com.footballmanager.app.ui.components.PlayerDetailBottomSheet(
            player = inspectingPlayer,
            onDismiss = { inspectingPlayer = null },
        )
    }
}
