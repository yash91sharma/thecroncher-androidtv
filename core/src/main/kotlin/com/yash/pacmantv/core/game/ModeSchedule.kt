package com.yash.pacmantv.core.game

/**
 * The scatter/chase wave table for a level.
 *
 * Ghosts spend the level alternating between retreating to their corners and
 * hunting. Later levels scatter for less and less time until they effectively
 * never stop chasing. Every transition also forces the ghosts to reverse, which is
 * the visible "gear change" players learn to anticipate.
 */
class ModeSchedule(level: Int, scatterScale: Double = 1.0) {

    /** Phase durations in ticks; the final entry runs forever. */
    private val durations: LongArray

    init {
        val seconds: List<Double> = when {
            level <= 1 -> listOf(7.0, 20.0, 7.0, 20.0, 5.0, 20.0, 5.0, FOREVER)
            level <= 4 -> listOf(7.0, 20.0, 7.0, 20.0, 5.0, 1033.0, 1.0 / 60.0, FOREVER)
            else -> listOf(5.0, 20.0, 5.0, 20.0, 5.0, 1037.0, 1.0 / 60.0, FOREVER)
        }

        durations = LongArray(seconds.size) { i ->
            val s = seconds[i]
            if (s == FOREVER) Long.MAX_VALUE
            else {
                // Only the scatter phases (the even ones) stretch with difficulty.
                val scaled = if (i % 2 == 0) s * scatterScale else s
                (scaled * GameLoop.TICKS_PER_SECOND).toLong().coerceAtLeast(1)
            }
        }
    }

    val phaseCount: Int get() = durations.size

    fun phaseDurationTicks(index: Int): Long = durations[index]

    /** Even phases scatter, odd phases chase. */
    fun phaseMode(index: Int): GhostMode =
        if (index % 2 == 0) GhostMode.SCATTER else GhostMode.CHASE

    fun modeAt(elapsedTicks: Long): GhostMode = phaseMode(phaseIndexAt(elapsedTicks))

    fun phaseIndexAt(elapsedTicks: Long): Int {
        var remaining = elapsedTicks.coerceAtLeast(0)
        for (i in durations.indices) {
            if (durations[i] == Long.MAX_VALUE) return i
            if (remaining < durations[i]) return i
            remaining -= durations[i]
        }
        return durations.lastIndex
    }

    /** True on the exact tick a phase changes — when ghosts must turn around. */
    fun isTransitionTick(elapsedTicks: Long): Boolean {
        if (elapsedTicks <= 0) return false
        return phaseIndexAt(elapsedTicks) != phaseIndexAt(elapsedTicks - 1)
    }

    private companion object {
        const val FOREVER = -1.0
    }
}
