package com.yash.thecroncher.core.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The three settings tiers. They are data, so retuning them — or adding a fourth —
 * is an edit here rather than a change to the game.
 */
class DifficultyTest {

    @Test
    fun `three tiers are offered, normal by default`() {
        assertEquals(3, Difficulties.all.size)
        assertEquals(Difficulties.NORMAL, Difficulties.default)
    }

    @Test
    fun `ids are unique and stable for persistence`() {
        val ids = Difficulties.all.map { it.id }
        assertEquals(ids.size, ids.distinct().size)
        assertEquals(listOf("easy", "normal", "hard"), ids)
    }

    @Test
    fun `lives match the specification`() {
        assertEquals(5, Difficulties.EASY.lives)
        assertEquals(3, Difficulties.NORMAL.lives)
        assertEquals(2, Difficulties.HARD.lives)
    }

    @Test
    fun `ghost speed scales as specified`() {
        assertEquals(0.85, Difficulties.EASY.ghostSpeedScale, 1e-9)
        assertEquals(1.00, Difficulties.NORMAL.ghostSpeedScale, 1e-9)
        assertEquals(1.10, Difficulties.HARD.ghostSpeedScale, 1e-9)
    }

    @Test
    fun `easy and hard pin the fright duration, normal follows the arcade table`() {
        assertEquals(9.0, Difficulties.EASY.frightSecondsOverride!!, 1e-9)
        assertEquals(3.0, Difficulties.HARD.frightSecondsOverride!!, 1e-9)
        assertNull("normal must defer to the level table", Difficulties.NORMAL.frightSecondsOverride)
    }

    @Test
    fun `difficulty is ordered from kindest to harshest`() {
        val byLives = Difficulties.all.sortedByDescending { it.lives }
        assertEquals(Difficulties.all, byLives)
        assertTrue(Difficulties.EASY.ghostSpeedScale < Difficulties.HARD.ghostSpeedScale)
        assertTrue(Difficulties.EASY.scatterScale > Difficulties.HARD.scatterScale)
    }

    @Test
    fun `lookup by id falls back to the default`() {
        assertNotNull(Difficulties.byId("hard"))
        assertEquals(Difficulties.HARD, Difficulties.byId("hard"))
        assertEquals(Difficulties.default, Difficulties.byIdOrDefault("nonsense"))
        assertEquals(Difficulties.default, Difficulties.byIdOrDefault(null))
    }

    @Test
    fun `fright duration resolves per difficulty`() {
        // Level 1 in the arcade table is six seconds.
        assertEquals(6.0, Difficulties.NORMAL.frightSeconds(level = 1), 1e-9)
        assertEquals(9.0, Difficulties.EASY.frightSeconds(level = 1), 1e-9)
        assertEquals(3.0, Difficulties.HARD.frightSeconds(level = 1), 1e-9)
    }

    @Test
    fun `fright can reach zero on a late level for normal but never for easy`() {
        assertEquals(0.0, Difficulties.NORMAL.frightSeconds(level = 19), 1e-9)
        assertEquals(9.0, Difficulties.EASY.frightSeconds(level = 19), 1e-9)
    }
}
