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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.footballmanager.app.ui.components.TransferBidDialog
import com.footballmanager.app.ui.theme.FmAccentBlue
import com.footballmanager.app.ui.theme.FmBorder
import com.footballmanager.app.ui.theme.FmCardBg
import com.footballmanager.app.ui.theme.FmDarkBg
import com.footballmanager.app.ui.theme.FmPrimaryGreen
import com.footballmanager.app.ui.theme.FmSurface
import com.footballmanager.app.ui.theme.FmSurfaceHover
import com.footballmanager.app.ui.theme.FmTextMuted
import com.footballmanager.app.ui.theme.FmTextPrimary
import com.footballmanager.app.ui.theme.FmTextSecondary
import com.footballmanager.app.ui.viewmodel.GameUiState
import com.footballmanager.calculator.TransferValuationCalculator
import com.footballmanager.model.Player
import com.footballmanager.model.PositionGroup

@Composable
fun ScoutingScreen(
    state: GameUiState,
    onSubmitBid: (playerId: Long, fee: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedGroup by remember { mutableStateOf<PositionGroup?>(null) }
    var biddingPlayer by remember { mutableStateOf<Player?>(null) }

    val allPlayers = state.allPlayers
    val humanSquadIds = state.humanSquad.map { it.id }.toSet()

    val filteredPlayers = remember(allPlayers, searchQuery, selectedGroup) {
        allPlayers.filter { player ->
            player.id !in humanSquadIds &&
                (searchQuery.isBlank() || player.name.contains(searchQuery, ignoreCase = true)) &&
                (selectedGroup == null || player.bestPosition().group == selectedGroup)
        }.sortedByDescending { it.bestOverall() }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FmDarkBg),
    ) {
        // Top Filter Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(FmSurface)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search player name...", fontSize = 12.sp, color = FmTextMuted) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = FmTextMuted, modifier = Modifier.size(16.dp)) },
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FmAccentBlue,
                    unfocusedBorderColor = FmBorder,
                ),
                singleLine = true,
            )

            // Group Chips
            listOf(
                null to "All",
                PositionGroup.GOALKEEPER to "GK",
                PositionGroup.DEFENDER to "DEF",
                PositionGroup.MIDFIELDER to "MID",
                PositionGroup.ATTACKER to "ATT",
            ).forEach { (group, label) ->
                val isSelected = selectedGroup == group
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedGroup = group },
                    label = { Text(label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FmAccentBlue,
                        selectedLabelColor = FmTextPrimary,
                    ),
                    modifier = Modifier.height(32.dp),
                )
            }
        }

        HorizontalDivider(color = FmBorder, thickness = 1.dp)

        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(FmCardBg)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("NAME & POSITION", fontSize = 11.sp, color = FmTextMuted, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
            Text("AGE", fontSize = 11.sp, color = FmTextMuted, fontWeight = FontWeight.Bold, modifier = Modifier.width(44.dp))
            Text("OVR", fontSize = 11.sp, color = FmTextMuted, fontWeight = FontWeight.Bold, modifier = Modifier.width(44.dp))
            Text("EST. VALUATION", fontSize = 11.sp, color = FmTextMuted, fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp))
            Text("WAGE", fontSize = 11.sp, color = FmTextMuted, fontWeight = FontWeight.Bold, modifier = Modifier.width(80.dp))
            Text("ACTION", fontSize = 11.sp, color = FmTextMuted, fontWeight = FontWeight.Bold, modifier = Modifier.width(70.dp))
        }

        HorizontalDivider(color = FmBorder, thickness = 1.dp)

        // Players Table List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            items(filteredPlayers, key = { it.id }) { player ->
                val valuation = TransferValuationCalculator.calculateMarketValue(player)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(width = 0.5.dp, color = FmBorder)
                        .background(FmDarkBg)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(2f)) {
                        Text(player.name, fontSize = 13.sp, color = FmTextPrimary, fontWeight = FontWeight.SemiBold)
                        Text(player.bestPosition().name, fontSize = 11.sp, color = FmAccentBlue)
                    }

                    Text("${player.age}", fontSize = 12.sp, color = FmTextSecondary, modifier = Modifier.width(44.dp))
                    Text("${player.bestOverall()}", fontSize = 13.sp, color = FmPrimaryGreen, fontWeight = FontWeight.Bold, modifier = Modifier.width(44.dp))
                    Text("$${"%,d".format(valuation)}", fontSize = 12.sp, color = FmTextPrimary, fontWeight = FontWeight.Medium, modifier = Modifier.width(100.dp))
                    Text("$${"%,d".format(player.contract.weeklyWage)}/w", fontSize = 11.sp, color = FmTextMuted, modifier = Modifier.width(80.dp))

                    Button(
                        onClick = { biddingPlayer = player },
                        colors = ButtonDefaults.buttonColors(containerColor = FmPrimaryGreen),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier
                            .width(70.dp)
                            .height(28.dp),
                    ) {
                        Text("Bid", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        biddingPlayer?.let { player ->
            TransferBidDialog(
                player = player,
                availableBudget = state.humanFinance.transferBudget,
                onDismiss = { biddingPlayer = null },
                onSubmitBid = { fee ->
                    onSubmitBid(player.id, fee)
                    biddingPlayer = null
                },
            )
        }
    }
}
