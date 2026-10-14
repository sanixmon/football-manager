package com.footballmanager.calculator

import com.footballmanager.model.Attribute
import com.footballmanager.model.Facilities
import com.footballmanager.model.MAX_ATTRIBUTE
import com.footballmanager.model.MIN_ATTRIBUTE
import com.footballmanager.model.Player
import com.footballmanager.model.PlayerAttributes
import com.footballmanager.model.PlayerSeasonStats
import com.footballmanager.model.TrainingFocus
import com.footballmanager.model.TrainingIntensity
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Pure calculator for player attribute development, progression curves, and age decline.
 *
 * Enforces realistic football progression:
 * - Young players (< 22) with high headroom develop rapidly towards their [Player.potential].
 * - Prime players (22..28) refine their key positional attributes.
 * - Mature players (29..31) stabilize.
 * - Veteran players (32+) experience natural physical decline while conserving tactical knowledge.
 * - Training focus, training intensity, club facilities, and competitive match minutes directly
 *   influence progression rate without unbounded attribute inflation.
 */
object PlayerDevelopmentCalculator {

    /**
     * Calculates the attribute changes for a player over a full season cycle.
     *
     * @param player The current player state.
     * @param facilities The club's facilities (affects training quality).
     * @param seasonStats Statistics recorded for the player in the concluded season.
     * @param random Deterministic random source for minor attribute variation.
     * @return Map of attribute delta adjustments (-N to +N).
     */
    fun calculateAnnualDevelopment(
        player: Player,
        facilities: Facilities = Facilities(),
        seasonStats: PlayerSeasonStats? = null,
        random: Random = Random(player.id xor 2026L),
    ): Map<Attribute, Int> {
        val currentBestOverall = player.bestOverall()
        val potentialGap = (player.potential - currentBestOverall).coerceAtLeast(0)
        val age = player.age

        val deltas = mutableMapOf<Attribute, Int>()

        if (age < 29) {
            // ── GROWTH PHASE ──────────────────────────────────────────────────────────
            val ageMultiplier = when {
                age <= 20 -> 1.40
                age <= 23 -> 1.15
                age <= 26 -> 0.70
                else -> 0.35
            }

            val playingTimeMultiplier = when {
                seasonStats == null -> 0.85
                seasonStats.starts >= 12 || seasonStats.appearances >= 14 -> 1.30
                seasonStats.appearances >= 6 -> 1.00
                else -> 0.75
            }

            val facilityMultiplier = (0.80 + (facilities.trainingLevel.coerceIn(1, 5) - 1) * 0.10)

            val intensityMultiplier = when (player.trainingIntensity) {
                TrainingIntensity.LIGHT -> 0.70
                TrainingIntensity.NORMAL -> 1.00
                TrainingIntensity.INTENSIVE -> 1.30
            }

            // Raw potential points available to distribute this season (capped to avoid runaway inflation)
            val growthPointsRaw = (potentialGap * 0.20 * ageMultiplier * playingTimeMultiplier * facilityMultiplier * intensityMultiplier)
            val growthPoints = growthPointsRaw.coerceIn(0.0, 5.0).roundToInt()

            val targetAttributes = getTargetAttributes(player.trainingFocus, player)

            if (growthPoints > 0 && targetAttributes.isNotEmpty()) {
                // Distribute points among target attributes
                var pointsRemaining = growthPoints
                // Priority distribution: first pass adds +1 to primary attributes
                val shuffledTargets = targetAttributes.shuffled(random)
                for (attr in shuffledTargets) {
                    if (pointsRemaining <= 0) break
                    val currentVal = player.attributes[attr]
                    if (currentVal < player.potential && currentVal < MAX_ATTRIBUTE) {
                        deltas[attr] = (deltas[attr] ?: 0) + 1
                        pointsRemaining--
                    }
                }
                // Second pass if points remain and player has strong potential gap
                if (pointsRemaining > 0 && potentialGap >= 5) {
                    for (attr in shuffledTargets) {
                        if (pointsRemaining <= 0) break
                        val currentVal = player.attributes[attr] + (deltas[attr] ?: 0)
                        if (currentVal < player.potential && currentVal < MAX_ATTRIBUTE) {
                            deltas[attr] = (deltas[attr] ?: 0) + 1
                            pointsRemaining--
                        }
                    }
                }
            }
        } else if (age in 29..31) {
            // ── PEAK / STABILIZATION PHASE ───────────────────────────────────────────
            // Slight physical decay (-1 Pace/Physical) compensated if training intensively
            if (player.trainingIntensity == TrainingIntensity.LIGHT && age >= 31) {
                deltas[Attribute.PACE] = -1
            }
        } else {
            // ── VETERAN DECLINE PHASE (age 32+) ──────────────────────────────────────
            val physicalDrop = when {
                age in 32..33 -> -1
                age in 34..35 -> -2
                else -> -3
            }

            // High facilities soften veteran physical decline
            val soften = if (facilities.trainingLevel >= 4 && player.trainingIntensity == TrainingIntensity.INTENSIVE) 1 else 0
            val effectivePhysicalDrop = (physicalDrop + soften).coerceAtMost(0)

            deltas[Attribute.PACE] = effectivePhysicalDrop
            deltas[Attribute.PHYSICAL] = effectivePhysicalDrop

            // Technical attributes decline much slower (only at 35+ years old)
            if (age >= 35) {
                deltas[Attribute.DRIBBLING] = -1
                if (player.bestPosition().name != "GK") {
                    deltas[Attribute.SHOOTING] = -1
                }
            }
        }

        return deltas
    }

    /**
     * Applies annual development deltas to a player and returns the updated Player instance.
     */
    fun applyDevelopment(
        player: Player,
        facilities: Facilities = Facilities(),
        seasonStats: PlayerSeasonStats? = null,
        random: Random = Random(player.id xor 2026L),
    ): Player {
        val deltas = calculateAnnualDevelopment(player, facilities, seasonStats, random)
        if (deltas.isEmpty()) return player

        val currentMap = player.attributes.toMap().toMutableMap()
        for ((attr, delta) in deltas) {
            val newVal = (currentMap.getValue(attr) + delta).coerceIn(MIN_ATTRIBUTE, MAX_ATTRIBUTE)
            currentMap[attr] = newVal
        }

        return player.copy(
            attributes = PlayerAttributes(currentMap),
        )
    }

    /**
     * Determines which attributes are prioritized for training focus.
     */
    fun getTargetAttributes(focus: TrainingFocus, player: Player): List<Attribute> {
        return when (focus) {
            TrainingFocus.ATTACKING -> listOf(Attribute.SHOOTING, Attribute.DRIBBLING, Attribute.PACE)
            TrainingFocus.DEFENDING -> listOf(Attribute.DEFENDING, Attribute.PHYSICAL, Attribute.PACE)
            TrainingFocus.PHYSICAL -> listOf(Attribute.PHYSICAL, Attribute.PACE)
            TrainingFocus.TECHNICAL -> listOf(Attribute.PASSING, Attribute.DRIBBLING, Attribute.SHOOTING)
            TrainingFocus.GOALKEEPING -> listOf(Attribute.GOALKEEPER, Attribute.PHYSICAL)
            TrainingFocus.BALANCED -> {
                val bestPos = player.bestPosition()
                when {
                    bestPos.name == "GK" -> listOf(Attribute.GOALKEEPER, Attribute.PHYSICAL)
                    bestPos.name.endsWith("B") || bestPos.name == "DM" -> listOf(Attribute.DEFENDING, Attribute.PHYSICAL, Attribute.PASSING)
                    bestPos.name in listOf("CM", "AM", "LM", "RM") -> listOf(Attribute.PASSING, Attribute.DRIBBLING, Attribute.PACE)
                    else -> listOf(Attribute.SHOOTING, Attribute.PACE, Attribute.DRIBBLING)
                }
            }
        }
    }
}
