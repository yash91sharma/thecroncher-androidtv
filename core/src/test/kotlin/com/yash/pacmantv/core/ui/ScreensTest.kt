package com.yash.pacmantv.core.ui

import com.yash.pacmantv.core.game.Difficulties
import com.yash.pacmantv.core.game.Direction
import com.yash.pacmantv.core.input.Button
import com.yash.pacmantv.core.input.InputEvent
import com.yash.pacmantv.core.ports.InMemorySettingsStore
import com.yash.pacmantv.core.ports.RecordingAudioOut
import com.yash.pacmantv.core.ports.SeededRng
import com.yash.pacmantv.core.support.RecordingGfx
import com.yash.pacmantv.core.theme.ThemeRegistry
import com.yash.pacmantv.core.ui.screens.GameScreen
import com.yash.pacmantv.core.ui.screens.MenuScreen
import com.yash.pacmantv.core.ui.screens.PauseScreen
import com.yash.pacmantv.core.ui.screens.SettingsScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The screens themselves, driven exactly as a controller would drive them and
 * rendered into a recording surface. No emulator involved — this is what the `Gfx`
 * port was for.
 */
class ScreensTest {

    private val store = InMemorySettingsStore()
    private val settings = GameSettings(store)
    private val audio = RecordingAudioOut()
    private val theme = ThemeRegistry.default

    private val up = InputEvent.Move(Direction.UP)
    private val down = InputEvent.Move(Direction.DOWN)
    private val left = InputEvent.Move(Direction.LEFT)
    private val right = InputEvent.Move(Direction.RIGHT)
    private val confirm = InputEvent.Press(Button.CONFIRM)
    private val back = InputEvent.Press(Button.BACK)
    private val pause = InputEvent.Press(Button.PAUSE)

    private fun menuScreen() = MenuScreen(settings, audio, SeededRng(1))
    private fun settingsScreen() = SettingsScreen(settings, audio)
    private fun draw(screen: Screen) = RecordingGfx().also { screen.render(it, theme, 0) }

    // ------------------------------------------------------------- settings --

    @Test
    fun `changing difficulty writes through immediately`() {
        val s = settingsScreen()
        assertEquals(Difficulties.NORMAL, settings.difficulty)
        s.handle(right)
        assertEquals(Difficulties.HARD, settings.difficulty)
        s.handle(left); s.handle(left)
        assertEquals(Difficulties.EASY, settings.difficulty)
    }

    @Test
    fun `settings survive being rebuilt from the store`() {
        settingsScreen().handle(right)          // difficulty -> hard
        val reloaded = GameSettings(store)
        assertEquals(Difficulties.HARD, reloaded.difficulty)
    }

    @Test
    fun `changing theme writes through and notifies the host`() {
        var notified = 0
        val s = SettingsScreen(settings, audio, onThemeChanged = { notified++ })
        s.handle(down)                          // theme row
        s.handle(right)
        assertNotEquals(ThemeRegistry.default, settings.theme)
        assertEquals(1, notified)
    }

    @Test
    fun `turning sound off tells the audio backend as well as the store`() {
        val s = settingsScreen()
        s.handle(down); s.handle(down)          // sound row
        s.handle(left)                          // ON -> OFF
        assertEquals(false, settings.soundEnabled)
        assertEquals(false, audio.enabled)
    }

    @Test
    fun `back leaves the settings screen`() {
        assertEquals(Transition.Pop, settingsScreen().handle(back))
    }

    @Test
    fun `the settings screen lists every option`() {
        val texts = draw(settingsScreen()).textStrings()
        assertTrue(Strings.DIFFICULTY in texts)
        assertTrue(Strings.THEME in texts)
        assertTrue(Strings.SOUND in texts)
        assertTrue(Strings.BACK in texts)
    }

    @Test
    fun `the controller test only appears when a probe is available`() {
        assertTrue(Strings.CONTROLLER_TEST !in draw(settingsScreen()).textStrings())
        val withProbe = SettingsScreen(settings, audio, probe = ControllerProbe())
        assertTrue(Strings.CONTROLLER_TEST in draw(withProbe).textStrings())
    }

    // ----------------------------------------------------------- main menu --

    @Test
    fun `the main menu offers play, settings and exit`() {
        val texts = draw(menuScreen()).textStrings()
        assertTrue(Strings.PLAY in texts)
        assertTrue(Strings.SETTINGS in texts)
        assertTrue(Strings.EXIT in texts)
    }

    @Test
    fun `play starts a game and settings opens the settings`() {
        val m = menuScreen()
        assertTrue((m.handle(confirm) as Transition.Push).screen is GameScreen)

        val m2 = menuScreen()
        m2.handle(down)
        assertTrue((m2.handle(confirm) as Transition.Push).screen is SettingsScreen)
    }

    @Test
    fun `exit is offered and asks the host to quit`() {
        val m = menuScreen()
        m.handle(up)                            // wraps to EXIT
        assertEquals(Transition.Exit, m.handle(confirm))
    }

    // ---------------------------------------------------------- pause flow --

    @Test
    fun `pause opens over the game and resume returns to it`() {
        val game = GameScreen(settings, audio, SeededRng(2))
        val stack = ScreenStack(game)

        stack.handle(pause)
        assertTrue(stack.current is PauseScreen)
        assertEquals(2, stack.depth)

        stack.handle(confirm)                   // RESUME
        assertSame(game, stack.current)
    }

    @Test
    fun `the paused game does not advance`() {
        val game = GameScreen(settings, audio, SeededRng(2))
        val stack = ScreenStack(game)
        repeat(200) { stack.update(it.toLong()) }
        val ticksWhilePlaying = game.state.phaseTicks

        stack.handle(pause)
        repeat(200) { stack.update(it.toLong()) }
        assertEquals("the game must be frozen while paused", ticksWhilePlaying, game.state.phaseTicks)
    }

    @Test
    fun `quit to menu closes both the pause overlay and the game`() {
        val menu = menuScreen()
        val stack = ScreenStack(menu)
        stack.handle(confirm)                   // PLAY
        stack.handle(pause)
        assertEquals(3, stack.depth)

        stack.handle(down); stack.handle(down)  // QUIT TO MENU
        stack.handle(confirm)

        assertSame("should be back at the title screen", menu, stack.current)
        assertEquals(1, stack.depth)
    }

    @Test
    fun `restart from the pause menu resets the score`() {
        val game = GameScreen(settings, audio, SeededRng(2))
        val stack = ScreenStack(game)
        repeat(400) { stack.update(it.toLong()) }

        stack.handle(pause)
        stack.handle(down)                      // RESTART
        stack.handle(confirm)
        assertEquals(0, game.state.score)
        assertSame(game, stack.current)
    }

    @Test
    fun `the pause overlay draws the game behind it rather than blanking it`() {
        val game = GameScreen(settings, audio, SeededRng(2))
        val paused = PauseScreen(game)
        val gfx = draw(paused)
        assertTrue("the maze should still be visible behind the menu", gfx.sprites.size > 100)
        assertTrue(Strings.PAUSED in gfx.textStrings())
    }

    // -------------------------------------------------------- high scores --

    @Test
    fun `the high score is kept separately for each difficulty`() {
        settings.difficulty = Difficulties.EASY
        settings.highScore = 5000
        settings.difficulty = Difficulties.HARD
        assertEquals("a hard run must not inherit an easy high score", 0, settings.highScore)
        settings.difficulty = Difficulties.EASY
        assertEquals(5000, settings.highScore)
    }

    @Test
    fun `the high score never goes down`() {
        settings.highScore = 8000
        settings.highScore = 100
        assertEquals(8000, settings.highScore)
    }
}
