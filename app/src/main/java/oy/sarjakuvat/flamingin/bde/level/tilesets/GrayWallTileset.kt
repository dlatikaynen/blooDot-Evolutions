package oy.sarjakuvat.flamingin.bde.level.tilesets

import android.graphics.*
import android.util.Size
import oy.sarjakuvat.flamingin.bde.R
import oy.sarjakuvat.flamingin.bde.algo.MonominoLookup
import oy.sarjakuvat.flamingin.bde.rendition.offscreen.BlobPainterBase
import java.lang.IllegalArgumentException

class GrayWallTileset : BlobPainterBase() {
    private val basePaint = Paint()
    private val decoPaint = Paint()

    override val spriteSheetName get() = R.drawable.tile_set_001

    override fun paintBlobTile(paintTo: Canvas, primeIndex: Int) {
        paintTo.drawRect(Rect(0, 0, tileSize, tileSize), basePaint)
        when (primeIndex) {
            MonominoLookup.primeIndexShy -> {
                paintTo.drawRect(RectF(cornerNWx, cornerNWy, cornerSEx, cornerSEy), decoPaint)
            }
            MonominoLookup.primeIndexU -> {
                paintTo.drawRect(RectF(cornerNWx, -cornerNWy, cornerSEx, cornerSEy), decoPaint)
            }
            MonominoLookup.primeIndexKnee -> {
                paintTo.drawLine(cornerNWx, -cornerNWy, cornerSWx, cornerSWy, decoPaint)
                paintTo.drawLine(cornerSWx, cornerSWy, cornerSEx * 2f, cornerSEy, decoPaint)
                paintTo.drawLine(cornerNEx, -cornerNEy, cornerNEx, cornerNEy, decoPaint)
                paintTo.drawLine(cornerNEx, cornerNEy, cornerNEx * 2f, cornerNEy, decoPaint)
            }
            MonominoLookup.primeIndexL -> {
                paintTo.drawLine(cornerNWx, -cornerNWy, cornerSWx, cornerSWy, decoPaint)
                paintTo.drawLine(cornerSWx, cornerSWy, cornerSEx * 2f, cornerSEy, decoPaint)
            }
            MonominoLookup.primeIndexPipe -> {
                paintTo.drawLine(cornerNWx, -cornerNWy, cornerSWx, cornerSWy * 2f, decoPaint)
                paintTo.drawLine(cornerNEx, -cornerNEy, cornerSEx, cornerSEy * 2f, decoPaint)
            }
            MonominoLookup.primeIndexT -> {
                paintTo.drawLine(cornerNWx, -cornerNWy, cornerSWx, cornerSWy * 2f, decoPaint)
                paintTo.drawLine(cornerNEx, -cornerNEy, cornerNEx, cornerNEy, decoPaint)
                paintTo.drawLine(cornerNEx, cornerNEy, cornerNEx * 2, cornerNEy, decoPaint)
                paintTo.drawLine(cornerSEx,cornerSEy * 2f, cornerSEx, cornerSEy, decoPaint)
                paintTo.drawLine(cornerSEx, cornerSEy, cornerSEx * 2f, cornerSEy, decoPaint)
            }
            MonominoLookup.primeIndexWSE -> {
                paintTo.drawLine(cornerNWx, -cornerNWy, cornerSWx, cornerSWy * 2f, decoPaint)
                paintTo.drawLine(cornerSEx, cornerSEy * 2f, cornerSEx, cornerSEy, decoPaint)
                paintTo.drawLine(cornerSEx, cornerSEy, cornerSEx * 2f, cornerSEy, decoPaint)
            }
            MonominoLookup.primeIndexWNE -> {
                paintTo.drawLine(cornerNWx, -cornerNWy, cornerSWx, cornerSWy * 2f, decoPaint)
                paintTo.drawLine(cornerNEx, -cornerNEy, cornerNEx, cornerNEy, decoPaint)
                paintTo.drawLine(cornerNEx, cornerNEy, cornerNEx * 2f, cornerNEy, decoPaint)
            }
            MonominoLookup.primeIndexW -> {
                paintTo.drawLine(cornerNWx, -cornerNWy, cornerSWx, cornerSWy * 2f, decoPaint)
            }
            MonominoLookup.primeIndexCross -> {
                paintTo.drawLine(-cornerNWx, cornerNWy, cornerNWx, cornerNWy, decoPaint)
                paintTo.drawLine(cornerNWx, cornerNWy, cornerNWx, -cornerNWy, decoPaint)
                paintTo.drawLine(-cornerSWx, cornerSWy, cornerSWx, cornerSWy, decoPaint)
                paintTo.drawLine(cornerSWx, cornerSWy, cornerSWx, cornerSWy * 2f, decoPaint)
                paintTo.drawLine(cornerNEx, -cornerNEy, cornerNEx, cornerNEy, decoPaint)
                paintTo.drawLine(cornerNEx, cornerNEy, cornerNEx * 2f, cornerNEy, decoPaint)
                paintTo.drawLine(cornerSEx, cornerSEy * 2f, cornerSEx, cornerSEy, decoPaint)
                paintTo.drawLine(cornerSEx, cornerSEy, cornerSEx * 2f, cornerSEy, decoPaint)
            }
            MonominoLookup.primeIndexInvThreeNE -> {
                paintTo.drawLine(-cornerNWx, cornerNWy, cornerNWx, cornerNWy, decoPaint)
                paintTo.drawLine(cornerNWx, cornerNWy, cornerNWx, -cornerNWy, decoPaint)
                paintTo.drawLine(-cornerSWx, cornerSWy, cornerSWx, cornerSWy, decoPaint)
                paintTo.drawLine(cornerSWx, cornerSWy, cornerSWx, cornerSWy * 2f, decoPaint)
                paintTo.drawLine(cornerSEx,cornerSEy * 2f, cornerSEx, cornerSEy, decoPaint)
                paintTo.drawLine(cornerSEx, cornerSEy, cornerSEx * 2f, cornerSEy, decoPaint)
            }
            MonominoLookup.primeIndexTwoNWSW -> {
                paintTo.drawLine(-cornerNWx, cornerNWy, cornerNWx, cornerNWy, decoPaint)
                paintTo.drawLine(cornerNWx, cornerNWy, cornerNWx, -cornerNWy, decoPaint)
                paintTo.drawLine(-cornerSWx, cornerSWy, cornerSWx, cornerSWy, decoPaint)
                paintTo.drawLine(cornerSWx, cornerSWy, cornerSWx, cornerSWy * 2f, decoPaint)
            }
            MonominoLookup.primeIndexTwoNWSE -> {
                paintTo.drawLine(-cornerNWx, cornerNWy, cornerNWx, cornerNWy, decoPaint)
                paintTo.drawLine(cornerNWx, cornerNWy, cornerNWx, -cornerNWy, decoPaint)
                paintTo.drawLine(cornerSEx,cornerSEy * 2f, cornerSEx, cornerSEy, decoPaint)
                paintTo.drawLine(cornerSEx, cornerSEy, cornerSEx * 2f, cornerSEy, decoPaint)
            }
            MonominoLookup.primeIndexThreeNW -> {
                paintTo.drawLine(-cornerNWx, cornerNWy, cornerNWx, cornerNWy, decoPaint)
                paintTo.drawLine(cornerNWx, cornerNWy, cornerNWx, -cornerNWy, decoPaint)
            }
            MonominoLookup.primeIndexImmersed -> {
                /* nothing to do - this is identical to the blank */
            }
            else -> throw IllegalArgumentException("${GrayWallTileset::class.simpleName} cannot draw $primeIndex")
        }
    }

    init {
        /* https://www.qb64.org/wiki/COLOR */
        basePaint.color = Color.argb(0xff, 0x54, 0x54, 0x54)
        basePaint.style = Paint.Style.FILL
        decoPaint.color = Color.argb(0xff, 0xa8, 0xa8, 0xa8)
        decoPaint.style = Paint.Style.STROKE
        decoPaint.strokeWidth = 2.43f
    }

    override fun tileNumberToBaseSheetPosition(tileNumber: Int): Point {
        return when(tileNumber) {
            TileCatalog.FloorTiles.marbleFloor -> return Point(0, 0)
            else -> throw IllegalArgumentException("The number $tileNumber is not a valid value for  tileNumber in ${GrayWallTileset::class.simpleName}")
        }
    }

    override fun tilePartitionSizeOnSheet(tileNumber: Int) : Size {
        return when(tileNumber) {
            TileCatalog.FloorTiles.marbleFloor -> Size(3, 3)
            else -> super.tilePartitionSizeOnSheet(tileNumber)
        }
    }

    companion object {
        const val cornerNWx = tileSize / 7f
        const val cornerNWy = cornerNWx
        const val cornerNEx = tileSize - tileSize / 7f
        const val cornerNEy = cornerNWx
        const val cornerSWx = cornerNWx
        const val cornerSWy = cornerNEx
        const val cornerSEx = cornerNEx
        const val cornerSEy = cornerSWy
    }
}
