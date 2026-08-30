package com.footballmanager.usecase

import com.footballmanager.calculator.WageExpectationCalculator
import com.footballmanager.model.BidStatus
import com.footballmanager.model.ContractOffer
import com.footballmanager.model.Player
import com.footballmanager.model.TransferBid

sealed class PlayerContractDecision {
    data object Accept : PlayerContractDecision()
    data class Demand(val demandedWage: Long) : PlayerContractDecision()
    data object Reject : PlayerContractDecision()
}

class NegotiateContractUseCase {

    fun evaluate(
        player: Player,
        offer: ContractOffer,
    ): PlayerContractDecision {
        val expectedWage = WageExpectationCalculator.calculateExpectedWage(
            player = player,
            promisedStatus = offer.squadStatus,
        )

        val minAcceptableWage = (expectedWage * 0.90).toLong()
        val minCounterWage = (expectedWage * 0.75).toLong()

        return when {
            offer.weeklyWage >= minAcceptableWage -> PlayerContractDecision.Accept
            offer.weeklyWage >= minCounterWage -> PlayerContractDecision.Demand(expectedWage)
            else -> PlayerContractDecision.Reject
        }
    }

    fun applyOffer(
        bid: TransferBid,
        offer: ContractOffer,
        decision: PlayerContractDecision,
    ): TransferBid {
        val newStatus = when (decision) {
            is PlayerContractDecision.Accept -> BidStatus.ACCEPTED_BY_PLAYER
            is PlayerContractDecision.Demand -> BidStatus.TERMS_OFFERED
            is PlayerContractDecision.Reject -> BidStatus.REJECTED_BY_PLAYER
        }
        return bid.copy(status = newStatus, contractOffer = offer)
    }
}
