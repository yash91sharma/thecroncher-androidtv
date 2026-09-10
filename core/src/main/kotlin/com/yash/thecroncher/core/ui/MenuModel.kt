package com.yash.thecroncher.core.ui

/** What selecting a menu entry does. */
sealed interface ItemKind {

    /** Runs something and says where to go next. */
    class Action(val onSelect: () -> Transition) : ItemKind

    /**
     * Cycles through a fixed list of options, reading and writing the real setting
     * so the menu never holds a stale copy of it.
     */
    class Choice(
        val options: List<String>,
        val getIndex: () -> Int,
        val setIndex: (Int) -> Unit,
    ) : ItemKind
}

data class MenuItem(val label: String, val kind: ItemKind)

/**
 * A menu, as data.
 *
 * Adding an option is one [MenuItem] in a list: [MenuRenderer] can draw any menu
 * and this class handles all the navigation, so no new drawing or input code is
 * ever needed for a new setting.
 */
class MenuModel(
    val title: String?,
    val items: List<MenuItem>,
) {

    var selectedIndex: Int = 0
        private set

    fun moveUp() = move(-1)

    fun moveDown() = move(1)

    private fun move(delta: Int) {
        if (items.isEmpty()) return
        // Wrapping keeps a D-pad usable without hunting for the end of the list.
        selectedIndex = ((selectedIndex + delta) % items.size + items.size) % items.size
    }

    /** Left/right on a choice. Returns false if this item has no value to change. */
    fun adjust(delta: Int): Boolean {
        val kind = (items.getOrNull(selectedIndex)?.kind as? ItemKind.Choice) ?: return false
        if (kind.options.isEmpty()) return false
        val next = ((kind.getIndex() + delta) % kind.options.size + kind.options.size) % kind.options.size
        kind.setIndex(next)
        return true
    }

    /** Confirm. On a choice this steps forward, so one button can do everything. */
    fun activate(): Transition {
        return when (val kind = items.getOrNull(selectedIndex)?.kind) {
            is ItemKind.Action -> kind.onSelect()
            is ItemKind.Choice -> { adjust(1); Transition.None }
            null -> Transition.None
        }
    }

    /** The option text to show beside an item, or null if it has no value. */
    fun valueOf(index: Int): String? {
        val kind = items.getOrNull(index)?.kind as? ItemKind.Choice ?: return null
        if (kind.options.isEmpty()) return null
        // Clamp rather than crash if the stored setting has drifted out of range.
        return kind.options[kind.getIndex().coerceIn(0, kind.options.lastIndex)]
    }
}
