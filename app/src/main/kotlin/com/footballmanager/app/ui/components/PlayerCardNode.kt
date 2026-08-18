package com.footballmanager.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.footballmanager.app.ui.theme.BorderSlate
import com.footballmanager.app.ui.theme.ElectricBlue
import com.footballmanager.app.ui.theme.StadiumEmerald
import com.footballmanager.app.ui.theme.StatusAmber
import com.footballmanager.app.ui.theme.StatusCoral
import com.footballmanager.app.ui.theme.StatusGreen
import com.footballmanager.app.ui.theme.SurfaceSlate
import com.footballmanager.model.Player
import com.footballmanager.model.Position

@Composable
fun PlayerCardNode(
    player: Player,
    slot: Position,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fitnessColor = when {
        player.fitness >= 85 -> StatusGreen
        player.fitness >= 60 -> StatusAmber
        else -> StatusCoral
    }

    val borderColor = if (isSelected) StadiumEmerald else BorderSlate

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(72.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceSlate.copy(alpha = 0.92f))
            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = slot.name,
                style = MaterialTheme.typography.labelSmall,
                color = ElectricBlue,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${player.effectiveOverall(slot)}",
                style = MaterialTheme.typography.labelSmall,
                color = StadiumEmerald,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            text = player.name.split(" ").lastOrNull() ?: player.name,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = Color.White,
        )
        Spacer(modifier = Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { player.fitness / 100f },
            modifier = Modifier.width(52.dp).height(3.dp),
            color = fitnessColor,
            trackColor = BorderSlate,
        )
    }
}
