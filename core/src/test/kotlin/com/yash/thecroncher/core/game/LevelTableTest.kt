package com.yash.thecroncher.core.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The arcade's per-level speed, fright and toy progression. */
class LevelTableTest {

    @Test
    fun `level one matches the arcade`() {
        val l = LevelTable.forLevel(1)
        assertEquals(0.80, l.croncherSpeed, 1e-9)
        assertEquals(0.75, l.ghostSpeed, 1e-9)
        assertEquals(6.0, l.frightSeconds, 1e-9)
        assertEquals(5, l.frightFlashes)
    }

    @Test
    fun `the game gets faster and then stops getting faster`() {
        assertTrue(LevelTable.forLevel(2).croncherSpeed > LevelTable.forLevel(1).croncherSpeed)
        assertEquals(LevelTable.forLevel(21).croncherSpeed, LevelTable.forLevel(99).croncherSpeed, 1e-9)
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
    fun `the toy follows the arcade sequence and then repeats`() {
        assertEquals(Toy.YARN, LevelTable.toyForLevel(1))
        assertEquals(Toy.MILK, LevelTable.toyForLevel(2))
        assertEquals(Toy.FISH, LevelTable.toyForLevel(3))
        assertEquals(Toy.GOLDFISH, LevelTable.toyForLevel(13))
        assertEquals(
            "level 14 and beyond stay on the golden fish",
            Toy.GOLDFISH, LevelTable.toyForLevel(14),
        )
        assertEquals(Toy.GOLDFISH, LevelTable.toyForLevel(40))
    }

    @Test
    fun `toy values ascend`() {
        val values = listOf(
            Toy.YARN, Toy.MILK, Toy.FISH, Toy.MOUSE,
            Toy.FEATHER, Toy.BIRD, Toy.BELL, Toy.GOLDFISH,
        ).map { it.points }
        assertEquals(values.sorted(), values)
        assertEquals(100, Toy.YARN.points)
        assertEquals(5000, Toy.GOLDFISH.points)
    }
}
