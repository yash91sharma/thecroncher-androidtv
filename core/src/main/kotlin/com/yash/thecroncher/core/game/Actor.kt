package com.yash.thecroncher.core.game

/**
 * Shared movement for anything that walks the maze on the sub-pixel grid.
 *
 * Positions are the actor's centre, in sub-pixel units. Turns are only permitted
 * when centred on a tile — except for reversals, which the arcade allows at any
 * time and which the ghosts rely on when a mode changes.
 */
abstract class Actor(protected val maze: Maze) {

    var x: Int = 0
    var y: Int = 0
    var direction: Direction = Direction.LEFT
    var speed: Int = FULL_SPEED

    /** Which tiles this actor may occupy; ghosts override to include the house. */
    protected open fun canEnter(x: Int, y: Int): Boolean = maze.isWalkable(x, y)

    fun tile(): TilePos = TilePos(
        maze.wrapX(Math.floorDiv(x, TILE_SUB)),
        Math.floorDiv(y, TILE_SUB),
    )

    open fun placeAtTileCentre(tile: TilePos, facing: Direction) {
        x = tileCentreSub(tile.x)
        y = tileCentreSub(tile.y)
        direction = facing
    }

    /** True when sitting exactly on the centre of a tile, where turns are legal. */
    fun isAtTileCentre(): Boolean =
        Math.floorMod(x, TILE_SUB) == HALF_TILE_SUB &&
            Math.floorMod(y, TILE_SUB) == HALF_TILE_SUB

    /** How far past the current tile's centre we are, along [dir]. */
    private fun overshootAlong(dir: Direction): Int {
        val here = tile()
        return when (dir) {
            Direction.LEFT -> tileCentreSub(here.x) - x
            Direction.RIGHT -> x - tileCentreSub(here.x)
            Direction.UP -> tileCentreSub(here.y) - y
            Direction.DOWN -> y - tileCentreSub(here.y)
        }
    }

    protected fun neighbourIsOpen(dir: Direction): Boolean {
        val here = tile()
        return canEnter(maze.wrapX(here.x + dir.dx), here.y + dir.dy)
    }

    /**
     * Advances by [speed] along [direction], stopping centred on the current tile
     * if the way ahead is blocked, and wrapping through the side tunnels.
     */
    protected fun step() {
        val blocked = !neighbourIsOpen(direction)
        var moved = speed

        if (blocked) {
            // Never travel past the centre of the tile we are stopping in.
            val room = -overshootAlong(direction)
            moved = moved.coerceAtMost(room.coerceAtLeast(0))
        }

        x += direction.dx * moved
        y += direction.dy * moved

        // Horizontal wrap-around: the side tunnels.
        val span = maze.width * TILE_SUB
        x = Math.floorMod(x, span)
    }

    /**
     * Applies a queued turn if it is legal now. Reversals are always legal;
     * anything else waits for a tile centre with an opening.
     *
     * Returns true if the direction changed.
     */
    protected fun tryTurn(desired: Direction): Boolean {
        if (desired == direction) return false

        if (desired == direction.opposite) {
            direction = desired
            return true
        }

        if (isAtTileCentre() && neighbourIsOpen(desired)) {
            direction = desired
            return true
        }
        return false
    }
}
