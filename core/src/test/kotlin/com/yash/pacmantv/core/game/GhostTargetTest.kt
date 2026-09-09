package com.yash.pacmantv.core.game

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

    private val pac = TilePos(13, 23)
    private val blinkyAt = TilePos(13, 11)

    private fun chase(kind: GhostKind, pacDir: Direction, ghostAt: TilePos = TilePos(1, 1)) =
        GhostAi.chaseTarget(kind, pac, pacDir, blinkyAt, ghostAt)

    // ------------------------------------------------------------- Blinky --

    @Test
    fun `blinky simply aims at pacman`() {
        assertEquals(pac, chase(GhostKind.BLINKY, Direction.LEFT))
        assertEquals(pac, chase(GhostKind.BLINKY, Direction.UP))
    }

    // -------------------------------------------------------------- Pinky --

    @Test
    fun `pinky aims four tiles ahead`() {
        assertEquals(TilePos(9, 23), chase(GhostKind.PINKY, Direction.LEFT))
        assertEquals(TilePos(17, 23), chase(GhostKind.PINKY, Direction.RIGHT))
        assertEquals(TilePos(13, 27), chase(GhostKind.PINKY, Direction.DOWN))
    }

    @Test
    fun `pinky keeps the original up-direction overflow quirk`() {
        // The 1980 code added the offset to both axes when Pac-Man faced up. It is
        // a bug, it is famous, and removing it changes how the game plays — so it
        // stays.
        assertEquals(TilePos(9, 19), chase(GhostKind.PINKY, Direction.UP))
    }

    // --------------------------------------------------------------- Inky --

    @Test
    fun `inky doubles the vector from blinky through the point ahead of pacman`() {
        // Two ahead of Pac-Man facing left is (11,23); doubling from Blinky at
        // (13,11) gives (2*11-13, 2*23-11) = (9,35).
        assertEquals(TilePos(9, 35), chase(GhostKind.INKY, Direction.LEFT))
    }

    @Test
    fun `inky inherits the same up-direction quirk`() {
        // Two ahead facing up is (11,21) thanks to the overflow, so the doubled
        // vector from (13,11) is (9,31).
        assertEquals(TilePos(9, 31), chase(GhostKind.INKY, Direction.UP))
    }

    @Test
    fun `inky's target moves when blinky moves`() {
        val a = GhostAi.chaseTarget(GhostKind.INKY, pac, Direction.LEFT, TilePos(13, 11), TilePos(1, 1))
        val b = GhostAi.chaseTarget(GhostKind.INKY, pac, Direction.LEFT, TilePos(20, 5), TilePos(1, 1))
        assertNotEquals("inky must depend on blinky's position", a, b)
    }

    // -------------------------------------------------------------- Clyde --

    @Test
    fun `clyde chases while he is more than eight tiles away`() {
        val far = TilePos(1, 29)   // well over eight tiles from (13,23)
        assertEquals(pac, chase(GhostKind.CLYDE, Direction.LEFT, ghostAt = far))
    }

    @Test
    fun `clyde loses his nerve within eight tiles and heads for his corner`() {
        val near = TilePos(13, 25)
        assertEquals(Maze.SCATTER_CLYDE, chase(GhostKind.CLYDE, Direction.LEFT, ghostAt = near))
    }

    @Test
    fun `clyde's eight-tile boundary is tested on both sides`() {
        // Exactly eight tiles away counts as "near" — the arcade compares against
        // eight, not more-than-eight.
        val exactlyEight = TilePos(5, 23)
        assertEquals(Maze.SCATTER_CLYDE, chase(GhostKind.CLYDE, Direction.LEFT, ghostAt = exactlyEight))

        val justOver = TilePos(4, 23)
        assertEquals(pac, chase(GhostKind.CLYDE, Direction.LEFT, ghostAt = justOver))
    }

    // ------------------------------------------------------------ Scatter --

    @Test
    fun `each ghost scatters to its own corner`() {
        assertEquals(Maze.SCATTER_BLINKY, GhostAi.scatterTarget(GhostKind.BLINKY))
        assertEquals(Maze.SCATTER_PINKY, GhostAi.scatterTarget(GhostKind.PINKY))
        assertEquals(Maze.SCATTER_INKY, GhostAi.scatterTarget(GhostKind.INKY))
        assertEquals(Maze.SCATTER_CLYDE, GhostAi.scatterTarget(GhostKind.CLYDE))
    }

    @Test
    fun `the four scatter corners are all different`() {
        val corners = GhostKind.entries.map { GhostAi.scatterTarget(it) }
        assertEquals(4, corners.distinct().size)
    }
}
