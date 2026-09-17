package com.yash.thecroncher.core.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * The four ghosts differ *only* in the tile they aim at. Everything else — the
 * pathfinding, the tie-breaks — is shared. Get these four rules right and the
 * ghosts feel like the arcade; get them wrong and the game is either trivial or
 * impossible.
 */
class GhostTargetTest {

    private val cat = TilePos(13, 23)
    private val chaserAt = TilePos(13, 11)

    private fun chase(kind: GhostKind, catDir: Direction, ghostAt: TilePos = TilePos(1, 1)) =
        GhostAi.chaseTarget(kind, cat, catDir, chaserAt, ghostAt)

    // ------------------------------------------------------------- Chaser --

    @Test
    fun `chaser simply aims at croncher`() {
        assertEquals(cat, chase(GhostKind.CHASER, Direction.LEFT))
        assertEquals(cat, chase(GhostKind.CHASER, Direction.UP))
    }

    // ----------------------------------------------------------- Ambusher --

    @Test
    fun `ambusher aims four tiles ahead`() {
        assertEquals(TilePos(9, 23), chase(GhostKind.AMBUSHER, Direction.LEFT))
        assertEquals(TilePos(17, 23), chase(GhostKind.AMBUSHER, Direction.RIGHT))
        assertEquals(TilePos(13, 27), chase(GhostKind.AMBUSHER, Direction.DOWN))
    }

    @Test
    fun `ambusher keeps the original up-direction overflow quirk`() {
        // The 1980 code added the offset to both axes when the croncher faced up. It is
        // a bug, it is famous, and removing it changes how the game plays — so it
        // stays.
        assertEquals(TilePos(9, 19), chase(GhostKind.AMBUSHER, Direction.UP))
    }

    // ------------------------------------------------------------ Flanker --

    @Test
    fun `flanker doubles the vector from chaser through the point ahead of croncher`() {
        // Two ahead of the croncher facing left is (11,23); doubling from the chaser at
        // (13,11) gives (2*11-13, 2*23-11) = (9,35).
        assertEquals(TilePos(9, 35), chase(GhostKind.FLANKER, Direction.LEFT))
    }

    @Test
    fun `flanker inherits the same up-direction quirk`() {
        // Two ahead facing up is (11,21) thanks to the overflow, so the doubled
        // vector from (13,11) is (9,31).
        assertEquals(TilePos(9, 31), chase(GhostKind.FLANKER, Direction.UP))
    }

    @Test
    fun `flanker's target moves when chaser moves`() {
        val a = GhostAi.chaseTarget(GhostKind.FLANKER, cat, Direction.LEFT, TilePos(13, 11), TilePos(1, 1))
        val b = GhostAi.chaseTarget(GhostKind.FLANKER, cat, Direction.LEFT, TilePos(20, 5), TilePos(1, 1))
        assertNotEquals("flanker must depend on chaser's position", a, b)
    }

    // ------------------------------------------------------------- Coward --

    @Test
    fun `coward chases while he is more than eight tiles away`() {
        val far = TilePos(1, 29)   // well over eight tiles from (13,23)
        assertEquals(cat, chase(GhostKind.COWARD, Direction.LEFT, ghostAt = far))
    }

    @Test
    fun `coward loses his nerve within eight tiles and heads for his corner`() {
        val near = TilePos(13, 25)
        assertEquals(Maze.SCATTER_COWARD, chase(GhostKind.COWARD, Direction.LEFT, ghostAt = near))
    }

    @Test
    fun `coward's eight-tile boundary is tested on both sides`() {
        // Exactly eight tiles away counts as "near" — the arcade compares against
        // eight, not more-than-eight.
        val exactlyEight = TilePos(5, 23)
        assertEquals(Maze.SCATTER_COWARD, chase(GhostKind.COWARD, Direction.LEFT, ghostAt = exactlyEight))

        val justOver = TilePos(4, 23)
        assertEquals(cat, chase(GhostKind.COWARD, Direction.LEFT, ghostAt = justOver))
    }

    // ------------------------------------------------------------ Scatter --

    @Test
    fun `each ghost scatters to its own corner`() {
        assertEquals(Maze.SCATTER_CHASER, GhostAi.scatterTarget(GhostKind.CHASER))
        assertEquals(Maze.SCATTER_AMBUSHER, GhostAi.scatterTarget(GhostKind.AMBUSHER))
        assertEquals(Maze.SCATTER_FLANKER, GhostAi.scatterTarget(GhostKind.FLANKER))
        assertEquals(Maze.SCATTER_COWARD, GhostAi.scatterTarget(GhostKind.COWARD))
    }

    @Test
    fun `the four scatter corners are all different`() {
        val corners = GhostKind.entries.map { GhostAi.scatterTarget(it) }
        assertEquals(4, corners.distinct().size)
    }
}
