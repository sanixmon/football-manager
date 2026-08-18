package com.footballmanager.simulation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TacticsTest {

    @Test
    fun `default tactics leave ratings unchanged`() {
        val team = Team(1, 70, 60)
        assertEquals(Formation.FOUR_FOUR_TWO, team.tactics.formation)
        assertEquals(Mentality.BALANCED, team.tactics.mentality)
        assertEquals(70, team.effectiveAttack())
        assertEquals(60, team.effectiveDefense())
    }

    @Test
    fun `attacking formation boosts attack and lowers defense`() {
        val team = Team(1, 70, 70, Tactics(Formation.FOUR_THREE_THREE, Mentality.BALANCED))
        assertTrue(team.effectiveAttack() > team.attack)
        assertTrue(team.effectiveDefense() < team.defense)
    }

    @Test
    fun `defensive formation lowers attack and boosts defense`() {
        val team = Team(1, 70, 70, Tactics(Formation.FIVE_THREE_TWO, Mentality.BALANCED))
        assertTrue(team.effectiveAttack() < team.attack)
        assertTrue(team.effectiveDefense() > team.defense)
    }

    @Test
    fun `formation and mentality stack multiplicatively`() {
        val tactics = Tactics(Formation.FOUR_THREE_THREE, Mentality.ATTACKING)
        assertEquals(1.265, tactics.attackModifier, 1e-9)
        assertEquals(0.765, tactics.defenseModifier, 1e-9)
    }

    @Test
    fun `effective ratings stay within bounds`() {
        val team = Team(1, 99, 1, Tactics(Formation.FOUR_THREE_THREE, Mentality.ATTACKING))
        assertEquals(100, team.effectiveAttack())
        assertEquals(1, team.effectiveDefense())
    }
}
