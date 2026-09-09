package com.yash.pacmantv

import com.yash.pacmantv.core.game.VIRTUAL_HEIGHT
import com.yash.pacmantv.core.game.VIRTUAL_WIDTH
import com.yash.pacmantv.core.ports.Align
import com.yash.pacmantv.core.ports.Gfx
import com.yash.pacmantv.core.theme.SpriteId
import com.yash.pacmantv.core.theme.Theme

/**
 * Phase 3 scaffolding: proves the renderer, the theme system, the sprite source,
 * the font and the 60 Hz loop all work together on the actual television, before
 * any gameplay exists. Replaced by the real screens in Phase 5.
 */
object TestPatternScreen {

    fun render(g: Gfx, theme: Theme, tick: Long, fps: Int) {
        g.clear(theme.background)

        // A one-pixel border proves nothing is being cropped by overscan.
        drawBorder(g, theme.maze.wall)

        g.drawText("PAC-MAN TV", VIRTUAL_WIDTH / 2, 16, theme.menu.title, Align.CENTER)
        g.drawText(theme.displayName, VIRTUAL_WIDTH / 2, 26, theme.menu.item, Align.CENTER)

        // Animated Pac-Man, cycling mouth and direction so all frames get seen.
        val mouthFrame = MOUTH_CYCLE[((tick / 6) % MOUTH_CYCLE.size).toInt()]
        val facing = FACINGS[((tick / 48) % FACINGS.size).toInt()]
        g.drawSpriteCentred(
            theme.sprites.sprite(facing, mouthFrame),
            VIRTUAL_WIDTH / 2,
            60,
            theme.entities.pacman,
        )

        // The four ghosts, each in its own colour, feet shuffling.
        val ghostFrame = ((tick / 8) % 2).toInt()
        val ghostSprite = theme.sprites.sprite(SpriteId.GHOST_LEFT, ghostFrame)
        for (i in 0 until 4) {
            g.drawSpriteCentred(ghostSprite, 52 + i * 40, 92, theme.ghostColour(i))
        }

        // Frightened, flashing and eyes-only states.
        g.drawSpriteCentred(
            theme.sprites.sprite(SpriteId.GHOST_FRIGHTENED, ghostFrame),
            72, 120, theme.entities.frightened,
        )
        g.drawSpriteCentred(
            theme.sprites.sprite(SpriteId.GHOST_FRIGHTENED_FLASH, ghostFrame),
            112, 120, theme.entities.frightenedFlash,
        )
        g.drawSpriteCentred(
            theme.sprites.sprite(SpriteId.EYES_LEFT, 0),
            152, 120, theme.entities.eyeWhite,
        )

        // Pellets and every fruit, to confirm the whole sprite table resolves.
        g.drawSpriteCentred(theme.sprites.sprite(SpriteId.PELLET, 0), 24, 148, theme.pellet.pellet)
        g.drawSpriteCentred(theme.sprites.sprite(SpriteId.ENERGIZER, 0), 40, 148, theme.pellet.energizer)
        for ((i, fruit) in FRUITS.withIndex()) {
            g.drawSpriteCentred(theme.sprites.sprite(fruit, 0), 64 + i * 20, 148, theme.hud.fruitText)
        }

        // The death animation runs on a slow loop of its own.
        val deathFrame = ((tick / 8) % SpriteId.PACMAN_DEATH.frameCount).toInt()
        g.drawText("DEATH", 30, 176, theme.hud.text, Align.LEFT)
        g.drawSpriteCentred(
            theme.sprites.sprite(SpriteId.PACMAN_DEATH, deathFrame),
            110, 180, theme.entities.pacman,
        )

        // A grey ramp: on an OLED this makes banding or a wrong colour space obvious.
        for (i in 0 until 16) {
            val v = i * 17
            g.fillRect(16 + i * 12, 200, 12, 10, 0xFF000000.toInt() or (v shl 16) or (v shl 8) or v)
        }

        // Font sample and live counters.
        g.drawText("ABCDEFGHIJKLM", VIRTUAL_WIDTH / 2, 220, theme.hud.text, Align.CENTER)
        g.drawText("NOPQRSTUVWXYZ", VIRTUAL_WIDTH / 2, 230, theme.hud.text, Align.CENTER)
        g.drawText("0123456789 .,:-!?", VIRTUAL_WIDTH / 2, 240, theme.hud.text, Align.CENTER)

        g.drawText("FPS $fps", 8, 256, theme.hud.score, Align.LEFT)
        g.drawText("TICK $tick", VIRTUAL_WIDTH - 8, 256, theme.hud.highScore, Align.RIGHT)
        g.drawText("DPAD: THEME   A: NEXT", VIRTUAL_WIDTH / 2, 272, theme.menu.footer, Align.CENTER)
    }

    private fun drawBorder(g: Gfx, color: Int) {
        g.fillRect(0, 0, VIRTUAL_WIDTH, 1, color)
        g.fillRect(0, VIRTUAL_HEIGHT - 1, VIRTUAL_WIDTH, 1, color)
        g.fillRect(0, 0, 1, VIRTUAL_HEIGHT, color)
        g.fillRect(VIRTUAL_WIDTH - 1, 0, 1, VIRTUAL_HEIGHT, color)
    }

    private val MOUTH_CYCLE = intArrayOf(0, 1, 2, 1)

    private val FACINGS = arrayOf(
        SpriteId.PACMAN_RIGHT,
        SpriteId.PACMAN_DOWN,
        SpriteId.PACMAN_LEFT,
        SpriteId.PACMAN_UP,
    )

    private val FRUITS = arrayOf(
        SpriteId.FRUIT_CHERRY,
        SpriteId.FRUIT_STRAWBERRY,
        SpriteId.FRUIT_ORANGE,
        SpriteId.FRUIT_APPLE,
        SpriteId.FRUIT_MELON,
        SpriteId.FRUIT_GALAXIAN,
        SpriteId.FRUIT_BELL,
        SpriteId.FRUIT_KEY,
    )
}
