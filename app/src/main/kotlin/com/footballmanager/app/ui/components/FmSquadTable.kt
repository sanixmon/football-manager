package com.footballmanager.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.SentimentDissatisfied
import androidx.compose.material.icons.outlined.SentimentNeutral
import androidx.compose.material.icons.outlined.SentimentSatisfied
import androidx.compose.material.icons.outlined.SentimentVerySatisfied
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.footballmanager.model.Position
import com.footballmanager.model.PositionGroup

enum class SquadSortColumn {
    POSITION,
    NAME,
    AGE,
    RATING,
    FITNESS,
    MORALE,
    WAGE,
    VALUE,
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FmSquadTable(
    players: List<Player>,
    selectedPlayerId: Long?,
    onPlayerClick: (Player) -> Unit,
    modifier: Modifier = Modifier,
) {
    var sortColumn by remember { mutableStateOf(SquadSortColumn.RATING) }
    var sortAscending by remember { mutableStateOf(false) }

    fun toggleSort(column: SquadSortColumn) {
        if (sortColumn == column) {
            sortAscending = !sortAscending
        } else {
            sortColumn = column
            sortAscending = false
        }
    }

    val sortedPlayers = remember(players, sortColumn, sortAscending) {
        val comparator: Comparator<Player> = when (sortColumn) {
            SquadSortColumn.POSITION -> compareBy { it.bestPosition().ordinal }
            SquadSortColumn.NAME -> compareBy { it.name }
            SquadSortColumn.AGE -> compareBy { it.age }
            SquadSortColumn.RATING -> compareBy { it.bestOverall() }
            SquadSortColumn.FITNESS -> compareBy { it.fitness }
            SquadSortColumn.MORALE -> compareBy { it.morale }
            SquadSortColumn.WAGE -> compareBy { it.contract.weeklyWage }
            SquadSortColumn.VALUE -> compareBy { it.bestOverall() * 100_000_000L }
        }
        if (sortAscending) players.sortedWith(comparator) else players.sortedWith(comparator.reversed())
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FmDarkBg),
    ) {
        // Sticky Header Row with Sort Indicators
        stickyHeader {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .background(Color(0xFF181B20))
                    .border(width = 1.dp, color = FmBorder)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TableHeaderCell("POS", 42.dp, sortColumn == SquadSortColumn.POSITION, sortAscending) {
                    toggleSort(SquadSortColumn.POSITION)
                }
                TableHeaderCell("PLAYER", Modifier.weight(2.6f), sortColumn == SquadSortColumn.NAME, sortAscending) {
                    toggleSort(SquadSortColumn.NAME)
                }
                TableHeaderCell("AGE", 36.dp, sortColumn == SquadSortColumn.AGE, sortAscending, TextAlign.Center) {
                    toggleSort(SquadSortColumn.AGE)
                }
                TableHeaderCell("RAT", 40.dp, sortColumn == SquadSortColumn.RATING, sortAscending, TextAlign.Center) {
                    toggleSort(SquadSortColumn.RATING)
                }
                TableHeaderCell("CON", 54.dp, sortColumn == SquadSortColumn.FITNESS, sortAscending, TextAlign.Center) {
                    toggleSort(SquadSortColumn.FITNESS)
                }
                TableHeaderCell("MOR", 36.dp, sortColumn == SquadSortColumn.MORALE, sortAscending, TextAlign.Center) {
                    toggleSort(SquadSortColumn.MORALE)
                }
                TableHeaderCell("WAGE", Modifier.weight(1.1f), sortColumn == SquadSortColumn.WAGE, sortAscending, TextAlign.End) {
                    toggleSort(SquadSortColumn.WAGE)
                }
                TableHeaderCell("VALUE", Modifier.weight(1.1f), sortColumn == SquadSortColumn.VALUE, sortAscending, TextAlign.End) {
                    toggleSort(SquadSortColumn.VALUE)
                }
            }
        }

        // Data Rows (Compact ~42dp, Zebra striping)
        itemsIndexed(sortedPlayers) { index, player ->
            val isSelected = player.id == selectedPlayerId
            val rowBg = when {
                isSelected -> FmSurfaceSelected
                index % 2 == 0 -> FmSurface
                else -> FmSurfaceAlt
            }

            val bestPos = player.bestPosition()
            val rating = player.bestOverall()

            // Rating semantic background
            val ratingColor = when {
                rating >= 70 -> FmRatingHigh
                rating >= 60 -> FmRatingMed
                else -> FmRatingLow
            }

            // Position semantic badge color
            val posBadgeColor = when (bestPos.group) {
                PositionGroup.GOALKEEPER -> FmPosGk
                PositionGroup.DEFENDER -> FmPosDef
                PositionGroup.MIDFIELDER -> FmPosMid
                PositionGroup.ATTACKER -> FmPosAtt
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .background(rowBg)
                    .clickable { onPlayerClick(player) }
                    .border(
                        width = if (isSelected) 1.dp else 0.dp,
                        color = if (isSelected) FmAccentBlue else Color.Transparent,
                    )
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 1. Position Badge (Compact Box rounded)
                Box(
                    modifier = Modifier
                        .width(42.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(posBadgeColor.copy(alpha = 0.22f))
                            .border(1.dp, posBadgeColor.copy(alpha = 0.6f), RoundedCornerShape(3.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = bestPos.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = posBadgeColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                        )
                    }
                }

                // 2. Player Name + Flag / Nationality Tag
                Row(
                    modifier = Modifier.weight(2.6f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFF2A2E36))
                            .padding(horizontal = 3.dp, vertical = 1.dp),
                    ) {
                        Text(
                            text = player.nationality.take(3).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = FmTextMuted,
                            fontSize = 9.sp,
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = player.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) Color.White else FmTextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // 3. Age
                Text(
                    text = "${player.age}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FmTextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(36.dp),
                )

                // 4. Rating (Circular/Rounded Box semantic color)
                Box(
                    modifier = Modifier.width(40.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(ratingColor),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "$rating",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                        )
                    }
                }

                // 5. Condition / Fitness (% + mini progress bar)
                Column(
                    modifier = Modifier.width(54.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "${player.fitness}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (player.fitness >= 85) FmRatingHigh else if (player.fitness >= 60) FmRatingMed else FmRatingLow,
                        fontSize = 10.sp,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    LinearProgressIndicator(
                        progress = { player.fitness / 100f },
                        modifier = Modifier
                            .width(42.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(1.5.dp)),
                        color = if (player.fitness >= 85) FmRatingHigh else if (player.fitness >= 60) FmRatingMed else FmRatingLow,
                        trackColor = FmBorder,
                    )
                }

                // 6. Morale Icon
                Box(
                    modifier = Modifier.width(36.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    val (moraleIcon, moraleTint) = when {
                        player.morale >= 70 -> Icons.Outlined.SentimentVerySatisfied to FmRatingHigh
                        player.morale >= 50 -> Icons.Outlined.SentimentSatisfied to FmRatingHigh
                        player.morale >= 35 -> Icons.Outlined.SentimentNeutral to FmRatingMed
                        else -> Icons.Outlined.SentimentDissatisfied to FmRatingLow
                    }
                    Icon(
                        imageVector = moraleIcon,
                        contentDescription = "Morale",
                        tint = moraleTint,
                        modifier = Modifier.size(16.dp),
                    )
                }

                // 7. Wage
                Text(
                    text = "Rp ${(player.contract.weeklyWage / 1_000_000).coerceAtLeast(5)}M/w",
                    style = MaterialTheme.typography.bodySmall,
                    color = FmTextSecondary,
                    fontSize = 11.sp,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier.weight(1.1f),
                )

                // 8. Value
                val estimatedValue = ((rating * rating * 450_000L) / 1_000_000_000.0)
                Text(
                    text = String.format("Rp %.1fB", estimatedValue),
                    style = MaterialTheme.typography.bodySmall,
                    color = FmAccentCyan,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier.weight(1.1f),
                )
            }

            HorizontalDivider(color = FmBorder.copy(alpha = 0.4f), thickness = 0.5.dp)
        }
    }
}

@Composable
private fun TableHeaderCell(
    title: String,
    width: androidx.compose.ui.unit.Dp,
    isSorted: Boolean,
    sortAscending: Boolean,
    textAlign: TextAlign = TextAlign.Start,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .width(width)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = when (textAlign) {
            TextAlign.Center -> Arrangement.Center
            TextAlign.End -> Arrangement.End
            else -> Arrangement.Start
        },
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSorted) FmAccentBlue else FmTextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
        )
        if (isSorted) {
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
                imageVector = if (sortAscending) Icons.Outlined.ArrowUpward else Icons.Outlined.ArrowDownward,
                contentDescription = null,
                tint = FmAccentBlue,
                modifier = Modifier.size(10.dp),
            )
        }
    }
}

@Composable
private fun TableHeaderCell(
    title: String,
    modifier: Modifier,
    isSorted: Boolean,
    sortAscending: Boolean,
    textAlign: TextAlign = TextAlign.Start,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = when (textAlign) {
            TextAlign.Center -> Arrangement.Center
            TextAlign.End -> Arrangement.End
            else -> Arrangement.Start
        },
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSorted) FmAccentBlue else FmTextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
        )
        if (isSorted) {
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
                imageVector = if (sortAscending) Icons.Outlined.ArrowUpward else Icons.Outlined.ArrowDownward,
                contentDescription = null,
                tint = FmAccentBlue,
                modifier = Modifier.size(10.dp),
            )
        }
    }
}
