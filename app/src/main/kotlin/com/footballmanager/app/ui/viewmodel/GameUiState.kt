package com.footballmanager.app.ui.viewmodel

import com.footballmanager.app.ui.components.FmNavSection
import com.footballmanager.app.ui.components.FmSquadTab
import com.footballmanager.model.Club
import com.footballmanager.model.Game
import com.footballmanager.model.Player
import com.footballmanager.simulation.Lineup
import com.footballmanager.simulation.MatchResult
import com.footballmanager.simulation.Team
import com.footballmanager.simulation.season.SeasonState

enum class AppScreen {
    MAIN_MENU,
    NEW_GAME_WIZARD,
    IN_GAME,
}

data class GameUiState(
    val game: Game,
    val currentSeason: SeasonState,
    val currentScreen: AppScreen = AppScreen.MAIN_MENU,
    val humanClubId: Long = 1L,
    val selectedStarterPlayerId: Long? = null,
    val isSimulating: Boolean = false,
    val currentSimTick: Int = 0,
    val lastMatchResult: MatchResult? = null,
    // Navigation
    val activeNavSection: FmNavSection = FmNavSection.HOME,
    val activeSquadTab: FmSquadTab = FmSquadTab.OVERVIEW,
) {
    val humanClub: Club get() = game.club(humanClubId)
    val humanSquad: List<Player>
        get() = currentSeason.clubs[humanClubId]?.squad?.playerIds?.mapNotNull { currentSeason.players[it] }
            ?: game.squad(humanClubId)
    val humanTeam: Team
        get() = currentSeason.teams.firstOrNull { it.clubId == humanClubId }
            ?: Team.fromSquad(humanClubId, humanSquad)
    val humanLineup: Lineup
        get() = currentSeason.lineups[humanClubId]
            ?: Lineup.autoSelect(humanSquad, humanTeam.tactics)
    val starters: List<Player>
        get() = humanLineup.starters.mapNotNull { currentSeason.players[it] }
    val substitutes: List<Player>
        get() = humanLineup.substitutes.mapNotNull { currentSeason.players[it] }
    val allPlayers: List<Player>
        get() = currentSeason.players.values.toList().ifEmpty { game.players.values.toList() }
    val activeBids: List<com.footballmanager.model.TransferBid>
        get() = currentSeason.activeBids
    val transferHistory: List<com.footballmanager.model.TransferRecord>
        get() = currentSeason.transferHistory
    val humanFinance: com.footballmanager.model.Finance
        get() = currentSeason.clubs[humanClubId]?.finance ?: humanClub.finance
}
