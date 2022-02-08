package oy.sarjakuvat.flamingin.bde.rendition.offscreen

import android.graphics.Canvas
import android.graphics.Point

abstract class BlobPainterBase : TilePainterBase() {
    abstract fun paintBlobTile(paintTo: Canvas, primeIndex: Int)
    abstract override fun tileNumberToBaseSheetPosition(tileNumber: Int): Point
}
