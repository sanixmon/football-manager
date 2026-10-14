package com.footballmanager.serialization

import com.footballmanager.model.Calendar
import com.footballmanager.model.Club
import com.footballmanager.model.Contract
import com.footballmanager.model.Facilities
import com.footballmanager.model.Finance
import com.footballmanager.model.Game
import com.footballmanager.model.InjuryStatus
import com.footballmanager.model.League
import com.footballmanager.model.Player
import com.footballmanager.model.PlayerAttributes
import com.footballmanager.model.Squad
import com.footballmanager.model.SuspensionStatus
import com.footballmanager.simulation.Lineup
import com.footballmanager.simulation.MatchResult
import com.footballmanager.simulation.Tactics
import com.footballmanager.simulation.Team
import com.footballmanager.simulation.season.Fixture
import com.footballmanager.simulation.season.SeasonResult
import com.footballmanager.simulation.season.SeasonState
import com.footballmanager.simulation.season.StandingEntry
import com.footballmanager.simulation.season.Standings

object SaveGameMapper {

    fun toDto(game: Game): SaveGameDto {
        val canonicalClubs = game.clubs.values.map { club ->
            ClubDto(
                id = club.id,
                name = club.name,
                shortName = club.shortName,
                leagueId = club.leagueId,
                balance = club.finance.balance,
                transferBudget = club.finance.transferBudget,
                weeklyWageBudget = club.finance.weeklyWageBudget,
                // Requirement 12: Do not persist ephemeral emergency GKs (id < 0)
                playerIds = club.squad.playerIds.filter { it > 0 },
                defaultTactics = club.defaultTactics?.let { t ->
                    TacticsDto(t.formation, t.mentality, t.tempo, t.pressing, t.passing)
                },
                graphicsId = club.graphicsId,
            )
        }

        val canonicalPlayers = game.players.values.filter { it.id > 0 }.map { p ->
            PlayerDto(
                id = p.id,
                name = p.name,
                age = p.age,
                nationality = p.nationality,
                naturalPositions = p.naturalPositions,
                attributes = p.attributes.toMap(),
                weeklyWage = p.contract.weeklyWage,
                contractExpiresOn = p.contract.expiresOn,
                squadStatus = p.contract.squadStatus,
                fitness = p.fitness,
                morale = p.morale,
                potential = p.potential,
                form = p.form,
                injuryDaysRemaining = p.injuryStatus?.daysRemaining,
                injuryDescription = p.injuryStatus?.description,
                suspensionMatchesRemaining = p.suspensionStatus?.matchesRemaining,
                suspensionReason = p.suspensionStatus?.reason,
                graphicsId = p.graphicsId,
                trainingFocus = p.trainingFocus,
                trainingIntensity = p.trainingIntensity,
            )
        }

        val league = game.competitions.values.filterIsInstance<League>().firstOrNull()
            ?: League(1L, "League", game.clubs.keys.toList())

        val leagueDto = LeagueDto(
            id = league.id,
            name = league.name,
            clubIds = league.clubIds,
        )

        val seasonDto = game.currentSeason?.let { season ->
            SeasonStateDto(
                round = season.nextMatchday ?: 1,
                nextFixtureIndex = season.nextFixtureIndex,
                currentDate = season.currentDate,
                humanClubId = season.humanClubId,
                fixtures = season.fixtures.map { f ->
                    FixtureDto(
                        round = f.round,
                        date = f.date,
                        homeClubId = f.home.clubId,
                        awayClubId = f.away.clubId,
                    )
                },
                results = season.results.map { r ->
                    MatchResultDto(
                        homeClubId = r.homeClubId,
                        awayClubId = r.awayClubId,
                        homeScore = r.homeScore,
                        awayScore = r.awayScore,
                        events = r.events,
                        stats = r.stats,
                        durationMinutes = r.durationMinutes,
                        playerStats = r.playerStats,
                        playerRatings = r.playerRatings,
                        substitutions = r.substitutions,
                    )
                },
                lineups = season.lineups.mapValues { (_, l) ->
                    LineupDto(starters = l.starters, substitutes = l.substitutes)
                },
                activeBids = season.activeBids,
                transferHistory = season.transferHistory,
                playerStats = season.playerStats.values.toList(),
                managerProfile = season.managerProfile,
                boardObjectives = season.boardObjectives,
            )
        }

        val lastSeasonDto = game.lastSeason?.let { result ->
            SeasonResultDto(
                leagueId = result.league.id,
                championClubId = result.champion.team.clubId,
                fixtures = result.fixtures.map { f ->
                    FixtureDto(
                        round = f.round,
                        date = f.date,
                        homeClubId = f.home.clubId,
                        awayClubId = f.away.clubId,
                    )
                },
                standings = result.standings.entries.map { e ->
                    StandingEntryDto(
                        clubId = e.team.clubId,
                        played = e.played,
                        won = e.won,
                        drawn = e.drawn,
                        lost = e.lost,
                        goalsFor = e.goalsFor,
                        goalsAgainst = e.goalsAgainst,
                    )
                },
                results = result.results.map { r ->
                    MatchResultDto(
                        homeClubId = r.homeClubId,
                        awayClubId = r.awayClubId,
                        homeScore = r.homeScore,
                        awayScore = r.awayScore,
                        events = r.events,
                        stats = r.stats,
                        durationMinutes = r.durationMinutes,
                        playerStats = r.playerStats,
                        playerRatings = r.playerRatings,
                        substitutions = r.substitutions,
                    )
                },
            )
        }

        return SaveGameDto(
            saveVersion = CURRENT_SAVE_VERSION,
            name = game.name,
            currentDate = game.currentDate,
            clubs = canonicalClubs,
            players = canonicalPlayers,
            league = leagueDto,
            competitions = game.competitions,
            calendar = game.calendar,
            currentSeason = seasonDto,
            lastSeason = lastSeasonDto,
        )
    }

    fun toDomain(dto: SaveGameDto): Game {
        // Reconstruct players
        val playersMap = dto.players.associate { pDto ->
            val injury = if (pDto.injuryDaysRemaining != null && pDto.injuryDaysRemaining > 0) {
                InjuryStatus(pDto.injuryDescription ?: "Injured", pDto.injuryDaysRemaining)
            } else null

            val suspension = if (pDto.suspensionMatchesRemaining != null && pDto.suspensionMatchesRemaining > 0) {
                SuspensionStatus(pDto.suspensionMatchesRemaining, pDto.suspensionReason ?: "Suspended")
            } else null

            val player = Player(
                id = pDto.id,
                name = pDto.name,
                age = pDto.age,
                nationality = pDto.nationality,
                naturalPositions = pDto.naturalPositions,
                attributes = PlayerAttributes(pDto.attributes),
                contract = Contract(
                    weeklyWage = pDto.weeklyWage,
                    expiresOn = pDto.contractExpiresOn,
                    squadStatus = pDto.squadStatus,
                ),
                fitness = pDto.fitness,
                morale = pDto.morale,
                potential = pDto.potential,
                form = pDto.form,
                injuryStatus = injury,
                suspensionStatus = suspension,
                graphicsId = pDto.graphicsId,
                trainingFocus = pDto.trainingFocus,
                trainingIntensity = pDto.trainingIntensity,
            )
            pDto.id to player
        }

        // Reconstruct clubs
        val clubsMap = dto.clubs.associate { cDto ->
            val tactics = cDto.defaultTactics?.let { t ->
                Tactics(t.formation, t.mentality, t.tempo, t.pressing, t.passing)
            }
            val club = Club(
                id = cDto.id,
                name = cDto.name,
                shortName = cDto.shortName,
                leagueId = cDto.leagueId,
                finance = Finance(
                    balance = cDto.balance,
                    transferBudget = cDto.transferBudget,
                    weeklyWageBudget = cDto.weeklyWageBudget,
                ),
                facilities = Facilities(),
                squad = Squad(clubId = cDto.id, playerIds = cDto.playerIds),
                defaultTactics = tactics,
                graphicsId = cDto.graphicsId,
            )
            cDto.id to club
        }

        val league = League(
            id = dto.league.id,
            name = dto.league.name,
            clubIds = dto.league.clubIds,
        )

        // Reconstruct currentSeason if present
        val currentSeasonState: SeasonState? = dto.currentSeason?.let { sDto ->
            val teams = clubsMap.values.sortedBy { it.id }.map { club ->
                val squadPlayers = club.squad.playerIds.mapNotNull { playersMap[it] }
                Team.fromSquad(club.id, squadPlayers, club.defaultTactics ?: Tactics())
            }
            val teamByClubId = teams.associateBy { it.clubId }

            val fixtures = sDto.fixtures.map { fDto ->
                Fixture(
                    round = fDto.round,
                    date = fDto.date,
                    home = teamByClubId.getValue(fDto.homeClubId),
                    away = teamByClubId.getValue(fDto.awayClubId),
                )
            }

            val results = sDto.results.map { rDto ->
                MatchResult(
                    homeClubId = rDto.homeClubId,
                    awayClubId = rDto.awayClubId,
                    homeScore = rDto.homeScore,
                    awayScore = rDto.awayScore,
                    events = rDto.events,
                    stats = rDto.stats,
                    durationMinutes = rDto.durationMinutes,
                    playerStats = rDto.playerStats,
                    playerRatings = rDto.playerRatings,
                    substitutions = rDto.substitutions,
                )
            }

            // Requirement 2: Standings are derived/recalculated on load
            val recalculatedStandings = Standings.compute(
                teams = teams,
                fixtures = fixtures.take(sDto.nextFixtureIndex),
                results = results,
            )

            val lineups = sDto.lineups.mapValues { (_, lDto) ->
                Lineup(starters = lDto.starters, substitutes = lDto.substitutes)
            }

            val playerStatsMap = sDto.playerStats.associateBy { it.playerId }

            SeasonState(
                league = league,
                teams = teams,
                fixtures = fixtures,
                nextFixtureIndex = sDto.nextFixtureIndex,
                results = results,
                standings = recalculatedStandings,
                currentDate = sDto.currentDate,
                humanClubId = sDto.humanClubId,
                clubs = clubsMap,
                players = playersMap,
                lineups = lineups,
                activeBids = sDto.activeBids,
                transferHistory = sDto.transferHistory,
                playerStats = playerStatsMap,
                boardObjectives = sDto.boardObjectives,
                managerProfile = sDto.managerProfile,
            )
        }

        // Reconstruct lastSeason if present
        val lastSeasonResult: SeasonResult? = dto.lastSeason?.let { resDto ->
            val teams = clubsMap.values.sortedBy { it.id }.map { club ->
                val squadPlayers = club.squad.playerIds.mapNotNull { playersMap[it] }
                Team.fromSquad(club.id, squadPlayers, club.defaultTactics ?: Tactics())
            }
            val teamByClubId = teams.associateBy { it.clubId }

            val standingsEntries = resDto.standings.map { sDto ->
                val team = teamByClubId.getValue(sDto.clubId)
                StandingEntry(
                    team = team,
                    played = sDto.played,
                    won = sDto.won,
                    drawn = sDto.drawn,
                    lost = sDto.lost,
                    goalsFor = sDto.goalsFor,
                    goalsAgainst = sDto.goalsAgainst,
                )
            }

            val results = resDto.results.map { rDto ->
                MatchResult(
                    homeClubId = rDto.homeClubId,
                    awayClubId = rDto.awayClubId,
                    homeScore = rDto.homeScore,
                    awayScore = rDto.awayScore,
                    events = rDto.events,
                    stats = rDto.stats,
                    durationMinutes = rDto.durationMinutes,
                    playerStats = rDto.playerStats,
                    playerRatings = rDto.playerRatings,
                    substitutions = rDto.substitutions,
                )
            }

            val fixtures = resDto.fixtures.map { fDto ->
                Fixture(
                    round = fDto.round,
                    date = fDto.date,
                    home = teamByClubId.getValue(fDto.homeClubId),
                    away = teamByClubId.getValue(fDto.awayClubId),
                )
            }

            SeasonResult(
                league = league,
                fixtures = fixtures,
                results = results,
                standings = Standings(standingsEntries),
            )
        }

        val allCompetitions = (mapOf(league.id to league) + dto.competitions)
        return Game(
            name = dto.name,
            currentDate = dto.currentDate,
            clubs = clubsMap,
            players = playersMap,
            competitions = allCompetitions,
            calendar = dto.calendar,
            lastSeason = lastSeasonResult,
            currentSeason = currentSeasonState,
        )
    }
}
