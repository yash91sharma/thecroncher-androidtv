package com.yash.pacmantv.core.ui

import com.yash.pacmantv.core.ports.Align
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FontTest {

    @Test
    fun `every glyph is exactly five by seven`() {
        for (c in "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 .,:-_/!?'()<>%+=*") {
            val rows = Font.glyph(c)
            assertEquals("$c has wrong height", Font.GLYPH_HEIGHT, rows.size)
            for (row in rows) {
                assertEquals("$c row '$row' has wrong width", Font.GLYPH_WIDTH, row.length)
            }
        }
    }

    @Test
    fun `the alphabet and digits are all covered`() {
        for (c in 'A'..'Z') assertTrue("missing $c", Font.hasGlyph(c))
        for (c in '0'..'9') assertTrue("missing $c", Font.hasGlyph(c))
    }

    @Test
    fun `lowercase maps onto the uppercase glyph`() {
        assertTrue(Font.glyph('a').contentEquals(Font.glyph('A')))
    }

    @Test
    fun `an unknown character falls back to a box rather than vanishing`() {
        assertFalse(Font.hasGlyph('©'))
        val glyph = Font.glyph('©')
        assertEquals(Font.GLYPH_HEIGHT, glyph.size)
        assertTrue(glyph.any { it.contains('#') })
    }

    @Test
    fun `measure accounts for inter-character spacing`() {
        assertEquals(0, Font.measure(""))
        assertEquals(Font.GLYPH_WIDTH, Font.measure("A"))
        assertEquals(Font.GLYPH_WIDTH * 2 + 1, Font.measure("AB"))
    }

    @Test
    fun `alignment positions text as expected`() {
        val text = "SCORE"
        val w = Font.measure(text)
        assertEquals(100, Font.originFor(text, 100, Align.LEFT))
        assertEquals(100 - w / 2, Font.originFor(text, 100, Align.CENTER))
        assertEquals(100 - w, Font.originFor(text, 100, Align.RIGHT))
    }

    @Test
    fun `a space lights no pixels but still advances the cursor`() {
        var lit = 0
        Font.forEachPixel(" ", 0, 0, Align.LEFT) { _, _ -> lit++ }
        assertEquals(0, lit)
        assertEquals(Font.GLYPH_WIDTH, Font.measure(" "))
    }

    @Test
    fun `rendered pixels stay inside the measured bounds`() {
        val text = "HIGH SCORE 1234"
        val left = 20
        val top = 8
        var minX = Int.MAX_VALUE; var maxX = Int.MIN_VALUE
        var minY = Int.MAX_VALUE; var maxY = Int.MIN_VALUE
        Font.forEachPixel(text, left, top, Align.LEFT) { x, y ->
            if (x < minX) minX = x; if (x > maxX) maxX = x
            if (y < minY) minY = y; if (y > maxY) maxY = y
        }
        assertTrue(minX >= left)
        assertTrue(maxX < left + Font.measure(text))
        assertTrue(minY >= top)
        assertTrue(maxY < top + Font.GLYPH_HEIGHT)
    }

    @Test
    fun `centred text is centred about the anchor`() {
        val text = "PLAY"
        var minX = Int.MAX_VALUE; var maxX = Int.MIN_VALUE
        Font.forEachPixel(text, 112, 0, Align.CENTER) { x, _ ->
            if (x < minX) minX = x; if (x > maxX) maxX = x
        }
        val centre = (minX + maxX) / 2
        assertTrue("centre was $centre", kotlin.math.abs(centre - 112) <= 2)
    }
}
