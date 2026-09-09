package com.yash.pacmantv.core.game

/** Everything that varies from one level to the next. */
data class LevelSpec(
    val pacmanSpeed: Double,
    val pacmanFrightSpeed: Double,
    val ghostSpeed: Double,
    val ghostTunnelSpeed: Double,
    val ghostFrightSpeed: Double,
    val frightSeconds: Double,
    val frightFlashes: Int,
    val elroyDotsLeft: Int,
)

/**
 * The arcade's per-level progression: everything speeds up, the blue time shrinks
 * to nothing, and by level nineteen the energizers stop working altogether.
 *
 * Speeds are fractions of the notional 100% (one pixel per tick). These follow the
 * published tables; the sub-frame timing of the original is approximated, which is
 * the one place this game knowingly differs from the ROM.
 */
object LevelTable {

    private val LEVELS: List<LevelSpec> = listOf(
        //             pac   pacFr ghost tunnel gFright  fright flashes elroy
        LevelSpec(0.80, 0.90, 0.75, 0.40, 0.50, 6.0, 5, 20),   // 1
        LevelSpec(0.90, 0.95, 0.85, 0.45, 0.55, 5.0, 5, 30),   // 2
        LevelSpec(0.90, 0.95, 0.85, 0.45, 0.55, 4.0, 5, 40),   // 3
        LevelSpec(0.90, 0.95, 0.85, 0.45, 0.55, 3.0, 5, 40),   // 4
        LevelSpec(1.00, 1.00, 0.95, 0.50, 0.60, 2.0, 5, 40),   // 5
        LevelSpec(1.00, 1.00, 0.95, 0.50, 0.60, 5.0, 5, 50),   // 6
        LevelSpec(1.00, 1.00, 0.95, 0.50, 0.60, 2.0, 5, 50),   // 7
        LevelSpec(1.00, 1.00, 0.95, 0.50, 0.60, 2.0, 5, 50),   // 8
        LevelSpec(1.00, 1.00, 0.95, 0.50, 0.60, 1.0, 3, 60),   // 9
        LevelSpec(1.00, 1.00, 0.95, 0.50, 0.60, 5.0, 5, 60),   // 10
        LevelSpec(1.00, 1.00, 0.95, 0.50, 0.60, 2.0, 5, 60),   // 11
        LevelSpec(1.00, 1.00, 0.95, 0.50, 0.60, 1.0, 3, 80),   // 12
        LevelSpec(1.00, 1.00, 0.95, 0.50, 0.60, 1.0, 3, 80),   // 13
        LevelSpec(1.00, 1.00, 0.95, 0.50, 0.60, 3.0, 5, 80),   // 14
        LevelSpec(1.00, 1.00, 0.95, 0.50, 0.60, 1.0, 3, 100),  // 15
        LevelSpec(1.00, 1.00, 0.95, 0.50, 0.60, 1.0, 3, 100),  // 16
        LevelSpec(1.00, 1.00, 0.95, 0.50, 0.60, 0.0, 0, 100),  // 17
        LevelSpec(1.00, 1.00, 0.95, 0.50, 0.60, 1.0, 3, 100),  // 18
        LevelSpec(1.00, 1.00, 0.95, 0.50, 0.60, 0.0, 0, 120),  // 19
        LevelSpec(1.00, 1.00, 0.95, 0.50, 0.60, 0.0, 0, 120),  // 20
        LevelSpec(0.90, 0.90, 0.95, 0.50, 0.60, 0.0, 0, 120),  // 21 and beyond
    )

    fun forLevel(level: Int): LevelSpec =
        LEVELS[(level - 1).coerceIn(0, LEVELS.size - 1)]

    private val FRUIT_BY_LEVEL = listOf(
        Fruit.CHERRY, Fruit.STRAWBERRY, Fruit.ORANGE, Fruit.ORANGE,
        Fruit.APPLE, Fruit.APPLE, Fruit.MELON, Fruit.MELON,
        Fruit.GALAXIAN, Fruit.GALAXIAN, Fruit.BELL, Fruit.BELL,
        Fruit.KEY,
    )

    /** From level thirteen onwards it is always the key. */
    fun fruitForLevel(level: Int): Fruit =
        FRUIT_BY_LEVEL[(level - 1).coerceIn(0, FRUIT_BY_LEVEL.size - 1)]
}
