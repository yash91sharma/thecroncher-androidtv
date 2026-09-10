package com.yash.thecroncher.core.ui

import com.yash.thecroncher.core.game.Difficulties
import com.yash.thecroncher.core.game.Difficulty
import com.yash.thecroncher.core.ports.SettingsStore
import com.yash.thecroncher.core.theme.Theme
import com.yash.thecroncher.core.theme.ThemeRegistry

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

    /**
     * The look of the game. There is one theme and the game is named after it, so
     * this is not a stored preference — it is here because every screen asks the
     * settings for its theme, and that stays true the day a second one appears.
     */
    val theme: Theme get() = ThemeRegistry.default

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
        const val KEY_SOUND = "sound"
        const val KEY_HIGH_SCORE_PREFIX = "high_score_"
    }
}
