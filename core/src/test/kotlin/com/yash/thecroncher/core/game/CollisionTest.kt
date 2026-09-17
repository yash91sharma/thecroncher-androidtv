package com.yash.thecroncher.core.game

import com.yash.thecroncher.core.ports.SeededRng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * What happens when the croncher and a ghost occupy the same tile — which, depending on
 * the ghost's mode, is either the best or the worst thing that can happen to him.
 */
class CollisionTest {

    private fun game(difficulty: Difficulty = Difficulties.NORMAL) = GameState(
        maze = Maze.loadDefault(),
        difficulty = difficulty,
        rng = SeededRng(3),
    ).apply {
        startNewGame()
        repeat(GameState.READY_TICKS) { tick() }
    }

    /** Drops a ghost straight onto the croncher. */
    private fun GameState.collide(kind: GhostKind) {
        val g = ghost(kind)
        g.placeAtTileCentre(croncher.tile(), Direction.LEFT)
    }

    @Test
    fun `a hunting ghost costs a life`() {
        val g = game()
        val livesBefore = g.lives
        g.ghost(GhostKind.CHASER).applyScheduleMode(GhostMode.CHASE)
        g.collide(GhostKind.CHASER)
        g.tick()
        assertEquals(livesBefore - 1, g.lives)
        assertEquals(GamePhase.DYING, g.phase)
    }

    @Test
    fun `a frightened ghost is eaten for two hundred`() {
        val g = game()
        val spot = g.maze.energizerPositions.first()
        g.croncher.placeAtTileCentre(spot, Direction.DOWN)
        g.tick()                                    // eats the energizer
        val scoreAfterEnergizer = g.score

        g.collide(GhostKind.CHASER)
        g.tick()

        assertEquals(scoreAfterEnergizer + 200, g.score)
        assertEquals(GhostMode.EATEN, g.ghost(GhostKind.CHASER).mode)
        assertNotEquals("must not cost a life", GamePhase.DYING, g.phase)
    }

    @Test
    fun `eating four in one energizer pays 200 400 800 1600`() {
        val g = game(Difficulties.EASY)          // nine seconds, plenty of room
        g.croncher.placeAtTileCentre(g.maze.energizerPositions.first(), Direction.DOWN)
        g.tick()

        // Park him in his dotless starting pocket facing the wall, so he neither
        // moves nor scores anything else while the ghosts are fed to him.
        val pocket = Maze.CRONCHER_START_TILE

        val values = listOf(200, 400, 800, 1600)
        for ((i, kind) in GhostKind.entries.withIndex()) {
            g.croncher.placeAtTileCentre(pocket, Direction.UP)
            // Three of the four start inside the house, where they are neither
            // edible nor dangerous — put them on the maze first.
            g.ghost(kind).debugPlaceOutside(pocket, GhostMode.FRIGHTENED)
            val before = g.score
            g.tick()
            assertEquals("ghost ${i + 1}", values[i], g.score - before)
        }
    }

    @Test
    fun `a pair of eyes is harmless to walk through`() {
        val g = game()
        val livesBefore = g.lives
        g.ghost(GhostKind.CHASER).getEaten()
        g.collide(GhostKind.CHASER)
        g.tick()
        assertEquals(livesBefore, g.lives)
        assertNotEquals(GamePhase.DYING, g.phase)
    }

    @Test
    fun `ghosts still in the house cannot catch anyone`() {
        val g = game()
        val livesBefore = g.lives
        val coward = g.ghost(GhostKind.COWARD)
        assertTrue(coward.mode.isInsideHouse)
        coward.placeAtTileCentre(g.croncher.tile(), Direction.LEFT)
        g.tick()
        assertEquals(livesBefore, g.lives)
    }

    @Test
    fun `running out of lives ends the game`() {
        val g = game(Difficulties.HARD)   // two lives, so this is quick
        var guard = 0
        while (g.phase != GamePhase.GAME_OVER && guard++ < 20) {
            if (g.phase == GamePhase.PLAYING) {
                g.ghost(GhostKind.CHASER).applyScheduleMode(GhostMode.CHASE)
                g.collide(GhostKind.CHASER)
            }
            repeat(GameState.DYING_TICKS + GameState.READY_TICKS + 2) { g.tick() }
        }
        assertEquals(GamePhase.GAME_OVER, g.phase)
        assertTrue(g.lives <= 0)
    }
}
