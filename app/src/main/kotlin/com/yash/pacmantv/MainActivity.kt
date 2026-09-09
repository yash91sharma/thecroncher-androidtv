package com.yash.pacmantv

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.yash.pacmantv.core.game.Difficulties
import com.yash.pacmantv.core.game.Direction
import com.yash.pacmantv.core.game.GamePhase
import com.yash.pacmantv.core.game.GameState
import com.yash.pacmantv.core.game.Maze
import com.yash.pacmantv.core.ports.SeededRng
import com.yash.pacmantv.core.ports.SilentAudioOut
import com.yash.pacmantv.core.ports.SettingsStore
import com.yash.pacmantv.core.theme.ThemeRegistry
import com.yash.pacmantv.core.ui.GameRenderer

/**
 * The one and only activity. It owns the surface and the Android-side adapters;
 * everything above it lives in `:core` and knows nothing about Android.
 */
class MainActivity : Activity() {

    private lateinit var surface: GameSurfaceView
    private lateinit var settings: SettingsStore
    private lateinit var game: GameState
    private var audio: AndroidAudioOut? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        settings = AndroidSettingsStore(this)
        audio = runCatching { AndroidAudioOut() }.getOrNull()

        game = newGame()

        surface = GameSurfaceView(
            context = this,
            onTick = { game.tick() },
            onRender = { ctx -> GameRenderer.render(ctx.gfx, ctx.theme, game, ctx.tick) },
        )
        surface.theme = ThemeRegistry.byIdOrDefault(settings.getString(KEY_THEME, ""))
        setContentView(surface)
        surface.requestFocus()

        goFullscreen()
    }

    private fun newGame(): GameState = GameState(
        maze = Maze.loadClassic(),
        difficulty = Difficulties.byIdOrDefault(settings.getString(KEY_DIFFICULTY, "")),
        rng = SeededRng(System.nanoTime()),
        audio = if (settings.getBoolean(KEY_SOUND, true)) audio ?: SilentAudioOut else SilentAudioOut,
    ).also {
        it.scores.highScore = settings.getInt(KEY_HIGH_SCORE, 0)
        it.startNewGame()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> game.requestDirection(Direction.UP)
            KeyEvent.KEYCODE_DPAD_DOWN -> game.requestDirection(Direction.DOWN)
            KeyEvent.KEYCODE_DPAD_LEFT -> game.requestDirection(Direction.LEFT)
            KeyEvent.KEYCODE_DPAD_RIGHT -> game.requestDirection(Direction.RIGHT)

            KeyEvent.KEYCODE_BUTTON_A, KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER ->
                if (game.phase == GamePhase.GAME_OVER) restart()

            // Phase 4 scaffolding: cycle themes until the real settings screen lands.
            KeyEvent.KEYCODE_BUTTON_X, KeyEvent.KEYCODE_MENU -> cycleTheme()

            else -> return super.onKeyDown(keyCode, event)
        }
        return true
    }

    private fun restart() {
        settings.putInt(KEY_HIGH_SCORE, game.highScore)
        game = newGame()
    }

    private fun cycleTheme() {
        val themes = ThemeRegistry.all
        val next = themes[(themes.indexOf(surface.theme) + 1) % themes.size]
        surface.theme = next
        settings.putString(KEY_THEME, next.id)
        Log.i(GameSurfaceView.TAG, "theme -> ${next.id}")
    }

    override fun onResume() {
        super.onResume()
        surface.start()
    }

    override fun onDestroy() {
        audio?.release()
        audio = null
        super.onDestroy()
    }

    override fun onPause() {
        surface.stop()
        audio?.stopAll()
        settings.putInt(KEY_HIGH_SCORE, game.highScore)
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
        const val KEY_DIFFICULTY = "difficulty"
        const val KEY_HIGH_SCORE = "high_score"
        const val KEY_SOUND = "sound"
    }
}
