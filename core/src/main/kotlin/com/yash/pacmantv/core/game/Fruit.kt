package com.yash.pacmantv.core.game

import com.yash.pacmantv.core.theme.SpriteId

/** The bonus items, in the order the arcade awards them. */
enum class Fruit(val points: Int, val sprite: SpriteId) {
    CHERRY(100, SpriteId.FRUIT_CHERRY),
    STRAWBERRY(300, SpriteId.FRUIT_STRAWBERRY),
    ORANGE(500, SpriteId.FRUIT_ORANGE),
    APPLE(700, SpriteId.FRUIT_APPLE),
    MELON(1000, SpriteId.FRUIT_MELON),
    GALAXIAN(2000, SpriteId.FRUIT_GALAXIAN),
    BELL(3000, SpriteId.FRUIT_BELL),
    KEY(5000, SpriteId.FRUIT_KEY),
}
