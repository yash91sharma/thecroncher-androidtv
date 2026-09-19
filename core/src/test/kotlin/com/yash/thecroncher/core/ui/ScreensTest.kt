package com.yash.thecroncher.core.ui

import com.yash.thecroncher.core.game.Difficulties
import com.yash.thecroncher.core.game.Direction
import com.yash.thecroncher.core.input.Button
import com.yash.thecroncher.core.input.InputEvent
import com.yash.thecroncher.core.ports.Align
import com.yash.thecroncher.core.ports.InMemorySettingsStore
import com.yash.thecroncher.core.ports.RecordingAudioOut
import com.yash.thecroncher.core.ports.SeededRng
import com.yash.thecroncher.core.support.RecordingGfx
import com.yash.thecroncher.core.theme.SpriteId
import com.yash.thecroncher.core.theme.ThemeRegistry
import com.yash.thecroncher.core.theme.cats.CatRegistry
import com.yash.thecroncher.core.theme.cats.OrangeTabby
import com.yash.thecroncher.core.theme.cats.Siamese
import com.yash.thecroncher.core.ui.screens.CatPickerScreen
import com.yash.thecroncher.core.ui.screens.GameScreen
import com.yash.thecroncher.core.ui.screens.MenuScreen
import com.yash.thecroncher.core.ui.screens.PauseScreen
import com.yash.thecroncher.core.ui.screens.SettingsScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    private fun picker() = CatPickerScreen(settings)
    private fun draw(screen: Screen) = RecordingGfx().also { screen.render(it, theme, 0) }

    private val CatBreedSize = com.yash.thecroncher.core.theme.cats.CatBreed.SIZE

    // --------------------------------------------------------- screen awake --

    @Test
    fun `only the game screen holds the television awake`() {
        val game = GameScreen(settings, audio, SeededRng(1))
        assertTrue(game.keepsScreenAwake)
        assertFalse(menuScreen().keepsScreenAwake)
        assertFalse(settingsScreen().keepsScreenAwake)
        assertFalse(picker().keepsScreenAwake)
        assertFalse(PauseScreen(game).keepsScreenAwake)
    }

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
    fun `the game has a single look and offers no theme picker`() {
        // One theme, and the game is named after it — so there is no row to cycle
        // and nothing for the settings screen to write.
        assertEquals(1, ThemeRegistry.all.size)
        assertEquals(ThemeRegistry.default, settings.theme)
        assertTrue("THEME" !in draw(settingsScreen()).textStrings())
    }

    @Test
    fun `turning sound off tells the audio backend as well as the store`() {
        val s = settingsScreen()
        s.handle(down)                          // sound row
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
        assertTrue(Strings.SOUND in texts)
        assertTrue(Strings.BACK in texts)
    }

    @Test
    fun `the controller test only appears when a probe is available`() {
        assertTrue(Strings.CONTROLLER_TEST !in draw(settingsScreen()).textStrings())
        val withProbe = SettingsScreen(settings, audio, ControllerProbe())
        assertTrue(Strings.CONTROLLER_TEST in draw(withProbe).textStrings())
    }

    // ----------------------------------------------------------- main menu --

    @Test
    fun `the main menu offers play, select my cat, settings and exit, in that order`() {
        val texts = draw(menuScreen()).textStrings()
        val items = texts.filter { it in listOf(Strings.PLAY, Strings.SELECT_MY_CAT, Strings.SETTINGS, Strings.EXIT) }
        assertEquals(listOf(Strings.PLAY, Strings.SELECT_MY_CAT, Strings.SETTINGS, Strings.EXIT), items)
        assertEquals("SELECT MY CAT", Strings.SELECT_MY_CAT)
    }

    @Test
    fun `the title screen draws every line of the tagline, centred, one under the other`() {
        val tagline = draw(menuScreen()).texts.filter { it.text in Strings.TAGLINE }
        assertEquals(Strings.TAGLINE, tagline.map { it.text })
        for ((i, line) in tagline.withIndex()) {
            assertEquals(Align.CENTER, line.align)
            assertEquals(Layout.SCREEN_WIDTH / 2, line.x)
            assertEquals(Layout.TAGLINE_Y + i * Layout.TAGLINE_LINE_SPACING, line.y)
        }
    }

    @Test
    fun `every tagline line fits inside the safe area`() {
        for (line in Strings.TAGLINE) {
            assertTrue(
                "'$line' is ${Font.measure(line)}px wide",
                Font.measure(line) <= Layout.SCREEN_WIDTH - 2 * Layout.MARGIN,
            )
        }
    }

    @Test
    fun `play starts a game, select my cat opens the picker, settings opens the settings`() {
        val m = menuScreen()
        assertTrue((m.handle(confirm) as Transition.Push).screen is GameScreen)

        val m2 = menuScreen()
        m2.handle(down)
        assertTrue((m2.handle(confirm) as Transition.Push).screen is CatPickerScreen)

        val m3 = menuScreen()
        m3.handle(down); m3.handle(down)
        assertTrue((m3.handle(confirm) as Transition.Push).screen is SettingsScreen)
    }

    // ---------------------------------------------------------- cat picker --

    @Test
    fun `the picker opens on the cat currently chosen`() {
        assertEquals(CatRegistry.default, picker().highlighted)
        settings.cat = Siamese
        assertEquals(Siamese, picker().highlighted)
    }

    @Test
    fun `left and right walk the roster in order and wrap at the ends`() {
        val p = picker()
        p.handle(right)
        assertEquals(CatRegistry.all[1], p.highlighted)
        p.handle(left); p.handle(left)
        assertEquals("left from the first cat wraps to the last", CatRegistry.all.last(), p.highlighted)
        p.handle(right)
        assertEquals(CatRegistry.all.first(), p.highlighted)
    }

    @Test
    fun `up and down move a whole row of three`() {
        val p = picker()
        p.handle(down)
        assertEquals(CatRegistry.all[3], p.highlighted)
        p.handle(down)
        assertEquals("down from the bottom row wraps to the top", CatRegistry.all[0], p.highlighted)
        p.handle(right); p.handle(up)
        assertEquals(CatRegistry.all[4], p.highlighted)
    }

    @Test
    fun `confirm keeps the highlighted cat and closes the picker`() {
        val p = picker()
        p.handle(right)
        assertEquals("nothing is written until A is pressed", CatRegistry.default, settings.cat)
        assertEquals(Transition.Pop, p.handle(confirm))
        assertEquals(OrangeTabby, settings.cat)
    }

    @Test
    fun `back closes the picker without changing the cat`() {
        val p = picker()
        p.handle(right); p.handle(down)
        assertEquals(Transition.Pop, p.handle(back))
        assertEquals(CatRegistry.default, settings.cat)
    }

    @Test
    fun `the picker shows every face, twice life size, in two rows of three`() {
        val gfx = draw(picker())
        assertTrue(Strings.SELECT_MY_CAT in gfx.textStrings())

        val faces = gfx.sprites
        assertEquals(CatRegistry.all.size, faces.size)
        for (face in faces) {
            assertEquals(CatBreedSize * Layout.CAT_PICKER_SCALE, face.sprite.width)
        }
        val columns = faces.map { it.x }.distinct()
        val rows = faces.map { it.y }.distinct()
        assertEquals(3, columns.size)
        assertEquals(2, rows.size)
        // The first three read left to right on the top row.
        assertEquals(columns.sorted(), faces.take(3).map { it.x })
        assertEquals(rows.min(), faces[0].y)
        assertEquals(rows.max(), faces[3].y)
    }

    @Test
    fun `the picker draws each cat in its own colours, whichever is chosen`() {
        settings.cat = Siamese
        val faces = draw(picker()).sprites
        for ((i, cat) in CatRegistry.all.withIndex()) {
            assertTrue("${cat.id} is not drawn as itself", cat.fur in faces[i].sprite.pixels)
        }
    }

    @Test
    fun `the highlighted cat is marked with the menu cursor rule`() {
        val p = picker()
        p.handle(right)
        val gfx = draw(p)
        val face = gfx.sprites[1]
        val centreX = face.x + face.sprite.width / 2
        val rule = gfx.rects.filter { it.color == theme.menu.cursor }
        assertTrue("no cursor rule drawn", rule.isNotEmpty())
        assertTrue(
            "the rule is not under the highlighted cat",
            rule.any { centreX in it.x until it.x + it.w && it.y > face.y },
        )
    }

    @Test
    fun `the picker stays inside the safe area`() {
        val gfx = draw(picker())
        for (face in gfx.sprites) {
            assertTrue(face.x >= Layout.MARGIN)
            assertTrue(face.y >= Layout.MARGIN)
            assertTrue(face.x + face.sprite.width <= Layout.SCREEN_WIDTH - Layout.MARGIN)
            assertTrue(face.y + face.sprite.height <= Layout.SCREEN_HEIGHT - Layout.MARGIN)
        }
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

    // ------------------------------------------------------------- the cat --

    @Test
    fun `a fresh install plays as the grey tabby`() {
        assertEquals(CatRegistry.default, settings.cat)
        assertEquals("grey-tabby", settings.cat.id)
    }

    @Test
    fun `the chosen cat is written through and survives a reload`() {
        settings.cat = OrangeTabby
        assertEquals(OrangeTabby, GameSettings(store).cat)
    }

    @Test
    fun `an unknown stored cat falls back to the default rather than failing`() {
        store.putString("cat", "no-such-cat")
        assertEquals(CatRegistry.default, settings.cat)
    }

    @Test
    fun `the theme every screen draws with wears the chosen cat`() {
        fun fur() = settings.theme.sprites.sprite(SpriteId.CAT, 0).pixels.toSet()
        assertTrue(CatRegistry.default.fur in fur())
        settings.cat = OrangeTabby
        assertTrue(OrangeTabby.fur in fur())
        assertTrue(CatRegistry.default.fur !in fur())
        assertEquals("only the cat changes", ThemeRegistry.default.id, settings.theme.id)
    }

    @Test
    fun `asking for the theme twice for the same cat gives the same object`() {
        // It is asked for every frame; rebuilding the sprite cache each time
        // would throw away every rasterised sprite sixty times a second.
        assertSame(settings.theme, settings.theme)
        settings.cat = OrangeTabby
        assertSame(settings.theme, settings.theme)
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
