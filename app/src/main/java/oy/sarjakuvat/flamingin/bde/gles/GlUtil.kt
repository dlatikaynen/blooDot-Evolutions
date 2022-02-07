package oy.sarjakuvat.flamingin.bde.gles

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import android.opengl.Matrix
import android.util.Log
import java.lang.RuntimeException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

object GlUtil {
    private const val SIZEOF_FLOAT = 4

    @JvmField
    val IDENTITY_MATRIX: FloatArray = FloatArray(16)
    val TAG = GlUtil::class.simpleName!!

    @JvmStatic
    fun createProgram(vertexSource: String?, fragmentSource: String?): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        if (vertexShader == 0) {
            return 0
        }

        val pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
        if (pixelShader == 0) {
            return 0
        }

        var program = GLES20.glCreateProgram()
        checkGlError("glCreateProgram")
        if (program == 0) {
            Log.e(TAG, "Could not create program")
        }

        GLES20.glAttachShader(program, vertexShader)
        checkGlError("glAttachShader")
        GLES20.glAttachShader(program, pixelShader)
        checkGlError("glAttachShader")
        GLES20.glLinkProgram(program)
        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] != GLES20.GL_TRUE) {
            Log.e(TAG, "Failed to link GPU shader program $program")
            Log.e(TAG, GLES20.glGetProgramInfoLog(program))
            GLES20.glDeleteProgram(program)
            program = 0
        }

        return program
    }

    private fun loadShader(shaderType: Int, source: String?): Int {
        var shader = GLES20.glCreateShader(shaderType)
        checkGlError("glCreateShader type $shaderType")
        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)
        val compiled = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            Log.e(TAG, "Failed to compile $shaderType GPU shader program $shader")
            Log.e(TAG, GLES20.glGetShaderInfoLog(shader))
            GLES20.glDeleteShader(shader)
            shader = 0
        }

        return shader
    }

    @JvmStatic
    fun checkGlError(op: String) {
        val error = GLES20.glGetError()
        if (error != GLES20.GL_NO_ERROR) {
            val msg = "$op caused glError code 0x${Integer.toHexString(error)}"
            Log.e(TAG, msg)
            throw RuntimeException(msg)
        }
    }

    @JvmStatic
    fun verifyLocLabel(location: Int, label: String) {
        if (location < 0) {
            throw RuntimeException("Failed to locate $label in GPU shader program")
        }
    }

    fun createImageTexture(data: ByteBuffer?, width: Int, height: Int, format: Int): Int {
        val textureHandles = IntArray(1)
        GLES20.glGenTextures(1, textureHandles, 0)
        val textureHandle: Int = textureHandles[0]
        checkGlError("glGenTextures")
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle)
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_LINEAR
        )

        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR
        )

        checkGlError("glBindTexture")
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D,
            0,
            format,
            width,
            height,
            0,
            format,
            GLES20.GL_UNSIGNED_BYTE,
            data
        )

        checkGlError("glTexImage2D")
        return textureHandle
    }

    fun createBitmapTexture(bitmap: Bitmap): Int {
        val textureHandles = IntArray(1)
        GLES20.glGenTextures(1, textureHandles, 0)
        val textureHandle: Int = textureHandles[0]
        checkGlError("createBitmapTexture")
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle)
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_LINEAR
        )

        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR
        )

        checkGlError("glTexParameteri")
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        checkGlError("texImage2D")
        return textureHandle
    }

    fun createFloatBuffer(coords: FloatArray): FloatBuffer {
        val bb = ByteBuffer.allocateDirect(coords.size * SIZEOF_FLOAT)
        bb.order(ByteOrder.nativeOrder())
        val fb = bb.asFloatBuffer()
        fb.put(coords)
        fb.position(0)
        return fb
    }

    fun logVersionInfo() {
        Log.i(TAG, "GLES vendor:   ${GLES20.glGetString(GLES20.GL_VENDOR)}")
        Log.i(TAG, "GLES renderer: ${GLES20.glGetString(GLES20.GL_RENDERER)}")
        Log.i(TAG, "GLES version:  ${GLES20.glGetString(GLES20.GL_VERSION)}")
    }

    init {
        Matrix.setIdentityM(IDENTITY_MATRIX, 0)
    }
}
