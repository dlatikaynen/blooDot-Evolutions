package oy.sarjakuvat.flamingin.bde.gles

import android.graphics.BitmapFactory
import android.opengl.GLES20
import oy.sarjakuvat.flamingin.bde.App
import java.nio.ByteBuffer

/// Full disclosure:
/// The pattern of the triangle and the rectangle
/// are taken from one of the Google Grafika demo
/// project, and are kept around for our internal test purposes only.
/// This class will not exist in the published version
/// https://github.com/google/grafika
/// https://github.com/google/grafika/blob/master/LICENSE
object GeneratedTexture {
    private const val BLACK = 0x00000000
    private const val RED = 0x000000ff
    private const val GREEN = 0x0000ff00
    private const val BLUE = 0x00ff0000
    private const val MAGENTA = RED or BLUE
    private const val YELLOW = RED or GREEN
    private const val CYAN = GREEN or BLUE
    private const val WHITE = RED or GREEN or BLUE
    private const val OPAQUE = 0xff000000L.toInt()
    private const val HALF = 0x80000000L.toInt()
    private const val LOW = 0x40000000L.toInt()
    private const val TRANSP = 0
    private const val TEX_SIZE = 512
    private const val FORMAT = GLES20.GL_RGBA
    private const val BYTES_PER_PIXEL = 4

    private val GRID = intArrayOf(
        OPAQUE or RED, OPAQUE or YELLOW, OPAQUE or GREEN, OPAQUE or MAGENTA,
        OPAQUE or WHITE, LOW or RED, LOW or GREEN, OPAQUE or YELLOW,
        OPAQUE or MAGENTA, TRANSP or GREEN, HALF or RED, OPAQUE or BLACK,
        OPAQUE or CYAN, OPAQUE or MAGENTA, OPAQUE or CYAN, OPAQUE or BLUE
    )

    private val sCoarseImageData = generateCoarseData()
    private val sFineImageData = generateFineData()

    fun createTestTexture(which: Image, resId: Int): Int {
        val buf: ByteBuffer = when (which) {
            Image.PICTURE -> {
                /* do not premultiply alpha */
                val options = BitmapFactory.Options()
                options.inScaled = false
                val bitmap = BitmapFactory.decodeResource(App.context.get()!!.resources, resId, options)
                val textureName = GlUtil.createBitmapTexture(bitmap)
                bitmap.recycle()
                return textureName
            }
            Image.COARSE -> sCoarseImageData
            Image.FINE -> sFineImageData
        }

        return GlUtil.createImageTexture(buf, TEX_SIZE, TEX_SIZE, FORMAT)
    }

    private fun generateCoarseData(): ByteBuffer {
        val buf = ByteArray(TEX_SIZE * TEX_SIZE * BYTES_PER_PIXEL)
        val scale = TEX_SIZE / 4 // pixelate, 64 by 64 to become 4 by 4
        var i = 0
        while (i < buf.size) {
            val texRow = i / BYTES_PER_PIXEL / TEX_SIZE
            val texCol = i / BYTES_PER_PIXEL % TEX_SIZE
            val gridRow = texRow / scale // 0-3
            val gridCol = texCol / scale // 0-3
            val gridIndex = gridRow * 4 + gridCol // 0-15
            var color = GRID[gridIndex]

            // override the pixels in two corners to check coverage
            if (i == 0) {
                color = OPAQUE or WHITE
            } else if (i == buf.size - BYTES_PER_PIXEL) {
                color = OPAQUE or WHITE
            }

            // extract unsigned color components
            val red = color and 0xff
            val green = color shr 8 and 0xff
            val blue = color shr 16 and 0xff
            val alpha = color shr 24 and 0xff

            // pre-multiply colors and store in buffer
            val alphaM = alpha / 255.0f
            buf[i] = (red * alphaM).toInt().toByte()
            buf[i + 1] = (green * alphaM).toInt().toByte()
            buf[i + 2] = (blue * alphaM).toInt().toByte()
            buf[i + 3] = alpha.toByte()
            i += BYTES_PER_PIXEL
        }

        val byteBuf = ByteBuffer.allocateDirect(buf.size)
        byteBuf.put(buf)
        byteBuf.position(0)
        return byteBuf
    }

    private fun generateFineData(): ByteBuffer {
        val buf = ByteArray(TEX_SIZE * TEX_SIZE * BYTES_PER_PIXEL)

        // top/left: single-pixel red/blue
        checkerPattern(
            buf, 0, 0, TEX_SIZE / 2, TEX_SIZE / 2,
            OPAQUE or RED, OPAQUE or BLUE, 0x01
        )

        // bottom/right: two-pixel red/green
        checkerPattern(
            buf, TEX_SIZE / 2, TEX_SIZE / 2, TEX_SIZE, TEX_SIZE,
            OPAQUE or RED, OPAQUE or GREEN, 0x02
        )

        // bottom/left: four-pixel blue/green
        checkerPattern(
            buf, 0, TEX_SIZE / 2, TEX_SIZE / 2, TEX_SIZE,
            OPAQUE or BLUE, OPAQUE or GREEN, 0x04
        )

        // top/right: eight-pixel black/white
        checkerPattern(
            buf, TEX_SIZE / 2, 0, TEX_SIZE, TEX_SIZE / 2,
            OPAQUE or WHITE, OPAQUE or BLACK, 0x08
        )

        val byteBuf = ByteBuffer.allocateDirect(buf.size)
        byteBuf.put(buf)
        byteBuf.position(0)
        return byteBuf
    }

    private fun checkerPattern(
        buf: ByteArray, left: Int, top: Int, right: Int, bottom: Int,
        color1: Int, color2: Int, bit: Int
    ) {
        for (row in top until bottom) {
            val rowOffset = row * TEX_SIZE * BYTES_PER_PIXEL
            for (col in left until right) {
                val offset = rowOffset + col * BYTES_PER_PIXEL
                val color: Int = if (row and bit xor (col and bit) == 0) {
                    color1
                } else {
                    color2
                }

                // get unsigned color components
                val red = color and 0xff
                val green = color shr 8 and 0xff
                val blue = color shr 16 and 0xff
                val alpha = color shr 24 and 0xff

                // pre-multiply transparency
                val alphaM = alpha / 255.0f
                buf[offset] = (red * alphaM).toInt().toByte()
                buf[offset + 1] = (green * alphaM).toInt().toByte()
                buf[offset + 2] = (blue * alphaM).toInt().toByte()
                buf[offset + 3] = alpha.toByte()
            }
        }
    }

    enum class Image {
        PICTURE,
        COARSE,
        FINE
    }
}
