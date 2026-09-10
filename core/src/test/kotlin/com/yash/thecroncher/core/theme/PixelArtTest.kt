package com.yash.thecroncher.core.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The art in this game is hand-drawn as ASCII grids and coloured through a
 * palette, so a character is legible in the source and restyling it is a colour
 * change rather than a redraw. These tests guard the grid reader itself: a
 * mistyped row or a colour with no ink must fail here, not on the television.
 */
class PixelArtTest {

    private val ink = mapOf('a' to RED, 'b' to BLUE)

    private val square = listOf(
        "aab.",
        "aab.",
        "bbb.",
        "....",
    )

    @Test
    fun `a grid becomes a sprite of the same shape`() {
        val sprite = PixelArt.sprite(square, ink)
        assertEquals(4, sprite.width)
        assertEquals(4, sprite.height)
        assertEquals(RED, sprite.pixelAt(0, 0))
        assertEquals(BLUE, sprite.pixelAt(2, 0))
        assertEquals(Sprite.TRANSPARENT, sprite.pixelAt(3, 0))
    }

    @Test
    fun `dots and spaces are both transparent so grids can be drawn either way`() {
        val sprite = PixelArt.sprite(listOf("a. ", "   "), ink)
        assertEquals(RED, sprite.pixelAt(0, 0))
        assertEquals(Sprite.TRANSPARENT, sprite.pixelAt(1, 0))
        assertEquals(Sprite.TRANSPARENT, sprite.pixelAt(2, 0))
    }

    @Test
    fun `a ragged grid is rejected rather than silently padded`() {
        val error = runCatching { PixelArt.sprite(listOf("aa", "a"), ink) }.exceptionOrNull()
        assertTrue("expected a complaint about row width, got $error", error is IllegalArgumentException)
    }

    @Test
    fun `a character with no ink is rejected so a typo cannot go unnoticed`() {
        val error = runCatching { PixelArt.sprite(listOf("az"), ink) }.exceptionOrNull()
        assertTrue("expected a complaint about the unknown ink, got $error", error is IllegalArgumentException)
        assertTrue(error!!.message!!.contains("z"))
    }

    @Test
    fun `mirroring flips left to right and leaves the size alone`() {
        val sprite = PixelArt.mirrored(PixelArt.sprite(square, ink))
        assertEquals(4, sprite.width)
        assertEquals(Sprite.TRANSPARENT, sprite.pixelAt(0, 0))
        assertEquals(BLUE, sprite.pixelAt(1, 0))
        assertEquals(RED, sprite.pixelAt(3, 0))
    }

    @Test
    fun `shifting moves the art and drops whatever falls off the edge`() {
        val sprite = PixelArt.shifted(PixelArt.sprite(square, ink), dx = 0, dy = 1)
        assertEquals(Sprite.TRANSPARENT, sprite.pixelAt(0, 0))
        assertEquals(RED, sprite.pixelAt(0, 1))
        assertEquals(4, sprite.height)
    }

    @Test
    fun `a silhouette keeps the shape and replaces every colour`() {
        val sprite = PixelArt.silhouette(PixelArt.sprite(square, ink), GREEN)
        assertEquals(GREEN, sprite.pixelAt(0, 0))
        assertEquals(GREEN, sprite.pixelAt(2, 0))
        assertEquals(Sprite.TRANSPARENT, sprite.pixelAt(3, 0))
    }

    @Test
    fun `overlaying draws a second grid on top of the first`() {
        val base = PixelArt.sprite(square, ink)
        val result = PixelArt.overlaid(base, listOf("....", "....", "....", "aaaa"), ink)
        assertEquals(RED, result.pixelAt(0, 3))
        assertEquals("the base must show through where the overlay is blank", RED, result.pixelAt(0, 0))
    }

    @Test
    fun `overlaying within the shape cannot spill outside it`() {
        val base = PixelArt.sprite(square, ink)
        val before = base.pixels.count { (it ushr 24) != 0 }
        val result = PixelArt.overlaidWithin(base, PixelArt.sprite(List(4) { "bbbb" }, ink))
        assertEquals("the silhouette must not grow", before, result.pixels.count { (it ushr 24) != 0 })
        assertEquals(BLUE, result.pixelAt(0, 0))
        assertEquals("nothing may appear in the empty corner", Sprite.TRANSPARENT, result.pixelAt(3, 0))
    }

    @Test
    fun `dissolving removes more of the art the further it goes`() {
        val base = PixelArt.sprite(List(8) { "aaaaaaaa" }, ink)
        fun visible(amount: Double) =
            PixelArt.dissolved(base, amount).pixels.count { (it ushr 24) != 0 }

        assertEquals("nothing should go at zero", 64, visible(0.0))
        assertTrue(visible(0.5) in 1 until 64)
        assertTrue("a heavy dissolve must leave less than a light one", visible(0.9) < visible(0.5))
        assertTrue("something must always remain, or the sprite flickers out", visible(1.0) >= 1)
    }

    @Test
    fun `dissolving is stable so a frame does not shimmer between draws`() {
        val base = PixelArt.sprite(List(8) { "aaaaaaaa" }, ink)
        val once = PixelArt.dissolved(base, 0.4).pixels
        val twice = PixelArt.dissolved(base, 0.4).pixels
        assertTrue(once.contentEquals(twice))
        assertNotEquals(once.toList(), PixelArt.dissolved(base, 0.6).pixels.toList())
    }

    private companion object {
        const val RED = 0xFFFF0000.toInt()
        const val BLUE = 0xFF0000FF.toInt()
        const val GREEN = 0xFF00FF00.toInt()
    }
}
