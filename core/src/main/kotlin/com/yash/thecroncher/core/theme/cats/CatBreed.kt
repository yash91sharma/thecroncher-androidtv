package com.yash.thecroncher.core.theme.cats

/**
 * One playable cat: its face, and the colours the face is drawn in.
 *
 * A breed is a grid of inks plus what each ink looks like, exactly the way the
 * rest of the cast is drawn — so a recolour of an existing face (the orange tabby)
 * and a breed with its own silhouette (the sphynx) are the same kind of thing and
 * cost the same to add: one file in this package, one line in [CatRegistry].
 *
 * The cat inks are the top of the alphabet in `CronchArt`:
 * ```
 *   F fur   D marking (stripe, mask, patch)   L muzzle and light patches
 *   E ear   N nose   Y eye   P pupil   M mouth
 * ```
 * Whiskers are the shared `i` highlight ink, so every cat's whiskers match the
 * glints on the rest of the cast.
 *
 * Colours are packed ARGB and must be fully opaque; `CatRegistryTest` checks it.
 */
data class CatBreed(
    val id: String,
    /** Sixteen rows of sixteen inks: the face, looking at the player. */
    val face: List<String>,
    val fur: Int,
    val marking: Int,
    val muzzle: Int,
    val earInner: Int,
    val nose: Int,
    val eye: Int,
    val pupil: Int,
    val mouth: Int,
) {

    /** Maps the cat's inks onto this breed's colours. */
    val ink: Map<Char, Int> = mapOf(
        'F' to fur, 'D' to marking, 'L' to muzzle, 'E' to earInner,
        'N' to nose, 'Y' to eye, 'P' to pupil, 'M' to mouth,
    )

    /** Every slot with its name, so a forgotten colour fails a test not a TV. */
    fun allColours(): List<Pair<String, Int>> = listOf(
        "fur" to fur,
        "marking" to marking,
        "muzzle" to muzzle,
        "earInner" to earInner,
        "nose" to nose,
        "eye" to eye,
        "pupil" to pupil,
        "mouth" to mouth,
    )

    companion object {
        /** Every face is this many pixels square: two maze tiles, like the foes. */
        const val SIZE = 16
    }
}
