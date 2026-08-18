package com.footballmanager.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.footballmanager.app.ui.theme.TurfStripeDark
import com.footballmanager.app.ui.theme.TurfStripeLight
import com.footballmanager.model.Player
import com.footballmanager.model.Position
import com.footballmanager.model.PositionGroup
import com.footballmanager.simulation.Formation

@Composable
fun PitchCanvas(
    starters: List<Player>,
    formation: Formation,
    selectedPlayerId: Long?,
    onPlayerClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(340.dp)
            .clip(RoundedCornerShape(12.dp)),
    ) {
        val widthPx = maxWidth
        val heightPx = maxHeight

        // 1. Draw Turf Grass & Markings
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stripeCount = 6
            val stripeHeight = size.height / stripeCount
            for (i in 0 until stripeCount) {
                drawRect(
                    color = if (i % 2 == 0) TurfStripeDark else TurfStripeLight,
                    topLeft = Offset(0f, i * stripeHeight),
                    size = Size(size.width, stripeHeight),
                )
            }

            val lineColor = Color.White.copy(alpha = 0.35f)
            val stroke = Stroke(width = 2.dp.toPx())

            // Halfway line & center circle
            drawLine(lineColor, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), strokeWidth = 2.dp.toPx())
            drawCircle(lineColor, radius = 35.dp.toPx(), center = Offset(size.width / 2, size.height / 2), style = stroke)

            // Top Penalty Box
            drawRect(lineColor, Offset(size.width * 0.25f, 0f), Size(size.width * 0.5f, size.height * 0.18f), style = stroke)
            // Bottom Penalty Box
            drawRect(lineColor, Offset(size.width * 0.25f, size.height * 0.82f), Size(size.width * 0.5f, size.height * 0.18f), style = stroke)
        }

        // 2. Position 11 Starters According to Formation Slots
        val slots = formation.slots
        val startersWithSlots = starters.take(11).zip(slots)

        val gk = startersWithSlots.filter { it.second == Position.GK }
        val defenders = startersWithSlots.filter { it.second.group == PositionGroup.DEFENDER }
        val midfielders = startersWithSlots.filter { it.second.group == PositionGroup.MIDFIELDER }
        val attackers = startersWithSlots.filter { it.second.group == PositionGroup.ATTACKER }

        PitchRow(gk, 0.86f, widthPx, heightPx, selectedPlayerId, onPlayerClick)
        PitchRow(defenders, 0.64f, widthPx, heightPx, selectedPlayerId, onPlayerClick)
        PitchRow(midfielders, 0.38f, widthPx, heightPx, selectedPlayerId, onPlayerClick)
        PitchRow(attackers, 0.12f, widthPx, heightPx, selectedPlayerId, onPlayerClick)
    }
}

@Composable
private fun PitchRow(
    items: List<Pair<Player, Position>>,
    yFactor: Float,
    width: androidx.compose.ui.unit.Dp,
    height: androidx.compose.ui.unit.Dp,
    selectedPlayerId: Long?,
    onPlayerClick: (Long) -> Unit,
) {
    val count = items.size
    items.forEachIndexed { index, (player, slot) ->
        val xFraction = (index + 1f) / (count + 1f)
        val xOffset = (width * xFraction) - 36.dp
        val yOffset = (height * yFactor) - 20.dp

        Box(modifier = Modifier.offset(x = xOffset, y = yOffset)) {
            PlayerCardNode(
                player = player,
                slot = slot,
                isSelected = player.id == selectedPlayerId,
                onClick = { onPlayerClick(player.id) },
            )
        }
    }
}
