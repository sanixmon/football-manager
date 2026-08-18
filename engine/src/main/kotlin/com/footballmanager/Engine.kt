package com.footballmanager

/**
 * Root of the pure-Kotlin football simulation engine.
 *
 * Everything in this module must be free of the Android SDK so the engine and
 * its tests run on a plain JVM — and in GitHub Actions CI without an emulator.
 *
 * Package layout:
 *   com.footballmanager.model               — Club, Player, Squad, League, Calendar, ...
 *   com.footballmanager.simulation          — MatchEngine, RandomSource, Team, Tactics, ...
 *   com.footballmanager.simulation.season   — SeasonSimulator, fixtures, standings
 *   com.footballmanager.seed                — seed data / demo world
 */
object Engine {
    const val NAME: String = "football-manager"
    const val VERSION: String = "0.1.0"
}
