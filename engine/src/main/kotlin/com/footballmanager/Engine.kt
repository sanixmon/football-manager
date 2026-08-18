package com.footballmanager

/**
 * Root of the pure-Kotlin football simulation engine.
 *
 * Everything in this module must be free of the Android SDK so the engine and
 * its tests run on a plain JVM — and in GitHub Actions CI without an emulator.
 *
 * Planned package layout:
 *   com.footballmanager.model      — Club, Player, Squad, League, Calendar, ...
 *   com.footballmanager.simulation — MatchEngine, SeasonSimulator, ...
 *   com.footballmanager.tactics    — Formation, Mentality, TacticsModifier, ...
 *   com.footballmanager.season     — League tables, fixtures, schedule, ...
 */
object Engine {
    const val NAME: String = "football-manager"
    const val VERSION: String = "0.1.0"
}
