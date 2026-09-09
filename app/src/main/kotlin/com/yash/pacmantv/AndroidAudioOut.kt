package com.yash.pacmantv

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import com.yash.pacmantv.core.ports.AudioOut
import com.yash.pacmantv.core.ports.SoundEvent
import kotlin.math.PI
import kotlin.math.sin

/**
 * The chiptune backend: every sound is synthesised as square and triangle waves at
 * startup, so the app ships with no audio files at all.
 *
 * Each event gets its own small static [AudioTrack], which means replaying a sound
 * is a rewind rather than an allocation, and two different sounds can overlap
 * without any mixing code.
 */
class AndroidAudioOut : AudioOut {

    private val tracks = HashMap<SoundEvent, AudioTrack>()
    private var enabled = true

    init {
        for (event in SoundEvent.entries) {
            runCatching { tracks[event] = buildTrack(render(event)) }
                .onFailure { Log.w(GameSurfaceView.TAG, "could not build audio for $event", it) }
        }
    }

    override fun play(event: SoundEvent) {
        if (!enabled) return
        val track = tracks[event] ?: return
        runCatching {
            // Static-mode tracks are replayed by rewinding, not by re-writing.
            if (track.playState != AudioTrack.PLAYSTATE_STOPPED) track.stop()
            track.reloadStaticData()
            track.play()
        }
    }

    /** The siren is not implemented as a continuous voice yet; see plan.md. */
    override fun setSirenIntensity(level: Float) = Unit

    override fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
        if (!enabled) stopAll()
    }

    override fun stopAll() {
        for (track in tracks.values) runCatching { track.stop() }
    }

    fun release() {
        for (track in tracks.values) runCatching { track.release() }
        tracks.clear()
    }

    // ------------------------------------------------------------ synthesis --

    private fun buildTrack(samples: ShortArray): AudioTrack {
        val bytes = samples.size * 2
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build(),
            )
            .setBufferSizeInBytes(bytes)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        track.write(samples, 0, samples.size)
        return track
    }

    private fun render(event: SoundEvent): ShortArray = when (event) {
        // A short two-tone blip; at speed it becomes the familiar "waka".
        SoundEvent.CHOMP -> sweep(440.0, 220.0, 0.055, Wave.SQUARE, volume = 0.30)

        // A warbling arpeggio, unmistakably "you are now the dangerous one".
        SoundEvent.POWER_PELLET -> sequence(
            listOf(220.0 to 0.06, 330.0 to 0.06, 440.0 to 0.06, 330.0 to 0.06, 550.0 to 0.10),
            Wave.SQUARE, volume = 0.32,
        )

        SoundEvent.GHOST_EATEN -> sweep(200.0, 1200.0, 0.30, Wave.SQUARE, volume = 0.30)

        SoundEvent.FRUIT_EATEN -> sequence(
            listOf(660.0 to 0.05, 880.0 to 0.05, 1100.0 to 0.10),
            Wave.TRIANGLE, volume = 0.30,
        )

        SoundEvent.DEATH -> sweep(700.0, 90.0, 1.00, Wave.TRIANGLE, volume = 0.35)

        SoundEvent.EXTRA_LIFE -> sequence(
            listOf(880.0 to 0.08, 1320.0 to 0.16),
            Wave.SQUARE, volume = 0.32,
        )

        // The little fanfare while "READY!" is on screen.
        SoundEvent.INTRO -> sequence(
            listOf(
                523.0 to 0.12, 659.0 to 0.12, 784.0 to 0.12, 1047.0 to 0.18,
                784.0 to 0.10, 1047.0 to 0.24,
            ),
            Wave.SQUARE, volume = 0.28,
        )
    }

    private enum class Wave { SQUARE, TRIANGLE }

    private fun sample(wave: Wave, phase: Double): Double = when (wave) {
        Wave.SQUARE -> if (sin(phase) >= 0) 1.0 else -1.0
        // A rounded triangle: softer than a square, still obviously 8-bit.
        Wave.TRIANGLE -> 2.0 / PI * kotlin.math.asin(sin(phase))
    }

    /** A single tone gliding from one frequency to another. */
    private fun sweep(from: Double, to: Double, seconds: Double, wave: Wave, volume: Double): ShortArray {
        val count = (SAMPLE_RATE * seconds).toInt()
        val out = ShortArray(count)
        var phase = 0.0
        for (i in 0 until count) {
            val t = i.toDouble() / count
            val freq = from + (to - from) * t
            phase += 2 * PI * freq / SAMPLE_RATE
            out[i] = (sample(wave, phase) * volume * envelope(t) * Short.MAX_VALUE).toInt().toShort()
        }
        return out
    }

    /** A run of discrete notes, each a (frequency, seconds) pair. */
    private fun sequence(notes: List<Pair<Double, Double>>, wave: Wave, volume: Double): ShortArray {
        val total = notes.sumOf { (SAMPLE_RATE * it.second).toInt() }
        val out = ShortArray(total)
        var offset = 0
        for ((freq, seconds) in notes) {
            val count = (SAMPLE_RATE * seconds).toInt()
            var phase = 0.0
            for (i in 0 until count) {
                phase += 2 * PI * freq / SAMPLE_RATE
                val t = i.toDouble() / count
                out[offset + i] =
                    (sample(wave, phase) * volume * envelope(t) * Short.MAX_VALUE).toInt().toShort()
            }
            offset += count
        }
        return out
    }

    /** Quick attack, gentle decay — enough to stop every note clicking. */
    private fun envelope(t: Double): Double = when {
        t < 0.02 -> t / 0.02
        t > 0.80 -> (1.0 - t) / 0.20
        else -> 1.0
    }

    private companion object {
        const val SAMPLE_RATE = 22_050
    }
}
