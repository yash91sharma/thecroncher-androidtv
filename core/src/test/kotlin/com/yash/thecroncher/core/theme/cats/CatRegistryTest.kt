package com.yash.thecroncher.core.theme.cats

import com.yash.thecroncher.core.theme.PixelArt
import com.yash.thecroncher.core.theme.Sprite
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The cats are an extension point in the same way themes are: adding one is a new
 * file plus a registry line. These tests are what make that cheap — a breed with a
 * mistyped grid or a forgotten colour fails here, not on the television.
 */
class CatRegistryTest {

    @Test
    fun `the grey tabby is the default, and the roster has six`() {
        assertEquals("grey-tabby", CatRegistry.default.id)
        assertEquals(6, CatRegistry.all.size)
        assertEquals(CatRegistry.default, CatRegistry.all.first())
    }

    @Test
    fun `breed ids are unique and non-blank`() {
        val ids = CatRegistry.all.map { it.id }
        assertEquals(ids.size, ids.distinct().size)
        assertTrue(ids.all { it.isNotBlank() })
    }

    @Test
    fun `lookup by id works and unknown ids fall back to the default`() {
        val last = CatRegistry.all.last()
        assertNotNull(CatRegistry.byId(last.id))
        assertNull(CatRegistry.byId("no-such-cat"))
        assertEquals(CatRegistry.default, CatRegistry.byIdOrDefault("no-such-cat"))
        assertEquals(CatRegistry.default, CatRegistry.byIdOrDefault(null))
        assertEquals(last, CatRegistry.byIdOrDefault(last.id))
    }

    @Test
    fun `every breed is drawn in the sixteen pixel box the maze was built for`() {
        // The corridor is eight pixels and the foes are sixteen; a bigger cat
        // sits on the walls and dwarfs the things chasing it.
        for (cat in CatRegistry.all) {
            assertEquals("${cat.id} is not 16 rows", CatBreed.SIZE, cat.face.size)
            for ((y, row) in cat.face.withIndex()) {
                assertEquals("${cat.id} row $y is not 16 wide", CatBreed.SIZE, row.length)
            }
        }
    }

    @Test
    fun `every breed uses only the cat inks, so its grid can be read`() {
        for (cat in CatRegistry.all) {
            val sprite = PixelArt.sprite(cat.face, cat.ink + ('i' to 0xFFFFFFFF.toInt()))
            assertTrue("${cat.id} draws nothing", sprite.pixels.any { it != Sprite.TRANSPARENT })
        }
    }

    @Test
    fun `every colour in every breed is fully opaque`() {
        for (cat in CatRegistry.all) {
            for ((slot, colour) in cat.allColours()) {
                assertEquals("${cat.id}.$slot is not opaque", 0xFF, colour ushr 24)
            }
        }
    }

    @Test
    fun `every breed has eyes, a nose and a mouth`() {
        // A face without them is a blob; these are what make it a cat from the sofa.
        for (cat in CatRegistry.all) {
            val inks = cat.face.joinToString("").toSet()
            assertTrue("${cat.id} has no eyes", 'Y' in inks && 'P' in inks)
            assertTrue("${cat.id} has no nose", 'N' in inks)
            assertTrue("${cat.id} has no mouth", 'M' in inks)
            assertTrue("${cat.id} has no ears", 'E' in inks)
        }
    }

    @Test
    fun `the six faces are six different pictures`() {
        val pictures = CatRegistry.all.map { cat ->
            PixelArt.sprite(cat.face, cat.ink + ('i' to 0xFFFFFFFF.toInt())).pixels.toList()
        }
        assertEquals("two breeds look identical", CatRegistry.all.size, pictures.distinct().size)
    }

    @Test
    fun `no breed is pure white or as dark as the background`() {
        // OLED burn-in guard on one side, visibility on the other: the life icons
        // sit still for a long time, and a black cat on a black maze is no cat.
        for (cat in CatRegistry.all) {
            assertTrue("${cat.id} fur is pure white", cat.fur != 0xFFFFFFFF.toInt())
            val luma = ((cat.fur shr 16) and 0xFF) + ((cat.fur shr 8) and 0xFF) + (cat.fur and 0xFF)
            assertTrue("${cat.id} fur is too dark to see", luma > 0x60)
        }
    }
}
