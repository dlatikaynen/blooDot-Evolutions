package oy.sarjakuvat.flamingin.bde.rendition.offscreen

import android.graphics.Canvas
import android.graphics.Rect
import oy.sarjakuvat.flamingin.bde.algo.MonominoIndex
import oy.sarjakuvat.flamingin.bde.algo.MonominoLookup
import oy.sarjakuvat.flamingin.bde.level.tilesets.TileCatalog
import oy.sarjakuvat.flamingin.bde.rendition.offscreen.TilePainterBase.Companion.tileSize
import kotlin.math.PI

class TilesetPainter(private val basePainter: BlobPainterBase, private val paintTo: Canvas) {
    fun paintTile(xPos: Float, yPos: Float, tileNumber: Int, monominoIndex: Int = MonominoLookup.primeIndexShy, strideX: Int = 0, strideY: Int = 0) {
        when(tileNumber) {
            TileCatalog.Tiles.classicWall -> {
                paintBlobTile(xPos, yPos, monominoIndex)
            }
            else -> {
                val tileSource = basePainter.tileNumberToSheetCoordinates(tileNumber, strideX, strideY)
                paintStaticTile(xPos, yPos, tileSource.x, tileSource.y)
            }
        }
    }

    private fun paintBlobTile(xPos: Float, yPos: Float, monominoIndex: Int) {
        val paintInstructions = MonominoIndex.primeTileFrom(monominoIndex)
        paintTo.save()
        paintTo.translate(xPos, yPos)
        paintTo.clipRect(clipRect)
        rotate(paintInstructions.numRotations)
        basePainter.paintBlobTile(paintTo, paintInstructions.primeIndex)
        paintTo.restore()
    }

    private fun paintStaticTile(xPos: Float, yPos: Float, indexOnSpriteSheetX: Int, indexOnSpriteSheetY : Int, numRotations: Int = 0) {
        paintTo.save()
        paintTo.translate(xPos, yPos)
        paintTo.clipRect(clipRect)
        rotate(numRotations)
        basePainter.paintStaticTile(paintTo, indexOnSpriteSheetX, indexOnSpriteSheetY)
        paintTo.restore()
    }

    private fun rotate(numRotations: Int) {
        if (numRotations > 0) {
            paintTo.translate(tileSize / 2f, tileSize / 2f)
            paintTo.rotate((PI / 2f * numRotations).toFloat())
            paintTo.translate(-tileSize / 2f, -tileSize / 2f)
        }
    }

    companion object {
        val clipRect = Rect(0, 0, tileSize, tileSize)
    }
}
