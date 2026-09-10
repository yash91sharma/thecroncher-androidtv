package com.yash.thecroncher.core.game

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
class CroncherMovementTest {

    private val maze = Maze.loadDefault()

    private fun croncher(
        tile: TilePos = Maze.CRONCHER_START_TILE,
        facing: Direction = Direction.LEFT,
        speed: Int = FULL_SPEED,
    ) = Croncher(maze).apply {
        placeAtTileCentre(tile, facing)
        this.speed = speed
    }

    /**
     * The speeds the game actually runs at are not whole pixels per tick: level
     * one is 80% of one pixel, which is 205 sub-pixel units against a tile of
     * 2048. An actor moving at that speed passes *over* a tile centre without
     * ever landing exactly on one, so anything that waits for an exact centre
     * waits roughly for ever — the turn only happens when a wall stops him dead
     * and snaps him back to the centre.
     */
    @Test
    fun `turns at a junction even at a level speed that never lands on a centre`() {
        val speed = speedOf(LevelTable.forLevel(1).croncherSpeed)
        assertNotEquals("this test is pointless at a speed that divides a tile", 0, TILE_SUB % speed)

        val junction = firstJunction(travel = Direction.LEFT, turn = Direction.UP)
        val start = TilePos(junction.x + 3, junction.y)
        val p = croncher(tile = start, facing = Direction.LEFT, speed = speed)

        p.requestDirection(Direction.UP)
        repeat(40) { p.update() }

        assertEquals("he never took the turn", Direction.UP, p.direction)
        assertTrue("he should be above the junction by now", p.tile().y < junction.y)
    }

    @Test
    fun `a queued turn is taken at the junction, not three tiles later`() {
        val speed = speedOf(LevelTable.forLevel(1).croncherSpeed)
        val junction = firstJunction(travel = Direction.LEFT, turn = Direction.UP)
        val p = croncher(tile = TilePos(junction.x + 3, junction.y), facing = Direction.LEFT, speed = speed)

        p.requestDirection(Direction.UP)
        var turnedAt: TilePos? = null
        repeat(40) {
            p.update()
            if (turnedAt == null && p.direction == Direction.UP) turnedAt = p.tile()
        }
        assertEquals("the turn was taken in the wrong tile", junction, turnedAt)
    }

    /**
     * A tile you can travel through and also turn out of, with a clear run-up
     * behind it: the run-up tiles must *not* offer the same turn, or the croncher
     * would rightly take it early and the test would be measuring the wrong tile.
     */
    private fun firstJunction(travel: Direction, turn: Direction, runUp: Int = 6): TilePos {
        for (y in 0 until maze.height) {
            for (x in 0 until maze.width) {
                if (!maze.isWalkable(x, y)) continue
                if (!maze.isWalkable(x + travel.dx, y + travel.dy)) continue
                if (!maze.isWalkable(x + turn.dx, y + turn.dy)) continue
                val approach = (1..runUp).map { TilePos(x - travel.dx * it, y - travel.dy * it) }
                if (approach.all { maze.isWalkable(it.x, it.y) } &&
                    approach.none { maze.isWalkable(it.x + turn.dx, it.y + turn.dy) }
                ) {
                    return TilePos(x, y)
                }
            }
        }
        throw AssertionError("no junction with a clear run-up found in the maze")
    }

    @Test
    fun `starts centred on its tile`() {
        val p = croncher()
        assertEquals(Maze.CRONCHER_START_TILE, p.tile())
        assertTrue("should be exactly centred", p.isAtTileCentre())
    }

    @Test
    fun `crosses one tile in eight ticks at full speed`() {
        val start = Maze.CRONCHER_START_TILE
        val p = croncher(facing = Direction.LEFT)
        repeat(8) { p.update() }
        assertEquals(TilePos(start.x - 1, start.y), p.tile())
        assertTrue(p.isAtTileCentre())
    }

    @Test
    fun `a wall stops it dead at the tile centre`() {
        // Run west along the croncher's own row until the wall at the end of it:
        // he must come to rest centred, no matter how long he pushes.
        val end = deadEndWest()
        val p = croncher(tile = end, facing = Direction.LEFT)
        repeat(100) { p.update() }
        assertEquals(end, p.tile())
        assertTrue(p.isAtTileCentre())
    }

    /** The westmost walkable tile on the croncher's row that has a wall beyond it. */
    private fun deadEndWest(): TilePos {
        val y = Maze.CRONCHER_START_TILE.y
        for (x in 1 until maze.width) {
            if (maze.isWalkable(x, y) && !maze.isWalkable(x - 1, y)) return TilePos(x, y)
        }
        throw AssertionError("the croncher's row has no wall on the left")
    }

    @Test
    fun `will not turn into a wall`() {
        // Above the start pocket is solid, so the request must be held, not obeyed.
        val p = croncher(tile = Maze.CRONCHER_START_TILE, facing = Direction.LEFT)
        p.requestDirection(Direction.UP)
        p.update()
        assertEquals(Direction.LEFT, p.direction)
    }

    @Test
    fun `never turns into a wall, however long it runs`() {
        // The invariant behind buffered turns: whenever the turn is taken, the way
        // must actually have been open. Holding UP for the whole run exercises
        // every junction along the corridor.
        val p = croncher(tile = Maze.CRONCHER_START_TILE, facing = Direction.LEFT)
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
        // Ask to go up while still short of the opening. The request must survive
        // until it becomes legal, and must be taken at the junction itself.
        val junction = firstJunction(travel = Direction.LEFT, turn = Direction.UP)
        val p = croncher(tile = TilePos(junction.x + 3, junction.y), facing = Direction.LEFT)
        p.requestDirection(Direction.UP)
        assertEquals(Direction.LEFT, p.direction)
        repeat(8 * 4) { p.update() }
        assertEquals("should have turned up at the junction", Direction.UP, p.direction)
        assertEquals(junction.x, p.tile().x)
    }

    @Test
    fun `an early request is not forgotten across several tiles`() {
        val junction = firstJunction(travel = Direction.LEFT, turn = Direction.UP)
        val p = croncher(tile = TilePos(junction.x + 5, junction.y), facing = Direction.LEFT)
        p.requestDirection(Direction.UP)
        repeat(8 * 6) { p.update() }
        assertEquals(Direction.UP, p.direction)
    }

    @Test
    fun `reversing happens immediately, not at the next junction`() {
        val p = croncher(facing = Direction.LEFT)
        repeat(3) { p.update() }          // deliberately mid-tile
        assertTrue(!p.isAtTileCentre())
        p.requestDirection(Direction.RIGHT)
        p.update()
        assertEquals(Direction.RIGHT, p.direction)
    }

    @Test
    fun `the tunnel wraps from the left edge to the right`() {
        val p = croncher(tile = TilePos(0, Maze.TUNNEL_ROW), facing = Direction.LEFT)
        repeat(8) { p.update() }
        assertEquals(maze.width - 1, p.tile().x)
        assertEquals(Maze.TUNNEL_ROW, p.tile().y)
    }

    @Test
    fun `the tunnel wraps from the right edge to the left`() {
        val p = croncher(tile = TilePos(maze.width - 1, Maze.TUNNEL_ROW), facing = Direction.RIGHT)
        repeat(8) { p.update() }
        assertEquals(0, p.tile().x)
    }

    @Test
    fun `position never drifts off the sub-pixel grid`() {
        val p = croncher(facing = Direction.LEFT)
        repeat(500) { p.update() }
        assertTrue("x=${p.x}", p.x >= 0)
        // Travelling horizontally, he must stay exactly on the lane centre line.
        assertEquals(tileCentreSub(Maze.CRONCHER_START_TILE.y), p.y)
    }

    @Test
    fun `a slower speed simply takes proportionally longer`() {
        val fast = croncher(facing = Direction.LEFT, speed = FULL_SPEED)
        val slow = croncher(facing = Direction.LEFT, speed = FULL_SPEED / 2)
        repeat(8) { fast.update() }
        repeat(16) { slow.update() }
        assertEquals(fast.tile(), slow.tile())
    }

    @Test
    fun `moving changes the tile it reports`() {
        val p = croncher(facing = Direction.LEFT)
        val before = p.tile()
        repeat(8) { p.update() }
        assertNotEquals(before, p.tile())
    }
}
