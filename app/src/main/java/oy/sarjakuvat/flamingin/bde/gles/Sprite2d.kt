package oy.sarjakuvat.flamingin.bde.gles

import android.opengl.Matrix

class Sprite2d(private val drawablePrimitive: Drawable2dBase) {
    private val spriteBaseColor: FloatArray = FloatArray(4)
    private val modelViewProjectionMatrix: FloatArray
    private val blankMatrix = FloatArray(16)

    private var isMatrixReady: Boolean
    private var spriteTextureId: Int
    private var rotationAngle = 0f

    var scaleX = 0f
        private set

    var scaleY = 0f
        private set

    var positionX = 0f
        private set

    var positionY = 0f
        private set

    val hasTexture : Boolean get() = spriteTextureId != 0

    private fun recomputeMatrix() {
        val modelView = modelViewProjectionMatrix
        Matrix.setIdentityM(modelView, 0)
        Matrix.translateM(modelView, 0, positionX, positionY, 0.0f)
        if (rotationAngle != 0.0f) {
            Matrix.rotateM(modelView, 0, rotationAngle, 0.0f, 0.0f, 1.0f)
        }
        Matrix.scaleM(modelView, 0, scaleX, scaleY, 1.0f)
        isMatrixReady = true
    }

    fun setScale(scaleX: Float, scaleY: Float) {
        this.scaleX = scaleX
        this.scaleY = scaleY
        isMatrixReady = false
    }

    var rotation: Float
        get() = rotationAngle
        set(angle) {
            var localAngle = angle
            while (localAngle >= 360.0f) {
                localAngle -= 360.0f
            }

            while (localAngle <= -360.0f) {
                localAngle += 360.0f
            }

            rotationAngle = localAngle
            isMatrixReady = false
        }

    fun setPosition(posX: Float, posY: Float) {
        positionX = posX
        positionY = posY
        isMatrixReady = false
    }

    private val modelViewMatrix: FloatArray
        get() {
            if (!isMatrixReady) {
                recomputeMatrix()
            }

            return modelViewProjectionMatrix
        }

    fun setColor(red: Float, green: Float, blue: Float) {
        spriteBaseColor[0] = red
        spriteBaseColor[1] = green
        spriteBaseColor[2] = blue
    }

    fun setTexture(textureId: Int) {
        spriteTextureId = textureId
    }

    fun draw(program: ShaderFlatProgram, projectionMatrix: FloatArray?) {
        Matrix.multiplyMM(
            blankMatrix,
            0,
            projectionMatrix,
            0,
            modelViewMatrix,
            0
        )

        program.draw(
            blankMatrix,
            spriteBaseColor,
            drawablePrimitive.vertexArray,
            0,
            drawablePrimitive.vertexCount,
            drawablePrimitive.coordsPerVertex,
            drawablePrimitive.vertexStride
        )
    }

    fun draw(program: ShaderTextureProgram, projectionMatrix: FloatArray?) {
        Matrix.multiplyMM(blankMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0)
        program.draw(
            blankMatrix, drawablePrimitive.vertexArray, 0,
            drawablePrimitive.vertexCount, drawablePrimitive.coordsPerVertex,
            drawablePrimitive.vertexStride, GlUtil.IDENTITY_MATRIX, drawablePrimitive.textureVertexArray,
            spriteTextureId, drawablePrimitive.texCoordStride
        )
    }

    override fun toString(): String {
        return "[Sprite is $drawablePrimitive at $positionX,$positionY sized $scaleX,$scaleY rotated $rotationAngle colored ${spriteBaseColor[0]},${spriteBaseColor[1]},${spriteBaseColor[2]}"
    }

    init {
        spriteBaseColor[3] = 1.0f
        spriteTextureId = -1
        modelViewProjectionMatrix = FloatArray(16)
        isMatrixReady = false
    }
}
