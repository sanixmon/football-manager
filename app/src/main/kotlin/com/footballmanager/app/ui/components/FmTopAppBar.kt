package com.footballmanager.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.footballmanager.app.ui.theme.FmContinueGreen
import com.footballmanager.app.ui.theme.FmRatingHigh
import com.footballmanager.app.ui.theme.FmSurface
import com.footballmanager.app.ui.theme.FmSurfaceAlt
import com.footballmanager.app.ui.theme.FmTextMuted
import com.footballmanager.app.ui.theme.FmTextPrimary
import com.footballmanager.app.ui.theme.FmTextSecondary
import com.footballmanager.app.ui.theme.FmTopBarBg

@Composable
fun FmTopAppBar(
    clubName: String,
    currentDateText: String,
    breadcrumb: String = "",
    leaguePosition: Int = 1,
    leaguePoints: Int = 0,
    balanceText: String = "",
    budgetText: String = "",
    onMenuClick: (() -> Unit)? = null,
    continueButtonText: String = "CONTINUE",
    continueButtonColor: Color = FmContinueGreen,
    continueTextColor: Color = Color.Black,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(FmTopBarBg)
            .border(width = 1.dp, color = FmBorder),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // ── Left: Menu Drawer Toggle + Club Identity ──────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1.2f),
            ) {
                if (onMenuClick != null) {
                    IconButton(
                        onClick = onMenuClick,
                        modifier = Modifier.size(34.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Menu,
                            contentDescription = "Menu",
                            tint = FmTextPrimary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(FmAccentBlue.copy(alpha = 0.2f))
                        .border(1.dp, FmAccentBlue.copy(alpha = 0.5f), RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Shield,
                        contentDescription = null,
                        tint = FmAccentCyan,
                        modifier = Modifier.size(16.dp),
                    )
                }

                Column {
                    Text(
                        text = clubName,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                    )
                    Text(
                        text = if (breadcrumb.isNotBlank()) breadcrumb else "Liga Nusantara • $currentDateText",
                        style = MaterialTheme.typography.bodySmall,
                        color = FmTextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                    )
                }
            }

            // ── Center / Meta: Contextual Badges ──────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 4.dp),
            ) {
                // League Position Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(FmSurfaceAlt)
                        .border(1.dp, FmBorder, RoundedCornerShape(3.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = "#$leaguePosition • $leaguePoints pts",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (leaguePosition <= 3) FmRatingHigh else FmTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                // Balance Badge if provided
                if (balanceText.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(FmSurfaceAlt)
                            .border(1.dp, FmBorder, RoundedCornerShape(3.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = balanceText,
                            style = MaterialTheme.typography.labelSmall,
                            color = FmContinueGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            // ── Right: FM Signature Continue Button ───────────────────────
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(continueButtonColor)
                    .clickable { onContinueClick() }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = continueButtonText,
                        style = MaterialTheme.typography.labelLarge,
                        color = continueTextColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp,
                    )
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = continueTextColor,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
    }
}
