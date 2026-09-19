package com.yash.thecroncher.core.ui

import com.yash.thecroncher.core.game.Maze
import com.yash.thecroncher.core.game.VIRTUAL_HEIGHT
import com.yash.thecroncher.core.game.VIRTUAL_WIDTH

/**
 * Every fixed screen position in one place, so moving a piece of the HUD is an
 * edit here rather than a hunt through the drawing code.
 *
 * The screen is 384 x 216 pixels, exactly 16:9, which is an exact x5 on a 1080p
 * panel and x10 on a 4K one, so the game fills a television with no letterboxing.
 * Inside that: a [MARGIN] all round, the score, the 46 x 21 maze, and a row for
 * lives and toys.
 */
object Layout {

    const val SCREEN_WIDTH = VIRTUAL_WIDTH
    const val SCREEN_HEIGHT = VIRTUAL_HEIGHT

    const val TILE = Maze.TILE_SIZE

    /**
     * The safe area. Nothing is drawn inside this border on any screen — not the
     * maze, not the score, not the lives, not a menu. Two reasons: a television
     * overscans, so anything flush to the edge may simply not be on the panel;
     * and a game that runs into all four corners feels cramped even when it fits.
     */
    const val MARGIN = 6

    /** The maze is 46 tiles wide in a 48-tile screen, so it centres on a tile. */
    const val MAZE_ORIGIN_X = TILE
    const val MAZE_ORIGIN_Y = 24

    const val MAZE_PIXEL_HEIGHT = 21 * TILE

    // --- top HUD, inside the margin ---
    const val SCORE_LABEL_Y = MARGIN
    const val SCORE_VALUE_Y = MARGIN + 8
    const val SCORE_LEFT_X = MAZE_ORIGIN_X
    const val HIGH_SCORE_CENTRE_X = SCREEN_WIDTH / 2

    // --- bottom HUD, sitting between the maze and the margin ---
    const val LIVES_Y = MAZE_ORIGIN_Y + MAZE_PIXEL_HEIGHT + 2
    const val LIVES_LEFT_X = MAZE_ORIGIN_X
    const val LIVES_SPACING = 16
    const val TOY_RIGHT_X = SCREEN_WIDTH - MAZE_ORIGIN_X

    // --- title screen ---
    /** First line of the tagline; it sits just under the big wordmark. */
    const val TAGLINE_Y = 54
    /** One glyph row plus a three-pixel gap between tagline lines. */
    const val TAGLINE_LINE_SPACING = 10

    // --- centre messages ---
    const val MESSAGE_CENTRE_X = SCREEN_WIDTH / 2
    const val READY_Y = MAZE_ORIGIN_Y + 16 * TILE + 1
    const val GAME_OVER_Y = READY_Y

    /** Converts a maze pixel coordinate to a screen coordinate. */
    fun mazeToScreenY(mazePixelY: Int) = MAZE_ORIGIN_Y + mazePixelY

    fun mazeToScreenX(mazePixelX: Int) = MAZE_ORIGIN_X + mazePixelX
}
