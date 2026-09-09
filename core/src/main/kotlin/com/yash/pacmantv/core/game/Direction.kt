package com.yash.pacmantv.core.game

/**
 * The four ways anything can move. Order matters: when a ghost has a tie for the
 * shortest route it must prefer up, then left, then down, then right — the
 * original arcade tie-break, and part of what makes the ghosts feel right.
 */
enum class Direction(val dx: Int, val dy: Int) {
    UP(0, -1),
    LEFT(-1, 0),
    DOWN(0, 1),
    RIGHT(1, 0),
    ;

    val opposite: Direction
        get() = when (this) {
            UP -> DOWN
            LEFT -> RIGHT
            DOWN -> UP
            RIGHT -> LEFT
        }
}
