package com.yash.pacmantv.core.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Movement is on a fixed-point sub-pixel grid so it is exactly reproducible.
 * One pixel is [SUBPIXEL] units and one tile is eight pixels, so at full speed
 * (one pixel per tick) an actor crosses a tile in eight ticks.
 *
 * The behaviour that matters most here is the *buffered turn*: pressing a
 * direction slightly too early must still be honoured when the junction arrives.
 * Without it the controls feel broken, however correct everything else is.
 */
class PacmanMovementTest {

    private val maze = Maze.loadClassic()

    private fun pacman(
        tile: TilePos = Maze.PACMAN_START_TILE,
        facing: Direction = Direction.LEFT,
        speed: Int = FULL_SPEED,
    ) = Pacman(maze).apply {
        placeAtTileCentre(tile, facing)
        this.speed = speed
    }

    @Test
    fun `starts centred on its tile`() {
        val p = pacman()
        assertEquals(Maze.PACMAN_START_TILE, p.tile())
        assertTrue("should be exactly centred", p.isAtTileCentre())
    }

    @Test
    fun `crosses one tile in eight ticks at full speed`() {
        val p = pacman(facing = Direction.LEFT)
        repeat(8) { p.update() }
        assertEquals(TilePos(12, 23), p.tile())
        assertTrue(p.isAtTileCentre())
    }

    @Test
    fun `a wall stops it dead at the tile centre`() {
        // From (6,23) the tile to the left is solid, so it must come to rest
        // centred on (6,23) no matter how long it pushes.
        val p = pacman(tile = TilePos(6, 23), facing = Direction.LEFT)
        repeat(100) { p.update() }
        assertEquals(TilePos(6, 23), p.tile())
        assertTrue(p.isAtTileCentre())
    }

    @Test
    fun `will not turn into a wall`() {
        // Above the start pocket is solid, so the request must be held, not obeyed.
        val p = pacman(tile = TilePos(13, 23), facing = Direction.LEFT)
        p.requestDirection(Direction.UP)
        p.update()
        assertEquals(Direction.LEFT, p.direction)
    }

    @Test
    fun `never turns into a wall, however long it runs`() {
        // The invariant behind buffered turns: whenever the turn is taken, the way
        // must actually have been open. Holding UP for the whole run exercises
        // every junction along the corridor.
        val p = pacman(tile = TilePos(13, 23), facing = Direction.LEFT)
        p.requestDirection(Direction.UP)
        var turns = 0
        repeat(400) {
            val before = p.direction
            p.update()
            if (p.direction == Direction.UP && before != Direction.UP) {
                turns++
                val t = p.tile()
                assertTrue(
                    "turned up at $t but (${t.x},${t.y - 1}) is a wall",
                    maze.isWalkable(t.x, t.y - 1),
                )
            }
        }
        assertTrue("expected at least one legitimate turn", turns > 0)
    }

    @Test
    fun `a buffered turn is applied when the junction arrives`() {
        // Ask to go up while still a couple of tiles short of the opening at
        // (6,23). The request must survive until it becomes legal.
        val p = pacman(tile = TilePos(9, 23), facing = Direction.LEFT)
        p.requestDirection(Direction.UP)
        assertEquals(Direction.LEFT, p.direction)
        repeat(8 * 3) { p.update() }
        assertEquals("should have turned up at the junction", Direction.UP, p.direction)
        assertEquals(6, p.tile().x)
    }

    @Test
    fun `an early request is not forgotten across several tiles`() {
        val p = pacman(tile = TilePos(11, 23), facing = Direction.LEFT)
        p.requestDirection(Direction.UP)
        repeat(8 * 5) { p.update() }
        assertEquals(Direction.UP, p.direction)
    }

    @Test
    fun `reversing happens immediately, not at the next junction`() {
        val p = pacman(facing = Direction.LEFT)
        repeat(3) { p.update() }          // deliberately mid-tile
        assertTrue(!p.isAtTileCentre())
        p.requestDirection(Direction.RIGHT)
        p.update()
        assertEquals(Direction.RIGHT, p.direction)
    }

    @Test
    fun `the tunnel wraps from the left edge to the right`() {
        val p = pacman(tile = TilePos(0, Maze.TUNNEL_ROW), facing = Direction.LEFT)
        repeat(8) { p.update() }
        assertEquals(27, p.tile().x)
        assertEquals(Maze.TUNNEL_ROW, p.tile().y)
    }

    @Test
    fun `the tunnel wraps from the right edge to the left`() {
        val p = pacman(tile = TilePos(27, Maze.TUNNEL_ROW), facing = Direction.RIGHT)
        repeat(8) { p.update() }
        assertEquals(0, p.tile().x)
    }

    @Test
    fun `position never drifts off the sub-pixel grid`() {
        val p = pacman(facing = Direction.LEFT)
        repeat(500) { p.update() }
        assertTrue("x=${p.x}", p.x >= 0)
        // Travelling horizontally, he must stay exactly on the lane centre line.
        assertEquals(tileCentreSub(23), p.y)
    }

    @Test
    fun `a slower speed simply takes proportionally longer`() {
        val fast = pacman(facing = Direction.LEFT, speed = FULL_SPEED)
        val slow = pacman(facing = Direction.LEFT, speed = FULL_SPEED / 2)
        repeat(8) { fast.update() }
        repeat(16) { slow.update() }
        assertEquals(fast.tile(), slow.tile())
    }

    @Test
    fun `moving changes the tile it reports`() {
        val p = pacman(facing = Direction.LEFT)
        val before = p.tile()
        repeat(8) { p.update() }
        assertNotEquals(before, p.tile())
    }
}
