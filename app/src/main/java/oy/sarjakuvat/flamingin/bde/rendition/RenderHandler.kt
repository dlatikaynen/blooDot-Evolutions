package oy.sarjakuvat.flamingin.bde.rendition

import android.os.Handler
import android.os.Message
import android.util.Log
import oy.sarjakuvat.flamingin.bde.GameActivity
import java.lang.RuntimeException
import java.lang.ref.WeakReference

class RenderHandler(rt: RenderThread) : Handler() {
    private val renderThreadRef: WeakReference<RenderThread> = WeakReference(rt)

    fun sendSurfaceCreated() {
        sendMessage(obtainMessage(MSG_SURFACE_CREATED))
    }

    fun sendSurfaceChanged(width: Int, height: Int) {
        sendMessage(obtainMessage(MSG_SURFACE_CHANGED, width, height))
    }

    fun sendDoFrame(frameTimeNanos: Long) {
        sendMessage(
            obtainMessage(
                MSG_DO_FRAME,
                (frameTimeNanos shr 32).toInt(), frameTimeNanos.toInt()
            )
        )
    }

    fun sendSetFlatShading(useFlatShading: Boolean) {
        sendMessage(obtainMessage(MSG_FLAT_SHADING, if (useFlatShading) 1 else 0, 0))
    }

    fun sendShutdown() {
        sendMessage(obtainMessage(MSG_SHUTDOWN))
    }

    /// Runs on render thread
    override fun handleMessage(msg: Message) {
        val what = msg.what
        val renderThread = renderThreadRef.get()
        if (renderThread == null) {
            Log.w(GameActivity.TAG, "RenderHandler.handleMessage's weak reference is null")
            return
        }

        when (what) {
            MSG_SURFACE_CREATED -> renderThread.surfaceCreated()
            MSG_SURFACE_CHANGED -> renderThread.surfaceChanged(msg.arg1, msg.arg2)
            MSG_DO_FRAME -> {
                /* translate nanoseconds timestamp back into one variable */
                val timestamp = msg.arg1.toLong() shl 32 or (msg.arg2.toLong() and 0xffffffffL)
                renderThread.doFrame(timestamp)
            }

            MSG_FLAT_SHADING -> renderThread.setFlatShading(msg.arg1 != 0)
            MSG_SHUTDOWN -> renderThread.shutdown()
            else -> throw RuntimeException("Render thread could not handle message: $what is not supported")
        }
    }

    companion object {
        private const val MSG_SURFACE_CREATED = 0
        private const val MSG_SURFACE_CHANGED = 1
        private const val MSG_DO_FRAME = 2
        private const val MSG_FLAT_SHADING = 3
        private const val MSG_SHUTDOWN = 5
    }
}
