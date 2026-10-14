package com.footballmanager.usecase

import com.footballmanager.model.Attribute
import com.footballmanager.model.Contract
import com.footballmanager.model.MAX_ATTRIBUTE
import com.footballmanager.model.MIN_ATTRIBUTE
import com.footballmanager.model.Player
import com.footballmanager.model.PlayerAttributes
import com.footballmanager.model.Position
import com.footballmanager.model.PositionWeights
import com.footballmanager.model.Squad
import com.footballmanager.simulation.Team
import com.footballmanager.simulation.season.FixtureGenerator
import com.footballmanager.simulation.season.SeasonState
import com.footballmanager.simulation.season.StandingEntry
import com.footballmanager.simulation.season.Standings
import com.footballmanager.calculator.PlayerDevelopmentCalculator
import com.footballmanager.calculator.WageExpectationCalculator
import com.footballmanager.model.Facilities
import com.footballmanager.simulation.season.SeasonResult
import java.time.LocalDate
import kotlin.random.Random

data class RolloverOutput(
    val nextSeason: SeasonState,
    val completedSeasonResult: SeasonResult,
)

class SeasonRolloverUseCase(
    private val random: Random = Random(2026),
) {

    fun execute(currentSeason: SeasonState): SeasonState =
        executeWithResult(currentSeason).nextSeason

    fun executeWithResult(currentSeason: SeasonState): RolloverOutput {
        require(currentSeason.isFinished) { "Cannot rollover an unfinished season" }

        val completedSeasonResult = SeasonResult(
            league = currentSeason.league,
            fixtures = currentSeason.fixtures,
            results = currentSeason.results,
            standings = currentSeason.standings,
        )

        val standings = currentSeason.standings.entries
        val nextYear = currentSeason.currentDate.year + 1
        val newStartDate = LocalDate.of(nextYear, 8, 1)

        // 1. Award Prize Money
        val updatedClubs = currentSeason.clubs.toMutableMap()
        standings.forEachIndexed { index, entry ->
            val rank = index + 1
            val prizeMoney = when (rank) {
                1 -> 4_000_000L
                2 -> 2_500_000L
                3 -> 1_500_000L
                in 4..6 -> 800_000L
                else -> 400_000L
            }
            val club = updatedClubs[entry.team.clubId]
            if (club != null) {
                updatedClubs[club.id] = club.copy(
                    finance = club.finance.copy(
                        balance = club.finance.balance + prizeMoney,
                        transferBudget = club.finance.transferBudget + (prizeMoney / 2),
                    ),
                )
            }
        }

        // 2. Age players, apply annual development & handle expiring contracts
        val updatedPlayers = currentSeason.players.mapValues { (_, player) ->
            val club = updatedClubs.values.firstOrNull { it.squad.contains(player.id) }
            val facilities = club?.facilities ?: Facilities()
            val seasonStats = currentSeason.playerStats[player.id]

            // Apply realistic player development / age curve decline
            val developedPlayer = PlayerDevelopmentCalculator.applyDevelopment(
                player = player,
                facilities = facilities,
                seasonStats = seasonStats,
                random = random,
            )

            val newAge = developedPlayer.age + 1

            // Contract rollover check
            val updatedContract = if (developedPlayer.contract.expiresOn <= newStartDate) {
                if (newAge <= 33 || developedPlayer.bestOverall() >= 65) {
                    val newWage = WageExpectationCalculator.calculateExpectedWage(
                        player = developedPlayer,
                        promisedStatus = developedPlayer.contract.squadStatus,
                    )
                    developedPlayer.contract.copy(
                        weeklyWage = newWage,
                        expiresOn = LocalDate.of(nextYear + 2, 6, 30),
                    )
                } else {
                    // Veteran extension (1 year)
                    developedPlayer.contract.copy(
                        expiresOn = LocalDate.of(nextYear + 1, 6, 30),
                    )
                }
            } else {
                developedPlayer.contract
            }

            developedPlayer.copy(
                age = newAge,
                contract = updatedContract,
                fitness = 100,
                morale = 75,
                injuryStatus = null,
                suspensionStatus = null,
            )
        }.toMutableMap()

        // 3. Youth Intake (2 young players per club)
        var maxPlayerId = (updatedPlayers.keys.maxOrNull() ?: 1000L) + 1L
        val firstNames = listOf("Bagas", "Rizky", "Dimas", "Eko", "Fajar", "Ilham", "Wahyu", "Bayu")
        val lastNames = listOf("Pratama", "Saputra", "Kusuma", "Hidayat", "Nugroho", "Santoso", "Wijaya")

        updatedClubs.forEach { (clubId, club) ->
            val newPlayerIds = mutableListOf<Long>()
            repeat(2) {
                val pId = maxPlayerId++
                val pos = listOf(Position.CB, Position.CM, Position.ST, Position.GK).random(random)
                val weights = PositionWeights.weights.getValue(pos)
                val baseQuality = 55 + random.nextInt(0, 15)
                val attrs = Attribute.entries.associateWith { attr ->
                    val weight = weights[attr] ?: 0
                    (baseQuality + (weight - 2) * 4 + random.nextInt(-4, 5)).coerceIn(MIN_ATTRIBUTE, MAX_ATTRIBUTE)
                }
                val youthPlayer = Player(
                    id = pId,
                    name = "${firstNames.random(random)} ${lastNames.random(random)}",
                    age = 17,
                    nationality = "ID",
                    naturalPositions = listOf(pos),
                    attributes = PlayerAttributes(attrs),
                    contract = Contract(weeklyWage = 800L, expiresOn = LocalDate.of(nextYear + 3, 6, 30)),
                    potential = (baseQuality + 15).coerceIn(60, 100),
                    form = 60,
                )
                updatedPlayers[pId] = youthPlayer
                newPlayerIds.add(pId)
            }
            updatedClubs[clubId] = club.copy(squad = Squad(clubId, club.squad.playerIds + newPlayerIds))
        }

        // 4. Update Manager Trophies if human finished 1st
        val humanWonLeague = standings.firstOrNull()?.team?.clubId == currentSeason.humanClubId
        val updatedManager = currentSeason.managerProfile?.let { mgr ->
            if (humanWonLeague) mgr.copy(trophiesWon = mgr.trophiesWon + 1, reputation = (mgr.reputation + 10).coerceAtMost(100))
            else mgr
        }

        // 5. Generate fresh new fixtures & standings
        val teams = updatedClubs.values.sortedBy { it.id }.map { club ->
            val squad = club.squad.playerIds.mapNotNull { updatedPlayers[it] }
            Team.fromSquad(club.id, squad, club.defaultTactics ?: currentSeason.teams.first { it.clubId == club.id }.tactics)
        }

        val fixtures = FixtureGenerator.generate(teams, newStartDate)
        val initialStandings = Standings(teams.map { StandingEntry(it) }.sortedWith(Standings.comparator))

        val nextSeasonState = SeasonState(
            league = currentSeason.league,
            teams = teams,
            fixtures = fixtures,
            nextFixtureIndex = 0,
            results = emptyList(),
            standings = initialStandings,
            currentDate = newStartDate,
            humanClubId = currentSeason.humanClubId,
            clubs = updatedClubs,
            players = updatedPlayers,
            lineups = emptyMap(),
            activeBids = emptyList(),
            transferHistory = emptyList(),
            playerStats = emptyMap(),
            boardObjectives = currentSeason.boardObjectives,
            managerProfile = updatedManager,
        )

        return RolloverOutput(
            nextSeason = nextSeasonState,
            completedSeasonResult = completedSeasonResult,
        )
    }
}
