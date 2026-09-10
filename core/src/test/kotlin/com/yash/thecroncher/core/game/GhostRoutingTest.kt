package com.yash.thecroncher.core.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * How a ghost turns its target tile into an actual move. Ghosts do not path-find:
 * at each junction they simply take whichever legal exit leaves them closest to
 * the target, never reversing. That greedy rule, plus a fixed tie-break, is the
 * whole of it.
 */
class GhostRoutingTest {

    private val maze = Maze.loadDefault()

    private fun choose(
        from: TilePos,
        facing: Direction,
        target: TilePos,
        allowReverse: Boolean = false,
    ) = GhostAi.chooseDirection(maze, from, facing, target, allowReverse, ghostsMayEnterHouse = false)

    /** The long open corridor just inside the bottom wall. */
    private val BOTTOM_ROW = (maze.height - 2 downTo 0).first { y ->
        (1 until maze.width - 1).all { maze.isWalkable(it, y) }
    }

    @Test
    fun `it heads towards the target`() {
        // In the long corridor along the bottom, a target west means going west.
        val dir = choose(from = TilePos(10, BOTTOM_ROW), facing = Direction.LEFT, target = TilePos(1, BOTTOM_ROW))
        assertEquals(Direction.LEFT, dir)
    }

    @Test
    fun `it never reverses unless explicitly told it may`() {
        val dir = choose(from = TilePos(10, BOTTOM_ROW), facing = Direction.LEFT, target = TilePos(20, BOTTOM_ROW))
        assertNotEquals("reversing is forbidden", Direction.RIGHT, dir)
    }

    @Test
    fun `a forced reversal is obeyed when allowed`() {
        val dir = choose(
            from = TilePos(10, BOTTOM_ROW),
            facing = Direction.LEFT,
            target = TilePos(20, BOTTOM_ROW),
            allowReverse = true,
        )
        assertEquals(Direction.RIGHT, dir)
    }

    @Test
    fun `ties are broken up, then left, then down, then right`() {
        // At a junction where several exits are equidistant, the arcade's fixed
        // preference order decides. Anything else makes the ghosts feel random.
        val order = GhostAi.TIE_BREAK_ORDER
        assertEquals(
            listOf(Direction.UP, Direction.LEFT, Direction.DOWN, Direction.RIGHT),
            order,
        )
    }

    @Test
    fun `it only ever picks a direction it can actually walk`() {
        // Sweep the whole maze: every choice must land on an enterable tile.
        for (y in 0 until maze.height) {
            for (x in 0 until maze.width) {
                if (!maze.isWalkable(x, y)) continue
                val here = TilePos(x, y)
                for (facing in Direction.entries) {
                    val dir = GhostAi.chooseDirection(
                        maze, here, facing, TilePos(1, 1),
                        allowReverse = true, ghostsMayEnterHouse = false,
                    ) ?: continue
                    val next = TilePos(maze.wrapX(x + dir.dx), y + dir.dy)
                    assertTrue(
                        "at $here facing $facing it chose $dir into a wall",
                        maze.isGhostWalkable(next.x, next.y),
                    )
                }
            }
        }
    }

    @Test
    fun `ghosts may not turn upward on the four restricted tiles`() {
        // A quirk of the original maze: on these tiles a ghost is forbidden to turn
        // up, which is what creates the safe pockets experienced players rely on.
        for (tile in GhostAi.NO_UP_TURN_TILES) {
            val dir = GhostAi.chooseDirection(
                maze, tile, Direction.LEFT,
                target = TilePos(tile.x, 0),   // dangle the target directly above
                allowReverse = false,
                ghostsMayEnterHouse = false,
            )
            assertNotEquals("turned up on restricted tile $tile", Direction.UP, dir)
        }
    }

    @Test
    fun `there are four tiles where a ghost may not turn up`() {
        assertEquals(
            setOf(TilePos(12, 5), TilePos(33, 5), TilePos(12, 17), TilePos(33, 17)),
            GhostAi.NO_UP_TURN_TILES.toSet(),
        )
    }

    @Test
    fun `a dead end returns null rather than throwing`() {
        // Inside the house with the door closed to it, there may be no legal exit.
        val dir = GhostAi.chooseDirection(
            maze, Maze.HOUSE_CENTRE_TILE, Direction.UP, TilePos(1, 1),
            allowReverse = false, ghostsMayEnterHouse = false,
        )
        assertEquals(null, dir)
    }
}
