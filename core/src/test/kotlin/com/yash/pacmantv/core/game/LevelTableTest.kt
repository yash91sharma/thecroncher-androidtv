package com.yash.pacmantv.core.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The arcade's per-level speed, fright and fruit progression. */
class LevelTableTest {

    @Test
    fun `level one matches the arcade`() {
        val l = LevelTable.forLevel(1)
        assertEquals(0.80, l.pacmanSpeed, 1e-9)
        assertEquals(0.75, l.ghostSpeed, 1e-9)
        assertEquals(6.0, l.frightSeconds, 1e-9)
        assertEquals(5, l.frightFlashes)
    }

    @Test
    fun `the game gets faster and then stops getting faster`() {
        assertTrue(LevelTable.forLevel(2).pacmanSpeed > LevelTable.forLevel(1).pacmanSpeed)
        assertEquals(LevelTable.forLevel(21).pacmanSpeed, LevelTable.forLevel(99).pacmanSpeed, 1e-9)
    }

    @Test
    fun `fright time shrinks to nothing on the late levels`() {
        assertTrue(LevelTable.forLevel(1).frightSeconds > LevelTable.forLevel(5).frightSeconds)
        assertEquals(0.0, LevelTable.forLevel(19).frightSeconds, 1e-9)
        assertEquals(0.0, LevelTable.forLevel(50).frightSeconds, 1e-9)
    }

    @Test
    fun `ghosts in the tunnel are always slower than in the open`() {
        for (level in 1..25) {
            val l = LevelTable.forLevel(level)
            assertTrue("level $level", l.ghostTunnelSpeed < l.ghostSpeed)
        }
    }

    @Test
    fun `level zero and negatives are clamped rather than crashing`() {
        assertEquals(LevelTable.forLevel(1), LevelTable.forLevel(0))
        assertEquals(LevelTable.forLevel(1), LevelTable.forLevel(-5))
    }

    @Test
    fun `fruit follows the arcade sequence and then repeats`() {
        assertEquals(Fruit.CHERRY, LevelTable.fruitForLevel(1))
        assertEquals(Fruit.STRAWBERRY, LevelTable.fruitForLevel(2))
        assertEquals(Fruit.ORANGE, LevelTable.fruitForLevel(3))
        assertEquals(Fruit.KEY, LevelTable.fruitForLevel(13))
        assertEquals("level 14 and beyond stay on the key", Fruit.KEY, LevelTable.fruitForLevel(14))
        assertEquals(Fruit.KEY, LevelTable.fruitForLevel(40))
    }

    @Test
    fun `fruit values ascend`() {
        val values = listOf(
            Fruit.CHERRY, Fruit.STRAWBERRY, Fruit.ORANGE, Fruit.APPLE,
            Fruit.MELON, Fruit.GALAXIAN, Fruit.BELL, Fruit.KEY,
        ).map { it.points }
        assertEquals(values.sorted(), values)
        assertEquals(100, Fruit.CHERRY.points)
        assertEquals(5000, Fruit.KEY.points)
    }
}
