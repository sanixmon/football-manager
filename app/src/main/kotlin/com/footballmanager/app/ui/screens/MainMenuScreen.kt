package com.footballmanager.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.footballmanager.app.ui.theme.FmAccentBlue
import com.footballmanager.app.ui.theme.FmAccentCyan
import com.footballmanager.app.ui.theme.FmBorder
import com.footballmanager.app.ui.theme.FmCardBg
import com.footballmanager.app.ui.theme.FmContinueGreen
import com.footballmanager.app.ui.theme.FmSurface
import com.footballmanager.app.ui.theme.FmTextMuted
import com.footballmanager.app.ui.theme.FmTextPrimary
import com.footballmanager.app.ui.theme.FmTextSecondary
import com.footballmanager.app.ui.viewmodel.GameUiState

// Landscape-only main menu: brand panel left, career + actions right.
@Composable
fun MainMenuScreen(
    state: GameUiState,
    onContinueGame: () -> Unit,
    onLoadGame: () -> Unit = {},
    onNewGame: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val club = state.humanClub
    val manager = state.currentSeason.managerProfile?.name ?: "Manager"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF020617), Color(0xFF000000))
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.9f),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left: brand panel.
            Column(
                modifier = Modifier.weight(0.9f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(FmAccentBlue.copy(alpha = 0.2f))
                        .border(2.dp, FmAccentCyan, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SportsSoccer,
                        contentDescription = null,
                        tint = FmAccentCyan,
                        modifier = Modifier.size(40.dp),
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "FOOTBALL MANAGER",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = Color.White,
                    )
                    Text(
                        text = "MOBILE 2026 EDITION • LANDSCAPE",
                        style = MaterialTheme.typography.labelSmall,
                        color = FmContinueGreen,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp,
                    )
                }
                Text(
                    text = "Pure JVM Domain Architecture · Verified Clean",
                    fontSize = 11.sp,
                    color = FmTextMuted,
                )
            }

            // Right: career card + actions (scrollable for short height).
            Column(
                modifier = Modifier
                    .weight(1.1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(FmCardBg)
                        .border(1.dp, FmBorder, RoundedCornerShape(10.dp))
                        .padding(14.dp),
                ) {
                    Text("CURRENT CAREER", fontSize = 11.sp, color = FmAccentBlue, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(club.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = FmTextPrimary)
                            Text("$manager · Season ${state.currentSeason.currentDate.year}", fontSize = 12.sp, color = FmTextSecondary)
                        }
                        Text(
                            text = "${state.currentSeason.currentDate}",
                            fontSize = 11.sp,
                            color = FmTextMuted,
                        )
                    }
                }

                MenuButton(
                    title = "CONTINUE CAREER",
                    subtitle = "${club.name} — ${state.currentSeason.currentDate}",
                    icon = Icons.Outlined.PlayArrow,
                    isPrimary = true,
                    onClick = onContinueGame,
                )

                MenuButton(
                    title = "LOAD SAVED CAREER",
                    subtitle = "Select from saved career slots",
                    icon = Icons.Outlined.FolderOpen,
                    isPrimary = false,
                    onClick = onLoadGame,
                )

                MenuButton(
                    title = "START NEW CAREER",
                    subtitle = "Choose a new club & build your legacy",
                    icon = Icons.Outlined.AddCircleOutline,
                    isPrimary = false,
                    onClick = onNewGame,
                )
            }
        }
    }
}

@Composable
private fun MenuButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isPrimary: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPrimary) FmContinueGreen else FmSurface,
        ),
        shape = RoundedCornerShape(8.dp),
        border = if (!isPrimary) androidx.compose.foundation.BorderStroke(1.dp, FmBorder) else null,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isPrimary) Color.Black else FmTextPrimary,
                modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPrimary) Color.Black else FmTextPrimary,
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = if (isPrimary) Color.Black.copy(alpha = 0.7f) else FmTextMuted,
                )
            }
        }
    }
}
