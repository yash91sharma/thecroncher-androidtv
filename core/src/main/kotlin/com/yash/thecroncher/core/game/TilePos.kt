package com.yash.thecroncher.core.game

/** A position on the tile grid. */
data class TilePos(val x: Int, val y: Int) {

    fun offset(dir: Direction, distance: Int = 1) =
        TilePos(x + dir.dx * distance, y + dir.dy * distance)

    /** Squared distance — comparing these avoids a needless square root. */
    fun squaredDistanceTo(other: TilePos): Int {
        val dx = x - other.x
        val dy = y - other.y
        return dx * dx + dy * dy
    }
}
