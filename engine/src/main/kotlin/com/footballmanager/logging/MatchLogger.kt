package com.footballmanager.logging

import com.footballmanager.simulation.MatchEvent
import com.footballmanager.simulation.MatchResult
import com.footballmanager.simulation.Team

/**
 * Structured observer/logger for match simulation events and timeline tracing.
 */
interface MatchLogger {
    fun onMatchStart(home: Team, away: Team)
    fun onTickEvent(tick: Int, event: MatchEvent)
    fun onMatchEnd(result: MatchResult)
}

object NoOpMatchLogger : MatchLogger {
    override fun onMatchStart(home: Team, away: Team) {}
    override fun onTickEvent(tick: Int, event: MatchEvent) {}
    override fun onMatchEnd(result: MatchResult) {}
}

class InMemoryMatchLogger : MatchLogger {
    val logs = mutableListOf<String>()

    override fun onMatchStart(home: Team, away: Team) {
        logs.add("Match started: Club #${home.clubId} vs Club #${away.clubId}")
    }

    override fun onTickEvent(tick: Int, event: MatchEvent) {
        logs.add("Minute ${event.minute}: ${event.type} by ${event.side}")
    }

    override fun onMatchEnd(result: MatchResult) {
        logs.add("Match ended: Club #${result.homeClubId} ${result.homeScore} - ${result.awayScore} Club #${result.awayClubId}")
    }
}
