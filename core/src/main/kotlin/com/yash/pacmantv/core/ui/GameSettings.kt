package com.yash.pacmantv.core.ui

import com.yash.pacmantv.core.game.Difficulties
import com.yash.pacmantv.core.game.Difficulty
import com.yash.pacmantv.core.ports.SettingsStore
import com.yash.pacmantv.core.theme.Theme
import com.yash.pacmantv.core.theme.ThemeRegistry

/**
 * The player's preferences, typed, over the raw key/value [SettingsStore].
 *
 * Every setting is written the moment it changes, so the game survives being
 * killed by the television without losing anything. Unknown or corrupted values
 * fall back to defaults rather than failing.
 */
class GameSettings(private val store: SettingsStore) {

    var difficulty: Difficulty
        get() = Difficulties.byIdOrDefault(store.getString(KEY_DIFFICULTY, ""))
        set(value) = store.putString(KEY_DIFFICULTY, value.id)

    var theme: Theme
        get() = ThemeRegistry.byIdOrDefault(store.getString(KEY_THEME, ""))
        set(value) = store.putString(KEY_THEME, value.id)

    var soundEnabled: Boolean
        get() = store.getBoolean(KEY_SOUND, true)
        set(value) = store.putBoolean(KEY_SOUND, value)

    /** Kept per difficulty, so an easy run cannot flatter a hard one. */
    var highScore: Int
        get() = store.getInt(highScoreKey(), 0)
        set(value) {
            if (value > highScore) store.putInt(highScoreKey(), value)
        }

    private fun highScoreKey() = "$KEY_HIGH_SCORE_PREFIX${difficulty.id}"

    private companion object {
        const val KEY_DIFFICULTY = "difficulty"
        const val KEY_THEME = "theme"
        const val KEY_SOUND = "sound"
        const val KEY_HIGH_SCORE_PREFIX = "high_score_"
    }
}
