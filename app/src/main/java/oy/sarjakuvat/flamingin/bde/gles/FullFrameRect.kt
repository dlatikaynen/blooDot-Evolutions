package oy.sarjakuvat.flamingin.bde.gles

class FullFrameRect(private var program: ShaderTextureProgram?) {
    private val rectangleDrawable = Drawable2dBase(Drawable2dBase.ShapePrimitive.ScreenRect)

    fun changeProgram(program: ShaderTextureProgram?) {
        program!!.release()
        this.program = program
    }

    fun createTextureObject(): Int {
        return program!!.createTextureObject()
    }

    fun drawFrame(textureId: Int, texMatrix: FloatArray?) {
        program!!.draw(
            GlUtil.IDENTITY_MATRIX,
            rectangleDrawable.vertexArray,
            0,
            rectangleDrawable.vertexCount,
            rectangleDrawable.coordsPerVertex,
            rectangleDrawable.vertexStride,
            texMatrix,
            rectangleDrawable.textureVertexArray,
            textureId,
            rectangleDrawable.texCoordStride
        )
    }

    fun release(doEglCleanup: Boolean) {
        if (program != null) {
            if (doEglCleanup) {
                program!!.release()
            }

            program = null
        }
    }
}
