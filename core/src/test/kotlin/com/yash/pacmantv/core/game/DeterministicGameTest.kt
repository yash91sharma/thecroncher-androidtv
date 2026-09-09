package com.yash.pacmantv.core.game

import com.yash.pacmantv.core.ports.SeededRng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The golden regression test.
 *
 * A seeded game is played out under a fixed script of inputs and every tick is
 * folded into a rolling checksum, so the fingerprint covers the whole trajectory
 * rather than just wherever the game happened to end up. It does not argue that
 * the behaviour is *correct* — the other tests do that — it argues that the
 * behaviour has not silently *changed*. Drift anywhere in movement, ghost
 * targeting, mode scheduling or scoring surfaces here as one loud failure.
 *
 * If it fails after a deliberate change: check the other tests are green, then
 * re-record the constants below and say so in the commit message.
 */
class DeterministicGameTest {

    private val script = listOf(
        Direction.LEFT, Direction.UP, Direction.RIGHT, Direction.DOWN,
        Direction.LEFT, Direction.LEFT, Direction.UP, Direction.RIGHT,
        Direction.DOWN, Direction.DOWN, Direction.RIGHT, Direction.UP,
    )

    private fun newGame(seed: Long) = GameState(
        maze = Maze.loadClassic(),
        difficulty = Difficulties.NORMAL,
        rng = SeededRng(seed),
    )

    /**
     * Plays [ticks] frames and returns a checksum of every state along the way.
     * The game restarts on game over so the run keeps producing signal.
     */
    private fun trajectoryHash(ticks: Int, seed: Long = 20260908L): Long {
        val game = newGame(seed)
        game.startNewGame()

        var hash = -3750763034362895579L        // FNV-1a 64-bit offset basis
        fun fold(value: Long) {
            hash = (hash xor value) * 1099511628211L
        }

        for (tick in 0 until ticks) {
            if (tick % 37 == 0) game.requestDirection(script[(tick / 37) % script.size])
            game.tick()
            if (game.phase == GamePhase.GAME_OVER) game.startNewGame()

            fold(game.score.toLong())
            fold(game.dotsEaten.toLong())
            fold(game.lives.toLong())
            fold(game.pacman.x.toLong() * 31 + game.pacman.y)
            fold(game.pacman.direction.ordinal.toLong())
            for (ghost in game.ghosts) {
                fold(ghost.x.toLong() * 31 + ghost.y)
                fold(ghost.direction.ordinal * 7L + ghost.mode.ordinal)
            }
        }
        return hash
    }

    @Test
    fun `the trajectory matches the recorded fingerprint`() {
        assertEquals(GOLDEN_10K, trajectoryHash(10_000))
        assertEquals(GOLDEN_30K, trajectoryHash(30_000))
    }

    @Test
    fun `the same seed and inputs always produce the same game`() {
        assertEquals(trajectoryHash(5_000), trajectoryHash(5_000))
    }

    @Test
    fun `frightened ghosts wander differently under a different seed`() {
        // The RNG is only consulted while ghosts are frightened, so this is the
        // one situation where the seed can actually change anything.
        fun wander(seed: Long): String {
            val g = newGame(seed)
            g.startNewGame()
            repeat(GameState.READY_TICKS) { g.tick() }

            // Everyone out on the maze and scared; Pac-Man parked out of the way in
            // his dotless pocket, where a frightened ghost cannot hurt him.
            for (kind in GhostKind.entries) {
                g.ghost(kind).debugPlaceOutside(TilePos(13, 11), GhostMode.FRIGHTENED)
            }
            repeat(200) {
                g.pacman.placeAtTileCentre(Maze.PACMAN_START_TILE, Direction.UP)
                g.tick()
            }
            return g.ghosts.joinToString { "${it.x},${it.y}" }
        }
        assertNotEquals(wander(1L), wander(2L))
    }

    @Test
    fun `a long run stays in a sane state`() {
        val game = newGame(99)
        game.startNewGame()
        repeat(60_000) { tick ->
            if (tick % 37 == 0) game.requestDirection(script[(tick / 37) % script.size])
            game.tick()
            if (game.phase == GamePhase.GAME_OVER) game.startNewGame()

            assertTrue("score went negative", game.score >= 0)
            assertTrue("dots exceeded the maze", game.dotsEaten <= game.maze.totalPellets)
            assertTrue("lives went below zero", game.lives >= 0)
        }
    }

    @Test
    fun `pacman never ends up inside a wall`() {
        val game = GameState(Maze.loadClassic(), Difficulties.HARD, SeededRng(5))
        game.startNewGame()
        repeat(20_000) { tick ->
            if (tick % 23 == 0) game.requestDirection(script[(tick / 23) % script.size])
            game.tick()
            if (game.phase == GamePhase.GAME_OVER) game.startNewGame()
            val t = game.pacman.tile()
            assertTrue("pacman is inside a wall at $t", game.maze.isWalkable(t.x, t.y))
        }
    }

    @Test
    fun `ghosts never end up inside a wall`() {
        val game = newGame(17)
        game.startNewGame()
        repeat(20_000) { tick ->
            if (tick % 29 == 0) game.requestDirection(script[(tick / 29) % script.size])
            game.tick()
            if (game.phase == GamePhase.GAME_OVER) game.startNewGame()
            for (ghost in game.ghosts) {
                val t = ghost.tile()
                assertTrue(
                    "${ghost.kind} is inside a wall at $t while ${ghost.mode}",
                    game.maze.isGhostWalkable(t.x, t.y),
                )
            }
        }
    }

    @Test
    fun `the scripted run actually plays a real game`() {
        // Guards the golden test itself: if the script stopped producing an
        // interesting game, the fingerprint would keep passing while covering
        // nothing. Assert the run really eats, dies and restarts.
        val game = newGame(20260908L)
        game.startNewGame()
        var maxDots = 0
        var deaths = 0
        var wasDying = false
        repeat(10_000) { tick ->
            if (tick % 37 == 0) game.requestDirection(script[(tick / 37) % script.size])
            game.tick()
            if (game.phase == GamePhase.GAME_OVER) game.startNewGame()
            maxDots = maxOf(maxDots, game.dotsEaten)
            val dying = game.phase == GamePhase.DYING
            if (dying && !wasDying) deaths++
            wasDying = dying
        }
        assertTrue("the script barely ate anything ($maxDots dots)", maxDots > 20)
        assertTrue("the script never got caught, so collisions are untested", deaths > 0)
    }

    private companion object {
        // Recorded 2026-09-08 from the implementation at that commit.
        const val GOLDEN_10K = 8042856243782083524L
        const val GOLDEN_30K = -1677214051332628817L
    }
}
