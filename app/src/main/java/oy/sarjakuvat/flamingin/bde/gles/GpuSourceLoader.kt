package oy.sarjakuvat.flamingin.bde.gles

import android.support.annotation.RawRes
import oy.sarjakuvat.flamingin.bde.App
import oy.sarjakuvat.flamingin.bde.extensions.getRawTextFile

class GpuSourceLoader {
    companion object {
        fun loadGpuSourceCode(@RawRes resId: Int): String {
            return App.context.get()!!.resources.getRawTextFile(resId)
        }
    }
}
