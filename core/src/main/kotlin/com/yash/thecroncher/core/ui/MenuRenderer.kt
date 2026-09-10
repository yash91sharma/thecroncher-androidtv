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

    private const val TITLE_Y = 44
    private const val TITLE_SCALE = 2
    private const val ITEM_SPACING = 20
    private const val LABEL_X = 48
    private const val CURSOR_X = 30

    /** Values are right-aligned close to the edge, so a long label cannot collide. */
    private const val VALUE_X = Layout.SCREEN_WIDTH - 24

    /** The item block is centred on this line, whatever the number of items. */
    private const val ITEMS_CENTRE_Y = 156

    fun render(
        g: Gfx,
        theme: Theme,
        model: MenuModel,
        tick: Long,
        footer: String? = null,
        clearBackground: Boolean = true,
    ) {
        // The pause overlay draws the frozen game first and must not wipe it.
        if (clearBackground) g.clear(theme.menu.background)

        model.title?.let {
            g.drawText(
                it, Layout.SCREEN_WIDTH / 2, TITLE_Y, theme.menu.title, Align.CENTER, TITLE_SCALE,
            )
        }

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
                    theme.sprites.sprite(SpriteId.CAT_RIGHT, frame), CURSOR_X, y + 3,
                )
                // A thin rule under the row it is sitting on, so the selection is
                // unmistakable across a room, cat or no cat.
                g.fillRect(LABEL_X, y + Font.GLYPH_HEIGHT + 1, RULE_WIDTH, 1, theme.menu.cursor)
            }
        }

        footer?.let {
            g.drawText(
                it, Layout.SCREEN_WIDTH / 2, Layout.SCREEN_HEIGHT - 24,
                theme.menu.footer, Align.CENTER,
            )
        }
    }

    private const val RULE_WIDTH = VALUE_X - LABEL_X
}
