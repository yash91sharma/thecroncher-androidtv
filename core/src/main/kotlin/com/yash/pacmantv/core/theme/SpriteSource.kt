package com.yash.pacmantv.core.theme

/**
 * Where a theme's art comes from. Implemented in `:core` by
 * [ProceduralSpriteSource] (drawn in code) and in `:app` by a bitmap source that
 * reads PNG packs from assets — swapping between them is a theme-level choice, not
 * a code change at any call site.
 */
interface SpriteSource {
    val id: String

    fun sprite(spriteId: SpriteId, frame: Int): Sprite
}
