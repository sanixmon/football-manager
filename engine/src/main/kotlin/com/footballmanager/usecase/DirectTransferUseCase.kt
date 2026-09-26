package com.footballmanager.usecase

import com.footballmanager.model.Club
import com.footballmanager.model.Player
import com.footballmanager.model.Squad
import com.footballmanager.model.TransferRecord
import com.footballmanager.simulation.Lineup
import com.footballmanager.simulation.season.SeasonState

data class DirectTransferRequest(
    val playerId: Long,
    val fromClubId: Long,
    val toClubId: Long,
    val transferFee: Long = 0L,
)

class DirectTransferUseCase {

    fun execute(
        season: SeasonState,
        request: DirectTransferRequest,
    ): SeasonState {
        val player = season.players[request.playerId]
            ?: throw IllegalArgumentException("Invalid player ID: ${request.playerId}")

        val fromClub = season.clubs[request.fromClubId]
            ?: throw IllegalArgumentException("Invalid source club ID: ${request.fromClubId}")

        val toClub = season.clubs[request.toClubId]
            ?: throw IllegalArgumentException("Invalid destination club ID: ${request.toClubId}")

        require(request.fromClubId != request.toClubId) {
            "Source and destination clubs must be different"
        }

        require(player.id in fromClub.squad.playerIds) {
            "Player ${player.name} (id=${player.id}) is not registered to source club ${fromClub.name} (id=${fromClub.id})"
        }

        require(player.id !in toClub.squad.playerIds) {
            "Player ${player.name} (id=${player.id}) is already registered to destination club ${toClub.name} (id=${toClub.id})"
        }

        require(request.transferFee >= 0L) {
            "Transfer fee cannot be negative: ${request.transferFee}"
        }

        require(toClub.finance.balance >= request.transferFee) {
            "Destination club ${toClub.name} has insufficient balance (${toClub.finance.balance}) for transfer fee (${request.transferFee})"
        }

        // Update finances
        val updatedToClubFinance = toClub.finance.copy(
            balance = toClub.finance.balance - request.transferFee,
            transferBudget = (toClub.finance.transferBudget - request.transferFee).coerceAtLeast(0L),
        )
        val updatedFromClubFinance = fromClub.finance.copy(
            balance = fromClub.finance.balance + request.transferFee,
            transferBudget = fromClub.finance.transferBudget + request.transferFee,
        )

        // Move player from source squad to destination squad
        val updatedFromClub = fromClub.copy(
            finance = updatedFromClubFinance,
            squad = Squad(clubId = fromClub.id, playerIds = fromClub.squad.playerIds - player.id),
        )
        val updatedToClub = toClub.copy(
            finance = updatedToClubFinance,
            squad = Squad(clubId = toClub.id, playerIds = toClub.squad.playerIds + player.id),
        )

        val updatedClubs = season.clubs + mapOf(
            updatedFromClub.id to updatedFromClub,
            updatedToClub.id to updatedToClub,
        )

        // Clean up fromClub lineup if it contained transferred player
        val updatedLineups = season.lineups.toMutableMap()
        val oldLineup = updatedLineups[fromClub.id]
        if (oldLineup != null && (player.id in oldLineup.starters || player.id in oldLineup.substitutes)) {
            val remainingSquad = updatedFromClub.squad.playerIds.mapNotNull { season.players[it] }
            if (remainingSquad.size >= 11) {
                updatedLineups[fromClub.id] = Lineup.autoSelect(
                    players = remainingSquad,
                    tactics = fromClub.defaultTactics ?: season.teams.firstOrNull { it.clubId == fromClub.id }?.tactics ?: com.footballmanager.simulation.Tactics(),
                    clubId = fromClub.id,
                )
            } else {
                updatedLineups.remove(fromClub.id)
            }
        }

        // Append to transferHistory
        val nextRecordId = (season.transferHistory.maxOfOrNull { it.id } ?: 0L) + 1L
        val transferRecord = TransferRecord(
            id = nextRecordId,
            playerId = player.id,
            playerName = player.name,
            fromClubId = fromClub.id,
            toClubId = toClub.id,
            fee = request.transferFee,
            date = season.currentDate,
        )

        return season.copy(
            clubs = updatedClubs,
            lineups = updatedLineups,
            transferHistory = season.transferHistory + transferRecord,
        )
    }
}
