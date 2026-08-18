# Football Manager Android UI (Jetpack Compose) Design Spec

_Date: 2026-08-18 · Status: Approved_

## 1. Overview & Goals

The headless Kotlin JVM football manager engine is complete, deterministic, and test-covered. This specification defines the Android mobile user interface built with **Jetpack Compose**, **Material 3**, and a **clean multi-module architecture**.

Key capabilities:
1. **Interactive Manager Hub (Dashboard)**: Current club status, next match preview, season calendar progression, and squad health.
2. **Tactical Pitch & Squad Selection**: Full visual pitch canvas rendering the starting 11 by tactical formation slots (`4-4-2`, `4-3-3`, `5-3-2`), with live condition gauges and fluid tap-to-swap substitution.
3. **Live Matchday Simulation**: Animated matchday player with match clock ($0' \rightarrow 90'$), live event feed (Goals, Saves, Shots) using Material Icons, possession ratios, and post-match recap.
4. **League Standings**: Full Material 3 tabular leaderboard highlighting the user's club.
5. **Clean MVVM Architecture**: `GameViewModel` reactive state flow decoupling UI from simulation domain, with automatic JSON persistence to internal storage.

---

## 2. Architecture & Gradle Module Layout

```
football-manager/
├── build.gradle.kts          # Root build configuration with AGP & Kotlin Compose plugins
├── settings.gradle.kts       # Includes :engine and :app
├── engine/                   # Pure Kotlin JVM simulation engine (no Android deps)
│   └── src/
└── app/                      # Android Application module
    ├── build.gradle.kts
    └── src/
        └── main/
            ├── AndroidManifest.xml
            ├── res/
            └── kotlin/com/footballmanager/app/
                ├── MainActivity.kt
                ├── ui/
                │   ├── theme/
                │   │   ├── Color.kt
                │   │   ├── Type.kt
                │   │   └── Theme.kt
                │   ├── components/
                │   │   ├── PitchCanvas.kt
                │   │   ├── PlayerCardNode.kt
                │   │   ├── StatGauge.kt
                │   │   └── FormBadge.kt
                │   ├── navigation/
                │   │   └── AppNavigation.kt
                │   ├── screens/
                │   │   ├── HomeScreen.kt
                │   │   ├── TacticsScreen.kt
                │   │   ├── StandingsScreen.kt
                │   │   └── MatchdayScreen.kt
                │   └── viewmodel/
                │       ├── GameViewModel.kt
                │       └── GameUiState.kt
```

### Dependencies (`app/build.gradle.kts`)
* `implementation(project(":engine"))`
* `implementation(platform("androidx.compose:compose-bom:2024.10.01"))`
* `implementation("androidx.compose.material3:material3")`
* `implementation("androidx.compose.material:material-icons-extended")`
* `implementation("androidx.navigation:navigation-compose:2.8.3")`
* `implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")`
* `implementation("io.coil-kt:coil-compose:2.7.0")`

---

## 3. Design System & Styling (Material 3)

### 3.1 Color Palette & Theme Tokens
* **Primary (Stadium Emerald)**: `#10B981` (Accents, active tab, win indicators, CTAs)
* **Secondary (Electric Blue)**: `#3B82F6` (Midfield badges, secondary highlights)
* **Background**: `#0F172A` (Deep Slate / Dark Navy)
* **Surface / Container**: `#1E293B` (Elevated Charcoal Slate)
* **Surface Variant / Border**: `#334155` (Subtle card dividing lines)
* **Text High-Contrast**: `#F8FAFC`
* **Text Muted / Subtitle**: `#94A3B8`
* **Condition Status Indicators**:
  * High ($\ge 85\%$ fitness / $\ge 65$ morale): `#22C55E` (Green)
  * Medium ($60–84\%$): `#EAB308` (Amber)
  * Low ($< 60\%$): `#EF4444` (Coral Red)

### 3.2 Iconography Rules (Strictly No Emojis)
All UI visual indicators use vector icons from `androidx.compose.material.icons`:
* **Match Events**: `Icons.Outlined.SportsSoccer` (Goals), `Icons.Outlined.Shield` (Saves), `Icons.Outlined.Close` (Misses / Off-target).
* **Tactics & Actions**: `Icons.Outlined.SwapHoriz` (Substitutions), `Icons.Outlined.AutoFixHigh` (Auto-Pick Best XI), `Icons.Outlined.FlashOn` (Attack rating), `Icons.Outlined.Security` (Defense rating).
* **Navigation Tabs**: `Icons.Outlined.Dashboard` (Home), `Icons.Outlined.Sports` (Tactics), `Icons.Outlined.FormatListNumbered` (Standings), `Icons.Outlined.PlayCircle` (Matchday).

---

## 4. Screen Specifications & User Interactions

### 4.1 Home / Dashboard Screen (`HomeScreen.kt`)
* **Header Card**: Club crest/badge, Club name, League name, current in-game calendar date, and current standings rank badge.
* **Next Match Card**:
  * Home vs Away indicator, Opponent name, Opponent league rank.
  * Primary Action: "Proceed to Matchday" navigation trigger.
* **Squad Health Card**: Average squad fitness and morale progress bars with color-coded status chips.
* **Recent Form Row**: Vector circular badges for the last 5 matches (`W` green, `D` slate, `L` coral).

### 4.2 Tactics & Interactive Pitch Screen (`TacticsScreen.kt`)
* **Tactical Controls Header**:
  * **Formation Segmented Selector**: `4-4-2` | `4-3-3` | `5-3-2`.
  * **Mentality Segmented Selector**: `Defensive` | `Balanced` | `Attacking`.
  * **Effective Power Chips**: Live Attack & Defense strength numbers.
  * **Auto-Select Action**: "Auto Best XI" button that runs `Lineup.autoSelect(...)`.
* **Interactive Pitch Visualizer (`PitchCanvas.kt`)**:
  * Canvas-rendered football turf with field lines (halfway line, penalty boxes, center circle).
  * 11 starter nodes arranged by `Formation.slots` coordinates.
  * Each node displays: Position badge (`ST`, `CM`, `CB`, `GK`), player short name, effective overall rating (`84`), mini fitness bar.
* **Substitutes & Reserves Tray**:
  * Scrollable bench list below the pitch.
  * **Tap-to-Swap**: Tapping a starter node highlights it; tapping a bench substitute swaps their slots and recalculates team ratings in real time.

### 4.3 League Table Screen (`StandingsScreen.kt`)
* Sticky column headers: `#`, `Club`, `P`, `W`, `D`, `L`, `GF`, `GA`, `GD`, `Pts`.
* Table rows with monospace tabular numbers for aligned reading.
* Highlights the user's club with an elevated Emerald border/surface tint.

### 4.4 Matchday & Live Simulation Screen (`MatchdayScreen.kt`)
* **Scoreboard Header**: Home Club vs Away Club, Live Minute Counter ($0' \rightarrow 90'$).
* **Live Event Timeline**:
  * Chronological scrollable feed displaying match incidents with minute tags and Material Icons.
  * Dynamic possession progress bar updated as ticks advance.
* **Simulation Speed Controls**: "Fast Sim", "Tick Step", or "Skip to Full Time".
* **Post-Match Dialog / Summary**:
  * Full-time scoreline.
  * Match statistics (Shots, Shots on Target, Possession %).
  * Squad condition impact report ($\Delta$ Fitness on starters, $\Delta$ Morale).
  * Button: "Return to Dashboard" (auto-saves season state).

---

## 5. State Management & ViewModel (`GameViewModel.kt`)

```kotlin
data class GameUiState(
    val game: Game,
    val seasonState: SeasonState,
    val humanClubId: Long = 1L,
    val selectedStarterPlayerId: Long? = null,
    val isSimulating: Boolean = false,
    val simulationTick: Int = 0,
    val currentMatchResult: MatchResult? = null,
    val message: String? = null,
)
```

### State Mutations
1. `setFormation(formation: Formation)`: Updates tactics and re-aligns starting lineup.
2. `setMentality(mentality: Mentality)`: Updates mentality modifiers.
3. `selectStarterForSwap(playerId: Long)`: Sets `selectedStarterPlayerId`.
4. `swapWithSubstitute(benchPlayerId: Long)`: Exchanges starting XI player ID with substitute ID and updates `SeasonState.lineups`.
5. `autoSelectLineup()`: Invokes `Lineup.autoSelect` for human club.
6. `advanceMatchday()`: Executes `SeasonRunner.playNextMatchday(seasonState)` and updates `game.players` and `game.currentSeason`.
7. `saveGame()`: Serializes `game` to `context.filesDir/save_game.json`.

---

## 6. Testing & CI Integration

1. **Unit Tests (`app/src/test/`)**:
   * `GameViewModelTest`: Verifies tactics change, lineup swap mutations, auto-selection, and matchday state advancement.
   * `UiStateMappingTest`: Verifies correct mapping from `SeasonState` / `Player` to UI models.
2. **CI Workflow (`.github/workflows/ci.yml`)**:
   * Runs `gradle build` (building both `:engine` and `:app`) and executes all test suites.
