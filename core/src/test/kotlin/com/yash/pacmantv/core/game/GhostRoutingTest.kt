package com.yash.pacmantv.core.game

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

    private val maze = Maze.loadClassic()

    private fun choose(
        from: TilePos,
        facing: Direction,
        target: TilePos,
        allowReverse: Boolean = false,
    ) = GhostAi.chooseDirection(maze, from, facing, target, allowReverse, ghostsMayEnterHouse = false)

    @Test
    fun `it heads towards the target`() {
        // In the long corridor on row 23, a target to the west means going west.
        val dir = choose(from = TilePos(10, 23), facing = Direction.LEFT, target = TilePos(1, 23))
        assertEquals(Direction.LEFT, dir)
    }

    @Test
    fun `it never reverses unless explicitly told it may`() {
        val dir = choose(from = TilePos(10, 23), facing = Direction.LEFT, target = TilePos(20, 23))
        assertNotEquals("reversing is forbidden", Direction.RIGHT, dir)
    }

    @Test
    fun `a forced reversal is obeyed when allowed`() {
        val dir = choose(
            from = TilePos(10, 23),
            facing = Direction.LEFT,
            target = TilePos(20, 23),
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
    fun `the restricted tiles are the four the arcade uses`() {
        assertEquals(
            setOf(TilePos(12, 11), TilePos(15, 11), TilePos(12, 23), TilePos(15, 23)),
            GhostAi.NO_UP_TURN_TILES.toSet(),
        )
    }

    @Test
    fun `a dead end returns null rather than throwing`() {
        // Inside the house with the door closed to it, there may be no legal exit.
        val dir = GhostAi.chooseDirection(
            maze, TilePos(13, 14), Direction.UP, TilePos(1, 1),
            allowReverse = false, ghostsMayEnterHouse = false,
        )
        assertEquals(null, dir)
    }
}
