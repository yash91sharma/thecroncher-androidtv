package com.yash.thecroncher.core

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Phase 2 gate: proves the sealed toolchain compiles Kotlin and reports test
 * results, before any game code exists. If this fails, the problem is the build,
 * not the game.
 */
class ToolchainSmokeTest {

    @Test
    fun `test infrastructure runs`() {
        assertEquals(4, 2 + 2)
    }
}
