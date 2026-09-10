package com.yash.thecroncher.core.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The maze is loaded from a text resource rather than hardcoded, so a new maze is
 * a new file. These tests pin down the classic layout — most importantly that it
 * is actually playable, which a hand-edited text file can easily stop being.
 */
class MazeTest {

    private val maze = Maze.loadClassic()

    @Test
    fun `classic maze is 28 by 31 tiles`() {
        assertEquals(28, maze.width)
        assertEquals(31, maze.height)
    }

    @Test
    fun `there are exactly 240 dots and 4 energizers`() {
        assertEquals(240, maze.dotCount)
        assertEquals(4, maze.energizerCount)
        assertEquals(244, maze.totalPellets)
    }

    @Test
    fun `the four energizers sit in the traditional corners`() {
        assertEquals(
            setOf(TilePos(1, 3), TilePos(26, 3), TilePos(1, 23), TilePos(26, 23)),
            maze.energizerPositions.toSet(),
        )
    }

    @Test
    fun `the outer border is solid apart from the tunnel`() {
        for (x in 0 until maze.width) {
            assertTrue("top edge at $x", maze.isWall(x, 0))
            assertTrue("bottom edge at $x", maze.isWall(x, maze.height - 1))
        }
        for (y in 0 until maze.height) {
            if (y == Maze.TUNNEL_ROW) continue
            assertTrue("left edge at $y", maze.isWall(0, y))
            assertTrue("right edge at $y", maze.isWall(maze.width - 1, y))
        }
    }

    @Test
    fun `the tunnel row is open at both ends`() {
        assertFalse(maze.isWall(0, Maze.TUNNEL_ROW))
        assertFalse(maze.isWall(maze.width - 1, Maze.TUNNEL_ROW))
        assertTrue(maze.isWalkable(0, Maze.TUNNEL_ROW))
        assertTrue(maze.isWalkable(maze.width - 1, Maze.TUNNEL_ROW))
    }

    @Test
    fun `x wraps around through the tunnel`() {
        assertEquals(27, maze.wrapX(-1))
        assertEquals(0, maze.wrapX(28))
        assertEquals(26, maze.wrapX(-2))
        assertEquals(1, maze.wrapX(29))
        assertEquals(13, maze.wrapX(13))
    }

    @Test
    fun `the ghost house door is two tiles wide`() {
        assertEquals(Tile.DOOR, maze.tileAt(13, 12))
        assertEquals(Tile.DOOR, maze.tileAt(14, 12))
        assertEquals(setOf(TilePos(13, 12), TilePos(14, 12)), maze.doorPositions.toSet())
    }

    @Test
    fun `the ghost house interior is marked and only ghosts may enter`() {
        for (x in 11..16) {
            for (y in 13..15) {
                assertEquals("($x,$y)", Tile.HOUSE, maze.tileAt(x, y))
                assertFalse("croncher must not enter the house at ($x,$y)", maze.isWalkable(x, y))
                assertTrue("ghosts must be able to occupy ($x,$y)", maze.isGhostWalkable(x, y))
            }
        }
    }

    @Test
    fun `croncher cannot pass through the door but ghosts can`() {
        assertFalse(maze.isWalkable(13, 12))
        assertTrue(maze.isGhostWalkable(13, 12))
    }

    @Test
    fun `croncher's start tile is open and carries no dot`() {
        // A hand-edited maze can easily wall this in; if it does, the game is
        // unplayable from the first frame.
        val start = Maze.CRONCHER_START_TILE
        assertTrue("start tile is not walkable", maze.isWalkable(start.x, start.y))
        assertEquals(Tile.EMPTY, maze.tileAt(start.x, start.y))
    }

    @Test
    fun `every pellet is reachable from croncher's start`() {
        // The strongest structural guarantee: if a maze edit strands a pellet, the
        // level can never be completed and this fails immediately.
        val start = Maze.CRONCHER_START_TILE
        val seen = HashSet<TilePos>()
        val stack = ArrayDeque<TilePos>()
        stack.addLast(start)
        while (stack.isNotEmpty()) {
            val here = stack.removeLast()
            if (!seen.add(here)) continue
            for (dir in Direction.entries) {
                val next = TilePos(maze.wrapX(here.x + dir.dx), here.y + dir.dy)
                if (next.y !in 0 until maze.height) continue
                if (maze.isWalkable(next.x, next.y) && next !in seen) stack.addLast(next)
            }
        }
        val unreachable = maze.pelletPositions.filterNot { it in seen }
        assertTrue("unreachable pellets: $unreachable", unreachable.isEmpty())
    }

    @Test
    fun `out of bounds rows read as wall rather than throwing`() {
        assertTrue(maze.isWall(5, -1))
        assertTrue(maze.isWall(5, maze.height))
        assertEquals(Tile.WALL, maze.tileAt(5, -1))
    }

    @Test
    fun `parsing rejects a ragged maze`() {
        val ragged = "####\n##\n####"
        val error = runCatching { Maze.parse(ragged) }.exceptionOrNull()
        assertTrue("expected a helpful failure, got $error", error is IllegalArgumentException)
    }

    @Test
    fun `a freshly loaded maze is independent of any previous one`() {
        // Maze is the immutable layout; eaten pellets live in game state. Two loads
        // must therefore be identical.
        val other = Maze.loadClassic()
        assertEquals(maze.totalPellets, other.totalPellets)
        assertEquals(maze.pelletPositions, other.pelletPositions)
    }
}
