package com.yash.thecroncher.core.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScoringTest {

    private val score = ScoreBoard()

    @Test
    fun `a dot is ten and an energizer fifty`() {
        score.eatDot()
        assertEquals(10, score.score)
        score.eatEnergizer()
        assertEquals(60, score.score)
    }

    @Test
    fun `the ghost chain doubles up to sixteen hundred`() {
        assertEquals(200, score.eatGhost())
        assertEquals(400, score.eatGhost())
        assertEquals(800, score.eatGhost())
        assertEquals(1600, score.eatGhost())
        assertEquals(3000, score.score)
    }

    @Test
    fun `a fifth ghost in one energizer cannot happen but is handled anyway`() {
        repeat(4) { score.eatGhost() }
        assertEquals("must not double past 1600", 1600, score.eatGhost())
    }

    @Test
    fun `the chain resets with each new energizer`() {
        score.eatEnergizer()
        assertEquals(200, score.eatGhost())
        assertEquals(400, score.eatGhost())

        score.eatEnergizer()
        assertEquals("a fresh energizer restarts the chain", 200, score.eatGhost())
    }

    @Test
    fun `the chain also resets when fright ends without eating four`() {
        score.eatEnergizer()
        score.eatGhost()
        score.endFright()
        score.eatEnergizer()
        assertEquals(200, score.eatGhost())
    }

    @Test
    fun `toy scores its face value`() {
        score.eatToy(Toy.MILK)
        assertEquals(300, score.score)
    }

    @Test
    fun `an extra life is awarded at ten thousand, exactly once`() {
        assertFalse(score.consumeExtraLifeAward())
        repeat(999) { score.eatDot() }        // 9,990
        assertFalse(score.consumeExtraLifeAward())
        score.eatDot()                         // 10,000
        assertTrue("extra life should be due", score.consumeExtraLifeAward())
        assertFalse("and only once", score.consumeExtraLifeAward())

        repeat(2000) { score.eatDot() }        // well past it
        assertFalse(score.consumeExtraLifeAward())
    }

    @Test
    fun `the high score tracks the best run and never decreases`() {
        score.highScore = 5000
        repeat(100) { score.eatDot() }         // 1,000
        assertEquals(5000, score.highScore)

        repeat(500) { score.eatDot() }         // 6,000
        assertEquals(6000, score.highScore)

        score.resetForNewGame()
        assertEquals("high score survives a new game", 6000, score.highScore)
        assertEquals(0, score.score)
    }

    @Test
    fun `resetting a game clears the ghost chain too`() {
        score.eatEnergizer()
        score.eatGhost()
        score.resetForNewGame()
        assertEquals(200, score.eatGhost())
    }
}
