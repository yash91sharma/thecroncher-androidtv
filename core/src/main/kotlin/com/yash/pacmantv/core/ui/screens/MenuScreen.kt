package com.yash.pacmantv.core.ui.screens

import com.yash.pacmantv.core.game.Direction
import com.yash.pacmantv.core.input.Button
import com.yash.pacmantv.core.input.InputEvent
import com.yash.pacmantv.core.ports.Align
import com.yash.pacmantv.core.ports.AudioOut
import com.yash.pacmantv.core.ports.Gfx
import com.yash.pacmantv.core.ports.Rng
import com.yash.pacmantv.core.ui.ControllerProbe
import com.yash.pacmantv.core.ui.GameSettings
import com.yash.pacmantv.core.ui.ItemKind
import com.yash.pacmantv.core.ui.Layout
import com.yash.pacmantv.core.ui.MenuItem
import com.yash.pacmantv.core.ui.MenuModel
import com.yash.pacmantv.core.ui.MenuRenderer
import com.yash.pacmantv.core.ui.Screen
import com.yash.pacmantv.core.ui.Strings
import com.yash.pacmantv.core.ui.Transition
import com.yash.pacmantv.core.theme.SpriteId
import com.yash.pacmantv.core.theme.Theme

/** The title screen: PLAY, SETTINGS, EXIT. */
class MenuScreen(
    private val settings: GameSettings,
    private val audio: AudioOut,
    private val rng: Rng,
    private val onThemeChanged: () -> Unit = {},
    private val probe: ControllerProbe? = null,
) : Screen {

    private val model = MenuModel(
        title = null,   // the title is drawn as artwork instead
        items = listOf(
            MenuItem(Strings.PLAY, ItemKind.Action {
                Transition.Push(GameScreen(settings, audio, rng))
            }),
            MenuItem(Strings.SETTINGS, ItemKind.Action {
                Transition.Push(SettingsScreen(settings, audio, onThemeChanged, probe))
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

    /** A big wordmark with the cast chasing across underneath it. */
    private fun drawTitleArt(g: Gfx, theme: Theme, tick: Long) {
        val cx = Layout.SCREEN_WIDTH / 2
        g.drawText(Strings.TITLE, cx, 40, theme.menu.title, Align.CENTER)
        g.drawText("TV", cx, 54, theme.menu.title, Align.CENTER)

        // Pac-Man runs a lap of the screen, pursued as ever.
        val period = 320
        val x = ((tick / 2) % period).toInt() - 32
        val y = 76
        val frame = MOUTH_CYCLE[((tick / 4) % MOUTH_CYCLE.size).toInt()]
        g.drawSpriteCentred(
            theme.sprites.sprite(SpriteId.PACMAN_RIGHT, frame), x, y, theme.entities.pacman,
        )
        for (i in 0 until 4) {
            g.drawSpriteCentred(
                theme.sprites.sprite(SpriteId.GHOST_RIGHT, ((tick / 8) % 2).toInt()),
                x - 22 - i * 18, y, theme.ghostColour(i),
            )
        }
    }

    private companion object {
        val MOUTH_CYCLE = intArrayOf(0, 1, 2, 1)
    }
}
