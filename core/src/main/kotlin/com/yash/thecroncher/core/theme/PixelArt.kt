package com.yash.thecroncher.core.theme

/**
 * Turns hand-drawn ASCII grids into [Sprite]s.
 *
 * Every character in a grid names an *ink*, and the ink's actual colour comes from
 * the theme's [SpritePalette]. So the art says "fur here, nose there" and the
 * theme decides what fur looks like — restyling the whole cast is a palette edit,
 * never a redraw.
 *
 * The transforms below exist so a character needs drawing only once: facing the
 * other way is [mirrored], bobbing along is [shifted], and being frightened is a
 * [silhouette] with a face [overlaid] on it.
 */
object PixelArt {

    private const val TRANSPARENT_INK = '.'
    private const val TRANSPARENT_INK_ALT = ' '

    /**
     * Reads a grid. Rows must all be the same length and every character must have
     * an ink — a mistyped grid is a compile-time-ish failure in the sprite tests
     * rather than a hole in a character on the television.
     */
    fun sprite(rows: List<String>, ink: Map<Char, Int>): Sprite {
        require(rows.isNotEmpty()) { "a sprite needs at least one row" }
        val width = rows[0].length
        require(width > 0) { "a sprite needs at least one column" }
        rows.forEachIndexed { y, row ->
            require(row.length == width) {
                "row $y is ${row.length} wide but the sprite is $width wide: \"$row\""
            }
        }

        val pixels = IntArray(width * rows.size)
        for ((y, row) in rows.withIndex()) {
            for ((x, ch) in row.withIndex()) {
                if (ch == TRANSPARENT_INK || ch == TRANSPARENT_INK_ALT) continue
                val colour = ink[ch]
                require(colour != null) { "no ink for '$ch' at $x,$y" }
                pixels[y * width + x] = colour
            }
        }
        return Sprite(width, rows.size, pixels)
    }

    /** The same character facing the other way. */
    fun mirrored(sprite: Sprite): Sprite {
        val pixels = IntArray(sprite.width * sprite.height)
        for (y in 0 until sprite.height) {
            for (x in 0 until sprite.width) {
                pixels[y * sprite.width + x] = sprite.pixelAt(sprite.width - 1 - x, y)
            }
        }
        return Sprite(sprite.width, sprite.height, pixels)
    }

    /** Moves the art within its own box — a one-pixel bob is a whole animation. */
    fun shifted(sprite: Sprite, dx: Int, dy: Int): Sprite {
        val pixels = IntArray(sprite.width * sprite.height)
        for (y in 0 until sprite.height) {
            for (x in 0 until sprite.width) {
                pixels[y * sprite.width + x] = sprite.pixelAt(x - dx, y - dy)
            }
        }
        return Sprite(sprite.width, sprite.height, pixels)
    }

    /** The shape only, in one flat colour. How a foe turns harmless-blue. */
    fun silhouette(sprite: Sprite, colour: Int): Sprite {
        val pixels = IntArray(sprite.pixels.size)
        for (i in pixels.indices) {
            pixels[i] = if ((sprite.pixels[i] ushr 24) != 0) colour else Sprite.TRANSPARENT
        }
        return Sprite(sprite.width, sprite.height, pixels)
    }

    /** Draws a grid over an existing sprite, leaving the base showing through gaps. */
    fun overlaid(base: Sprite, rows: List<String>, ink: Map<Char, Int>): Sprite =
        overlaid(base, sprite(rows, ink))

    fun overlaid(base: Sprite, top: Sprite): Sprite {
        val pixels = base.pixels.copyOf()
        for (y in 0 until base.height) {
            for (x in 0 until base.width) {
                val pixel = top.pixelAt(x, y)
                if ((pixel ushr 24) != 0) pixels[y * base.width + x] = pixel
            }
        }
        return Sprite(base.width, base.height, pixels)
    }

    /**
     * Like [overlaid], but clipped to the base's own shape — nothing may appear
     * where the base is empty. This is how a frightened foe gets a face without
     * its silhouette changing by a single pixel.
     */
    fun overlaidWithin(base: Sprite, top: Sprite): Sprite {
        val pixels = base.pixels.copyOf()
        for (i in pixels.indices) {
            if ((pixels[i] ushr 24) == 0) continue
            val x = i % base.width
            val y = i / base.width
            val pixel = top.pixelAt(x, y)
            if ((pixel ushr 24) != 0) pixels[i] = pixel
        }
        return Sprite(base.width, base.height, pixels)
    }

    /**
     * Eats the sprite away, 0.0 for untouched to 1.0 for nearly gone. Used for the
     * cat fainting: it fizzles out rather than simply vanishing.
     *
     * The pattern is a fixed function of the pixel's position, so the same amount
     * always produces the same frame and the animation cannot shimmer.
     */
    fun dissolved(sprite: Sprite, amount: Double): Sprite {
        if (amount <= 0.0) return sprite
        val pixels = sprite.pixels.copyOf()
        var kept = 0
        for (y in 0 until sprite.height) {
            for (x in 0 until sprite.width) {
                val index = y * sprite.width + x
                if ((pixels[index] ushr 24) == 0) continue
                if (noise(x, y) < amount) pixels[index] = Sprite.TRANSPARENT else kept++
            }
        }
        // A fully blank frame would flicker to nothing a beat early, so keep a speck.
        if (kept == 0) {
            val survivor = sprite.pixels.indexOfFirst { (it ushr 24) != 0 }
            if (survivor >= 0) pixels[survivor] = sprite.pixels[survivor]
        }
        return Sprite(sprite.width, sprite.height, pixels)
    }

    /** A scattered but repeatable 0..1 value per pixel. */
    private fun noise(x: Int, y: Int): Double {
        val h = (x * 73856093) xor (y * 19349663)
        return ((h ushr 8) and 0xFF) / 255.0
    }
}
