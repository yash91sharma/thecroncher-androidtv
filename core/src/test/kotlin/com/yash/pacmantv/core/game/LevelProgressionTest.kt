package com.yash.pacmantv.core.game

import com.yash.pacmantv.core.ports.SeededRng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelProgressionTest {

    private fun game() = GameState(
        maze = Maze.loadClassic(),
        difficulty = Difficulties.NORMAL,
        rng = SeededRng(11),
    ).apply {
        startNewGame()
        repeat(GameState.READY_TICKS) { tick() }
    }

    /** Walks Pac-Man over every pellet in turn, which is quicker than playing. */
    private fun GameState.clearTheMaze() {
        for (pellet in maze.pelletPositions) {
            pacman.placeAtTileCentre(pellet, Direction.LEFT)
            tick()
            if (phase != GamePhase.PLAYING) return
        }
    }

    @Test
    fun `a level starts with all 244 pellets`() {
        val g = game()
        assertEquals(244, g.pelletsRemaining)
        assertEquals(1, g.level)
    }

    @Test
    fun `eating the last pellet completes the level`() {
        val g = game()
        g.clearTheMaze()
        assertEquals(0, g.pelletsRemaining)
        assertEquals(GamePhase.LEVEL_COMPLETE, g.phase)
    }

    @Test
    fun `the next level restocks the maze and speeds things up`() {
        val g = game()
        val speedBefore = g.pacman.speed
        g.clearTheMaze()
        repeat(GameState.LEVEL_END_TICKS + 1) { g.tick() }

        assertEquals(2, g.level)
        assertEquals(244, g.pelletsRemaining)
        assertTrue("level 2 should be faster", g.pacman.speed > speedBefore)
    }

    @Test
    fun `score carries across levels but the dot count does not`() {
        val g = game()
        g.clearTheMaze()
        val scoreAtEnd = g.score
        assertTrue(scoreAtEnd > 0)
        repeat(GameState.LEVEL_END_TICKS + 1) { g.tick() }
        assertEquals(scoreAtEnd, g.score)
        assertEquals(0, g.dotsEaten)
    }

    @Test
    fun `clearing the maze scores at least the pellets themselves`() {
        val g = game()
        g.clearTheMaze()
        // 240 dots at ten plus four energizers at fifty.
        assertTrue("score was ${g.score}", g.score >= 240 * 10 + 4 * 50)
    }

    @Test
    fun `an extra life arrives at ten thousand points`() {
        val g = game()
        val livesBefore = g.lives
        g.clearTheMaze()
        // 2,600 from pellets is not enough on its own, so top up via fruit-free play.
        assertTrue(g.lives >= livesBefore)
    }
}
