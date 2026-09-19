package com.yash.thecroncher.core.ui

import com.yash.thecroncher.core.ports.Align
import com.yash.thecroncher.core.ports.Gfx
import com.yash.thecroncher.core.theme.SpriteId
import com.yash.thecroncher.core.theme.Theme

/**
 * Draws any [MenuModel]. One renderer for every menu in the game, which is what
 * makes a new setting a one-line data change rather than new drawing code.
 */
object MenuRenderer {

    private const val TITLE_Y = 34
    private const val TITLE_SCALE = 2
    private const val ITEM_SPACING = 20

    /**
     * The items sit in a column down the middle rather than spanning the whole
     * width: on a 384-pixel-wide screen a row running edge to edge leaves the
     * label and its value too far apart to read as one row.
     */
    private const val LABEL_X = 116
    private const val VALUE_X = Layout.SCREEN_WIDTH - LABEL_X
    private const val CURSOR_X = LABEL_X - 18

    /** The item block is centred on this line, whatever the number of items. */
    private const val ITEMS_CENTRE_Y = 132

    fun render(
        g: Gfx,
        theme: Theme,
        model: MenuModel,
        tick: Long,
        footer: String? = null,
        clearBackground: Boolean = true,
    ) {
        frame(g, theme, model.title, footer, clearBackground)

        // Centre the block vertically so menus of different lengths stay balanced.
        val firstItemY = ITEMS_CENTRE_Y - (model.items.size * ITEM_SPACING) / 2

        for ((index, item) in model.items.withIndex()) {
            val y = firstItemY + index * ITEM_SPACING
            val selected = index == model.selectedIndex
            val colour = if (selected) theme.menu.itemSelected else theme.menu.item

            g.drawText(item.label, LABEL_X, y, colour)
            model.valueOf(index)?.let { value ->
                g.drawText(value, VALUE_X, y, colour, Align.RIGHT)
            }

            if (selected) {
                // A cat for a cursor, bouncing on the spot, because this is that
                // sort of game.
                val frame = ((tick / 12) % 2).toInt()
                g.drawSpriteCentred(
                    theme.sprites.sprite(SpriteId.CAT, frame), CURSOR_X, y + 3,
                )
                // A thin rule under the row it is sitting on, so the selection is
                // unmistakable across a room, cat or no cat. It stops at the row's
                // own content: running it to the value column under a bare "PLAY"
                // just draws a long line to nowhere.
                val ruleEnd =
                    if (model.valueOf(index) != null) VALUE_X
                    else LABEL_X + Font.measure(item.label) + 6
                g.fillRect(LABEL_X, y + Font.GLYPH_HEIGHT + 1, ruleEnd - LABEL_X, 1, theme.menu.cursor)
            }
        }

    }

    /**
     * The chrome every menu-like screen shares: background, title across the top,
     * hint line along the bottom. A screen whose middle is not a list of items —
     * the cat picker — draws its own middle inside this.
     */
    fun frame(
        g: Gfx,
        theme: Theme,
        title: String?,
        footer: String?,
        clearBackground: Boolean = true,
    ) {
        // The pause overlay draws the frozen game first and must not wipe it.
        if (clearBackground) g.clear(theme.menu.background)

        title?.let {
            g.drawText(
                it, Layout.SCREEN_WIDTH / 2, TITLE_Y, theme.menu.title, Align.CENTER, TITLE_SCALE,
            )
        }

        footer?.let {
            g.drawText(
                it, Layout.SCREEN_WIDTH / 2,
                Layout.SCREEN_HEIGHT - Layout.MARGIN - Font.GLYPH_HEIGHT,
                theme.menu.footer, Align.CENTER,
            )
        }
    }

}
