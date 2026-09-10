package com.yash.thecroncher.core.ui.screens

import com.yash.thecroncher.core.game.Difficulties
import com.yash.thecroncher.core.game.Direction
import com.yash.thecroncher.core.input.Button
import com.yash.thecroncher.core.input.InputEvent
import com.yash.thecroncher.core.ports.AudioOut
import com.yash.thecroncher.core.ports.Gfx
import com.yash.thecroncher.core.theme.Theme
import com.yash.thecroncher.core.ui.ControllerProbe
import com.yash.thecroncher.core.ui.GameSettings
import com.yash.thecroncher.core.ui.ItemKind
import com.yash.thecroncher.core.ui.MenuItem
import com.yash.thecroncher.core.ui.MenuModel
import com.yash.thecroncher.core.ui.MenuRenderer
import com.yash.thecroncher.core.ui.Screen
import com.yash.thecroncher.core.ui.Strings
import com.yash.thecroncher.core.ui.Transition

/**
 * Difficulty, sound, and the controller test.
 *
 * Every entry is a [MenuItem] reading and writing [GameSettings] directly, so
 * changes take effect and persist the instant they are made — there is no "apply"
 * step to forget, and no copy of the settings to get out of step.
 */
class SettingsScreen(
    private val settings: GameSettings,
    private val audio: AudioOut,
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
