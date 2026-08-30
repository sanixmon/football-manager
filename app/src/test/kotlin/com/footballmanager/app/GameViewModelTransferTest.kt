package com.footballmanager.app

import com.footballmanager.app.ui.viewmodel.GameViewModel
import com.footballmanager.model.BidStatus
import com.footballmanager.model.SquadStatus
import com.footballmanager.repository.InMemoryGameRepository
import com.footballmanager.seed.SeedData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GameViewModelTransferTest {

    @Test
    fun `submitting transfer bid, offering contract, and completing transfer updates squad and finances`() {
        val game = SeedData.game()
        val repo = InMemoryGameRepository(game)
        val vm = GameViewModel(gameRepository = repo)

        val targetPlayer = game.players.values.first { it.id !in vm.uiState.value.humanSquad.map { p -> p.id } }
        val targetPlayerId = targetPlayer.id
        val initialBudget = vm.uiState.value.humanFinance.transferBudget

        // 1. Submit Bid
        vm.submitTransferBid(playerId = targetPlayerId, feeOffered = 1_000_000L)
        val activeBids = vm.uiState.value.activeBids
        assertEquals(1, activeBids.size)
        val bid = activeBids.first()
        assertEquals(targetPlayerId, bid.playerId)

        // 2. Offer Contract
        vm.offerContractTerms(
            bidId = bid.id,
            weeklyWage = 10_000L,
            years = 3,
            squadStatus = SquadStatus.FIRST_TEAM,
        )
        val updatedBid = vm.uiState.value.activeBids.first()
        assertNotNull(updatedBid.contractOffer)

        // 3. Complete Deal (if accepted)
        if (updatedBid.status == BidStatus.ACCEPTED_BY_PLAYER) {
            vm.completeTransferDeal(bid.id)
            assertTrue(vm.uiState.value.activeBids.isEmpty())
            assertEquals(1, vm.uiState.value.transferHistory.size)
            assertTrue(targetPlayerId in vm.uiState.value.humanSquad.map { it.id })
            assertEquals(initialBudget - 1_000_000L, vm.uiState.value.humanFinance.transferBudget)
        }
    }
}
