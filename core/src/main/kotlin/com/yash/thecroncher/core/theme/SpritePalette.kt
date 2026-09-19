package com.yash.thecroncher.core.theme

/**
 * Every colour the art is drawn in, named by what it *is* rather than what it
 * looks like.
 *
 * The grids in [CronchArt] say "tongue", "water", "yarn"; this says what those
 * are. Nothing outside a theme file is allowed to name a colour, so restyling the
 * whole cast — a night-time palette — is one of these, with no change to a single
 * grid or a single call site. The cat is the exception: the player chooses him,
 * so his colours live with his breed in `cats/`.
 *
 * Colours are packed ARGB and must be fully opaque; `ThemeRegistryTest` checks it.
 */
data class SpritePalette(
    // --- the dog ---
    val dogFur: Int,
    val dogEars: Int,
    val dogSnout: Int,
    val dogTongue: Int,

    // --- the vacuum ---
    val vacuumBody: Int,
    val vacuumShade: Int,
    val vacuumHose: Int,
    val vacuumLight: Int,

    // --- the spray bottle ---
    val sprayBottle: Int,
    val sprayShade: Int,
    val sprayWater: Int,

    // --- the cucumber ---
    val cucumberBody: Int,
    val cucumberRidge: Int,
    val cucumberFlesh: Int,

    // --- a foe that has been dealt with, and one that is frightened ---
    val puffBody: Int,
    val puffShade: Int,
    val scaredBody: Int,
    val scaredFace: Int,
    val flashBody: Int,
    val flashFace: Int,

    // --- the crunchy bits ---
    val treatTriangle: Int,
    val treatSquare: Int,
    val treatFish: Int,
    val treatStar: Int,
    val treatShade: Int,
    val catnipLeaf: Int,
    val catnipVein: Int,

    // --- the bonus toys ---
    val toyYarn: Int,
    val toyYarnDark: Int,
    val toyMilk: Int,
    val toyBowl: Int,
    val toyFish: Int,
    val toyFishDark: Int,
    val toyMouse: Int,
    val toyMousePink: Int,
    val toyFeather: Int,
    val toyStick: Int,
    val toyBird: Int,
    val toyBeak: Int,
    val toyBell: Int,
    val toyBellDark: Int,
    val toyGold: Int,
    val toyGoldDark: Int,

    // --- shared ---
    val outline: Int,
    val metal: Int,
    val highlight: Int,
) {

    /** Maps the ink alphabet of [CronchArt] onto this palette. */
    val ink: Map<Char, Int> = mapOf(
        'g' to dogFur, 'h' to dogEars, 's' to dogSnout, 't' to dogTongue,
        'v' to vacuumBody, 'w' to vacuumShade, 'u' to vacuumHose, 'r' to vacuumLight,
        'b' to sprayBottle, 'c' to sprayShade, 'q' to sprayWater,
        'k' to cucumberBody, 'j' to cucumberRidge, 'f' to cucumberFlesh,
        'p' to puffBody, 'n' to puffShade,
        '1' to treatTriangle, '2' to treatSquare, '3' to treatFish,
        '4' to treatStar, '5' to treatShade,
        '6' to catnipLeaf, '7' to catnipVein,
        'A' to toyYarn, 'a' to toyYarnDark, 'B' to toyMilk, 'C' to toyBowl,
        'G' to toyFish, 'H' to toyFishDark, 'I' to toyMouse, 'J' to toyMousePink,
        'K' to toyFeather, 'S' to toyStick, 'T' to toyBird, 'U' to toyBeak,
        'V' to toyBell, 'W' to toyBellDark, 'X' to toyGold, 'Z' to toyGoldDark,
        'o' to outline, 'm' to metal, 'i' to highlight,
    )

    /** Every slot with its name, so a forgotten colour fails a test not a TV. */
    fun allColours(): List<Pair<String, Int>> = listOf(
        "dogFur" to dogFur,
        "dogEars" to dogEars,
        "dogSnout" to dogSnout,
        "dogTongue" to dogTongue,
        "vacuumBody" to vacuumBody,
        "vacuumShade" to vacuumShade,
        "vacuumHose" to vacuumHose,
        "vacuumLight" to vacuumLight,
        "sprayBottle" to sprayBottle,
        "sprayShade" to sprayShade,
        "sprayWater" to sprayWater,
        "cucumberBody" to cucumberBody,
        "cucumberRidge" to cucumberRidge,
        "cucumberFlesh" to cucumberFlesh,
        "puffBody" to puffBody,
        "puffShade" to puffShade,
        "scaredBody" to scaredBody,
        "scaredFace" to scaredFace,
        "flashBody" to flashBody,
        "flashFace" to flashFace,
        "treatTriangle" to treatTriangle,
        "treatSquare" to treatSquare,
        "treatFish" to treatFish,
        "treatStar" to treatStar,
        "treatShade" to treatShade,
        "catnipLeaf" to catnipLeaf,
        "catnipVein" to catnipVein,
        "toyYarn" to toyYarn,
        "toyYarnDark" to toyYarnDark,
        "toyMilk" to toyMilk,
        "toyBowl" to toyBowl,
        "toyFish" to toyFish,
        "toyFishDark" to toyFishDark,
        "toyMouse" to toyMouse,
        "toyMousePink" to toyMousePink,
        "toyFeather" to toyFeather,
        "toyStick" to toyStick,
        "toyBird" to toyBird,
        "toyBeak" to toyBeak,
        "toyBell" to toyBell,
        "toyBellDark" to toyBellDark,
        "toyGold" to toyGold,
        "toyGoldDark" to toyGoldDark,
        "outline" to outline,
        "metal" to metal,
        "highlight" to highlight,
    )
}
