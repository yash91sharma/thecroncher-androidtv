package com.yash.thecroncher.core.ui

import com.yash.thecroncher.core.input.Button
import com.yash.thecroncher.core.input.InputEvent
import com.yash.thecroncher.core.ports.Gfx
import com.yash.thecroncher.core.theme.Theme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Screens are pushed and popped on a stack, so adding a new one (high scores, say)
 * is a new class plus one entry, and never a change to the screens around it.
 */
class ScreenStackTest {

    private class Probe(val name: String, var result: Transition = Transition.None) : Screen {
        var entered = 0
        var exited = 0
        var handled = 0
        var updates = 0
        var renders = 0

        override fun onEnter() { entered++ }
        override fun onExit() { exited++ }
        override fun update(tick: Long) { updates++ }
        override fun render(g: Gfx, theme: Theme, tick: Long) { renders++ }
        override fun handle(event: InputEvent): Transition {
            handled++
            return result
        }
    }

    private val confirm = InputEvent.Press(Button.CONFIRM)

    @Test
    fun `the root screen is entered when the stack is created`() {
        val root = Probe("root")
        ScreenStack(root)
        assertEquals(1, root.entered)
    }

    @Test
    fun `pushing enters the new screen without exiting the old one`() {
        val root = Probe("root")
        val child = Probe("child")
        root.result = Transition.Push(child)

        val stack = ScreenStack(root)
        stack.handle(confirm)

        assertSame(child, stack.current)
        assertEquals(1, child.entered)
        assertEquals("the screen underneath is still alive", 0, root.exited)
        assertEquals(2, stack.depth)
    }

    @Test
    fun `popping returns to the screen underneath and exits the top one`() {
        val root = Probe("root")
        val child = Probe("child", result = Transition.Pop)
        root.result = Transition.Push(child)

        val stack = ScreenStack(root)
        stack.handle(confirm)     // push child
        stack.handle(confirm)     // child pops itself

        assertSame(root, stack.current)
        assertEquals(1, child.exited)
        assertEquals(1, stack.depth)
    }

    @Test
    fun `replacing swaps the top screen`() {
        val root = Probe("root")
        val other = Probe("other")
        root.result = Transition.Replace(other)

        val stack = ScreenStack(root)
        stack.handle(confirm)

        assertSame(other, stack.current)
        assertEquals(1, root.exited)
        assertEquals(1, other.entered)
        assertEquals("replacing does not grow the stack", 1, stack.depth)
    }

    @Test
    fun `popping the last screen is refused so the stack is never empty`() {
        val root = Probe("root", result = Transition.Pop)
        val stack = ScreenStack(root)
        stack.handle(confirm)
        assertSame(root, stack.current)
        assertEquals(1, stack.depth)
    }

    @Test
    fun `only the top screen receives input, updates and renders`() {
        val root = Probe("root")
        val child = Probe("child")
        root.result = Transition.Push(child)

        val stack = ScreenStack(root)
        stack.handle(confirm)
        val rootHandled = root.handled

        stack.handle(confirm)
        stack.update(1)
        stack.render(RecordingSink, ThemeStub, 1)

        assertEquals("the buried screen must not react", rootHandled, root.handled)
        assertEquals(0, root.updates)
        assertEquals(1, child.updates)
        assertEquals(1, child.renders)
    }

    @Test
    fun `an exit transition is reported to the host rather than handled here`() {
        val root = Probe("root", result = Transition.Exit)
        var exitRequested = false
        val stack = ScreenStack(root)
        stack.onExitRequested = { exitRequested = true }

        stack.handle(confirm)
        assertTrue("the activity, not the stack, decides how to quit", exitRequested)
    }

    @Test
    fun `enter and exit each fire exactly once per visit`() {
        val root = Probe("root")
        val child = Probe("child")
        root.result = Transition.Push(child)
        child.result = Transition.Pop

        val stack = ScreenStack(root)
        repeat(3) {
            stack.handle(confirm)   // push
            stack.handle(confirm)   // pop
        }
        assertEquals(3, child.entered)
        assertEquals(3, child.exited)
    }

    private companion object {
        val RecordingSink = com.yash.thecroncher.core.support.RecordingGfx()
        val ThemeStub = com.yash.thecroncher.core.theme.ThemeRegistry.default
    }
}
