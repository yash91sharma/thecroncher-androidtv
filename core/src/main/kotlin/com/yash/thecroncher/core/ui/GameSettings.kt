package com.yash.thecroncher.core.ui

import com.yash.thecroncher.core.game.Difficulties
import com.yash.thecroncher.core.game.Difficulty
import com.yash.thecroncher.core.ports.SettingsStore
import com.yash.thecroncher.core.theme.Theme
import com.yash.thecroncher.core.theme.ThemeRegistry
import com.yash.thecroncher.core.theme.cats.CatBreed
import com.yash.thecroncher.core.theme.cats.CatRegistry

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

    /** Who the player is. A fresh install is the grey tabby the game began with. */
    var cat: CatBreed
        get() = CatRegistry.byIdOrDefault(store.getString(KEY_CAT, ""))
        set(value) = store.putString(KEY_CAT, value.id)

    /**
     * The look of the game, dressed in the chosen cat. There is one theme and the
     * game is named after it, so that part is not a stored preference; the cat is.
     *
     * Asked for every frame, so the dressed theme is kept until the cat changes —
     * it owns the sprite cache, and rebuilding that sixty times a second would
     * rasterise the whole cast on every draw.
     */
    val theme: Theme
        get() {
            val cat = cat
            val cached = dressed
            if (cached != null && cached.first == cat) return cached.second
            return ThemeRegistry.default.forCat(cat).also { dressed = cat to it }
        }

    private var dressed: Pair<CatBreed, Theme>? = null

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
        const val KEY_CAT = "cat"
        const val KEY_SOUND = "sound"
        const val KEY_HIGH_SCORE_PREFIX = "high_score_"
    }
}
