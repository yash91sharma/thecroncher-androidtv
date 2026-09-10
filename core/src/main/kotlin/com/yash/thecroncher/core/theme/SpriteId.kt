package com.yash.thecroncher.core.theme

/**
 * Every piece of art the game can draw, addressed by a stable name.
 *
 * Call sites only ever name a [SpriteId] — they never know whether the pixels were
 * drawn as an ASCII grid or loaded from a PNG pack. That indirection is what lets
 * the art be swapped without touching game or UI code.
 */
enum class SpriteId(val frameCount: Int) {
    /**
     * The croncher himself. One face, looking straight at the player whichever
     * way he is running — his expression is the character, and turning it into a
     * profile or the back of a head at sixteen pixels only made him hard to read.
     * Frame 1 is that face lifted a pixel, so walking gives him a bounce.
     */
    CAT(2),

    /** Caught: the cat fizzles out in a puff of fur. */
    CAT_FAINT(11),

    /** A dog. Loud, fast, and directly behind you. */
    DOG_RIGHT(2),
    DOG_LEFT(2),
    DOG_UP(2),
    DOG_DOWN(2),
    DOG_SCARED(2),
    DOG_SCARED_FLASH(2),

    /** A vacuum cleaner, roaring around the corner you were about to take. */
    VACUUM_RIGHT(2),
    VACUUM_LEFT(2),
    VACUUM_UP(2),
    VACUUM_DOWN(2),
    VACUUM_SCARED(2),
    VACUUM_SCARED_FLASH(2),

    /** The spray bottle. It only has to be picked up to win. */
    SPRAY_RIGHT(2),
    SPRAY_LEFT(2),
    SPRAY_UP(2),
    SPRAY_DOWN(2),
    SPRAY_SCARED(2),
    SPRAY_SCARED_FLASH(2),

    /** A cucumber, left on the floor behind an unsuspecting cat. */
    CUCUMBER_RIGHT(2),
    CUCUMBER_LEFT(2),
    CUCUMBER_UP(2),
    CUCUMBER_DOWN(2),
    CUCUMBER_SCARED(2),
    CUCUMBER_SCARED_FLASH(2),

    /** What is left of a foe after the cat has dealt with it: a puff, hurrying home. */
    PUFF(2),

    /** The crunchy bits. Four shapes, scattered through the maze. */
    TREAT_TRIANGLE(1),
    TREAT_SQUARE(1),
    TREAT_FISH(1),
    TREAT_STAR(1),

    /** The big one. Eat it and the fears become the prey. */
    CATNIP(1),

    TOY_YARN(1),
    TOY_MILK(1),
    TOY_FISH(1),
    TOY_MOUSE(1),
    TOY_FEATHER(1),
    TOY_BIRD(1),
    TOY_BELL(1),
    TOY_GOLDFISH(1),

    /** One per life still in reserve. */
    LIFE_ICON(1),
    ;

    /** Wraps out-of-range frame indices so animation code can count freely. */
    fun normaliseFrame(frame: Int): Int =
        ((frame % frameCount) + frameCount) % frameCount
}
