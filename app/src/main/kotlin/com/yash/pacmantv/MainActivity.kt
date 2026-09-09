package com.yash.pacmantv

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.yash.pacmantv.core.ports.SettingsStore
import com.yash.pacmantv.core.theme.ThemeRegistry

/**
 * The one and only activity. It owns the surface and the Android-side adapters;
 * everything above it lives in `:core` and knows nothing about Android.
 */
class MainActivity : Activity() {

    private lateinit var surface: GameSurfaceView
    private lateinit var settings: SettingsStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        settings = AndroidSettingsStore(this)

        surface = GameSurfaceView(
            context = this,
            onRender = { ctx ->
                TestPatternScreen.render(ctx.gfx, ctx.theme, ctx.tick, ctx.fps)
            },
        )
        surface.theme = ThemeRegistry.byIdOrDefault(settings.getString(KEY_THEME, ""))
        setContentView(surface)
        surface.requestFocus()

        goFullscreen()
    }

    /** Phase 3 scaffolding: lets the theme be cycled on the TV to prove it works. */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val step = when (keyCode) {
            KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_BUTTON_A,
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> 1
            KeyEvent.KEYCODE_DPAD_LEFT -> -1
            else -> return super.onKeyDown(keyCode, event)
        }
        val themes = ThemeRegistry.all
        val next = themes[((themes.indexOf(surface.theme) + step) + themes.size) % themes.size]
        surface.theme = next
        settings.putString(KEY_THEME, next.id)
        Log.i(GameSurfaceView.TAG, "theme -> ${next.id}")
        return true
    }

    override fun onResume() {
        super.onResume()
        surface.start()
    }

    override fun onPause() {
        surface.stop()
        super.onPause()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // Android puts the system bars back whenever focus returns.
        if (hasFocus) goFullscreen()
    }

    private fun goFullscreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private companion object {
        const val KEY_THEME = "theme"
    }
}
