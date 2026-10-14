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
import com.footballmanager.simulation.Passing
import com.footballmanager.simulation.Pressing
import com.footballmanager.simulation.Tempo
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
import com.footballmanager.serialization.SaveSlot
import com.footballmanager.simulation.LineupValidationResult
import com.footballmanager.simulation.LineupValidator
import com.footballmanager.usecase.DirectTransferRequest
import com.footballmanager.usecase.DirectTransferUseCase
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
    private val playerDevelopmentUseCase: com.footballmanager.usecase.PlayerDevelopmentUseCase = com.footballmanager.usecase.PlayerDevelopmentUseCase(),
    private val saveCareerUseCase: com.footballmanager.usecase.SaveCareerUseCase = com.footballmanager.usecase.SaveCareerUseCase(),
    private val loadCareerUseCase: com.footballmanager.usecase.LoadCareerUseCase = com.footballmanager.usecase.LoadCareerUseCase(),
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

    private var isSimulatingInFlight = false
    private var isTransferringInFlight = false

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
                availableSaveSlots = loadCareerUseCase.getAllSlotsMetadata(),
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

    fun updateTempo(tempo: Tempo) {
        _uiState.update { state ->
            val newTactics = state.humanTeam.tactics.copy(tempo = tempo)
            val updatedSeason = state.currentSeason.setTactics(state.humanClubId, newTactics)
            val updatedGame = state.game.copy(currentSeason = updatedSeason)
            gameRepository.saveGame(updatedGame)
            state.copy(
                game = updatedGame,
                currentSeason = updatedSeason,
            )
        }
    }

    fun updatePressing(pressing: Pressing) {
        _uiState.update { state ->
            val newTactics = state.humanTeam.tactics.copy(pressing = pressing)
            val updatedSeason = state.currentSeason.setTactics(state.humanClubId, newTactics)
            val updatedGame = state.game.copy(currentSeason = updatedSeason)
            gameRepository.saveGame(updatedGame)
            state.copy(
                game = updatedGame,
                currentSeason = updatedSeason,
            )
        }
    }

    fun updatePassing(passing: Passing) {
        _uiState.update { state ->
            val newTactics = state.humanTeam.tactics.copy(passing = passing)
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

            val newLineup = try {
                Lineup(starters = newStarters, substitutes = newSubs)
            } catch (e: Exception) {
                return@update state.copy(lineupValidationError = e.message)
            }

            val validation = LineupValidator.validate(newLineup, state.currentSeason.players)
            if (validation is LineupValidationResult.Invalid) {
                return@update state.copy(lineupValidationError = validation.message)
            }

            val updatedSeason = state.currentSeason.setLineup(state.humanClubId, newLineup)
            val updatedGame = state.game.copy(currentSeason = updatedSeason)
            gameRepository.saveGame(updatedGame)
            state.copy(
                game = updatedGame,
                currentSeason = updatedSeason,
                selectedStarterPlayerId = null,
                lineupValidationError = null,
            )
        }
    }

    // ── Matchday & Calendar Progression ──────────────────────────────────
    fun advanceSmart() {
        if (_uiState.value.isSimulating || isSimulatingInFlight) return
        val state = _uiState.value
        if (state.currentSeason.isFinished) {
            rolloverToNextSeason()
            return
        }
        val nextFixture = state.currentSeason.fixtures.getOrNull(state.currentSeason.nextFixtureIndex)
        if (nextFixture != null && nextFixture.date <= state.currentSeason.currentDate) {
            playNextMatchday()
        } else {
            advanceDay()
        }
    }

    fun playNextMatchday() {
        val currentState = _uiState.value
        if (currentState.isSimulating || isSimulatingInFlight || currentState.currentSeason.isFinished) return

        // Validate human lineup before starting match
        val validation = LineupValidator.validate(currentState.humanLineup, currentState.currentSeason.players)
        if (validation is LineupValidationResult.Invalid) {
            _uiState.update { it.copy(lineupValidationError = "Cannot start match: ${validation.message}") }
            return
        }

        isSimulatingInFlight = true
        _uiState.update { it.copy(isSimulating = true, lineupValidationError = null) }
        try {
            val prevIndex = currentState.currentSeason.nextFixtureIndex
            val nextSeason = runner.playNextMatchday(currentState.currentSeason)
            val userMatchResult = nextSeason.results.lastOrNull {
                it.homeClubId == currentState.humanClubId || it.awayClubId == currentState.humanClubId
            }
            val updatedGame = currentState.game.copy(
                clubs = nextSeason.clubs,
                players = nextSeason.players,
                currentSeason = nextSeason,
                currentDate = nextSeason.currentDate,
            )
            gameRepository.saveGame(updatedGame)
            _uiState.update {
                it.copy(
                    game = updatedGame,
                    currentSeason = nextSeason,
                    lastMatchResult = userMatchResult,
                    selectedStarterPlayerId = null,
                    lineupValidationError = null,
                    isSimulating = false,
                    saveStatusMessage = "Autosaved after matchday ${nextSeason.nextMatchday ?: "complete"}",
                )
            }
        } finally {
            isSimulatingInFlight = false
            _uiState.update { it.copy(isSimulating = false) }
        }
    }

    // ── Persistence: Save & Load Career ──────────────────────────────────
    fun refreshSaveSlots() {
        _uiState.update { it.copy(availableSaveSlots = loadCareerUseCase.getAllSlotsMetadata()) }
    }

    fun showSaveDialog(show: Boolean) {
        if (show) refreshSaveSlots()
        _uiState.update { it.copy(showSaveDialog = show) }
    }

    fun showLoadDialog(show: Boolean) {
        if (show) refreshSaveSlots()
        _uiState.update { it.copy(showLoadDialog = show) }
    }

    fun showTrainingDialog(show: Boolean) {
        _uiState.update { it.copy(showTrainingDialog = show) }
    }

    fun showSeasonHistoryDialog(show: Boolean) {
        _uiState.update { it.copy(showSeasonHistoryDialog = show) }
    }

    fun saveCareer(slot: SaveSlot = SaveSlot.MANUAL) {
        try {
            val state = _uiState.value
            saveCareerUseCase.execute(state.game, slot)
            gameRepository.saveGame(state.game)
            val updatedSlots = loadCareerUseCase.getAllSlotsMetadata()
            _uiState.update {
                it.copy(
                    saveStatusMessage = "Career saved successfully (${slot.displayName})",
                    availableSaveSlots = updatedSlots,
                    showSaveDialog = false,
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(saveStatusMessage = "Failed to save: ${e.message}") }
        }
    }

    fun loadCareer(slot: SaveSlot = SaveSlot.MANUAL) {
        try {
            val reloaded = try {
                loadCareerUseCase.execute(slot)
            } catch (_: Exception) {
                gameRepository.reload()
            }
            val season = reloaded.currentSeason ?: error("No current season in save")
            gameRepository.saveGame(reloaded)
            val updatedSlots = loadCareerUseCase.getAllSlotsMetadata()
            _uiState.update {
                it.copy(
                    game = reloaded,
                    currentSeason = season,
                    humanClubId = season.humanClubId ?: it.humanClubId,
                    lastMatchResult = season.results.lastOrNull { r -> r.homeClubId == it.humanClubId || r.awayClubId == it.humanClubId },
                    saveStatusMessage = "Career loaded successfully (${slot.displayName})",
                    lineupValidationError = null,
                    availableSaveSlots = updatedSlots,
                    showLoadDialog = false,
                    currentScreen = AppScreen.IN_GAME,
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(saveStatusMessage = "Failed to load: ${e.message}") }
        }
    }

    fun clearSaveStatus() {
        _uiState.update { it.copy(saveStatusMessage = null) }
    }

    // ── Training Management ────────────────────────────────────────────────
    fun updatePlayerTraining(playerId: Long, focus: com.footballmanager.model.TrainingFocus, intensity: com.footballmanager.model.TrainingIntensity) {
        _uiState.update { state ->
            val updatedSeason = playerDevelopmentUseCase.setPlayerTraining(
                season = state.currentSeason,
                playerId = playerId,
                focus = focus,
                intensity = intensity,
            )
            val updatedGame = state.game.copy(
                players = updatedSeason.players,
                currentSeason = updatedSeason,
            )
            gameRepository.saveGame(updatedGame)
            state.copy(game = updatedGame, currentSeason = updatedSeason)
        }
    }

    fun updateClubTraining(clubId: Long, focus: com.footballmanager.model.TrainingFocus, intensity: com.footballmanager.model.TrainingIntensity) {
        _uiState.update { state ->
            val updatedSeason = playerDevelopmentUseCase.setClubSquadTraining(
                season = state.currentSeason,
                clubId = clubId,
                focus = focus,
                intensity = intensity,
            )
            val updatedGame = state.game.copy(
                players = updatedSeason.players,
                currentSeason = updatedSeason,
            )
            gameRepository.saveGame(updatedGame)
            state.copy(game = updatedGame, currentSeason = updatedSeason)
        }
    }

    // ── Direct Transfers ──────────────────────────────────────────────────
    fun executeDirectTransfer(playerId: Long, fromClubId: Long, toClubId: Long, fee: Long) {
        if (isTransferringInFlight) return
        isTransferringInFlight = true
        try {
            val state = _uiState.value
            val updatedSeason = DirectTransferUseCase().execute(
                season = state.currentSeason,
                request = DirectTransferRequest(
                    playerId = playerId,
                    fromClubId = fromClubId,
                    toClubId = toClubId,
                    transferFee = fee,
                ),
            )
            val updatedGame = state.game.copy(
                clubs = updatedSeason.clubs,
                players = updatedSeason.players,
                currentSeason = updatedSeason,
            )
            gameRepository.saveGame(updatedGame)
            _uiState.update {
                it.copy(
                    game = updatedGame,
                    currentSeason = updatedSeason,
                    transferSuccessMessage = "Transferred player successfully!",
                    transferErrorMessage = null,
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    transferErrorMessage = e.message ?: "Transfer failed",
                    transferSuccessMessage = null,
                )
            }
        } finally {
            isTransferringInFlight = false
        }
    }

    fun clearTransferFeedback() {
        _uiState.update { it.copy(transferErrorMessage = null, transferSuccessMessage = null) }
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
            val rolloverResult = seasonRolloverUseCase.executeWithResult(state.currentSeason)
            val nextSeason = rolloverResult.nextSeason
            val updatedGame = state.game.copy(
                clubs = nextSeason.clubs,
                players = nextSeason.players,
                currentSeason = nextSeason,
                currentDate = nextSeason.currentDate,
                lastSeason = rolloverResult.completedSeasonResult,
            )
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
