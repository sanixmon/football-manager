package com.footballmanager.logging

import com.footballmanager.simulation.MatchEvent
import com.footballmanager.simulation.MatchResult
import com.footballmanager.simulation.Team

enum class LogLevel { DEBUG, INFO, WARN, ERROR }

/**
 * Structured observer/logger for match simulation events and timeline tracing.
 */
interface MatchLogger {
    val isEnabled: Boolean get() = true

    fun onMatchStart(home: Team, away: Team)
    fun onTickEvent(tick: Int, event: MatchEvent)
    fun onMatchEnd(result: MatchResult)
    fun logEvent(level: LogLevel, messageSupplier: () -> String) {}
}

object NoOpMatchLogger : MatchLogger {
    override val isEnabled: Boolean = false

    override fun onMatchStart(home: Team, away: Team) {}
    override fun onTickEvent(tick: Int, event: MatchEvent) {}
    override fun onMatchEnd(result: MatchResult) {}
    override fun logEvent(level: LogLevel, messageSupplier: () -> String) {}
}

class InMemoryMatchLogger(
    override val isEnabled: Boolean = true,
) : MatchLogger {
    val logs = mutableListOf<String>()

    override fun onMatchStart(home: Team, away: Team) {
        if (!isEnabled) return
        logs.add("Match started: Club #${home.clubId} vs Club #${away.clubId}")
    }

    override fun onTickEvent(tick: Int, event: MatchEvent) {
        if (!isEnabled) return
        logs.add("Minute ${event.minute}: ${event.type} by ${event.side}")
    }

    override fun onMatchEnd(result: MatchResult) {
        if (!isEnabled) return
        logs.add("Match ended: Club #${result.homeClubId} ${result.homeScore} - ${result.awayScore} Club #${result.awayClubId}")
    }

    override fun logEvent(level: LogLevel, messageSupplier: () -> String) {
        if (!isEnabled) return
        logs.add("[$level] ${messageSupplier()}")
    }
}

