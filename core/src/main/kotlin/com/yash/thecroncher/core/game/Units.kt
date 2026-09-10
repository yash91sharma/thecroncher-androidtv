package com.yash.thecroncher.core.game

/**
 * Fixed-point units per pixel. Positions are integers in these units, so movement
 * is exactly reproducible — no floating-point drift, which is what lets the golden
 * regression test assert an exact game state after thousands of ticks.
 */
const val SUBPIXEL = 256

/** Sub-pixel units across one 8-pixel tile. */
const val TILE_SUB = Maze.TILE_SIZE * SUBPIXEL

/** Half a tile: the offset of a tile's centre from its top-left corner. */
const val HALF_TILE_SUB = TILE_SUB / 2

/**
 * One pixel per tick, the arcade's notional 100%. Everything else is a percentage
 * of this, so a "speed" is directly comparable to the original tables.
 */
const val FULL_SPEED = SUBPIXEL

/** Turns an arcade speed percentage into sub-pixels per tick. */
fun speedOf(percent: Double): Int = Math.round(FULL_SPEED * percent).toInt()

/** Centre of a tile, in sub-pixel units. */
fun tileCentreSub(tile: Int): Int = tile * TILE_SUB + HALF_TILE_SUB
