package com.footballmanager.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.footballmanager.app.ui.theme.FmAccentCyan
import com.footballmanager.app.ui.theme.FmBorder
import com.footballmanager.app.ui.theme.FmFooterBg
import com.footballmanager.app.ui.theme.FmTextMuted
import com.footballmanager.app.ui.theme.FmTextSecondary

@Composable
fun FmFooterBar(
    playerCount: Int,
    totalWage: Long,
    wageBudget: Long = 500_000_000L,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(FmFooterBg)
            .border(width = 1.dp, color = FmBorder)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "$playerCount Players",
                style = MaterialTheme.typography.labelSmall,
                color = FmTextSecondary,
                fontSize = 10.sp,
            )
            Text(
                text = "•",
                style = MaterialTheme.typography.labelSmall,
                color = FmTextMuted,
            )
            Text(
                text = "Wage: Rp ${(totalWage / 1_000_000)}M / Rp ${(wageBudget / 1_000_000)}M p/w",
                style = MaterialTheme.typography.labelSmall,
                color = FmAccentCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Text(
            text = "Tap row for player report",
            style = MaterialTheme.typography.labelSmall,
            color = FmTextMuted,
            fontSize = 9.sp,
        )
    }
}
