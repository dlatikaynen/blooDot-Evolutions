package oy.sarjakuvat.flamingin.bde.gles

import java.nio.FloatBuffer

open class Drawable2dBase(shape: ShapePrimitive) {
    val texCoordStride: Int
    private val shapePrimitive: ShapePrimitive

    open var textureVertexArray: FloatBuffer? = null
    var vertexArray: FloatBuffer? = null
    var vertexCount = 0
    var coordsPerVertex = 0
    var vertexStride = 0

    enum class ShapePrimitive {
        IsocelesTriangle,
        CenteredUnitRect,
        ScreenRect
    }

    override fun toString(): String {
        return "[${Drawable2dBase::class.simpleName}: $shapePrimitive]"
    }

    companion object {
        private const val FLOAT_SIZE = 4
        private val ISOCELES_VERTICES = floatArrayOf(
            0.0f, 0.577350269f,
            -0.5f, -0.288675135f,
            0.5f, -0.288675135f
        )

        private val ISOCELES_TEXTURE_VERTICES = floatArrayOf(
            0.5f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f
        )

        private val RECT_VERTICES = floatArrayOf(
            -0.5f, -0.5f,
            0.5f, -0.5f,
            -0.5f, 0.5f,
            0.5f, 0.5f
        )

        private val RECT_TEXTURE_VERTICES = floatArrayOf(
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f
        )

        private val SCREEN_VERTICES = floatArrayOf(
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f
        )

        private val SCREEN_TEXTURE_VERTICES = floatArrayOf(
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f
        )

        private val isocelesBuffer = GlUtil.createFloatBuffer(ISOCELES_VERTICES)
        private val isocelesTextureBuffer = GlUtil.createFloatBuffer(ISOCELES_TEXTURE_VERTICES)
        private val rectBuffer = GlUtil.createFloatBuffer(RECT_VERTICES)
        private val rectTextureBuffer = GlUtil.createFloatBuffer(RECT_TEXTURE_VERTICES)
        private val screenRectBuffer = GlUtil.createFloatBuffer(SCREEN_VERTICES)
        private val screenRectTextureBuffer = GlUtil.createFloatBuffer(SCREEN_TEXTURE_VERTICES)
    }

    init {
        @Suppress("LeakingThis")
        when (shape) {
            ShapePrimitive.IsocelesTriangle -> {
                vertexArray = isocelesBuffer
                textureVertexArray = isocelesTextureBuffer
                coordsPerVertex = 2
                vertexStride = coordsPerVertex * FLOAT_SIZE
                vertexCount = ISOCELES_VERTICES.size / coordsPerVertex
            }
            ShapePrimitive.CenteredUnitRect -> {
                vertexArray = rectBuffer
                textureVertexArray = rectTextureBuffer
                coordsPerVertex = 2
                vertexStride = coordsPerVertex * FLOAT_SIZE
                vertexCount = RECT_VERTICES.size / coordsPerVertex
            }
            ShapePrimitive.ScreenRect -> {
                vertexArray = screenRectBuffer
                textureVertexArray = screenRectTextureBuffer
                coordsPerVertex = 2
                vertexStride = coordsPerVertex * FLOAT_SIZE
                vertexCount = SCREEN_VERTICES.size / coordsPerVertex
            }
        }

        texCoordStride = 2 * FLOAT_SIZE
        shapePrimitive = shape
    }
}
