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
    protected fun overshootAlong(dir: Direction): Int {
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
     *
     * If the move would carry the actor *over* the centre of its tile, it stops
     * there first, calls [atCentre], and spends whatever is left of the tick in
     * whatever direction that left it facing. This is what makes junctions work:
     * the speeds the game actually runs at are fractions of a pixel per tick — 205
     * sub-pixel units against a tile of 2048 at level one — so an actor sails over
     * a centre without ever landing on one. Waiting for an exact centre meant a
     * turn only happened when a wall stopped him dead.
     *
     * A tick never covers more than one tile, so at most one centre can fall
     * inside it, and no movement is lost: the two halves add up to [speed].
     */
    protected fun step(atCentre: () -> Unit = {}) {
        var budget = speed

        val toCentre = -overshootAlong(direction)
        if (toCentre in 0..budget) {
            advance(toCentre)
            budget -= toCentre
            atCentre()
        }

        val room = if (neighbourIsOpen(direction)) budget else (-overshootAlong(direction)).coerceAtLeast(0)
        advance(budget.coerceAtMost(room))

        // Horizontal wrap-around: the side tunnels.
        val span = maze.width * TILE_SUB
        x = Math.floorMod(x, span)
    }

    private fun advance(distance: Int) {
        x += direction.dx * distance
        y += direction.dy * distance
    }

    /**
     * Applies a queued turn if it is legal now. Reversals are always legal;
     * anything else waits for a tile centre with an opening.
     *
     * Returns true if the direction changed.
     */
    protected open fun tryTurn(desired: Direction): Boolean {
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
