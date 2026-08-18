# Laporan Progres — Football Manager (Kotlin)

*Review: technical lead · Tanggal: 18 Agustus 2026 · Commit terakhir: `542c838` (season simulation layer)*

## Status Umum

Project ini masih murni **simulation engine headless** — belum ada UI, belum ada persistence, dan belum ada "dunia" (seed data) yang menjalankannya. Yang sudah solid adalah **match engine** (18-tick, deterministik, RNG injectable) dan **season simulator** (single round-robin + standings), keduanya tertutup test (48 test, hijau di CI). Domain model inti (Player/Club/Competition) sudah lengkap sebagai data structure, tapi belum terhubung ke engine secara end-to-end. Secara keseluruhan ini masih jauh dari game yang bisa "dimainkan", tapi fondasi simulasi-nya sudah benar dan testable.

## Tech Stack

| Aspek | Detail |
|---|---|
| Bahasa | Kotlin (Kotlin/JVM), toolchain JDK 17 |
| Build | Gradle **8.14.4** (di-download CI via `setup-gradle@v6`, **tanpa wrapper ter-commit**), Kotlin Gradle Plugin **2.4.10** |
| Test | `kotlin-test` + JUnit 5 (Jupiter 5.10.1 + launcher 1.10.1, transitif dari `kotlin-test-junit5`) |
| Runtime dependency | **Tidak ada** (stdlib + `java.time` + `kotlin.random`) — tidak ada Android SDK, Room, kotlinx.serialization, Compose, atau framework DI |
| CI | GitHub Actions: `checkout@v7`, `setup-java@v5` (temurin 17), `setup-gradle@v6` (`gradle-version: 8.14.4`, `cache-provider: basic`) |
| Modul | Single module `:engine` (pure JVM). **Belum ada** modul `:app` (Android/Compose) |

## Struktur Data Inti & Status Implementasi

| Struktur | File | Status |
|---|---|---|
| `Attribute` (17: technical/physical/mental) + `PlayerAttributes` | `model/Attributes.kt` | ✅ penuh + clamping 1..100 + tested |
| `Position` (10) + `PositionGroup` + `PositionWeights` | `model/Position.kt` | ✅ penuh + overall per posisi |
| `Player` + `Contract` + `SquadStatus` | `model/Player.kt` | ✅ data + `overall()/bestPosition()/bestOverall()`; field `fitness`/`morale` **tidak dipakai engine** |
| `Club` + `Squad` + `Finance` + `Facilities` | `model/Club.kt` | ✅ data saja (tanpa logika ekonomi) |
| `Competition` (sealed) + `League` + `Cup` | `model/Competition.kt` | ⚠️ `League` dipakai SeasonSimulator; **`Cup` tidak pernah dipakai** |
| `Calendar` + `Fixture` (model) | `model/Calendar.kt` | ⚠️ ada query helper, tapi **tidak diisi** oleh season simulation |
| `Game` (aggregate root) | `model/Game.kt` | ⚠️ didefinisikan tapi **tidak pernah dikonstruksi/dipopulasi** |
| `Team` + `Team.fromSquad()` | `simulation/Team.kt` | ✅ dipakai engine; `fromSquad` **hanya dipakai test** (orphaned) |
| `MatchEngine`/`MatchResult`/`MatchEvent`/`MatchStats`/`RandomSource` | `simulation/*.kt` | ✅ penuh + tested |
| Season: `Fixture`/`FixtureGenerator`/`StandingEntry`/`Standings`/`SeasonResult`/`SeasonSimulator` | `simulation/season/*.kt` | ✅ penuh + tested |
| Taktik / Formasi / Mentality | — | ❌ tidak ada (hanya komentar "akan ditambah") |

## Core Game Loop

**Yang sudah jalan:**
- **Match loop** — `MatchEngine.simulate(home, away)` → 18 tick × 5 menit (possession → chance → shot → goal/save/miss), home advantage ×1.05, RNG injectable (`RandomSource`). Menghasilkan `MatchResult` (skor, events, stats).
- **Season loop** — `SeasonSimulator.simulate(league, teams, startDate)` → generate round-robin (circle method) → mainkan semua fixture lewat `MatchEngine` → susun `Standings` (W=3/D=1/L=0, sort Pts→GD→GF) → `champion`.

**Yang belum ada:**
- Tidak ada loop "dunia" yang **memajukan tanggal/kalender** per matchday, tidak ada `fun main()`/entry point, dan **tidak ada seed data** (klub + pemain nyata).
- Loop beroperasi di level `Team` abstrak (angka attack/defense), **bukan** di atas `Club`+`Squad`+`Player`. Bridge-nya (`Team.fromSquad`) sudah ada tapi tidak terpasang.
- `model.Calendar` tidak terhubung ke season (season memakai `season.Fixture` sendiri).

## Sudah Jalan ✅

- **Model domain** — `Player`, `PlayerAttributes` (17 atribut), `Position`/`PositionWeights`, `Contract`, `Club`/`Squad`/`Finance`/`Facilities`, `Competition`/`League`/`Cup`, `Calendar`, `Game`. — `model/*.kt`
- **Overall per posisi** — pemain sama bisa beda rating (ST vs CB), `bestPosition()`/`bestOverall()`. — `model/Position.kt`, `model/Player.kt` + `OverallTest`
- **Match engine** — 18 tick, home advantage, chance/shot/outcome, events + stats, RNG injectable & deterministic. — `simulation/MatchEngine.kt` dkk
- **Season simulation** — single round-robin (circle method), bye untuk tim ganjil, standings 3/1/0, champion. — `simulation/season/*`
- **Test suite** — **48 test** (9 kelas: EngineTest, 3 model, 3 match-engine, 3 season) semua hijau di CI, termasuk statistical test (10k match & 300 musim).

## Setengah Jalan 🚧

- **Save/load** — `Game` aggregate + referensi berbasis id sudah disiapkan (komentar "maps cleanly onto persistence later"), tapi **belum ada serialization/Room**. Tidak ada yang bisa disimpan/dimuat.
- **Bridge Club→Squad→Team** — `Team.fromSquad(clubId, players)` ada, tapi cuma dipakai di test. `SeasonSimulator` menerima `List<Team>` mentah; tidak ada jalur `Game`/`Club`/`Squad` → simulasi.
- **Integrasi kalender** — `model.Calendar` (query `fixturesOn`/`nextFixture`) ada tapi tidak diisi oleh season; belum ada loop maju-per-tanggal.
- **Fitness/Morale** — field `fitness`/`morale` ada di `Player` tapi tidak memengaruhi `MatchEngine` (yang hanya lihat `Team.attack/defense`).
- **Contract/Finance/Facilities** — data class saja, tanpa logika (expiry, akumulasi gaji, dll).
- **Goalkeeper** — atribut GK di-approx dari atribut outfield; set atribut GK khusus (reflexes/handling) belum ada.
- **Dokumentasi stale** — `Engine.kt` masih menyebut `com.footballmanager.tactics` dan `com.footballmanager.season` sebagai package top-level, padahal season diimplementasikan sebagai `simulation.season` dan tactics belum ada.

## Belum Disentuh ❌

- **UI** (Android/Compose) — tidak ada modul `:app`.
- **Persistence** (Room atau kotlinx.serialization) — tidak ada.
- **Taktik/Formasi** (Formation, Mentality, Tempo, Pressing, dll) — nol kode.
- **Training, player development, staff** — nol kode (hanya field `trainingLevel`/`youthLevel`).
- **Transfer, kontrak/expiry, gaji, finansial klub, scouting** — nol kode (hanya field `transferBudget`/`balance`).
- **AI manager / AI transfer** — nol.
- **Youth academy, injury, retirement, player generation** — nol.
- **News/inbox** — nol.
- **Season history, records, achievements** — nol.
- **Cup competition** (bracket/turnamen) — `Cup` ada sebagai tipe data tapi tanpa logika.

## Flag TODO / Belum Selesai

Tidak ada literal `TODO`/`FIXME`, tapi ada komentar penanda kerjaan belum selesai:
- `model/Position.kt:30` — GK di-approx "until a dedicated goalkeeping set is introduced".
- `simulation/Team.kt:11,28` — "Tactics/formation modifiers will extend this later" / "not modeled yet".
- `model/Calendar.kt:5` — "Result and events are added by the match engine (Phase 2)".
- `model/Game.kt:8` — "maps cleanly onto persistence later" (persistence belum ada).
- `Engine.kt:12` — package `tactics` masih "planned".

## Estimasi MVP

- **Simulation engine (headless): ~60–65%** — match + season penuh & tested; kurang taktik, per-match player ratings, efek fitness/morale, integrasi kalender, dan persistence.
- **Game "playable MVP" (end-to-end): ~20–25%** — engine core solid, tapi **UI, save/load, seed data, dan gameplay manager (taktik/transfer/training) sama sekali belum ada**. "Playable" belum tercapai karena tidak ada entry point maupun cara memuat/menyimpan progres.

## Rekomendasi Prioritas Selanjutnya

1. **Wire end-to-end + seed data** — bangun `Game` dari data klub+pemain, ubah jadi `List<Team>` via `Team.fromSquad`, jalankan `SeasonSimulator`. Ini menutup gap terbesar: engine sudah jalan tapi belum menggerakkan "dunia" nyata.
2. **Save/load (`kotlinx.serialization` untuk `Game`)** — tanpa ini tidak ada progres yang bertahan; lebih ringan dari Room dan cocok untuk engine JVM dulu.
3. **Taktik/Formasi sebagai modifier `MatchEngine`** — titik masuk alami di `Team`/`simulate`, menambah kedalaman tanpa mengubah arsitektur.
4. **Modul `:app` + Compose** — baru masuk UI setelah item 1–2 stabil, sesuai arah awal "engine dulu, UI belakangan".
