package com.yash.thecroncher.core.game

/**
 * A difficulty tier. Pure data: retuning the game, or adding a fourth tier, is an
 * edit to [Difficulties.all] and nothing else.
 */
data class Difficulty(
    val id: String,
    val displayName: String,

    /** Multiplies the level table's ghost speed. */
    val ghostSpeedScale: Double,

    /** Multiplies the length of every scatter phase, leaving chases alone. */
    val scatterScale: Double,

    val lives: Int,

    /** Fixed blue time in seconds, or null to follow the arcade level table. */
    val frightSecondsOverride: Double?,
) {
    fun frightSeconds(level: Int): Double =
        frightSecondsOverride ?: LevelTable.forLevel(level).frightSeconds
}

object Difficulties {

    val EASY = Difficulty(
        id = "easy",
        displayName = "Easy",
        ghostSpeedScale = 0.85,
        scatterScale = 1.5,
        lives = 5,
        frightSecondsOverride = 9.0,
    )

    val NORMAL = Difficulty(
        id = "normal",
        displayName = "Normal",
        ghostSpeedScale = 1.00,
        scatterScale = 1.0,
        lives = 3,
        frightSecondsOverride = null,   // arcade-accurate
    )

    val HARD = Difficulty(
        id = "hard",
        displayName = "Hard",
        ghostSpeedScale = 1.10,
        scatterScale = 0.6,
        lives = 2,
        frightSecondsOverride = 3.0,
    )

    val all = listOf(EASY, NORMAL, HARD)

    val default = NORMAL

    fun byId(id: String?): Difficulty? = all.firstOrNull { it.id == id }

    fun byIdOrDefault(id: String?): Difficulty = byId(id) ?: default
}
