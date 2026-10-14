package com.footballmanager.app.ui.components

import androidx.compose.foundation.clickable
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
import com.footballmanager.model.Player
import com.footballmanager.model.PositionGroup
import com.footballmanager.model.TrainingFocus
import com.footballmanager.model.TrainingIntensity

@Composable
fun PlayerDetailContent(
    player: Player,
    onClose: () -> Unit,
    onUpdateTraining: (TrainingFocus, TrainingIntensity) -> Unit = { _, _ -> },
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

        val outfieldAttributes = listOf(
            Attribute.PACE,
            Attribute.SHOOTING,
            Attribute.PASSING,
            Attribute.DRIBBLING,
            Attribute.DEFENDING,
            Attribute.PHYSICAL,
        )
        val gkAttributes = listOf(Attribute.GOALKEEPER)

        AttributeSection("OUTFIELD ATTRIBUTES", outfieldAttributes, player)
        if (player.naturalPositions.contains(com.footballmanager.model.Position.GK) || player.attributes[Attribute.GOALKEEPER] > 1) {
            Spacer(modifier = Modifier.height(10.dp))
            AttributeSection("GOALKEEPING", gkAttributes, player)
        }
        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = FmBorder, thickness = 1.dp)
        Spacer(modifier = Modifier.height(12.dp))

        // Current Status & Condition
        Text(
            text = "STATUS & AVAILABILITY",
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
                Text("Condition", style = MaterialTheme.typography.bodySmall, color = FmTextMuted, fontSize = 10.sp)
                Text("${player.fitness}%", style = MaterialTheme.typography.titleMedium, color = if (player.fitness >= 80) FmRatingHigh else FmRatingMed, fontWeight = FontWeight.Bold)
            }
            Column {
                Text("Form", style = MaterialTheme.typography.bodySmall, color = FmTextMuted, fontSize = 10.sp)
                Text("${player.form}", style = MaterialTheme.typography.titleMedium, color = if (player.form >= 70) FmRatingHigh else if (player.form < 55) FmRatingLow else FmTextPrimary, fontWeight = FontWeight.Bold)
            }
            Column {
                Text("Potential", style = MaterialTheme.typography.bodySmall, color = FmTextMuted, fontSize = 10.sp)
                Text("${player.potential}", style = MaterialTheme.typography.titleMedium, color = FmAccentCyan, fontWeight = FontWeight.Bold)
            }
            Column {
                Text("Availability", style = MaterialTheme.typography.bodySmall, color = FmTextMuted, fontSize = 10.sp)
                val availText = when {
                    player.isInjured -> "Injured (${player.injuryStatus?.daysRemaining}d)"
                    player.isSuspended -> "Suspended (${player.suspensionStatus?.matchesRemaining}m)"
                    else -> "Available"
                }
                val availColor = if (player.isAvailable) FmRatingHigh else FmRatingLow
                Text(availText, style = MaterialTheme.typography.titleMedium, color = availColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
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

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = FmBorder, thickness = 1.dp)
        Spacer(modifier = Modifier.height(14.dp))

        // Training & Development Section
        Text(
            text = "TRAINING & DEVELOPMENT",
            style = MaterialTheme.typography.labelSmall,
            color = FmAccentBlue,
            fontSize = 11.sp,
        )
        Spacer(modifier = Modifier.height(8.dp))

        val potentialHeadroom = (player.potential - player.bestOverall()).coerceAtLeast(0)
        Text(
            text = "Growth Ceiling: ${player.potential} OVR (Headroom: +$potentialHeadroom pts)",
            style = MaterialTheme.typography.bodySmall,
            color = FmTextSecondary,
            fontSize = 10.5.sp,
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text("Training Focus:", style = MaterialTheme.typography.labelSmall, color = FmTextMuted, fontSize = 10.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            TrainingFocus.entries.take(4).forEach { focus ->
                val isSelected = player.trainingFocus == focus
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isSelected) FmAccentBlue else FmSurfaceAlt)
                        .border(1.dp, if (isSelected) FmAccentCyan else FmBorder, RoundedCornerShape(3.dp))
                        .clickable { onUpdateTraining(focus, player.trainingIntensity) }
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = focus.name.take(3),
                        fontSize = 9.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else FmTextSecondary,
                    )
                }
            }
            TrainingFocus.entries.drop(4).forEach { focus ->
                val isSelected = player.trainingFocus == focus
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isSelected) FmAccentBlue else FmSurfaceAlt)
                        .border(1.dp, if (isSelected) FmAccentCyan else FmBorder, RoundedCornerShape(3.dp))
                        .clickable { onUpdateTraining(focus, player.trainingIntensity) }
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = focus.name.take(4),
                        fontSize = 9.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else FmTextSecondary,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Intensity:", style = MaterialTheme.typography.labelSmall, color = FmTextMuted, fontSize = 10.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            TrainingIntensity.entries.forEach { intensity ->
                val isSelected = player.trainingIntensity == intensity
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isSelected) (if (intensity == TrainingIntensity.INTENSIVE) FmPosAtt else FmAccentBlue) else FmSurfaceAlt)
                        .border(1.dp, if (isSelected) Color.White else FmBorder, RoundedCornerShape(3.dp))
                        .clickable { onUpdateTraining(player.trainingFocus, intensity) }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = intensity.name,
                        fontSize = 9.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else FmTextSecondary,
                    )
                }
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
    onUpdateTraining: (TrainingFocus, TrainingIntensity) -> Unit = { _, _ -> },
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
            onUpdateTraining = onUpdateTraining,
            modifier = Modifier.fillMaxHeight(0.85f),
        )
    }
}
