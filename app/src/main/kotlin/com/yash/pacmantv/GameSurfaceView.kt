package com.yash.pacmantv

import android.content.Context
import android.graphics.Canvas
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager
import com.yash.pacmantv.core.game.GameLoop
import com.yash.pacmantv.core.game.Scaling
import com.yash.pacmantv.core.game.Viewport
import com.yash.pacmantv.core.theme.Theme
import com.yash.pacmantv.core.theme.ThemeRegistry

/**
 * Hosts the game on its own render thread at a fixed 60 Hz timestep.
 *
 * Also asks the compositor for the panel's true resolution: an Android TV app is
 * handed a 1080p surface by default even on a 4K set, and on a 77" screen the
 * difference between a x3 and a x7 pixel scale is very visible.
 */
class GameSurfaceView(
    context: Context,
    private val onRender: (RenderContext) -> Unit,
    private val onTick: (Long) -> Unit = {},
) : SurfaceView(context), SurfaceHolder.Callback, Runnable {

    /** What a frame needs in order to draw itself. */
    data class RenderContext(
        val gfx: CanvasGfx,
        val theme: Theme,
        val tick: Long,
        val fps: Int,
    )

    private val gfx = CanvasGfx()
    private val loop = GameLoop()

    var theme: Theme = ThemeRegistry.default

    private var thread: Thread? = null

    @Volatile
    private var running = false

    private var viewport: Viewport = Scaling.viewport(1920, 1080)

    private var fps = 0
    private var framesThisSecond = 0
    private var fpsWindowStart = 0L

    init {
        holder.addCallback(this)
        isFocusable = true
        isFocusableInTouchMode = true
    }

    // ------------------------------------------------------ surface lifecycle --

    override fun surfaceCreated(holder: SurfaceHolder) {
        requestNativeResolution(holder)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        viewport = Scaling.viewport(width, height)
        Log.i(
            TAG,
            "surface ${width}x$height -> playfield scale x${viewport.scale} " +
                "(${viewport.width}x${viewport.height} at ${viewport.x},${viewport.y})",
        )
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) = stop()

    /**
     * Ask for the panel's real mode. If the device declines, the scaling maths is
     * resolution-agnostic and simply lands on a smaller whole-number scale, so
     * nothing breaks — we just log what we actually got.
     */
    private fun requestNativeResolution(holder: SurfaceHolder) {
        try {
            @Suppress("DEPRECATION")
            val display = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
                .defaultDisplay
            val mode = display.mode
            val w = mode.physicalWidth
            val h = mode.physicalHeight
            Log.i(TAG, "display reports ${w}x$h; requesting it for the surface")
            if (w > 0 && h > 0) holder.setFixedSize(w, h)
        } catch (e: Exception) {
            Log.w(TAG, "could not query the display mode, using the default surface", e)
        }
    }

    // ------------------------------------------------------------ the loop --

    fun start() {
        if (running) return
        running = true
        loop.reset()
        thread = Thread(this, "pacman-render").also { it.start() }
    }

    fun stop() {
        running = false
        thread?.join(1_000)
        thread = null
    }

    override fun run() {
        var last = System.nanoTime()
        fpsWindowStart = last

        while (running) {
            val now = System.nanoTime()
            val elapsed = now - last
            last = now

            repeat(loop.advance(elapsed)) { onTick(loop.totalTicks) }

            drawFrame()
            countFrame(now)
        }
    }

    private fun drawFrame() {
        val surface = holder
        if (!surface.surface.isValid) return

        val canvas: Canvas = surface.lockCanvas() ?: return
        try {
            onRender(RenderContext(gfx, theme, loop.totalTicks, fps))
            gfx.present(canvas, viewport, theme.background)
        } finally {
            surface.unlockCanvasAndPost(canvas)
        }
    }

    private fun countFrame(now: Long) {
        framesThisSecond++
        if (now - fpsWindowStart >= 1_000_000_000L) {
            fps = framesThisSecond
            framesThisSecond = 0
            fpsWindowStart = now
            // Occasional, so the log stays readable but the frame rate is provable.
            if (++fpsReports % 5 == 0) Log.i(TAG, "fps=$fps tick=${loop.totalTicks}")
        }
    }

    private var fpsReports = 0

    companion object {
        const val TAG = "PacmanTV"
    }
}
