package com.yash.thecroncher.core.input

import com.yash.thecroncher.core.game.Direction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Normalises everything a controller might send into a handful of intents.
 *
 * The reason this is more than a lookup table: a Stadia pad in Bluetooth mode may
 * report its D-pad as discrete key codes *or* as a hat switch on the axes,
 * depending on firmware and how Android resolves the device's key layout. Both
 * paths are handled, plus the left stick — and crucially, when two of them report
 * the same push it must count once.
 */
class InputMapperTest {

    private val mapper = InputMapper()

    private fun key(code: Int) = mapper.onKey(code, down = true)
    private fun release(code: Int) = mapper.onKey(code, down = false)
    private fun axes(hatX: Float = 0f, hatY: Float = 0f, stickX: Float = 0f, stickY: Float = 0f) =
        mapper.onAxes(hatX, hatY, stickX, stickY)

    // ------------------------------------------------------------ d-pad keys --

    @Test
    fun `the d-pad keys map to the four directions`() {
        assertEquals(InputEvent.Move(Direction.UP), key(KeyCodes.DPAD_UP))
        assertEquals(InputEvent.Move(Direction.DOWN), key(KeyCodes.DPAD_DOWN))
        assertEquals(InputEvent.Move(Direction.LEFT), key(KeyCodes.DPAD_LEFT))
        assertEquals(InputEvent.Move(Direction.RIGHT), key(KeyCodes.DPAD_RIGHT))
    }

    // ---------------------------------------------------------- hat switch --

    @Test
    fun `a hat switch maps to the same directions`() {
        assertEquals(InputEvent.Move(Direction.LEFT), axes(hatX = -1f))
        assertEquals(InputEvent.Move(Direction.RIGHT), axes(hatX = 1f))
        assertEquals(InputEvent.Move(Direction.UP), axes(hatY = -1f))
        assertEquals(InputEvent.Move(Direction.DOWN), axes(hatY = 1f))
    }

    // ------------------------------------------------------------- stick --

    @Test
    fun `the left stick works too`() {
        assertEquals(InputEvent.Move(Direction.LEFT), axes(stickX = -0.9f))
        assertEquals(InputEvent.Move(Direction.UP), axes(stickY = -0.8f))
    }

    @Test
    fun `small stick movement inside the deadzone is ignored`() {
        assertNull(axes(stickX = 0.3f))
        assertNull(axes(stickX = -0.49f))
        assertNull(axes(stickX = 0.1f, stickY = 0.1f))
    }

    @Test
    fun `the stick's dominant axis wins so diagonals do not jitter`() {
        assertEquals(InputEvent.Move(Direction.LEFT), axes(stickX = -0.9f, stickY = -0.6f))
        assertEquals(InputEvent.Move(Direction.UP), axes(stickX = -0.6f, stickY = -0.9f))
    }

    // ------------------------------------------------------ no double-fire --

    @Test
    fun `holding a direction reports it once, not every frame`() {
        assertEquals(InputEvent.Move(Direction.LEFT), axes(hatX = -1f))
        assertNull("the same hat position must not fire again", axes(hatX = -1f))
        assertNull(axes(hatX = -1f))
    }

    @Test
    fun `a hat and a key reporting the same push counts once`() {
        // Exactly the Stadia case: some firmware sends both.
        assertEquals(InputEvent.Move(Direction.LEFT), axes(hatX = -1f))
        assertNull("the key echo of the same push must be swallowed", key(KeyCodes.DPAD_LEFT))
    }

    @Test
    fun `releasing and pressing again fires again`() {
        assertEquals(InputEvent.Move(Direction.LEFT), key(KeyCodes.DPAD_LEFT))
        release(KeyCodes.DPAD_LEFT)
        assertEquals(InputEvent.Move(Direction.LEFT), key(KeyCodes.DPAD_LEFT))
    }

    @Test
    fun `changing direction fires immediately`() {
        assertEquals(InputEvent.Move(Direction.LEFT), axes(hatX = -1f))
        assertEquals(InputEvent.Move(Direction.UP), axes(hatY = -1f))
    }

    // ----------------------------------------------------------- buttons --

    @Test
    fun `confirm comes from A, the d-pad centre or enter`() {
        for (code in listOf(KeyCodes.BUTTON_A, KeyCodes.DPAD_CENTER, KeyCodes.ENTER)) {
            assertEquals(
                "code $code should confirm",
                InputEvent.Press(Button.CONFIRM), mapper.onKey(code, down = true),
            )
        }
    }

    @Test
    fun `back comes from B or the system back key`() {
        assertEquals(InputEvent.Press(Button.BACK), key(KeyCodes.BUTTON_B))
        assertEquals(InputEvent.Press(Button.BACK), key(KeyCodes.BACK))
    }

    @Test
    fun `pause comes from start or menu`() {
        assertEquals(InputEvent.Press(Button.PAUSE), key(KeyCodes.BUTTON_START))
        assertEquals(InputEvent.Press(Button.PAUSE), key(KeyCodes.MENU))
    }

    @Test
    fun `buttons fire on press, not on release`() {
        assertEquals(InputEvent.Press(Button.CONFIRM), mapper.onKey(KeyCodes.BUTTON_A, down = true))
        assertNull(mapper.onKey(KeyCodes.BUTTON_A, down = false))
    }

    @Test
    fun `an unknown key code is ignored rather than crashing`() {
        assertNull(key(9999))
        assertNull(key(0))
    }

    // -------------------------------------------------------- held state --

    @Test
    fun `the currently held direction is available for the game to read`() {
        assertNull(mapper.heldDirection)
        key(KeyCodes.DPAD_LEFT)
        assertEquals(Direction.LEFT, mapper.heldDirection)
        release(KeyCodes.DPAD_LEFT)
        assertNull(mapper.heldDirection)
    }

    @Test
    fun `letting the stick fall back to centre clears the held direction`() {
        axes(stickX = -0.9f)
        assertEquals(Direction.LEFT, mapper.heldDirection)
        axes(stickX = 0f)
        assertNull(mapper.heldDirection)
    }

    @Test
    fun `a reset clears everything, as when a controller disconnects`() {
        key(KeyCodes.DPAD_LEFT)
        mapper.reset()
        assertNull(mapper.heldDirection)
        assertEquals(InputEvent.Move(Direction.LEFT), key(KeyCodes.DPAD_LEFT))
    }
}
