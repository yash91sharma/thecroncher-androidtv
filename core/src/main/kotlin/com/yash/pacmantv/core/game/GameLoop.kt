package com.yash.pacmantv.core.game

/**
 * Fixed-timestep accumulator running at the arcade's native 60 Hz, so one tick is
 * one arcade frame. Simulation never varies with the display's frame rate, which
 * is what keeps the ghost timings faithful and the whole game reproducible in
 * tests.
 */
class GameLoop {

    private var accumulator = 0L

    /** Ticks simulated since the loop started. */
    var totalTicks = 0L
        private set

    /**
     * Feed the wall-clock nanoseconds since the previous frame; returns how many
     * simulation ticks to run now. Leftover time is carried over, so no fraction
     * of a tick is ever lost.
     */
    fun advance(elapsedNanos: Long): Int {
        if (elapsedNanos > 0) accumulator += elapsedNanos

        var ticks = 0
        while (accumulator >= TICK_NANOS && ticks < MAX_TICKS_PER_FRAME) {
            accumulator -= TICK_NANOS
            ticks++
        }

        // After a long stall (app resumed, GC pause, TV woke up) the backlog is
        // dropped rather than carried: trying to catch up would spiral, and the
        // player would rather lose a moment than watch the game fast-forward.
        if (accumulator >= TICK_NANOS) accumulator = 0L

        totalTicks += ticks
        return ticks
    }

    /** How far into the current tick we are, 0..1, for render interpolation. */
    fun alpha(): Float = accumulator.toFloat() / TICK_NANOS

    /** Discards any partial tick — call when resuming after a pause. */
    fun reset() {
        accumulator = 0L
    }

    companion object {
        /** 60 Hz, the rate the original hardware ran at. */
        const val TICKS_PER_SECOND = 60

        const val TICK_NANOS = 1_000_000_000L / TICKS_PER_SECOND

        /** Cap on catch-up ticks in a single frame — the anti-spiral guard. */
        const val MAX_TICKS_PER_FRAME = 5
    }
}
