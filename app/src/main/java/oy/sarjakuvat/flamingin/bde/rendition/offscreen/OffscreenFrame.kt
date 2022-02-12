package oy.sarjakuvat.flamingin.bde.rendition.offscreen

import android.opengl.GLES20
import android.opengl.GLES30
import android.util.Log
import oy.sarjakuvat.flamingin.bde.gles.*
import java.lang.RuntimeException

class OffscreenFrame {
    private var width: Int = 0
    private var height: Int = 0
    private var projectionMatrix = FloatArray(16)

    private var frameBufferId = 0
    private var containedTextureId = 0
    private var renderBufferId = 0
    private var sourceTextureId = 0
    private lateinit var textureShaderId: ShaderTextureProgram

    fun initializeSharedGpuNames(sharedSourceTextureId: Int, sharedTextureProgram: ShaderTextureProgram) {
        sourceTextureId = sharedSourceTextureId
        textureShaderId = sharedTextureProgram
    }

    fun prepareOffscreenFramebuffer(newWidth: Int, newHeight: Int, newProjectionMatrix: FloatArray) {
        val values = IntArray(1)
        width = newWidth
        height = newHeight
        projectionMatrix = newProjectionMatrix

        GlUtil.checkGlError("prepareOffscreenFramebuffer::begin")
        GLES20.glGenTextures(1, values, 0)
        GlUtil.checkGlError("glGenTextures")
        containedTextureId = values[0] // expected > 0
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, containedTextureId)
        GlUtil.checkGlError("glBindTexture $containedTextureId")
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
            GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null
        )

        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_NEAREST.toFloat()
        )

        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )

        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )

        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )

        GlUtil.checkGlError("glTexParameter")
        GLES20.glGenFramebuffers(1, values, 0)
        GlUtil.checkGlError("glGenFramebuffers")
        frameBufferId = values[0]
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferId)
        GlUtil.checkGlError("glBindFramebuffer $frameBufferId")
        GLES20.glGenRenderbuffers(1, values, 0)
        GlUtil.checkGlError("glGenRenderbuffers")
        renderBufferId = values[0]
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, renderBufferId)
        GlUtil.checkGlError("glBindRenderbuffer $renderBufferId")
        GLES20.glRenderbufferStorage(
            GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16,
            width, height
        )

        GlUtil.checkGlError("glRenderbufferStorage")
        GLES20.glFramebufferRenderbuffer(
            GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT,
            GLES20.GL_RENDERBUFFER, renderBufferId
        )

        GlUtil.checkGlError("glFramebufferRenderbuffer")
        GLES20.glFramebufferTexture2D(
            GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
            GLES20.GL_TEXTURE_2D, containedTextureId, 0
        )

        GlUtil.checkGlError("glFramebufferTexture2D")
        val status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            throw RuntimeException("Offline framebuffer not complete, status=$status")
        }

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        GlUtil.checkGlError("prepareOfflineFramebuffers done")
    }

    fun populate(useTexture: Int = sourceTextureId) {
        val mPictDrawable = Drawable2dBase(Drawable2dBase.ShapePrimitive.CenteredUnitRect)
        val mPict = Sprite2d(mPictDrawable)
        mPict.setTexture(useTexture)
        mPict.setScale(width.toFloat(), height.toFloat())
        mPict.setPosition(width / 2f, height / 2f)
        Log.d(OffscreenFrame::class.simpleName, "mPict: $mPict")
        GlUtil.checkGlError("drawToOffscreenFramebuffers start")
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferId)
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glDisable(GLES20.GL_CULL_FACE)
        GLES20.glClearColor(0f, 0f, 0f, 0.5f) /* initialize with transparency */
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // Textures may include alpha
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        mPict.draw(textureShaderId, projectionMatrix)
        GLES20.glDisable(GLES20.GL_BLEND)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        GlUtil.checkGlError("drawToOffscreenFramebuffers complete")
    }

    fun destroyOffscreenFramebuffer() {
        val values = IntArray(1)
        if (frameBufferId != 0) {
            values[0] = renderBufferId
            GLES20.glDeleteRenderbuffers(1, values, 0)
            GlUtil.checkGlError("glDeleteRenderbuffers")
            renderBufferId = 0

            values[0] = frameBufferId
            GLES20.glDeleteFramebuffers(1, values, 0)
            GlUtil.checkGlError("glDeleteFramebuffers")
            frameBufferId = 0

            values[0] = containedTextureId
            GLES20.glDeleteTextures(1, values, 0)
            GlUtil.checkGlError("glDeleteTextures")
            containedTextureId = 0
        }
    }
}
