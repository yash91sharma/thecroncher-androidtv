package com.yash.thecroncher.core.theme

/**
 * A block of ARGB pixels. Deliberately plain data with no Android types, so the
 * whole art pipeline is testable on the JVM and a renderer can upload it however
 * it likes.
 */
class Sprite(
    val width: Int,
    val height: Int,
    val pixels: IntArray,
) {
    init {
        require(pixels.size == width * height) {
            "sprite is ${width}x$height but has ${pixels.size} pixels"
        }
    }

    fun pixelAt(x: Int, y: Int): Int =
        if (x in 0 until width && y in 0 until height) pixels[y * width + x] else TRANSPARENT

    companion object {
        const val TRANSPARENT = 0
    }
}
