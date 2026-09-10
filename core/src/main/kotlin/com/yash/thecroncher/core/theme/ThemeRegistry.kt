package com.yash.thecroncher.core.theme

import com.yash.thecroncher.core.theme.themes.TheCroncher

/**
 * The list of themes the game offers.
 *
 * There is one, and the game is named after it. The machinery is still here
 * because it costs nothing and is what keeps colours out of the drawing code: to
 * add another, create a `Theme` in `theme/themes/` and add it to [all]. That is
 * the whole procedure — `ThemeRegistryTest` will tell you if you left a colour
 * slot or a sprite unfilled.
 */
object ThemeRegistry {

    val all: List<Theme> = listOf(TheCroncher)

    val default: Theme = TheCroncher

    fun byId(id: String?): Theme? = all.firstOrNull { it.id == id }

    fun byIdOrDefault(id: String?): Theme = byId(id) ?: default
}
