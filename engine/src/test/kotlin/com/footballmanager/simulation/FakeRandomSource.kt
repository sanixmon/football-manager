package com.footballmanager.simulation

/**
 * Test [RandomSource] that replays a fixed sequence of doubles, cycling when
 * exhausted. [nextInt] is derived from [nextDouble] for simplicity.
 */
class FakeRandomSource(
    private val values: List<Double>,
) : RandomSource {
    init {
        require(values.isNotEmpty()) { "FakeRandomSource needs at least one value" }
    }

    private var index = 0

    override fun nextDouble(): Double = values[index++ % values.size]

    override fun nextInt(from: Int, until: Int): Int {
        val offset = (nextDouble() * (until - from)).toInt().coerceIn(0, until - from - 1)
        return from + offset
    }
}
