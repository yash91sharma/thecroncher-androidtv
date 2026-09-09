package com.yash.pacmantv.core.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Arcade Pac-Man's logic runs at 60 Hz, so one tick is one arcade frame. Keeping
 * the simulation on a fixed timestep is what makes the timings faithful AND what
 * makes the whole game deterministic enough to unit-test.
 */
class GameLoopTest {

    @Test
    fun `one tick worth of time yields exactly one tick`() {
        val loop = GameLoop()
        assertEquals(1, loop.advance(GameLoop.TICK_NANOS))
        assertEquals(1L, loop.totalTicks)
    }

    @Test
    fun `half a tick yields nothing but is not thrown away`() {
        val loop = GameLoop()
        assertEquals(0, loop.advance(GameLoop.TICK_NANOS / 2))
        assertEquals(0L, loop.totalTicks)
        // the remainder carries over, so the next half-tick completes it
        assertEquals(1, loop.advance(GameLoop.TICK_NANOS / 2))
        assertEquals(1L, loop.totalTicks)
    }

    @Test
    fun `three ticks worth of time yields three ticks in one call`() {
        val loop = GameLoop()
        assertEquals(3, loop.advance(GameLoop.TICK_NANOS * 3))
    }

    @Test
    fun `no drift over ten thousand frames`() {
        val loop = GameLoop()
        repeat(10_000) { loop.advance(GameLoop.TICK_NANOS) }
        assertEquals(10_000L, loop.totalTicks)
    }

    @Test
    fun `a long stall is clamped instead of spiralling`() {
        val loop = GameLoop()
        // Ten seconds of stall must not try to run 600 ticks in one frame.
        val ticks = loop.advance(GameLoop.TICK_NANOS * 600)
        assertEquals(GameLoop.MAX_TICKS_PER_FRAME, ticks)
        // and the backlog must be dropped, not carried as permanent debt
        assertEquals(0, loop.advance(0))
    }

    @Test
    fun `alpha stays in range for interpolation`() {
        val loop = GameLoop()
        assertEquals(0f, loop.alpha(), 1e-6f)
        loop.advance(GameLoop.TICK_NANOS / 2)
        val a = loop.alpha()
        assertTrue("alpha=$a", a >= 0f && a < 1f)
        assertEquals(0.5f, a, 0.01f)
    }

    @Test
    fun `negative or zero elapsed time is ignored`() {
        val loop = GameLoop()
        assertEquals(0, loop.advance(0))
        assertEquals(0, loop.advance(-1_000_000))
        assertEquals(0L, loop.totalTicks)
    }

    @Test
    fun `sixty ticks is approximately one second`() {
        val loop = GameLoop()
        repeat(60) { loop.advance(GameLoop.TICK_NANOS) }
        val elapsed = GameLoop.TICK_NANOS * 60
        assertTrue(
            "60 ticks should span ~1s, was ${elapsed / 1e9}s",
            elapsed in 995_000_000..1_005_000_000,
        )
    }
}
