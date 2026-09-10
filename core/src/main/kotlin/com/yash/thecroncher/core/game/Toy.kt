package com.yash.thecroncher.core.game

import com.yash.thecroncher.core.theme.SpriteId

/**
 * The bonus items, in the order they are awarded. Same points and same schedule
 * as the arcade's fruit — a cat simply wants different prizes.
 */
enum class Toy(val points: Int, val sprite: SpriteId) {
    YARN(100, SpriteId.TOY_YARN),
    MILK(300, SpriteId.TOY_MILK),
    FISH(500, SpriteId.TOY_FISH),
    MOUSE(700, SpriteId.TOY_MOUSE),
    FEATHER(1000, SpriteId.TOY_FEATHER),
    BIRD(2000, SpriteId.TOY_BIRD),
    BELL(3000, SpriteId.TOY_BELL),
    GOLDFISH(5000, SpriteId.TOY_GOLDFISH),
}
