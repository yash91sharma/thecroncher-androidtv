package com.yash.thecroncher.core.theme

import com.yash.thecroncher.core.theme.cats.CatBreed

/**
 * Where a theme's art comes from. Implemented in `:core` by
 * [ProceduralSpriteSource] (drawn in code) and in `:app` by a bitmap source that
 * reads PNG packs from assets — swapping between them is a theme-level choice, not
 * a code change at any call site.
 */
interface SpriteSource {
    val id: String

    fun sprite(spriteId: SpriteId, frame: Int): Sprite

    /**
     * The same art with a different cat in it. The player's choice of breed is
     * the one thing about the cast that is not the theme's to decide.
     */
    fun forCat(cat: CatBreed): SpriteSource
}
