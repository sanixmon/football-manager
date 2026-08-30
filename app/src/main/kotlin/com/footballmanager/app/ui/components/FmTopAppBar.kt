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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Notifications
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
import com.footballmanager.app.ui.theme.FmRatingLow
import com.footballmanager.app.ui.theme.FmTextMuted
import com.footballmanager.app.ui.theme.FmTextPrimary
import com.footballmanager.app.ui.theme.FmTextSecondary
import com.footballmanager.app.ui.theme.FmTopBarBg

@Composable
fun FmTopAppBar(
    clubName: String,
    breadcrumb: String,
    currentDateText: String,
    onMenuClick: (() -> Unit)? = null,
    continueButtonText: String = "CONTINUE",
    continueButtonColor: Color = FmContinueGreen,
    continueTextColor: Color = Color.Black,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(FmTopBarBg)
            .border(width = 1.dp, color = FmBorder)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // Left: Menu Hamburger (if mobile) + Club Crest + Club Name & Breadcrumb
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            if (onMenuClick != null) {
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Menu,
                        contentDescription = "Menu",
                        tint = FmTextPrimary,
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            // Club Crest Shield Badge
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(FmAccentBlue.copy(alpha = 0.2f))
                    .border(1.dp, FmAccentBlue.copy(alpha = 0.6f), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Shield,
                    contentDescription = null,
                    tint = FmAccentCyan,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = clubName,
                    style = MaterialTheme.typography.titleMedium,
                    color = FmTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                )
                Text(
                    text = breadcrumb,
                    style = MaterialTheme.typography.bodySmall,
                    color = FmTextSecondary,
                    fontSize = 11.sp,
                )
            }
        }

        // Right: Notification Icon + In-Game Date + Continue CTA Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Notification with badge dot
            Box(contentAlignment = Alignment.TopEnd) {
                IconButton(
                    onClick = { /* Notification action */ },
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = FmTextSecondary,
                    )
                }
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(FmRatingLow, shape = CircleShape)
                        .border(1.dp, FmTopBarBg, CircleShape),
                )
            }

            // In-Game Date Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF1E2229))
                    .border(1.dp, FmBorder, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 5.dp),
            ) {
                Text(
                    text = currentDateText,
                    style = MaterialTheme.typography.labelSmall,
                    color = FmTextSecondary,
                    fontSize = 11.sp,
                )
            }

            // FM Iconic CONTINUE Pill Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .height(34.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .background(continueButtonColor)
                    .clickable(onClick = onContinueClick)
                    .padding(horizontal = 14.dp),
            ) {
                Text(
                    text = continueButtonText,
                    color = continueTextColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = continueTextColor,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
