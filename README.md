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
└── simulation/  # MatchEngine + season/ (SeasonSimulator, fixtures, standings)
```

## Mods (custom databases & graphics)

The engine can load community content without touching core code.

### Data mods (JSON)

A data mod is a JSON file describing a league, its clubs and players. Ids are
assigned automatically, and attribute/position/tactics values use the engine's
own names:

```json
{
  "name": "My League",
  "startDate": "2026-08-01",
  "league": { "name": "Liga Saya" },
  "clubs": [
    {
      "name": "Klub A",
      "shortName": "KA",
      "formation": "4-3-3",
      "mentality": "Attacking",
      "players": [
        { "name": "Striker", "position": "ST", "age": 26, "attributes": { "FINISHING": 88, "PACE": 85 } }
      ]
    }
  ]
}
```

Load it with `ModLoader.loadFromFile(path)` / `loadFromJson(text)` /
`loadFromResource("mod/…")`. A sample lives at `engine/src/main/resources/mod/sample-mod.json`.

### Graphics packs (PNG)

Logos, kits and faces are resolved by club/player id from a directory following
this convention (`GraphicsPack`):

```
<root>/logos/<clubId>.png
<root>/kits/<clubId>/{home,away,third}.png
<root>/faces/<playerId>.png
```

A mod can give a club/player an optional `"graphicsId"` so its assets are keyed
by an external id (e.g. a community pack's id) instead of the auto-assigned
internal id. `GraphicsPack.logoPath(club)` / `facePath(player)` / `kitPath(club, side)`
prefer `graphicsId` and fall back to the internal id.

Note: this project does **not** read Sports Interactive's proprietary `.fmf`
database files, and does not bundle real player data.

## Local development (optional)

To build locally you only need a JDK 17+ and Gradle 8.14+:

```bash
gradle build
```

Or generate a wrapper once and commit it later if preferred:

```bash
gradle wrapper --gradle-version 8.14.4
```
