package com.footballmanager.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.SupervisorAccount
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.footballmanager.app.ui.theme.FmAccentBlue
import com.footballmanager.app.ui.theme.FmBorder
import com.footballmanager.app.ui.theme.FmDarkBg
import com.footballmanager.app.ui.theme.FmSurface
import com.footballmanager.app.ui.theme.FmSurfaceHover
import com.footballmanager.app.ui.theme.FmSurfaceSelected
import com.footballmanager.app.ui.theme.FmTextMuted
import com.footballmanager.app.ui.theme.FmTextPrimary
import com.footballmanager.app.ui.theme.FmTextSecondary

enum class FmNavSection(
    val title: String,
    val icon: ImageVector,
    val group: String,
) {
    HOME("Home", Icons.Outlined.Home, "GENERAL"),
    INBOX("Inbox", Icons.Outlined.Mail, "GENERAL"),
    SQUAD("Squad", Icons.Outlined.Group, "TEAM"),
    DYNAMICS("Dynamics", Icons.Outlined.Hub, "TEAM"),
    TACTICS("Tactics", Icons.Outlined.SportsSoccer, "TEAM"),
    DATA_HUB("Data Hub", Icons.Outlined.Analytics, "TEAM"),
    STAFF("Staff", Icons.Outlined.SupervisorAccount, "OPERATIONS"),
    TRAINING("Training", Icons.Outlined.FitnessCenter, "OPERATIONS"),
    MEDICAL("Medical Centre", Icons.Outlined.LocalHospital, "OPERATIONS"),
    SCHEDULE("Schedule", Icons.Outlined.CalendarMonth, "OPERATIONS"),
    COMPETITIONS("Competitions", Icons.Outlined.EmojiEvents, "RECRUITMENT"),
    SCOUTING("Scouting", Icons.Outlined.Search, "RECRUITMENT"),
    TRANSFERS("Transfers", Icons.Outlined.SwapHoriz, "RECRUITMENT"),
    CLUB_INFO("Club Info", Icons.Outlined.Shield, "CLUB"),
    FINANCES("Finances", Icons.Outlined.AccountBalance, "CLUB"),
}

@Composable
fun FmNavigationDrawerContent(
    currentSection: FmNavSection,
    onSectionSelected: (FmNavSection) -> Unit,
    onMainMenuClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .width(260.dp)
            .fillMaxHeight()
            .background(FmDarkBg)
            .border(width = 1.dp, color = FmBorder)
            .verticalScroll(scrollState),
    ) {
        // Drawer Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(FmSurface)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Text(
                text = "FOOTBALL MANAGER",
                style = MaterialTheme.typography.labelSmall,
                color = FmAccentBlue,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Black,
            )
        }

        HorizontalDivider(color = FmBorder, thickness = 1.dp)

        val groups = listOf("GENERAL", "TEAM", "OPERATIONS", "RECRUITMENT", "CLUB")

        groups.forEach { groupName ->
            Text(
                text = groupName,
                style = MaterialTheme.typography.labelSmall,
                color = FmTextMuted,
                fontSize = 10.sp,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp),
            )

            val itemsInGroup = FmNavSection.entries.filter { it.group == groupName }
            itemsInGroup.forEach { section ->
                val isSelected = section == currentSection
                FmNavItemRow(
                    section = section,
                    isSelected = isSelected,
                    onClick = { onSectionSelected(section) },
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = FmBorder, thickness = 1.dp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clickable { onMainMenuClick() }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(Icons.Outlined.Home, contentDescription = null, tint = FmTextMuted, modifier = Modifier.size(18.dp))
            Text("Main Menu", style = MaterialTheme.typography.bodySmall, color = FmTextMuted, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun FmNavigationRail(
    currentSection: FmNavSection,
    onSectionSelected: (FmNavSection) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .width(64.dp)
            .fillMaxHeight()
            .background(FmDarkBg)
            .border(width = 1.dp, color = FmBorder)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        FmNavSection.entries.forEach { section ->
            val isSelected = section == currentSection
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(if (isSelected) FmSurfaceSelected else Color.Transparent)
                    .clickable { onSectionSelected(section) },
                contentAlignment = Alignment.Center,
            ) {
                // Left 3dp indicator bar
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .width(3.dp)
                            .fillMaxHeight()
                            .background(FmAccentBlue),
                    )
                }

                Icon(
                    imageVector = section.icon,
                    contentDescription = section.title,
                    tint = if (isSelected) FmTextPrimary else FmTextSecondary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun FmNavItemRow(
    section: FmNavSection,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp)
            .background(if (isSelected) FmSurfaceSelected else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Active indicator line on left
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(if (isSelected) FmAccentBlue else Color.Transparent),
        )

        Spacer(modifier = Modifier.width(13.dp))

        Icon(
            imageVector = section.icon,
            contentDescription = null,
            tint = if (isSelected) FmTextPrimary else FmTextSecondary,
            modifier = Modifier.size(18.dp),
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = section.title,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) FmTextPrimary else FmTextSecondary,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 12.sp,
        )
    }
}
