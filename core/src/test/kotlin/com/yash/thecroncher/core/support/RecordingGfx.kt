package com.yash.thecroncher.core.support

import com.yash.thecroncher.core.game.VIRTUAL_HEIGHT
import com.yash.thecroncher.core.game.VIRTUAL_WIDTH
import com.yash.thecroncher.core.ports.Align
import com.yash.thecroncher.core.ports.Gfx
import com.yash.thecroncher.core.theme.Sprite

/**
 * A [Gfx] that records instead of painting. This is what makes the HUD and the
 * menus unit-testable: a test can assert "three items were drawn and the second
 * one used the selected colour" with no emulator anywhere in sight.
 */
class RecordingGfx(
    override val width: Int = VIRTUAL_WIDTH,
    override val height: Int = VIRTUAL_HEIGHT,
) : Gfx {

    sealed interface Call {
        data class Clear(val color: Int) : Call
        data class Rect(val x: Int, val y: Int, val w: Int, val h: Int, val color: Int) : Call
        data class SpriteDraw(val sprite: Sprite, val x: Int, val y: Int, val tint: Int?) : Call
        data class Text(
            val text: String,
            val x: Int,
            val y: Int,
            val color: Int,
            val align: Align,
            val scale: Int,
        ) : Call
    }

    val calls = mutableListOf<Call>()

    val texts: List<Call.Text> get() = calls.filterIsInstance<Call.Text>()
    val sprites: List<Call.SpriteDraw> get() = calls.filterIsInstance<Call.SpriteDraw>()
    val rects: List<Call.Rect> get() = calls.filterIsInstance<Call.Rect>()

    fun textStrings(): List<String> = texts.map { it.text }

    fun reset() = calls.clear()

    override fun clear(color: Int) { calls += Call.Clear(color) }

    override fun fillRect(x: Int, y: Int, w: Int, h: Int, color: Int) {
        calls += Call.Rect(x, y, w, h, color)
    }

    override fun drawSprite(sprite: Sprite, x: Int, y: Int, tint: Int?) {
        calls += Call.SpriteDraw(sprite, x, y, tint)
    }

    override fun drawText(text: String, x: Int, y: Int, color: Int, align: Align, scale: Int) {
        calls += Call.Text(text, x, y, color, align, scale)
    }
}
