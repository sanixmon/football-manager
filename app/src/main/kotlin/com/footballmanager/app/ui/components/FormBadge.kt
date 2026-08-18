package com.footballmanager.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.footballmanager.app.ui.theme.StatusCoral
import com.footballmanager.app.ui.theme.StatusGreen
import com.footballmanager.app.ui.theme.SurfaceSlate

enum class MatchFormResult { WIN, DRAW, LOSS }

@Composable
fun FormBadge(result: MatchFormResult, modifier: Modifier = Modifier) {
    val (bgColor, label) = when (result) {
        MatchFormResult.WIN -> StatusGreen to "W"
        MatchFormResult.DRAW -> SurfaceSlate to "D"
        MatchFormResult.LOSS -> StatusCoral to "L"
    }
    Box(
        modifier = modifier
            .size(24.dp)
            .background(bgColor, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.White)
    }
}
