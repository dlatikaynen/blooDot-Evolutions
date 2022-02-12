package oy.sarjakuvat.flamingin.bde.rendition

import android.opengl.GLES20
import android.opengl.Matrix
import android.os.Looper
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import oy.sarjakuvat.flamingin.bde.GameActivity
import oy.sarjakuvat.flamingin.bde.R
import oy.sarjakuvat.flamingin.bde.audio.SoundOrchestrator
import oy.sarjakuvat.flamingin.bde.gles.*

class RenderThread(@field:Volatile private var renderSurfaceHolder: SurfaceHolder) : Thread() {
    private val startLockObject = Any()
    private val displayProjectionMatrix = FloatArray(16)
    private val viewportOrchestrator: ViewportOrchestrator = ViewportOrchestrator()
    private val spriteTriangle: Sprite2d
    private val spriteRectangle: Sprite2d
    private val floorSlivers: Array<Sprite2d> = Array(4) { Sprite2d(Drawable2dBase(Drawable2dBase.ShapePrimitive.CenteredUnitRect)) }
    private val rooofSlivers: Array<Sprite2d> = Array(4) { Sprite2d(Drawable2dBase(Drawable2dBase.ShapePrimitive.CenteredUnitRect)) }
    private val screenFrame: Array<Sprite2d?>

    private var isReady = false
    private var eglCoreInstance: EglCore? = null
    private var renderWindowSurface: WindowSurface? = null
    private var shaderFlat: ShaderFlatProgram? = null
    private var shaderTexture: ShaderTextureProgram? = null
    private var textureIdPicture = 0
    private var textureIdCoarse = 0
    private var textureIdFine = 0
    private var useFlatShader = false

    private var rectangleVelocityX = 0f
    private var rectangleVelocityY = 0f

    private var localScrollOffsetX = 120f
    private var localScrollOffsetY = 120f
    private var localScrollVelocity = 3.5f

    private var screenInnerLeft = 0f
    private var screenInnerTop = 0f
    private var screenInnerRight = 0f
    private var screenInnerBottom = 0f
    private var previousFrameTime: Long = 0
    private var sfXTimer: Long = 100

    @Volatile
    var handler: RenderHandler? = null
        private set

    override fun run() {
        Looper.prepare()
        handler = RenderHandler(this)
        eglCoreInstance = EglCore(null, EglCore.FLAG_TRY_GLES3)
        synchronized(startLockObject) {
            isReady = true
            @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
            (startLockObject as Object).notify()
        }

        Looper.loop()
        Log.d(GameActivity.TAG, "Exiting game loop in render thread")
        releaseGl()
        eglCoreInstance!!.release()
        synchronized(startLockObject) { isReady = false }
    }

    fun waitUntilReady() {
        synchronized(startLockObject) {
            while (!isReady) {
                try {
                    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
                    (startLockObject as Object).wait()
                } catch (ie: InterruptedException) {
                    /* cannot interrupt thread before it is ready */
                }
            }
        }
    }

    fun shutdown() {
        Log.d(GameActivity.TAG, "Render thread shutting down")
        Looper.myLooper()!!.quit()
    }

    fun surfaceCreated() {
        val surface = renderSurfaceHolder.surface
        prepareGl(surface)
    }

    private fun prepareGl(surface: Surface) {
        val resId = R.drawable.co_512

        Log.d(GameActivity.TAG, "prepareGl")
        renderWindowSurface = WindowSurface(eglCoreInstance, surface, false)
        renderWindowSurface!!.makeCurrent()
        shaderFlat = ShaderFlatProgram()
        shaderTexture = ShaderTextureProgram(ShaderTextureProgram.ProgramType.Texture2D)
        textureIdPicture = GeneratedTexture.createTestTexture(GeneratedTexture.Image.PICTURE, resId)
        textureIdCoarse = GeneratedTexture.createTestTexture(GeneratedTexture.Image.COARSE, 0)
        textureIdFine = GeneratedTexture.createTestTexture(GeneratedTexture.Image.FINE,  0)

        shareWithOffscreenFramebuffers()
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glDisable(GLES20.GL_CULL_FACE)
    }

    /// Attention: the actual new dimension will be available only after
    /// the next buffer swap. The dimensions passed to this call are the current ones ^-^
    fun surfaceChanged(width: Int, height: Int) {
        Log.d(GameActivity.TAG, "surfaceChanged from $width by $height")

        GLES20.glViewport(0, 0, width, height)
        Matrix.orthoM(displayProjectionMatrix, 0, 0f, width.toFloat(), 0f, height.toFloat(), -1f, 1f)
        val smallDim = width.coerceAtMost(height)
        spriteTriangle.setColor(0.1f, 0.9f, 0.1f)
        spriteTriangle.setTexture(textureIdFine)
        spriteTriangle.setScale(smallDim / 3.0f, smallDim / 3.0f)
        spriteTriangle.setPosition(width / 2.0f, height / 2.0f)
        spriteRectangle.setColor(0.9f, 0.1f, 0.1f)
        spriteRectangle.setTexture(textureIdCoarse)
        spriteRectangle.setScale(smallDim / 2.5f, smallDim / 2.5f)
        spriteRectangle.setPosition(width / 2.0f, height / 2.0f)
        rectangleVelocityX = 1 + smallDim / 2.0f
        rectangleVelocityY = 1 + smallDim / 2.5f

        /* slivers */
        for(i in floorSlivers.indices) {
            floorSlivers[i].setScale(width.toFloat(), height.toFloat())
            rooofSlivers[i].setScale(width.toFloat(), height.toFloat())
        }

        /* edge lines */
        val edgeWidth = 1 + width / 64.0f
        screenFrame[0]!!.setColor(0.5f, 0.5f, 0.5f)
        screenFrame[0]!!.setScale(edgeWidth, height.toFloat())
        screenFrame[0]!!.setPosition(edgeWidth / 2.0f, height / 2.0f)
        screenFrame[1]!!.setColor(0.5f, 0.5f, 0.5f)
        screenFrame[1]!!.setScale(edgeWidth, height.toFloat())
        screenFrame[1]!!.setPosition(width - edgeWidth / 2.0f, height / 2.0f)
        screenFrame[2]!!.setColor(0.5f, 0.5f, 0.5f)
        screenFrame[2]!!.setScale(width.toFloat(), edgeWidth)
        screenFrame[2]!!.setPosition(width / 2.0f, height - edgeWidth / 2.0f)
        screenFrame[3]!!.setColor(0.5f, 0.5f, 0.5f)
        screenFrame[3]!!.setScale(width.toFloat(), edgeWidth)
        screenFrame[3]!!.setPosition(width / 2.0f, edgeWidth / 2.0f)

        screenInnerLeft = edgeWidth
        screenInnerBottom = edgeWidth
        screenInnerRight = width - 1 - edgeWidth
        screenInnerTop = height - 1 - edgeWidth

        Log.d(GameActivity.TAG, "spriteTriangle: $spriteTriangle")
        Log.d(GameActivity.TAG, "spriteRectangle: $spriteRectangle")
        destroyOffscreenFramebuffers()
        prepareOffscreenFramebuffers(width, height)
        populateOffscreenFramebuffers()
    }

    private fun shareWithOffscreenFramebuffers() {
        viewportOrchestrator.shareWithOffscreenFramebuffers(textureIdPicture, shaderTexture!!)
    }

    private fun prepareOffscreenFramebuffers(width: Int, height: Int) {
        viewportOrchestrator.prepareOffscreenFramebuffers(width, height, displayProjectionMatrix)
    }

    private fun populateOffscreenFramebuffers() {
        viewportOrchestrator.populateOffscreenFramebuffers()
        associateViewportWithSlivers()
    }

    private fun associateViewportWithSlivers() {
        viewportOrchestrator.assignSliverPositionsAndTextures(floorSlivers, rooofSlivers)
    }

    private fun moveViewportSlivers() {
        viewportOrchestrator.assignSliverPositions(floorSlivers, rooofSlivers, localScrollOffsetX, localScrollOffsetY)
    }

    private fun destroyOffscreenFramebuffers() {
        /* only attempt deallocating if middle one indicates prior allocation */
        if(viewportOrchestrator.getViewportSliver(1,1).floorTextureId != 0) {
            viewportOrchestrator.destroyOffscreenFramebuffers()
        }
    }

    private fun releaseGl() {
        GlUtil.checkGlError("releaseGl::begin")
        destroyOffscreenFramebuffers()
        if (renderWindowSurface != null) {
            renderWindowSurface!!.release()
            renderWindowSurface = null
        }

        if (shaderFlat != null) {
            shaderFlat!!.release()
            shaderFlat = null
        }

        if (shaderTexture != null) {
            shaderTexture!!.release()
            shaderTexture = null
        }

        GlUtil.checkGlError("releaseGl::completed")
        eglCoreInstance!!.naughtCurrent()
    }

    fun setFlatShading(useFlatShading: Boolean) {
        useFlatShader = useFlatShading
    }

    fun doFrame(timeStampNanos: Long) {
        update(timeStampNanos)
        val diff = (System.nanoTime() - timeStampNanos) / 1000000
        if (diff > 15) {
            Log.d(GameActivity.TAG, "Took too long ($diff ms), skipping a frame rendition")
            return
        }

        draw()
        renderWindowSurface!!.swapBuffers()
    }

    private fun update(timeStampNanos: Long) {
        var intervalNanos: Long
        if (previousFrameTime == 0L) {
            intervalNanos = 0
        } else {
            intervalNanos = timeStampNanos - previousFrameTime
            val oneSecondNanos = 1000000000L
            if (intervalNanos > oneSecondNanos) {
                Log.d(GameActivity.TAG, "Maximum between-frames time exceeded (paused?): ${intervalNanos.toDouble() / oneSecondNanos} seconds")
                intervalNanos = 0
            }
        }

        --sfXTimer
        if(sfXTimer < 0) {
            sfXTimer = 75
            SoundOrchestrator.playSoundeffect(SoundOrchestrator.soundEffectOpening)
        }

        previousFrameTime = timeStampNanos
        val oneBillionF = 1000000000.0f
        val elapsedSeconds = intervalNanos / oneBillionF
        val secsPerSpin = 3
        val angleDelta = 360.0f / secsPerSpin * elapsedSeconds
        spriteTriangle.rotation = spriteTriangle.rotation + angleDelta

        var xpos = spriteRectangle.positionX
        var ypos = spriteRectangle.positionY
        val xscale = spriteRectangle.scaleX
        val yscale = spriteRectangle.scaleY
        xpos += rectangleVelocityX * elapsedSeconds
        ypos += rectangleVelocityY * elapsedSeconds
        if (rectangleVelocityX < 0 && xpos - xscale / 2 < screenInnerLeft || rectangleVelocityX > 0 && xpos + xscale / 2 > screenInnerRight + 100 + 1) {
            rectangleVelocityX = -rectangleVelocityX
        }

        if (rectangleVelocityY < 0 && ypos - yscale / 2 < screenInnerBottom || rectangleVelocityY > 0 && ypos + yscale / 2 > screenInnerTop + 1) {
            rectangleVelocityY = -rectangleVelocityY
        }

        spriteRectangle.setPosition(xpos, ypos)

        if(localScrollVelocity > 0) {
            localScrollOffsetX += localScrollVelocity
            if(localScrollOffsetX >= 120f) {
                localScrollOffsetX = 120f
                localScrollOffsetY = 120f
                localScrollVelocity = -localScrollVelocity
            }
            else {
                localScrollOffsetY += localScrollVelocity
            }
        }
        else {
            localScrollOffsetX += localScrollVelocity
            if(localScrollOffsetX <= -120f) {
                localScrollOffsetX = -120f
                localScrollOffsetY = -120f
                localScrollVelocity = -localScrollVelocity
            }
            else {
                localScrollOffsetY += localScrollVelocity
            }
        }

        moveViewportSlivers()
    }

    private fun draw() {
        GlUtil.checkGlError("draw::begin")
        GLES20.glClearColor(0.2f, 0.2f, 0.2f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // textures may include alpha, turn blending on
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        if (useFlatShader) {
            spriteTriangle.draw(shaderFlat!!, displayProjectionMatrix)
            spriteRectangle.draw(shaderFlat!!, displayProjectionMatrix)
        } else {
            for(i in floorSlivers.indices) {
                if(floorSlivers[i].hasTexture) {
                    floorSlivers[i].draw(shaderTexture!!, displayProjectionMatrix)
                }
            }

            spriteTriangle.draw(shaderTexture!!, displayProjectionMatrix)
            spriteRectangle.draw(shaderTexture!!, displayProjectionMatrix)
            for(i in rooofSlivers.indices) {
                if(rooofSlivers[i].hasTexture) {
                    rooofSlivers[i].draw(shaderTexture!!, displayProjectionMatrix)
                }
            }
        }

        GLES20.glDisable(GLES20.GL_BLEND)
//        for (i in 0..3) {
//            screenFrame[i]!!.draw(shaderFlat!!, displayProjectionMatrix)
//        }

        GlUtil.checkGlError("draw::completed")
    }

    init {
        val mIdentityMatrix = FloatArray(16)
        Matrix.setIdentityM(mIdentityMatrix, 0)
        val triangleDrawable = Drawable2dBase(Drawable2dBase.ShapePrimitive.IsocelesTriangle)
        spriteTriangle = Sprite2d(triangleDrawable)
        val rectDrawable = Drawable2dBase(Drawable2dBase.ShapePrimitive.CenteredUnitRect)
        spriteRectangle = Sprite2d(rectDrawable)
        screenFrame = arrayOfNulls(4)
        for (i in screenFrame.indices) {
            screenFrame[i] = Sprite2d(rectDrawable)
        }
    }
}
