package com.yash.pacmantv.core.theme

/** Colours are packed ARGB ints. Every slot must be fully opaque. */
data class MazeColors(
    val wall: Int,
    val wallInner: Int,
    val door: Int,
    val tunnel: Int,
)

data class EntityColors(
    val pacman: Int,
    val blinky: Int,
    val pinky: Int,
    val inky: Int,
    val clyde: Int,
    val frightened: Int,
    val frightenedFlash: Int,
    val eyeWhite: Int,
    val eyePupil: Int,
)

data class HudColors(
    val text: Int,
    val score: Int,
    val highScore: Int,
    val lifeIcon: Int,
    val fruitText: Int,
)

data class MenuColors(
    val background: Int,
    val title: Int,
    val item: Int,
    val itemSelected: Int,
    val cursor: Int,
    val footer: Int,
)

data class PelletColors(
    val pellet: Int,
    val energizer: Int,
)

/**
 * The complete visual identity of the game: every colour and the art it uses.
 *
 * Nothing outside a theme file is allowed to name a colour. Restyling the whole
 * game — or swapping the sprite art for a PNG pack — is therefore a matter of
 * adding one of these and registering it, with no change to any drawing code.
 */
data class Theme(
    val id: String,
    val displayName: String,
    val background: Int,
    val maze: MazeColors,
    val entities: EntityColors,
    val hud: HudColors,
    val menu: MenuColors,
    val pellet: PelletColors,
    val sprites: SpriteSource,
) {
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
        "maze.tunnel" to maze.tunnel,
        "entities.pacman" to entities.pacman,
        "entities.blinky" to entities.blinky,
        "entities.pinky" to entities.pinky,
        "entities.inky" to entities.inky,
        "entities.clyde" to entities.clyde,
        "entities.frightened" to entities.frightened,
        "entities.frightenedFlash" to entities.frightenedFlash,
        "entities.eyeWhite" to entities.eyeWhite,
        "entities.eyePupil" to entities.eyePupil,
        "hud.text" to hud.text,
        "hud.score" to hud.score,
        "hud.highScore" to hud.highScore,
        "hud.lifeIcon" to hud.lifeIcon,
        "hud.fruitText" to hud.fruitText,
        "menu.background" to menu.background,
        "menu.title" to menu.title,
        "menu.item" to menu.item,
        "menu.itemSelected" to menu.itemSelected,
        "menu.cursor" to menu.cursor,
        "menu.footer" to menu.footer,
        "pellet.pellet" to pellet.pellet,
        "pellet.energizer" to pellet.energizer,
    )

    /** The colour a ghost's body should be tinted, given its index 0..3. */
    fun ghostColour(index: Int): Int = when (index) {
        0 -> entities.blinky
        1 -> entities.pinky
        2 -> entities.inky
        else -> entities.clyde
    }
}
