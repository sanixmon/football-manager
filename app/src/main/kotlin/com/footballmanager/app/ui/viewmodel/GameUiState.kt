package com.footballmanager.app.ui.viewmodel

import com.footballmanager.model.Club
import com.footballmanager.model.Game
import com.footballmanager.model.Player
import com.footballmanager.simulation.Lineup
import com.footballmanager.simulation.MatchResult
import com.footballmanager.simulation.Team
import com.footballmanager.simulation.season.SeasonState

data class GameUiState(
    val game: Game,
    val currentSeason: SeasonState,
    val humanClubId: Long = 1L,
    val selectedStarterPlayerId: Long? = null,
    val isSimulating: Boolean = false,
    val currentSimTick: Int = 0,
    val lastMatchResult: MatchResult? = null,
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
}
