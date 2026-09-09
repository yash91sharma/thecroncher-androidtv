package com.yash.pacmantv.core.theme

/**
 * Every piece of art the game can draw, addressed by a stable name.
 *
 * Call sites only ever name a [SpriteId] — they never know whether the pixels were
 * generated in code or loaded from a PNG pack. That indirection is what lets the
 * art be swapped without touching game or UI code.
 */
enum class SpriteId(val frameCount: Int) {
    /** Frame 0 is the closed mouth (a full circle), 1 and 2 open progressively. */
    PACMAN_RIGHT(3),
    PACMAN_LEFT(3),
    PACMAN_UP(3),
    PACMAN_DOWN(3),

    /** The spin-and-vanish death animation. */
    PACMAN_DEATH(11),

    /** Ghost bodies are drawn white and tinted per ghost, so one shape serves all four. */
    GHOST_RIGHT(2),
    GHOST_LEFT(2),
    GHOST_UP(2),
    GHOST_DOWN(2),
    GHOST_FRIGHTENED(2),
    GHOST_FRIGHTENED_FLASH(2),

    /** What is left of a ghost after Pac-Man eats it. */
    EYES_RIGHT(1),
    EYES_LEFT(1),
    EYES_UP(1),
    EYES_DOWN(1),

    PELLET(1),
    ENERGIZER(1),

    FRUIT_CHERRY(1),
    FRUIT_STRAWBERRY(1),
    FRUIT_ORANGE(1),
    FRUIT_APPLE(1),
    FRUIT_MELON(1),
    FRUIT_GALAXIAN(1),
    FRUIT_BELL(1),
    FRUIT_KEY(1),

    LIFE_ICON(1),
    ;

    /** Wraps out-of-range frame indices so animation code can count freely. */
    fun normaliseFrame(frame: Int): Int =
        ((frame % frameCount) + frameCount) % frameCount
}
