package oy.sarjakuvat.flamingin.bde.gles

import android.opengl.EGL14
import kotlin.Throws
import android.opengl.GLES20
import android.graphics.Bitmap
import android.util.Log
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.RuntimeException
import java.nio.ByteBuffer
import java.nio.ByteOrder

open class EglSurfaceBase protected constructor(
    eglCore: EglCore
) {
    @Suppress("UNNECESSARY_LATEINIT", "JoinDeclarationAndAssignment")
    protected lateinit var eglCoreInstance: EglCore

    private var eglSurfaceInstance = EGL14.EGL_NO_SURFACE
    private var surfaceWidth = -1
    private var surfaceHeight = -1

    init {
        eglCoreInstance = eglCore
    }

    fun createWindowSurface(surface: Any?) {
        check(!(eglSurfaceInstance !== EGL14.EGL_NO_SURFACE)) {
            "Attempt to create surface twice"
        }

        eglSurfaceInstance = eglCoreInstance.createWindowSurface(surface!!)
    }

    fun createOffscreenSurface(width: Int, height: Int) {
        check(!(eglSurfaceInstance !== EGL14.EGL_NO_SURFACE)) {
            "Attempt to create offscreen surface twice"
        }

        eglSurfaceInstance = eglCoreInstance.createOffscreenSurface(width, height)
        surfaceWidth = width
        surfaceHeight = height
    }

    val width: Int
        get() = if (surfaceWidth < 0) {
            eglCoreInstance.querySurface(eglSurfaceInstance, EGL14.EGL_WIDTH)
        } else {
            surfaceWidth
        }

    val height: Int
        get() = if (surfaceHeight < 0) {
            eglCoreInstance.querySurface(eglSurfaceInstance, EGL14.EGL_HEIGHT)
        } else {
            surfaceHeight
        }

    fun makeCurrent() {
        eglCoreInstance.makeCurrent(eglSurfaceInstance)
    }

    fun makeCurrentReadFrom(readSurface: EglSurfaceBase) {
        eglCoreInstance.makeCurrent(eglSurfaceInstance, readSurface.eglSurfaceInstance)
    }

    fun swapBuffers(): Boolean {
        val result = eglCoreInstance.swapBuffers(eglSurfaceInstance)
        if (!result) {
            Log.d(TAG, "Failed to invoke EglCore swapBuffers")
        }

        return result
    }

    fun setPresentationTime(nsecs: Long) {
        eglCoreInstance.setPresentationTime(eglSurfaceInstance, nsecs)
    }

    @Throws(IOException::class)
    fun screenShot(file: File) {
        if (!eglCoreInstance.isSurfaceCurrent(eglSurfaceInstance)) {
            throw RuntimeException("Egl context and surface must be current to save a frame")
        }

        val filename = file.toString()
        val width = width
        val height = height
        val buf = ByteBuffer.allocateDirect(width * height * 4)
        buf.order(ByteOrder.LITTLE_ENDIAN)
        GLES20.glReadPixels(
            0, 0, width, height,
            GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buf
        )

        GlUtil.checkGlError("glReadPixels")
        buf.rewind()
        BufferedOutputStream(FileOutputStream(filename)).use { bos ->
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bmp.copyPixelsFromBuffer(buf)
            bmp.compress(Bitmap.CompressFormat.PNG, 90, bos)
            bmp.recycle()
        }

        Log.d(TAG, "A $width by $height pixel frame was saved to $filename")
    }

    fun releaseEglSurface() {
        eglCoreInstance.releaseSurface(eglSurfaceInstance)
        eglSurfaceInstance = EGL14.EGL_NO_SURFACE
        surfaceHeight = -1
        surfaceWidth = -1
    }

    companion object {
        protected val TAG = GlUtil.TAG
    }
}
