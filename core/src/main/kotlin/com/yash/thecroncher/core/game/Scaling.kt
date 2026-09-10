package com.yash.thecroncher.core.game

/** Width of the arcade framebuffer, in pixels: 28 tiles of 8px. */
const val VIRTUAL_WIDTH = 224

/** Height of the arcade framebuffer: 31 maze rows plus the HUD rows, 36 tiles of 8px. */
const val VIRTUAL_HEIGHT = 288

/** Where the arcade framebuffer lands on the real surface, and how big. */
data class Viewport(
    val scale: Int,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
)

/**
 * Maps the 224x288 arcade framebuffer onto whatever surface the television gives
 * us. The scale is deliberately a whole number: anything else resamples the pixel
 * art and turns a 77" screen into a blurry mess.
 */
object Scaling {

    /** Largest whole-number scale that still fits, never below 1. */
    fun computeScale(surfaceWidth: Int, surfaceHeight: Int): Int {
        val horizontal = surfaceWidth / VIRTUAL_WIDTH
        val vertical = surfaceHeight / VIRTUAL_HEIGHT
        return minOf(horizontal, vertical).coerceAtLeast(1)
    }

    /** The centred, letterboxed destination rectangle for a given surface. */
    fun viewport(surfaceWidth: Int, surfaceHeight: Int): Viewport {
        val scale = computeScale(surfaceWidth, surfaceHeight)
        val width = VIRTUAL_WIDTH * scale
        val height = VIRTUAL_HEIGHT * scale
        return Viewport(
            scale = scale,
            x = ((surfaceWidth - width) / 2).coerceAtLeast(0),
            y = ((surfaceHeight - height) / 2).coerceAtLeast(0),
            width = width,
            height = height,
        )
    }
}
