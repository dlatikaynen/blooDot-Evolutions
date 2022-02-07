package oy.sarjakuvat.flamingin.bde.rendition.offscreen

import android.opengl.GLES20
import android.opengl.GLES30
import android.util.Log
import oy.sarjakuvat.flamingin.bde.gles.*
import java.lang.RuntimeException
import kotlin.random.Random

class OffscreenFrame {
    private var width: Int = 0
    private var height: Int = 0
    private var projectionMatrix = FloatArray(16)

    private var mOffscreenFramebuffer = 0
    private var mOffscreenTexture = 0
    private var mOffscreenRenderBuffer = 0
    private var mTextureName = 0
    private lateinit var mTextureProgram: ShaderTextureProgram

    fun initializeSharedGpuNames(sharedTextureName: Int, sharedTextureProgram: ShaderTextureProgram) {
        mTextureName = sharedTextureName
        mTextureProgram = sharedTextureProgram
    }

    fun prepareOffscreenFramebuffer(newWidth: Int, newHeight: Int, newProjectionMatrix: FloatArray) {
        val values = IntArray(1)
        width = newWidth
        height = newHeight
        projectionMatrix = newProjectionMatrix

        GlUtil.checkGlError("prepareOffscreenFramebuffer::begin")
        GLES20.glGenTextures(1, values, 0)
        GlUtil.checkGlError("glGenTextures")
        mOffscreenTexture = values[0] // expected > 0
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mOffscreenTexture)
        GlUtil.checkGlError("glBindTexture $mOffscreenTexture")
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
        mOffscreenFramebuffer = values[0] // expected > 0
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mOffscreenFramebuffer)
        GlUtil.checkGlError("glBindFramebuffer $mOffscreenFramebuffer")
        GLES20.glGenRenderbuffers(1, values, 0)
        GlUtil.checkGlError("glGenRenderbuffers")
        mOffscreenRenderBuffer = values[0] // expected > 0
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, mOffscreenRenderBuffer)
        GlUtil.checkGlError("glBindRenderbuffer $mOffscreenRenderBuffer")
        GLES20.glRenderbufferStorage(
            GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16,
            width, height
        )

        GlUtil.checkGlError("glRenderbufferStorage")
        GLES20.glFramebufferRenderbuffer(
            GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT,
            GLES20.GL_RENDERBUFFER, mOffscreenRenderBuffer
        )

        GlUtil.checkGlError("glFramebufferRenderbuffer")
        GLES20.glFramebufferTexture2D(
            GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
            GLES20.GL_TEXTURE_2D, mOffscreenTexture, 0
        )

        GlUtil.checkGlError("glFramebufferTexture2D")
        val status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            throw RuntimeException("Offline framebuffer not complete, status=$status")
        }

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        GlUtil.checkGlError("prepareOfflineFramebuffers done")
    }

    fun populate(textureName: Int = mTextureName) {
        val mPictDrawable = Drawable2dBase(Drawable2dBase.ShapePrimitive.CenteredUnitRect)
        val mPict = Sprite2d(mPictDrawable)
        mPict.setTexture(textureName)
        mPict.setScale(width.toFloat(), height.toFloat())
        mPict.setPosition(width / 2f, height / 2f)
        Log.d(OffscreenFrame::class.simpleName, "mPict: $mPict")
        GlUtil.checkGlError("drawToOffscreenFramebuffers start")
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mOffscreenFramebuffer)
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glDisable(GLES20.GL_CULL_FACE)
        GLES20.glClearColor(Random.nextFloat(), Random.nextFloat(), Random.nextFloat(), 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // Textures may include alpha
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        mPict.draw(mTextureProgram, projectionMatrix)
        GLES20.glDisable(GLES20.GL_BLEND)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        GlUtil.checkGlError("drawToOffscreenFramebuffers complete")
    }

    fun blit(
        srcLeft: Int,
        srcTop: Int,
        srcRight: Int,
        srcBottom: Int,
        dstLeft: Int,
        dstTop: Int,
        dstRight: Int,
        dstBottom: Int
    ) {
        GLES30.glBindFramebuffer(GLES30.GL_READ_FRAMEBUFFER, mOffscreenFramebuffer)
        GLES30.glBlitFramebuffer(
            srcLeft, srcTop, srcRight, srcBottom,
            dstLeft, dstTop, dstRight, dstBottom,
            GLES30.GL_COLOR_BUFFER_BIT,
            GLES30.GL_NEAREST
        )

        var errNo: Int
        if (GLES30.glGetError().also { errNo = it } != GLES30.GL_NO_ERROR) {
            Log.w(
                OffscreenFrame::class.simpleName,
                "Failed to glBlitFramebuffer with error code 0x${Integer.toHexString(errNo)}"
            )
        }
    }

    fun destroyOffscreenFramebuffer() {
        val values = IntArray(1)
        if (mOffscreenFramebuffer != 0) {
            values[0] = mOffscreenRenderBuffer
            GLES20.glDeleteRenderbuffers(1, values, 0)
            GlUtil.checkGlError("glDeleteRenderbuffers")
            values[0] = mOffscreenFramebuffer
            GLES20.glDeleteFramebuffers(1, values, 0)
            GlUtil.checkGlError("glDeleteFramebuffers")
            values[0] = mOffscreenTexture
            GLES20.glDeleteTextures(1, values, 0)
            GlUtil.checkGlError("glDeleteTextures")
            mOffscreenFramebuffer = 0
        }
    }
}
