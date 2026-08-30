# Architecture & Design Invariants

This document outlines the architecture, layer boundaries, and invariants for the Football Manager project.

---

## 1. Multi-Module Layout

```
football-manager/
├── engine/src/main/kotlin/com/footballmanager/
│   ├── model/          # Pure immutable domain models (Player, Club, League, Game)
│   ├── calculator/     # Pure mathematical formulas & ratings (PlayerCalculator)
│   ├── usecase/        # Domain orchestrations (SelectLineupUseCase, SimulateMatchUseCase, etc.)
│   ├── repository/     # Persistence interfaces & implementations (GameRepository, PlayerRepository)
│   ├── simulation/     # MatchEngine, Tactics, Lineup, SeasonRunner, SeasonState
│   ├── serialization/  # JSON serialization & atomic file IO
│   ├── logging/        # Zero-overhead lazy observer/logging interfaces
│   └── seed/           # Deterministic world generator
└── app/src/main/kotlin/com/footballmanager/app/
    ├── di/             # Manual DI (AppContainer)
    ├── ui/             # Jetpack Compose UI (screens, components, theme)
    └── ui/viewmodel/   # Reactive state management (GameViewModel, GameUiState)
```

---

## 2. Key Architectural Invariants

1. **Pure Kotlin Engine Isolation**:
   - `:engine` must NEVER import `android.*`, `androidx.*`, or UI libraries.
   - Enforced automatically by `ArchitectureFitnessTest`.

2. **Immutable Domain Entities**:
   - Domain models (`Player`, `Club`, `Tactics`, `Lineup`, `SeasonState`, `Game`) are immutable data classes.
   - Modifications return new copies (`copy()`, `setTactics()`, `setLineup()`).

3. **Deterministic Simulation & Injectable RNG**:
   - Simulation engines (`MatchEngine`, `SeasonSimulator`, `SeasonRunner`) receive `RandomSource` or explicit `seed`.
   - Production uses `KotlinRandomSource`; unit/integration tests use `FakeRandomSource` or fixed seeds.

4. **Zero-Overhead Logging & Lazy Evaluation**:
   - `MatchLogger` provides `isEnabled` checks and `() -> String` message suppliers so simulations have zero string allocation overhead when disabled.

5. **Thread-Safe & Atomic Persistence**:
   - `InMemoryGameRepository` uses `AtomicReference<Game>`.
   - `JsonFileGameRepository` uses `@Volatile` cache and atomic `.tmp` file replacement (`StandardCopyOption.ATOMIC_MOVE`).

6. **Decoupled Calculations**:
   - `PlayerCalculator` encapsulates positional weighting, fitness scaling, and morale modifiers, keeping `Player` a lightweight data carrier.
