package com.yash.thecroncher.core.theme

import com.yash.thecroncher.core.game.Toy
import com.yash.thecroncher.core.theme.themes.TheCroncherPalette
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Art is addressed by a stable [SpriteId] so no call site knows where it comes
 * from — hand-drawn grids today, a PNG pack tomorrow. This test is what makes
 * swapping art safe: a source that forgets a sprite fails here, not on the TV.
 *
 * It also holds the cast to its brief. The Croncher has to read as a cat being
 * chased by things cats hate, at sixteen pixels, from the sofa — so the shapes
 * must differ from one another, and the mouth has to actually open.
 */
class SpriteSourceTest {

    private val source = ThemeRegistry.default.sprites

    private fun visiblePixels(id: SpriteId, frame: Int = 0) =
        source.sprite(id, frame).pixels.count { (it ushr 24) != 0 }

    private fun colours(id: SpriteId, frame: Int = 0) =
        source.sprite(id, frame).pixels.filter { (it ushr 24) != 0 }.distinct()

    @Test
    fun `every sprite id resolves for every one of its frames`() {
        for (id in SpriteId.values()) {
            for (frame in 0 until id.frameCount) {
                val sprite = source.sprite(id, frame)
                assertTrue("$id frame $frame has no size", sprite.width > 0 && sprite.height > 0)
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
                assertTrue("$id frame $frame is fully transparent", visiblePixels(id, frame) > 0)
            }
        }
    }

    @Test
    fun `every sprite carries its own colours rather than waiting to be tinted`() {
        // The cast is multi-coloured now — a grey tabby, a brown dog, a green
        // cucumber — so nothing may come out as a flat white silhouette.
        for (id in SpriteId.values()) {
            assertTrue("$id is a single flat colour", colours(id).size >= 2)
        }
    }

    @Test
    fun `frame index wraps so animation code cannot crash on overflow`() {
        val a = source.sprite(SpriteId.CAT_RIGHT, 0)
        val b = source.sprite(SpriteId.CAT_RIGHT, SpriteId.CAT_RIGHT.frameCount)
        assertTrue(a.pixels.contentEquals(b.pixels))
    }

    @Test
    fun `negative frame index is handled rather than throwing`() {
        assertTrue(source.sprite(SpriteId.CAT_RIGHT, -1).pixels.isNotEmpty())
    }

    // ------------------------------------------------------------- the cat --

    @Test
    fun `the cat holds his expression and bounces instead of chewing`() {
        // A mouth opening and closing at sixteen pixels turns a cat into a shape
        // with a hole in it. He keeps his face; the walk is a one-pixel hop.
        for (id in CAT_FACING) {
            val still = source.sprite(id, 0)
            val hopped = source.sprite(id, 1)
            assertEquals(
                "$id loses pixels between frames, so something is animating",
                still.pixels.count { (it ushr 24) != 0 },
                hopped.pixels.count { (it ushr 24) != 0 },
            )
            assertTrue(
                "$id does not hop",
                PixelArt.shifted(still, dx = 0, dy = -1).pixels.contentEquals(hopped.pixels),
            )
        }
    }

    @Test
    fun `the cat faces four different ways`() {
        val distinct = CAT_FACING.map { source.sprite(it, 0).pixels.toList() }.distinct()
        assertEquals("each direction must have its own face", 4, distinct.size)
    }

    @Test
    fun `the cat has whiskers and a mouth on the faces that show one`() {
        val palette = TheCroncherPalette
        for (id in listOf(SpriteId.CAT_RIGHT, SpriteId.CAT_LEFT, SpriteId.CAT_DOWN)) {
            val used = colours(id)
            assertTrue("$id has no whiskers", palette.highlight in used)
            assertTrue("$id has no mouth", palette.catMouth in used)
            assertTrue("$id has no muzzle", palette.catMuzzle in used)
        }
    }

    @Test
    fun `the cat has ears, a pink nose and green eyes in every direction it faces`() {
        val palette = TheCroncherPalette
        for (id in CAT_FACING) {
            val used = colours(id, 0)
            assertTrue("$id has no fur", palette.catFur in used)
            assertTrue("$id has no ear lining", palette.catEarInner in used)
            // Facing away, the eyes and nose are on the far side of the head.
            if (id != SpriteId.CAT_UP) {
                assertTrue("$id has no eyes", palette.catEye in used)
                assertTrue("$id has no nose", palette.catNose in used)
            }
        }
    }

    @Test
    fun `fainting fades the cat away`() {
        val first = visiblePixels(SpriteId.CAT_FAINT, 0)
        val last = visiblePixels(SpriteId.CAT_FAINT, SpriteId.CAT_FAINT.frameCount - 1)
        assertTrue("the faint should thin out: $first -> $last", last < first / 2)
        assertTrue("the last frame must not be empty", last > 0)
    }

    // ------------------------------------------------------------ the foes --

    @Test
    fun `the four foes have four different silhouettes`() {
        val shapes = FoeCast.all.map { foe ->
            source.sprite(foe.right, 0).pixels.map { (it ushr 24) != 0 }
        }
        assertEquals("two foes are the same shape", 4, shapes.distinct().size)
    }

    @Test
    fun `each foe keeps its own colours so it is tellable apart at a glance`() {
        val signatures = FoeCast.all.map { colours(it.right).toSet() }
        assertEquals("two foes share an entire palette", 4, signatures.distinct().size)
    }

    @Test
    fun `a foe faces the way it is going`() {
        for (foe in FoeCast.all) {
            val right = source.sprite(foe.right, 0)
            val left = source.sprite(foe.left, 0)
            assertTrue(
                "${foe.id} looks identical going left and right",
                PixelArt.mirrored(right).pixels.contentEquals(left.pixels),
            )
        }
    }

    @Test
    fun `a frightened foe keeps its shape but turns harmless`() {
        for (foe in FoeCast.all) {
            val normal = source.sprite(foe.down, 0)
            val scared = source.sprite(foe.scared, 0)
            assertEquals(
                "${foe.id} changes shape when frightened",
                normal.pixels.count { (it ushr 24) != 0 },
                scared.pixels.count { (it ushr 24) != 0 },
            )
            assertTrue(
                "${foe.id} is not scared-blue when frightened",
                TheCroncherPalette.scaredBody in colours(foe.scared),
            )
            assertTrue(
                "${foe.id} does not flash white as the fright runs out",
                TheCroncherPalette.flashBody in colours(foe.scaredFlash),
            )
        }
    }

    // -------------------------------------------------------- treats, toys --

    @Test
    fun `the treats are four different shapes so the maze is not just dots`() {
        val shapes = TreatArt.shapes.map { source.sprite(it, 0).pixels.toList() }
        assertEquals("two treats look the same", TreatArt.shapes.size, shapes.distinct().size)
    }

    @Test
    fun `catnip is bigger than a treat, the way an energizer must be`() {
        for (treat in TreatArt.shapes) {
            assertTrue(
                "catnip is no bigger than $treat",
                visiblePixels(SpriteId.CATNIP) > visiblePixels(treat),
            )
        }
    }

    @Test
    fun `every bonus toy has its own art`() {
        val toys = Toy.values().map { source.sprite(it.sprite, 0).pixels.toList() }
        assertEquals("two toys share their art", Toy.values().size, toys.distinct().size)
    }

    private companion object {
        val CAT_FACING = listOf(
            SpriteId.CAT_RIGHT, SpriteId.CAT_LEFT, SpriteId.CAT_UP, SpriteId.CAT_DOWN,
        )
    }
}
