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
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.footballmanager.app.ui.theme.FmAccentBlue
import com.footballmanager.app.ui.theme.FmAccentCyan
import com.footballmanager.app.ui.theme.FmBorder
import com.footballmanager.app.ui.theme.FmRatingHigh
import com.footballmanager.app.ui.theme.FmRatingLow
import com.footballmanager.app.ui.theme.FmRatingMed
import com.footballmanager.app.ui.theme.FmSurface
import com.footballmanager.app.ui.theme.FmTextMuted
import com.footballmanager.app.ui.theme.FmTextPrimary
import com.footballmanager.app.ui.theme.FmTextSecondary

// Shared section heading: kicker label + optional trailing action.
// Replaces ad-hoc uppercase blue labels scattered across screens.
@Composable
fun FmSectionHeader(
    title: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = FmAccentBlue,
            fontSize = 11.sp,
            letterSpacing = 0.5.sp,
        )
        if (actionLabel != null && onActionClick != null) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .clickable(onClick = onActionClick)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(actionLabel, fontSize = 11.sp, color = FmAccentCyan, fontWeight = FontWeight.Bold)
                Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = FmAccentCyan, modifier = Modifier.size(14.dp))
            }
        }
    }
    Spacer(modifier = Modifier.height(6.dp))
    HorizontalDivider(color = FmBorder, thickness = 1.dp)
    Spacer(modifier = Modifier.height(6.dp))
}

// Status pill that always pairs color with an icon + text label.
// Never communicates status by color alone.
@Composable
fun FmStatusBadge(
    text: String,
    status: FmStatus,
    modifier: Modifier = Modifier,
) {
    val (bg, fg) = when (status) {
        FmStatus.POSITIVE -> FmRatingHigh.copy(alpha = 0.18f) to FmRatingHigh
        FmStatus.WARNING -> FmRatingMed.copy(alpha = 0.18f) to FmRatingMed
        FmStatus.NEGATIVE -> FmRatingLow.copy(alpha = 0.18f) to FmRatingLow
        FmStatus.INFO -> FmAccentCyan.copy(alpha = 0.15f) to FmAccentCyan
        FmStatus.NEUTRAL -> FmSurface to FmTextSecondary
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(3.dp))
            .background(bg)
            .border(1.dp, fg.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(fg),
        )
        Text(text, fontSize = 11.sp, color = fg, fontWeight = FontWeight.Bold)
    }
}

enum class FmStatus { POSITIVE, WARNING, NEGATIVE, INFO, NEUTRAL }

// Dismissible banner for transient messages (save confirmations, errors).
@Composable
fun FmNoticeBanner(
    message: String,
    accent: Color,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(FmSurface)
            .border(1.dp, accent)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            message,
            color = FmTextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
        if (onDismiss != null) {
            IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Outlined.Close, contentDescription = "Dismiss", tint = FmTextMuted, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// Centered empty state for lists with no data.
@Composable
fun FmEmptyState(
    title: String,
    detail: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(icon, contentDescription = null, tint = FmTextMuted, modifier = Modifier.size(28.dp))
        Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = FmTextPrimary)
        Text(detail, fontSize = 11.sp, color = FmTextSecondary)
        Spacer(modifier = Modifier.height(2.dp))
    }
}
