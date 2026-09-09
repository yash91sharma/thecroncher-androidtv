package com.yash.pacmantv.core.theme

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot

/**
 * Draws every sprite from code, so the game ships with no art files at all —
 * nothing to download, nothing to license, and a theme can restyle the lot by
 * tinting.
 *
 * Shapes are rasterised into flat ARGB arrays once and cached. Sprites are drawn
 * in white (0xFFFFFFFF) and coloured at draw time by the renderer's tint, which is
 * what lets one ghost body serve all four ghosts.
 */
object ProceduralSpriteSource : SpriteSource {

    override val id: String = "procedural"

    /** Pac-Man and the ghosts occupy a 16x16 cell; pellets and fruit are 8x8. */
    private const val ENTITY = 16
    private const val SMALL = 8

    private const val WHITE = 0xFFFFFFFF.toInt()

    private val cache = HashMap<Pair<SpriteId, Int>, Sprite>()

    override fun sprite(spriteId: SpriteId, frame: Int): Sprite {
        val f = spriteId.normaliseFrame(frame)
        return cache.getOrPut(spriteId to f) { render(spriteId, f) }
    }

    private fun render(id: SpriteId, frame: Int): Sprite = when (id) {
        SpriteId.PACMAN_RIGHT -> pacman(frame, facing = 0.0)
        SpriteId.PACMAN_LEFT -> pacman(frame, facing = Math.PI)
        SpriteId.PACMAN_UP -> pacman(frame, facing = -Math.PI / 2)
        SpriteId.PACMAN_DOWN -> pacman(frame, facing = Math.PI / 2)
        SpriteId.PACMAN_DEATH -> pacmanDeath(frame)

        SpriteId.GHOST_RIGHT -> ghost(frame, Look.EYES, dx = 1, dy = 0)
        SpriteId.GHOST_LEFT -> ghost(frame, Look.EYES, dx = -1, dy = 0)
        SpriteId.GHOST_UP -> ghost(frame, Look.EYES, dx = 0, dy = -1)
        SpriteId.GHOST_DOWN -> ghost(frame, Look.EYES, dx = 0, dy = 1)
        SpriteId.GHOST_FRIGHTENED -> ghost(frame, Look.SCARED, dx = 0, dy = 0)
        SpriteId.GHOST_FRIGHTENED_FLASH -> ghost(frame, Look.SCARED_FLASH, dx = 0, dy = 0)

        SpriteId.EYES_RIGHT -> eyesOnly(dx = 1, dy = 0)
        SpriteId.EYES_LEFT -> eyesOnly(dx = -1, dy = 0)
        SpriteId.EYES_UP -> eyesOnly(dx = 0, dy = -1)
        SpriteId.EYES_DOWN -> eyesOnly(dx = 0, dy = 1)

        SpriteId.PUPILS_RIGHT -> pupilsOnly(dx = 1, dy = 0)
        SpriteId.PUPILS_LEFT -> pupilsOnly(dx = -1, dy = 0)
        SpriteId.PUPILS_UP -> pupilsOnly(dx = 0, dy = -1)
        SpriteId.PUPILS_DOWN -> pupilsOnly(dx = 0, dy = 1)

        SpriteId.PELLET -> disc(SMALL, radius = 1.2)
        SpriteId.ENERGIZER -> disc(SMALL, radius = 3.4)

        SpriteId.FRUIT_CHERRY -> fruitCherry()
        SpriteId.FRUIT_STRAWBERRY -> fruitBlob(stemHeight = 2)
        SpriteId.FRUIT_ORANGE -> fruitBlob(stemHeight = 1)
        SpriteId.FRUIT_APPLE -> fruitBlob(stemHeight = 2)
        SpriteId.FRUIT_MELON -> fruitBlob(stemHeight = 1)
        SpriteId.FRUIT_GALAXIAN -> fruitGalaxian()
        SpriteId.FRUIT_BELL -> fruitBell()
        SpriteId.FRUIT_KEY -> fruitKey()

        SpriteId.LIFE_ICON -> pacman(frame = 1, facing = 0.0)
    }

    // ------------------------------------------------------------- Pac-Man --

    /**
     * A disc with a wedge removed. Frame 0 leaves the mouth shut, so a closed
     * Pac-Man looks identical whichever way he is facing — same as the arcade.
     */
    private fun pacman(frame: Int, facing: Double): Sprite {
        val mouthByFrame = doubleArrayOf(0.0, 0.30, 0.60)
        val half = mouthByFrame[frame.coerceIn(0, mouthByFrame.size - 1)]
        return wedgeDisc(ENTITY, radius = 7.2, facing = facing, halfMouthRadians = half)
    }

    /** The death spiral: the mouth opens until nothing is left. */
    private fun pacmanDeath(frame: Int): Sprite {
        val steps = SpriteId.PACMAN_DEATH.frameCount
        val openness = frame.toDouble() / (steps - 1)
        val half = openness * Math.PI
        // The last frame would be empty, which the "no blank sprites" rule forbids
        // and which would also flicker oddly — leave a sliver instead.
        return wedgeDisc(
            ENTITY,
            radius = 7.2,
            facing = -Math.PI / 2,
            halfMouthRadians = half.coerceAtMost(Math.PI * 0.94),
        )
    }

    private fun wedgeDisc(size: Int, radius: Double, facing: Double, halfMouthRadians: Double): Sprite {
        val pixels = IntArray(size * size)
        val centre = (size - 1) / 2.0
        for (y in 0 until size) {
            for (x in 0 until size) {
                val dx = x - centre
                val dy = y - centre
                if (hypot(dx, dy) > radius) continue
                if (halfMouthRadians > 0.0) {
                    val angle = angleDelta(atan2(dy, dx), facing)
                    if (abs(angle) < halfMouthRadians) continue
                }
                pixels[y * size + x] = WHITE
            }
        }
        return Sprite(size, size, pixels)
    }

    /** Signed smallest difference between two angles, in radians. */
    private fun angleDelta(a: Double, b: Double): Double {
        var d = a - b
        while (d > Math.PI) d -= 2 * Math.PI
        while (d < -Math.PI) d += 2 * Math.PI
        return d
    }

    // -------------------------------------------------------------- Ghosts --

    private enum class Look { EYES, SCARED, SCARED_FLASH }

    /**
     * Dome on top, wavy skirt below. The two frames alternate the skirt so the
     * ghost appears to shuffle along.
     */
    private fun ghost(frame: Int, look: Look, dx: Int, dy: Int): Sprite {
        val size = ENTITY
        val pixels = IntArray(size * size)
        val centreX = (size - 1) / 2.0
        val domeCentreY = 6.5
        val radius = 7.2

        for (y in 0 until size) {
            for (x in 0 until size) {
                val inBody = if (y <= domeCentreY) {
                    hypot(x - centreX, y - domeCentreY) <= radius
                } else {
                    abs(x - centreX) <= radius
                }
                if (!inBody) continue
                if (y >= size - 3 && inSkirtNotch(x, y, size, frame)) continue
                pixels[y * size + x] = WHITE
            }
        }

        when (look) {
            Look.EYES -> punchEyes(pixels, size, dx, dy)
            Look.SCARED -> drawScaredFace(pixels, size)
            Look.SCARED_FLASH -> drawScaredFace(pixels, size)
        }
        return Sprite(size, size, pixels)
    }

    /** The scalloped bottom edge; frame 1 shifts the notches by half a period. */
    private fun inSkirtNotch(x: Int, y: Int, size: Int, frame: Int): Boolean {
        val period = 5
        val phase = if (frame == 0) 0 else period / 2
        val depth = y - (size - 3)
        val position = (x + phase) % period
        return position < depth || position >= period - depth + 1
    }

    /** Cuts eye holes out of the body, looking in the direction of travel. */
    private fun punchEyes(pixels: IntArray, size: Int, dx: Int, dy: Int) {
        for (side in intArrayOf(-1, 1)) {
            val eyeX = (size - 1) / 2.0 + side * 3.0
            val eyeY = 6.0
            for (y in 0 until size) {
                for (x in 0 until size) {
                    if (hypot(x - eyeX, y - eyeY) <= 2.2) {
                        pixels[y * size + x] = Sprite.TRANSPARENT
                    }
                }
            }
        }
        // The pupils sit inside the holes, pushed toward where the ghost is heading.
        for (side in intArrayOf(-1, 1)) {
            val pupilX = (size - 1) / 2.0 + side * 3.0 + dx * 1.0
            val pupilY = 6.0 + dy * 1.0
            for (y in 0 until size) {
                for (x in 0 until size) {
                    if (hypot(x - pupilX, y - pupilY) <= 1.1) {
                        pixels[y * size + x] = WHITE
                    }
                }
            }
        }
    }

    /** Frightened ghosts get dot eyes and a zig-zag mouth. */
    private fun drawScaredFace(pixels: IntArray, size: Int) {
        fun clear(x: Int, y: Int) {
            if (x in 0 until size && y in 0 until size) pixels[y * size + x] = Sprite.TRANSPARENT
        }
        for (side in intArrayOf(-1, 1)) {
            val eyeX = (size - 1) / 2 + side * 3
            clear(eyeX, 5); clear(eyeX + 1, 5)
            clear(eyeX, 6); clear(eyeX + 1, 6)
        }
        val mouthY = 10
        for (x in 3..12) {
            clear(x, if ((x / 2) % 2 == 0) mouthY else mouthY + 1)
        }
    }

    private fun eyesOnly(dx: Int, dy: Int): Sprite {
        val size = ENTITY
        val pixels = IntArray(size * size)
        for (side in intArrayOf(-1, 1)) {
            val eyeX = (size - 1) / 2.0 + side * 3.0
            val eyeY = 6.0
            for (y in 0 until size) {
                for (x in 0 until size) {
                    if (hypot(x - eyeX, y - eyeY) <= 2.2) pixels[y * size + x] = WHITE
                }
            }
        }
        return Sprite(size, size, pixels)
    }

    /** Just the pupils, offset the way the ghost is looking. */
    private fun pupilsOnly(dx: Int, dy: Int): Sprite {
        val size = ENTITY
        val pixels = IntArray(size * size)
        for (side in intArrayOf(-1, 1)) {
            val cx = (size - 1) / 2.0 + side * 3.0 + dx * 1.0
            val cy = 6.0 + dy * 1.2
            for (y in 0 until size) {
                for (x in 0 until size) {
                    if (hypot(x - cx, y - cy) <= 1.2) pixels[y * size + x] = WHITE
                }
            }
        }
        return Sprite(size, size, pixels)
    }

    // ------------------------------------------------------- Pellets, fruit --

    private fun disc(size: Int, radius: Double): Sprite {
        val pixels = IntArray(size * size)
        val centre = (size - 1) / 2.0
        for (y in 0 until size) {
            for (x in 0 until size) {
                if (hypot(x - centre, y - centre) <= radius) pixels[y * size + x] = WHITE
            }
        }
        return Sprite(size, size, pixels)
    }

    private fun blank(size: Int) = IntArray(size * size)

    private fun fruitBlob(stemHeight: Int): Sprite {
        val size = ENTITY
        val pixels = blank(size)
        val centre = (size - 1) / 2.0
        for (y in 0 until size) {
            for (x in 0 until size) {
                if (hypot(x - centre, y - centre - 2) <= 5.5) pixels[y * size + x] = WHITE
            }
        }
        for (s in 0 until stemHeight) {
            val y = 2 + s
            pixels[y * size + centre.toInt()] = WHITE
            pixels[y * size + centre.toInt() + 1] = WHITE
        }
        return Sprite(size, size, pixels)
    }

    private fun fruitCherry(): Sprite {
        val size = ENTITY
        val pixels = blank(size)
        for ((cx, cy) in listOf(5.0 to 10.0, 10.0 to 11.0)) {
            for (y in 0 until size) {
                for (x in 0 until size) {
                    if (hypot(x - cx, y - cy) <= 3.2) pixels[y * size + x] = WHITE
                }
            }
        }
        for (i in 0 until 6) {
            val y = 3 + i
            pixels[y * size + (5 + i).coerceAtMost(size - 1)] = WHITE
        }
        return Sprite(size, size, pixels)
    }

    private fun fruitGalaxian(): Sprite {
        val size = ENTITY
        val pixels = blank(size)
        for (y in 3..12) {
            val halfWidth = (y - 3) / 2
            for (x in (7 - halfWidth)..(8 + halfWidth)) {
                if (x in 0 until size) pixels[y * size + x] = WHITE
            }
        }
        return Sprite(size, size, pixels)
    }

    private fun fruitBell(): Sprite {
        val size = ENTITY
        val pixels = blank(size)
        for (y in 3..11) {
            val halfWidth = 1 + (y - 3) / 2
            for (x in (7 - halfWidth)..(8 + halfWidth)) {
                if (x in 0 until size) pixels[y * size + x] = WHITE
            }
        }
        for (x in 4..11) pixels[12 * size + x] = WHITE
        return Sprite(size, size, pixels)
    }

    private fun fruitKey(): Sprite {
        val size = ENTITY
        val pixels = blank(size)
        for (y in 3..6) {
            for (x in 6..9) {
                if (y == 3 || y == 6 || x == 6 || x == 9) pixels[y * size + x] = WHITE
            }
        }
        for (y in 7..13) pixels[y * size + 7] = WHITE
        for (x in 7..9) pixels[12 * size + x] = WHITE
        return Sprite(size, size, pixels)
    }
}
