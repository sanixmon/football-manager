package com.footballmanager.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.footballmanager.app.ui.theme.FmDarkBg
import com.footballmanager.app.ui.theme.FmRatingHigh
import com.footballmanager.app.ui.theme.FmRatingMed
import com.footballmanager.app.ui.theme.FmSurface
import com.footballmanager.app.ui.theme.FmSurfaceAlt
import com.footballmanager.app.ui.theme.FmTextMuted
import com.footballmanager.app.ui.theme.FmTextPrimary
import com.footballmanager.app.ui.theme.FmTextSecondary
import com.footballmanager.app.ui.viewmodel.GameUiState

private data class InboxMessage(
    val icon: ImageVector,
    val iconColor: Color,
    val from: String,
    val subject: String,
    val preview: String,
    val time: String,
    val isRead: Boolean,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InboxScreen(
    state: GameUiState,
    modifier: Modifier = Modifier,
) {
    val club = state.humanClub
    val round = state.currentSeason.nextMatchday ?: 18
    val season = state.currentSeason

    // Generate contextual inbox messages from game state
    val messages = buildList {
        add(InboxMessage(
            icon = Icons.Outlined.Notifications,
            iconColor = FmAccentBlue,
            from = "Board of Directors",
            subject = "Season ${season.currentDate.year} Objectives",
            preview = "The board expects you to finish in the top half this season. Maintain squad morale above 60.",
            time = "${season.currentDate}",
            isRead = round > 1,
        ))
        if (round > 1) {
            add(InboxMessage(
                icon = Icons.Outlined.SportsSoccer,
                iconColor = FmRatingHigh,
                from = "Match Report",
                subject = "Round ${round - 1} Summary",
                preview = state.lastMatchResult?.let { r ->
                    val home = state.game.club(r.homeClubId).name
                    val away = state.game.club(r.awayClubId).name
                    "$home ${r.homeScore} – ${r.awayScore} $away"
                } ?: "Season is underway. Your squad is ready for action.",
                time = "${season.currentDate}",
                isRead = false,
            ))
        }
        add(InboxMessage(
            icon = Icons.Outlined.TrendingUp,
            iconColor = FmAccentCyan,
            from = "Assistant Manager",
            subject = "Squad Fitness Report",
            preview = "Average squad fitness is at ${if (season.players.isNotEmpty()) season.players.values.map { it.fitness }.average().toInt() else 85}%. " +
                "Recommend rotating squad in upcoming matches.",
            time = "${season.currentDate}",
            isRead = true,
        ))
        if (season.players.isNotEmpty()) {
            val lowMorale = season.players.values.filter { it.morale < 40 }
            if (lowMorale.isNotEmpty()) {
                add(InboxMessage(
                    icon = Icons.Outlined.Inbox,
                    iconColor = FmRatingMed,
                    from = "Team Psychologist",
                    subject = "${lowMorale.size} Players Have Low Morale",
                    preview = "${lowMorale.take(2).joinToString(", ") { it.name }} are unsatisfied. Consider a team meeting.",
                    time = "${season.currentDate}",
                    isRead = false,
                ))
            }
        }
        // Dynamic Transfer Notifications
        season.activeBids.forEach { bid ->
            val player = season.players[bid.playerId]
            val playerName = player?.name ?: "Player #${bid.playerId}"
            when (bid.status) {
                com.footballmanager.model.BidStatus.ACCEPTED_BY_PLAYER -> {
                    add(InboxMessage(
                        icon = Icons.Outlined.SportsSoccer,
                        iconColor = FmRatingHigh,
                        from = "Transfer Office",
                        subject = "Contract Agreed: $playerName",
                        preview = "$playerName has accepted your contract terms. Head to Transfers to finalize the deal.",
                        time = "${season.currentDate}",
                        isRead = false,
                    ))
                }
                com.footballmanager.model.BidStatus.ACCEPTED_BY_CLUB -> {
                    add(InboxMessage(
                        icon = Icons.Outlined.Notifications,
                        iconColor = FmAccentCyan,
                        from = "Transfer Office",
                        subject = "Bid Accepted for $playerName",
                        preview = "The selling club agreed to your offer of $${"%,d".format(bid.feeOffered)}. Offer contract terms to proceed.",
                        time = "${season.currentDate}",
                        isRead = false,
                    ))
                }
                com.footballmanager.model.BidStatus.REJECTED_BY_CLUB -> {
                    add(InboxMessage(
                        icon = Icons.Outlined.Notifications,
                        iconColor = Color(0xFFEF4444),
                        from = "Transfer Office",
                        subject = "Bid Rejected: $playerName",
                        preview = "The selling club turned down your transfer bid of $${"%,d".format(bid.feeOffered)}.",
                        time = "${season.currentDate}",
                        isRead = true,
                    ))
                }
                else -> {}
            }
        }

        season.transferHistory.takeLast(2).forEach { record ->
            add(InboxMessage(
                icon = Icons.Outlined.MarkEmailRead,
                iconColor = FmRatingHigh,
                from = "Transfer Office",
                subject = "Transfer Confirmed: ${record.playerName}",
                preview = "Deal completed for a fee of $${"%,d".format(record.fee)} on ${record.date}.",
                time = "${record.date}",
                isRead = true,
            ))
        }

        add(InboxMessage(
            icon = Icons.Outlined.MarkEmailRead,
            iconColor = FmTextSecondary,
            from = "Scouting Team",
            subject = "Transfer Window Update",
            preview = "We have identified potential transfer targets for your consideration. Tap Scouting to review.",
            time = "${season.currentDate.minusDays(3)}",
            isRead = true,
        ))
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(FmDarkBg),
    ) {
        stickyHeader {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .background(Color(0xFF181B20))
                    .border(1.dp, FmBorder)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("INBOX", style = MaterialTheme.typography.labelSmall, color = FmAccentBlue, fontSize = 10.sp, letterSpacing = 1.sp)
                Text("${messages.count { !it.isRead }} unread", style = MaterialTheme.typography.labelSmall, color = FmTextMuted, fontSize = 10.sp)
            }
        }

        itemsIndexed(messages) { index, msg ->
            val bg = if (index % 2 == 0) FmSurface else FmSurfaceAlt

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bg)
                    .clickable { /* could open detail */ }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Icon badge
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(msg.iconColor.copy(alpha = 0.15f))
                        .border(1.dp, msg.iconColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(msg.icon, contentDescription = null, tint = msg.iconColor, modifier = Modifier.size(18.dp))
                }

                // Message body
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            msg.from,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (!msg.isRead) FmTextPrimary else FmTextSecondary,
                            fontWeight = if (!msg.isRead) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp,
                        )
                        Text(msg.time, style = MaterialTheme.typography.bodySmall, color = FmTextMuted, fontSize = 9.sp)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        msg.subject,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (!msg.isRead) FmTextPrimary else FmTextSecondary,
                        fontWeight = if (!msg.isRead) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 12.sp,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        msg.preview,
                        style = MaterialTheme.typography.bodySmall,
                        color = FmTextMuted,
                        fontSize = 11.sp,
                        maxLines = 2,
                        lineHeight = 14.sp,
                    )
                }

                // Unread indicator
                if (!msg.isRead) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(FmAccentBlue),
                    )
                }
            }

            HorizontalDivider(color = FmBorder.copy(alpha = 0.4f), thickness = 0.5.dp)
        }
    }
}
