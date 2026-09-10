package com.yash.thecroncher.core.ui

import com.yash.thecroncher.core.game.Maze
import com.yash.thecroncher.core.game.VIRTUAL_HEIGHT
import com.yash.thecroncher.core.game.VIRTUAL_WIDTH

/**
 * Every fixed screen position in one place, so moving a piece of the HUD is an
 * edit here rather than a hunt through the drawing code.
 *
 * The arcade screen is 36 tiles tall: three rows of score at the top, the 31-row
 * maze, then two rows for lives and toy.
 */
object Layout {

    const val SCREEN_WIDTH = VIRTUAL_WIDTH
    const val SCREEN_HEIGHT = VIRTUAL_HEIGHT

    const val TILE = Maze.TILE_SIZE

    /**
     * The maze is 46 tiles wide inside a 48-tile screen, drawn one tile in from
     * each edge. A television overscans, and a corridor flush against the panel
     * edge is a corridor you cannot see.
     */
    const val MAZE_ORIGIN_X = TILE

    /** Three tile-rows down, leaving room for the score. */
    const val MAZE_ORIGIN_Y = 3 * TILE

    const val MAZE_PIXEL_HEIGHT = 22 * TILE

    // --- top HUD ---
    const val SCORE_LABEL_Y = 1
    const val SCORE_VALUE_Y = 9
    const val SCORE_LEFT_X = TILE + 8
    const val HIGH_SCORE_CENTRE_X = SCREEN_WIDTH / 2

    // --- bottom HUD ---
    const val LIVES_Y = MAZE_ORIGIN_Y + MAZE_PIXEL_HEIGHT
    const val LIVES_LEFT_X = TILE + 4
    const val LIVES_SPACING = 16
    const val TOY_RIGHT_X = SCREEN_WIDTH - TILE - 4

    // --- centre messages ---
    const val MESSAGE_CENTRE_X = SCREEN_WIDTH / 2
    const val READY_Y = MAZE_ORIGIN_Y + 14 * TILE + 1
    const val GAME_OVER_Y = READY_Y

    /** Converts a maze pixel coordinate to a screen coordinate. */
    fun mazeToScreenY(mazePixelY: Int) = MAZE_ORIGIN_Y + mazePixelY

    fun mazeToScreenX(mazePixelX: Int) = MAZE_ORIGIN_X + mazePixelX
}
