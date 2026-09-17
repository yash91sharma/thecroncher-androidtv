package com.yash.thecroncher.core.game

/**
 * The ghosts' brains — deliberately tiny.
 *
 * They do not path-find. At every tile a ghost looks at its legal exits, and takes
 * whichever one leaves it closest (as the crow flies) to a target tile, never
 * turning back on itself. All four ghosts share that machinery; what makes them
 * feel like four different characters is only [chaseTarget].
 */
object GhostAi {

    /** Preference when two exits are equally good. Straight from the arcade. */
    val TIE_BREAK_ORDER = listOf(
        Direction.UP,
        Direction.LEFT,
        Direction.DOWN,
        Direction.RIGHT,
    )

    /**
     * Four tiles where a ghost may not turn upward. A quirk of the original maze
     * rather than a rule with a reason, but it shapes how the game is played.
     */
    val NO_UP_TURN_TILES = listOf(
        TilePos(12, 5),
        TilePos(33, 5),
        TilePos(12, 17),
        TilePos(33, 17),
    )

    /** The coward turns tail inside this radius. Compared squared, to avoid a sqrt. */
    private const val COWARD_PANIC_DISTANCE_SQUARED = 8 * 8

    fun scatterTarget(kind: GhostKind): TilePos = when (kind) {
        GhostKind.CHASER -> Maze.SCATTER_CHASER
        GhostKind.AMBUSHER -> Maze.SCATTER_AMBUSHER
        GhostKind.FLANKER -> Maze.SCATTER_FLANKER
        GhostKind.COWARD -> Maze.SCATTER_COWARD
    }

    /**
     * The tile this ghost is currently aiming at while chasing.
     *
     * Targets are deliberately allowed to fall outside the maze — that is what
     * makes the ghosts circle their corners instead of settling in them.
     */
    fun chaseTarget(
        kind: GhostKind,
        croncherTile: TilePos,
        croncherDirection: Direction,
        chaserTile: TilePos,
        ghostTile: TilePos,
    ): TilePos = when (kind) {

        // Straight at him.
        GhostKind.CHASER -> croncherTile

        // Four tiles in front, to cut him off.
        GhostKind.AMBUSHER -> ahead(croncherTile, croncherDirection, 4)

        // The vector from the chaser to two tiles ahead of the croncher, doubled — which is why
        // the flanker is only dangerous when the chaser is close.
        GhostKind.FLANKER -> {
            val pivot = ahead(croncherTile, croncherDirection, 2)
            TilePos(2 * pivot.x - chaserTile.x, 2 * pivot.y - chaserTile.y)
        }

        // Bold at a distance, shy up close.
        GhostKind.COWARD ->
            if (ghostTile.squaredDistanceTo(croncherTile) > COWARD_PANIC_DISTANCE_SQUARED) {
                croncherTile
            } else {
                Maze.SCATTER_COWARD
            }
    }

    /**
     * [distance] tiles in front of the croncher — reproducing the original overflow
     * bug, which also shifted the target left whenever he faced up.
     *
     * This is not an accident being preserved for nostalgia's sake: the ambusher's
     * and flanker's whole character depends on it, and "fixing" it makes them behave
     * like different ghosts entirely.
     */
    private fun ahead(from: TilePos, direction: Direction, distance: Int): TilePos =
        if (direction == Direction.UP) {
            TilePos(from.x - distance, from.y - distance)
        } else {
            TilePos(from.x + direction.dx * distance, from.y + direction.dy * distance)
        }

    /**
     * Picks the exit from [from] that gets closest to [target].
     *
     * Returns null when there is no legal move at all, which can happen inside the
     * ghost house.
     */
    fun chooseDirection(
        maze: Maze,
        from: TilePos,
        facing: Direction,
        target: TilePos,
        allowReverse: Boolean,
        ghostsMayEnterHouse: Boolean,
    ): Direction? {
        val banned = if (allowReverse) null else facing.opposite
        val noUpHere = from in NO_UP_TURN_TILES

        var best: Direction? = null
        var bestDistance = Int.MAX_VALUE

        for (dir in TIE_BREAK_ORDER) {
            if (dir == banned) continue
            if (dir == Direction.UP && noUpHere) continue

            val next = TilePos(maze.wrapX(from.x + dir.dx), from.y + dir.dy)
            if (next.y !in 0 until maze.height) continue

            val tile = maze.tileAt(next.x, next.y)
            val passable = when (tile) {
                Tile.WALL -> false
                Tile.DOOR, Tile.HOUSE -> ghostsMayEnterHouse
                else -> true
            }
            if (!passable) continue

            // Strictly less-than, so an earlier entry in TIE_BREAK_ORDER wins ties.
            val distance = next.squaredDistanceTo(target)
            if (distance < bestDistance) {
                bestDistance = distance
                best = dir
            }
        }
        return best
    }
}
