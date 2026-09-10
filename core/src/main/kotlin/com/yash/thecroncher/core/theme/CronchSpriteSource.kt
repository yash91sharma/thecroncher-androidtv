package com.yash.thecroncher.core.theme

/**
 * Builds every sprite from the grids in [CronchArt], coloured through a
 * [SpritePalette].
 *
 * The game ships with no art files at all — nothing to download, nothing to
 * licence — and a theme restyles the whole cast by handing this a different
 * palette. Sprites are rasterised once and cached, because a grid never changes
 * once it has been read.
 *
 * Three tricks keep the amount of hand-drawing down, and are the reason a foe
 * needs only two pictures:
 *  - facing left is the right-facing art [PixelArt.mirrored], and walking is the
 *    same art [PixelArt.shifted] up a pixel;
 *  - being frightened is the same shape as a [PixelArt.silhouette] with a face
 *    painted *inside* it, so the outline never changes;
 *  - fainting is the cat [PixelArt.dissolved] a little further each frame.
 */
class CronchSpriteSource(private val palette: SpritePalette) : SpriteSource {

    override val id: String = "cronch"

    private val ink = palette.ink
    private val cache = HashMap<Pair<SpriteId, Int>, Sprite>()

    override fun sprite(spriteId: SpriteId, frame: Int): Sprite {
        val f = spriteId.normaliseFrame(frame)
        return cache.getOrPut(spriteId to f) { render(spriteId, f) }
    }

    private fun render(id: SpriteId, frame: Int): Sprite = when (id) {
        SpriteId.CAT_RIGHT -> bounced(grid(CronchArt.CAT_RIGHT), frame)
        SpriteId.CAT_LEFT -> bounced(PixelArt.mirrored(grid(CronchArt.CAT_RIGHT)), frame)
        SpriteId.CAT_UP -> bounced(grid(CronchArt.CAT_UP), frame)
        SpriteId.CAT_DOWN -> bounced(grid(CronchArt.CAT_DOWN), frame)
        SpriteId.CAT_FAINT -> faint(frame)

        SpriteId.DOG_RIGHT -> grid(CronchArt.DOG_SIDE[frame])
        SpriteId.DOG_LEFT -> PixelArt.mirrored(grid(CronchArt.DOG_SIDE[frame]))
        SpriteId.DOG_UP, SpriteId.DOG_DOWN -> grid(CronchArt.DOG_FRONT[frame])
        SpriteId.DOG_SCARED -> scared(CronchArt.DOG_FRONT[frame], flashing = false)
        SpriteId.DOG_SCARED_FLASH -> scared(CronchArt.DOG_FRONT[frame], flashing = true)

        SpriteId.VACUUM_RIGHT -> grid(CronchArt.VACUUM[frame])
        SpriteId.VACUUM_LEFT -> PixelArt.mirrored(grid(CronchArt.VACUUM[frame]))
        SpriteId.VACUUM_UP, SpriteId.VACUUM_DOWN -> grid(CronchArt.VACUUM[frame])
        SpriteId.VACUUM_SCARED -> scared(CronchArt.VACUUM[frame], flashing = false)
        SpriteId.VACUUM_SCARED_FLASH -> scared(CronchArt.VACUUM[frame], flashing = true)

        SpriteId.SPRAY_RIGHT -> grid(CronchArt.SPRAY[frame])
        SpriteId.SPRAY_LEFT -> PixelArt.mirrored(grid(CronchArt.SPRAY[frame]))
        SpriteId.SPRAY_UP, SpriteId.SPRAY_DOWN -> grid(CronchArt.SPRAY[frame])
        SpriteId.SPRAY_SCARED -> scared(CronchArt.SPRAY[frame], flashing = false)
        SpriteId.SPRAY_SCARED_FLASH -> scared(CronchArt.SPRAY[frame], flashing = true)

        SpriteId.CUCUMBER_RIGHT -> grid(CronchArt.CUCUMBER[frame])
        SpriteId.CUCUMBER_LEFT -> PixelArt.mirrored(grid(CronchArt.CUCUMBER[frame]))
        SpriteId.CUCUMBER_UP, SpriteId.CUCUMBER_DOWN -> grid(CronchArt.CUCUMBER[frame])
        SpriteId.CUCUMBER_SCARED -> scared(CronchArt.CUCUMBER[frame], flashing = false)
        SpriteId.CUCUMBER_SCARED_FLASH -> scared(CronchArt.CUCUMBER[frame], flashing = true)

        SpriteId.PUFF -> grid(CronchArt.PUFF[frame])

        SpriteId.TREAT_TRIANGLE -> grid(CronchArt.TREAT_TRIANGLE)
        SpriteId.TREAT_SQUARE -> grid(CronchArt.TREAT_SQUARE)
        SpriteId.TREAT_FISH -> grid(CronchArt.TREAT_FISH)
        SpriteId.TREAT_STAR -> grid(CronchArt.TREAT_STAR)
        SpriteId.CATNIP -> grid(CronchArt.CATNIP)

        SpriteId.TOY_YARN -> grid(CronchArt.TOY_YARN)
        SpriteId.TOY_MILK -> grid(CronchArt.TOY_MILK)
        SpriteId.TOY_FISH -> grid(CronchArt.TOY_FISH)
        SpriteId.TOY_MOUSE -> grid(CronchArt.TOY_MOUSE)
        SpriteId.TOY_FEATHER -> grid(CronchArt.TOY_FEATHER)
        SpriteId.TOY_BIRD -> grid(CronchArt.TOY_BIRD)
        SpriteId.TOY_BELL -> grid(CronchArt.TOY_BELL)
        SpriteId.TOY_GOLDFISH -> grid(CronchArt.TOY_GOLDFISH)

        // The reserve lives are the cat's face, sitting patiently.
        SpriteId.LIFE_ICON -> grid(CronchArt.CAT_DOWN)
    }

    private fun grid(rows: List<String>) = PixelArt.sprite(rows, ink)

    /**
     * The cat's whole animation: the same face, a pixel higher on the off-beat.
     * Keeping the face still is deliberate — a chewing mouth at this size turns a
     * cat into a shape with a hole in it.
     */
    private fun bounced(sprite: Sprite, frame: Int) =
        if (frame == 0) sprite else PixelArt.shifted(sprite, dx = 0, dy = -1)

    /**
     * Blue and harmless, with a wobbly face. The face is painted only where the
     * foe already is, so a vacuum stays a vacuum and nothing floats beside it.
     */
    private fun scared(rows: List<String>, flashing: Boolean): Sprite {
        val body = if (flashing) palette.flashBody else palette.scaredBody
        val face = if (flashing) palette.flashFace else palette.scaredFace
        val shape = PixelArt.silhouette(grid(rows), body)
        return PixelArt.overlaidWithin(shape, PixelArt.sprite(CronchArt.SCARED_FACE, mapOf('*' to face)))
    }

    /** Dizzy eyes first, then the cat fizzles away over the remaining frames. */
    private fun faint(frame: Int): Sprite {
        val dizzy = PixelArt.overlaidWithin(grid(CronchArt.CAT_DOWN), PixelArt.sprite(CronchArt.CAT_DIZZY, ink))
        val steps = SpriteId.CAT_FAINT.frameCount - 1
        return PixelArt.dissolved(dizzy, frame.toDouble() / steps)
    }
}
