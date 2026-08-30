package com.footballmanager.usecase

import com.footballmanager.model.BoardObjectives
import com.footballmanager.model.PlayerSeasonStats
import com.footballmanager.model.Position
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

        // 2. Identify lineups
        val homeLineup = season.lineups[homeClubId]
        val awayLineup = season.lineups[awayClubId]

        val homeStarters = homeLineup?.starters ?: emptyList()
        val awayStarters = awayLineup?.starters ?: emptyList()

        val updatedPlayers = season.players.toMutableMap()
        val updatedStats = season.playerStats.toMutableMap()

        // 3. Process Player Conditions and Stats
        fun processTeam(
            clubId: Long,
            starters: List<Long>,
            isWin: Boolean,
            isLoss: Boolean,
            goalsScored: Int,
            concededGoals: Int,
        ) {
            // Find forwards/attackers among starters to attribute goals
            val starterPlayers = starters.mapNotNull { updatedPlayers[it] }
            val attackingStarters = starterPlayers.filter {
                it.bestPosition() == Position.ST || it.naturalPositions.any { pos -> pos.name.startsWith("W") || pos.name.startsWith("A") }
            }.ifEmpty { starterPlayers.takeLast(3) }

            var remainingGoals = goalsScored
            val goalsByPlayer = mutableMapOf<Long, Int>()
            if (attackingStarters.isNotEmpty()) {
                var idx = 0
                while (remainingGoals > 0) {
                    val p = attackingStarters[idx % attackingStarters.size]
                    goalsByPlayer[p.id] = (goalsByPlayer[p.id] ?: 0) + 1
                    remainingGoals--
                    idx++
                }
            }

            for (playerId in starters) {
                val player = updatedPlayers[playerId] ?: continue
                val goals = goalsByPlayer[playerId] ?: 0
                val isGkOrDef = player.bestPosition() == Position.GK || player.naturalPositions.any { it.name.endsWith("B") }
                val cleanSheet = isGkOrDef && concededGoals == 0

                // Fatigue & Morale update
                val newFitness = (player.fitness - 14).coerceIn(1, 100)
                val moraleDelta = if (isWin) +6 else if (isLoss) -4 else +1
                val newMorale = (player.morale + moraleDelta).coerceIn(1, 100)
                updatedPlayers[playerId] = player.copy(fitness = newFitness, morale = newMorale)

                // Match rating formula
                val baseRating = if (isWin) 6.8 else if (isLoss) 5.8 else 6.2
                val matchRating = (baseRating + (goals * 1.2) + (if (cleanSheet) 0.6 else 0.0))
                    .coerceIn(4.0, 10.0)

                val existingStat = updatedStats[playerId] ?: PlayerSeasonStats(playerId = playerId)
                updatedStats[playerId] = existingStat.withMatchAppearance(
                    isStarter = true,
                    goalsScored = goals,
                    assistsMade = 0,
                    cleanSheet = cleanSheet,
                    matchRating = matchRating,
                    yellow = false,
                    red = false,
                )
            }
        }

        processTeam(homeClubId, homeStarters, isHomeWin, isAwayWin, homeGoals, awayGoals)
        processTeam(awayClubId, awayStarters, isAwayWin, isHomeWin, awayGoals, homeGoals)

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
