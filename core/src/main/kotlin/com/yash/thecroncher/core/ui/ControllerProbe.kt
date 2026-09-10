package com.yash.thecroncher.core.ui

/**
 * Live, raw controller state for the Controller Test screen.
 *
 * This exists because the Stadia pad's Bluetooth firmware is not entirely
 * predictable: if a button reports something unexpected on a particular unit, this
 * screen shows it on the television immediately, rather than requiring a logcat
 * session against a device in another room.
 */
class ControllerProbe {
    var deviceName: String = "-"
    var lastKeyCode: Int = -1
    var lastKeyName: String = "-"
    var lastKeyDown: Boolean = false

    var hatX: Float = 0f
    var hatY: Float = 0f
    var stickX: Float = 0f
    var stickY: Float = 0f
    var rightStickX: Float = 0f
    var rightStickY: Float = 0f
    var triggerLeft: Float = 0f
    var triggerRight: Float = 0f

    fun recordKey(code: Int, name: String, down: Boolean) {
        lastKeyCode = code
        lastKeyName = name
        lastKeyDown = down
    }
}
