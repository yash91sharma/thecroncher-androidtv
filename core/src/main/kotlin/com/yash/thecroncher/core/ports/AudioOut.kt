package com.yash.thecroncher.core.ports

/** Everything the game can make a noise about. */
enum class SoundEvent {
    INTRO,
    CHOMP,
    POWER_PELLET,
    GHOST_EATEN,
    TOY_EATEN,
    DEATH,
    EXTRA_LIFE,
}

/**
 * The game emits sound *events*; how they become audible is `:app`'s problem.
 * Keeps the simulation free of Android audio types, and lets tests assert that
 * eating a pellet made a chomp without synthesising anything.
 */
interface AudioOut {
    fun play(event: SoundEvent)

    /** The rising siren tracks how many pellets are left, 0..1. */
    fun setSirenIntensity(level: Float)

    fun setEnabled(enabled: Boolean)

    fun stopAll()
}

/** Does nothing. Used in tests and when sound is switched off. */
object SilentAudioOut : AudioOut {
    override fun play(event: SoundEvent) = Unit
    override fun setSirenIntensity(level: Float) = Unit
    override fun setEnabled(enabled: Boolean) = Unit
    override fun stopAll() = Unit
}

/** Records what was played, for tests. */
class RecordingAudioOut : AudioOut {
    val played = mutableListOf<SoundEvent>()
    var enabled = true; private set

    override fun play(event: SoundEvent) { played += event }
    override fun setSirenIntensity(level: Float) = Unit
    override fun setEnabled(enabled: Boolean) { this.enabled = enabled }
    override fun stopAll() { played.clear() }
}
