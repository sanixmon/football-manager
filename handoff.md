# Handoff — Football Manager (Kotlin)

_Last updated: 2026-08-18 · Repo: `github.com/sanixmon/football-manager` (public) · Branch: `main`_

A football management / simulation game in **pure Kotlin (JVM)**. The simulation
engine is fully decoupled from any UI toolkit — there is no Android/Compose yet.

---

## 1. Status in one paragraph

The headless engine is complete and green in CI: domain model, tick-based match
engine, tactics, double round-robin season simulation, an incremental
matchday-by-matchday runner, deterministic seed data, JSON save/load, custom
database "mods", and a graphics-pack resolver. A human can already "play" a
season from the CLI (`Main.kt`): pick a club, rotate tactics each matchday, and
save/resume mid-season. The next big milestone is a UI (Android/Compose).

---

## 2. Tech stack

| Concern | Choice |
|---|---|
| Language | Kotlin (JVM) |
| Kotlin version | **2.4.10** |
| Gradle | **8.14.4** (no wrapper committed — see §4) |
| JDK | **17** (Temurin) |
| Build tool | Gradle Kotlin DSL |
| Serialization | `kotlinx-serialization-json` **1.11.0** |
| Tests | JUnit 5 via `kotlin("test-junit5")` (no extra deps) |
| Runtime deps | **none** (JSON lib is the only non-test dependency) |
| CI | GitHub Actions (`.github/workflows/ci.yml`) |

---

## 3. Module layout

Single Gradle module `:engine` (pure Kotlin JVM). Package root
`com.footballmanager`:

```
model/          # domain: Player, Club, Squad, Competition (League/Cup), Calendar, Game
simulation/     # MatchEngine, Team, Tactics, MatchResult/Event/Stats, RandomSource
simulation/season/  # Fixture, FixtureGenerator, Standings, SeasonSimulator, SeasonRunner, SeasonState, SeasonResult
serialization/  # Game.saveToFile/loadFromFile + custom serializers (LocalDate, PlayerAttributes, Calendar)
mod/            # ModFile schema + ModLoader (JSON -> Game)
graphics/       # GraphicsPack (logo/kit/face resolver by id)
seed/           # SeedData (10 clubs x 18 players, deterministic)
Main.kt         # CLI entry point (playable demo + mod demo)
resources/mod/sample-mod.json   # example custom database mod
```

---

## 4. Build & CI (important)

**There is no local build and no Gradle wrapper.** Everything runs in GitHub
Actions. The repo does not commit `gradlew`; CI uses
`gradle/actions/setup-gradle@v6` with `gradle-version: '8.14.4'` and invokes
`gradle` directly (not `./gradlew`).

CI steps: `checkout` → `setup-java` (Temurin 17) → `setup-gradle` (caches Gradle
+ deps, `cache-provider: basic`) → `gradle build` → `gradle run -q` (the demo).

To run the demo's output, read the "Run demo" step log in the Actions tab (the
CI output is the only executable proof of the entry point).

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
- **Seed data**: 10 fictional Indonesian clubs × 18 players, deterministic (`Random` seed).
- **Save/load**: whole `Game` (including `lastSeason` and `currentSeason` with updated `players` and `lineups`)
  round-trips to/from pretty-printed JSON. Custom serializers exist for `java.time.LocalDate`,
  `PlayerAttributes` (a map), and `Calendar` (a list).
- **Modding**: `ModLoader` turns an author-friendly JSON file (clubs → players, string
  enums like `"ST"`, `"FINISHING"`, `"4-3-3"`) into a `Game`. Optional `graphicsId`
  lets assets map to external ids.
- **Graphics packs**: `GraphicsPack` resolves `<root>/logos/<id>.png`,
  `<root>/kits/<id>/{home,away,third}.png`, `<root>/faces/<id>.png` by club/player id,
  preferring `graphicsId` then falling back to internal id. (No image rendering yet.)

---

## 6. Key design decisions

- **Engine is pure Kotlin, UI-free** — match/season logic is testable without Android.
- **Overall rating is derived, not stored** — a player's rating depends on position.
- **IDs are `Long`, entities reference each other by id** (Room-friendly later).
- **RNG injected, never global** — deterministic and reproducible tests.
- **Immutable domain** — `StandingEntry.record()` and `SeasonState.copy(...)` accumulate
  state without mutation.
- **One-shot vs incremental**: `SeasonSimulator` simulates a whole season at once;
  `SeasonRunner` plays matchday-by-matchday and is proven equivalent (same seed →
  same standings) by `SeasonRunnerTest`.
- **Condition & Lineups**: Starting 11 players drive team match power; squad rotation is rewarded.

---

## 7. Testing

**93 tests, all green.** Highlights:

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
gradle build       # compile + run all tests
gradle run -q      # run Main.kt demo (prints standings + save/load + mod demo)
```

---

## 9. Roadmap / next steps

1. **Android/Compose UI** — render logos/kits/faces via `GraphicsPack`, interactive
   squad / tactics / standings screens. Engine is ready; UI is the missing face.
2. **Season-end loop** — promote `currentSeason` → `lastSeason`, reset for next season.
3. **Transfers / contracts / finance / scouting** (Phase 4 of the original plan).
4. **World simulation** — AI managers, youth academy, injuries, retirement, news.
5. **Cup tournament (knockout)** — `Cup` type exists but is unused.

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
9684939 fix: add value equality to Calendar and verify full game round-trip serialization
1d286e0 feat: persist player condition and showcase lineup rotation in demo
199db45 feat: manage lineup and player condition updates across matchdays
d6db96d feat: derive team power from lineup starting XI and condition
8dbe142 feat: add Lineup model and auto-selection logic
bd2c11c feat: add formation positional slots and player effectiveOverall
1ccd127 docs: add implementation plan for pre-match selection and player condition
9ab6acc docs: add design spec for pre-match selection and player condition
cd8f8e7 Map graphics to external ids via Club/Player.graphicsId
f85f973 Add mod support: JSON custom databases and graphics packs
9c2e002 Add incremental matchday-by-matchday season runner
3bc7723 Persist season results on the Game aggregate
4a2883b Add JSON save/load via kotlinx.serialization
2938460 Switch to double round-robin fixtures
1418de6 Add tactics (formation + mentality) as team modifiers
7a666d3 Wire seed data end-to-end and add demo entry point
542c838 Add season simulation layer
eb139b2 Add core:simulation match engine v0.1
1b9cfe4 Add core:model domain types for the simulation engine
f2bd8e4 Scaffold pure-Kotlin engine module with GitHub Actions CI
```
