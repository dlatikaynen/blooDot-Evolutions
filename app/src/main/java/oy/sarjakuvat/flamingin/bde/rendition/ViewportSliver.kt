package oy.sarjakuvat.flamingin.bde.rendition

import android.opengl.GLES20
import oy.sarjakuvat.flamingin.bde.gles.GlUtil
import java.lang.IllegalStateException

class ViewportSliver {
    var floorTextureId = 0
    var rooofTextureId = 0

    fun releaseTextures() {
        if(floorTextureId == 0)
        {
            throw IllegalStateException("Attempt to release ${ViewportSliver::class.simpleName}::floorTextureId when it was already zero")
        }

        if(rooofTextureId == 0)
        {
            throw IllegalStateException("Attempt to release ${ViewportSliver::class.simpleName}::rooofTextureId when it was already zero")
        }

        val values = IntArray(2)
        values[0] = rooofTextureId
        values[1] = floorTextureId
        GLES20.glDeleteTextures(values.size, values, 0)
        GlUtil.checkGlError("${ViewportSliver::class.simpleName}::glDeleteTextures")
        rooofTextureId = 0
        floorTextureId = 0
    }
}
