package com.yash.thecroncher

import android.app.Activity
import android.hardware.input.InputManager
import android.os.Bundle
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.yash.thecroncher.core.input.Button
import com.yash.thecroncher.core.input.InputEvent
import com.yash.thecroncher.core.input.InputMapper
import com.yash.thecroncher.core.ports.AudioOut
import com.yash.thecroncher.core.ports.SeededRng
import com.yash.thecroncher.core.ports.SilentAudioOut
import com.yash.thecroncher.core.ui.ControllerProbe
import com.yash.thecroncher.core.ui.GameSettings
import com.yash.thecroncher.core.ui.ScreenStack
import com.yash.thecroncher.core.ui.screens.MenuScreen

/**
 * The one and only activity: it owns the surface, the audio device and the input
 * plumbing, and hands everything else to `:core`, which knows nothing of Android.
 */
class MainActivity : Activity(), InputManager.InputDeviceListener {

    private lateinit var surface: GameSurfaceView
    private lateinit var settings: GameSettings
    private lateinit var stack: ScreenStack

    private val probe = ControllerProbe()
    private val mapper = InputMapper()
    private lateinit var adapter: AndroidInputAdapter

    private var audioDevice: AndroidAudioOut? = null
    private val audio: AudioOut get() = audioDevice ?: SilentAudioOut

    private var inputManager: InputManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        settings = GameSettings(AndroidSettingsStore(this))
        audioDevice = runCatching { AndroidAudioOut() }
            .onFailure { Logs.warn("no audio available", it) }
            .getOrNull()
        audio.setEnabled(settings.soundEnabled)

        stack = ScreenStack(newMenuScreen())
        stack.onExitRequested = { finishAndRemoveTask() }
        // Only live gameplay holds the panel on; a menu left up must let the TV
        // dim and run its screensaver, or the wordmark burns into the OLED.
        stack.onKeepScreenAwakeChanged = { awake -> runOnUiThread { keepScreenAwake(awake) } }
        keepScreenAwake(stack.keepScreenAwake)

        adapter = AndroidInputAdapter(mapper, probe) { stack.handle(it) }

        surface = GameSurfaceView(
            context = this,
            onTick = { stack.update(it) },
            // The theme is asked for each frame because it wears the chosen cat,
            // and the picker changes that while the surface is running.
            onRender = { ctx -> stack.render(ctx.gfx, settings.theme, ctx.tick) },
        )
        surface.theme = settings.theme
        setContentView(surface)
        surface.requestFocus()

        inputManager = (getSystemService(INPUT_SERVICE) as? InputManager)?.also {
            it.registerInputDeviceListener(this, null)
        }

        goFullscreen()
    }

    private fun newMenuScreen() = MenuScreen(
        settings = settings,
        audio = audio,
        rng = SeededRng(System.nanoTime()),
        probe = probe,
    )

    // ---------------------------------------------------------------- input --

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean =
        adapter.onKey(event) || super.onKeyDown(keyCode, event)

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean =
        adapter.onKey(event) || super.onKeyUp(keyCode, event)

    override fun onGenericMotionEvent(event: MotionEvent): Boolean =
        adapter.onMotion(event) || super.onGenericMotionEvent(event)

    // ----------------------------------------------- controller connection --

    override fun onInputDeviceAdded(deviceId: Int) {
        Logs.info { "controller connected: ${InputDevice.getDevice(deviceId)?.name}" }
    }

    override fun onInputDeviceChanged(deviceId: Int) = Unit

    override fun onInputDeviceRemoved(deviceId: Int) {
        // Losing the pad mid-game would otherwise leave the croncher running into a wall
        // with nobody driving. Drop any held direction and pause.
        Logs.info { "controller disconnected" }
        mapper.reset()
        stack.handle(InputEvent.Press(Button.PAUSE))
    }

    // ------------------------------------------------------------ lifecycle --

    override fun onResume() {
        super.onResume()
        surface.start()
    }

    override fun onPause() {
        surface.stop()
        audio.stopAll()
        super.onPause()
    }

    override fun onDestroy() {
        inputManager?.unregisterInputDeviceListener(this)
        audioDevice?.release()
        audioDevice = null
        super.onDestroy()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // Android restores the system bars whenever focus returns.
        if (hasFocus) goFullscreen()
    }

    private fun keepScreenAwake(awake: Boolean) {
        val flag = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        if (awake) window.addFlags(flag) else window.clearFlags(flag)
    }

    private fun goFullscreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
