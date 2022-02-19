package oy.sarjakuvat.flamingin.bde.rendition

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.opengl.GLES20
import android.os.Debug
import oy.sarjakuvat.flamingin.bde.BuildConfig
import oy.sarjakuvat.flamingin.bde.algo.ArenaTesselation
import oy.sarjakuvat.flamingin.bde.gles.GlUtil
import oy.sarjakuvat.flamingin.bde.level.Arena
import oy.sarjakuvat.flamingin.bde.level.tilesets.GrayWallTileset
import oy.sarjakuvat.flamingin.bde.rendition.offscreen.TilePainterBase.Companion.tileSize
import oy.sarjakuvat.flamingin.bde.rendition.offscreen.TilesetPainter
import java.lang.IllegalStateException

enum class ViewportSliverStatus {
    Empty,
    Populating,
    Populated,
    OnScreen,
}

class ViewportSliver {
    var floorTextureId = 0
    var rooofTextureId = 0
    var status: ViewportSliverStatus = ViewportSliverStatus.Empty

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

    fun populate(width: Int, height: Int, tesselation: ArenaTesselation, floorSink: Canvas, rooofSink: Canvas) {
        val paint = Paint()
        paint.style = Paint.Style.STROKE

        /* debug grid */
        if(BuildConfig.DEBUG) {
            if(Debug.isDebuggerConnected()) {
                for (x in 0 until tesselation.numTilesX) {
                    val xPos = tesselation.gridLeftXpx + x * tileSize
                    floorSink.drawLine(xPos, 0f, xPos, height.toFloat(), paint)
                }
            }
        }

        for(y in 0 until tesselation.numTilesY) {
            val yPos = tesselation.gridTopYpx + y * tileSize
            floorSink.drawLine(0f, yPos, width.toFloat(), yPos, paint)
        }

        floorSink.drawColor(Color.argb(0.2f, 0.75f,0.75f,0.7f))
        rooofSink.drawColor(Color.argb(0.2f,1f,0f,0f))

        paint.strokeWidth = 3f
        paint.color = Color.MAGENTA
        floorSink.drawRoundRect(RectF(13f, 13f, width - 13f, height - 13f), 13f, 13f, paint)

        paint.color = Color.argb(0.5f,0f,1f,0f)
        paint.style = Paint.Style.FILL
        floorSink.drawRect(80f,height-120f,120f,height - 80f, paint)
        paint.style = Paint.Style.STROKE

        paint.strokeWidth = 2f
        paint.color = Color.RED
        rooofSink.drawRect(RectF(6f, 6f, width - 6f, height - 6f), paint)

        /* tiles */
        val gwT = GrayWallTileset()
        gwT.load()
        val tsPF = TilesetPainter(gwT, floorSink)
        val tsPR = TilesetPainter(gwT, rooofSink)
        for(x in 0 until tesselation.numTilesX) {
            for(y in 0 until tesselation.numTilesY) {
                val arenaX = tesselation.arenaGridIndexLeft + x
                val arenaY = tesselation.arenaGridIndexTop + y
                val cell = Arena.cells[arenaX][arenaY]
                if(cell != null) {
                    val xPos = tesselation.gridLeftXpx + x * tileSize
                    val yPos = tesselation.gridTopYpx + y * tileSize
                    for (entity in cell.contents) {
                        if(entity.tileNumberFloor > 0) {
                            tsPF.paintTile(xPos, yPos, entity.tileNumberFloor, entity.monominoIndex, arenaX, arenaY)
                        }

                        if(entity.tileNumberRooof > 0) {
                            tsPR.paintTile(xPos, yPos, entity.tileNumberRooof, entity.monominoIndex, arenaX, arenaY)
                        }
                    }
                }
            }
        }
    }
}
