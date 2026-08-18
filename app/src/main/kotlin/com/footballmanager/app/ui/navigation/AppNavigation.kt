package com.footballmanager.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Sports
import androidx.compose.ui.graphics.vector.ImageVector

enum class NavigationTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Outlined.Dashboard),
    TACTICS("Tactics", Icons.Outlined.Sports),
    STANDINGS("Standings", Icons.Outlined.FormatListNumbered),
    MATCHDAY("Matchday", Icons.Outlined.PlayCircle),
}
