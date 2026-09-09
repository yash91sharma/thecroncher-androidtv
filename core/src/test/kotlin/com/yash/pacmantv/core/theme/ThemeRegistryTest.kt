package com.yash.pacmantv.core.theme

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
 */
class ThemeRegistryTest {

    @Test
    fun `at least the three shipped themes are registered`() {
        assertTrue(ThemeRegistry.all.size >= 3)
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
    fun `the default theme is registered`() {
        assertTrue(ThemeRegistry.all.contains(ThemeRegistry.default))
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
    fun `themes are visually distinct from one another`() {
        val walls = ThemeRegistry.all.map { it.maze.wall }
        assertEquals("two themes share a maze colour", walls.size, walls.distinct().size)
    }

    @Test
    fun `ghost colours differ within a theme so the four are tellable apart`() {
        for (theme in ThemeRegistry.all) {
            val ghosts = with(theme.entities) { listOf(blinky, pinky, inky, clyde) }
            assertEquals("${theme.id} reuses a ghost colour", 4, ghosts.distinct().size)
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
        }
    }
}
