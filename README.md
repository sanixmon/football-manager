# Football Manager (Kotlin)

A football management / simulation game in Kotlin. The simulation engine is pure
Kotlin (JVM) and fully independent of the Android SDK, so it can be tested
headlessly.

## Build strategy

There is **no local build** — everything runs in GitHub Actions CI
(`.github/workflows/ci.yml`). The repository does not commit the Gradle wrapper;
instead the workflow uses `gradle/actions/setup-gradle@v6` to download and cache
a pinned Gradle version (`8.14.4`), keeping CI fully reproducible.

## Modules

| Module    | Purpose                                                        |
|-----------|----------------------------------------------------------------|
| `:engine` | Pure Kotlin simulation engine (models, match engine, tactics, season). No Android. |

Planned layout under `:engine`:

```
engine/src/main/kotlin/com/footballmanager/
├── model/       # Club, Player, Squad, League, Calendar, ...
├── simulation/  # MatchEngine, SeasonSimulator, ...
├── tactics/     # Formation, Mentality, TacticsModifier, ...
└── season/      # Tables, fixtures, schedule, ...
```

## Local development (optional)

To build locally you only need a JDK 17+ and Gradle 8.14+:

```bash
gradle build
```

Or generate a wrapper once and commit it later if preferred:

```bash
gradle wrapper --gradle-version 8.14.4
```
