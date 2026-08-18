package com.footballmanager.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.footballmanager.app.ui.theme.FmAccentBlue
import com.footballmanager.app.ui.theme.FmBorder
import com.footballmanager.app.ui.theme.FmSurface
import com.footballmanager.app.ui.theme.FmTextPrimary
import com.footballmanager.app.ui.theme.FmTextSecondary

enum class FmSquadTab(val label: String) {
    OVERVIEW("Overview"),
    REPORT("Report"),
    DYNAMICS("Dynamics"),
    STATS("Stats"),
    CONTRACTS("Contracts"),
}

@Composable
fun FmTabRow(
    tabs: List<FmSquadTab> = FmSquadTab.entries,
    selectedTab: FmSquadTab,
    onTabSelected: (FmSquadTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedIndex = tabs.indexOf(selectedTab).coerceAtLeast(0)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp)
            .background(FmSurface),
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedIndex,
            containerColor = FmSurface,
            contentColor = FmTextPrimary,
            edgePadding = 12.dp,
            divider = {
                HorizontalDivider(color = FmBorder, thickness = 1.dp)
            },
            indicator = { tabPositions ->
                if (selectedIndex < tabPositions.size) {
                    val currentTabPosition = tabPositions[selectedIndex]
                    // Custom 2px thin underline indicator (Strictly NO rounded pill)
                    Box(
                        modifier = Modifier
                            .tabIndicatorOffset(currentTabPosition)
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(FmAccentBlue),
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = tab == selectedTab
                Tab(
                    selected = isSelected,
                    onClick = { onTabSelected(tab) },
                    text = {
                        Text(
                            text = tab.label.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) FmTextPrimary else FmTextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp,
                        )
                    },
                    modifier = Modifier.height(36.dp),
                )
            }
        }
    }
}
