package com.footballmanager.usecase

import com.footballmanager.model.BoardObjectives
import com.footballmanager.model.InjuryStatus
import com.footballmanager.model.PlayerSeasonStats
import com.footballmanager.model.SuspensionStatus
import com.footballmanager.simulation.MatchEventType
import com.footballmanager.simulation.MatchResult
import com.footballmanager.simulation.season.SeasonState

class ApplyMatchResultToWorldUseCase {

    fun execute(
        season: SeasonState,
        matchResult: MatchResult,
    ): SeasonState {
        val homeClubId = matchResult.homeClubId
        val awayClubId = matchResult.awayClubId

        val homeGoals = matchResult.homeScore
        val awayGoals = matchResult.awayScore

        val isHomeWin = homeGoals > awayGoals
        val isAwayWin = awayGoals > homeGoals
        val isDraw = homeGoals == awayGoals

        // 1. Update Home Club Gate Receipts
        val updatedClubs = season.clubs.toMutableMap()
        val homeClub = updatedClubs[homeClubId]
        if (homeClub != null) {
            val gateReceipts = 75_000L
            val updatedHomeFinance = homeClub.finance.copy(
                balance = homeClub.finance.balance + gateReceipts,
            )
            updatedClubs[homeClubId] = homeClub.copy(finance = updatedHomeFinance)
        }

        val updatedPlayers = season.players.toMutableMap()
        val updatedStats = season.playerStats.toMutableMap()

        // 2. Identify club squads
        val homeSquadIds = updatedClubs[homeClubId]?.squad?.playerIds ?: emptyList()
        val awaySquadIds = updatedClubs[awayClubId]?.squad?.playerIds ?: emptyList()
        val participatingClubSquadIds = (homeSquadIds + awaySquadIds).toSet()

        // 3. Process each player belonging to participating clubs
        for (playerId in participatingClubSquadIds) {
            val player = updatedPlayers[playerId] ?: continue
            val isHome = playerId in homeSquadIds
            val isWin = if (isHome) isHomeWin else isAwayWin
            val isLoss = if (isHome) isAwayWin else isHomeWin
            val pMatchStats = matchResult.playerStats[playerId]

            val isParticipant = if (matchResult.playerStats.isNotEmpty()) {
                pMatchStats != null && pMatchStats.minutesPlayed > 0
            } else {
                val homeStarters = season.lineups[homeClubId]?.starters ?: homeSquadIds.take(11)
                val awayStarters = season.lineups[awayClubId]?.starters ?: awaySquadIds.take(11)
                homeStarters.contains(playerId) || awayStarters.contains(playerId)
            }

            if (isParticipant) {
                // ── PARTICIPATING PLAYER ──────────────────────────────────────
                val currentSeasonStat = updatedStats[playerId] ?: PlayerSeasonStats(playerId = playerId)
                val rating = matchResult.playerRatings[playerId] ?: 6.5
                val goals = pMatchStats?.goals ?: 0
                val assists = pMatchStats?.assists ?: 0
                val yellow = (pMatchStats?.yellowCards ?: 0) > 0
                val red = (pMatchStats?.redCards ?: 0) > 0
                val isStarter = pMatchStats?.isStarter ?: true
                val isInjured = pMatchStats?.isInjured ?: false

                // Card classification in match
                val yellowCardCount = pMatchStats?.yellowCards ?: 0
                val hasDoubleYellowRed = yellowCardCount >= 2 || matchResult.events.any {
                    it.type == MatchEventType.RED_CARD && it.playerId == playerId &&
                        it.description.contains("Second yellow", ignoreCase = true)
                }
                val hasStraightRed = red && !hasDoubleYellowRed
                // Yellow accumulation logic
                var newUnservedYellows = currentSeasonStat.unservedYellowCards
                var suspensionToApply: SuspensionStatus? = null

                if (hasStraightRed) {
                    suspensionToApply = SuspensionStatus(matchesRemaining = 2, reason = "Straight red card")
                } else if (hasDoubleYellowRed) {
                    suspensionToApply = SuspensionStatus(matchesRemaining = 1, reason = "Two yellow cards")
                } else if (yellow) {
                    newUnservedYellows += (pMatchStats?.yellowCards ?: 1)
                    if (newUnservedYellows >= 5) {
                        suspensionToApply = SuspensionStatus(matchesRemaining = 1, reason = "Yellow card accumulation")
                        newUnservedYellows -= 5
                    }
                }

                // Update Season Stats
                val cleanSheet = (player.naturalPositions.any { it.name.endsWith("B") || it.name == "GK" }) &&
                    (if (isHome) awayGoals == 0 else homeGoals == 0)

                updatedStats[playerId] = currentSeasonStat.withMatchAppearance(
                    isStarter = isStarter,
                    goalsScored = goals,
                    assistsMade = assists,
                    cleanSheet = cleanSheet,
                    matchRating = rating,
                    yellow = yellow,
                    red = red,
                    newUnservedYellowCards = newUnservedYellows,
                )

                // Form progression (bounded 1..100)
                val formDelta = when {
                    rating >= 7.5 -> +4
                    rating >= 7.0 -> +2
                    rating < 5.5 -> -4
                    rating < 6.2 -> -2
                    else -> 0
                }
                val newForm = (player.form + formDelta).coerceIn(1, 100)

                // Fitness & Morale
                val fitnessDrain = if (isStarter) 12 else 6
                val newFitness = (player.fitness - fitnessDrain).coerceIn(1, 100)
                val moraleDelta = if (isWin) +6 else if (isLoss) -4 else +1
                val newMorale = (player.morale + moraleDelta).coerceIn(1, 100)

                // Injury update
                val newInjury = if (isInjured) {
                    InjuryStatus(description = "Match Injury", daysRemaining = 14)
                } else {
                    player.injuryStatus
                }

                // Apply suspension if incurred (active for future matches)
                val newSuspension = suspensionToApply ?: player.suspensionStatus

                updatedPlayers[playerId] = player.copy(
                    fitness = newFitness,
                    morale = newMorale,
                    form = newForm,
                    injuryStatus = newInjury,
                    suspensionStatus = newSuspension,
                )
            } else {
                // ── NON-PARTICIPATING PLAYER ──────────────────────────────────
                var updatedP = player

                // Suspension Decrement: If player missed this match due to an existing suspension
                if (updatedP.suspensionStatus != null && updatedP.suspensionStatus!!.matchesRemaining > 0) {
                    val remaining = updatedP.suspensionStatus!!.matchesRemaining - 1
                    updatedP = if (remaining <= 0) {
                        updatedP.copy(suspensionStatus = null)
                    } else {
                        updatedP.copy(suspensionStatus = updatedP.suspensionStatus!!.copy(matchesRemaining = remaining))
                    }
                }

                // Injury recovery countdown: 7 days per matchday
                if (updatedP.injuryStatus != null) {
                    val remainingDays = (updatedP.injuryStatus!!.daysRemaining - 7).coerceAtLeast(0)
                    updatedP = if (remainingDays <= 0) {
                        updatedP.copy(injuryStatus = null)
                    } else {
                        updatedP.copy(injuryStatus = updatedP.injuryStatus!!.copy(daysRemaining = remainingDays))
                    }
                }

                // Natural fitness recovery for resting players
                if (updatedP.fitness < 100) {
                    updatedP = updatedP.copy(fitness = (updatedP.fitness + 12).coerceAtMost(100))
                }

                // Unused substitutes / resting players do NOT receive artificial form/morale changes
                updatedPlayers[playerId] = updatedP
            }
        }

        // 4. Update Manager Profile if human club participated
        val humanClubId = season.humanClubId
        val updatedManager = season.managerProfile?.let { mgr ->
            if (humanClubId == homeClubId || humanClubId == awayClubId) {
                val humanWon = (humanClubId == homeClubId && isHomeWin) || (humanClubId == awayClubId && isAwayWin)
                mgr.withMatchResult(isWin = humanWon, isDraw = isDraw)
            } else {
                mgr
            }
        }

        // 5. Update Board Objectives
        val updatedBoard = season.boardObjectives.toMutableMap()
        if (humanClubId != null && (humanClubId == homeClubId || humanClubId == awayClubId)) {
            val userPosition = season.standings.entries.indexOfFirst { it.team.clubId == humanClubId } + 1
            val humanWon = (humanClubId == homeClubId && isHomeWin) || (humanClubId == awayClubId && isAwayWin)
            val humanLost = (humanClubId == homeClubId && isAwayWin) || (humanClubId == awayClubId && isHomeWin)
            val currentObj = updatedBoard[humanClubId] ?: BoardObjectives(clubId = humanClubId, targetLeaguePosition = 4)
            updatedBoard[humanClubId] = currentObj.withMatchEvaluation(
                currentPosition = if (userPosition > 0) userPosition else 4,
                isWin = humanWon,
                isLoss = humanLost,
            )
        }

        return season.copy(
            clubs = updatedClubs,
            players = updatedPlayers,
            playerStats = updatedStats,
            boardObjectives = updatedBoard,
            managerProfile = updatedManager,
        )
    }
}
