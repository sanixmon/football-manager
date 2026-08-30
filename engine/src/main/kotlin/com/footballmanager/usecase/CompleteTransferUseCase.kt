package com.footballmanager.usecase

import com.footballmanager.model.BidStatus
import com.footballmanager.model.Club
import com.footballmanager.model.Contract
import com.footballmanager.model.Player
import com.footballmanager.model.Squad
import com.footballmanager.model.TransferBid
import com.footballmanager.model.TransferRecord
import java.time.LocalDate

data class CompleteTransferResult(
    val updatedBuyer: Club,
    val updatedSeller: Club?,
    val updatedPlayer: Player,
    val completedBid: TransferBid,
    val record: TransferRecord,
)

class CompleteTransferUseCase {

    fun execute(
        bid: TransferBid,
        buyer: Club,
        seller: Club?,
        player: Player,
        currentDate: LocalDate,
    ): CompleteTransferResult {
        val offer = bid.contractOffer ?: error("Cannot complete transfer without contract offer terms")
        require(buyer.finance.transferBudget >= bid.feeOffered) {
            "Buyer ${buyer.name} cannot afford fee ${bid.feeOffered}"
        }

        // 1. Update buyer finance & squad
        val buyerNewTransferBudget = buyer.finance.transferBudget - bid.feeOffered
        val buyerNewBalance = buyer.finance.balance - bid.feeOffered
        val buyerNewSquadPlayerIds = if (player.id in buyer.squad.playerIds) {
            buyer.squad.playerIds
        } else {
            buyer.squad.playerIds + player.id
        }
        val updatedBuyer = buyer.copy(
            finance = buyer.finance.copy(
                balance = buyerNewBalance,
                transferBudget = buyerNewTransferBudget,
            ),
            squad = Squad(clubId = buyer.id, playerIds = buyerNewSquadPlayerIds),
        )

        // 2. Update seller finance & squad (if not free agent)
        val updatedSeller = seller?.let { s ->
            val sellShare = (bid.feeOffered * 0.80).toLong()
            val sellerNewTransferBudget = s.finance.transferBudget + sellShare
            val sellerNewBalance = s.finance.balance + bid.feeOffered
            val sellerNewSquadPlayerIds = s.squad.playerIds - player.id
            s.copy(
                finance = s.finance.copy(
                    balance = sellerNewBalance,
                    transferBudget = sellerNewTransferBudget,
                ),
                squad = Squad(clubId = s.id, playerIds = sellerNewSquadPlayerIds),
            )
        }

        // 3. Update player contract
        val newContract = Contract(
            weeklyWage = offer.weeklyWage,
            expiresOn = currentDate.plusYears(offer.contractYears.toLong()),
            squadStatus = offer.squadStatus,
        )
        val updatedPlayer = player.copy(contract = newContract)

        // 4. Create record & final bid state
        val record = TransferRecord(
            id = bid.id,
            playerId = player.id,
            playerName = player.name,
            fromClubId = seller?.id,
            toClubId = buyer.id,
            fee = bid.feeOffered,
            date = currentDate,
        )
        val completedBid = bid.copy(status = BidStatus.COMPLETED)

        return CompleteTransferResult(
            updatedBuyer = updatedBuyer,
            updatedSeller = updatedSeller,
            updatedPlayer = updatedPlayer,
            completedBid = completedBid,
            record = record,
        )
    }
}
