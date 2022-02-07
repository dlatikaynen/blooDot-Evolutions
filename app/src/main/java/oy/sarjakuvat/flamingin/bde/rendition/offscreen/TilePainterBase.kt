package oy.sarjakuvat.flamingin.bde.rendition.offscreen

import android.graphics.*
import android.util.Size
import oy.sarjakuvat.flamingin.bde.App

abstract class TilePainterBase {
    private lateinit var spriteSheet: Bitmap
    private val paint: Paint = Paint()

    open val spriteSheetName : Int get() = 0

    fun load() {
        if(spriteSheetName != 0) {
            spriteSheet = BitmapFactory.decodeResource(App.context.get()!!.resources, spriteSheetName)
        }
    }

    open fun tilePartitionSizeOnSheet(tileNumber: Int) : Size {
        return Size(1,1)
    }

    abstract fun tileNumberToBaseSheetPosition(tileNumber: Int) : Point

    fun tileNumberToSheetCoordinates(tileNumber: Int, strideX: Int = 0, strideY: Int = 0): Point {
        val tilePartitionSize = tilePartitionSizeOnSheet(tileNumber)
        val basePosOnSheet = tileNumberToBaseSheetPosition(tileNumber)
        var absPosOnSheet = Point(basePosOnSheet.x, basePosOnSheet.y)
        if(tilePartitionSize.height > 1) {
            absPosOnSheet = Point(absPosOnSheet.x + (strideX % tilePartitionSize.width), absPosOnSheet.y)
        }

        if(tilePartitionSize.height > 1) {
            absPosOnSheet = Point(absPosOnSheet.x, absPosOnSheet.y + (strideY % tilePartitionSize.height))
        }

        return absPosOnSheet
    }

    fun paintStaticTile(paintTo: Canvas, indexOnSpriteSheetX: Int, indexOnSpriteSheetY: Int) {
        // the +1 are the gridlines
        val srcLeft = 1 + (tileSize + 1) * indexOnSpriteSheetX
        val srcTop = 1 + (tileSize + 1) * indexOnSpriteSheetY
        paintTo.drawBitmap(
            spriteSheet,
            Rect(
                srcLeft,
                srcTop,
                srcLeft + tileSize,
                srcTop + tileSize
            ),
            RectF(0f,0f, tileSize.toFloat(), tileSize.toFloat()),
            paint
        )
    }

    companion object {
        const val tileSize = 33
    }
}