package oy.sarjakuvat.flamingin.bde.gles

import kotlin.jvm.JvmOverloads
import kotlin.Throws
import android.graphics.SurfaceTexture
import android.opengl.*
import android.util.Log
import android.view.Surface
import java.lang.RuntimeException

class EglCore @JvmOverloads constructor(sharedContext: EGLContext? = null, flags: Int = 0) {
    private var mEGLDisplay = EGL14.EGL_NO_DISPLAY
    private var mEGLContext = EGL14.EGL_NO_CONTEXT
    private var mEGLConfig: EGLConfig? = null
    private var glVersion = -1

    private fun getConfig(flags: Int, version: Int): EGLConfig? {
        var renderableType = EGL14.EGL_OPENGL_ES2_BIT
        if (version >= 3) {
            renderableType = renderableType or EGLExt.EGL_OPENGL_ES3_BIT_KHR
        }

        val attribList = intArrayOf(
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_RENDERABLE_TYPE, renderableType,
            EGL14.EGL_NONE, 0,
            EGL14.EGL_NONE
        )

        if (flags and FLAG_RECORDABLE != 0) {
            attribList[attribList.size - 3] = EGL_RECORDABLE_ANDROID
            attribList[attribList.size - 2] = 1
        }

        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        if (!EGL14.eglChooseConfig(
                mEGLDisplay, attribList, 0, configs, 0, configs.size,
                numConfigs, 0
            )
        ) {
            Log.w(TAG, "RGB8888 is needed. Version $version of EGLConfig could not be selected")
            return null
        }

        return configs[0]
    }

    fun createWindowSurface(surface: Any): EGLSurface {
        if (surface !is Surface && surface !is SurfaceTexture) {
            throw RuntimeException("The $surface surface is invalid")
        }

        // Create a window surface, and attach it to the Surface we received.
        val surfaceAttribs = intArrayOf(
            EGL14.EGL_NONE
        )

        val eglSurface = EGL14.eglCreateWindowSurface(
            mEGLDisplay, mEGLConfig, surface,
            surfaceAttribs, 0
        )

        checkEglError("eglCreateWindowSurface")
        if (eglSurface == null) {
            throw RuntimeException("Could not create surface")
        }

        return eglSurface
    }

    fun createOffscreenSurface(width: Int, height: Int): EGLSurface {
        val surfaceAttribs = intArrayOf(
            EGL14.EGL_WIDTH, width,
            EGL14.EGL_HEIGHT, height,
            EGL14.EGL_NONE
        )

        val eglSurface = EGL14.eglCreatePbufferSurface(
            mEGLDisplay, mEGLConfig,
            surfaceAttribs, 0
        )

        checkEglError("eglCreatePbufferSurface")
        if (eglSurface == null) {
            throw RuntimeException("Could not create offscreen surface")
        }

        return eglSurface
    }

    fun makeCurrent(eglSurface: EGLSurface?) {
        if (mEGLDisplay === EGL14.EGL_NO_DISPLAY) {
            Log.d(TAG, "Display must be set before surface can be made current")
        }

        if (!EGL14.eglMakeCurrent(mEGLDisplay, eglSurface, eglSurface, mEGLContext)) {
            throw RuntimeException("Failed to make $eglSurface the display's current surface")
        }
    }

    /// For blitting between surfaces
    fun makeCurrent(drawSurface: EGLSurface?, readSurface: EGLSurface?) {
        if (mEGLDisplay === EGL14.EGL_NO_DISPLAY) {
            Log.d(TAG, "Display must be set before read/write surfaces")
        }

        if (!EGL14.eglMakeCurrent(mEGLDisplay, drawSurface, readSurface, mEGLContext)) {
            throw RuntimeException("Read/write combined call to eglMakeCurrent failed")
        }
    }

    fun naughtCurrent() {
        if (!EGL14.eglMakeCurrent(
                mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_CONTEXT
            )
        ) {
            throw RuntimeException("Cleanup call to eglMakeCurrent failed")
        }
    }

    fun setPresentationTime(eglSurface: EGLSurface?, nsecs: Long) {
        EGLExt.eglPresentationTimeANDROID(mEGLDisplay, eglSurface, nsecs)
    }

    fun swapBuffers(eglSurface: EGLSurface?): Boolean {
        return EGL14.eglSwapBuffers(mEGLDisplay, eglSurface)
    }

    fun isSurfaceCurrent(eglSurface: EGLSurface): Boolean {
        return mEGLContext == EGL14.eglGetCurrentContext() && eglSurface == EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW)
    }

    fun querySurface(eglSurface: EGLSurface?, what: Int): Int {
        val value = IntArray(1)
        EGL14.eglQuerySurface(mEGLDisplay, eglSurface, what, value, 0)
        return value[0]
    }

    fun queryString(which: Int): String {
        return EGL14.eglQueryString(mEGLDisplay, which)
    }

    private fun checkEglError(msg: String) {
        var error: Int
        if (EGL14.eglGetError().also { error = it } != EGL14.EGL_SUCCESS) {
            throw RuntimeException("$msg; Egl error code 0x${Integer.toHexString(error)}")
        }
    }

    /// Always call from the thread off which the context was originally obtained
    fun release() {
        if (mEGLDisplay !== EGL14.EGL_NO_DISPLAY) {
            EGL14.eglMakeCurrent(
                mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_CONTEXT
            )

            EGL14.eglDestroyContext(mEGLDisplay, mEGLContext)
            EGL14.eglReleaseThread()
            EGL14.eglTerminate(mEGLDisplay)
        }

        mEGLDisplay = EGL14.EGL_NO_DISPLAY
        mEGLContext = EGL14.EGL_NO_CONTEXT
        mEGLConfig = null
    }

    fun releaseSurface(eglSurface: EGLSurface?) {
        EGL14.eglDestroySurface(mEGLDisplay, eglSurface)
    }

    @Throws(Throwable::class)
    protected fun finalize() {
        if (mEGLDisplay !== EGL14.EGL_NO_DISPLAY) {
            Log.w(TAG, "Egl display still bound whilst finalizing")
            release()
        }
    }

    companion object {
        private val TAG = GlUtil.TAG
        const val FLAG_RECORDABLE = 0x01
        const val FLAG_TRY_GLES3 = 0x02
        private const val EGL_RECORDABLE_ANDROID = 0x3142

        fun logCurrent(msg: String) {
            val display: EGLDisplay = EGL14.eglGetCurrentDisplay()
            val context: EGLContext = EGL14.eglGetCurrentContext()
            val surface: EGLSurface = EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW)
            Log.i(TAG, "$msg (Egl): display $display, context $context, surface $surface")
        }
    }

    init {
        var localSharedContext = sharedContext
        if (mEGLDisplay !== EGL14.EGL_NO_DISPLAY) {
            throw RuntimeException("Attempt to initialize Egl twice")
        }

        if (localSharedContext == null) {
            localSharedContext = EGL14.EGL_NO_CONTEXT
        }

        mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (mEGLDisplay === EGL14.EGL_NO_DISPLAY) {
            throw RuntimeException("Failed to obtain Egl 14 display")
        }

        val version = IntArray(2)
        if (!EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1)) {
            mEGLDisplay = null
            throw RuntimeException("Failed to initialize Egl 14")
        }

        if (flags and FLAG_TRY_GLES3 != 0) {
            val config = getConfig(flags, 3)
            if (config != null) {
                val attrib3List = intArrayOf(
                    EGL14.EGL_CONTEXT_CLIENT_VERSION, 3,
                    EGL14.EGL_NONE
                )

                val context = EGL14.eglCreateContext(
                    mEGLDisplay, config, localSharedContext,
                    attrib3List, 0
                )

                if (EGL14.eglGetError() == EGL14.EGL_SUCCESS) {
                    mEGLConfig = config
                    mEGLContext = context
                    glVersion = 3
                }
            }
        }

        if (mEGLContext === EGL14.EGL_NO_CONTEXT) {
            val config = getConfig(flags, 2) ?: throw RuntimeException("Fallback to 2 EGL failed")
            val attrib2List = intArrayOf(
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
            )

            val context = EGL14.eglCreateContext(
                mEGLDisplay, config, localSharedContext,
                attrib2List, 0
            )

            checkEglError("eglCreateContext")
            mEGLConfig = config
            mEGLContext = context
            glVersion = 2
        }

        val values = IntArray(1)
        EGL14.eglQueryContext(
            mEGLDisplay, mEGLContext, EGL14.EGL_CONTEXT_CLIENT_VERSION,
            values, 0
        )

        Log.d(TAG, "EglContext version ${values[0]} created")
    }
}
