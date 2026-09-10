package com.yash.thecroncher.core.ui

import com.yash.thecroncher.core.game.Direction
import com.yash.thecroncher.core.game.GamePhase
import com.yash.thecroncher.core.game.GameState
import com.yash.thecroncher.core.game.GhostMode
import com.yash.thecroncher.core.game.LevelTable
import com.yash.thecroncher.core.game.SUBPIXEL
import com.yash.thecroncher.core.game.Tile
import com.yash.thecroncher.core.ports.Align
import com.yash.thecroncher.core.ports.Gfx
import com.yash.thecroncher.core.theme.FoeArt
import com.yash.thecroncher.core.theme.FoeCast
import com.yash.thecroncher.core.theme.SpriteId
import com.yash.thecroncher.core.theme.Theme
import com.yash.thecroncher.core.theme.TreatArt

/**
 * Draws the playfield.
 *
 * Everything goes through [Gfx] and takes its art and colours from [Theme], so
 * this same code renders any theme, and a test can drive it against a recording
 * Gfx and assert what was drawn without an emulator.
 *
 * The cast carries its own colours — a grey tabby, a brown dog — so sprites are
 * drawn untinted. Walls are drawn as thin lines along the boundary between wall
 * and corridor rather than as solid blocks, which is what gives the maze its
 * arcade look.
 */
object GameRenderer {

    fun render(g: Gfx, theme: Theme, state: GameState, tick: Long) {
        g.clear(theme.background)

        drawTopHud(g, theme, state)
        drawMaze(g, theme, state, tick)
        drawToy(g, theme, state)
        drawCat(g, theme, state, tick)
        drawFoes(g, theme, state, tick)
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
        // One cat per life still in reserve.
        val icon = theme.sprites.sprite(SpriteId.LIFE_ICON, 0)
        for (i in 0 until (state.lives - 1).coerceIn(0, 5)) {
            g.drawSprite(icon, Layout.LIVES_LEFT_X + i * Layout.LIVES_SPACING, Layout.LIVES_Y)
        }

        // The last few levels' toys, newest on the right, as the arcade does.
        val shown = (1..state.level).toList().takeLast(6)
        for ((i, level) in shown.withIndex()) {
            val toy = LevelTable.toyForLevel(level)
            val x = Layout.TOY_RIGHT_X - (shown.size - i) * 16
            g.drawSprite(theme.sprites.sprite(toy.sprite, 0), x, Layout.LIVES_Y)
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
                theme.hud.alert, Align.CENTER,
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
                    // The treats are four shapes, picked by tile so one never
                    // changes under the cat.
                    Tile.DOT -> g.drawSprite(
                        theme.sprites.sprite(TreatArt.forTile(tx, ty), 0), px, py,
                    )
                    Tile.ENERGIZER ->
                        // The catnip blinks, which is how the arcade draws the eye to it.
                        if ((tick / 12) % 2 == 0L) {
                            g.drawSprite(theme.sprites.sprite(SpriteId.CATNIP, 0), px, py)
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

    private fun drawCat(g: Gfx, theme: Theme, state: GameState, tick: Long) {
        if (state.phase == GamePhase.LEVEL_COMPLETE) return

        val cx = Layout.mazeToScreenX(state.croncher.x / SUBPIXEL)
        val cy = Layout.mazeToScreenY(state.croncher.y / SUBPIXEL)

        if (state.phase == GamePhase.DYING) {
            val frame = (state.phaseTicks / 10).toInt()
                .coerceAtMost(SpriteId.CAT_FAINT.frameCount - 1)
            g.drawSpriteCentred(theme.sprites.sprite(SpriteId.CAT_FAINT, frame), cx, cy)
            return
        }

        val facing = when (state.croncher.direction) {
            Direction.UP -> SpriteId.CAT_UP
            Direction.DOWN -> SpriteId.CAT_DOWN
            Direction.LEFT -> SpriteId.CAT_LEFT
            Direction.RIGHT -> SpriteId.CAT_RIGHT
        }
        // He bounces along on the move and stands still when he is stopped.
        val frame = ((state.croncher.movingTicks / BOUNCE_TICKS) % 2).toInt()
        g.drawSpriteCentred(theme.sprites.sprite(facing, frame), cx, cy)
    }

    private fun drawFoes(g: Gfx, theme: Theme, state: GameState, tick: Long) {
        if (state.phase == GamePhase.DYING || state.phase == GamePhase.LEVEL_COMPLETE) return

        for ((index, ghost) in state.ghosts.withIndex()) {
            val cx = Layout.mazeToScreenX(ghost.x / SUBPIXEL)
            val cy = Layout.mazeToScreenY(ghost.y / SUBPIXEL)
            val wobble = ((tick / 8) % 2).toInt()
            val art = FoeCast.forIndex(index)

            val sprite = when (ghost.mode) {
                // Dealt with: all that is left is a puff, hurrying home.
                GhostMode.EATEN -> SpriteId.PUFF

                GhostMode.FRIGHTENED -> {
                    // Near the end of the catnip the foes flash white as a warning.
                    val flashing = state.isFrightFlashing && (tick / 8) % 2 == 0L
                    if (flashing) art.scaredFlash else art.scared
                }

                else -> facing(art, ghost.direction)
            }
            g.drawSpriteCentred(theme.sprites.sprite(sprite, wobble), cx, cy)
        }
    }

    private fun facing(art: FoeArt, direction: Direction): SpriteId = when (direction) {
        Direction.UP -> art.up
        Direction.DOWN -> art.down
        Direction.LEFT -> art.left
        Direction.RIGHT -> art.right
    }

    private fun drawToy(g: Gfx, theme: Theme, state: GameState) {
        val tile = state.toyTile ?: return
        val toy = LevelTable.toyForLevel(state.level)
        g.drawSpriteCentred(
            theme.sprites.sprite(toy.sprite, 0),
            Layout.mazeToScreenX(tile.x * Layout.TILE + Layout.TILE / 2),
            Layout.mazeToScreenY(tile.y * Layout.TILE + Layout.TILE / 2),
        )
    }

    /** Ticks per half-bounce: fast enough to read as a trot, slow enough to see. */
    private const val BOUNCE_TICKS = 6
}
