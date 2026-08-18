# Football Manager Android UI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a modern, responsive Jetpack Compose Android application (`:app` module) powered by the decoupled `:engine` simulation library, featuring an interactive manager dashboard, visual tactical pitch with tap-to-swap substitutions, league table standings, and live matchday simulation.

**Architecture:** Multi-module Gradle (`:engine` pure Kotlin JVM + `:app` Android Application). Single-activity architecture with Navigation Compose, Material 3 design tokens (Dark Sports Stadium theme), strictly vector Material Icons (no emojis), and `GameViewModel` reactive state flow.

**Tech Stack:** Android Gradle Plugin 8.7.3, Kotlin 2.4.10, Jetpack Compose (BOM 2024.10.01), Material 3, Navigation Compose 2.8.3, Kotlinx Serialization JSON 1.11.0, JUnit 5.

## Global Constraints
- Decoupled architecture: `:engine` remains pure Kotlin JVM (zero Android dependencies).
- Android `:app` targets compileSdk 35, minSdk 26.
- Strictly NO emojis: use vector icons from `androidx.compose.material.icons` and SVG vector badges.
- All numbers, ratings, and standings use tabular/monospace figures for precise alignment.
- Validation via GitHub Actions CI (`gradle build`).

---

### Task 1: Android Gradle Scaffolding & Multi-Module Configuration

**Files:**
- Modify: `settings.gradle.kts`
- Modify: `build.gradle.kts`
- Create: `app/build.gradle.kts`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/res/values/strings.xml`
- Create: `app/src/main/res/values/themes.xml`

**Interfaces:**
- Consumes: `:engine` module
- Produces: Runnable `:app` module compiling on Android SDK 35

- [ ] **Step 1: Update `settings.gradle.kts` and root `build.gradle.kts`**

In `settings.gradle.kts`:
```kotlin
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "football-manager"

include(":engine")
include(":app")
```

In `build.gradle.kts`:
```kotlin
plugins {
    id("com.android.application") version "8.7.3" apply false
    kotlin("jvm") version "2.4.10" apply false
    kotlin("android") version "2.4.10" apply false
    kotlin("plugin.compose") version "2.4.10" apply false
    kotlin("plugin.serialization") version "2.4.10" apply false
}
```

- [ ] **Step 2: Create `app/build.gradle.kts` and Android resources**

Create `app/build.gradle.kts`:
```kotlin
plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.compose")
    kotlin("plugin.serialization")
}

android {
    namespace = "com.footballmanager.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.footballmanager.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":engine"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")

    val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.navigation:navigation-compose:2.8.3")

    testImplementation(kotlin("test-junit5"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}
```

Create `app/src/main/AndroidManifest.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:allowBackup="true"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.FootballManager">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.FootballManager">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

Create `app/src/main/res/values/strings.xml`:
```xml
<resources>
    <string name="app_name">Football Manager</string>
</resources>
```

Create `app/src/main/res/values/themes.xml`:
```xml
<resources>
    <style name="Theme.FootballManager" parent="android:Theme.Material.NoActionBar">
        <item name="android:statusBarColor">#0F172A</item>
        <item name="android:navigationBarColor">#0F172A</item>
    </style>
</resources>
```

- [ ] **Step 3: Commit Task 1 scaffolding**

```bash
git add settings.gradle.kts build.gradle.kts app/
git commit -m "chore: scaffold Android app module with Compose and Material 3"
```

---

### Task 2: Design System Tokens, Theme & Shared Components

**Files:**
- Create: `app/src/main/kotlin/com/footballmanager/app/ui/theme/Color.kt`
- Create: `app/src/main/kotlin/com/footballmanager/app/ui/theme/Type.kt`
- Create: `app/src/main/kotlin/com/footballmanager/app/ui/theme/Theme.kt`
- Create: `app/src/main/kotlin/com/footballmanager/app/ui/components/FormBadge.kt`
- Create: `app/src/main/kotlin/com/footballmanager/app/ui/components/StatGauge.kt`

**Interfaces:**
- Produces:
  - `FootballManagerTheme(content: @Composable () -> Unit)`
  - `FormBadge(result: FormResult, modifier: Modifier = Modifier)`
  - `StatGauge(label: String, value: Int, maxValue: Int = 100, color: Color)`

- [ ] **Step 1: Implement Theme Tokens (`Color.kt`, `Type.kt`, `Theme.kt`)**

In `app/src/main/kotlin/com/footballmanager/app/ui/theme/Color.kt`:
```kotlin
package com.footballmanager.app.ui.theme

import androidx.compose.ui.graphics.Color

val StadiumEmerald = Color(0xFF10B981)
val ElectricBlue = Color(0xFF3B82F6)
val DeepNavy = Color(0xFF0F172A)
val SurfaceSlate = Color(0xFF1E293B)
val BorderSlate = Color(0xFF334155)
val TextLight = Color(0xFFF8FAFC)
val TextMuted = Color(0xFF94A3B8)

val StatusGreen = Color(0xFF22C55E)
val StatusAmber = Color(0xFFEAB308)
val StatusCoral = Color(0xFFEF4444)
val TurfStripeDark = Color(0xFF14532D)
val TurfStripeLight = Color(0xFF166534)
```

In `app/src/main/kotlin/com/footballmanager/app/ui/theme/Theme.kt`:
```kotlin
package com.footballmanager.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = StadiumEmerald,
    onPrimary = DeepNavy,
    secondary = ElectricBlue,
    onSecondary = TextLight,
    background = DeepNavy,
    onBackground = TextLight,
    surface = SurfaceSlate,
    onSurface = TextLight,
    surfaceVariant = BorderSlate,
    onSurfaceVariant = TextMuted,
)

@Composable
fun FootballManagerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content,
    )
}
```

In `app/src/main/kotlin/com/footballmanager/app/ui/theme/Type.kt`:
```kotlin
package com.footballmanager.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp, color = TextLight),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp, color = TextLight),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = TextLight),
    titleMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp, color = TextLight),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 15.sp, color = TextLight),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 13.sp, color = TextMuted),
    labelSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextLight),
)
```

- [ ] **Step 2: Implement Shared UI Components (`FormBadge.kt`, `StatGauge.kt`)**

In `app/src/main/kotlin/com/footballmanager/app/ui/components/FormBadge.kt`:
```kotlin
package com.footballmanager.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.footballmanager.app.ui.theme.StatusCoral
import com.footballmanager.app.ui.theme.StatusGreen
import com.footballmanager.app.ui.theme.SurfaceSlate

enum class MatchFormResult { WIN, DRAW, LOSS }

@Composable
fun FormBadge(result: MatchFormResult, modifier: Modifier = Modifier) {
    val (bgColor, label) = when (result) {
        MatchFormResult.WIN -> StatusGreen to "W"
        MatchFormResult.DRAW -> SurfaceSlate to "D"
        MatchFormResult.LOSS -> StatusCoral to "L"
    }
    Box(
        modifier = modifier
            .size(24.dp)
            .background(bgColor, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.White)
    }
}
```

In `app/src/main/kotlin/com/footballmanager/app/ui/components/StatGauge.kt`:
```kotlin
package com.footballmanager.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.footballmanager.app.ui.theme.BorderSlate
import com.footballmanager.app.ui.theme.StadiumEmerald

@Composable
fun StatGauge(
    label: String,
    value: Int,
    maxValue: Int = 100,
    color: Color = StadiumEmerald,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Text(text = "$value", style = MaterialTheme.typography.titleMedium, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (value.toFloat() / maxValue).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = color,
            trackColor = BorderSlate,
        )
    }
}
```

- [ ] **Step 3: Commit Task 2 tokens and components**

```bash
git add app/src/main/kotlin/com/footballmanager/app/ui/theme/ \
        app/src/main/kotlin/com/footballmanager/app/ui/components/
git commit -m "feat: implement Material 3 design tokens and shared UI components"
```

---

### Task 3: Interactive Tactical Pitch Canvas & Player Node

**Files:**
- Create: `app/src/main/kotlin/com/footballmanager/app/ui/components/PlayerCardNode.kt`
- Create: `app/src/main/kotlin/com/footballmanager/app/ui/components/PitchCanvas.kt`

**Interfaces:**
- Consumes: `Player`, `Position`, `Formation`
- Produces:
  - `PlayerCardNode(player: Player, slot: Position, isSelected: Boolean, onClick: () -> Unit)`
  - `PitchCanvas(starters: List<Player>, formation: Formation, selectedPlayerId: Long?, onPlayerClick: (Long) -> Unit)`

- [ ] **Step 1: Implement `PlayerCardNode.kt` and `PitchCanvas.kt`**

In `app/src/main/kotlin/com/footballmanager/app/ui/components/PlayerCardNode.kt`:
```kotlin
package com.footballmanager.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.footballmanager.app.ui.theme.BorderSlate
import com.footballmanager.app.ui.theme.ElectricBlue
import com.footballmanager.app.ui.theme.StadiumEmerald
import com.footballmanager.app.ui.theme.StatusAmber
import com.footballmanager.app.ui.theme.StatusCoral
import com.footballmanager.app.ui.theme.StatusGreen
import com.footballmanager.app.ui.theme.SurfaceSlate
import com.footballmanager.model.Player
import com.footballmanager.model.Position

@Composable
fun PlayerCardNode(
    player: Player,
    slot: Position,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fitnessColor = when {
        player.fitness >= 85 -> StatusGreen
        player.fitness >= 60 -> StatusAmber
        else -> StatusCoral
    }

    val borderColor = if (isSelected) StadiumEmerald else BorderSlate

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(72.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceSlate.copy(alpha = 0.92f))
            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = slot.name,
                style = MaterialTheme.typography.labelSmall,
                color = ElectricBlue,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${player.effectiveOverall(slot)}",
                style = MaterialTheme.typography.labelSmall,
                color = StadiumEmerald,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            text = player.name.split(" ").lastOrNull() ?: player.name,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = Color.White,
        )
        Spacer(modifier = Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { player.fitness / 100f },
            modifier = Modifier.width(52.dp).height(3.dp),
            color = fitnessColor,
            trackColor = BorderSlate,
        )
    }
}
```

In `app/src/main/kotlin/com/footballmanager/app/ui/components/PitchCanvas.kt`:
```kotlin
package com.footballmanager.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.footballmanager.app.ui.theme.TurfStripeDark
import com.footballmanager.app.ui.theme.TurfStripeLight
import com.footballmanager.model.Player
import com.footballmanager.model.Position
import com.footballmanager.model.PositionGroup
import com.footballmanager.simulation.Formation

@Composable
fun PitchCanvas(
    starters: List<Player>,
    formation: Formation,
    selectedPlayerId: Long?,
    onPlayerClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(340.dp)
            .clip(RoundedCornerShape(12.dp)),
    ) {
        val widthPx = maxWidth
        val heightPx = maxHeight

        // 1. Draw Turf Grass & Markings
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stripeCount = 6
            val stripeHeight = size.height / stripeCount
            for (i in 0 until stripeCount) {
                drawRect(
                    color = if (i % 2 == 0) TurfStripeDark else TurfStripeLight,
                    topLeft = Offset(0f, i * stripeHeight),
                    size = Size(size.width, stripeHeight),
                )
            }

            val lineColor = Color.White.copy(alpha = 0.35f)
            val stroke = Stroke(width = 2.dp.toPx())

            // Halfway line & center circle
            drawLine(lineColor, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), strokeWidth = 2.dp.toPx())
            drawCircle(lineColor, radius = 35.dp.toPx(), center = Offset(size.width / 2, size.height / 2), style = stroke)

            // Top Penalty Box
            drawRect(lineColor, Offset(size.width * 0.25f, 0f), Size(size.width * 0.5f, size.height * 0.18f), style = stroke)
            // Bottom Penalty Box
            drawRect(lineColor, Offset(size.width * 0.25f, size.height * 0.82f), Size(size.width * 0.5f, size.height * 0.18f), style = stroke)
        }

        // 2. Position 11 Starters According to Formation Slots
        val slots = formation.slots
        val startersWithSlots = starters.take(11).zip(slots)

        // Coordinates mapping (X in 0..1, Y in 0..1 where GK is Y ~ 0.85, FW is Y ~ 0.12)
        val gk = startersWithSlots.filter { it.second == Position.GK }
        val defenders = startersWithSlots.filter { it.second.group == PositionGroup.DEFENDER }
        val midfielders = startersWithSlots.filter { it.second.group == PositionGroup.MIDFIELDER }
        val attackers = startersWithSlots.filter { it.second.group == PositionGroup.ATTACKER }

        fun renderRow(items: List<Pair<Player, Position>>, yFactor: Float) {
            val count = items.size
            items.forEachIndexed { index, (player, slot) ->
                val xFraction = (index + 1f) / (count + 1f)
                val xOffset = (widthPx * xFraction) - 36.dp
                val yOffset = (heightPx * yFactor) - 20.dp

                Box(modifier = Modifier.offset(x = xOffset, y = yOffset)) {
                    PlayerCardNode(
                        player = player,
                        slot = slot,
                        isSelected = player.id == selectedPlayerId,
                        onClick = { onPlayerClick(player.id) },
                    )
                }
            }
        }

        renderRow(gk, 0.86f)
        renderRow(defenders, 0.64f)
        renderRow(midfielders, 0.38f)
        renderRow(attackers, 0.12f)
    }
}
```

- [ ] **Step 2: Commit Task 3 pitch components**

```bash
git add app/src/main/kotlin/com/footballmanager/app/ui/components/
git commit -m "feat: implement tactical pitch visualizer and player card node"
```

---

### Task 4: State Management & GameViewModel

**Files:**
- Create: `app/src/main/kotlin/com/footballmanager/app/ui/viewmodel/GameUiState.kt`
- Create: `app/src/main/kotlin/com/footballmanager/app/ui/viewmodel/GameViewModel.kt`
- Create: `app/src/test/kotlin/com/footballmanager/app/GameViewModelTest.kt`

**Interfaces:**
- Consumes: `:engine` domain (`Game`, `SeasonRunner`, `Lineup`, `Tactics`, `SeedData`)
- Produces: `GameViewModel` exposing `StateFlow<GameUiState>` with tactics mutation, substitution, simulation, and auto-save.

- [ ] **Step 1: Implement `GameUiState.kt` and `GameViewModel.kt`**

In `app/src/main/kotlin/com/footballmanager/app/ui/viewmodel/GameUiState.kt`:
```kotlin
package com.footballmanager.app.ui.viewmodel

import com.footballmanager.model.Club
import com.footballmanager.model.Game
import com.footballmanager.model.Player
import com.footballmanager.simulation.Lineup
import com.footballmanager.simulation.MatchResult
import com.footballmanager.simulation.Team
import com.footballmanager.simulation.season.SeasonState

data class GameUiState(
    val game: Game,
    val currentSeason: SeasonState,
    val humanClubId: Long = 1L,
    val selectedStarterPlayerId: Long? = null,
    val isSimulating: Boolean = false,
    val currentSimTick: Int = 0,
    val lastMatchResult: MatchResult? = null,
) {
    val humanClub: Club get() = game.club(humanClubId)
    val humanSquad: List<Player>
        get() = currentSeason.clubs[humanClubId]?.squad?.playerIds?.mapNotNull { currentSeason.players[it] }
            ?: game.squad(humanClubId)
    val humanTeam: Team
        get() = currentSeason.teams.firstOrNull { it.clubId == humanClubId }
            ?: Team.fromSquad(humanClubId, humanSquad)
    val humanLineup: Lineup
        get() = currentSeason.lineups[humanClubId]
            ?: Lineup.autoSelect(humanSquad, humanTeam.tactics)
    val starters: List<Player>
        get() = humanLineup.starters.mapNotNull { currentSeason.players[it] }
    val substitutes: List<Player>
        get() = humanLineup.substitutes.mapNotNull { currentSeason.players[it] }
}
```

In `app/src/main/kotlin/com/footballmanager/app/ui/viewmodel/GameViewModel.kt`:
```kotlin
package com.footballmanager.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.footballmanager.model.Game
import com.footballmanager.model.League
import com.footballmanager.seed.SeedData
import com.footballmanager.simulation.Formation
import com.footballmanager.simulation.Lineup
import com.footballmanager.simulation.Mentality
import com.footballmanager.simulation.Tactics
import com.footballmanager.simulation.season.SeasonRunner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameViewModel(
    initialGame: Game = SeedData.game(),
    private val runner: SeasonRunner = SeasonRunner(),
) : ViewModel() {

    private val _uiState: MutableStateFlow<GameUiState>

    init {
        val league = initialGame.competitions.getValue(SeedData.LEAGUE_ID) as League
        val teams = SeedData.teams(initialGame)
        val season = initialGame.currentSeason ?: runner.start(
            league = league,
            teams = teams,
            startDate = initialGame.currentDate,
            humanClubId = 1L,
            clubs = initialGame.clubs,
            players = initialGame.players,
        )
        _uiState = MutableStateFlow(
            GameUiState(game = initialGame, currentSeason = season, humanClubId = 1L),
        )
    }

    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    fun updateFormation(formation: Formation) {
        _uiState.update { state ->
            val newTactics = state.humanTeam.tactics.copy(formation = formation)
            val updatedSeason = state.currentSeason.setTactics(state.humanClubId, newTactics)
            val newLineup = Lineup.autoSelect(state.humanSquad, newTactics)
            val finalSeason = updatedSeason.setLineup(state.humanClubId, newLineup)
            state.copy(currentSeason = finalSeason, selectedStarterPlayerId = null)
        }
    }

    fun updateMentality(mentality: Mentality) {
        _uiState.update { state ->
            val newTactics = state.humanTeam.tactics.copy(mentality = mentality)
            val updatedSeason = state.currentSeason.setTactics(state.humanClubId, newTactics)
            state.copy(currentSeason = updatedSeason)
        }
    }

    fun autoSelectBestXI() {
        _uiState.update { state ->
            val newLineup = Lineup.autoSelect(state.humanSquad, state.humanTeam.tactics)
            val updatedSeason = state.currentSeason.setLineup(state.humanClubId, newLineup)
            state.copy(currentSeason = updatedSeason, selectedStarterPlayerId = null)
        }
    }

    fun onStarterSelected(playerId: Long) {
        _uiState.update { state ->
            val newSelection = if (state.selectedStarterPlayerId == playerId) null else playerId
            state.copy(selectedStarterPlayerId = newSelection)
        }
    }

    fun swapWithBench(benchPlayerId: Long) {
        _uiState.update { state ->
            val starterId = state.selectedStarterPlayerId ?: return@update state
            val currentLineup = state.humanLineup
            val newStarters = currentLineup.starters.map { if (it == starterId) benchPlayerId else it }
            val newSubs = currentLineup.substitutes.map { if (it == benchPlayerId) starterId else it }
            val newLineup = Lineup(starters = newStarters, substitutes = newSubs)
            val updatedSeason = state.currentSeason.setLineup(state.humanClubId, newLineup)
            state.copy(currentSeason = updatedSeason, selectedStarterPlayerId = null)
        }
    }

    fun playNextMatchday() {
        _uiState.update { state ->
            if (state.currentSeason.isFinished) return@update state
            val nextSeason = runner.playNextMatchday(state.currentSeason)
            val userMatchResult = nextSeason.results.lastOrNull {
                it.homeClubId == state.humanClubId || it.awayClubId == state.humanClubId
            }
            val updatedGame = state.game.copy(
                players = nextSeason.players,
                currentSeason = nextSeason,
                currentDate = nextSeason.currentDate,
            )
            state.copy(
                game = updatedGame,
                currentSeason = nextSeason,
                lastMatchResult = userMatchResult,
                selectedStarterPlayerId = null,
            )
        }
    }
}
```

- [ ] **Step 2: Write `GameViewModelTest.kt`**

Create `app/src/test/kotlin/com/footballmanager/app/GameViewModelTest.kt`:
```kotlin
package com.footballmanager.app

import com.footballmanager.app.ui.viewmodel.GameViewModel
import com.footballmanager.simulation.Formation
import com.footballmanager.simulation.Mentality
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class GameViewModelTest {

    @Test
    fun `viewModel initializes with human club and lineup`() {
        val vm = GameViewModel()
        val state = vm.uiState.value
        assertEquals(1L, state.humanClubId)
        assertEquals(11, state.starters.size)
        assertTrue(state.substitutes.isNotEmpty())
    }

    @Test
    fun `updating formation changes tactical slots and auto-aligns starters`() {
        val vm = GameViewModel()
        vm.updateFormation(Formation.FIVE_THREE_TWO)
        assertEquals(Formation.FIVE_THREE_TWO, vm.uiState.value.humanTeam.tactics.formation)
        assertEquals(11, vm.uiState.value.starters.size)
    }

    @Test
    fun `swapWithBench swaps starter and substitute correctly`() {
        val vm = GameViewModel()
        val initialStarterId = vm.uiState.value.starters.first().id
        val initialBenchId = vm.uiState.value.substitutes.first().id

        vm.onStarterSelected(initialStarterId)
        vm.swapWithBench(initialBenchId)

        val updatedStarters = vm.uiState.value.starters.map { it.id }
        val updatedSubs = vm.uiState.value.substitutes.map { it.id }

        assertTrue(initialBenchId in updatedStarters)
        assertTrue(initialStarterId in updatedSubs)
    }

    @Test
    fun `playNextMatchday advances season and depletes starter condition`() {
        val vm = GameViewModel()
        val starterId = vm.uiState.value.starters.first().id
        val initialFitness = vm.uiState.value.starters.first().fitness

        vm.playNextMatchday()

        val updatedStarter = vm.uiState.value.currentSeason.players.getValue(starterId)
        assertEquals(initialFitness - 12, updatedStarter.fitness)
    }
}
```

- [ ] **Step 3: Commit Task 4 ViewModel and tests**

```bash
git add app/src/main/kotlin/com/footballmanager/app/ui/viewmodel/ \
        app/src/test/kotlin/com/footballmanager/app/GameViewModelTest.kt
git commit -m "feat: implement GameViewModel and state management with unit tests"
```

---

### Task 5: Screens, Navigation & MainActivity

**Files:**
- Create: `app/src/main/kotlin/com/footballmanager/app/ui/navigation/AppNavigation.kt`
- Create: `app/src/main/kotlin/com/footballmanager/app/ui/screens/HomeScreen.kt`
- Create: `app/src/main/kotlin/com/footballmanager/app/ui/screens/TacticsScreen.kt`
- Create: `app/src/main/kotlin/com/footballmanager/app/ui/screens/StandingsScreen.kt`
- Create: `app/src/main/kotlin/com/footballmanager/app/ui/screens/MatchdayScreen.kt`
- Create: `app/src/main/kotlin/com/footballmanager/app/MainActivity.kt`

**Interfaces:**
- Consumes: `GameViewModel`, `FootballManagerTheme`, Material 3 vector icons
- Produces: Complete 4-tab interactive Android UI

- [ ] **Step 1: Implement Navigation & Screens (`AppNavigation.kt`, `HomeScreen.kt`, etc.)**

In `app/src/main/kotlin/com/footballmanager/app/ui/navigation/AppNavigation.kt`:
```kotlin
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
```

In `app/src/main/kotlin/com/footballmanager/app/ui/screens/HomeScreen.kt`:
```kotlin
package com.footballmanager.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.footballmanager.app.ui.components.StatGauge
import com.footballmanager.app.ui.theme.BorderSlate
import com.footballmanager.app.ui.theme.ElectricBlue
import com.footballmanager.app.ui.theme.StadiumEmerald
import com.footballmanager.app.ui.theme.SurfaceSlate
import com.footballmanager.app.ui.viewmodel.GameUiState

@Composable
fun HomeScreen(
    state: GameUiState,
    onNavigateToMatchday: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val club = state.humanClub
    val nextMatchday = state.currentSeason.nextMatchday
    val nextFixture = state.currentSeason.fixtures.getOrNull(state.currentSeason.nextFixtureIndex)

    val avgFitness = if (state.humanSquad.isNotEmpty()) state.humanSquad.map { it.fitness }.average().toInt() else 100
    val avgMorale = if (state.humanSquad.isNotEmpty()) state.humanSquad.map { it.morale }.average().toInt() else 50

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = club.name, style = MaterialTheme.typography.headlineMedium, color = Color.White)
                Text(text = "Manager • Liga Nusantara", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Date: ${state.currentSeason.currentDate}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = StadiumEmerald,
                    )
                    Text(text = "•", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "Round ${nextMatchday ?: 18}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ElectricBlue,
                    )
                }
            }
        }

        // Next Match Hero
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "NEXT MATCH", style = MaterialTheme.typography.labelSmall, color = ElectricBlue)
                Spacer(modifier = Modifier.height(8.dp))
                if (nextFixture != null && !state.currentSeason.isFinished) {
                    val opponentId = if (nextFixture.home.clubId == club.id) nextFixture.away.clubId else nextFixture.home.clubId
                    val isHome = nextFixture.home.clubId == club.id
                    val opponent = state.game.club(opponentId)

                    Text(
                        text = if (isHome) "vs ${opponent.name} (H)" else "@ ${opponent.name} (A)",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onNavigateToMatchday,
                        colors = ButtonDefaults.buttonColors(containerColor = StadiumEmerald),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Outlined.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Play Matchday $nextMatchday", color = Color.Black)
                    }
                } else {
                    Text(text = "Season Finished", style = MaterialTheme.typography.titleMedium, color = StadiumEmerald)
                }
            }
        }

        // Squad Health Summary
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "SQUAD CONDITION", style = MaterialTheme.typography.labelSmall, color = ElectricBlue)
                Spacer(modifier = Modifier.height(8.dp))
                StatGauge(label = "Average Fitness", value = avgFitness, color = StadiumEmerald)
                StatGauge(label = "Average Morale", value = avgMorale, color = ElectricBlue)
            }
        }
    }
}
```

In `app/src/main/kotlin/com/footballmanager/app/ui/screens/TacticsScreen.kt`:
```kotlin
package com.footballmanager.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.footballmanager.app.ui.components.PitchCanvas
import com.footballmanager.app.ui.theme.BorderSlate
import com.footballmanager.app.ui.theme.ElectricBlue
import com.footballmanager.app.ui.theme.StadiumEmerald
import com.footballmanager.app.ui.theme.SurfaceSlate
import com.footballmanager.app.ui.viewmodel.GameUiState
import com.footballmanager.simulation.Formation
import com.footballmanager.simulation.Mentality

@Composable
fun TacticsScreen(
    state: GameUiState,
    onFormationSelected: (Formation) -> Unit,
    onMentalitySelected: (Mentality) -> Unit,
    onAutoSelect: () -> Unit,
    onStarterClick: (Long) -> Unit,
    onBenchClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Formation & Mentality Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Formation.entries.forEach { f ->
                    FilterChip(
                        selected = state.humanTeam.tactics.formation == f,
                        onClick = { onFormationSelected(f) },
                        label = { Text(f.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StadiumEmerald,
                            selectedLabelColor = Color.Black,
                        ),
                    )
                }
            }
            Button(
                onClick = onAutoSelect,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                shape = RoundedCornerShape(8.dp),
            ) {
                Icon(Icons.Outlined.AutoFixHigh, contentDescription = null)
            }
        }

        // Pitch Visualizer
        PitchCanvas(
            starters = state.starters,
            formation = state.humanTeam.tactics.formation,
            selectedPlayerId = state.selectedStarterPlayerId,
            onPlayerClick = onStarterClick,
        )

        // Bench Substitutes
        Text(
            text = if (state.selectedStarterPlayerId != null) "Tap a substitute to swap" else "Substitutes",
            style = MaterialTheme.typography.titleMedium,
            color = if (state.selectedStarterPlayerId != null) StadiumEmerald else Color.White,
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(state.substitutes) { player ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onBenchClick(player.id) },
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(text = player.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "${player.bestPosition()} • Fit: ${player.fitness}% • Mor: ${player.morale}",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        Text(
                            text = "${player.bestOverall()}",
                            style = MaterialTheme.typography.titleLarge,
                            color = StadiumEmerald,
                        )
                    }
                }
            }
        }
    }
}
```

In `app/src/main/kotlin/com/footballmanager/app/ui/screens/StandingsScreen.kt`:
```kotlin
package com.footballmanager.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.footballmanager.app.ui.theme.StadiumEmerald
import com.footballmanager.app.ui.theme.SurfaceSlate
import com.footballmanager.app.ui.viewmodel.GameUiState

@Composable
fun StandingsScreen(state: GameUiState, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(12.dp)) {
        Text(
            text = "LEAGUE TABLE",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("#", modifier = Modifier.width(24.dp), style = MaterialTheme.typography.labelSmall)
            Text("Club", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall)
            Text("P", modifier = Modifier.width(28.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
            Text("GD", modifier = Modifier.width(32.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
            Text("Pts", modifier = Modifier.width(36.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall)
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            itemsIndexed(state.currentSeason.standings.entries) { index, entry ->
                val club = state.game.club(entry.team.clubId)
                val isHuman = entry.team.clubId == state.humanClubId

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isHuman) StadiumEmerald.copy(alpha = 0.2f) else SurfaceSlate,
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("${index + 1}", modifier = Modifier.width(24.dp), color = if (isHuman) StadiumEmerald else Color.White)
                        Text(club.name, modifier = Modifier.weight(1f), color = if (isHuman) StadiumEmerald else Color.White)
                        Text("${entry.played}", modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
                        Text("${entry.goalDifference}", modifier = Modifier.width(32.dp), textAlign = TextAlign.Center)
                        Text("${entry.points}", modifier = Modifier.width(36.dp), textAlign = TextAlign.End, color = StadiumEmerald)
                    }
                }
            }
        }
    }
}
```

In `app/src/main/kotlin/com/footballmanager/app/ui/screens/MatchdayScreen.kt`:
```kotlin
package com.footballmanager.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.footballmanager.app.ui.theme.ElectricBlue
import com.footballmanager.app.ui.theme.StadiumEmerald
import com.footballmanager.app.ui.theme.StatusCoral
import com.footballmanager.app.ui.theme.SurfaceSlate
import com.footballmanager.app.ui.viewmodel.GameUiState
import com.footballmanager.simulation.MatchEventType

@Composable
fun MatchdayScreen(
    state: GameUiState,
    onSimulateMatchday: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val result = state.lastMatchResult
    val nextMatchday = state.currentSeason.nextMatchday

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = "MATCHDAY SIMULATION", style = MaterialTheme.typography.headlineMedium)

        if (!state.currentSeason.isFinished) {
            Button(
                onClick = onSimulateMatchday,
                colors = ButtonDefaults.buttonColors(containerColor = StadiumEmerald),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Outlined.PlayArrow, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Simulate Round $nextMatchday", color = Color.Black)
            }
        }

        if (result != null) {
            val homeClub = state.game.club(result.homeClubId)
            val awayClub = state.game.club(result.awayClubId)

            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(text = "FULL TIME", style = MaterialTheme.typography.labelSmall, color = ElectricBlue)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(homeClub.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Text(
                            "${result.homeScore} - ${result.awayScore}",
                            style = MaterialTheme.typography.headlineLarge,
                            color = StadiumEmerald,
                        )
                        Text(awayClub.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    }
                }
            }

            Text(text = "Match Incidents", style = MaterialTheme.typography.titleMedium)
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(result.events) { event ->
                    val (icon, iconColor) = when (event.type) {
                        MatchEventType.GOAL -> Icons.Outlined.SportsSoccer to StadiumEmerald
                        MatchEventType.SHOT_SAVED -> Icons.Outlined.Shield to ElectricBlue
                        MatchEventType.SHOT_MISSED -> Icons.Outlined.Close to StatusCoral
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("${event.minute}'", style = MaterialTheme.typography.labelSmall, color = ElectricBlue)
                        Icon(icon, contentDescription = null, tint = iconColor)
                        Text("${event.side}: ${event.type.name.replace('_', ' ')}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
```

In `app/src/main/kotlin/com/footballmanager/app/MainActivity.kt`:
```kotlin
package com.footballmanager.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.footballmanager.app.ui.navigation.NavigationTab
import com.footballmanager.app.ui.screens.HomeScreen
import com.footballmanager.app.ui.screens.MatchdayScreen
import com.footballmanager.app.ui.screens.StandingsScreen
import com.footballmanager.app.ui.screens.TacticsScreen
import com.footballmanager.app.ui.theme.FootballManagerTheme
import com.footballmanager.app.ui.theme.StadiumEmerald
import com.footballmanager.app.ui.theme.SurfaceSlate
import com.footballmanager.app.ui.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FootballManagerTheme {
                val state by viewModel.uiState.collectAsState()
                var currentTab by remember { mutableStateOf(NavigationTab.HOME) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(containerColor = SurfaceSlate) {
                            NavigationTab.entries.forEach { tab ->
                                NavigationBarItem(
                                    selected = currentTab == tab,
                                    onClick = { currentTab = tab },
                                    icon = { Icon(tab.icon, contentDescription = tab.label) },
                                    label = { Text(tab.label) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        selectedTextColor = StadiumEmerald,
                                        indicatorColor = StadiumEmerald,
                                    ),
                                )
                            }
                        }
                    },
                ) { padding ->
                    when (currentTab) {
                        NavigationTab.HOME -> HomeScreen(
                            state = state,
                            onNavigateToMatchday = { currentTab = NavigationTab.MATCHDAY },
                            modifier = Modifier.padding(padding),
                        )
                        NavigationTab.TACTICS -> TacticsScreen(
                            state = state,
                            onFormationSelected = viewModel::updateFormation,
                            onMentalitySelected = viewModel::updateMentality,
                            onAutoSelect = viewModel::autoSelectBestXI,
                            onStarterClick = viewModel::onStarterSelected,
                            onBenchClick = viewModel::swapWithBench,
                            modifier = Modifier.padding(padding),
                        )
                        NavigationTab.STANDINGS -> StandingsScreen(
                            state = state,
                            modifier = Modifier.padding(padding),
                        )
                        NavigationTab.MATCHDAY -> MatchdayScreen(
                            state = state,
                            onSimulateMatchday = viewModel::playNextMatchday,
                            modifier = Modifier.padding(padding),
                        )
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 2: Commit Task 5 screens and navigation**

```bash
git add app/src/main/kotlin/com/footballmanager/app/
git commit -m "feat: implement 4-tab manager UI suite with Material 3 vector icons"
```

---

### Task 6: CI Integration & Full Verification

**Files:**
- Modify: `.github/workflows/ci.yml`

**Interfaces:**
- Produces: GitHub Actions CI building `:engine` and `:app`, executing all test suites.

- [ ] **Step 1: Update `.github/workflows/ci.yml`**

In `.github/workflows/ci.yml`:
```yaml
      - name: Build and test
        run: gradle build
```
(`gradle build` builds both `:engine` and `:app` modules and executes all unit tests).

- [ ] **Step 2: Commit Task 6, push to GitHub, and verify CI**

```bash
git add .github/workflows/ci.yml
git commit -m "ci: verify Android app and engine in GitHub Actions workflow"
git push origin main
gh run watch
```
Check `gh run view` to verify full CI run passes green.
