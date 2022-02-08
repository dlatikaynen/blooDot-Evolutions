package oy.sarjakuvat.flamingin.bde.rendition

import android.graphics.Bitmap
import android.graphics.Bitmap.DENSITY_NONE
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.opengl.GLES20
import android.util.DisplayMetrics.DENSITY_DEFAULT
import oy.sarjakuvat.flamingin.bde.gles.GlUtil

class DrawableToTexture(val width: Int, val height: Int) {
    private val bitmap: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    private val canvas: Canvas

    init {
        bitmap.density = DENSITY_DEFAULT
        canvas = Canvas(bitmap)
    }

    val sink: Canvas get() = canvas

    fun asNewTexture() : Int {
        val textureName = GlUtil.createBitmapTexture(bitmap)
        bitmap.recycle()
        return textureName
    }

    fun deleteTextureAfterUse(textureName: Int) {
        GLES20.glDeleteTextures(1, IntArray(textureName) ,0)
    }
}
