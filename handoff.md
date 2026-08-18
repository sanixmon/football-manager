# Handoff — Football Manager (Kotlin)

_Last updated: 2026-08-18 · Repo: `github.com/sanixmon/football-manager` (public) · Branch: `main`_

A football management / simulation game in **pure Kotlin (JVM)**. The simulation
engine is fully decoupled from any UI toolkit — there is no Android/Compose yet.

---

## 1. Status in one paragraph

The headless engine and the Android Jetpack Compose UI are complete and green in CI:
domain model, tick-based match engine, tactics, double round-robin season simulation,
an incremental matchday-by-matchday runner, deterministic seed data, JSON save/load,
custom database "mods", graphics-pack resolver, and an interactive 4-tab Android
application (`:app` module with Jetpack Compose & Material 3: Home Dashboard, Tactics &
Interactive Pitch Visualizer, Standings Table, and Live Matchday Simulation).

---

## 2. Tech stack

| Concern | Choice |
|---|---|
| Language | Kotlin (JVM & Android) |
| Kotlin version | **2.4.10** |
| Android Gradle Plugin | **8.7.3** (compileSdk 35, minSdk 26) |
| UI Framework | **Jetpack Compose** (BOM `2024.10.01`, Material 3) |
| Navigation | `androidx.navigation:navigation-compose:2.8.3` |
| Gradle | **8.14.4** (no wrapper committed — see §4) |
| JDK | **17** (Temurin) |
| Build tool | Gradle Kotlin DSL |
| Serialization | `kotlinx-serialization-json` **1.11.0** |
| Tests | JUnit 5 via `kotlin("test-junit5")` |
| CI | GitHub Actions (`.github/workflows/ci.yml`) |

---

## 3. Module layout

Multi-module Gradle project:
- **`:engine`**: Pure Kotlin JVM simulation engine (no Android deps).
- **`:app`**: Android Application (Jetpack Compose, Material 3, ViewModel, Navigation).

```
football-manager/
├── engine/src/main/kotlin/com/footballmanager/
│   ├── model/          # domain: Player, Club, Squad, Competition, Calendar, Game
│   ├── simulation/     # MatchEngine, Team, Tactics, Lineup, MatchResult/Event
│   ├── simulation/season/  # SeasonSimulator, SeasonRunner, SeasonState, SeasonResult
│   ├── serialization/  # Game.saveToFile/loadFromFile + custom serializers
│   ├── mod/            # ModFile schema + ModLoader
│   ├── graphics/       # GraphicsPack (logo/kit/face resolver by id)
│   └── seed/           # SeedData (10 clubs x 18 players, deterministic)
└── app/src/main/kotlin/com/footballmanager/app/
    ├── MainActivity.kt
    ├── ui/theme/       # Material 3 Dark Stadium theme & typography tokens
    ├── ui/components/  # PitchCanvas, PlayerCardNode, StatGauge, FormBadge
    ├── ui/navigation/  # 4-tab AppNavigation
    ├── ui/screens/     # HomeScreen, TacticsScreen, StandingsScreen, MatchdayScreen
    └── ui/viewmodel/   # GameViewModel & GameUiState
```

---

## 4. Build & CI (important)

**There is no local build and no Gradle wrapper.** Everything runs in GitHub
Actions. The repo does not commit `gradlew`; CI uses
`gradle/actions/setup-gradle@v6` with `gradle-version: '8.14.4'` and invokes
`gradle` directly (not `./gradlew`).

CI steps: `checkout` → `setup-java` (Temurin 17) → `setup-gradle` (caches Gradle
+ deps, `cache-provider: basic`) → `gradle build` → `gradle :engine:run -q` (the demo).

---

## 5. What is implemented

- **Domain model** (`model/`): `Game` aggregate (clubs/players/competitions/calendar),
  `Player` (attributes, contract, fitness, morale, `graphicsId`), `Club` (finance,
  facilities, squad, `defaultTactics`, `graphicsId`), `Competition` (sealed: `League`/`Cup`),
  `Calendar` (sorted fixtures). Attributes are 1–100; **overall rating is computed
  per position** from `PositionWeights` (a striker rates higher at ST than CB).
  **`Player.effectiveOverall(position)`** accounts for real-time `fitness` and `morale`.
- **Match engine** (`MatchEngine`): 90 min = 18 ticks × 5 min. Possession → chance →
  shot → goal/save/miss. Home advantage ×1.05. `RandomSource` is injectable so
  results are reproducible (prod `KotlinRandomSource`, tests use `FakeRandomSource`).
- **Tactics & Lineup Selection**: `Formation` (4-4-2, 4-3-3, 5-3-2) defines 11 positional slots
  (`Formation.slots`). `Lineup` specifies starting 11 players and substitutes; `Lineup.autoSelect`
  assigns optimal squad members to slots. `Team.fromLineup` derives team strength from starting XI
  ratings and condition.
- **Season simulation** (`SeasonSimulator`): double round-robin (leg 2 = leg 1 with
  home/away swapped), standings W=3/D=1/L=0, sorted Pts → GD → GF, champion.
- **Incremental play** (`SeasonRunner` + `SeasonState`): advance one matchday at a
  time, `setTactics(clubId, tactics)` and `setLineup(clubId, lineup)` before each match.
  Simulates matchdays with dynamic starting lineups; starters deplete stamina (-12),
  benched players recover (+18), and match outcomes update squad morale.
- **Android Jetpack Compose UI (`:app`)**:
  - **Home Dashboard**: Club overview, next fixture card, date, round counter, squad fitness/morale gauges.
  - **Tactics & Interactive Pitch**: Visual turf grass canvas rendering 11 starter cards according to formation slots, live attack/defense ratings, bench tray, and tap-to-swap player substitution.
  - **Standings Table**: Full league table with human club highlighted and tabular alignment.
  - **Matchday Live Sim**: Match simulator with minute ticker, Material 3 vector incident icons (goals, saves, misses), and scoreline summaries.
  - **GameViewModel**: Reactive `StateFlow<GameUiState>` managing engine state and persistent saves.
- **Seed data**: 10 fictional Indonesian clubs × 18 players, deterministic (`Random` seed).
- **Save/load**: whole `Game` (including `lastSeason` and `currentSeason` with updated `players` and `lineups`)
  round-trips to/from pretty-printed JSON. Custom serializers exist for `java.time.LocalDate`,
  `PlayerAttributes` (a map), and `Calendar` (a list).
- **Modding**: `ModLoader` turns an author-friendly JSON file (clubs → players, string
  enums like `"ST"`, `"FINISHING"`, `"4-3-3"`) into a `Game`. Optional `graphicsId`
  lets assets map to external ids.
- **Graphics packs**: `GraphicsPack` resolves `<root>/logos/<id>.png`,
  `<root>/kits/<id>/{home,away,third}.png`, `<root>/faces/<id>.png` by club/player id,
  preferring `graphicsId` then falling back to internal id.

---

## 6. Key design decisions

- **Engine is pure Kotlin, UI-free** — match/season logic is testable without Android.
- **Overall rating is derived, not stored** — a player's rating depends on position.
- **IDs are `Long`, entities reference each other by id** (Room-friendly later).
- **RNG injected, never global** — deterministic and reproducible tests.
- **Immutable domain** — `StandingEntry.record()` and `SeasonState.copy(...)` accumulate
  state without mutation.
- **Strictly Vector Icons (No emojis)** — Material Icons and vector paths for clean sports dashboard styling.
- **One-shot vs incremental**: `SeasonSimulator` simulates a whole season at once;
  `SeasonRunner` plays matchday-by-matchday and is proven equivalent (same seed →
  same standings) by `SeasonRunnerTest`.
- **Condition & Lineups**: Starting 11 players drive team match power; squad rotation is rewarded.

---

## 7. Testing

**97 tests, all green.** Highlights:

- `GameViewModelTest` — tactics updates, lineup swap mutations, and matchday condition progression in Android `:app`.
- `MatchEngineTest` / `MatchEngineStatisticsTest` — 18 ticks, score bounds, deterministic
  RNG, 10k-match statistical tests (stronger team wins more, home advantage, draws possible).
- `TacticsTest` / `TacticsStatisticsTest` — attacking concedes & scores more; formation bias; 11-slot validation.
- `PlayerConditionTest` — effective rating calculation under varying fitness/morale.
- `LineupTest` — lineup validation and optimal slot auto-selection.
- `TeamLineupTest` — team power derived from starting XI and condition drops.
- `SeasonRunnerConditionTest` — matchday stamina drain, recovery, and morale progression.
- `FixtureGeneratorTest` — double round-robin: 10 teams → 90 fixtures, every pair meets
  twice (once per venue), no team plays twice per round.
- `SeasonSimulatorTest` / `SeasonSimulatorStatisticsTest` — points, GD, sorting, champion.
- `SeasonRunnerTest` — matchday-by-matchday matches one-shot season; tactics apply.
- `GameSerializationTest` — save/load round-trips (small game, full seed world, season
  result, mid-season resume with player condition).
- `ModLoaderTest` — JSON mod → Game; `graphicsId` passthrough; bad enum fails fast.
- `GraphicsPackTest` — logo/kit/face resolution, graphicsId fallback, missing files.

---

## 8. Commands (CI is authoritative; local optional)

```bash
gradle build              # compile + run all tests across :engine and :app
gradle :engine:run -q     # run Main.kt demo (prints standings + save/load + mod demo)
```

---

## 9. Roadmap / next steps

1. **Season-end loop** — promote `currentSeason` → `lastSeason`, reset for next season.
2. **Transfers / contracts / finance / scouting** (Phase 4 of the original plan).
3. **World simulation** — AI managers, youth academy, injuries, retirement, news.
4. **Cup tournament (knockout)** — `Cup` type exists but is unused.

---

## 10. Gotchas & constraints

- **No local build** — verify via GitHub Actions; if a run fails, read the step log.
- **`Main.kt` writes `demo-save.json`** (gitignored) and `gradle run` prints a lot;
  the demo is non-interactive (CI can't do stdin).
- **Do NOT parse Sports Interactive `.fmf`/`.dbc`** (proprietary) or bundle real player
  data (copyrighted). Mod support uses an open JSON format instead.
- **`Club.defaultTactics` / `Player.graphicsId` / `Game.lastSeason` / `Game.currentSeason`
  are all optional (`null` default)** — existing code remains backward compatible.
- **Transient CI failures** (e.g. Maven Central `429`) are retry-able; rerun the workflow.

---

## 11. Recent history (git log, newest first)

```
3fa9888 fix: extract PitchRow as top-level @Composable in PitchCanvas
76a67c1 chore: enable android.useAndroidX in gradle.properties
9b820e5 fix: use kotlin jvmToolchain in app module
f36d747 ci: update multi-module run command in CI workflow
c52f44c feat: implement 4-tab manager UI suite with Material 3 vector icons
cc3a984 feat: implement GameViewModel and state management with unit tests
c8771e0 feat: implement tactical pitch visualizer and player card node
dcf8956 feat: implement Material 3 design tokens and shared UI components
be1f94e chore: scaffold Android app module with Compose and Material 3
a3a7c30 docs: add implementation plan for Android Jetpack Compose UI
82de2f3 docs: add design spec for Android Jetpack Compose UI
```
