package com.yash.thecroncher.core.theme

/**
 * The art one foe wears, in every state it can be seen in.
 *
 * The game underneath still runs four ghosts with four different chase rules; this
 * is only what each of them looks like. Recasting the game — swapping the vacuum
 * for a hair dryer, say — is an edit to [FoeCast.all] plus the grids behind the
 * sprite ids, and no change at all to the simulation.
 */
data class FoeArt(
    val id: String,
    val displayName: String,
    val right: SpriteId,
    val left: SpriteId,
    val up: SpriteId,
    val down: SpriteId,
    val scared: SpriteId,
    val scaredFlash: SpriteId,
)

object FoeCast {

    val DOG = FoeArt(
        id = "dog",
        displayName = "DOG",
        right = SpriteId.DOG_RIGHT,
        left = SpriteId.DOG_LEFT,
        up = SpriteId.DOG_UP,
        down = SpriteId.DOG_DOWN,
        scared = SpriteId.DOG_SCARED,
        scaredFlash = SpriteId.DOG_SCARED_FLASH,
    )

    val VACUUM = FoeArt(
        id = "vacuum",
        displayName = "VACUUM",
        right = SpriteId.VACUUM_RIGHT,
        left = SpriteId.VACUUM_LEFT,
        up = SpriteId.VACUUM_UP,
        down = SpriteId.VACUUM_DOWN,
        scared = SpriteId.VACUUM_SCARED,
        scaredFlash = SpriteId.VACUUM_SCARED_FLASH,
    )

    val SPRAY = FoeArt(
        id = "spray",
        displayName = "SPRAY",
        right = SpriteId.SPRAY_RIGHT,
        left = SpriteId.SPRAY_LEFT,
        up = SpriteId.SPRAY_UP,
        down = SpriteId.SPRAY_DOWN,
        scared = SpriteId.SPRAY_SCARED,
        scaredFlash = SpriteId.SPRAY_SCARED_FLASH,
    )

    val CUCUMBER = FoeArt(
        id = "cucumber",
        displayName = "CUCUMBER",
        right = SpriteId.CUCUMBER_RIGHT,
        left = SpriteId.CUCUMBER_LEFT,
        up = SpriteId.CUCUMBER_UP,
        down = SpriteId.CUCUMBER_DOWN,
        scared = SpriteId.CUCUMBER_SCARED,
        scaredFlash = SpriteId.CUCUMBER_SCARED_FLASH,
    )

    /** In ghost order: the dog wears the chaser, which is the one that starts outside. */
    val all: List<FoeArt> = listOf(DOG, VACUUM, SPRAY, CUCUMBER)

    /** The art for ghost slot [index]; wraps so an extra ghost cannot crash a draw. */
    fun forIndex(index: Int): FoeArt = all[((index % all.size) + all.size) % all.size]
}

/**
 * Which treat sits on a given tile.
 *
 * The maze holds one kind of dot; the variety is purely visual, and is a fixed
 * function of the tile so a treat never changes shape under the cat.
 */
object TreatArt {

    val shapes: List<SpriteId> = listOf(
        SpriteId.TREAT_TRIANGLE,
        SpriteId.TREAT_SQUARE,
        SpriteId.TREAT_FISH,
        SpriteId.TREAT_STAR,
    )

    fun forTile(tileX: Int, tileY: Int): SpriteId {
        val scatter = (tileX * 7 + tileY * 3 + (tileX * tileY) / 5)
        return shapes[((scatter % shapes.size) + shapes.size) % shapes.size]
    }
}
