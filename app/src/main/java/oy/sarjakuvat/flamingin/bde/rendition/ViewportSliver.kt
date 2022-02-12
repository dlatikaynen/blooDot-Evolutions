package oy.sarjakuvat.flamingin.bde.rendition

import android.opengl.GLES20
import oy.sarjakuvat.flamingin.bde.gles.GlUtil
import java.lang.IllegalStateException

class ViewportSliver {
    var textureId = 0

    fun releaseTexture() {
        if(textureId == 0)
        {
            throw IllegalStateException("Attempt to release ${ViewportSliver::class.simpleName}::textureId when it was already zero")
        }

        val values = IntArray(1)
        values[0] = textureId
        GLES20.glDeleteTextures(1, values, 0)
        GlUtil.checkGlError("${ViewportSliver::class.simpleName}::glDeleteTextures")
        textureId = 0
    }
}
