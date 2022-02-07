package oy.sarjakuvat.flamingin.bde.gles

import oy.sarjakuvat.flamingin.bde.MainActivity.Companion.gpuSourceTextureVertexShader
import oy.sarjakuvat.flamingin.bde.MainActivity.Companion.gpuSourceTextureFragmentShader2d
import oy.sarjakuvat.flamingin.bde.MainActivity.Companion.gpuSourceTextureFragmentShaderExternal
import oy.sarjakuvat.flamingin.bde.MainActivity.Companion.gpuSourceTextureFragmentShaderExternalBw
import oy.sarjakuvat.flamingin.bde.MainActivity.Companion.gpuSourceTextureFragmentShaderConvolutions
import oy.sarjakuvat.flamingin.bde.gles.GlUtil.createProgram
import oy.sarjakuvat.flamingin.bde.gles.GlUtil.verifyLocLabel
import oy.sarjakuvat.flamingin.bde.gles.GlUtil.checkGlError
import android.opengl.GLES20
import android.opengl.GLES11Ext
import android.util.Log
import java.lang.RuntimeException
import java.nio.FloatBuffer

class ShaderTextureProgram(programType: ProgramType) {
    private val uniformProjectionMatrix: Int
    private val uniformTextureMatrix: Int
    private val attributePosition: Int
    private val attributeTextureCoordinate: Int
    private val convolutionKernel = FloatArray(CONVOLUTION_KERNEL_SIZE)

    private var programHandle = 0
    private var uniformConvolutionKernel: Int
    private var uniformTextureOffset = 0
    private var uniformColorAdjust = 0
    private var textureTarget = 0
    private var colorAdjustment = 0f

    private lateinit var textureOffset: FloatArray

    enum class ProgramType {
        Texture2D,
        TextureExternal,
        TextureExternalMono,
        TextureExternalFiltered
    }

    fun createTextureObject(): Int {
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        checkGlError("glGenTextures")
        val textureId = textures[0]
        GLES20.glBindTexture(textureTarget, textureId)
        checkGlError("glBindTexture $textureId")
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_NEAREST.toFloat()
        )

        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )

        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        checkGlError("glTexParameter")
        return textureId
    }

    private fun defineConvolutionKernel(
        values: FloatArray,
        @Suppress("SameParameterValue") colorAdj: Float
    ) {
        require(values.size == CONVOLUTION_KERNEL_SIZE) {
            "Convolution kernel size is ${values.size} where it should be $CONVOLUTION_KERNEL_SIZE"
        }

        System.arraycopy(values, 0, convolutionKernel, 0, CONVOLUTION_KERNEL_SIZE)
        colorAdjustment = colorAdj
    }

    private fun setTextureDimensions(
        @Suppress("SameParameterValue") width: Int,
        @Suppress("SameParameterValue") height: Int
    ) {
        val rw = 1.0f / width
        val rh = 1.0f / height
        textureOffset = floatArrayOf(
            -rw, -rh, 0f, -rh, rw, -rh,
            -rw, 0f, 0f, 0f, rw, 0f,
            -rw, rh, 0f, rh, rw, rh
        )
    }

    fun draw(
        mvpMatrix: FloatArray?,
        vertexBuffer: FloatBuffer?,
        firstVertex: Int,
        vertexCount: Int,
        coordsPerVertex: Int,
        vertexStride: Int,
        texMatrix: FloatArray?,
        texBuffer: FloatBuffer?,
        textureId: Int,
        texStride: Int
    ) {
        checkGlError("draw::begin")
        GLES20.glUseProgram(programHandle)
        checkGlError("glUseProgram")
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(textureTarget, textureId)
        GLES20.glUniformMatrix4fv(uniformProjectionMatrix, 1, false, mvpMatrix, 0)
        checkGlError("glUniformMatrix4fv")
        GLES20.glUniformMatrix4fv(uniformTextureMatrix, 1, false, texMatrix, 0)
        checkGlError("glUniformMatrix4fv")
        GLES20.glEnableVertexAttribArray(attributePosition)
        checkGlError("glEnableVertexAttribArray")
        GLES20.glVertexAttribPointer(
            attributePosition, coordsPerVertex,
            GLES20.GL_FLOAT, false, vertexStride, vertexBuffer
        )

        checkGlError("glVertexAttribPointer")
        GLES20.glEnableVertexAttribArray(attributeTextureCoordinate)
        checkGlError("glEnableVertexAttribArray")
        GLES20.glVertexAttribPointer(
            attributeTextureCoordinate, 2,
            GLES20.GL_FLOAT, false, texStride, texBuffer
        )

        checkGlError("glVertexAttribPointer")
        if (uniformConvolutionKernel >= 0) {
            GLES20.glUniform1fv(uniformConvolutionKernel, CONVOLUTION_KERNEL_SIZE, convolutionKernel, 0)
            GLES20.glUniform2fv(uniformTextureOffset, CONVOLUTION_KERNEL_SIZE, textureOffset, 0)
            GLES20.glUniform1f(uniformColorAdjust, colorAdjustment)
        }

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, firstVertex, vertexCount)
        checkGlError("glDrawArrays")

        GLES20.glDisableVertexAttribArray(attributePosition)
        GLES20.glDisableVertexAttribArray(attributeTextureCoordinate)
        GLES20.glBindTexture(textureTarget, 0)
        GLES20.glUseProgram(0)
    }

    fun release() {
        Log.d(TAG, "Deleting GPU shader program $programHandle")
        GLES20.glDeleteProgram(programHandle)
        checkGlError("glDeleteProgram")
        programHandle = -1
    }

    init {
        when (programType) {
            ProgramType.Texture2D -> {
                textureTarget = GLES20.GL_TEXTURE_2D
                programHandle = createProgram(VERTEX_SHADER, FRAGMENT_SHADER_2D)
            }
            ProgramType.TextureExternal -> {
                textureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES
                programHandle = createProgram(VERTEX_SHADER, FRAGMENT_SHADER_EXT)
            }
            ProgramType.TextureExternalMono -> {
                textureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES
                programHandle = createProgram(VERTEX_SHADER, FRAGMENT_SHADER_EXT_BW)
            }
            ProgramType.TextureExternalFiltered -> {
                textureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES
                programHandle = createProgram(VERTEX_SHADER, FRAGMENT_SHADER_EXT_FILT)
            }
        }

        if (programHandle == 0) {
            throw RuntimeException("Failed to create GPU shader program")
        }

        Log.d(TAG, "Created GPU $programType shader program $programHandle")
        attributePosition = GLES20.glGetAttribLocation(programHandle, "aPosition")
        verifyLocLabel(attributePosition, "aPosition")
        attributeTextureCoordinate = GLES20.glGetAttribLocation(programHandle, "aTextureCoord")
        verifyLocLabel(attributeTextureCoordinate, "aTextureCoord")
        uniformProjectionMatrix = GLES20.glGetUniformLocation(programHandle, "uMVPMatrix")
        verifyLocLabel(uniformProjectionMatrix, "uMVPMatrix")
        uniformTextureMatrix = GLES20.glGetUniformLocation(programHandle, "uTexMatrix")
        verifyLocLabel(uniformTextureMatrix, "uTexMatrix")
        uniformConvolutionKernel = GLES20.glGetUniformLocation(programHandle, "uKernel")
        if (uniformConvolutionKernel < 0) {
            uniformConvolutionKernel = -1
            uniformTextureOffset = -1
            uniformColorAdjust = -1
        } else {
            uniformTextureOffset = GLES20.glGetUniformLocation(programHandle, "uTexOffset")
            verifyLocLabel(uniformTextureOffset, "uTexOffset")
            uniformColorAdjust = GLES20.glGetUniformLocation(programHandle, "uColorAdjust")
            verifyLocLabel(uniformColorAdjust, "uColorAdjust")
            defineConvolutionKernel(floatArrayOf(0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f), 0f)
            setTextureDimensions(256, 256)
        }
    }

    companion object {
        const val CONVOLUTION_KERNEL_SIZE = 9

        private val TAG = GlUtil.TAG
        private val VERTEX_SHADER = gpuSourceTextureVertexShader
        private val FRAGMENT_SHADER_2D = gpuSourceTextureFragmentShader2d
        private val FRAGMENT_SHADER_EXT = gpuSourceTextureFragmentShaderExternal
        private val FRAGMENT_SHADER_EXT_BW = gpuSourceTextureFragmentShaderExternalBw
        private val FRAGMENT_SHADER_EXT_FILT = gpuSourceTextureFragmentShaderConvolutions
    }
}
