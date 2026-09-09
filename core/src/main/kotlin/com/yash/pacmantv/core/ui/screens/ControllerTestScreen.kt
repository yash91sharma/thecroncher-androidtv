package com.yash.pacmantv.core.ui.screens

import com.yash.pacmantv.core.input.Button
import com.yash.pacmantv.core.input.InputEvent
import com.yash.pacmantv.core.ports.Align
import com.yash.pacmantv.core.ports.Gfx
import com.yash.pacmantv.core.theme.Theme
import com.yash.pacmantv.core.ui.ControllerProbe
import com.yash.pacmantv.core.ui.Layout
import com.yash.pacmantv.core.ui.Screen
import com.yash.pacmantv.core.ui.Transition
import kotlin.math.roundToInt

/**
 * Shows exactly what the controller is sending, live.
 *
 * This is the escape hatch for the Stadia pad: if a button turns out to report an
 * unexpected code on a particular unit, or the D-pad arrives as hat axes rather
 * than key codes, it is visible here on the television in a second — rather than
 * needing a logcat session against a device in the living room.
 */
class ControllerTestScreen(private val probe: ControllerProbe) : Screen {

    // Deliberately not Button.BACK: the whole point is to see every button, so
    // leaving is bound to a long-ish sequence rather than a single press.
    private var backHeldFrames = 0

    override fun handle(event: InputEvent): Transition {
        // Everything is swallowed and displayed; only a repeated BACK exits.
        if (event is InputEvent.Press && event.button == Button.BACK) {
            backHeldFrames++
            if (backHeldFrames >= 2) return Transition.Pop
        }
        return Transition.None
    }

    override fun render(g: Gfx, theme: Theme, tick: Long) {
        g.clear(theme.menu.background)
        val cx = Layout.SCREEN_WIDTH / 2

        g.drawText("CONTROLLER TEST", cx, 24, theme.menu.title, Align.CENTER)
        g.drawText(probe.deviceName.take(26).uppercase(), cx, 40, theme.menu.item, Align.CENTER)

        var y = 68
        fun line(label: String, value: String) {
            g.drawText(label, 24, y, theme.menu.item)
            g.drawText(value, Layout.SCREEN_WIDTH - 24, y, theme.menu.itemSelected, Align.RIGHT)
            y += 14
        }

        line("LAST KEY", if (probe.lastKeyCode < 0) "-" else probe.lastKeyName)
        line("KEY CODE", if (probe.lastKeyCode < 0) "-" else probe.lastKeyCode.toString())
        line("STATE", if (probe.lastKeyDown) "DOWN" else "UP")

        y += 6
        line("HAT X", axis(probe.hatX))
        line("HAT Y", axis(probe.hatY))
        line("STICK X", axis(probe.stickX))
        line("STICK Y", axis(probe.stickY))
        line("R-STICK X", axis(probe.rightStickX))
        line("R-STICK Y", axis(probe.rightStickY))
        line("TRIGGER L", axis(probe.triggerLeft))
        line("TRIGGER R", axis(probe.triggerRight))

        g.drawText(
            "PRESS B TWICE TO GO BACK",
            cx, Layout.SCREEN_HEIGHT - 24, theme.menu.footer, Align.CENTER,
        )
    }

    /** Two decimal places without pulling in any formatting machinery. */
    private fun axis(value: Float): String {
        val hundredths = (value * 100).roundToInt()
        val sign = if (hundredths < 0) "-" else ""
        val magnitude = kotlin.math.abs(hundredths)
        return "$sign${magnitude / 100}.${(magnitude % 100).toString().padStart(2, '0')}"
    }
}
