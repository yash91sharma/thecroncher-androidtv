package com.yash.thecroncher.core.game

/**
 * The croncher. The only interesting part is the buffered turn: a direction pressed
 * slightly too early is remembered and applied at the next junction where it
 * becomes legal, which is what makes the controls feel responsive rather than
 * unforgiving.
 */
class Croncher(maze: Maze) : Actor(maze) {

    /** The last direction the player asked for, honoured as soon as it is legal. */
    var desiredDirection: Direction = Direction.LEFT
        private set

    /** Ticks spent actually moving, used to drive the mouth animation. */
    var movingTicks: Long = 0
        private set

    fun reset(speed: Int = FULL_SPEED) {
        placeAtTileCentre(Maze.CRONCHER_START_TILE, Direction.LEFT)
        desiredDirection = Direction.LEFT
        this.speed = speed
        movingTicks = 0
    }

    /** Placing him also clears any queued turn, so he cannot instantly reverse. */
    override fun placeAtTileCentre(tile: TilePos, facing: Direction) {
        super.placeAtTileCentre(tile, facing)
        desiredDirection = facing
    }

    fun requestDirection(dir: Direction) {
        desiredDirection = dir
    }

    fun update() {
        // A reversal is legal anywhere, so it is worth trying before moving.
        tryTurn(desiredDirection)

        val before = x to y
        // Everything else waits for the centre of a tile, which step() now hands
        // us mid-tick — otherwise a queued turn would only land when a wall
        // stopped him and snapped him back to a centre.
        step { tryTurn(desiredDirection) }
        if (before != (x to y)) movingTicks++
    }

    /** True when hard against a wall with nowhere to go. */
    fun isStuck(): Boolean = !neighbourIsOpen(direction) && isAtTileCentre()

    override fun tryTurn(desired: Direction): Boolean {
        if (super.tryTurn(desired)) return true
        if (desired == direction || desired == direction.opposite) return false

        // Cornering tolerance: allow reactive post-turns within a window matching
        // the original arcade Pac-Man hardware's cornering mechanics.
        val here = tile()
        val overshoot = overshootAlong(direction)

        if (overshoot in 0..CORNERING_TOLERANCE && canEnter(maze.wrapX(here.x + desired.dx), here.y + desired.dy)) {
            alignToCorridor(here, desired)
            return true
        }

        val behind = TilePos(maze.wrapX(here.x - direction.dx), here.y - direction.dy)
        val distFromBehind = overshoot + TILE_SUB
        if (distFromBehind in 0..CORNERING_TOLERANCE && canEnter(maze.wrapX(behind.x + desired.dx), behind.y + desired.dy)) {
            alignToCorridor(behind, desired)
            return true
        }

        return false
    }

    private fun alignToCorridor(junction: TilePos, newDirection: Direction) {
        if (direction.dx != 0) {
            x = tileCentreSub(junction.x)
        } else {
            y = tileCentreSub(junction.y)
        }
        direction = newDirection
    }

    companion object {
        /**
         * Cornering tolerance: 4 pixels (1024 subpixels = half a tile), plus 1 pixel
         * grace for reactive turns just crossing the tile threshold. Matches the
         * 3-4 pixel pre/post-turn cornering window from the original Pac-Man arcade hardware.
         */
        const val CORNERING_TOLERANCE = HALF_TILE_SUB + SUBPIXEL
    }
}
