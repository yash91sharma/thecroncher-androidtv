package com.yash.pacmantv.core.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Art is addressed by a stable [SpriteId] so no call site knows where it comes
 * from — procedurally drawn today, a PNG pack tomorrow. This test is what makes
 * swapping art safe: a source that forgets a sprite fails here, not on the TV.
 */
class SpriteSourceTest {

    private val source = ProceduralSpriteSource

    @Test
    fun `every sprite id resolves for every one of its frames`() {
        for (id in SpriteId.values()) {
            for (frame in 0 until id.frameCount) {
                val sprite = source.sprite(id, frame)
                assertTrue(
                    "$id frame $frame has no size",
                    sprite.width > 0 && sprite.height > 0,
                )
                assertEquals(
                    "$id frame $frame pixel buffer does not match its size",
                    sprite.width * sprite.height,
                    sprite.pixels.size,
                )
            }
        }
    }

    @Test
    fun `no sprite is entirely blank`() {
        for (id in SpriteId.values()) {
            for (frame in 0 until id.frameCount) {
                val sprite = source.sprite(id, frame)
                val visible = sprite.pixels.count { (it ushr 24) != 0 }
                assertTrue("$id frame $frame is fully transparent", visible > 0)
            }
        }
    }

    @Test
    fun `frame index wraps so animation code cannot crash on overflow`() {
        val a = source.sprite(SpriteId.PACMAN_RIGHT, 0)
        val b = source.sprite(SpriteId.PACMAN_RIGHT, SpriteId.PACMAN_RIGHT.frameCount)
        assertTrue(a.pixels.contentEquals(b.pixels))
    }

    @Test
    fun `negative frame index is handled rather than throwing`() {
        val sprite = source.sprite(SpriteId.PACMAN_RIGHT, -1)
        assertTrue(sprite.pixels.isNotEmpty())
    }

    @Test
    fun `pacman faces four different directions`() {
        val right = source.sprite(SpriteId.PACMAN_RIGHT, 1).pixels
        val left = source.sprite(SpriteId.PACMAN_LEFT, 1).pixels
        val up = source.sprite(SpriteId.PACMAN_UP, 1).pixels
        val down = source.sprite(SpriteId.PACMAN_DOWN, 1).pixels
        val distinct = listOf(right, left, up, down).map { it.toList() }.distinct()
        assertEquals("open-mouth frames must differ per direction", 4, distinct.size)
    }

    @Test
    fun `a closed pacman is a full circle so all directions share frame zero`() {
        val right = source.sprite(SpriteId.PACMAN_RIGHT, 0).pixels
        val up = source.sprite(SpriteId.PACMAN_UP, 0).pixels
        assertTrue("closed mouth should look the same whichever way he faces", right.contentEquals(up))
    }

    @Test
    fun `an energizer is bigger than a pellet`() {
        val pellet = source.sprite(SpriteId.PELLET, 0).pixels.count { (it ushr 24) != 0 }
        val energizer = source.sprite(SpriteId.ENERGIZER, 0).pixels.count { (it ushr 24) != 0 }
        assertTrue("energizer=$energizer pellet=$pellet", energizer > pellet)
    }
}
