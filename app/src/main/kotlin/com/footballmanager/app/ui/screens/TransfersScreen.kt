package com.footballmanager.app.ui.screens

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
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.footballmanager.app.ui.theme.FmSurfaceAlt
import com.footballmanager.app.ui.theme.FmSurfaceSelected
import com.footballmanager.app.ui.theme.FmTextMuted
import com.footballmanager.app.ui.theme.FmTextPrimary
import com.footballmanager.app.ui.theme.FmTextSecondary
import com.footballmanager.app.ui.viewmodel.GameUiState
import com.footballmanager.calculator.TransferValuationCalculator
import com.footballmanager.model.Attribute
import com.footballmanager.model.Club
import com.footballmanager.model.Player
import com.footballmanager.model.PositionGroup
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
    var selectedGroupFilter by remember { mutableStateOf<PositionGroup?>(null) }

    val history = state.transferHistory
    val humanClubId = state.humanClubId
    val otherClubs = state.currentSeason.clubs.values.filter { it.id != humanClubId }
    val humanSquad = state.humanSquad

    val allMarketPlayers = remember(state.currentSeason.clubs, state.currentSeason.players) {
        otherClubs.flatMap { club ->
            club.squad.playerIds.mapNotNull { pId ->
                state.currentSeason.players[pId]?.let { p -> Pair(p, club) }
            }
        }.sortedByDescending { it.first.bestOverall() }
    }

    val filteredMarketPlayers = remember(allMarketPlayers, selectedGroupFilter) {
        if (selectedGroupFilter == null) allMarketPlayers else allMarketPlayers.filter { it.first.bestPosition().group == selectedGroupFilter }
    }

    // Selected player ID for inspector pane
    var selectedMarketPlayerId by remember {
        mutableStateOf(allMarketPlayers.firstOrNull()?.first?.id)
    }
    val selectedMarketEntry = allMarketPlayers.firstOrNull { it.first.id == selectedMarketPlayerId } ?: allMarketPlayers.firstOrNull()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(FmDarkBg),
    ) {
        val availableWidth = maxWidth
        val isExpanded = availableWidth >= 600.dp

        Column(modifier = Modifier.fillMaxSize()) {
            // ─── Header: Finances ─────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FmSurface)
                    .border(1.dp, FmBorder)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Outlined.SwapHoriz, contentDescription = null, tint = FmAccentCyan, modifier = Modifier.size(16.dp))
                        Text("TRANSFER MARKET", style = MaterialTheme.typography.labelSmall, color = FmAccentBlue, fontSize = 9.5.sp, letterSpacing = 0.5.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Balance: $${"%,d".format(state.humanFinance.balance)}", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Transfer Budget: $${"%,d".format(state.humanFinance.transferBudget)}", fontSize = 11.sp, color = FmContinueGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ─── Feedback Banners ─────────────────────────────────────────
            state.transferSuccessMessage?.let { msg ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FmContinueGreen.copy(alpha = 0.2f))
                        .border(1.dp, FmContinueGreen)
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                ) {
                    Text(msg, color = FmContinueGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            state.transferErrorMessage?.let { err ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FmRatingLow.copy(alpha = 0.2f))
                        .border(1.dp, FmRatingLow)
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                ) {
                    Text("Error: $err", color = FmRatingLow, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // ─── Subtabs ──────────────────────────────────────────────────
            TabRow(
                selectedTabIndex = selectedSubTab,
                containerColor = Color(0xFF141820),
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
                listOf("Browse Market (${filteredMarketPlayers.size})", "My Squad (${humanSquad.size})", "History (${history.size})").forEachIndexed { index, title ->
                    Tab(
                        selected = selectedSubTab == index,
                        onClick = { selectedSubTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = if (selectedSubTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedSubTab == index) Color.White else FmTextMuted,
                            )
                        },
                    )
                }
            }

            // ─── Main Content Area ────────────────────────────────────────
            when (selectedSubTab) {
                0 -> {
                    // Market Subtab
                    if (isExpanded) {
                        // ── Expanded Mode: 60% Market List | 40% Inspector & Action ──
                        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            // Left 60%: Filter + Player List
                            Column(modifier = Modifier.weight(0.60f).fillMaxHeight()) {
                                // Filter chips
                                MarketFilterRow(selectedGroupFilter) { selectedGroupFilter = it }

                                LazyColumn(
                                    modifier = Modifier.weight(1f).fillMaxWidth(),
                                    contentPadding = PaddingValues(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    items(filteredMarketPlayers, key = { it.first.id }) { (player, club) ->
                                        val isSelected = player.id == selectedMarketPlayerId
                                        val fee = (TransferValuationCalculator.calculateMarketValue(player) * 0.9).toLong().coerceAtLeast(50_000L)
                                        MarketPlayerRow(
                                            player = player,
                                            owningClub = club,
                                            fee = fee,
                                            isSelected = isSelected,
                                            onClick = { selectedMarketPlayerId = player.id },
                                        )
                                    }
                                }
                            }

                            // Right 40%: Persistent Profile & Buy Action
                            selectedMarketEntry?.let { (player, club) ->
                                val fee = (TransferValuationCalculator.calculateMarketValue(player) * 0.9).toLong().coerceAtLeast(50_000L)
                                val canAfford = state.humanFinance.balance >= fee

                                SelectedPlayerInspector(
                                    player = player,
                                    owningClub = club,
                                    fee = fee,
                                    canAfford = canAfford,
                                    currentBalance = state.humanFinance.balance,
                                    onBuy = { onDirectTransfer(player.id, club.id, humanClubId, fee) },
                                    modifier = Modifier.weight(0.40f).fillMaxHeight(),
                                )
                            }
                        }
                    } else {
                        // ── Compact Mode: Full-Width List with Inline Buy ──────────
                        Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            MarketFilterRow(selectedGroupFilter) { selectedGroupFilter = it }

                            LazyColumn(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                contentPadding = PaddingValues(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                items(filteredMarketPlayers, key = { it.first.id }) { (player, club) ->
                                    val fee = (TransferValuationCalculator.calculateMarketValue(player) * 0.9).toLong().coerceAtLeast(50_000L)
                                    val canAfford = state.humanFinance.balance >= fee
                                    MarketPlayerRowWithAction(
                                        player = player,
                                        owningClub = club,
                                        fee = fee,
                                        canAfford = canAfford,
                                        onBuy = { onDirectTransfer(player.id, club.id, humanClubId, fee) },
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Squad Sell Subtab with structured column header
                    Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF11151C))
                                .border(width = 1.dp, color = FmBorder)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("SQUAD PLAYER & VALUATION", style = MaterialTheme.typography.labelSmall, color = FmTextMuted, fontSize = 9.sp)
                            Text("BUYER STATUS / ACTION", style = MaterialTheme.typography.labelSmall, color = FmTextMuted, fontSize = 9.sp)
                        }

                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            items(humanSquad, key = { it.id }) { player ->
                                val fee = (TransferValuationCalculator.calculateMarketValue(player) * 0.9).toLong().coerceAtLeast(50_000L)
                                val buyer = otherClubs.firstOrNull { it.finance.balance >= fee }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(FmSurface)
                                        .border(1.dp, FmBorder, RoundedCornerShape(4.dp))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(player.name, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("${player.bestPosition().name} • ${player.age} yrs • OVR ${player.bestOverall()} • Form ${player.form}", fontSize = 10.5.sp, color = FmTextSecondary)
                                        Text("Valuation: $${"%,d".format(fee)}", fontSize = 11.sp, color = FmContinueGreen, fontWeight = FontWeight.Bold)
                                    }

                                    if (buyer != null) {
                                        Button(
                                            onClick = { onDirectTransfer(player.id, humanClubId, buyer.id, fee) },
                                            colors = ButtonDefaults.buttonColors(containerColor = FmAccentBlue),
                                            shape = RoundedCornerShape(3.dp),
                                        ) {
                                            Text("SELL TO ${buyer.shortName}", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        Text("No Buyers", color = FmTextMuted, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                else -> {
                    // History Subtab
                    if (history.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No completed transfers yet this season.", color = FmTextMuted, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            items(history) { rec ->
                                val toClub = state.game.club(rec.toClubId)
                                val fromClubName = rec.fromClubId?.let { state.game.club(it).name } ?: "Free Agent"
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(FmSurface)
                                        .border(1.dp, FmBorder, RoundedCornerShape(4.dp))
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column {
                                        Text(rec.playerName, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = Color.White)
                                        Text("$fromClubName -> ${toClub.name}", fontSize = 10.5.sp, color = FmTextSecondary)
                                    }
                                    Text("$${"%,d".format(rec.fee)}", color = FmContinueGreen, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MarketFilterRow(
    selectedGroup: PositionGroup?,
    onGroupSelected: (PositionGroup?) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF11151C))
            .border(width = 1.dp, color = FmBorder)
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text("FILTER:", style = MaterialTheme.typography.labelSmall, color = FmTextMuted, fontSize = 9.sp)
        val filterOptions = listOf(
            null to "ALL",
            PositionGroup.GOALKEEPER to "GK",
            PositionGroup.DEFENDER to "DEF",
            PositionGroup.MIDFIELDER to "MID",
            PositionGroup.ATTACKER to "ATT",
        )
        filterOptions.forEach { (grp, label) ->
            val isSelected = selectedGroup == grp
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (isSelected) FmAccentBlue else FmSurfaceAlt)
                    .border(1.dp, if (isSelected) FmAccentCyan else FmBorder, RoundedCornerShape(3.dp))
                    .clickable { onGroupSelected(grp) }
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) Color.White else FmTextSecondary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 9.5.sp,
                )
            }
        }
    }
}

@Composable
private fun MarketPlayerRow(
    player: Player,
    owningClub: Club,
    fee: Long,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(3.dp))
            .background(if (isSelected) FmSurfaceSelected else FmSurface)
            .border(1.dp, if (isSelected) FmAccentBlue else FmBorder, RoundedCornerShape(3.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(2.dp))
                    .background(FmAccentBlue.copy(alpha = 0.2f))
                    .padding(horizontal = 4.dp, vertical = 1.dp),
            ) {
                Text(player.bestPosition().name, fontSize = 9.sp, color = FmAccentCyan, fontWeight = FontWeight.Bold)
            }
            Column {
                Text(player.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else FmTextPrimary, maxLines = 1)
                Text("${owningClub.shortName} • ${player.age}y • OVR ${player.bestOverall()}", fontSize = 10.sp, color = FmTextSecondary)
            }
        }

        Text("$${"%,d".format(fee)}", fontSize = 11.sp, color = FmContinueGreen, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MarketPlayerRowWithAction(
    player: Player,
    owningClub: Club,
    fee: Long,
    canAfford: Boolean,
    onBuy: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(FmSurface)
            .border(1.dp, FmBorder, RoundedCornerShape(4.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(player.name, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .background(FmAccentBlue.copy(alpha = 0.2f))
                        .padding(horizontal = 4.dp, vertical = 1.dp),
                ) {
                    Text(player.bestPosition().name, fontSize = 9.sp, color = FmAccentCyan, fontWeight = FontWeight.Bold)
                }
            }
            Text("${owningClub.name} • ${player.age}y • OVR ${player.bestOverall()} • Form ${player.form}", fontSize = 10.5.sp, color = FmTextSecondary)
            Text("Fee: $${"%,d".format(fee)}", fontSize = 11.sp, color = FmContinueGreen, fontWeight = FontWeight.Bold)
        }

        Button(
            onClick = onBuy,
            enabled = canAfford,
            colors = ButtonDefaults.buttonColors(containerColor = FmContinueGreen),
            shape = RoundedCornerShape(3.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text(if (canAfford) "BUY" else "NO FUNDS", color = Color.Black, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SelectedPlayerInspector(
    player: Player,
    owningClub: Club,
    fee: Long,
    canAfford: Boolean,
    currentBalance: Long,
    onBuy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(Color(0xFF11151C))
            .border(1.dp, FmBorder)
            .padding(10.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text("PLAYER PROFILE", style = MaterialTheme.typography.labelSmall, color = FmAccentBlue, fontSize = 9.5.sp, letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(6.dp))

            Text(player.name, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("${player.nationality} • ${player.age} yrs • ${player.bestPosition().name} (OVR ${player.bestOverall()})", fontSize = 10.5.sp, color = FmTextSecondary)
            Text("Club: ${owningClub.name}", fontSize = 10.5.sp, color = FmAccentCyan)

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = FmBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // 7 Attributes Grid
            Text("KEY ATTRIBUTES", style = MaterialTheme.typography.labelSmall, color = FmTextMuted, fontSize = 9.sp)
            Spacer(modifier = Modifier.height(4.dp))
            listOf(
                Attribute.PACE,
                Attribute.SHOOTING,
                Attribute.PASSING,
                Attribute.DRIBBLING,
                Attribute.DEFENDING,
                Attribute.PHYSICAL,
            ).chunked(2).forEach { pair ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    pair.forEach { attr ->
                        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceBetween) {
                            val attrLabel = attr.name.lowercase().replaceFirstChar { it.uppercase() }
                            Text(attrLabel, fontSize = 10.sp, color = FmTextSecondary)
                            Text("${player.attributes[attr]}", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 6.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = FmBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Financial impact
            Text("FINANCIAL IMPACT", style = MaterialTheme.typography.labelSmall, color = FmTextMuted, fontSize = 9.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Transfer Fee", fontSize = 10.5.sp, color = FmTextSecondary)
                Text("$${"%,d".format(fee)}", fontSize = 11.sp, color = FmContinueGreen, fontWeight = FontWeight.Bold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Balance After", fontSize = 10.5.sp, color = FmTextSecondary)
                val remaining = currentBalance - fee
                if (remaining >= 0) {
                    Text("$${"%,d".format(remaining)}", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                } else {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(FmRatingLow.copy(alpha = 0.2f))
                            .border(1.dp, FmRatingLow, RoundedCornerShape(3.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text("⚠️", fontSize = 8.sp)
                        Text("-$${"%,d".format(-remaining)} (Deficit)", fontSize = 10.sp, color = FmRatingLow, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
        Button(
            onClick = onBuy,
            enabled = canAfford,
            colors = ButtonDefaults.buttonColors(containerColor = FmContinueGreen),
            shape = RoundedCornerShape(3.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (canAfford) "EXECUTE TRANSFER" else "INSUFFICIENT FUNDS", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
    }
}
