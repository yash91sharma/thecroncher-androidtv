package com.yash.thecroncher.core.game

import com.yash.thecroncher.core.ports.SeededRng
import com.yash.thecroncher.core.ports.RecordingAudioOut
import com.yash.thecroncher.core.ports.SoundEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Energizers: the one moment the game turns around. Getting the duration, the
 * flashing warning and the eaten-ghost handling right is what makes the power
 * pellet feel like a weapon rather than a formality.
 */
class FrightModeTest {

    private val audio = RecordingAudioOut()

    private fun game(difficulty: Difficulty = Difficulties.NORMAL) = GameState(
        maze = Maze.loadDefault(),
        difficulty = difficulty,
        rng = SeededRng(7),
        audio = audio,
    ).apply {
        startNewGame()
        repeat(GameState.READY_TICKS) { tick() }
    }

    /**
     * Walks the croncher onto an energizer and eats it, then parks him nose-first
     * against the wall above his start pocket. Without the parking he runs on down
     * the corridor and eats the *next* energizer, quietly restarting the fright he
     * is supposed to be timing.
     */
    private fun GameState.takeEnergizer() {
        val spot = maze.energizerPositions.first()
        croncher.placeAtTileCentre(spot, Direction.DOWN)
        tick()
        croncher.placeAtTileCentre(Maze.CRONCHER_START_TILE, Direction.UP)
    }

    @Test
    fun `eating an energizer frightens every ghost that is out`() {
        val g = game()
        g.takeEnergizer()
        val outside = g.ghosts.filterNot { it.mode.isInsideHouse }
        assertTrue("expected some ghosts outside", outside.isNotEmpty())
        assertTrue(outside.all { it.mode == GhostMode.FRIGHTENED })
    }

    @Test
    fun `frightened ghosts are edible and normal ones are not`() {
        val g = game()
        g.takeEnergizer()
        assertTrue(g.ghosts.filterNot { it.mode.isInsideHouse }.all { it.mode.isEdible })

        repeat(GameState.TICKS_PER_SECOND * 12) { g.tick() }
        assertTrue(g.ghosts.none { it.mode.isEdible })
    }

    @Test
    fun `the energizer makes ghosts turn around`() {
        val g = game()
        val chaser = g.ghost(GhostKind.CHASER)
        val before = chaser.direction
        g.takeEnergizer()
        assertNotEquals("chaser should have reversed", before, chaser.direction)
    }

    @Test
    fun `fright lasts six seconds on normal at level one`() {
        val g = game(Difficulties.NORMAL)
        g.takeEnergizer()
        repeat(GameState.TICKS_PER_SECOND * 6 - 2) { g.tick() }
        assertTrue("should still be frightened", g.frightTicksRemaining > 0)
        repeat(4) { g.tick() }
        assertEquals(0, g.frightTicksRemaining)
    }

    @Test
    fun `easy gives nine seconds and hard only three`() {
        val easy = game(Difficulties.EASY)
        easy.takeEnergizer()
        assertEquals(9 * GameState.TICKS_PER_SECOND, easy.frightTicksRemaining)

        val hard = game(Difficulties.HARD)
        hard.takeEnergizer()
        assertEquals(3 * GameState.TICKS_PER_SECOND, hard.frightTicksRemaining)
    }

    @Test
    fun `the blue ghosts flash as a warning before time runs out`() {
        val g = game(Difficulties.EASY)
        g.takeEnergizer()
        assertFalse("no flashing at the start", g.isFrightFlashing)

        while (g.frightTicksRemaining > GameState.TICKS_PER_SECOND * 2) g.tick()
        assertTrue("should be flashing near the end", g.isFrightFlashing)
    }

    @Test
    fun `an energizer plays its sound`() {
        val g = game()
        audio.played.clear()
        g.takeEnergizer()
        assertTrue(SoundEvent.POWER_PELLET in audio.played)
    }

    @Test
    fun `a second energizer restarts the clock`() {
        val g = game(Difficulties.EASY)
        g.takeEnergizer()
        repeat(GameState.TICKS_PER_SECOND * 4) { g.tick() }
        val partway = g.frightTicksRemaining
        assertTrue(partway < 9 * GameState.TICKS_PER_SECOND)

        val second = g.maze.energizerPositions[1]
        g.croncher.placeAtTileCentre(second, Direction.DOWN)
        g.tick()
        assertEquals(9 * GameState.TICKS_PER_SECOND, g.frightTicksRemaining)
    }

    @Test
    fun `on a late level the energizer gives no blue time at all`() {
        val g = game(Difficulties.NORMAL)
        g.debugSetLevel(19)
        g.takeEnergizer()
        assertEquals(0, g.frightTicksRemaining)
        assertTrue("no ghost should turn blue", g.ghosts.none { it.mode == GhostMode.FRIGHTENED })
    }

    @Test
    fun `eating an energizer increases croncher speed to fright speed`() {
        val g = game(Difficulties.NORMAL)
        val normalSpeed = speedOf(LevelTable.forLevel(1).croncherSpeed)
        val frightSpeed = speedOf(LevelTable.forLevel(1).croncherFrightSpeed)
        assertEquals(normalSpeed, g.croncher.speed)

        g.takeEnergizer()
        assertEquals(frightSpeed, g.croncher.speed)

        repeat(GameState.TICKS_PER_SECOND * 7) { g.tick() }
        assertEquals(0, g.frightTicksRemaining)
        assertEquals(normalSpeed, g.croncher.speed)
    }
}
