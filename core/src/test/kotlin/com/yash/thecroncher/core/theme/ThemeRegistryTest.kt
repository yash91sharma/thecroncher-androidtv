package com.yash.thecroncher.core.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Themes are the main extension point: adding one should be a new file plus a
 * registry line. These tests are the safety net that makes that cheap — a theme
 * with a forgotten colour slot fails here rather than rendering an invisible
 * maze on the television.
 *
 * The game ships with exactly one, and is named after it.
 */
class ThemeRegistryTest {

    @Test
    fun `the croncher is the only theme, and it is the default`() {
        assertEquals(1, ThemeRegistry.all.size)
        assertTrue(ThemeRegistry.all.contains(ThemeRegistry.default))
        assertEquals("croncher", ThemeRegistry.default.id)
    }

    @Test
    fun `theme ids are unique`() {
        val ids = ThemeRegistry.all.map { it.id }
        assertEquals(ids.size, ids.distinct().size)
    }

    @Test
    fun `every theme has a non-blank id and display name`() {
        for (theme in ThemeRegistry.all) {
            assertTrue(theme.id.isNotBlank())
            assertTrue("${theme.id} has no display name", theme.displayName.isNotBlank())
        }
    }

    @Test
    fun `every colour in every theme is fully opaque`() {
        // A slot left at 0 would be transparent black — invisible, and maddening to
        // debug on a 77" screen. Catch it here instead.
        for (theme in ThemeRegistry.all) {
            for ((slot, colour) in theme.allColours()) {
                val alpha = colour ushr 24
                assertEquals("${theme.id}.$slot is not opaque (alpha=$alpha)", 0xFF, alpha)
            }
        }
    }

    @Test
    fun `every colour slot in the palette is accounted for`() {
        // allColours is what the opacity check walks, so a slot missing from it is
        // a slot nothing is checking.
        val palette = ThemeRegistry.default.palette
        val named = palette.allColours().map { it.second }.toSet()
        for (colour in palette.ink.values) {
            assertTrue("an ink colour is not listed in allColours", colour in named)
        }
    }

    @Test
    fun `every theme resolves every sprite`() {
        for (theme in ThemeRegistry.all) {
            for (id in SpriteId.values()) {
                for (frame in 0 until id.frameCount) {
                    val sprite = theme.sprites.sprite(id, frame)
                    assertTrue("${theme.id} is missing $id frame $frame", sprite.pixels.isNotEmpty())
                }
            }
        }
    }

    @Test
    fun `lookup by id works and unknown ids fall back to the default`() {
        val first = ThemeRegistry.all.first()
        assertNotNull(ThemeRegistry.byId(first.id))
        assertNull(ThemeRegistry.byId("no-such-theme"))
        assertEquals(ThemeRegistry.default, ThemeRegistry.byIdOrDefault("no-such-theme"))
        assertEquals(ThemeRegistry.default, ThemeRegistry.byIdOrDefault(null))
        assertEquals(first, ThemeRegistry.byIdOrDefault(first.id))
    }

    @Test
    fun `the four foes are four different colours so they are tellable apart`() {
        for (theme in ThemeRegistry.all) {
            val bodies = with(theme.palette) {
                listOf(dogFur, vacuumBody, sprayBottle, cucumberBody)
            }
            assertEquals("${theme.id} reuses a foe colour", 4, bodies.distinct().size)
            assertTrue(
                "${theme.id} paints a foe the same grey as the cat",
                theme.palette.catFur !in bodies,
            )
        }
    }

    @Test
    fun `no theme paints static hud text pure white`() {
        // OLED burn-in guard for the LG C3: the score and lives sit still for a
        // long time, so they must never be full-brightness white.
        for (theme in ThemeRegistry.all) {
            assertTrue(
                "${theme.id} uses pure white for HUD text",
                theme.hud.text != 0xFFFFFFFF.toInt(),
            )
            assertTrue(
                "${theme.id} uses pure white for the score",
                theme.hud.score != 0xFFFFFFFF.toInt(),
            )
        }
    }
}
