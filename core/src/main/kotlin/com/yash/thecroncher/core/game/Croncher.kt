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
}
