package com.yash.thecroncher.core.ports

import com.yash.thecroncher.core.theme.Sprite

enum class Align { LEFT, CENTER, RIGHT }

/**
 * Everything the game is allowed to draw with. Coordinates are in the 224x288
 * arcade framebuffer; scaling to the television is the renderer's business.
 *
 * `:app` implements this over an Android Canvas. Tests implement it as a recorder
 * and assert on the calls, which is what makes the menus and HUD unit-testable
 * without an emulator.
 */
interface Gfx {
    val width: Int
    val height: Int

    fun clear(color: Int)

    fun fillRect(x: Int, y: Int, w: Int, h: Int, color: Int)

    /**
     * Draws a sprite with its top-left at (x, y). A [tint] replaces the sprite's
     * own colour while keeping its shape, which is how one ghost body serves all
     * four ghosts.
     */
    fun drawSprite(sprite: Sprite, x: Int, y: Int, tint: Int? = null)

    /** Draws a sprite centred on (cx, cy) — how entities are positioned. */
    fun drawSpriteCentred(sprite: Sprite, cx: Int, cy: Int, tint: Int? = null) =
        drawSprite(sprite, cx - sprite.width / 2, cy - sprite.height / 2, tint)

    /**
     * Draws a line of text. [scale] blows the five-by-seven font up into whole
     * pixel blocks — the title screen and the big messages are the same glyphs,
     * only larger, so they stay crisp at any television size.
     */
    fun drawText(
        text: String,
        x: Int,
        y: Int,
        color: Int,
        align: Align = Align.LEFT,
        scale: Int = 1,
    )
}
