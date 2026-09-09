package com.yash.pacmantv.core.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Menus are data. Adding a settings option should be one entry in a list, with no
 * new drawing or navigation code anywhere — these tests are what make that safe.
 */
class MenuModelTest {

    private var chosen: String? = null
    private var volume = 1

    private fun menu() = MenuModel(
        title = "SETTINGS",
        items = listOf(
            MenuItem("PLAY", ItemKind.Action { chosen = "PLAY"; Transition.None }),
            MenuItem(
                "VOLUME",
                ItemKind.Choice(listOf("LOW", "MID", "HIGH"), { volume }, { volume = it }),
            ),
            MenuItem("EXIT", ItemKind.Action { chosen = "EXIT"; Transition.Exit }),
        ),
    )

    @Test
    fun `the first item starts selected`() {
        assertEquals(0, menu().selectedIndex)
    }

    @Test
    fun `moving down and up walks the list`() {
        val m = menu()
        m.moveDown()
        assertEquals(1, m.selectedIndex)
        m.moveUp()
        assertEquals(0, m.selectedIndex)
    }

    @Test
    fun `selection wraps at both ends`() {
        val m = menu()
        m.moveUp()
        assertEquals("up from the top wraps to the bottom", 2, m.selectedIndex)
        m.moveDown()
        assertEquals("down from the bottom wraps to the top", 0, m.selectedIndex)
    }

    @Test
    fun `activating an action runs it and returns its transition`() {
        val m = menu()
        assertEquals(Transition.None, m.activate())
        assertEquals("PLAY", chosen)

        m.moveUp()      // wraps to EXIT
        assertEquals(Transition.Exit, m.activate())
        assertEquals("EXIT", chosen)
    }

    @Test
    fun `a choice cycles through its options with left and right`() {
        val m = menu()
        m.moveDown()                       // VOLUME, currently MID
        assertTrue(m.adjust(1))
        assertEquals(2, volume)
        assertTrue(m.adjust(-1))
        assertEquals(1, volume)
    }

    @Test
    fun `a choice wraps rather than sticking at the ends`() {
        val m = menu()
        m.moveDown()
        m.adjust(1); m.adjust(1)           // HIGH then wrap to LOW
        assertEquals(0, volume)
        m.adjust(-1)
        assertEquals("wrapping backwards lands on the last option", 2, volume)
    }

    @Test
    fun `adjusting an action item does nothing`() {
        val m = menu()
        assertFalse("actions have no value to change", m.adjust(1))
        assertNull(m.valueOf(0))
    }

    @Test
    fun `activating a choice steps it forward, so confirm works as well as right`() {
        val m = menu()
        m.moveDown()
        m.activate()
        assertEquals(2, volume)
    }

    @Test
    fun `the displayed value follows the underlying setting`() {
        val m = menu()
        assertEquals("MID", m.valueOf(1))
        volume = 0
        assertEquals("LOW", m.valueOf(1))
    }

    @Test
    fun `a value out of range is clamped rather than crashing`() {
        val m = menu()
        volume = 99
        assertEquals("HIGH", m.valueOf(1))
        volume = -3
        assertEquals("LOW", m.valueOf(1))
    }

    @Test
    fun `an empty menu is harmless`() {
        val m = MenuModel(title = null, items = emptyList())
        m.moveDown(); m.moveUp()
        assertEquals(0, m.selectedIndex)
        assertEquals(Transition.None, m.activate())
        assertFalse(m.adjust(1))
    }
}
