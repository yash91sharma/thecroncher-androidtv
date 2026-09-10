package com.yash.thecroncher.core.input

import com.yash.thecroncher.core.game.Direction
import kotlin.math.abs

/** The discrete buttons the game cares about. */
enum class Button { CONFIRM, BACK, PAUSE }

/** Something the player did, already normalised away from any particular pad. */
sealed interface InputEvent {
    data class Move(val direction: Direction) : InputEvent
    data class Press(val button: Button) : InputEvent
}

/**
 * Android key codes, copied here as plain integers so `:core` stays free of any
 * Android dependency and the mapping can be unit-tested on the JVM.
 */
object KeyCodes {
    const val BACK = 4
    const val DPAD_UP = 19
    const val DPAD_DOWN = 20
    const val DPAD_LEFT = 21
    const val DPAD_RIGHT = 22
    const val DPAD_CENTER = 23
    const val ENTER = 66
    const val MENU = 82
    const val BUTTON_A = 96
    const val BUTTON_B = 97
    const val BUTTON_X = 99
    const val BUTTON_Y = 100
    const val BUTTON_L1 = 102
    const val BUTTON_R1 = 103
    const val BUTTON_START = 108
    const val BUTTON_SELECT = 109
    const val BUTTON_MODE = 110
}

/**
 * Turns key codes and joystick axes into [InputEvent]s.
 *
 * The Stadia controller in Bluetooth mode is the reason this is stateful rather
 * than a lookup: depending on firmware its D-pad arrives as key codes, as a hat
 * switch, or as both at once. The mapper tracks what is currently held from each
 * source and only emits when the *resolved* direction actually changes, so a pad
 * that reports a push twice still moves the croncher once.
 */
class InputMapper {

    private var keyDirection: Direction? = null
    private var axisDirection: Direction? = null
    private var lastEmitted: Direction? = null

    /** The direction currently being held down, from whichever source. */
    val heldDirection: Direction?
        get() = axisDirection ?: keyDirection

    fun reset() {
        keyDirection = null
        axisDirection = null
        lastEmitted = null
    }

    fun onKey(keyCode: Int, down: Boolean): InputEvent? {
        directionForKey(keyCode)?.let { dir ->
            if (down) {
                keyDirection = dir
            } else if (keyDirection == dir) {
                keyDirection = null
            }
            return resolveDirection()
        }

        if (!down) return null      // buttons act on press
        val button = when (keyCode) {
            KeyCodes.BUTTON_A, KeyCodes.DPAD_CENTER, KeyCodes.ENTER -> Button.CONFIRM
            KeyCodes.BUTTON_B, KeyCodes.BACK -> Button.BACK
            KeyCodes.BUTTON_START, KeyCodes.MENU -> Button.PAUSE
            else -> return null
        }
        return InputEvent.Press(button)
    }

    /**
     * Feeds the analogue state. [hatX]/[hatY] are the D-pad-as-hat-switch axes;
     * [stickX]/[stickY] the left stick.
     */
    fun onAxes(hatX: Float, hatY: Float, stickX: Float, stickY: Float): InputEvent? {
        axisDirection = directionForHat(hatX, hatY) ?: directionForStick(stickX, stickY)
        return resolveDirection()
    }

    /** Emits only when the resolved direction changes, which is what stops double-fire. */
    private fun resolveDirection(): InputEvent? {
        val resolved = heldDirection
        if (resolved == lastEmitted) return null
        lastEmitted = resolved
        return resolved?.let { InputEvent.Move(it) }
    }

    private fun directionForKey(keyCode: Int): Direction? = when (keyCode) {
        KeyCodes.DPAD_UP -> Direction.UP
        KeyCodes.DPAD_DOWN -> Direction.DOWN
        KeyCodes.DPAD_LEFT -> Direction.LEFT
        KeyCodes.DPAD_RIGHT -> Direction.RIGHT
        else -> null
    }

    private fun directionForHat(hatX: Float, hatY: Float): Direction? = when {
        hatX <= -HAT_THRESHOLD -> Direction.LEFT
        hatX >= HAT_THRESHOLD -> Direction.RIGHT
        hatY <= -HAT_THRESHOLD -> Direction.UP
        hatY >= HAT_THRESHOLD -> Direction.DOWN
        else -> null
    }

    /** The dominant axis wins, so a diagonal push does not flicker between two. */
    private fun directionForStick(x: Float, y: Float): Direction? {
        val ax = abs(x)
        val ay = abs(y)
        if (ax < DEADZONE && ay < DEADZONE) return null
        return if (ax >= ay) {
            if (x < 0) Direction.LEFT else Direction.RIGHT
        } else {
            if (y < 0) Direction.UP else Direction.DOWN
        }
    }

    companion object {
        /** Sticks rest slightly off centre, so ignore anything smaller than this. */
        const val DEADZONE = 0.5f

        const val HAT_THRESHOLD = 0.5f
    }
}
