package com.footballmanager.usecase

import com.footballmanager.model.Position
import com.footballmanager.simulation.season.SeasonState
import kotlin.random.Random

class AiTransferActivityUseCase(
    private val directTransferUseCase: DirectTransferUseCase = DirectTransferUseCase(),
) {

    fun execute(season: SeasonState, seed: Long = 42L): SeasonState {
        // AI clubs (exclude humanClubId)
        val aiClubs = season.clubs.values.filter { it.id != season.humanClubId }
        if (aiClubs.size < 2) return season

        val random = Random(seed)

        // Pick an AI selling club that has a surplus squad (> 16 players)
        val seller = aiClubs.filter { it.squad.size > 16 }.randomOrNull(random) ?: return season

        // Pick a non-GK outfield player from seller's bench/depth
        val sellerPlayers = seller.squad.playerIds.mapNotNull { season.players[it] }
        val candidate = sellerPlayers.filter { Position.GK !in it.naturalPositions && !it.isInjured && !it.isSuspended }
            .sortedBy { it.bestOverall() }
            .take(3)
            .randomOrNull(random) ?: return season

        // Pick an AI buyer with budget that has <= 22 players
        val fee = (candidate.bestOverall() * 10_000L).coerceAtLeast(50_000L)
        val buyer = aiClubs.filter { it.id != seller.id && it.finance.balance >= fee && it.squad.size <= 22 }
            .randomOrNull(random) ?: return season

        return try {
            directTransferUseCase.execute(
                season = season,
                request = DirectTransferRequest(
                    playerId = candidate.id,
                    fromClubId = seller.id,
                    toClubId = buyer.id,
                    transferFee = fee,
                ),
            )
        } catch (_: Exception) {
            season
        }
    }
}
