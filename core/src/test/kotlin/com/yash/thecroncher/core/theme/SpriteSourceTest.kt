package com.yash.thecroncher.core.theme

import com.yash.thecroncher.core.game.Toy
import com.yash.thecroncher.core.theme.cats.CatFaces
import com.yash.thecroncher.core.theme.cats.CatRegistry
import com.yash.thecroncher.core.theme.cats.OrangeTabby
import com.yash.thecroncher.core.theme.cats.Siamese
import com.yash.thecroncher.core.theme.cats.Sphynx
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
        val a = source.sprite(SpriteId.CAT, 0)
        val b = source.sprite(SpriteId.CAT, SpriteId.CAT.frameCount)
        assertTrue(a.pixels.contentEquals(b.pixels))
    }

    @Test
    fun `negative frame index is handled rather than throwing`() {
        assertTrue(source.sprite(SpriteId.CAT, -1).pixels.isNotEmpty())
    }

    // ------------------------------------------------------------- the cat --

    @Test
    fun `the cat holds his expression and bounces instead of chewing`() {
        // A mouth opening and closing at sixteen pixels turns a cat into a shape
        // with a hole in it. He keeps his face; the walk is a one-pixel hop.
        val still = source.sprite(SpriteId.CAT, 0)
        val hopped = source.sprite(SpriteId.CAT, 1)
        assertEquals(
            "the cat loses pixels between frames, so something is animating",
            still.pixels.count { (it ushr 24) != 0 },
            hopped.pixels.count { (it ushr 24) != 0 },
        )
        assertTrue(
            "the cat does not hop",
            PixelArt.shifted(still, dx = 0, dy = -1).pixels.contentEquals(hopped.pixels),
        )
    }

    @Test
    fun `the cat looks at the player, not along his direction of travel`() {
        // There is one face and it is the character. A profile or a back-of-head
        // at this size read as a shape with a snout, not as a cat.
        val perDirection = SpriteId.values().filter {
            it.name.removePrefix("CAT_") in setOf("UP", "DOWN", "LEFT", "RIGHT")
        }
        assertTrue("the cat still has per-direction faces: $perDirection", perDirection.isEmpty())
    }

    @Test
    fun `the cat has ears, a pink nose, green eyes, whiskers and a closed mouth`() {
        val cat = CatRegistry.default
        val used = colours(SpriteId.CAT)
        assertTrue("no fur", cat.fur in used)
        assertTrue("no ear lining", cat.earInner in used)
        assertTrue("no eyes", cat.eye in used)
        assertTrue("no pupils", cat.pupil in used)
        assertTrue("no nose", cat.nose in used)
        assertTrue("no mouth", cat.mouth in used)
        assertTrue("no whiskers", TheCroncherPalette.highlight in used)
    }

    @Test
    fun `the default source draws the default cat`() {
        assertTrue(CatRegistry.default.fur in colours(SpriteId.CAT))
        assertTrue(OrangeTabby.fur !in colours(SpriteId.CAT))
    }

    @Test
    fun `a source for another breed draws that breed everywhere the cat appears`() {
        val orange = source.forCat(OrangeTabby)
        for (id in listOf(SpriteId.CAT, SpriteId.LIFE_ICON, SpriteId.CAT_FAINT)) {
            val used = orange.sprite(id, 0).pixels.filter { (it ushr 24) != 0 }.distinct()
            assertTrue("$id is not drawn in orange fur", OrangeTabby.fur in used)
            assertTrue("$id still has grey fur", CatRegistry.default.fur !in used)
        }
    }

    @Test
    fun `the rest of the cast is untouched by the choice of cat`() {
        val orange = source.forCat(OrangeTabby)
        for (id in SpriteId.values()) {
            if (id.name.startsWith("CAT") || id == SpriteId.LIFE_ICON) continue
            assertTrue(
                "$id changed when the cat did",
                source.sprite(id, 0).pixels.contentEquals(orange.sprite(id, 0).pixels),
            )
        }
    }

    @Test
    fun `every breed has a face, a hop and a faint`() {
        for (cat in CatRegistry.all) {
            val s = source.forCat(cat)
            for (id in listOf(SpriteId.CAT, SpriteId.LIFE_ICON, SpriteId.CAT_FAINT)) {
                for (frame in 0 until id.frameCount) {
                    assertTrue(
                        "${cat.id} has no $id frame $frame",
                        s.sprite(id, frame).pixels.any { (it ushr 24) != 0 },
                    )
                }
            }
            val still = s.sprite(SpriteId.CAT, 0)
            val hopped = s.sprite(SpriteId.CAT, 1)
            assertTrue(
                "${cat.id} does not hop",
                PixelArt.shifted(still, dx = 0, dy = -1).pixels.contentEquals(hopped.pixels),
            )
        }
    }

    @Test
    fun `fainting crosses out the eyes of every breed`() {
        // The dizzy overlay is painted at fixed rows, so a breed whose eyes sit
        // elsewhere would faint with its eyes open. Frame 0 is before any fading.
        for (cat in CatRegistry.all) {
            val awake = source.forCat(cat).sprite(SpriteId.CAT, 0)
            val dizzy = source.forCat(cat).sprite(SpriteId.CAT_FAINT, 0)
            assertTrue("${cat.id} has eyes open while fainting", cat.eye !in dizzy.pixels)
            assertTrue("${cat.id} lost its shape while fainting",
                awake.pixels.map { (it ushr 24) != 0 } == dizzy.pixels.map { (it ushr 24) != 0 })
        }
    }

    @Test
    fun `the crossed-out eyes stand out from the fur of every breed`() {
        // Drawn in the pupil colour, the X vanishes on a black cat; drawn in
        // white, it vanishes on a cream one. Whatever is chosen must read.
        fun luma(c: Int) = ((c shr 16) and 0xFF) + ((c shr 8) and 0xFF) + (c and 0xFF)
        for (cat in CatRegistry.all) {
            val dizzy = source.forCat(cat).sprite(SpriteId.CAT_FAINT, 0)
            val faceTop = (dizzy.height - cat.face.size) / 2
            for ((y, row) in CatFaces.DIZZY.withIndex()) {
                for ((x, ink) in row.withIndex()) {
                    if (ink != 'P') continue
                    val drawn = dizzy.pixelAt(x, y + faceTop)
                    assertTrue(
                        "${cat.id} dizzy X at $x,$y is lost against the fur",
                        kotlin.math.abs(luma(drawn) - luma(cat.fur)) >= 0x90,
                    )
                }
            }
        }
    }

    @Test
    fun `the sphynx has no whiskers and the siamese keeps its mask`() {
        val sphynx = source.forCat(Sphynx).sprite(SpriteId.CAT, 0)
        assertTrue(TheCroncherPalette.highlight !in sphynx.pixels)
        val siamese = source.forCat(Siamese).sprite(SpriteId.CAT, 0)
        assertTrue(Siamese.marking in siamese.pixels)
        assertTrue(Siamese.fur in siamese.pixels)
    }

    @Test
    fun `the whiskers stick out past the sides of his head`() {
        // Whiskers painted on the cheeks disappear into the fur. They have to
        // leave the silhouette to read as whiskers at all: in any row that has
        // them, a whisker pixel lies outside the head, and at least one reaches
        // the edge of the sprite. The sphynx is exempt because it has none.
        val whisker = TheCroncherPalette.highlight
        for (cat in CatRegistry.all.filter { it != Sphynx }) {
            val sprite = source.forCat(cat).sprite(SpriteId.CAT, 0)
            var reachedEdge = false
            for (y in 0 until sprite.height) {
                val row = (0 until sprite.width).map { sprite.pixelAt(it, y) }
                val head = row.withIndex().filter { (_, p) -> p != Sprite.TRANSPARENT && p != whisker }
                if (head.isEmpty()) continue
                val headLeft = head.first().index
                val headRight = head.last().index
                for ((x, p) in row.withIndex()) {
                    if (p != whisker) continue
                    assertTrue("${cat.id} whisker at $x,$y is inside the head", x < headLeft || x > headRight)
                    if (x == 0 || x == sprite.width - 1) reachedEdge = true
                }
            }
            assertTrue("${cat.id} whiskers do not reach the edge of the sprite", reachedEdge)
        }
    }

    @Test
    fun `the cat has headroom to hop without losing his ear tips`() {
        // The face fills its sixteen rows, so the one-pixel hop needs a spare row
        // above it — and one below, so he stays centred on his tile.
        for (cat in CatRegistry.all) {
            val still = source.forCat(cat).sprite(SpriteId.CAT, 0)
            assertEquals(cat.face.size + 2, still.height)
            assertTrue("${cat.id} top row is not empty", (0 until still.width).all { still.pixelAt(it, 0) == Sprite.TRANSPARENT })
            assertTrue("${cat.id} bottom row is not empty", (0 until still.width).all { still.pixelAt(it, still.height - 1) == Sprite.TRANSPARENT })
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
                "${foe.id} does not take the scared tint when frightened",
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

}
