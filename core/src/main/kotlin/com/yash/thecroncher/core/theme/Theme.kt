package com.yash.thecroncher.core.theme

import com.yash.thecroncher.core.theme.cats.CatBreed

/** Colours are packed ARGB ints. Every slot must be fully opaque. */
data class MazeColors(
    val wall: Int,
    val wallInner: Int,
    val door: Int,
)

data class HudColors(
    val text: Int,
    val score: Int,
    val highScore: Int,
    /** Reserved for the things that must be noticed: GAME OVER, and nothing else. */
    val alert: Int,
)

data class MenuColors(
    val background: Int,
    val title: Int,
    val item: Int,
    val itemSelected: Int,
    val cursor: Int,
    val footer: Int,
)

/**
 * The complete visual identity of the game: every colour and the art it uses.
 *
 * Nothing outside a theme file is allowed to name a colour. Restyling the whole
 * game is therefore a matter of adding one of these and registering it, with no
 * change to any drawing code — the characters themselves are coloured through
 * [palette], which is also what the [sprites] source paints with.
 */
data class Theme(
    val id: String,
    val displayName: String,
    val background: Int,
    val maze: MazeColors,
    val hud: HudColors,
    val menu: MenuColors,
    val palette: SpritePalette,
    val sprites: SpriteSource,
) {
    /** This theme, drawn with the player's chosen cat. Nothing else changes. */
    fun forCat(cat: CatBreed): Theme = copy(sprites = sprites.forCat(cat))

    /**
     * Every colour slot, paired with its name. Used by the tests to prove a theme
     * has no forgotten (and therefore invisible) entries — a transparent maze is a
     * miserable thing to debug on a television.
     */
    fun allColours(): List<Pair<String, Int>> = listOf(
        "background" to background,
        "maze.wall" to maze.wall,
        "maze.wallInner" to maze.wallInner,
        "maze.door" to maze.door,
        "hud.text" to hud.text,
        "hud.score" to hud.score,
        "hud.highScore" to hud.highScore,
        "hud.alert" to hud.alert,
        "menu.background" to menu.background,
        "menu.title" to menu.title,
        "menu.item" to menu.item,
        "menu.itemSelected" to menu.itemSelected,
        "menu.cursor" to menu.cursor,
        "menu.footer" to menu.footer,
    ) + palette.allColours().map { (name, colour) -> "palette.$name" to colour }
}
