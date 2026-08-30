package com.footballmanager.usecase

import com.footballmanager.model.BidStatus
import com.footballmanager.model.Club
import com.footballmanager.model.Player
import com.footballmanager.model.TransferBid
import java.time.LocalDate

class SubmitTransferBidUseCase {

    fun execute(
        bidId: Long,
        buyingClub: Club,
        player: Player,
        sellingClubId: Long?,
        feeOffered: Long,
        currentDate: LocalDate,
    ): TransferBid {
        require(feeOffered >= 0) { "Transfer fee offered cannot be negative" }
        require(buyingClub.finance.transferBudget >= feeOffered) {
            "Insufficient transfer budget: required $feeOffered, available ${buyingClub.finance.transferBudget}"
        }

        return TransferBid(
            id = bidId,
            playerId = player.id,
            buyingClubId = buyingClub.id,
            sellingClubId = sellingClubId,
            feeOffered = feeOffered,
            status = if (sellingClubId == null) BidStatus.ACCEPTED_BY_CLUB else BidStatus.PENDING,
            dateSubmitted = currentDate,
        )
    }
}
