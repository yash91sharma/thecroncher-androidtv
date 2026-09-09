package com.yash.pacmantv.core.ui.screens

import com.yash.pacmantv.core.game.Difficulties
import com.yash.pacmantv.core.game.GamePhase
import com.yash.pacmantv.core.game.GameState
import com.yash.pacmantv.core.game.Maze
import com.yash.pacmantv.core.input.Button
import com.yash.pacmantv.core.input.InputEvent
import com.yash.pacmantv.core.ports.AudioOut
import com.yash.pacmantv.core.ports.Gfx
import com.yash.pacmantv.core.ports.Rng
import com.yash.pacmantv.core.ports.SilentAudioOut
import com.yash.pacmantv.core.theme.Theme
import com.yash.pacmantv.core.ui.GameRenderer
import com.yash.pacmantv.core.ui.GameSettings
import com.yash.pacmantv.core.ui.Screen
import com.yash.pacmantv.core.ui.Transition

/** Plays the game. Owns the [GameState] and feeds it input, ticks and rendering. */
class GameScreen(
    private val settings: GameSettings,
    private val audio: AudioOut,
    private val rng: Rng,
) : Screen {

    val state = GameState(
        maze = Maze.loadClassic(),
        difficulty = settings.difficulty,
        rng = rng,
        audio = if (settings.soundEnabled) audio else SilentAudioOut,
    )

    private var gameOverHold = 0

    override fun onEnter() {
        state.scores.highScore = settings.highScore
        state.startNewGame()
    }

    override fun onExit() {
        settings.highScore = state.highScore
    }

    override fun handle(event: InputEvent): Transition = when (event) {
        is InputEvent.Move -> {
            state.requestDirection(event.direction)
            Transition.None
        }

        is InputEvent.Press -> when (event.button) {
            Button.PAUSE -> Transition.Push(PauseScreen(this))
            Button.BACK -> Transition.Push(PauseScreen(this))
            // Once it is over, any confirm starts again.
            Button.CONFIRM ->
                if (state.phase == GamePhase.GAME_OVER) restart() else Transition.None
        }
    }

    fun restart(): Transition {
        settings.highScore = state.highScore
        state.scores.highScore = settings.highScore
        state.startNewGame()
        return Transition.None
    }

    override fun update(tick: Long) {
        state.tick()
        if (state.phase == GamePhase.GAME_OVER) {
            gameOverHold++
            // Persist the score as soon as the run ends, not only when leaving.
            if (gameOverHold == 1) settings.highScore = state.highScore
        } else {
            gameOverHold = 0
        }
    }

    override fun render(g: Gfx, theme: Theme, tick: Long) =
        GameRenderer.render(g, theme, state, tick)
}
