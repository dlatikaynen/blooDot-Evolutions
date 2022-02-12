package oy.sarjakuvat.flamingin.bde.rendition

import oy.sarjakuvat.flamingin.bde.gles.Drawable2dBase
import java.lang.RuntimeException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/// Zoomable primitive
class ScaledDrawable2d(shape: ShapePrimitive?) : Drawable2dBase(shape!!) {
    private var textureCoordinates: FloatBuffer? = null
    private var scalingFactor = 1.0f
    private var recalculationPending = true

    fun setScale(scale: Float) {
        if (scale < 0.0f || scale > 1.0f) {
            throw RuntimeException("The scale needs to be in [0..1], $scale is not")
        }

        scalingFactor = scale
        recalculationPending = true
    }

    override var textureVertexArray: FloatBuffer?
        get() {
            if (recalculationPending) {
                val parentBuf = super.textureVertexArray
                val count = parentBuf!!.capacity()
                if (textureCoordinates == null) {
                    val bb = ByteBuffer.allocateDirect(count * SIZEOF_FLOAT)
                    bb.order(ByteOrder.nativeOrder())
                    textureCoordinates = bb.asFloatBuffer()
                }

                val fb = textureCoordinates
                val scale = scalingFactor
                for (i in 0 until count) {
                    var fl = parentBuf[i]
                    fl = (fl - 0.5f) * scale + 0.5f
                    fb!!.put(i, fl)
                }

                recalculationPending = false
            }

            return textureCoordinates
        }
        set(texCoordArray) {
            super.textureVertexArray = texCoordArray
        }

    companion object {
        private const val SIZEOF_FLOAT = 4
    }
}
