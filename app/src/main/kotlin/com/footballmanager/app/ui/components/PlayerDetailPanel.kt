package com.footballmanager.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.footballmanager.app.ui.theme.FmTextMuted
import com.footballmanager.app.ui.theme.FmTextPrimary
import com.footballmanager.app.ui.theme.FmTextSecondary
import com.footballmanager.model.Attribute
import com.footballmanager.model.AttributeCategory
import com.footballmanager.model.Player
import com.footballmanager.model.PositionGroup

@Composable
fun PlayerDetailContent(
    player: Player,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val rating = player.bestOverall()
    val bestPos = player.bestPosition()

    val ratingColor = when {
        rating >= 70 -> FmRatingHigh
        rating >= 60 -> FmRatingMed
        else -> FmRatingLow
    }

    val posBadgeColor = when (bestPos.group) {
        PositionGroup.GOALKEEPER -> FmPosGk
        PositionGroup.DEFENDER -> FmPosDef
        PositionGroup.MIDFIELDER -> FmPosMid
        PositionGroup.ATTACKER -> FmPosAtt
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(FmSurface)
            .border(1.dp, FmBorder)
            .verticalScroll(scrollState)
            .padding(16.dp),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(FmSurfaceAlt)
                        .border(1.dp, FmBorder, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = FmAccentCyan,
                        modifier = Modifier.size(26.dp),
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = player.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "${player.nationality} • ${player.age} yrs",
                            style = MaterialTheme.typography.bodySmall,
                            color = FmTextSecondary,
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(posBadgeColor.copy(alpha = 0.2f))
                                .border(1.dp, posBadgeColor, RoundedCornerShape(3.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp),
                        ) {
                            Text(
                                text = bestPos.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = posBadgeColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(ratingColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "$rating",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Outlined.Close, contentDescription = "Close", tint = FmTextSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = FmBorder, thickness = 1.dp)
        Spacer(modifier = Modifier.height(14.dp))

        // Natural Positions
        Text(
            text = "NATURAL POSITIONS",
            style = MaterialTheme.typography.labelSmall,
            color = FmAccentBlue,
            fontSize = 11.sp,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            player.naturalPositions.forEach { pos ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF2B313D))
                        .border(1.dp, FmBorder, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "${pos.name} (${player.overall(pos)})",
                        style = MaterialTheme.typography.labelSmall,
                        color = FmTextPrimary,
                        fontSize = 11.sp,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Attributes by Category
        Text(
            text = "ATTRIBUTES OVERVIEW",
            style = MaterialTheme.typography.labelSmall,
            color = FmAccentBlue,
            fontSize = 11.sp,
        )
        Spacer(modifier = Modifier.height(8.dp))

        val techAttributes = Attribute.entries.filter { it.category == AttributeCategory.TECHNICAL }
        val mentAttributes = Attribute.entries.filter { it.category == AttributeCategory.MENTAL }
        val physAttributes = Attribute.entries.filter { it.category == AttributeCategory.PHYSICAL }

        AttributeSection("TECHNICAL", techAttributes, player)
        Spacer(modifier = Modifier.height(10.dp))
        AttributeSection("MENTAL", mentAttributes, player)
        Spacer(modifier = Modifier.height(10.dp))
        AttributeSection("PHYSICAL", physAttributes, player)

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = FmBorder, thickness = 1.dp)
        Spacer(modifier = Modifier.height(14.dp))

        // Contract Info
        Text(
            text = "CONTRACT & VALUE",
            style = MaterialTheme.typography.labelSmall,
            color = FmAccentBlue,
            fontSize = 11.sp,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(text = "Weekly Wage", style = MaterialTheme.typography.bodySmall, color = FmTextMuted)
                Text(
                    text = "$${"%,d".format(player.contract.weeklyWage)}/w",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )
            }
            Column {
                Text(text = "Expires On", style = MaterialTheme.typography.bodySmall, color = FmTextMuted)
                Text(
                    text = "${player.contract.expiresOn}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )
            }
            Column {
                Text(text = "Squad Status", style = MaterialTheme.typography.bodySmall, color = FmTextMuted)
                Text(
                    text = player.contract.squadStatus.name.replace('_', ' '),
                    style = MaterialTheme.typography.titleMedium,
                    color = FmAccentCyan,
                )
            }
        }
    }
}

@Composable
private fun AttributeSection(
    title: String,
    attributes: List<Attribute>,
    player: Player,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF1E2228))
            .border(1.dp, FmBorder, RoundedCornerShape(6.dp))
            .padding(10.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = FmTextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(6.dp))
        attributes.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                pair.forEach { attr ->
                    val attrValue = player.attributes[attr]
                    val attrColor = when {
                        attrValue >= 75 -> FmRatingHigh
                        attrValue >= 60 -> Color(0xFF86EFAC)
                        attrValue >= 45 -> FmRatingMed
                        else -> FmRatingLow
                    }
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = attr.name.replace('_', ' ').lowercase().capitalizeWords(),
                            style = MaterialTheme.typography.bodySmall,
                            color = FmTextPrimary,
                            fontSize = 11.sp,
                        )
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(attrColor.copy(alpha = 0.2f))
                                .padding(horizontal = 4.dp, vertical = 1.dp),
                        ) {
                            Text(
                                text = "$attrValue",
                                style = MaterialTheme.typography.labelSmall,
                                color = attrColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun String.capitalizeWords(): String =
    split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerDetailBottomSheet(
    player: Player?,
    onDismiss: () -> Unit,
) {
    if (player == null) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FmSurface,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        dragHandle = null,
    ) {
        PlayerDetailContent(
            player = player,
            onClose = onDismiss,
            modifier = Modifier.fillMaxHeight(0.85f),
        )
    }
}
