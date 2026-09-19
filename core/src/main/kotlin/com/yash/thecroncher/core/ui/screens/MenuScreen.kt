package com.yash.thecroncher.core.ui.screens

import com.yash.thecroncher.core.game.Direction
import com.yash.thecroncher.core.input.Button
import com.yash.thecroncher.core.input.InputEvent
import com.yash.thecroncher.core.ports.Align
import com.yash.thecroncher.core.ports.AudioOut
import com.yash.thecroncher.core.ports.Gfx
import com.yash.thecroncher.core.ports.Rng
import com.yash.thecroncher.core.theme.FoeCast
import com.yash.thecroncher.core.theme.SpriteId
import com.yash.thecroncher.core.theme.Theme
import com.yash.thecroncher.core.ui.ControllerProbe
import com.yash.thecroncher.core.ui.GameSettings
import com.yash.thecroncher.core.ui.ItemKind
import com.yash.thecroncher.core.ui.Layout
import com.yash.thecroncher.core.ui.MenuItem
import com.yash.thecroncher.core.ui.MenuModel
import com.yash.thecroncher.core.ui.MenuRenderer
import com.yash.thecroncher.core.ui.Screen
import com.yash.thecroncher.core.ui.Strings
import com.yash.thecroncher.core.ui.Transition

/** The title screen: PLAY, SELECT MY CAT, SETTINGS, EXIT. */
class MenuScreen(
    private val settings: GameSettings,
    private val audio: AudioOut,
    private val rng: Rng,
    private val probe: ControllerProbe? = null,
) : Screen {

    private val model = MenuModel(
        title = null,   // the title is drawn as artwork instead
        items = listOf(
            MenuItem(Strings.PLAY, ItemKind.Action {
                Transition.Push(GameScreen(settings, audio, rng))
            }),
            MenuItem(Strings.SELECT_MY_CAT, ItemKind.Action {
                Transition.Push(CatPickerScreen(settings))
            }),
            MenuItem(Strings.SETTINGS, ItemKind.Action {
                Transition.Push(SettingsScreen(settings, audio, probe))
            }),
            MenuItem(Strings.EXIT, ItemKind.Action { Transition.Exit }),
        ),
    )

    override fun handle(event: InputEvent): Transition = when (event) {
        is InputEvent.Move -> {
            when (event.direction) {
                Direction.UP -> model.moveUp()
                Direction.DOWN -> model.moveDown()
                else -> Unit
            }
            Transition.None
        }

        is InputEvent.Press -> when (event.button) {
            Button.CONFIRM -> model.activate()
            // Back on the title screen means "leave the game".
            Button.BACK -> Transition.Exit
            Button.PAUSE -> Transition.None
        }
    }

    override fun render(g: Gfx, theme: Theme, tick: Long) {
        MenuRenderer.render(g, theme, model, tick, footer = "A SELECT   B BACK")
        drawTitleArt(g, theme, tick)
    }

    /** A big wordmark with the whole cast running across underneath it. */
    private fun drawTitleArt(g: Gfx, theme: Theme, tick: Long) {
        val cx = Layout.SCREEN_WIDTH / 2
        g.drawText(Strings.TITLE, cx, Layout.MARGIN + 6, theme.menu.title, Align.CENTER, TITLE_SMALL_SCALE)
        g.drawText(Strings.TITLE_SECOND_LINE, cx, 28, theme.menu.title, Align.CENTER, TITLE_SCALE)
        for ((i, line) in Strings.TAGLINE.withIndex()) {
            g.drawText(
                line, cx, Layout.TAGLINE_Y + i * Layout.TAGLINE_LINE_SPACING,
                theme.menu.footer, Align.CENTER,
            )
        }

        // The cat runs a lap of the screen, pursued as ever.
        val period = Layout.SCREEN_WIDTH + 64
        val x = ((tick / 2) % period).toInt() - 32
        val y = 84
        val frame = ((tick / 6) % 2).toInt()
        g.drawSpriteCentred(theme.sprites.sprite(SpriteId.CAT, frame), x, y)
        for ((i, foe) in FoeCast.all.withIndex()) {
            g.drawSpriteCentred(
                theme.sprites.sprite(foe.right, ((tick / 8) % 2).toInt()),
                x - 22 - i * 18, y,
            )
        }
    }

    private companion object {
        const val TITLE_SCALE = 3
        const val TITLE_SMALL_SCALE = 2
    }
}
