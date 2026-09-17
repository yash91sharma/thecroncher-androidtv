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
        maze = Maze.loadDefault(),
        difficulty = Difficulties.NORMAL,
        rng = SeededRng(42),
    ).apply { startNewGame() }

    @Test
    fun `chaser begins outside and the other three begin inside`() {
        val g = newGame()
        assertFalse(g.ghost(GhostKind.CHASER).mode.isInsideHouse)
        for (kind in listOf(GhostKind.AMBUSHER, GhostKind.FLANKER, GhostKind.COWARD)) {
            assertTrue("$kind should start in the house", g.ghost(kind).mode.isInsideHouse)
        }
    }

    @Test
    fun `the dot thresholds are ambusher zero, flanker thirty, coward sixty`() {
        assertEquals(0, GameState.houseDotLimit(GhostKind.AMBUSHER))
        assertEquals(30, GameState.houseDotLimit(GhostKind.FLANKER))
        assertEquals(60, GameState.houseDotLimit(GhostKind.COWARD))
    }

    @Test
    fun `ambusher is released straight away`() {
        val g = newGame()
        repeat(GameState.READY_TICKS + 240) { g.tick() }
        assertFalse("ambusher should be out by now", g.ghost(GhostKind.AMBUSHER).mode.isInsideHouse)
    }

    @Test
    fun `flanker waits for thirty dots and coward for sixty`() {
        val g = newGame()
        repeat(GameState.READY_TICKS) { g.tick() }

        g.debugSetDotsEaten(29)
        repeat(120) { g.tick() }
        assertTrue("flanker must still be waiting at 29 dots",
            g.ghost(GhostKind.FLANKER).mode.isInsideHouse || g.dotsEaten >= 30)

        // Asked "did he ever get out", not "is he out now": the croncher is
        // standing still in his pocket, so a ghost eventually catches him and the
        // whole cast is put back in the house.
        g.debugSetDotsEaten(60)
        var cowardGotOut = false
        repeat(600) {
            g.tick()
            if (!g.ghost(GhostKind.COWARD).mode.isInsideHouse) cowardGotOut = true
        }
        assertTrue("coward should be out at 60 dots", cowardGotOut)
    }

    @Test
    fun `ghosts leave in order - ambusher then flanker then coward`() {
        val g = newGame()
        repeat(GameState.READY_TICKS) { g.tick() }
        val order = mutableListOf<GhostKind>()
        repeat(4000) {
            g.debugSetDotsEaten(minOf(60, g.dotsEaten + 1))
            g.tick()
            for (kind in listOf(GhostKind.AMBUSHER, GhostKind.FLANKER, GhostKind.COWARD)) {
                if (!g.ghost(kind).mode.isInsideHouse && kind !in order) order += kind
            }
        }
        assertEquals(listOf(GhostKind.AMBUSHER, GhostKind.FLANKER, GhostKind.COWARD), order)
    }

    @Test
    fun `a ghost that gets out never wanders back inside on its own`() {
        val g = newGame()
        repeat(GameState.READY_TICKS) { g.tick() }
        g.debugSetDotsEaten(60)
        repeat(1200) { g.tick() }

        val chaser = g.ghost(GhostKind.CHASER)
        repeat(1200) {
            g.tick()
            if (chaser.mode != GhostMode.EATEN) {
                assertFalse(
                    "chaser drifted into the house while ${chaser.mode}",
                    chaser.mode == GhostMode.IN_HOUSE,
                )
            }
        }
    }
}
