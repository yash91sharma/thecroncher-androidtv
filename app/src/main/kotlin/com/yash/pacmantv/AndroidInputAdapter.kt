package com.yash.pacmantv

import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import com.yash.pacmantv.core.input.InputEvent
import com.yash.pacmantv.core.input.InputMapper
import com.yash.pacmantv.core.ui.ControllerProbe

/**
 * Turns Android's key and motion events into the game's own input events.
 *
 * Both paths are handled deliberately: a Stadia pad in Bluetooth mode may report
 * its D-pad as key codes, as a hat switch on the axes, or as both at once
 * depending on firmware. [InputMapper] resolves the overlap so a single push
 * always counts once.
 */
class AndroidInputAdapter(
    private val mapper: InputMapper,
    private val probe: ControllerProbe,
    private val onEvent: (InputEvent) -> Unit,
) {

    fun onKey(event: KeyEvent): Boolean {
        val down = event.action == KeyEvent.ACTION_DOWN

        // Android synthesises D-pad key events from hat axes on some pads; those
        // arrive tagged as joystick and would otherwise be counted twice.
        probe.recordKey(event.keyCode, KeyEvent.keyCodeToString(event.keyCode)
            .removePrefix("KEYCODE_"), down)
        probe.deviceName = event.device?.name ?: probe.deviceName

        // Auto-repeat would fire a menu item over and over from one long press.
        if (down && event.repeatCount > 0) return true

        mapper.onKey(event.keyCode, down)?.let(onEvent)
        return true
    }

    fun onMotion(event: MotionEvent): Boolean {
        if (!event.isFromJoystick()) return false

        val hatX = event.getAxisValue(MotionEvent.AXIS_HAT_X)
        val hatY = event.getAxisValue(MotionEvent.AXIS_HAT_Y)
        val stickX = event.getAxisValue(MotionEvent.AXIS_X)
        val stickY = event.getAxisValue(MotionEvent.AXIS_Y)

        probe.hatX = hatX
        probe.hatY = hatY
        probe.stickX = stickX
        probe.stickY = stickY
        probe.rightStickX = event.getAxisValue(MotionEvent.AXIS_Z)
        probe.rightStickY = event.getAxisValue(MotionEvent.AXIS_RZ)
        probe.triggerLeft = event.getAxisValue(MotionEvent.AXIS_LTRIGGER)
        probe.triggerRight = event.getAxisValue(MotionEvent.AXIS_RTRIGGER)
        probe.deviceName = event.device?.name ?: probe.deviceName

        mapper.onAxes(hatX, hatY, stickX, stickY)?.let(onEvent)
        return true
    }

    private fun MotionEvent.isFromJoystick(): Boolean =
        source and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK ||
            source and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD ||
            source and InputDevice.SOURCE_DPAD == InputDevice.SOURCE_DPAD
}
