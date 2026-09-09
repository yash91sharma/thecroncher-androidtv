package com.yash.pacmantv.core.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Ghosts alternate between scattering to their corners and hunting, on a fixed
 * schedule that tightens as the levels go on. The rhythm of those waves is most of
 * what gives the game its pacing — without it the ghosts simply never let up.
 */
class ScatterChaseTest {

    private fun schedule(level: Int = 1, scatterScale: Double = 1.0) =
        ModeSchedule(level, scatterScale)

    private fun seconds(s: Double) = (s * GameLoop.TICKS_PER_SECOND).toLong()

    @Test
    fun `a level opens by scattering`() {
        assertEquals(GhostMode.SCATTER, schedule().modeAt(0))
    }

    @Test
    fun `the first wave is seven seconds of scatter`() {
        val s = schedule()
        assertEquals(GhostMode.SCATTER, s.modeAt(seconds(7.0) - 1))
        assertEquals(GhostMode.CHASE, s.modeAt(seconds(7.0)))
    }

    @Test
    fun `scatter returns after twenty seconds of chase`() {
        val s = schedule()
        assertEquals(GhostMode.CHASE, s.modeAt(seconds(27.0) - 1))
        assertEquals(GhostMode.SCATTER, s.modeAt(seconds(27.0)))
    }

    @Test
    fun `the last phase is an endless chase`() {
        val s = schedule()
        assertEquals(GhostMode.CHASE, s.modeAt(seconds(1000.0)))
        assertEquals(GhostMode.CHASE, s.modeAt(Long.MAX_VALUE / 2))
    }

    @Test
    fun `later levels scatter for less time`() {
        val early = schedule(level = 1)
        val late = schedule(level = 5)
        assertTrue(late.phaseDurationTicks(0) < early.phaseDurationTicks(0))
    }

    @Test
    fun `an easier difficulty stretches the scatter phases but not the chases`() {
        val normal = schedule(scatterScale = 1.0)
        val easy = schedule(scatterScale = 1.5)
        assertTrue(easy.phaseDurationTicks(0) > normal.phaseDurationTicks(0))
        assertEquals(normal.phaseDurationTicks(1), easy.phaseDurationTicks(1))
    }

    @Test
    fun `phases alternate strictly between scatter and chase`() {
        val s = schedule()
        for (i in 0 until s.phaseCount) {
            val expected = if (i % 2 == 0) GhostMode.SCATTER else GhostMode.CHASE
            assertEquals("phase $i", expected, s.phaseMode(i))
        }
    }

    @Test
    fun `a mode change forces the ghosts to turn around`() {
        // The reversal on every scatter-to-chase transition is what makes the
        // ghosts suddenly wheel about; players read it as the game changing gear.
        val s = schedule()
        assertTrue(s.isTransitionTick(seconds(7.0)))
        assertTrue(s.isTransitionTick(seconds(27.0)))
        assertTrue(!s.isTransitionTick(seconds(7.0) + 1))
        assertTrue(!s.isTransitionTick(0))
    }
}
