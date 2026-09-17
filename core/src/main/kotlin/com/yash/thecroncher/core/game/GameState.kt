package com.yash.thecroncher.core.game

import com.yash.thecroncher.core.ports.AudioOut
import com.yash.thecroncher.core.ports.Rng
import com.yash.thecroncher.core.ports.SilentAudioOut
import com.yash.thecroncher.core.ports.SoundEvent

/** What the game is doing right now. */
enum class GamePhase {
    /** "READY!" — everyone in place, nobody moving yet. */
    READY,
    PLAYING,

    /** The croncher has been caught and is fainting away. */
    DYING,

    /** All the pellets are gone; the maze flashes before the next level. */
    LEVEL_COMPLETE,
    GAME_OVER,
}

/**
 * The whole simulation: one tick of this is one arcade frame.
 *
 * Everything here is pure Kotlin and deterministic — given the same seed and the
 * same inputs it produces byte-identical results — which is what allows the golden
 * regression test to assert an exact score after thousands of ticks.
 */
class GameState(
    val maze: Maze,
    val difficulty: Difficulty,
    private val rng: Rng,
    private val audio: AudioOut = SilentAudioOut,
) {

    val croncher = Croncher(maze)
    val ghosts: List<Ghost> = GhostKind.entries.map { Ghost(maze, it) }
    val scores = ScoreBoard()

    var level: Int = 1
        private set

    var lives: Int = difficulty.lives
        private set

    var phase: GamePhase = GamePhase.READY
        private set

    var phaseTicks: Long = 0
        private set

    /** Ticks elapsed in this level, driving the scatter/chase schedule. */
    var levelTicks: Long = 0
        private set

    var frightTicksRemaining: Int = 0
        private set

    var dotsEaten: Int = 0
        private set

    /** Which pellets are still on the floor, indexed by tile. */
    private val pelletEaten = BooleanArray(maze.width * maze.height)

    private var schedule = ModeSchedule(1, difficulty.scatterScale)
    private var spec = LevelTable.forLevel(1)

    var toyTile: TilePos? = null
        private set
    private var toyTicksRemaining = 0
    private var toysShownThisLevel = 0

    val pelletsRemaining: Int
        get() = maze.totalPellets - dotsEaten

    val score: Int get() = scores.score
    val highScore: Int get() = scores.highScore

    fun ghost(kind: GhostKind): Ghost = ghosts.first { it.kind == kind }

    /** True while the frightened ghosts should be flashing their warning. */
    val isFrightFlashing: Boolean
        get() = frightTicksRemaining in 1..FLASH_WARNING_TICKS

    val scheduleMode: GhostMode get() = schedule.modeAt(levelTicks)

    // ---------------------------------------------------------- lifecycle --

    fun startNewGame() {
        level = 1
        lives = difficulty.lives
        scores.resetForNewGame()
        dotsEaten = 0
        pelletEaten.fill(false)
        startLevel(1)
    }

    private fun startLevel(newLevel: Int) {
        level = newLevel
        spec = LevelTable.forLevel(level)
        schedule = ModeSchedule(level, difficulty.scatterScale)
        pelletEaten.fill(false)
        dotsEaten = 0
        toysShownThisLevel = 0
        toyTile = null
        toyTicksRemaining = 0
        placeEveryoneForNewLife()
    }

    private fun placeEveryoneForNewLife() {
        croncher.reset(speedOf(spec.croncherSpeed))
        for (g in ghosts) {
            g.reset()
            g.speed = ghostSpeed(g)
        }
        frightTicksRemaining = 0
        levelTicks = 0
        setPhase(GamePhase.READY)
        audio.play(SoundEvent.INTRO)
    }

    private fun setPhase(next: GamePhase) {
        phase = next
        phaseTicks = 0
    }

    fun requestDirection(dir: Direction) = croncher.requestDirection(dir)

    // --------------------------------------------------------------- tick --

    fun tick() {
        phaseTicks++
        when (phase) {
            GamePhase.READY -> if (phaseTicks >= READY_TICKS) setPhase(GamePhase.PLAYING)
            GamePhase.PLAYING -> tickPlaying()
            GamePhase.DYING -> if (phaseTicks >= DYING_TICKS) afterDeath()
            GamePhase.LEVEL_COMPLETE -> if (phaseTicks >= LEVEL_END_TICKS) startLevel(level + 1)
            GamePhase.GAME_OVER -> Unit
        }
    }

    private fun tickPlaying() {
        levelTicks++

        advanceSchedule()
        advanceFright()

        croncher.update()
        eatWhateverCroncherIsStandingOn()
        releaseGhostsDue()

        val ctx = Ghost.GhostContext(
            croncherTile = croncher.tile(),
            croncherDirection = croncher.direction,
            chaserTile = ghost(GhostKind.CHASER).tile(),
            scheduleMode = scheduleMode,
            rng = rng,
        )
        for (g in ghosts) {
            g.speed = ghostSpeed(g)
            g.update(ctx)
        }

        // Checked either side of the ghosts' move: without this a ghost and the
        // croncher can swap tiles in one tick and pass straight through each other.
        if (resolveCollisions()) return

        tickToy()

        if (pelletsRemaining == 0) {
            setPhase(GamePhase.LEVEL_COMPLETE)
        }
    }

    private fun advanceSchedule() {
        if (frightTicksRemaining > 0) return
        if (schedule.isTransitionTick(levelTicks)) {
            val mode = scheduleMode
            for (g in ghosts) {
                g.applyScheduleMode(mode)
                g.requestReverse()
            }
        } else {
            for (g in ghosts) g.applyScheduleMode(scheduleMode)
        }
    }

    private fun advanceFright() {
        if (frightTicksRemaining <= 0) return
        frightTicksRemaining--
        if (frightTicksRemaining == 0) {
            scores.endFright()
            croncher.speed = speedOf(spec.croncherSpeed)
            for (g in ghosts) g.unfrighten(scheduleMode)
        }
    }

    // -------------------------------------------------------------- eating --

    private fun eatWhateverCroncherIsStandingOn() {
        val tile = croncher.tile()
        val index = tile.y * maze.width + tile.x
        if (index !in pelletEaten.indices || pelletEaten[index]) return

        when (maze.tileAt(tile.x, tile.y)) {
            Tile.DOT -> {
                pelletEaten[index] = true
                dotsEaten++
                scores.eatDot()
                audio.play(SoundEvent.CHOMP)
            }
            Tile.ENERGIZER -> {
                pelletEaten[index] = true
                dotsEaten++
                scores.eatEnergizer()
                audio.play(SoundEvent.POWER_PELLET)
                beginFright()
            }
            else -> return
        }

        if (scores.consumeExtraLifeAward()) {
            lives++
            audio.play(SoundEvent.EXTRA_LIFE)
        }
        maybeReleaseToy()
        audio.setSirenIntensity(1f - pelletsRemaining.toFloat() / maze.totalPellets)
    }

    private fun beginFright() {
        val seconds = difficulty.frightSeconds(level)
        val ticks = (seconds * TICKS_PER_SECOND).toInt()
        if (ticks <= 0) {
            // On the late levels the energizer is worth points and nothing else.
            frightTicksRemaining = 0
            return
        }
        frightTicksRemaining = ticks
        croncher.speed = speedOf(spec.croncherFrightSpeed)
        for (g in ghosts) g.frighten()
    }

    /** True if the life ended, meaning the rest of this tick should be abandoned. */
    private fun resolveCollisions(): Boolean {
        val catTile = croncher.tile()
        for (g in ghosts) {
            if (g.mode == GhostMode.EATEN || g.mode.isInsideHouse) continue
            if (g.tile() != catTile) continue

            if (g.mode == GhostMode.FRIGHTENED) {
                val points = scores.eatGhost()
                g.getEaten()
                audio.play(SoundEvent.GHOST_EATEN)
                lastGhostPoints = points
            } else {
                loseLife()
                return true
            }
        }
        return false
    }

    var lastGhostPoints: Int = 0
        private set

    private fun loseLife() {
        lives--
        audio.play(SoundEvent.DEATH)
        audio.stopAll()
        setPhase(GamePhase.DYING)
    }

    private fun afterDeath() {
        if (lives <= 0) setPhase(GamePhase.GAME_OVER) else placeEveryoneForNewLife()
    }

    // --------------------------------------------------------------- house --

    private fun releaseGhostsDue() {
        for (g in ghosts) {
            if (g.mode != GhostMode.IN_HOUSE) continue
            if (dotsEaten >= houseDotLimit(g.kind)) {
                g.release()
                // One at a time, so they file out in order rather than in a clump.
                break
            }
        }
    }

    // --------------------------------------------------------------- toy --

    private fun maybeReleaseToy() {
        if (toysShownThisLevel >= TOY_APPEARANCES.size) return
        if (dotsEaten != TOY_APPEARANCES[toysShownThisLevel]) return
        toysShownThisLevel++
        toyTile = TOY_TILE
        toyTicksRemaining = TOY_TICKS
    }

    private fun tickToy() {
        val tile = toyTile ?: return
        if (croncher.tile() == tile) {
            scores.eatToy(LevelTable.toyForLevel(level))
            audio.play(SoundEvent.TOY_EATEN)
            toyTile = null
            return
        }
        if (--toyTicksRemaining <= 0) toyTile = null
    }

    // -------------------------------------------------------------- speeds --

    private fun ghostSpeed(g: Ghost): Int {
        val base = when {
            g.mode == GhostMode.EATEN -> EATEN_SPEED
            g.mode == GhostMode.FRIGHTENED -> spec.ghostFrightSpeed
            maze.isTunnel(g.tile().x, g.tile().y) -> spec.ghostTunnelSpeed
            else -> spec.ghostSpeed
        }
        val scaled = if (g.mode == GhostMode.EATEN) base else base * difficulty.ghostSpeedScale
        return speedOf(scaled)
    }

    /** Has this pellet already been eaten? Used by the renderer. */
    fun isPelletEaten(x: Int, y: Int): Boolean {
        val index = y * maze.width + x
        return index in pelletEaten.indices && pelletEaten[index]
    }

    // ------------------------------------------------- test-support hooks --

    /** Test hook: jump the dot counter to exercise the ghost-release thresholds. */
    fun debugSetDotsEaten(count: Int) {
        dotsEaten = count.coerceIn(0, maze.totalPellets)
    }

    /** Test hook: examine late-level behaviour without playing eighteen levels. */
    fun debugSetLevel(newLevel: Int) {
        level = newLevel
        spec = LevelTable.forLevel(newLevel)
        schedule = ModeSchedule(newLevel, difficulty.scatterScale)
    }

    companion object {
        const val TICKS_PER_SECOND = GameLoop.TICKS_PER_SECOND

        const val READY_TICKS = 2 * TICKS_PER_SECOND
        const val DYING_TICKS = 2 * TICKS_PER_SECOND
        const val LEVEL_END_TICKS = 2 * TICKS_PER_SECOND

        /** Frightened ghosts start flashing with this long left. */
        const val FLASH_WARNING_TICKS = 2 * TICKS_PER_SECOND

        /** Eyes travel fast — they are not a threat and should not dawdle. */
        const val EATEN_SPEED = 1.5

        /** Dots eaten before each ghost is let out (level one values). */
        fun houseDotLimit(kind: GhostKind): Int = when (kind) {
            GhostKind.CHASER -> 0
            GhostKind.AMBUSHER -> 0
            GhostKind.FLANKER -> 30
            GhostKind.COWARD -> 60
        }

        /** Toy appears after this many dots, twice per level. */
        val TOY_APPEARANCES = intArrayOf(70, 170)

        /** It sits just below the ghost house. */
        val TOY_TILE = TilePos(22, 17)

        const val TOY_TICKS = 9 * TICKS_PER_SECOND
    }
}
