package com.yash.pacmantv.core.theme

import com.yash.pacmantv.core.theme.themes.ClassicArcade
import com.yash.pacmantv.core.theme.themes.Monochrome
import com.yash.pacmantv.core.theme.themes.Neon

/**
 * The list of themes the game offers.
 *
 * To add one: create a `Theme` in `theme/themes/`, then add it to [all]. That is
 * the whole procedure — `ThemeRegistryTest` will tell you if you left a colour
 * slot or a sprite unfilled.
 */
object ThemeRegistry {

    val all: List<Theme> = listOf(ClassicArcade, Neon, Monochrome)

    val default: Theme = ClassicArcade

    fun byId(id: String?): Theme? = all.firstOrNull { it.id == id }

    fun byIdOrDefault(id: String?): Theme = byId(id) ?: default
}
