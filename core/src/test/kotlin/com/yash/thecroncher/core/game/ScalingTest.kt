package com.yash.thecroncher.core.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The playfield is drawn once at 384x216 and blown up by a whole-number factor,
 * which is what keeps the pixels sharp on a 77" panel instead of a blurry
 * bilinear smear. 384x216 is exactly 16:9, so on a 16:9 panel it lands on a whole
 * multiple with nothing left over: x5 fills 1080p and x10 fills 4K edge to edge.
 */
class ScalingTest {

    @Test
    fun `1080p gives scale 5`() {
        assertEquals(5, Scaling.computeScale(1920, 1080))
    }

    @Test
    fun `4k gives scale 10`() {
        assertEquals(10, Scaling.computeScale(3840, 2160))
    }

    @Test
    fun `720p gives scale 3`() {
        assertEquals(3, Scaling.computeScale(1280, 720))
    }

    @Test
    fun `scale is never zero even on an absurdly small surface`() {
        assertEquals(1, Scaling.computeScale(10, 10))
        assertEquals(1, Scaling.computeScale(0, 0))
    }

    @Test
    fun `a 16 by 9 surface is filled exactly, with no letterboxing`() {
        // The framebuffer is itself 16:9, so width and height run out together.
        for ((w, h) in listOf(1280 to 720, 1920 to 1080, 2560 to 1440, 3840 to 2160)) {
            assertEquals(h / VIRTUAL_HEIGHT, Scaling.computeScale(w, h))
        }
    }

    @Test
    fun `viewport is centred with the playfield fully inside the surface`() {
        val vp = Scaling.viewport(1920, 1080)
        assertEquals(5, vp.scale)
        assertEquals(VIRTUAL_WIDTH * 5, vp.width)
        assertEquals(VIRTUAL_HEIGHT * 5, vp.height)
        assertEquals("1080p is filled exactly", 0, vp.x)
        assertEquals("1080p is filled exactly", 0, vp.y)
        assertTrue(vp.x >= 0 && vp.y >= 0)
        assertTrue(vp.x + vp.width <= 1920)
        assertTrue(vp.y + vp.height <= 1080)
    }

    @Test
    fun `4k is filled edge to edge`() {
        val vp = Scaling.viewport(3840, 2160)
        assertEquals(3840, vp.width)
        assertEquals(2160, vp.height)
        assertEquals(0, vp.x)
        assertEquals(0, vp.y)
    }
}
