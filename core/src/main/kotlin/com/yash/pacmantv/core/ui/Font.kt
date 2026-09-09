package com.yash.pacmantv.core.ui

/**
 * A 5x7 bitmap font, defined as ASCII art so the glyphs are legible in the source
 * and cheap to correct. Rendering is done by whoever implements `Gfx`; this class
 * only says which pixels are lit.
 *
 * Text lives in the 224x288 arcade framebuffer, so glyphs are tiny by design —
 * integer-scaled up to the television they stay perfectly crisp.
 */
object Font {

    const val GLYPH_WIDTH = 5
    const val GLYPH_HEIGHT = 7

    /** One blank column between characters. */
    const val ADVANCE = GLYPH_WIDTH + 1

    private val GLYPHS: Map<Char, Array<String>> = buildMap {
        put('A', arrayOf(" ### ", "#   #", "#   #", "#####", "#   #", "#   #", "#   #"))
        put('B', arrayOf("#### ", "#   #", "#   #", "#### ", "#   #", "#   #", "#### "))
        put('C', arrayOf(" ####", "#    ", "#    ", "#    ", "#    ", "#    ", " ####"))
        put('D', arrayOf("#### ", "#   #", "#   #", "#   #", "#   #", "#   #", "#### "))
        put('E', arrayOf("#####", "#    ", "#    ", "#### ", "#    ", "#    ", "#####"))
        put('F', arrayOf("#####", "#    ", "#    ", "#### ", "#    ", "#    ", "#    "))
        put('G', arrayOf(" ####", "#    ", "#    ", "#  ##", "#   #", "#   #", " ####"))
        put('H', arrayOf("#   #", "#   #", "#   #", "#####", "#   #", "#   #", "#   #"))
        put('I', arrayOf("#####", "  #  ", "  #  ", "  #  ", "  #  ", "  #  ", "#####"))
        put('J', arrayOf("    #", "    #", "    #", "    #", "#   #", "#   #", " ### "))
        put('K', arrayOf("#   #", "#  # ", "# #  ", "##   ", "# #  ", "#  # ", "#   #"))
        put('L', arrayOf("#    ", "#    ", "#    ", "#    ", "#    ", "#    ", "#####"))
        put('M', arrayOf("#   #", "## ##", "# # #", "#   #", "#   #", "#   #", "#   #"))
        put('N', arrayOf("#   #", "##  #", "# # #", "#  ##", "#   #", "#   #", "#   #"))
        put('O', arrayOf(" ### ", "#   #", "#   #", "#   #", "#   #", "#   #", " ### "))
        put('P', arrayOf("#### ", "#   #", "#   #", "#### ", "#    ", "#    ", "#    "))
        put('Q', arrayOf(" ### ", "#   #", "#   #", "#   #", "# # #", "#  # ", " ## #"))
        put('R', arrayOf("#### ", "#   #", "#   #", "#### ", "# #  ", "#  # ", "#   #"))
        put('S', arrayOf(" ####", "#    ", "#    ", " ### ", "    #", "    #", "#### "))
        put('T', arrayOf("#####", "  #  ", "  #  ", "  #  ", "  #  ", "  #  ", "  #  "))
        put('U', arrayOf("#   #", "#   #", "#   #", "#   #", "#   #", "#   #", " ### "))
        put('V', arrayOf("#   #", "#   #", "#   #", "#   #", "#   #", " # # ", "  #  "))
        put('W', arrayOf("#   #", "#   #", "#   #", "#   #", "# # #", "## ##", "#   #"))
        put('X', arrayOf("#   #", "#   #", " # # ", "  #  ", " # # ", "#   #", "#   #"))
        put('Y', arrayOf("#   #", "#   #", " # # ", "  #  ", "  #  ", "  #  ", "  #  "))
        put('Z', arrayOf("#####", "    #", "   # ", "  #  ", " #   ", "#    ", "#####"))

        put('0', arrayOf(" ### ", "#   #", "#  ##", "# # #", "##  #", "#   #", " ### "))
        put('1', arrayOf("  #  ", " ##  ", "  #  ", "  #  ", "  #  ", "  #  ", " ### "))
        put('2', arrayOf(" ### ", "#   #", "    #", "   # ", "  #  ", " #   ", "#####"))
        put('3', arrayOf("#####", "   # ", "  #  ", "   # ", "    #", "#   #", " ### "))
        put('4', arrayOf("   # ", "  ## ", " # # ", "#  # ", "#####", "   # ", "   # "))
        put('5', arrayOf("#####", "#    ", "#### ", "    #", "    #", "#   #", " ### "))
        put('6', arrayOf("  ## ", " #   ", "#    ", "#### ", "#   #", "#   #", " ### "))
        put('7', arrayOf("#####", "    #", "   # ", "  #  ", " #   ", " #   ", " #   "))
        put('8', arrayOf(" ### ", "#   #", "#   #", " ### ", "#   #", "#   #", " ### "))
        put('9', arrayOf(" ### ", "#   #", "#   #", " ####", "    #", "   # ", " ##  "))

        put(' ', arrayOf("     ", "     ", "     ", "     ", "     ", "     ", "     "))
        put('.', arrayOf("     ", "     ", "     ", "     ", "     ", " ##  ", " ##  "))
        put(',', arrayOf("     ", "     ", "     ", "     ", " ##  ", " ##  ", " #   "))
        put(':', arrayOf("     ", " ##  ", " ##  ", "     ", " ##  ", " ##  ", "     "))
        put('-', arrayOf("     ", "     ", "     ", "#####", "     ", "     ", "     "))
        put('_', arrayOf("     ", "     ", "     ", "     ", "     ", "     ", "#####"))
        put('/', arrayOf("    #", "    #", "   # ", "  #  ", " #   ", "#    ", "#    "))
        put('!', arrayOf("  #  ", "  #  ", "  #  ", "  #  ", "  #  ", "     ", "  #  "))
        put('?', arrayOf(" ### ", "#   #", "    #", "   # ", "  #  ", "     ", "  #  "))
        put('\'', arrayOf("  #  ", "  #  ", "     ", "     ", "     ", "     ", "     "))
        put('(', arrayOf("   # ", "  #  ", " #   ", " #   ", " #   ", "  #  ", "   # "))
        put(')', arrayOf(" #   ", "  #  ", "   # ", "   # ", "   # ", "  #  ", " #   "))
        put('<', arrayOf("   # ", "  #  ", " #   ", "#    ", " #   ", "  #  ", "   # "))
        put('>', arrayOf(" #   ", "  #  ", "   # ", "    #", "   # ", "  #  ", " #   "))
        put('%', arrayOf("##  #", "##  #", "   # ", "  #  ", " #   ", "#  ##", "#  ##"))
        put('+', arrayOf("     ", "  #  ", "  #  ", "#####", "  #  ", "  #  ", "     "))
        put('=', arrayOf("     ", "     ", "#####", "     ", "#####", "     ", "     "))
        put('*', arrayOf("     ", "#   #", " # # ", "  #  ", " # # ", "#   #", "     "))
    }

    /** Anything the font does not know is drawn as a hollow box, not dropped. */
    private val FALLBACK =
        arrayOf("#####", "#   #", "#   #", "#   #", "#   #", "#   #", "#####")

    fun glyph(c: Char): Array<String> = GLYPHS[c.uppercaseChar()] ?: FALLBACK

    fun hasGlyph(c: Char): Boolean = GLYPHS.containsKey(c.uppercaseChar())

    /** Rendered width of [text] in framebuffer pixels, excluding the final gap. */
    fun measure(text: String): Int =
        if (text.isEmpty()) 0 else text.length * ADVANCE - 1

    /** Left edge at which to start drawing so [text] sits correctly against [x]. */
    fun originFor(text: String, x: Int, align: com.yash.pacmantv.core.ports.Align): Int =
        when (align) {
            com.yash.pacmantv.core.ports.Align.LEFT -> x
            com.yash.pacmantv.core.ports.Align.CENTER -> x - measure(text) / 2
            com.yash.pacmantv.core.ports.Align.RIGHT -> x - measure(text)
        }

    /**
     * Walks the lit pixels of [text], calling [plot] with framebuffer coordinates.
     * Renderers use this so glyph layout lives in exactly one place.
     */
    inline fun forEachPixel(
        text: String,
        x: Int,
        y: Int,
        align: com.yash.pacmantv.core.ports.Align,
        plot: (Int, Int) -> Unit,
    ) {
        var cursor = originFor(text, x, align)
        for (c in text) {
            val rows = glyph(c)
            for (row in 0 until GLYPH_HEIGHT) {
                val line = rows[row]
                for (col in 0 until GLYPH_WIDTH) {
                    if (col < line.length && line[col] != ' ') plot(cursor + col, y + row)
                }
            }
            cursor += ADVANCE
        }
    }
}
