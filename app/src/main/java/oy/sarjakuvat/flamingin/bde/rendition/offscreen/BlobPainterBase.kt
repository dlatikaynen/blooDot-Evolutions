package oy.sarjakuvat.flamingin.bde.rendition.offscreen

import android.graphics.Canvas
import android.graphics.Point
import oy.sarjakuvat.flamingin.bde.rendition.offscreen.TilePainterBase

open abstract class BlobPainterBase : TilePainterBase() {
    abstract fun paintBlobTile(paintTo: Canvas, primeIndex: Int)
    open abstract override fun tileNumberToBaseSheetPosition(tileNumber: Int): Point;
}
