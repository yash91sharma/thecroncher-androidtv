package com.yash.pacmantv.core.ui

import com.yash.pacmantv.core.game.Direction
import com.yash.pacmantv.core.game.GamePhase
import com.yash.pacmantv.core.game.GameState
import com.yash.pacmantv.core.game.Ghost
import com.yash.pacmantv.core.game.GhostMode
import com.yash.pacmantv.core.game.LevelTable
import com.yash.pacmantv.core.game.Maze
import com.yash.pacmantv.core.game.SUBPIXEL
import com.yash.pacmantv.core.game.Tile
import com.yash.pacmantv.core.ports.Align
import com.yash.pacmantv.core.ports.Gfx
import com.yash.pacmantv.core.theme.SpriteId
import com.yash.pacmantv.core.theme.Theme

/**
 * Draws the playfield.
 *
 * Everything goes through [Gfx] and takes its colours from [Theme], so this same
 * code renders every theme, and a test can drive it against a recording Gfx and
 * assert what was drawn without an emulator.
 *
 * Walls are drawn as thin lines along the boundary between wall and corridor
 * rather than as solid blocks, which is what gives the maze its arcade look.
 */
object GameRenderer {

    fun render(g: Gfx, theme: Theme, state: GameState, tick: Long) {
        g.clear(theme.background)

        drawTopHud(g, theme, state)
        drawMaze(g, theme, state, tick)
        drawFruit(g, theme, state)
        drawPacman(g, theme, state, tick)
        drawGhosts(g, theme, state, tick)
        drawBottomHud(g, theme, state)
        drawMessages(g, theme, state)
    }

    // ----------------------------------------------------------------- HUD --

    private fun drawTopHud(g: Gfx, theme: Theme, state: GameState) {
        g.drawText(Strings.ONE_UP, Layout.SCORE_LEFT_X, Layout.SCORE_LABEL_Y, theme.hud.text)
        g.drawText(
            state.score.toString().padStart(2, '0'),
            Layout.SCORE_LEFT_X, Layout.SCORE_VALUE_Y, theme.hud.score,
        )
        g.drawText(
            Strings.HIGH_SCORE,
            Layout.HIGH_SCORE_CENTRE_X, Layout.SCORE_LABEL_Y, theme.hud.text, Align.CENTER,
        )
        g.drawText(
            state.highScore.toString().padStart(2, '0'),
            Layout.HIGH_SCORE_CENTRE_X, Layout.SCORE_VALUE_Y, theme.hud.highScore, Align.CENTER,
        )
    }

    private fun drawBottomHud(g: Gfx, theme: Theme, state: GameState) {
        // One Pac-Man per life still in reserve.
        val icon = theme.sprites.sprite(SpriteId.LIFE_ICON, 0)
        for (i in 0 until (state.lives - 1).coerceIn(0, 5)) {
            g.drawSprite(icon, Layout.LIVES_LEFT_X + i * Layout.LIVES_SPACING, Layout.LIVES_Y, theme.hud.lifeIcon)
        }

        // The last few levels' fruit, newest on the right, as the arcade does.
        val shown = (1..state.level).toList().takeLast(6)
        for ((i, level) in shown.withIndex()) {
            val fruit = LevelTable.fruitForLevel(level)
            val x = Layout.FRUIT_RIGHT_X - (shown.size - i) * 16
            g.drawSprite(theme.sprites.sprite(fruit.sprite, 0), x, Layout.LIVES_Y, theme.hud.fruitText)
        }
    }

    private fun drawMessages(g: Gfx, theme: Theme, state: GameState) {
        when (state.phase) {
            GamePhase.READY -> g.drawText(
                Strings.READY, Layout.MESSAGE_CENTRE_X, Layout.READY_Y,
                theme.hud.highScore, Align.CENTER,
            )
            GamePhase.GAME_OVER -> g.drawText(
                Strings.GAME_OVER, Layout.MESSAGE_CENTRE_X, Layout.GAME_OVER_Y,
                theme.entities.blinky, Align.CENTER,
            )
            else -> Unit
        }
    }

    // ---------------------------------------------------------------- maze --

    private fun drawMaze(g: Gfx, theme: Theme, state: GameState, tick: Long) {
        val maze = state.maze

        for (ty in 0 until maze.height) {
            for (tx in 0 until maze.width) {
                val px = Layout.mazeToScreenX(tx * Layout.TILE)
                val py = Layout.mazeToScreenY(ty * Layout.TILE)

                when (maze.tileAt(tx, ty)) {
                    Tile.WALL -> drawWallEdges(g, theme, state, tx, ty, px, py)
                    Tile.DOOR -> g.fillRect(px, py + 3, Layout.TILE, 2, theme.maze.door)
                    else -> Unit
                }

                if (state.isPelletEaten(tx, ty)) continue
                when (maze.tileAt(tx, ty)) {
                    Tile.DOT -> g.drawSprite(
                        theme.sprites.sprite(SpriteId.PELLET, 0), px, py, theme.pellet.pellet,
                    )
                    Tile.ENERGIZER ->
                        // Energizers blink, which is how the arcade draws the eye to them.
                        if ((tick / 12) % 2 == 0L) {
                            g.drawSprite(
                                theme.sprites.sprite(SpriteId.ENERGIZER, 0), px, py,
                                theme.pellet.energizer,
                            )
                        }
                    else -> Unit
                }
            }
        }
    }

    /**
     * Outlines a wall tile only where it meets open space, producing the thin
     * double-line corridors of the original rather than a slab of solid colour.
     */
    private fun drawWallEdges(
        g: Gfx,
        theme: Theme,
        state: GameState,
        tx: Int,
        ty: Int,
        px: Int,
        py: Int,
    ) {
        val maze = state.maze
        val size = Layout.TILE
        val colour = theme.maze.wall

        fun solid(dx: Int, dy: Int): Boolean {
            val nx = maze.wrapX(tx + dx)
            val ny = ty + dy
            if (ny !in 0 until maze.height) return true
            return maze.tileAt(nx, ny) == Tile.WALL
        }

        if (!solid(0, -1)) g.fillRect(px, py, size, 1, colour)
        if (!solid(0, 1)) g.fillRect(px, py + size - 1, size, 1, colour)
        if (!solid(-1, 0)) g.fillRect(px, py, 1, size, colour)
        if (!solid(1, 0)) g.fillRect(px + size - 1, py, 1, size, colour)

        // Fill the inside corners so diagonally-touching walls join up cleanly.
        if (!solid(-1, 0) && !solid(0, -1)) g.fillRect(px, py, 1, 1, colour)
        if (!solid(1, 0) && !solid(0, -1)) g.fillRect(px + size - 1, py, 1, 1, colour)
    }

    // ------------------------------------------------------------- actors --

    private fun drawPacman(g: Gfx, theme: Theme, state: GameState, tick: Long) {
        if (state.phase == GamePhase.LEVEL_COMPLETE) return

        val cx = Layout.mazeToScreenX(state.pacman.x / SUBPIXEL)
        val cy = Layout.mazeToScreenY(state.pacman.y / SUBPIXEL)

        if (state.phase == GamePhase.DYING) {
            val frame = (state.phaseTicks / 10).toInt()
                .coerceAtMost(SpriteId.PACMAN_DEATH.frameCount - 1)
            g.drawSpriteCentred(
                theme.sprites.sprite(SpriteId.PACMAN_DEATH, frame), cx, cy, theme.entities.pacman,
            )
            return
        }

        val facing = when (state.pacman.direction) {
            Direction.UP -> SpriteId.PACMAN_UP
            Direction.DOWN -> SpriteId.PACMAN_DOWN
            Direction.LEFT -> SpriteId.PACMAN_LEFT
            Direction.RIGHT -> SpriteId.PACMAN_RIGHT
        }
        // Frame 0 is the closed mouth; the cycle 0-1-2-1 gives the classic chomp.
        val frame = MOUTH_CYCLE[((state.pacman.movingTicks / 4) % MOUTH_CYCLE.size).toInt()]
        g.drawSpriteCentred(theme.sprites.sprite(facing, frame), cx, cy, theme.entities.pacman)
    }

    private fun drawGhosts(g: Gfx, theme: Theme, state: GameState, tick: Long) {
        if (state.phase == GamePhase.DYING || state.phase == GamePhase.LEVEL_COMPLETE) return

        for ((index, ghost) in state.ghosts.withIndex()) {
            val cx = Layout.mazeToScreenX(ghost.x / SUBPIXEL)
            val cy = Layout.mazeToScreenY(ghost.y / SUBPIXEL)
            val wobble = ((tick / 8) % 2).toInt()

            when (ghost.mode) {
                GhostMode.EATEN -> drawEyes(g, theme, ghost, cx, cy)

                GhostMode.FRIGHTENED -> {
                    // Near the end of the blue time the ghosts flash white as a warning.
                    val flashing = state.isFrightFlashing && (tick / 8) % 2 == 0L
                    val sprite = if (flashing) SpriteId.GHOST_FRIGHTENED_FLASH else SpriteId.GHOST_FRIGHTENED
                    val colour = if (flashing) theme.entities.frightenedFlash else theme.entities.frightened
                    g.drawSpriteCentred(theme.sprites.sprite(sprite, wobble), cx, cy, colour)
                }

                else -> {
                    val body = when (ghost.direction) {
                        Direction.UP -> SpriteId.GHOST_UP
                        Direction.DOWN -> SpriteId.GHOST_DOWN
                        Direction.LEFT -> SpriteId.GHOST_LEFT
                        Direction.RIGHT -> SpriteId.GHOST_RIGHT
                    }
                    g.drawSpriteCentred(
                        theme.sprites.sprite(body, wobble), cx, cy, theme.ghostColour(index),
                    )
                    drawEyes(g, theme, ghost, cx, cy)
                }
            }
        }
    }

    private fun drawEyes(g: Gfx, theme: Theme, ghost: Ghost, cx: Int, cy: Int) {
        val (eyes, pupils) = when (ghost.direction) {
            Direction.UP -> SpriteId.EYES_UP to SpriteId.PUPILS_UP
            Direction.DOWN -> SpriteId.EYES_DOWN to SpriteId.PUPILS_DOWN
            Direction.LEFT -> SpriteId.EYES_LEFT to SpriteId.PUPILS_LEFT
            Direction.RIGHT -> SpriteId.EYES_RIGHT to SpriteId.PUPILS_RIGHT
        }
        g.drawSpriteCentred(theme.sprites.sprite(eyes, 0), cx, cy, theme.entities.eyeWhite)
        g.drawSpriteCentred(theme.sprites.sprite(pupils, 0), cx, cy, theme.entities.eyePupil)
    }

    private fun drawFruit(g: Gfx, theme: Theme, state: GameState) {
        val tile = state.fruitTile ?: return
        val fruit = LevelTable.fruitForLevel(state.level)
        g.drawSpriteCentred(
            theme.sprites.sprite(fruit.sprite, 0),
            Layout.mazeToScreenX(tile.x * Layout.TILE + Layout.TILE / 2),
            Layout.mazeToScreenY(tile.y * Layout.TILE + Layout.TILE / 2),
            theme.hud.fruitText,
        )
    }

    private val MOUTH_CYCLE = intArrayOf(0, 1, 2, 1)
}
