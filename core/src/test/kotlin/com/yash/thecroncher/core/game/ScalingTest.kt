package com.yash.thecroncher.core.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The playfield is drawn once at arcade resolution and blown up by a whole-number
 * factor, which is what keeps the pixels sharp on a 77" panel instead of a blurry
 * bilinear smear. These are the numbers that must come out.
 */
class ScalingTest {

    @Test
    fun `1080p gives scale 3`() {
        assertEquals(3, Scaling.computeScale(1920, 1080))
    }

    @Test
    fun `4k gives scale 7`() {
        assertEquals(7, Scaling.computeScale(3840, 2160))
    }

    @Test
    fun `720p gives scale 2`() {
        assertEquals(2, Scaling.computeScale(1280, 720))
    }

    @Test
    fun `scale is never zero even on an absurdly small surface`() {
        assertEquals(1, Scaling.computeScale(10, 10))
        assertEquals(1, Scaling.computeScale(0, 0))
    }

    @Test
    fun `height is the limiting dimension on every 16 by 9 surface`() {
        // 224x288 is taller than it is wide, so a 16:9 surface always runs out of
        // vertical room first. If this ever fails, the letterboxing is wrong.
        for ((w, h) in listOf(1280 to 720, 1920 to 1080, 2560 to 1440, 3840 to 2160)) {
            assertEquals(h / VIRTUAL_HEIGHT, Scaling.computeScale(w, h))
        }
    }

    @Test
    fun `viewport is centred with the playfield fully inside the surface`() {
        val vp = Scaling.viewport(1920, 1080)
        assertEquals(3, vp.scale)
        assertEquals(VIRTUAL_WIDTH * 3, vp.width)
        assertEquals(VIRTUAL_HEIGHT * 3, vp.height)
        assertEquals((1920 - 672) / 2, vp.x)
        assertEquals((1080 - 864) / 2, vp.y)
        assertTrue(vp.x >= 0 && vp.y >= 0)
        assertTrue(vp.x + vp.width <= 1920)
        assertTrue(vp.y + vp.height <= 1080)
    }

    @Test
    fun `4k viewport matches the sizes quoted in the plan`() {
        val vp = Scaling.viewport(3840, 2160)
        assertEquals(1568, vp.width)
        assertEquals(2016, vp.height)
        assertEquals(72, vp.y)
    }
}
