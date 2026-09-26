package com.footballmanager.simulation

import com.footballmanager.model.Attribute
import com.footballmanager.model.Contract
import com.footballmanager.model.EmergencyGoalkeeper
import com.footballmanager.model.InjuryStatus
import com.footballmanager.model.Player
import com.footballmanager.model.PlayerAttributes
import com.footballmanager.model.Position
import com.footballmanager.model.SuspensionStatus
import com.footballmanager.seed.SeedData
import com.footballmanager.serialization.gameJson
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class LineupTest {

    private fun createPlayer(
        id: Long,
        name: String,
        position: Position,
        isInjured: Boolean = false,
        isSuspended: Boolean = false,
    ): Player = Player(
        id = id,
        name = name,
        age = 24,
        nationality = "ID",
        naturalPositions = listOf(position),
        attributes = PlayerAttributes.uniform(70),
        contract = Contract(expiresOn = LocalDate.of(2030, 6, 30)),
        injuryStatus = if (isInjured) InjuryStatus(daysRemaining = 7) else null,
        suspensionStatus = if (isSuspended) SuspensionStatus(matchesRemaining = 1) else null,
    )

    private fun createStandardSquad(): List<Player> = listOf(
        createPlayer(1L, "GK 1", Position.GK),
        createPlayer(2L, "GK 2", Position.GK),
        createPlayer(3L, "CB 1", Position.CB),
        createPlayer(4L, "CB 2", Position.CB),
        createPlayer(5L, "CB 3", Position.CB),
        createPlayer(6L, "LB 1", Position.LB),
        createPlayer(7L, "RB 1", Position.RB),
        createPlayer(8L, "DM 1", Position.DM),
        createPlayer(9L, "CM 1", Position.CM),
        createPlayer(10L, "AM 1", Position.AM),
        createPlayer(11L, "LW 1", Position.LW),
        createPlayer(12L, "RW 1", Position.RW),
        createPlayer(13L, "ST 1", Position.ST),
        createPlayer(14L, "ST 2", Position.ST),
    )

    @Test
    fun `lineup requires exactly 11 unique starters`() {
        assertFailsWith<IllegalArgumentException> {
            Lineup(starters = (1L..10L).toList())
        }
        assertFailsWith<IllegalArgumentException> {
            Lineup(starters = listOf(1L, 1L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L))
        }
    }

    @Test
    fun `duplicate player in XI is rejected`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            Lineup(starters = listOf(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 1L))
        }
        assertTrue(ex.message?.contains("unique") == true)
    }

    @Test
    fun `duplicate starter and substitute is rejected`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            Lineup(
                starters = (1L..11L).toList(),
                substitutes = listOf(11L, 12L),
            )
        }
        assertTrue(ex.message?.contains("starter and substitute") == true)
    }

    @Test
    fun `duplicate substitutes is rejected`() {
        val ex = assertFailsWith<IllegalArgumentException> {
            Lineup(
                starters = (1L..11L).toList(),
                substitutes = listOf(12L, 12L),
            )
        }
        assertTrue(ex.message?.contains("unique") == true)
    }

    @Test
    fun `valid 11-player lineup is accepted`() {
        val squad = createStandardSquad()
        val playerMap = squad.associateBy { it.id }
        val lineup = Lineup(
            starters = listOf(1L, 3L, 4L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L),
            substitutes = listOf(2L, 5L, 14L),
        )

        val result = LineupValidator.validate(lineup, playerMap)
        assertIs<LineupValidationResult.Valid>(result)
        LineupValidator.requireValid(lineup, playerMap) // does not throw
    }

    @Test
    fun `non-GK in slot 0 is rejected`() {
        val squad = createStandardSquad()
        val playerMap = squad.associateBy { it.id }
        // Slot 0 is CB 1 (id=3L) instead of a GK
        val lineup = Lineup(
            starters = listOf(3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L),
            substitutes = listOf(1L, 2L, 14L),
        )

        val result = LineupValidator.validate(lineup, playerMap)
        assertIs<LineupValidationResult.Invalid>(result)
        assertTrue(result.message.contains("Slot 0 must be occupied by a natural GK"))
        assertFailsWith<IllegalArgumentException> {
            LineupValidator.requireValid(lineup, playerMap)
        }
    }

    @Test
    fun `injured GK cannot occupy GK slot`() {
        val injuredGk = createPlayer(1L, "Injured GK", Position.GK, isInjured = true)
        val squad = createStandardSquad().map { if (it.id == 1L) injuredGk else it }
        val playerMap = squad.associateBy { it.id }

        val lineup = Lineup(
            starters = listOf(1L, 3L, 4L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L),
            substitutes = listOf(2L, 5L, 14L),
        )

        val result = LineupValidator.validate(lineup, playerMap)
        assertIs<LineupValidationResult.Invalid>(result)
        assertTrue(result.message.contains("Injured player Injured GK (id=1) cannot start"))
    }

    @Test
    fun `suspended GK cannot occupy GK slot`() {
        val suspendedGk = createPlayer(1L, "Suspended GK", Position.GK, isSuspended = true)
        val squad = createStandardSquad().map { if (it.id == 1L) suspendedGk else it }
        val playerMap = squad.associateBy { it.id }

        val lineup = Lineup(
            starters = listOf(1L, 3L, 4L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L),
            substitutes = listOf(2L, 5L, 14L),
        )

        val result = LineupValidator.validate(lineup, playerMap)
        assertIs<LineupValidationResult.Invalid>(result)
        assertTrue(result.message.contains("Suspended player Suspended GK (id=1) cannot start"))
    }

    @Test
    fun `injured outfield player cannot start`() {
        val injuredCb = createPlayer(3L, "Injured CB", Position.CB, isInjured = true)
        val squad = createStandardSquad().map { if (it.id == 3L) injuredCb else it }
        val playerMap = squad.associateBy { it.id }

        val lineup = Lineup(
            starters = listOf(1L, 3L, 4L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L),
            substitutes = listOf(2L, 5L, 14L),
        )

        val result = LineupValidator.validate(lineup, playerMap)
        assertIs<LineupValidationResult.Invalid>(result)
        assertTrue(result.message.contains("Injured player Injured CB (id=3) cannot start"))
    }

    @Test
    fun `suspended outfield player cannot start`() {
        val suspendedStriker = createPlayer(13L, "Suspended ST", Position.ST, isSuspended = true)
        val squad = createStandardSquad().map { if (it.id == 13L) suspendedStriker else it }
        val playerMap = squad.associateBy { it.id }

        val lineup = Lineup(
            starters = listOf(1L, 3L, 4L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L),
            substitutes = listOf(2L, 5L, 14L),
        )

        val result = LineupValidator.validate(lineup, playerMap)
        assertIs<LineupValidationResult.Invalid>(result)
        assertTrue(result.message.contains("Suspended player Suspended ST (id=13) cannot start"))
    }

    @Test
    fun `autoSelect excludes injured and suspended players from starters and bench`() {
        val injuredGk = createPlayer(1L, "Injured GK", Position.GK, isInjured = true)
        val suspendedCb = createPlayer(3L, "Suspended CB", Position.CB, isSuspended = true)
        val squad = createStandardSquad().map {
            when (it.id) {
                1L -> injuredGk
                3L -> suspendedCb
                else -> it
            }
        }

        val lineup = Lineup.autoSelect(squad, Tactics())

        assertTrue(1L !in lineup.starters && 1L !in lineup.substitutes, "injured GK must not be selected")
        assertTrue(3L !in lineup.starters && 3L !in lineup.substitutes, "suspended CB must not be selected")
        assertEquals(2L, lineup.starters.first(), "healthy GK 2 must start in slot 0")
    }

    @Test
    fun `fewer than 11 eligible players rejects lineup`() {
        val squad = createStandardSquad().take(10) // only 10 players

        assertFailsWith<IllegalArgumentException> {
            Lineup.autoSelect(squad, Tactics())
        }
    }

    @Test
    fun `all GKs unavailable and no emergency GK rejects lineup`() {
        val squad = createStandardSquad().map {
            if (Position.GK in it.naturalPositions) it.copy(injuryStatus = InjuryStatus(daysRemaining = 5)) else it
        }

        assertFailsWith<IllegalStateException> {
            Lineup.autoSelect(squad, Tactics(), allowEmergencyGk = false)
        }
    }

    @Test
    fun `no eligible GK but emergency GK available accepts lineup`() {
        val squad = createStandardSquad().map {
            if (Position.GK in it.naturalPositions) it.copy(injuryStatus = InjuryStatus(daysRemaining = 5)) else it
        }

        val lineup = Lineup.autoSelect(squad, Tactics(), allowEmergencyGk = true, clubId = 7L)

        assertEquals(11, lineup.starters.size)
        val emergencyGkId = lineup.starters.first()
        assertTrue(emergencyGkId < 0, "emergency GK should have a negative ID ($emergencyGkId)")
        assertEquals(-7L, emergencyGkId)
    }

    @Test
    fun `autoSelect is deterministic`() {
        val squad = createStandardSquad()
        val tactics = Tactics(Formation.FOUR_FOUR_TWO)

        val lineup1 = Lineup.autoSelect(squad, tactics)
        val lineup2 = Lineup.autoSelect(squad, tactics)

        assertEquals(lineup1.starters, lineup2.starters)
        assertEquals(lineup1.substitutes, lineup2.substitutes)
    }

    @Test
    fun `lineup serializes and deserializes correctly`() {
        val lineup = Lineup(
            starters = (1L..11L).toList(),
            substitutes = listOf(12L, 13L, 14L),
        )
        val json = gameJson.encodeToString(Lineup.serializer(), lineup)
        val decoded = gameJson.decodeFromString(Lineup.serializer(), json)

        assertEquals(lineup, decoded)
    }
}
