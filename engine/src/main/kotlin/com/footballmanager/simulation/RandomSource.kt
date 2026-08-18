package com.footballmanager.simulation

import kotlin.random.Random

/**
 * Abstraction over randomness so the match engine is deterministic and testable.
 */
interface RandomSource {
    /** Uniform value in [0.0, 1.0). */
    fun nextDouble(): Double

    /** Uniform integer in [from, until). */
    fun nextInt(from: Int, until: Int): Int
}

/** Production [RandomSource] backed by [kotlin.random.Random]. */
class KotlinRandomSource(
    private val random: Random = Random.Default,
) : RandomSource {
    override fun nextDouble(): Double = random.nextDouble()

    override fun nextInt(from: Int, until: Int): Int = random.nextInt(from, until)
}
