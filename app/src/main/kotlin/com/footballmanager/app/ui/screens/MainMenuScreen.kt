package com.footballmanager.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.footballmanager.app.ui.theme.FmDarkBg
import com.footballmanager.app.ui.theme.FmSurface
import com.footballmanager.app.ui.theme.FmTextMuted
import com.footballmanager.app.ui.theme.FmTextPrimary
import com.footballmanager.app.ui.theme.FmTextSecondary
import com.footballmanager.app.ui.viewmodel.GameUiState

@Composable
fun MainMenuScreen(
    state: GameUiState,
    onContinueGame: () -> Unit,
    onNewGame: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val club = state.humanClub
    val manager = state.currentSeason.managerProfile?.name ?: "Manager"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF020617), Color(0xFF000000))
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .width(420.dp)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Game Brand Logo
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(FmAccentBlue.copy(alpha = 0.2f))
                    .border(2.dp, FmAccentCyan, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.SportsSoccer,
                    contentDescription = null,
                    tint = FmAccentCyan,
                    modifier = Modifier.size(36.dp),
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
                    text = "MOBILE 2026 EDITION",
                    style = MaterialTheme.typography.labelSmall,
                    color = FmContinueGreen,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Active Career Preview Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(FmCardBg)
                    .border(1.dp, FmBorder, RoundedCornerShape(10.dp))
                    .padding(16.dp),
            ) {
                Text("CURRENT CAREER", fontSize = 10.sp, color = FmAccentBlue, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
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
                        text = "Day ${state.currentSeason.currentDate}",
                        fontSize = 11.sp,
                        color = FmTextMuted,
                    )
                }
            }

            // Main Menu Action Buttons
            MenuButton(
                title = "CONTINUE CAREER",
                subtitle = "${club.name} — ${state.currentSeason.currentDate}",
                icon = Icons.Outlined.PlayArrow,
                isPrimary = true,
                onClick = onContinueGame,
            )

            MenuButton(
                title = "START NEW CAREER",
                subtitle = "Choose a new club & build your legacy",
                icon = Icons.Outlined.AddCircleOutline,
                isPrimary = false,
                onClick = onNewGame,
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Pure JVM Domain Architecture · Verified Clean",
                fontSize = 11.sp,
                color = FmTextMuted,
            )
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
                    fontSize = 10.sp,
                    color = if (isPrimary) Color.Black.copy(alpha = 0.7f) else FmTextMuted,
                )
            }
        }
    }
}
