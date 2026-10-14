# UI Redesign Audit — Football Manager (Phase 0)

_Date: 2026-10-14 · Branch: `main` @ `7465f77` (landscape-only UI merged)_
_Auditor method: direct inspection of `app/` sources, `engine/` interfaces, tests, CI._

## 1. Existing architecture summary

- **Modules**: `:engine` (pure Kotlin/JVM, no Android imports — enforced by `ArchitectureFitnessTest`)
  and `:app` (Android + Jetpack Compose Material 3, `compileSdk 35`, `minSdk 26`).
- **State**: single `GameViewModel` exposing `StateFlow<GameUiState>`; manual DI via
  `DefaultAppContainer` (`di/AppContainer.kt`). Persistence: `JsonFileGameRepository`
  (autosave `current_save.json`) + `SaveCareerUseCase`/`LoadCareerUseCase` (named slots).
- **Engine integration**: UI consumes use cases only — `SeasonRunner` (start/play matchday),
  `AdvanceDayUseCase`, `SeasonRolloverUseCase`, `SelectLineupUseCase`, `DirectTransferUseCase`,
  bid/contract use cases (`SubmitTransferBidUseCase`, `EvaluateTransferOfferUseCase`,
  `NegotiateContractUseCase`, `CompleteTransferUseCase`), `PlayerDevelopmentUseCase`
  (training focus/intensity), `ApplyMatchResultToWorldUseCase`.
- **Entry**: `MainActivity` (locked `sensorLandscape`, immersive sticky fullscreen) switches
  `MAIN_MENU` / `NEW_GAME_WIZARD` / `IN_GAME`. In-game shell is `SquadScreen`
  (rail + 42dp top bar + section content). No Compose Navigation library usage at runtime
  despite the dependency; navigation is `activeNavSection` state.
- **CI** (`.github/workflows/ci.yml`): `gradle build` (all tests) → `:engine:run` demo →
  `:app:assembleDebug` → APK artifact. Authoritative; no local Android build.

## 2. Screen and navigation inventory

| # | Screen / entry | File | Genuinely functional? |
|---|---|---|---|
| 1 | Main menu | `ui/screens/MainMenuScreen.kt` | Yes — Continue / Load dialog / New career. No empty state (a default career always exists). |
| 2 | New-career wizard | `ui/screens/NewGameWizardScreen.kt` | Yes — name/nationality/club → `startNewCareer`. |
| 3 | Dashboard | `ui/screens/HomeScreen.kt` | Yes, real data (fixture, form, standings, fitness, finances). |
| 4 | Squad | `SquadScreen.kt` + `FmSquadTable.kt` + `PlayerDetailPanel.kt` | Yes — sort/filter table, selection, training focus/intensity, facilities. |
| 5 | Tactics + pitch | `TacticsScreen.kt` + `PitchCanvas.kt` + `PlayerCardNode.kt` | Yes — formation/mentality/tempo/pressing/passing, tap-to-swap lineup, auto XI, validation. |
| 6 | Matchday | `MatchdayScreen.kt` | Partial — pre-match preview + full-time result (timeline/stats/ratings). Simulation is **instant whole-round** (`playNextMatchday`); no live/half-time states exist in the engine. |
| 7 | Standings | `ui/screens/StandingsScreen.kt` | Yes, real table + previous-season toggle. |
| 8 | Transfers | `ui/screens/TransfersScreen.kt` | Partial — market browse/filter, **instant** direct buy/sell, history. |
| 9 | Save/Load dialog | `ui/components/SaveLoadDialog.kt` | Yes — named slots with metadata. |
| 10 | `NavigationTab` enum | `ui/navigation/AppNavigation.kt` | **Dead** — zero references. |
| 11 | `FmNavigationDrawerContent` | `ui/components/FmNavigation.kt` | **Dead** — drawer removed from shell; rail is the only nav. |
| 12 | `StatGauge`, `FormBadge` | `ui/components/` | **Dead** — no usages. |
| 13 | `PlayerDetailBottomSheet` | `PlayerDetailPanel.kt` | **Dead** — portrait leftover, no callers. |
| 14 | `FmNavSection` companion aliases (`SCHEDULE`, `INBOX`, `SCOUTING`, …) | `FmNavigation.kt` | **Dead/misleading** — only `HOME` is used (VM/state defaults). |
| 15 | Squad tabs REPORT/DYNAMICS/STATS/CONTRACTS | `FmTabRow.kt` + `SquadScreen.kt` | **Placeholders** — all render the identical table; only OVERVIEW (= table) and TRAINING (= focus strip) map to real features. |

## 3. Functional vs placeholder classification

**Connected to real state changes**: Continue (advanceDay/playNextMatchday/rollover via
`advanceSmart`), formation + 4 tactical axes, auto XI, starter/bench swap with validation,
training focus/intensity (player + club), direct transfers buy/sell, save/load slots,
new career, section navigation, squad sort/filter/position filter, market position filter,
previous-season standings toggle, season rollover.

**Visually present but nonfunctional / misleading**:
1. `onSubmitBid` prop (`MainActivity` → `SquadScreen`) is accepted and **never forwarded**;
   the entire bid → contract → complete flow (`activeBids`, `TransferBid`, all four VM
   functions, covered by `GameViewModelTransferTest`) has **no UI surface**. The
   `TransfersScreen` params `onOfferContract`/`onCompleteDeal`/`onCancelDeal` are likewise
   accepted and never rendered — placeholder callbacks concealing missing behavior.
2. Squad tabs REPORT/DYNAMICS/STATS/CONTRACTS imply features; they render the same table.
3. `showTrainingDialog` / `showSeasonHistoryDialog` VM flags have no UI; `currentSimTick`
   is never updated (instant sim); `clearSaveStatus()` is tested but never called from UI,
   so the save banner persists until the next state change.
4. Standings zone coloring ("Champions / European / Relegation") implies competition rules
   the engine does not define (no relegation/Europe model; `Competition` is League/Cup with
   Cup unused).
5. Breadcrumb hardcodes `"Liga Nusantara"` (`SquadScreen.kt:126`, `FmTopAppBar.kt:122`
   fallback) while the default dataset is LaLiga — factually wrong label.
6. Main menu footer `"Pure JVM Domain Architecture · Verified Clean"` is dev-facing text in
   a player surface.

**Duplicated information**: finances appear in top-bar badge + dashboard + transfers header
(acceptable, different contexts); save/load buttons duplicated in dashboard while dialogs are
global (minor).

**Recomposition**: all screens read `state: GameUiState` directly; selection state that is
local (`selectedPlayerId`, market selection, sub-tabs, filters) is `remember`-held and
consistent. No duplicated sources of truth for domain data.

## 4. UI problems ranked by impact

1. **[High] Generic SaaS-card aesthetic.** Every screen repeats the same
   rounded-card + uppercase-blue-label pattern; screens are indistinguishable at a glance.
   No football identity beyond the pitch canvas.
2. **[High] Misleading navigation/tabs** (§3 items 1–2). Must be resolved honestly, not by
   fabricating screens.
3. **[High] Dashboard is a nav-menu clone.** QUICK WORKSPACE ACTIONS duplicates the rail;
   panels are not clickable; finances/action rows repeat global chrome.
4. **[Medium] Matchday is result-only.** No round context (other fixtures), no condition
   aftermath, no link back to tactics when validation blocks simulation.
5. **[Medium] League table implies rules** (zones) and buries user context (no form, no
   next fixtures for the user's club).
6. **[Medium] Transfers lack comparison data** (no wage/expiry in rows; valuation shown is
   a 0.9× estimate presented without qualification) and the real bid flow is hidden.
7. **[Low] Hardcoded "Liga Nusantara"** breadcrumb vs actual league data.
8. **[Low] Dead code** (`NavigationTab`, drawer content, `StatGauge`, `FormBadge`,
   bottom-sheet, companion aliases) widens the maintenance surface.
9. **[Low] Save banner not dismissible** (`clearSaveStatus` unwired).
10. **[Low] Detail panel `onClose` is `{}`** — selection can't be cleared.

## 5. Behavior that must be preserved (test-protected)

- VM init (club 1, 11 starters), `startNewCareer` → IN_GAME + DASHBOARD (`GameViewModelTest`).
- All six `FmNavSection` destinations reachable (`all six MVP screens are reachable`).
- Formation/mentality/tempo/pressing/passing updates, auto XI, swap validation +
  `lineupValidationError`, matchday stamina/morale progression, autosave message.
- Full bid → contract → complete/cancel transfer flow at VM level
  (`GameViewModelTransferTest`) — must keep passing even though UI exposes direct transfer.
- `GameFlowIntegrationTest`: container → formation change → `playNextMatchday` → results,
  and save-file persistence across VM restarts (save format untouched).
- Engine suite untouched (no engine changes in this redesign).
- Landscape-only lock, immersive fullscreen, CI green.

## 6. Planned files to modify or create

- **Create** `docs/ui-redesign-audit.md` (this file).
- **Create** `app/.../ui/components/FmShared.kt`: `FmSectionHeader`, `FmEmptyState`,
  `FmStatusBadge` (icon + text; never color-alone) — small, single-purpose.
- **Delete**: `ui/navigation/AppNavigation.kt`; `FmNavigationDrawerContent` (in
  `FmNavigation.kt`); misleading `FmNavSection` companion aliases except `HOME`.
- **Edit**: `FmNavigation.kt` (rail only), `SquadScreen.kt` (league-name breadcrumb,
  squad tabs → OVERVIEW + TRAINING via existing `tabs` param, inspector deselect,
  dismissible save banner via `clearSaveStatus`), `MainMenuScreen.kt` (drop tech footer),
  `HomeScreen.kt` (command-center hierarchy, clickable panels, drop nav-duplicate buttons),
  `TacticsScreen.kt` + `MatchdayScreen.kt` (shared headers, round context, tactics link),
  `StandingsScreen.kt` (honest zone treatment + user form/fixtures), `TransfersScreen.kt`
  (comparison data, qualified estimate label; remove dead bid/contract props),
  `MainActivity.kt` (drop dead `onSubmitBid` wiring), `FmTopAppBar.kt` (league-name
  fallback), `PlayerDetailPanel.kt` (close/deselect support — keep bottom-sheet? decide:
  delete dead `PlayerDetailBottomSheet`).
- Theme (`Color.kt`/`Type.kt`) is kept; semantic mapping documented in §7. No light theme.

## 7. Design-system semantic mapping (existing tokens, no new palette)

- Backgrounds: `FmDarkBg` / `FmSurface` / `FmSurfaceAlt`; borders `FmBorder`.
- Text: `FmTextPrimary` / `FmTextSecondary` / `FmTextMuted`.
- Primary action/continue: `FmContinueGreen` (black text); info/links: `FmAccentBlue`;
  selection highlight: `FmSurfaceSelected` + `FmAccentCyan` indicator.
- Status (uniform everywhere): positive `FmRatingHigh`, warning `FmRatingMed`, negative
  `FmRatingLow`; form W/D/L reuses the same three (`FmFormWin/Draw/Loss` aliases).
- Positions: GK `FmPosGk` / DEF `FmPosDef` / MID `FmPosMid` / ATT `FmPosAtt` + text label.
- Player rating: ≥70 high, ≥60 med, else low (already consistent in table + detail; keep).
- Typography: existing `Typography` scale kept; section headers via shared component;
  monospace `labelSmall` for numbers/table headers.

## 8. Known dependencies and risks

- Bid/contract engine flow stays VM-tested but UI-less; documented as future work, not
  fabricated. Removing dead UI props does not affect `GameViewModelTransferTest`.
- Instant whole-round simulation is an engine fact; matchday redesign must not imply live
  play. `currentSimTick` stays unused.
- Zone-label change in standings is presentation-only; sorting/points logic untouched.
- Narrow landscape (~700dp wide): rail 64dp + 42dp bar + 2-column content is the floor
  being designed for; no portrait support (locked).
- No emulator/device available to the agent: visual correctness is validated by code
  review + CI compile/tests only; stated explicitly in the final report.
