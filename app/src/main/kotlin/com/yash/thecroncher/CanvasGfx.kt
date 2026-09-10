package com.yash.thecroncher

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import com.yash.thecroncher.core.game.VIRTUAL_HEIGHT
import com.yash.thecroncher.core.game.VIRTUAL_WIDTH
import com.yash.thecroncher.core.game.Viewport
import com.yash.thecroncher.core.ports.Align
import com.yash.thecroncher.core.ports.Gfx
import com.yash.thecroncher.core.theme.Sprite
import com.yash.thecroncher.core.ui.Font

/**
 * Draws the game into a 224x288 integer pixel buffer, then blits that buffer to
 * the real surface at a whole-number scale with filtering off.
 *
 * Doing it this way — rather than drawing shapes straight onto a scaled Canvas —
 * is what guarantees every pixel on the 77" panel is a perfect square block with
 * no resampling blur.
 */
class CanvasGfx : Gfx {

    override val width = VIRTUAL_WIDTH
    override val height = VIRTUAL_HEIGHT

    private val buffer = IntArray(width * height)
    private val bitmap: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    private val source = Rect(0, 0, width, height)
    private val destination = Rect()

    private val blitPaint = Paint().apply {
        isFilterBitmap = false   // nearest-neighbour: the whole point
        isAntiAlias = false
        isDither = false
    }

    override fun clear(color: Int) {
        buffer.fill(color)
    }

    override fun fillRect(x: Int, y: Int, w: Int, h: Int, color: Int) {
        if (w <= 0 || h <= 0) return
        val x0 = x.coerceAtLeast(0)
        val y0 = y.coerceAtLeast(0)
        val x1 = (x + w).coerceAtMost(width)
        val y1 = (y + h).coerceAtMost(height)
        for (py in y0 until y1) {
            val row = py * width
            for (px in x0 until x1) buffer[row + px] = color
        }
    }

    override fun drawSprite(sprite: Sprite, x: Int, y: Int, tint: Int?) {
        for (sy in 0 until sprite.height) {
            val py = y + sy
            if (py < 0 || py >= height) continue
            val row = py * width
            val spriteRow = sy * sprite.width
            for (sx in 0 until sprite.width) {
                val px = x + sx
                if (px < 0 || px >= width) continue
                val pixel = sprite.pixels[spriteRow + sx]
                if ((pixel ushr 24) == 0) continue   // transparent
                buffer[row + px] = tint ?: pixel
            }
        }
    }

    override fun drawText(text: String, x: Int, y: Int, color: Int, align: Align, scale: Int) {
        Font.forEachPixel(text, x, y, align, scale) { px, py ->
            if (px in 0 until width && py in 0 until height) {
                buffer[py * width + px] = color
            }
        }
    }

    /** Pushes the pixel buffer onto the surface, scaled and letterboxed. */
    fun present(canvas: Canvas, viewport: Viewport, letterboxColor: Int) {
        bitmap.setPixels(buffer, 0, width, 0, 0, width, height)
        canvas.drawColor(letterboxColor)
        destination.set(
            viewport.x,
            viewport.y,
            viewport.x + viewport.width,
            viewport.y + viewport.height,
        )
        canvas.drawBitmap(bitmap, source, destination, blitPaint)
    }
}
