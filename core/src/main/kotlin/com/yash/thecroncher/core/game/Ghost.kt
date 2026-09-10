package com.yash.thecroncher.core.game

import com.yash.thecroncher.core.ports.Rng

/**
 * One ghost.
 *
 * The interesting states are the ones around the house: a ghost waiting inside
 * bobs on the spot, then walks a short scripted path out through the door, and an
 * eaten ghost retraces that path in reverse as a pair of eyes. Everywhere else it
 * simply follows [GhostAi] towards whatever tile it is currently aiming at.
 *
 * Deviation worth knowing about: the arcade parks ghosts on half-tile boundaries
 * in the house. Here everything stays on whole tile centres, which keeps the
 * movement grid uniform and testable at the cost of a pixel or two of fidelity in
 * the doorway.
 */
class Ghost(
    maze: Maze,
    val kind: GhostKind,
) : Actor(maze) {

    var mode: GhostMode = GhostMode.IN_HOUSE

    /** Set when the schedule changes; consumed at the next tile centre. */
    private var reversePending = false

    /** Where in the house this ghost waits. */
    private val homeTile: TilePos = when (kind) {
        GhostKind.BLINKY -> Maze.BLINKY_START_TILE
        GhostKind.PINKY -> TilePos(13, 14)
        GhostKind.INKY -> TilePos(12, 14)
        GhostKind.CLYDE -> TilePos(15, 14)
    }

    /** Ghosts may use the door and the house; the croncher may not. */
    override fun canEnter(x: Int, y: Int): Boolean = maze.isGhostWalkable(x, y)

    fun reset() {
        placeAtTileCentre(homeTile, if (kind == GhostKind.BLINKY) Direction.LEFT else Direction.UP)
        mode = if (kind == GhostKind.BLINKY) GhostMode.SCATTER else GhostMode.IN_HOUSE
        reversePending = false
    }

    /** Asks the ghost to turn around at the next opportunity. */
    fun requestReverse() {
        if (mode.isInsideHouse || mode == GhostMode.EATEN) return
        reversePending = true
    }

    fun release() {
        if (mode == GhostMode.IN_HOUSE) mode = GhostMode.LEAVING_HOUSE
    }

    fun frighten() {
        if (mode == GhostMode.EATEN || mode.isInsideHouse) return
        mode = GhostMode.FRIGHTENED
        reversePending = true
    }

    fun getEaten() {
        mode = GhostMode.EATEN
    }

    /** Restores a normal hunting mode when fright expires. */
    fun unfrighten(scheduleMode: GhostMode) {
        if (mode == GhostMode.FRIGHTENED) mode = scheduleMode
    }

    fun applyScheduleMode(scheduleMode: GhostMode) {
        if (mode == GhostMode.SCATTER || mode == GhostMode.CHASE) mode = scheduleMode
    }

    /**
     * Test hook: drop this ghost straight onto the maze, skipping the walk out of
     * the house. Lets a test set up a specific encounter without playing towards
     * it for a thousand ticks.
     */
    fun debugPlaceOutside(tile: TilePos, newMode: GhostMode = GhostMode.CHASE) {
        placeAtTileCentre(tile, Direction.LEFT)
        mode = newMode
        reversePending = false
    }

    fun update(ctx: GhostContext) {
        when (mode) {
            GhostMode.IN_HOUSE -> bobInHouse()
            GhostMode.LEAVING_HOUSE -> walkOutOfHouse(ctx)
            GhostMode.EATEN -> returnHome(ctx)
            else -> roam(ctx)
        }
    }

    // -------------------------------------------------------------- states --

    /** Waiting ghosts drift up and down a little so they do not look frozen. */
    private fun bobInHouse() {
        val centre = tileCentreSub(homeTile.y)
        val limit = TILE_SUB / 3
        if (direction != Direction.UP && direction != Direction.DOWN) direction = Direction.UP
        y += direction.dy * (speed / 2).coerceAtLeast(1)
        if (y <= centre - limit) { y = centre - limit; direction = Direction.DOWN }
        if (y >= centre + limit) { y = centre + limit; direction = Direction.UP }
    }

    /** Slide to the door column, then straight up and out. */
    private fun walkOutOfHouse(ctx: GhostContext) {
        val exit = Maze.BLINKY_START_TILE
        val targetX = tileCentreSub(exit.x)

        if (x != targetX) {
            direction = if (x < targetX) Direction.RIGHT else Direction.LEFT
            x = moveToward(x, targetX, speed)
            return
        }

        direction = Direction.UP
        y = moveToward(y, tileCentreSub(exit.y), speed)
        if (y == tileCentreSub(exit.y)) {
            mode = ctx.scheduleMode
            direction = Direction.LEFT
        }
    }

    /** Eyes hurrying home: back to the door, then down into the house. */
    private fun returnHome(ctx: GhostContext) {
        val door = Maze.BLINKY_START_TILE
        val atDoorColumn = x == tileCentreSub(door.x)
        val aboveHouse = y <= tileCentreSub(door.y)

        if (!(atDoorColumn && aboveHouse)) {
            steerToward(TilePos(door.x, door.y), allowHouse = true)
            step()
            return
        }

        // Lined up over the door: drop straight in.
        direction = Direction.DOWN
        x = tileCentreSub(door.x)
        y = moveToward(y, tileCentreSub(homeTile.y), speed)
        if (y == tileCentreSub(homeTile.y)) {
            x = tileCentreSub(homeTile.x)
            mode = GhostMode.LEAVING_HOUSE
        }
    }

    /** Ordinary hunting, scattering or fleeing. */
    private fun roam(ctx: GhostContext) {
        if (isAtTileCentre()) {
            if (mode == GhostMode.FRIGHTENED) {
                steerRandomly(ctx.rng)
            } else {
                steerToward(currentTarget(ctx), allowHouse = false)
            }
            reversePending = false
        }
        step()
    }

    // ------------------------------------------------------------ steering --

    private fun currentTarget(ctx: GhostContext): TilePos = when (mode) {
        GhostMode.SCATTER -> GhostAi.scatterTarget(kind)
        else -> GhostAi.chaseTarget(
            kind = kind,
            croncherTile = ctx.croncherTile,
            croncherDirection = ctx.croncherDirection,
            blinkyTile = ctx.blinkyTile,
            ghostTile = tile(),
        )
    }

    private fun steerToward(target: TilePos, allowHouse: Boolean) {
        if (!isAtTileCentre()) return
        val chosen = GhostAi.chooseDirection(
            maze = maze,
            from = tile(),
            facing = direction,
            target = target,
            allowReverse = reversePending,
            ghostsMayEnterHouse = allowHouse,
        )
        if (chosen != null) direction = chosen
    }

    /** Frightened ghosts wander: a random legal exit, still never doubling back. */
    private fun steerRandomly(rng: Rng) {
        val options = GhostAi.TIE_BREAK_ORDER.filter { dir ->
            if (!reversePending && dir == direction.opposite) return@filter false
            if (dir == Direction.UP && tile() in GhostAi.NO_UP_TURN_TILES) return@filter false
            val next = TilePos(maze.wrapX(tile().x + dir.dx), tile().y + dir.dy)
            next.y in 0 until maze.height && maze.isWalkable(next.x, next.y)
        }
        if (options.isNotEmpty()) direction = options[rng.nextInt(options.size)]
    }

    private fun moveToward(value: Int, target: Int, by: Int): Int {
        val step = by.coerceAtLeast(1)
        return when {
            value < target -> (value + step).coerceAtMost(target)
            value > target -> (value - step).coerceAtLeast(target)
            else -> value
        }
    }

    /** Everything a ghost needs to know about the wider game to make its move. */
    data class GhostContext(
        val croncherTile: TilePos,
        val croncherDirection: Direction,
        val blinkyTile: TilePos,
        val scheduleMode: GhostMode,
        val rng: Rng,
    )
}
