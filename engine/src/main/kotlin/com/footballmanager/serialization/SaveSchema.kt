package com.footballmanager.serialization

import com.footballmanager.model.Attribute
import com.footballmanager.model.BoardObjectives
import com.footballmanager.model.Calendar
import com.footballmanager.model.Competition
import com.footballmanager.model.ManagerProfile
import com.footballmanager.model.PlayerSeasonStats
import com.footballmanager.model.Position
import com.footballmanager.model.SquadStatus
import com.footballmanager.model.TrainingFocus
import com.footballmanager.model.TrainingIntensity
import com.footballmanager.model.TransferBid
import com.footballmanager.model.TransferRecord
import com.footballmanager.simulation.Formation
import com.footballmanager.simulation.MatchEvent
import com.footballmanager.simulation.MatchStats
import com.footballmanager.simulation.Mentality
import com.footballmanager.simulation.Passing
import com.footballmanager.simulation.PlayerMatchStats
import com.footballmanager.simulation.Pressing
import com.footballmanager.simulation.SubstitutionRecord
import com.footballmanager.simulation.Tempo
import java.time.LocalDate
import kotlinx.serialization.Serializable

const val CURRENT_SAVE_VERSION = 1

@Serializable
data class SaveGameDto(
    val saveVersion: Int = CURRENT_SAVE_VERSION,
    val name: String,
    @Serializable(with = LocalDateSerializer::class)
    val currentDate: LocalDate,
    val clubs: List<ClubDto>,
    val players: List<PlayerDto>,
    val league: LeagueDto,
    val competitions: Map<Long, Competition> = emptyMap(),
    val calendar: Calendar = Calendar(),
    val currentSeason: SeasonStateDto? = null,
    val lastSeason: SeasonResultDto? = null,
)

@Serializable
data class ClubDto(
    val id: Long,
    val name: String,
    val shortName: String,
    val leagueId: Long,
    val balance: Long,
    val transferBudget: Long,
    val weeklyWageBudget: Long,
    val playerIds: List<Long>,
    val defaultTactics: TacticsDto? = null,
    val graphicsId: Long? = null,
)

@Serializable
data class PlayerDto(
    val id: Long,
    val name: String,
    val age: Int,
    val nationality: String,
    val naturalPositions: List<Position>,
    val attributes: Map<Attribute, Int>,
    val weeklyWage: Long,
    @Serializable(with = LocalDateSerializer::class)
    val contractExpiresOn: LocalDate,
    val squadStatus: SquadStatus = SquadStatus.ROTATION,
    val fitness: Int = 100,
    val morale: Int = 50,
    val potential: Int = 75,
    val form: Int = 60,
    val injuryDaysRemaining: Int? = null,
    val injuryDescription: String? = null,
    val suspensionMatchesRemaining: Int? = null,
    val suspensionReason: String? = null,
    val graphicsId: Long? = null,
    val trainingFocus: TrainingFocus = TrainingFocus.BALANCED,
    val trainingIntensity: TrainingIntensity = TrainingIntensity.NORMAL,
)

@Serializable
data class LeagueDto(
    val id: Long,
    val name: String,
    val clubIds: List<Long>,
)

@Serializable
data class TacticsDto(
    val formation: Formation = Formation.FOUR_FOUR_TWO,
    val mentality: Mentality = Mentality.BALANCED,
    val tempo: Tempo = Tempo.NORMAL,
    val pressing: Pressing = Pressing.MEDIUM,
    val passing: Passing = Passing.MIXED,
)

@Serializable
data class SeasonStateDto(
    val round: Int = 1,
    val nextFixtureIndex: Int = 0,
    @Serializable(with = LocalDateSerializer::class)
    val currentDate: LocalDate,
    val humanClubId: Long? = null,
    val fixtures: List<FixtureDto>,
    val results: List<MatchResultDto>,
    val lineups: Map<Long, LineupDto> = emptyMap(),
    val activeBids: List<TransferBid> = emptyList(),
    val transferHistory: List<TransferRecord> = emptyList(),
    val playerStats: List<PlayerSeasonStats> = emptyList(),
    val managerProfile: ManagerProfile? = null,
    val boardObjectives: Map<Long, BoardObjectives> = emptyMap(),
)

@Serializable
data class FixtureDto(
    val round: Int,
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,
    val homeClubId: Long,
    val awayClubId: Long,
)

@Serializable
data class LineupDto(
    val starters: List<Long>,
    val substitutes: List<Long> = emptyList(),
)

@Serializable
data class MatchResultDto(
    val homeClubId: Long,
    val awayClubId: Long,
    val homeScore: Int,
    val awayScore: Int,
    val events: List<MatchEvent>,
    val stats: MatchStats,
    val durationMinutes: Int = 90,
    val playerStats: Map<Long, PlayerMatchStats> = emptyMap(),
    val playerRatings: Map<Long, Double> = emptyMap(),
    val substitutions: List<SubstitutionRecord> = emptyList(),
)

@Serializable
data class SeasonResultDto(
    val leagueId: Long,
    val championClubId: Long,
    val fixtures: List<FixtureDto> = emptyList(),
    val standings: List<StandingEntryDto>,
    val results: List<MatchResultDto>,
)

@Serializable
data class StandingEntryDto(
    val clubId: Long,
    val played: Int,
    val won: Int,
    val drawn: Int,
    val lost: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
)
