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
import com.footballmanager.usecase.SelectLineupUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameViewModel(
    private val gameRepository: GameRepository = InMemoryGameRepository(SeedData.game()),
    private val runner: SeasonRunner = SeasonRunner(),
    private val selectLineupUseCase: SelectLineupUseCase = SelectLineupUseCase(),
) : ViewModel() {

    /** Backward-compatible convenience constructor. */
    constructor(
        initialGame: Game,
        runner: SeasonRunner = SeasonRunner(),
        selectLineupUseCase: SelectLineupUseCase = SelectLineupUseCase(),
    ) : this(InMemoryGameRepository(initialGame), runner, selectLineupUseCase)

    private val _uiState: MutableStateFlow<GameUiState>

    init {
        val game = gameRepository.getGame()
        val league = game.competitions.getValue(SeedData.LEAGUE_ID) as League
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
            state.copy(currentSeason = finalSeason, selectedStarterPlayerId = null)
        }
    }

    fun updateMentality(mentality: Mentality) {
        _uiState.update { state ->
            val newTactics = state.humanTeam.tactics.copy(mentality = mentality)
            val updatedSeason = state.currentSeason.setTactics(state.humanClubId, newTactics)
            state.copy(currentSeason = updatedSeason)
        }
    }

    fun autoSelectBestXI() {
        _uiState.update { state ->
            val newLineup = selectLineupUseCase(state.humanSquad, state.humanTeam.tactics)
            val updatedSeason = state.currentSeason.setLineup(state.humanClubId, newLineup)
            state.copy(currentSeason = updatedSeason, selectedStarterPlayerId = null)
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
            state.copy(currentSeason = updatedSeason, selectedStarterPlayerId = null)
        }
    }

    // ── Matchday ──────────────────────────────────────────────────────────
    fun playNextMatchday() {
        _uiState.update { state ->
            if (state.currentSeason.isFinished) return@update state
            val nextSeason = runner.playNextMatchday(state.currentSeason)
            val userMatchResult = nextSeason.results.lastOrNull {
                it.homeClubId == state.humanClubId || it.awayClubId == state.humanClubId
            }
            val updatedGame = state.game.copy(
                players = nextSeason.players,
                currentSeason = nextSeason,
                currentDate = nextSeason.currentDate,
            )
            gameRepository.saveGame(updatedGame)
            state.copy(
                game = updatedGame,
                currentSeason = nextSeason,
                lastMatchResult = userMatchResult,
                selectedStarterPlayerId = null,
            )
        }
    }
}
