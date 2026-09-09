package com.yash.pacmantv.core.ui.screens

import com.yash.pacmantv.core.game.Difficulties
import com.yash.pacmantv.core.game.Direction
import com.yash.pacmantv.core.input.Button
import com.yash.pacmantv.core.input.InputEvent
import com.yash.pacmantv.core.ports.AudioOut
import com.yash.pacmantv.core.ports.Gfx
import com.yash.pacmantv.core.theme.Theme
import com.yash.pacmantv.core.theme.ThemeRegistry
import com.yash.pacmantv.core.ui.ControllerProbe
import com.yash.pacmantv.core.ui.GameSettings
import com.yash.pacmantv.core.ui.ItemKind
import com.yash.pacmantv.core.ui.MenuItem
import com.yash.pacmantv.core.ui.MenuModel
import com.yash.pacmantv.core.ui.MenuRenderer
import com.yash.pacmantv.core.ui.Screen
import com.yash.pacmantv.core.ui.Strings
import com.yash.pacmantv.core.ui.Transition

/**
 * Difficulty, theme, sound, and the controller test.
 *
 * Every entry is a [MenuItem] reading and writing [GameSettings] directly, so
 * changes take effect and persist the instant they are made — there is no "apply"
 * step to forget, and no copy of the settings to get out of step.
 */
class SettingsScreen(
    private val settings: GameSettings,
    private val audio: AudioOut,
    private val onThemeChanged: () -> Unit = {},
    private val probe: ControllerProbe? = null,
) : Screen {

    private val model = MenuModel(
        title = Strings.SETTINGS,
        items = buildList {
            add(
                MenuItem(
                    Strings.DIFFICULTY,
                    ItemKind.Choice(
                        options = Difficulties.all.map { it.displayName.uppercase() },
                        getIndex = { Difficulties.all.indexOf(settings.difficulty).coerceAtLeast(0) },
                        setIndex = { settings.difficulty = Difficulties.all[it] },
                    ),
                ),
            )
            add(
                MenuItem(
                    Strings.THEME,
                    ItemKind.Choice(
                        options = ThemeRegistry.all.map { it.displayName.uppercase() },
                        getIndex = { ThemeRegistry.all.indexOf(settings.theme).coerceAtLeast(0) },
                        setIndex = {
                            settings.theme = ThemeRegistry.all[it]
                            onThemeChanged()
                        },
                    ),
                ),
            )
            add(
                MenuItem(
                    Strings.SOUND,
                    ItemKind.Choice(
                        options = listOf(Strings.OFF, Strings.ON),
                        getIndex = { if (settings.soundEnabled) 1 else 0 },
                        setIndex = {
                            settings.soundEnabled = it == 1
                            audio.setEnabled(it == 1)
                        },
                    ),
                ),
            )
            if (probe != null) {
                add(
                    MenuItem(Strings.CONTROLLER_TEST, ItemKind.Action {
                        Transition.Push(ControllerTestScreen(probe))
                    }),
                )
            }
            add(MenuItem(Strings.BACK, ItemKind.Action { Transition.Pop }))
        },
    )

    override fun handle(event: InputEvent): Transition = when (event) {
        is InputEvent.Move -> {
            when (event.direction) {
                Direction.UP -> model.moveUp()
                Direction.DOWN -> model.moveDown()
                Direction.LEFT -> model.adjust(-1)
                Direction.RIGHT -> model.adjust(1)
            }
            Transition.None
        }

        is InputEvent.Press -> when (event.button) {
            Button.CONFIRM -> model.activate()
            Button.BACK -> Transition.Pop
            Button.PAUSE -> Transition.None
        }
    }

    override fun render(g: Gfx, theme: Theme, tick: Long) {
        MenuRenderer.render(g, theme, model, tick, footer = "LEFT/RIGHT CHANGE   B BACK")
    }
}
