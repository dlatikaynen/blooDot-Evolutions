package oy.sarjakuvat.flamingin.bde.gles

import android.graphics.SurfaceTexture
import android.view.Surface
import java.lang.RuntimeException

class WindowSurface : EglSurfaceBase {
    private var windowSurface: Surface? = null
    private var doReleaseAfter = false

    constructor(eglCore: EglCore?, surface: Surface?, releaseSurface: Boolean) : super(eglCore!!) {
        createWindowSurface(surface)
        windowSurface = surface
        doReleaseAfter = releaseSurface
    }

    constructor(eglCore: EglCore?, surfaceTexture: SurfaceTexture?) : super(eglCore!!) {
        createWindowSurface(surfaceTexture)
    }

    fun reCreateSurface(newEglCore: EglCore) {
        if (windowSurface == null) {
            throw RuntimeException("Attempt to recreate a null surface")
        }

        eglCoreInstance = newEglCore
        createWindowSurface(windowSurface)
    }

    fun release() {
        releaseEglSurface()
        if (windowSurface != null) {
            if (doReleaseAfter) {
                windowSurface!!.release()
            }

            windowSurface = null
        }
    }
}
