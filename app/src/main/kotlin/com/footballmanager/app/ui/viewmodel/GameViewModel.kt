package com.footballmanager.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.footballmanager.app.ui.components.FmNavSection
import com.footballmanager.app.ui.components.FmSquadTab
import com.footballmanager.model.Game
import com.footballmanager.model.League
import com.footballmanager.repository.GameRepository
import com.footballmanager.repository.InMemoryGameRepository
import com.footballmanager.seed.SeedData
import com.footballmanager.simulation.Formation
import com.footballmanager.simulation.Lineup
import com.footballmanager.simulation.Mentality
import com.footballmanager.simulation.season.SeasonRunner
import com.footballmanager.model.SquadStatus
import com.footballmanager.model.ContractOffer
import com.footballmanager.usecase.AdvanceDayUseCase
import com.footballmanager.usecase.ApplyMatchResultToWorldUseCase
import com.footballmanager.usecase.CalculateTeamStrengthUseCase
import com.footballmanager.usecase.CompleteTransferUseCase
import com.footballmanager.usecase.EvaluateTransferOfferUseCase
import com.footballmanager.usecase.NegotiateContractUseCase
import com.footballmanager.usecase.SeasonRolloverUseCase
import com.footballmanager.usecase.SelectLineupUseCase
import com.footballmanager.usecase.SubmitTransferBidUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameViewModel(
    private val gameRepository: GameRepository = InMemoryGameRepository(SeedData.game()),
    private val runner: SeasonRunner = SeasonRunner(),
    private val selectLineupUseCase: SelectLineupUseCase = SelectLineupUseCase(),
    private val submitTransferBidUseCase: SubmitTransferBidUseCase = SubmitTransferBidUseCase(),
    private val evaluateTransferOfferUseCase: EvaluateTransferOfferUseCase = EvaluateTransferOfferUseCase(),
    private val negotiateContractUseCase: NegotiateContractUseCase = NegotiateContractUseCase(),
    private val completeTransferUseCase: CompleteTransferUseCase = CompleteTransferUseCase(),
    private val applyMatchResultToWorldUseCase: ApplyMatchResultToWorldUseCase = ApplyMatchResultToWorldUseCase(),
    private val advanceDayUseCase: AdvanceDayUseCase = AdvanceDayUseCase(),
    private val seasonRolloverUseCase: SeasonRolloverUseCase = SeasonRolloverUseCase(),
) : ViewModel() {

    /** Backward-compatible convenience constructor. */
    constructor(
        initialGame: Game,
        runner: SeasonRunner = SeasonRunner(),
        selectLineupUseCase: SelectLineupUseCase = SelectLineupUseCase(),
    ) : this(
        gameRepository = InMemoryGameRepository(initialGame),
        runner = runner,
        selectLineupUseCase = selectLineupUseCase,
    )

    private val _uiState: MutableStateFlow<GameUiState>

    init {
        val game = gameRepository.getGame()
        val league = game.competitions.values.filterIsInstance<League>().firstOrNull()
            ?: (game.competitions[SeedData.LEAGUE_ID] as? League)
            ?: error("No League competition found in Game")
        val teams = SeedData.teams(game)
        val season = game.currentSeason ?: runner.start(
            league = league,
            teams = teams,
            startDate = game.currentDate,
            humanClubId = 1L,
            clubs = game.clubs,
            players = game.players,
        )
        val initialGame = if (game.currentSeason == null) {
            val updated = game.copy(currentSeason = season)
            gameRepository.saveGame(updated)
            updated
        } else {
            game
        }
        val lastResult = season.results.lastOrNull {
            it.homeClubId == 1L || it.awayClubId == 1L
        }
        _uiState = MutableStateFlow(
            GameUiState(
                game = initialGame,
                currentSeason = season,
                humanClubId = 1L,
                lastMatchResult = lastResult,
            ),
        )
    }

    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    // ── App Screen Navigation & Career Management ─────────────────────────
    fun goToAppScreen(screen: AppScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun startNewCareer(managerName: String, nationality: String, clubId: Long) {
        val freshGame = SeedData.game()
        val league = freshGame.competitions.values.filterIsInstance<League>().firstOrNull()
            ?: (freshGame.competitions[SeedData.LEAGUE_ID] as? League)
            ?: error("No League competition found in Game")
        val teams = SeedData.teams(freshGame)
        val initialSeason = runner.start(
            league = league,
            teams = teams,
            startDate = freshGame.currentDate,
            humanClubId = clubId,
            clubs = freshGame.clubs,
            players = freshGame.players,
        ).copy(
            managerProfile = com.footballmanager.model.ManagerProfile(
                name = managerName,
                nationality = nationality,
                clubId = clubId,
            ),
            boardObjectives = mapOf(clubId to com.footballmanager.model.BoardObjectives(clubId = clubId, targetLeaguePosition = 4)),
        )

        val updatedGame = freshGame.copy(currentSeason = initialSeason)
        gameRepository.saveGame(updatedGame)
        _uiState.update {
            it.copy(
                game = updatedGame,
                currentSeason = initialSeason,
                humanClubId = clubId,
                currentScreen = AppScreen.IN_GAME,
                activeNavSection = FmNavSection.HOME,
                lastMatchResult = null,
                selectedStarterPlayerId = null,
            )
        }
    }

    // ── Navigation ────────────────────────────────────────────────────────
    fun navigateTo(section: FmNavSection) {
        _uiState.update { it.copy(activeNavSection = section) }
    }

    fun selectSquadTab(tab: FmSquadTab) {
        _uiState.update { it.copy(activeSquadTab = tab) }
    }

    // ── Tactics ───────────────────────────────────────────────────────────
    fun updateFormation(formation: Formation) {
        _uiState.update { state ->
            val newTactics = state.humanTeam.tactics.copy(formation = formation)
            val updatedSeason = state.currentSeason.setTactics(state.humanClubId, newTactics)
            val newLineup = selectLineupUseCase(state.humanSquad, newTactics)
            val finalSeason = updatedSeason.setLineup(state.humanClubId, newLineup)
            val updatedGame = state.game.copy(currentSeason = finalSeason)
            gameRepository.saveGame(updatedGame)
            state.copy(
                game = updatedGame,
                currentSeason = finalSeason,
                selectedStarterPlayerId = null,
            )
        }
    }

    fun updateMentality(mentality: Mentality) {
        _uiState.update { state ->
            val newTactics = state.humanTeam.tactics.copy(mentality = mentality)
            val updatedSeason = state.currentSeason.setTactics(state.humanClubId, newTactics)
            val updatedGame = state.game.copy(currentSeason = updatedSeason)
            gameRepository.saveGame(updatedGame)
            state.copy(
                game = updatedGame,
                currentSeason = updatedSeason,
            )
        }
    }

    fun autoSelectBestXI() {
        _uiState.update { state ->
            val newLineup = selectLineupUseCase(state.humanSquad, state.humanTeam.tactics)
            val updatedSeason = state.currentSeason.setLineup(state.humanClubId, newLineup)
            val updatedGame = state.game.copy(currentSeason = updatedSeason)
            gameRepository.saveGame(updatedGame)
            state.copy(
                game = updatedGame,
                currentSeason = updatedSeason,
                selectedStarterPlayerId = null,
            )
        }
    }

    fun onStarterSelected(playerId: Long) {
        _uiState.update { state ->
            val newSelection = if (state.selectedStarterPlayerId == playerId) null else playerId
            state.copy(selectedStarterPlayerId = newSelection)
        }
    }

    fun swapWithBench(benchPlayerId: Long) {
        _uiState.update { state ->
            val starterId = state.selectedStarterPlayerId ?: return@update state
            val currentLineup = state.humanLineup
            val newStarters = currentLineup.starters.map { if (it == starterId) benchPlayerId else it }
            val newSubs = currentLineup.substitutes.map { if (it == benchPlayerId) starterId else it }
            val newLineup = Lineup(starters = newStarters, substitutes = newSubs)
            val updatedSeason = state.currentSeason.setLineup(state.humanClubId, newLineup)
            val updatedGame = state.game.copy(currentSeason = updatedSeason)
            gameRepository.saveGame(updatedGame)
            state.copy(
                game = updatedGame,
                currentSeason = updatedSeason,
                selectedStarterPlayerId = null,
            )
        }
    }

    // ── Matchday & Calendar Progression ──────────────────────────────────
    fun playNextMatchday() {
        _uiState.update { state ->
            if (state.currentSeason.isFinished) return@update state
            val prevIndex = state.currentSeason.nextFixtureIndex
            val nextSeason = runner.playNextMatchday(state.currentSeason)
            val newlyPlayedResults = nextSeason.results.subList(prevIndex, nextSeason.nextFixtureIndex)

            var worldSeason = nextSeason
            for (res in newlyPlayedResults) {
                worldSeason = applyMatchResultToWorldUseCase.execute(worldSeason, res)
            }

            val userMatchResult = worldSeason.results.lastOrNull {
                it.homeClubId == state.humanClubId || it.awayClubId == state.humanClubId
            }
            val updatedGame = state.game.copy(
                clubs = worldSeason.clubs,
                players = worldSeason.players,
                currentSeason = worldSeason,
                currentDate = worldSeason.currentDate,
            )
            gameRepository.saveGame(updatedGame)
            state.copy(
                game = updatedGame,
                currentSeason = worldSeason,
                lastMatchResult = userMatchResult,
                selectedStarterPlayerId = null,
            )
        }
    }

    fun advanceDay() {
        _uiState.update { state ->
            val updatedSeason = advanceDayUseCase.execute(state.currentSeason)
            val updatedGame = state.game.copy(
                players = updatedSeason.players,
                currentSeason = updatedSeason,
                currentDate = updatedSeason.currentDate,
            )
            gameRepository.saveGame(updatedGame)
            state.copy(game = updatedGame, currentSeason = updatedSeason)
        }
    }

    fun rolloverToNextSeason() {
        _uiState.update { state ->
            if (!state.currentSeason.isFinished) return@update state
            val nextSeason = seasonRolloverUseCase.execute(state.currentSeason)
            val updatedGame = state.game.copy(
                clubs = nextSeason.clubs,
                players = nextSeason.players,
                currentSeason = nextSeason,
                currentDate = nextSeason.currentDate,
            )
            gameRepository.saveGame(updatedGame)
            state.copy(
                game = updatedGame,
                currentSeason = nextSeason,
                lastMatchResult = null,
                selectedStarterPlayerId = null,
            )
        }
    }

    // ── Transfers & Finance ────────────────────────────────────────────────
    fun submitTransferBid(playerId: Long, feeOffered: Long) {
        _uiState.update { state ->
            val player = state.currentSeason.players[playerId] ?: return@update state
            val buyer = state.currentSeason.clubs[state.humanClubId] ?: state.humanClub
            val sellerClubId = state.currentSeason.clubs.values.firstOrNull { it.squad.contains(playerId) }?.id

            val bidId = (state.activeBids.maxOfOrNull { it.id } ?: 0L) + 1L
            val bid = submitTransferBidUseCase.execute(
                bidId = bidId,
                buyingClub = buyer,
                player = player,
                sellingClubId = sellerClubId,
                feeOffered = feeOffered,
                currentDate = state.currentSeason.currentDate,
            )

            val evaluatedBid = if (sellerClubId != null) {
                val decision = evaluateTransferOfferUseCase.execute(
                    bid = bid,
                    player = player,
                    currentDate = state.currentSeason.currentDate,
                )
                evaluateTransferOfferUseCase.applyDecisionToBid(bid, decision)
            } else {
                bid
            }

            val updatedBids = state.activeBids.filter { it.id != bidId } + evaluatedBid
            val updatedSeason = state.currentSeason.copy(activeBids = updatedBids)
            val updatedGame = state.game.copy(currentSeason = updatedSeason)
            gameRepository.saveGame(updatedGame)
            state.copy(game = updatedGame, currentSeason = updatedSeason)
        }
    }

    fun offerContractTerms(bidId: Long, weeklyWage: Long, years: Int, squadStatus: SquadStatus) {
        _uiState.update { state ->
            val bid = state.activeBids.firstOrNull { it.id == bidId } ?: return@update state
            val player = state.currentSeason.players[bid.playerId] ?: return@update state
            val offer = ContractOffer(
                weeklyWage = weeklyWage,
                contractYears = years,
                squadStatus = squadStatus,
            )
            val decision = negotiateContractUseCase.evaluate(player, offer)
            val updatedBid = negotiateContractUseCase.applyOffer(bid, offer, decision)

            val updatedBids = state.activeBids.map { if (it.id == bidId) updatedBid else it }
            val updatedSeason = state.currentSeason.copy(activeBids = updatedBids)
            val updatedGame = state.game.copy(currentSeason = updatedSeason)
            gameRepository.saveGame(updatedGame)
            state.copy(game = updatedGame, currentSeason = updatedSeason)
        }
    }

    fun completeTransferDeal(bidId: Long) {
        _uiState.update { state ->
            val bid = state.activeBids.firstOrNull { it.id == bidId } ?: return@update state
            val buyer = state.currentSeason.clubs[bid.buyingClubId] ?: state.game.club(bid.buyingClubId)
            val seller = bid.sellingClubId?.let { state.currentSeason.clubs[it] ?: state.game.club(it) }
            val player = state.currentSeason.players[bid.playerId] ?: return@update state

            val result = completeTransferUseCase.execute(
                bid = bid,
                buyer = buyer,
                seller = seller,
                player = player,
                currentDate = state.currentSeason.currentDate,
            )

            val updatedClubs = state.currentSeason.clubs + (result.updatedBuyer.id to result.updatedBuyer) +
                (result.updatedSeller?.let { listOf(it.id to it) } ?: emptyList())
            val updatedPlayers = state.currentSeason.players + (result.updatedPlayer.id to result.updatedPlayer)
            val updatedBids = state.activeBids.filter { it.id != bidId }
            val updatedHistory = state.transferHistory + result.record

            val updatedSeason = state.currentSeason.copy(
                clubs = updatedClubs,
                players = updatedPlayers,
                activeBids = updatedBids,
                transferHistory = updatedHistory,
            )
            val updatedGame = state.game.copy(
                clubs = updatedClubs,
                players = updatedPlayers,
                currentSeason = updatedSeason,
            )
            gameRepository.saveGame(updatedGame)
            state.copy(game = updatedGame, currentSeason = updatedSeason)
        }
    }

    fun cancelTransferDeal(bidId: Long) {
        _uiState.update { state ->
            val updatedBids = state.activeBids.filter { it.id != bidId }
            val updatedSeason = state.currentSeason.copy(activeBids = updatedBids)
            val updatedGame = state.game.copy(currentSeason = updatedSeason)
            gameRepository.saveGame(updatedGame)
            state.copy(game = updatedGame, currentSeason = updatedSeason)
        }
    }
}
