package com.yash.thecroncher.core.game

/**
 * Points, the ghost-eating chain, and the extra life.
 *
 * Kept separate from the rest of the game state because it is the one part a
 * player actually watches, and because the chain rule ("each ghost in a single
 * energizer is worth double the last") is easy to get subtly wrong.
 */
class ScoreBoard {

    var score: Int = 0
        private set

    var highScore: Int = 0

    private var ghostChain: Int = 0
    private var extraLifeAwarded = false
    private var extraLifePending = false

    fun eatDot() = add(DOT_POINTS)

    fun eatEnergizer() {
        // A new energizer always restarts the chain, even if the previous one is
        // still running.
        ghostChain = 0
        add(ENERGIZER_POINTS)
    }

    /** Scores the next ghost of this energizer and returns what it was worth. */
    fun eatGhost(): Int {
        val points = GHOST_POINTS[ghostChain.coerceAtMost(GHOST_POINTS.lastIndex)]
        if (ghostChain < GHOST_POINTS.lastIndex) ghostChain++
        add(points)
        return points
    }

    fun eatToy(toy: Toy) = add(toy.points)

    /** Called when the blue time runs out with ghosts left uneaten. */
    fun endFright() {
        ghostChain = 0
    }

    /** True once, on the tick the player earns their bonus life. */
    fun consumeExtraLifeAward(): Boolean {
        if (!extraLifePending) return false
        extraLifePending = false
        return true
    }

    fun resetForNewGame() {
        score = 0
        ghostChain = 0
        extraLifeAwarded = false
        extraLifePending = false
    }

    private fun add(points: Int) {
        score += points
        if (!extraLifeAwarded && score >= EXTRA_LIFE_AT) {
            extraLifeAwarded = true
            extraLifePending = true
        }
        if (score > highScore) highScore = score
    }

    companion object {
        const val DOT_POINTS = 10
        const val ENERGIZER_POINTS = 50
        const val EXTRA_LIFE_AT = 10_000

        val GHOST_POINTS = intArrayOf(200, 400, 800, 1600)
    }
}
