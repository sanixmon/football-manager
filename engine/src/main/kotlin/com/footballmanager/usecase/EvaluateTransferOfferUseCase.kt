package com.footballmanager.usecase

import com.footballmanager.calculator.TransferValuationCalculator
import com.footballmanager.model.BidStatus
import com.footballmanager.model.Player
import com.footballmanager.model.TransferBid
import com.footballmanager.model.TransferListingStatus
import java.time.LocalDate

sealed class ClubBidDecision {
    data object Accept : ClubBidDecision()
    data class Counter(val counterFee: Long) : ClubBidDecision()
    data object Reject : ClubBidDecision()
}

class EvaluateTransferOfferUseCase {

    fun execute(
        bid: TransferBid,
        player: Player,
        listingStatus: TransferListingStatus = TransferListingStatus.NONE,
        currentDate: LocalDate = bid.dateSubmitted,
    ): ClubBidDecision {
        if (bid.sellingClubId == null) {
            return ClubBidDecision.Accept // Free agents require no club fee agreement
        }

        val marketValue = TransferValuationCalculator.calculateMarketValue(
            player = player,
            listingStatus = listingStatus,
            currentDate = currentDate,
        )

        val minAcceptableRatio = if (listingStatus == TransferListingStatus.TRANSFER_LISTED) 0.85 else 1.05
        val minCounterRatio = if (listingStatus == TransferListingStatus.TRANSFER_LISTED) 0.70 else 0.80

        val minAcceptableFee = (marketValue * minAcceptableRatio).toLong()
        val minCounterFee = (marketValue * minCounterRatio).toLong()

        return when {
            bid.feeOffered >= minAcceptableFee -> ClubBidDecision.Accept
            bid.feeOffered >= minCounterFee -> ClubBidDecision.Counter(minAcceptableFee)
            else -> ClubBidDecision.Reject
        }
    }

    fun applyDecisionToBid(bid: TransferBid, decision: ClubBidDecision): TransferBid {
        return when (decision) {
            is ClubBidDecision.Accept -> bid.copy(status = BidStatus.ACCEPTED_BY_CLUB)
            is ClubBidDecision.Counter -> bid.copy(status = BidStatus.PENDING, feeOffered = decision.counterFee)
            is ClubBidDecision.Reject -> bid.copy(status = BidStatus.REJECTED_BY_CLUB)
        }
    }
}
