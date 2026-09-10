package com.yash.thecroncher.core.ui

import com.yash.thecroncher.core.game.Difficulties
import com.yash.thecroncher.core.game.Direction
import com.yash.thecroncher.core.game.GamePhase
import com.yash.thecroncher.core.game.GameState
import com.yash.thecroncher.core.game.Maze
import com.yash.thecroncher.core.ports.SeededRng
import com.yash.thecroncher.core.support.RecordingGfx
import com.yash.thecroncher.core.theme.FoeCast
import com.yash.thecroncher.core.theme.SpriteId
import com.yash.thecroncher.core.theme.ThemeRegistry
import com.yash.thecroncher.core.theme.TreatArt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Rendering tests without an emulator — the dividend from putting drawing behind
 * the [com.yash.thecroncher.core.ports.Gfx] port. These catch the class of bug that
 * is otherwise only visible by squinting at a screenshot of a television.
 */
class GameRendererTest {

    private val theme = ThemeRegistry.default

    private fun game() = GameState(
        maze = Maze.loadDefault(),
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
    fun `every treat in the maze is drawn at the start of a level`() {
        val drawn = TreatArt.shapes.sumOf { draw(game(), 0).spriteCount(it) }
        assertEquals(game().maze.dotCount, drawn)
    }

    @Test
    fun `the treats are not all the same shape`() {
        // The maze is scattered with different biscuits; if the mix ever collapses
        // to one shape the board goes back to being a field of identical dots.
        val gfx = draw(game(), 0)
        val used = TreatArt.shapes.filter { gfx.spriteCount(it) > 0 }
        assertEquals("every treat shape should appear somewhere", TreatArt.shapes.size, used.size)
    }

    @Test
    fun `a treat keeps the same shape from one frame to the next`() {
        val g = game()
        val first = TreatArt.shapes.map { draw(g, 0).spriteCount(it) }
        val later = TreatArt.shapes.map { draw(g, 97).spriteCount(it) }
        assertEquals("treats must not shuffle their shapes as the game runs", first, later)
    }

    @Test
    fun `catnip is drawn, and blinks rather than sitting still`() {
        // Two frames a fifth of a second apart: one must show them, one must not.
        val g = game()
        val counts = (0L until 48L).map { draw(g, it).spriteCount(SpriteId.CATNIP) }
        assertTrue("catnip is never drawn at all", counts.any { it == 4 })
        assertTrue("catnip never blinks off", counts.any { it == 0 })
        assertTrue("expected only 0 or 4 catnip, saw $counts", counts.all { it == 0 || it == 4 })
    }

    @Test
    fun `an eaten treat stops being drawn`() {
        val g = game()
        repeat(GameState.READY_TICKS) { g.tick() }
        val before = TreatArt.shapes.sumOf { draw(g, 0).spriteCount(it) }

        val dot = g.maze.pelletPositions.first { g.maze.tileAt(it.x, it.y) == com.yash.thecroncher.core.game.Tile.DOT }
        g.croncher.placeAtTileCentre(dot, Direction.LEFT)
        g.tick()

        val after = TreatArt.shapes.sumOf { draw(g, 0).spriteCount(it) }
        assertTrue("eating a treat should remove it from the screen", after < before)
    }

    @Test
    fun `the cat is drawn, facing the player whichever way he runs`() {
        val g = game()
        val cat = (0 until SpriteId.CAT.frameCount)
            .map { theme.sprites.sprite(SpriteId.CAT, it) }
            .toSet()
        for (dir in Direction.entries) {
            g.requestDirection(dir)
            repeat(4) { g.tick() }
            assertTrue(
                "the cat vanished when running $dir",
                draw(g, 0).sprites.any { it.sprite in cat },
            )
        }
    }

    @Test
    fun `each foe is drawn as itself`() {
        // On a fresh board the dog is out and the other three wait in the house,
        // and all four are on screen. Later in a life they may be blue, puffed, or
        // hidden behind the faint animation.
        val g = game()
        val gfx = draw(g, 0)
        for (foe in FoeCast.all) {
            val art = listOf(foe.right, foe.left, foe.up, foe.down)
                .flatMap { id -> (0 until id.frameCount).map { theme.sprites.sprite(id, it) } }
                .toSet()
            assertTrue("${foe.id} never appears on the board", gfx.sprites.any { it.sprite in art })
        }
    }

    @Test
    fun `the cast is drawn in its own colours rather than tinted`() {
        // A grey tabby and a brown dog cannot be one shape under two tints, so
        // nothing on the playfield asks Gfx to flatten a sprite to a single colour.
        val gfx = draw(game(), 0)
        assertTrue("a sprite was drawn tinted", gfx.sprites.all { it.tint == null })
    }

    @Test
    fun `lives are shown as one cat per life in reserve`() {
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
