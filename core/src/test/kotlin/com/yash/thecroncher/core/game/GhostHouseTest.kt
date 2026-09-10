package com.yash.thecroncher.core.game

import com.yash.thecroncher.core.ports.SeededRng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Ghosts leave the house one at a time, gated by how many dots the croncher has eaten.
 * It is the game's difficulty ramp for the opening seconds of a life: eat quickly
 * and you bring all four out early.
 */
class GhostHouseTest {

    private fun newGame() = GameState(
        maze = Maze.loadClassic(),
        difficulty = Difficulties.NORMAL,
        rng = SeededRng(42),
    ).apply { startNewGame() }

    @Test
    fun `blinky begins outside and the other three begin inside`() {
        val g = newGame()
        assertFalse(g.ghost(GhostKind.BLINKY).mode.isInsideHouse)
        for (kind in listOf(GhostKind.PINKY, GhostKind.INKY, GhostKind.CLYDE)) {
            assertTrue("$kind should start in the house", g.ghost(kind).mode.isInsideHouse)
        }
    }

    @Test
    fun `the dot thresholds are pinky zero, inky thirty, clyde sixty`() {
        assertEquals(0, GameState.houseDotLimit(GhostKind.PINKY))
        assertEquals(30, GameState.houseDotLimit(GhostKind.INKY))
        assertEquals(60, GameState.houseDotLimit(GhostKind.CLYDE))
    }

    @Test
    fun `pinky is released straight away`() {
        val g = newGame()
        repeat(GameState.READY_TICKS + 240) { g.tick() }
        assertFalse("pinky should be out by now", g.ghost(GhostKind.PINKY).mode.isInsideHouse)
    }

    @Test
    fun `inky waits for thirty dots and clyde for sixty`() {
        val g = newGame()
        repeat(GameState.READY_TICKS) { g.tick() }

        g.debugSetDotsEaten(29)
        repeat(120) { g.tick() }
        assertTrue("inky must still be waiting at 29 dots",
            g.ghost(GhostKind.INKY).mode.isInsideHouse || g.dotsEaten >= 30)

        g.debugSetDotsEaten(60)
        repeat(600) { g.tick() }
        assertFalse("clyde should be out at 60 dots", g.ghost(GhostKind.CLYDE).mode.isInsideHouse)
    }

    @Test
    fun `ghosts leave in order - pinky then inky then clyde`() {
        val g = newGame()
        repeat(GameState.READY_TICKS) { g.tick() }
        val order = mutableListOf<GhostKind>()
        repeat(4000) {
            g.debugSetDotsEaten(minOf(60, g.dotsEaten + 1))
            g.tick()
            for (kind in listOf(GhostKind.PINKY, GhostKind.INKY, GhostKind.CLYDE)) {
                if (!g.ghost(kind).mode.isInsideHouse && kind !in order) order += kind
            }
        }
        assertEquals(listOf(GhostKind.PINKY, GhostKind.INKY, GhostKind.CLYDE), order)
    }

    @Test
    fun `a ghost that gets out never wanders back inside on its own`() {
        val g = newGame()
        repeat(GameState.READY_TICKS) { g.tick() }
        g.debugSetDotsEaten(60)
        repeat(1200) { g.tick() }

        val blinky = g.ghost(GhostKind.BLINKY)
        repeat(1200) {
            g.tick()
            if (blinky.mode != GhostMode.EATEN) {
                assertFalse(
                    "blinky drifted into the house while ${blinky.mode}",
                    blinky.mode == GhostMode.IN_HOUSE,
                )
            }
        }
    }
}
