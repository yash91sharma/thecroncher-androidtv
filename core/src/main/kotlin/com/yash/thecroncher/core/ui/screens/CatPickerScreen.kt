package com.yash.thecroncher.core.ui.screens

import com.yash.thecroncher.core.game.Direction
import com.yash.thecroncher.core.input.Button
import com.yash.thecroncher.core.input.InputEvent
import com.yash.thecroncher.core.ports.Gfx
import com.yash.thecroncher.core.theme.PixelArt
import com.yash.thecroncher.core.theme.Sprite
import com.yash.thecroncher.core.theme.SpriteId
import com.yash.thecroncher.core.theme.Theme
import com.yash.thecroncher.core.theme.cats.CatBreed
import com.yash.thecroncher.core.theme.cats.CatRegistry
import com.yash.thecroncher.core.ui.GameSettings
import com.yash.thecroncher.core.ui.Layout
import com.yash.thecroncher.core.ui.MenuRenderer
import com.yash.thecroncher.core.ui.Screen
import com.yash.thecroncher.core.ui.Strings
import com.yash.thecroncher.core.ui.Transition

/**
 * SELECT MY CAT: every breed's face in a grid, no names. The player moves the
 * cursor across the faces, and A makes that cat theirs; B leaves things as
 * they were.
 *
 * The roster is [CatRegistry.all] and the grid is shaped by [Layout], so a new
 * breed appears here with no change to this file.
 */
class CatPickerScreen(private val settings: GameSettings) : Screen {

    private val cats = CatRegistry.all

    private var index = cats.indexOf(settings.cat).coerceAtLeast(0)

    /** The cat under the cursor — not yet the player's until they press A. */
    val highlighted: CatBreed get() = cats[index]

    // Each face is the theme dressed as that breed, blown up once and kept: both
    // hop frames, so nothing is rasterised while the cursor is bouncing.
    private val faces = HashMap<CatBreed, List<Sprite>>()

    override fun handle(event: InputEvent): Transition = when (event) {
        is InputEvent.Move -> {
            when (event.direction) {
                Direction.LEFT -> move(-1)
                Direction.RIGHT -> move(1)
                Direction.UP -> move(-Layout.CAT_PICKER_COLUMNS)
                Direction.DOWN -> move(Layout.CAT_PICKER_COLUMNS)
            }
            Transition.None
        }

        is InputEvent.Press -> when (event.button) {
            Button.CONFIRM -> {
                settings.cat = highlighted
                Transition.Pop
            }
            Button.BACK -> Transition.Pop
            Button.PAUSE -> Transition.None
        }
    }

    /** Wraps, so the D-pad never runs into a wall on a grid this small. */
    private fun move(delta: Int) {
        index = ((index + delta) % cats.size + cats.size) % cats.size
    }

    override fun render(g: Gfx, theme: Theme, tick: Long) {
        MenuRenderer.frame(g, theme, Strings.SELECT_MY_CAT, footer = "A SELECT   B BACK")

        for ((i, cat) in cats.withIndex()) {
            val column = i % Layout.CAT_PICKER_COLUMNS
            val row = i / Layout.CAT_PICKER_COLUMNS
            val cx = Layout.SCREEN_WIDTH / 2 +
                (column - (Layout.CAT_PICKER_COLUMNS - 1) / 2) * Layout.CAT_PICKER_CELL_WIDTH
            val cy = Layout.CAT_PICKER_FIRST_ROW_Y + row * Layout.CAT_PICKER_ROW_HEIGHT
            val selected = i == index

            // The cat under the cursor bounces on the spot, like the menu cursor.
            val frame = if (selected) ((tick / 12) % 2).toInt() else 0
            g.drawSpriteCentred(face(theme, cat, frame), cx, cy)

            if (selected) {
                val width = CatBreed.SIZE * Layout.CAT_PICKER_SCALE
                g.fillRect(
                    cx - width / 2, cy + Layout.CAT_PICKER_RULE_OFFSET_Y, width, 1, theme.menu.cursor,
                )
            }
        }
    }

    private fun face(theme: Theme, cat: CatBreed, frame: Int): Sprite =
        faces.getOrPut(cat) {
            val source = theme.sprites.forCat(cat)
            List(SpriteId.CAT.frameCount) {
                PixelArt.scaled(source.sprite(SpriteId.CAT, it), Layout.CAT_PICKER_SCALE)
            }
        }[SpriteId.CAT.normaliseFrame(frame)]
}
