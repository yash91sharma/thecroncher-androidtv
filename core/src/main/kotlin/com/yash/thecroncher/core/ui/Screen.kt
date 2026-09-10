package com.yash.thecroncher.core.ui

import com.yash.thecroncher.core.input.InputEvent
import com.yash.thecroncher.core.ports.Gfx
import com.yash.thecroncher.core.theme.Theme

/** What a screen wants to happen next. */
sealed interface Transition {
    /** Stay where we are. */
    data object None : Transition

    /** Open a screen on top of this one, keeping this one alive underneath. */
    data class Push(val screen: Screen) : Transition

    /** Close this screen and return to the one below. */
    data object Pop : Transition

    /**
     * Close everything above the root. "Quit to menu" needs to shut both the pause
     * overlay and the game beneath it, which a single [Pop] cannot express.
     */
    data object PopToRoot : Transition

    /** Swap this screen for another, without growing the stack. */
    data class Replace(val screen: Screen) : Transition

    /** Quit the app. The host decides what that means. */
    data object Exit : Transition
}

/**
 * One full-screen view. Everything the player sees is one of these, drawn through
 * [Gfx] with colours from [Theme] — so screens are testable on the JVM and any of
 * them can be replaced without the others noticing.
 */
interface Screen {
    fun onEnter() {}
    fun onExit() {}

    fun handle(event: InputEvent): Transition

    fun update(tick: Long) {}

    fun render(g: Gfx, theme: Theme, tick: Long)
}

/**
 * A stack of screens. Only the top one runs; the ones below keep their state, so
 * pausing a game and coming back finds it exactly as it was.
 */
class ScreenStack(root: Screen) {

    private val stack = ArrayDeque<Screen>()

    /** Called when a screen asks to quit; the host decides how. */
    var onExitRequested: () -> Unit = {}

    init {
        stack.addLast(root)
        root.onEnter()
    }

    val current: Screen get() = stack.last()

    val depth: Int get() = stack.size

    fun handle(event: InputEvent) = apply(current.handle(event))

    fun update(tick: Long) = current.update(tick)

    fun render(g: Gfx, theme: Theme, tick: Long) = current.render(g, theme, tick)

    /** Replaces the whole stack, as when settings change and screens are rebuilt. */
    fun reset(root: Screen) {
        while (stack.isNotEmpty()) stack.removeLast().onExit()
        stack.addLast(root)
        root.onEnter()
    }

    private fun apply(transition: Transition) {
        when (transition) {
            is Transition.None -> Unit

            is Transition.Push -> {
                stack.addLast(transition.screen)
                transition.screen.onEnter()
            }

            is Transition.Pop -> {
                // Refuse to empty the stack: there must always be something on screen.
                if (stack.size > 1) stack.removeLast().onExit()
            }

            is Transition.PopToRoot -> {
                while (stack.size > 1) stack.removeLast().onExit()
            }

            is Transition.Replace -> {
                stack.removeLast().onExit()
                stack.addLast(transition.screen)
                transition.screen.onEnter()
            }

            is Transition.Exit -> onExitRequested()
        }
    }
}
