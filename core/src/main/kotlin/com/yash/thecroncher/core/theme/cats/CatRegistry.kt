package com.yash.thecroncher.core.theme.cats

/**
 * The cats the player can choose from, in the order the picker shows them.
 *
 * To add one: create a [CatBreed] in this package and add it to [all]. That is
 * the whole procedure — `CatRegistryTest` will tell you if the grid is the wrong
 * size or a colour was left transparent.
 */
object CatRegistry {

    val all: List<CatBreed> = listOf(GreyTabby, OrangeTabby, BlackCat, Siamese, Sphynx, Tuxedo)

    /** The cat the game started with, and the one a fresh install plays as. */
    val default: CatBreed = GreyTabby

    fun byId(id: String?): CatBreed? = all.firstOrNull { it.id == id }

    fun byIdOrDefault(id: String?): CatBreed = byId(id) ?: default
}
