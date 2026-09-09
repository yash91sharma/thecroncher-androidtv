package com.yash.pacmantv.core.ui

import com.yash.pacmantv.core.game.Difficulties
import com.yash.pacmantv.core.game.Direction
import com.yash.pacmantv.core.game.GamePhase
import com.yash.pacmantv.core.game.GameState
import com.yash.pacmantv.core.game.Maze
import com.yash.pacmantv.core.ports.SeededRng
import com.yash.pacmantv.core.support.RecordingGfx
import com.yash.pacmantv.core.theme.SpriteId
import com.yash.pacmantv.core.theme.ThemeRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Rendering tests without an emulator — the dividend from putting drawing behind
 * the [com.yash.pacmantv.core.ports.Gfx] port. These catch the class of bug that
 * is otherwise only visible by squinting at a screenshot of a television.
 */
class GameRendererTest {

    private val theme = ThemeRegistry.default

    private fun game() = GameState(
        maze = Maze.loadClassic(),
        difficulty = Difficulties.NORMAL,
        rng = SeededRng(1),
    ).apply { startNewGame() }

    private fun draw(state: GameState, tick: Long): RecordingGfx =
        RecordingGfx().also { GameRenderer.render(it, theme, state, tick) }

    private fun RecordingGfx.spriteCount(id: SpriteId, frames: Int = 8): Int {
        val wanted = (0 until frames).map { theme.sprites.sprite(id, it) }.toSet()
        return sprites.count { it.sprite in wanted }
    }

    @Test
    fun `the screen is cleared to the theme background`() {
        val calls = draw(game(), 0).calls
        assertEquals(RecordingGfx.Call.Clear(theme.background), calls.first())
    }

    @Test
    fun `the score and high score are labelled and shown`() {
        val g = game()
        val texts = draw(g, 0).textStrings()
        assertTrue(Strings.ONE_UP in texts)
        assertTrue(Strings.HIGH_SCORE in texts)
    }

    @Test
    fun `READY is shown before play starts and not afterwards`() {
        val g = game()
        assertEquals(GamePhase.READY, g.phase)
        assertTrue(Strings.READY in draw(g, 0).textStrings())

        repeat(GameState.READY_TICKS + 1) { g.tick() }
        assertEquals(GamePhase.PLAYING, g.phase)
        assertTrue(Strings.READY !in draw(g, 0).textStrings())
    }

    @Test
    fun `all 240 dots are drawn at the start of a level`() {
        val drawn = draw(game(), 0).spriteCount(SpriteId.PELLET)
        assertEquals(240, drawn)
    }

    @Test
    fun `energizers are drawn, and blink rather than sitting still`() {
        // Two frames a fifth of a second apart: one must show them, one must not.
        val g = game()
        val counts = (0L until 48L).map { draw(g, it).spriteCount(SpriteId.ENERGIZER) }
        assertTrue("energizers are never drawn at all", counts.any { it == 4 })
        assertTrue("energizers never blink off", counts.any { it == 0 })
        assertTrue("expected only 0 or 4 energizers, saw $counts", counts.all { it == 0 || it == 4 })
    }

    @Test
    fun `an eaten pellet stops being drawn`() {
        val g = game()
        repeat(GameState.READY_TICKS) { g.tick() }
        val before = draw(g, 0).spriteCount(SpriteId.PELLET)

        val dot = g.maze.pelletPositions.first { g.maze.tileAt(it.x, it.y) == com.yash.pacmantv.core.game.Tile.DOT }
        g.pacman.placeAtTileCentre(dot, Direction.LEFT)
        g.tick()

        val after = draw(g, 0).spriteCount(SpriteId.PELLET)
        assertTrue("eating a dot should remove it from the screen", after < before)
    }

    @Test
    fun `pacman is drawn`() {
        val g = game()
        val gfx = draw(g, 0)
        val pacSprites = Direction.entries.flatMap { dir ->
            val id = when (dir) {
                Direction.UP -> SpriteId.PACMAN_UP
                Direction.DOWN -> SpriteId.PACMAN_DOWN
                Direction.LEFT -> SpriteId.PACMAN_LEFT
                Direction.RIGHT -> SpriteId.PACMAN_RIGHT
            }
            (0 until id.frameCount).map { theme.sprites.sprite(id, it) }
        }.toSet()
        assertTrue(gfx.sprites.any { it.sprite in pacSprites })
    }

    @Test
    fun `every ghost is drawn with its own colour`() {
        // On a fresh board: Blinky is out and the other three wait in the house,
        // and all four are on screen. Later in a life they may be blue, eaten, or
        // hidden behind the death animation.
        val g = game()
        val gfx = draw(g, 0)
        val tints = gfx.sprites.mapNotNull { it.tint }.toSet()
        for (i in 0 until 4) {
            assertTrue("ghost $i's colour never appears", theme.ghostColour(i) in tints)
        }
    }

    @Test
    fun `lives are shown as one icon per life in reserve`() {
        // Counted by sprite rather than by colour: the life icon and Pac-Man
        // himself are deliberately the same yellow.
        val g = game()
        val icon = theme.sprites.sprite(SpriteId.LIFE_ICON, 0)
        val icons = draw(g, 0).sprites.count { it.sprite === icon }
        assertEquals(g.lives - 1, icons)
    }

    @Test
    fun `nothing is drawn outside the screen bounds`() {
        val g = game()
        repeat(GameState.READY_TICKS + 300) { g.tick() }
        val gfx = draw(g, 0)
        for (rect in gfx.rects) {
            assertTrue("rect at ${rect.x},${rect.y} starts off-screen", rect.x >= -8 && rect.y >= -8)
            assertTrue(
                "rect at ${rect.x},${rect.y} runs past the screen",
                rect.x <= gfx.width + 8 && rect.y <= gfx.height + 8,
            )
        }
    }

    @Test
    fun `every theme renders without a missing sprite or colour`() {
        val g = game()
        repeat(GameState.READY_TICKS + 300) { g.tick() }
        for (t in ThemeRegistry.all) {
            val gfx = RecordingGfx()
            GameRenderer.render(gfx, t, g, 0)
            assertTrue("${t.id} drew nothing", gfx.calls.size > 100)
        }
    }
}
