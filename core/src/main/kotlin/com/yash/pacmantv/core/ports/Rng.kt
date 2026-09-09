package com.yash.pacmantv.core.ports

import kotlin.random.Random

/**
 * Randomness as a dependency rather than a global, so a test can seed it and get
 * the exact same game every time. The golden regression test depends on this.
 */
interface Rng {
    fun nextInt(bound: Int): Int
    fun nextFloat(): Float
    fun nextBoolean(): Boolean
}

class SeededRng(seed: Long) : Rng {
    private val random = Random(seed)
    override fun nextInt(bound: Int): Int = if (bound <= 0) 0 else random.nextInt(bound)
    override fun nextFloat(): Float = random.nextFloat()
    override fun nextBoolean(): Boolean = random.nextBoolean()
}
