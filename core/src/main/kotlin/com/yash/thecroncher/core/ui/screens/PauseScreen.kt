package com.yash.thecroncher.core.ui.screens

import com.yash.thecroncher.core.game.Direction
import com.yash.thecroncher.core.input.Button
import com.yash.thecroncher.core.input.InputEvent
import com.yash.thecroncher.core.ports.Gfx
import com.yash.thecroncher.core.theme.Theme
import com.yash.thecroncher.core.ui.ItemKind
import com.yash.thecroncher.core.ui.Layout
import com.yash.thecroncher.core.ui.MenuItem
import com.yash.thecroncher.core.ui.MenuModel
import com.yash.thecroncher.core.ui.MenuRenderer
import com.yash.thecroncher.core.ui.Screen
import com.yash.thecroncher.core.ui.Strings
import com.yash.thecroncher.core.ui.Transition

/**
 * Pause. Sits on top of the game screen, which stops ticking while this is up —
 * the state underneath is untouched, so resuming is exact.
 */
class PauseScreen(private val game: GameScreen) : Screen {

    private val model = MenuModel(
        title = Strings.PAUSED,
        items = listOf(
            MenuItem(Strings.RESUME, ItemKind.Action { Transition.Pop }),
            MenuItem(Strings.RESTART, ItemKind.Action {
                game.restart()
                Transition.Pop
            }),
            // Closes both this overlay and the game beneath it.
            MenuItem(Strings.QUIT_TO_MENU, ItemKind.Action { Transition.PopToRoot }),
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
            Button.BACK, Button.PAUSE -> Transition.Pop
        }
    }

    override fun render(g: Gfx, theme: Theme, tick: Long) {
        // The frozen game shows through behind the menu, dimmed so it reads as idle.
        game.render(g, theme, tick)
        dim(g, theme)
        MenuRenderer.render(
            g, theme, model, tick,
            footer = "A SELECT   B RESUME",
            clearBackground = false,
        )
    }

    /**
     * Scanlines knock the frozen game back so it reads as inactive, and a solid
     * panel sits behind the menu itself — without it the text fights the maze and
     * becomes genuinely hard to read on a television.
     */
    private fun dim(g: Gfx, theme: Theme) {
        for (y in 0 until Layout.SCREEN_HEIGHT step 2) {
            g.fillRect(0, y, Layout.SCREEN_WIDTH, 1, theme.menu.background)
        }
        g.fillRect(
            PANEL_X, PANEL_Y,
            Layout.SCREEN_WIDTH - PANEL_X * 2, PANEL_HEIGHT,
            theme.menu.background,
        )
    }

    private companion object {
        const val PANEL_X = 16
        const val PANEL_Y = 36
        const val PANEL_HEIGHT = 168
    }
}
