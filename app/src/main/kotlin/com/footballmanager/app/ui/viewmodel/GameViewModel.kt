package com.footballmanager.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.footballmanager.app.ui.components.FmNavSection
import com.footballmanager.app.ui.components.FmSquadTab
import com.footballmanager.model.Game
import com.footballmanager.model.League
import com.footballmanager.seed.SeedData
import com.footballmanager.simulation.Formation
import com.footballmanager.simulation.Lineup
import com.footballmanager.simulation.Mentality
import com.footballmanager.simulation.season.SeasonRunner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameViewModel(
    initialGame: Game = SeedData.game(),
    private val runner: SeasonRunner = SeasonRunner(),
) : ViewModel() {

    private val _uiState: MutableStateFlow<GameUiState>

    init {
        val league = initialGame.competitions.getValue(SeedData.LEAGUE_ID) as League
        val teams = SeedData.teams(initialGame)
        val season = initialGame.currentSeason ?: runner.start(
            league = league,
            teams = teams,
            startDate = initialGame.currentDate,
            humanClubId = 1L,
            clubs = initialGame.clubs,
            players = initialGame.players,
        )
        _uiState = MutableStateFlow(
            GameUiState(game = initialGame, currentSeason = season, humanClubId = 1L),
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
            val newLineup = Lineup.autoSelect(state.humanSquad, newTactics)
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
            val newLineup = Lineup.autoSelect(state.humanSquad, state.humanTeam.tactics)
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
            state.copy(
                game = updatedGame,
                currentSeason = nextSeason,
                lastMatchResult = userMatchResult,
                selectedStarterPlayerId = null,
            )
        }
    }
}
