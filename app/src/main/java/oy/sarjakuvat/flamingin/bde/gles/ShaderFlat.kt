package oy.sarjakuvat.flamingin.bde.gles

import oy.sarjakuvat.flamingin.bde.MainActivity.Companion.gpuSourceFlatVertexShader
import oy.sarjakuvat.flamingin.bde.MainActivity.Companion.gpuSourceFlatFragmentShader
import android.opengl.GLES20
import android.util.Log
import java.lang.RuntimeException
import java.nio.FloatBuffer

class ShaderFlatProgram {
    private var shaderProgramHandle: Int
    private val uniformColorLoc: Int
    private val uniformMatrixLoc: Int
    private val attributePositionLoc: Int

    fun draw(
        mvpMatrix: FloatArray?, color: FloatArray?, vertexBuffer: FloatBuffer?,
        firstVertex: Int, vertexCount: Int, coordsPerVertex: Int, vertexStride: Int
    ) {
        GlUtil.checkGlError("draw::begin")
        GLES20.glUseProgram(shaderProgramHandle)
        GlUtil.checkGlError("glUseProgram")
        GLES20.glUniformMatrix4fv(uniformMatrixLoc, 1, false, mvpMatrix, 0)
        GlUtil.checkGlError("glUniformMatrix4fv")
        GLES20.glUniform4fv(uniformColorLoc, 1, color, 0)
        GlUtil.checkGlError("glUniform4fv ")
        GLES20.glEnableVertexAttribArray(attributePositionLoc)
        GlUtil.checkGlError("glEnableVertexAttribArray")
        GLES20.glVertexAttribPointer(
            attributePositionLoc, coordsPerVertex,
            GLES20.GL_FLOAT, false, vertexStride, vertexBuffer
        )

        GlUtil.checkGlError("glVertexAttribPointer")
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, firstVertex, vertexCount)
        GlUtil.checkGlError("glDrawArrays")
        GLES20.glDisableVertexAttribArray(attributePositionLoc)
        GLES20.glUseProgram(0)
    }

    fun release() {
        GLES20.glDeleteProgram(shaderProgramHandle)
        GlUtil.checkGlError("glDeleteProgram")
        shaderProgramHandle = -1
    }

    companion object {
        private val TAG = GlUtil.TAG
        private val VERTEX_SHADER = gpuSourceFlatVertexShader
        private val FRAGMENT_SHADER = gpuSourceFlatFragmentShader
    }

    init {
        shaderProgramHandle = GlUtil.createProgram(VERTEX_SHADER, FRAGMENT_SHADER)
        if (shaderProgramHandle == 0) {
            throw RuntimeException("Failed to create GPU program")
        }

        Log.d(TAG, "GPU shader program $shaderProgramHandle created")
        attributePositionLoc = GLES20.glGetAttribLocation(shaderProgramHandle, "aPosition")
        GlUtil.verifyLocLabel(attributePositionLoc, "aPosition")
        uniformMatrixLoc = GLES20.glGetUniformLocation(shaderProgramHandle, "uMVPMatrix")
        GlUtil.verifyLocLabel(uniformMatrixLoc, "uMVPMatrix")
        uniformColorLoc = GLES20.glGetUniformLocation(shaderProgramHandle, "uColor")
        GlUtil.verifyLocLabel(uniformColorLoc, "uColor")
    }
}
