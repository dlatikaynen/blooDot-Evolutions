package oy.sarjakuvat.flamingin.bde.gles

class OffscreenSurface(eglCore: EglCore?, width: Int, height: Int) : EglSurfaceBase(eglCore!!) {
    init {
        createOffscreenSurface(width, height)
    }

    fun release() {
        releaseEglSurface()
    }
}
