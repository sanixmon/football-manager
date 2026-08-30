package com.footballmanager.usecase

import com.footballmanager.model.Attribute
import com.footballmanager.model.BidStatus
import com.footballmanager.model.Club
import com.footballmanager.model.Contract
import com.footballmanager.model.ContractOffer
import com.footballmanager.model.Finance
import com.footballmanager.model.Player
import com.footballmanager.model.PlayerAttributes
import com.footballmanager.model.Position
import com.footballmanager.model.Squad
import com.footballmanager.model.SquadStatus
import com.footballmanager.model.TransferListingStatus
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TransferUseCasesTest {

    private fun createPlayer(id: Long, rating: Int = 75, wage: Long = 3000L): Player {
        val attributes = PlayerAttributes(Attribute.entries.associateWith { rating })
        return Player(
            id = id,
            name = "Test Player $id",
            age = 24,
            nationality = "ID",
            naturalPositions = listOf(Position.ST),
            attributes = attributes,
            contract = Contract(weeklyWage = wage, expiresOn = LocalDate.of(2028, 6, 30)),
        )
    }

    private fun createClub(id: Long, name: String, budget: Long, playerIds: List<Long>): Club {
        return Club(
            id = id,
            name = name,
            shortName = name.take(3).uppercase(),
            leagueId = 1L,
            finance = Finance(balance = budget, transferBudget = budget, weeklyWageBudget = 50000L),
            squad = Squad(clubId = id, playerIds = playerIds),
        )
    }

    @Test
    fun `complete transfer flow updates squads, finances, and contracts atomically`() {
        val player = createPlayer(101L, rating = 78)
        val seller = createClub(1L, "Selling FC", budget = 2_000_000L, playerIds = listOf(101L))
        val buyer = createClub(2L, "Buying United", budget = 10_000_000L, playerIds = emptyList())
        val date = LocalDate.of(2026, 7, 15)

        // 1. Submit Bid
        val submitBidUseCase = SubmitTransferBidUseCase()
        val bid = submitBidUseCase.execute(
            bidId = 1L,
            buyingClub = buyer,
            player = player,
            sellingClubId = seller.id,
            feeOffered = 8_000_000L,
            currentDate = date,
        )
        assertEquals(BidStatus.PENDING, bid.status)

        // 2. Selling Club Evaluation
        val evaluateBidUseCase = EvaluateTransferOfferUseCase()
        val decision = evaluateBidUseCase.execute(
            bid = bid,
            player = player,
            listingStatus = TransferListingStatus.NONE,
            currentDate = date,
        )
        assertTrue(decision is ClubBidDecision.Accept || decision is ClubBidDecision.Counter)
        val clubAcceptedBid = evaluateBidUseCase.applyDecisionToBid(bid, ClubBidDecision.Accept)
        assertEquals(BidStatus.ACCEPTED_BY_CLUB, clubAcceptedBid.status)

        // 3. Player Contract Negotiation
        val negotiateContractUseCase = NegotiateContractUseCase()
        val contractOffer = ContractOffer(
            weeklyWage = 8000L,
            contractYears = 3,
            squadStatus = SquadStatus.FIRST_TEAM,
        )
        val playerDecision = negotiateContractUseCase.evaluate(player, contractOffer)
        assertEquals(PlayerContractDecision.Accept, playerDecision)
        val readyBid = negotiateContractUseCase.applyOffer(clubAcceptedBid, contractOffer, playerDecision)
        assertEquals(BidStatus.ACCEPTED_BY_PLAYER, readyBid.status)

        // 4. Complete Transfer
        val completeTransferUseCase = CompleteTransferUseCase()
        val result = completeTransferUseCase.execute(
            bid = readyBid,
            buyer = buyer,
            seller = seller,
            player = player,
            currentDate = date,
        )

        // Verifications
        assertEquals(2_000_000L, result.updatedBuyer.finance.transferBudget)
        assertTrue(101L in result.updatedBuyer.squad.playerIds)

        assertNotNull(result.updatedSeller)
        assertEquals(2_000_000L + (8_000_000L * 8 / 10), result.updatedSeller.finance.transferBudget)
        assertTrue(101L !in result.updatedSeller.squad.playerIds)

        assertEquals(8000L, result.updatedPlayer.contract.weeklyWage)
        assertEquals(SquadStatus.FIRST_TEAM, result.updatedPlayer.contract.squadStatus)
        assertEquals(BidStatus.COMPLETED, result.completedBid.status)
        assertEquals(8_000_000L, result.record.fee)
    }
}
