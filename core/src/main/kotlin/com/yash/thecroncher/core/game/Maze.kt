package com.yash.thecroncher.core.game

enum class Tile {
    WALL,
    EMPTY,
    DOT,
    ENERGIZER,

    /** The gate to the ghost house: ghosts pass, the croncher does not. */
    DOOR,

    /** Inside the ghost house. */
    HOUSE,
    ;

    val hasPellet: Boolean get() = this == DOT || this == ENERGIZER
}

/**
 * The immutable layout of a level, parsed from a text resource so that adding a
 * maze means adding a file rather than editing code.
 *
 * Which pellets have been eaten is *not* stored here — that is per-game state.
 * A [Maze] can therefore be shared and reloaded freely.
 */
class Maze private constructor(
    val width: Int,
    val height: Int,
    private val tiles: Array<Tile>,
) {

    val pelletPositions: List<TilePos> = buildList {
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (tiles[y * width + x].hasPellet) add(TilePos(x, y))
            }
        }
    }

    val energizerPositions: List<TilePos> =
        pelletPositions.filter { tileAt(it.x, it.y) == Tile.ENERGIZER }

    val doorPositions: List<TilePos> = buildList {
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (tiles[y * width + x] == Tile.DOOR) add(TilePos(x, y))
            }
        }
    }

    val energizerCount: Int = energizerPositions.size
    val dotCount: Int = pelletPositions.size - energizerCount
    val totalPellets: Int = pelletPositions.size

    /** Anything off the top or bottom of the maze reads as solid wall. */
    fun tileAt(x: Int, y: Int): Tile {
        if (y !in 0 until height) return Tile.WALL
        return tiles[y * width + wrapX(x)]
    }

    fun tileAt(pos: TilePos): Tile = tileAt(pos.x, pos.y)

    fun isWall(x: Int, y: Int): Boolean = tileAt(x, y) == Tile.WALL

    /** Where the croncher may go: everywhere except walls, the door and the house. */
    fun isWalkable(x: Int, y: Int): Boolean = when (tileAt(x, y)) {
        Tile.WALL, Tile.DOOR, Tile.HOUSE -> false
        else -> true
    }

    /** Where ghosts may go: as above, plus the door and their house. */
    fun isGhostWalkable(x: Int, y: Int): Boolean = tileAt(x, y) != Tile.WALL

    /** Horizontal wrap-around, which is what makes the side tunnels work. */
    fun wrapX(x: Int): Int = ((x % width) + width) % width

    /** True on the two tunnel stretches, where ghosts are slowed down. */
    fun isTunnel(x: Int, y: Int): Boolean =
        y == TUNNEL_ROW && (x < TUNNEL_LEFT_END || x > TUNNEL_RIGHT_START)

    companion object {
        const val TILE_SIZE = 8

        /** The one row that runs off both sides of the screen. */
        const val TUNNEL_ROW = 11

        /** Tunnel stretches: x below this on the left, above the other on the right. */
        const val TUNNEL_LEFT_END = 6
        const val TUNNEL_RIGHT_START = 39

        /** The croncher's dotless starting pocket, below the ghost house. */
        val CRONCHER_START_TILE = TilePos(22, 14)

        /** Where the dog waits, on the lane directly above the house door. */
        val HOUSE_EXIT_TILE = TilePos(22, 8)

        /** The middle of the ghost house, where the other three begin. */
        val HOUSE_CENTRE_TILE = TilePos(22, 11)

        /**
         * The corners each ghost retreats to during scatter. They sit outside the
         * maze, which is precisely why the ghosts circle rather than settle.
         */
        val SCATTER_CHASER = TilePos(43, 0)
        val SCATTER_AMBUSHER = TilePos(2, 0)
        val SCATTER_FLANKER = TilePos(45, 21)
        val SCATTER_COWARD = TilePos(0, 21)

        private const val MAZE_RESOURCE = "/maze/croncher.txt"

        fun loadDefault(): Maze = load(MAZE_RESOURCE)

        fun load(resource: String): Maze {
            val text = Maze::class.java.getResourceAsStream(resource)
                ?.bufferedReader()
                ?.use { it.readText() }
                ?: throw IllegalArgumentException("maze resource not found: $resource")
            return parse(text)
        }

        fun parse(text: String): Maze {
            val rows = text.trim('\n').lines().filter { it.isNotEmpty() }
            require(rows.isNotEmpty()) { "maze is empty" }

            val width = rows[0].length
            rows.forEachIndexed { y, row ->
                require(row.length == width) {
                    "maze is ragged: row $y is ${row.length} tiles, expected $width"
                }
            }

            val tiles = Array(width * rows.size) { Tile.EMPTY }
            rows.forEachIndexed { y, row ->
                row.forEachIndexed { x, c ->
                    tiles[y * width + x] = when (c) {
                        '#' -> Tile.WALL
                        '.' -> Tile.DOT
                        'o' -> Tile.ENERGIZER
                        '-' -> Tile.DOOR
                        '_' -> Tile.HOUSE
                        ' ' -> Tile.EMPTY
                        else -> throw IllegalArgumentException(
                            "unknown maze character '$c' at ($x,$y)"
                        )
                    }
                }
            }
            return Maze(width, rows.size, tiles)
        }
    }
}
