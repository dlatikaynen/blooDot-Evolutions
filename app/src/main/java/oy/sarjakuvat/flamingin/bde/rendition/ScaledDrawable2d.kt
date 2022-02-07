package oy.sarjakuvat.flamingin.bde.rendition

import oy.sarjakuvat.flamingin.bde.gles.Drawable2dBase
import java.lang.RuntimeException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/// Zoomable primitive
class ScaledDrawable2d(shape: ShapePrimitive?) : Drawable2dBase(shape!!) {
    private var mTweakedTexCoordArray: FloatBuffer? = null
    private var mScale = 1.0f
    private var mRecalculate = true

    fun setScale(scale: Float) {
        if (scale < 0.0f || scale > 1.0f) {
            throw RuntimeException("The scale needs to be in [0..1], $scale is not")
        }

        mScale = scale
        mRecalculate = true
    }

    override var textureVertexArray: FloatBuffer?
        get() {
            if (mRecalculate) {
                val parentBuf = super.textureVertexArray
                val count = parentBuf!!.capacity()
                if (mTweakedTexCoordArray == null) {
                    val bb = ByteBuffer.allocateDirect(count * SIZEOF_FLOAT)
                    bb.order(ByteOrder.nativeOrder())
                    mTweakedTexCoordArray = bb.asFloatBuffer()
                }

                val fb = mTweakedTexCoordArray
                val scale = mScale
                for (i in 0 until count) {
                    var fl = parentBuf[i]
                    fl = (fl - 0.5f) * scale + 0.5f
                    fb!!.put(i, fl)
                }

                mRecalculate = false
            }

            return mTweakedTexCoordArray
        }
        set(texCoordArray) {
            super.textureVertexArray = texCoordArray
        }

    companion object {
        private const val SIZEOF_FLOAT = 4
    }
}
