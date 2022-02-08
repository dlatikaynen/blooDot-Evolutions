package oy.sarjakuvat.flamingin.bde.rendition

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import oy.sarjakuvat.flamingin.bde.algo.MonominoLookup
import oy.sarjakuvat.flamingin.bde.gles.ShaderTextureProgram
import oy.sarjakuvat.flamingin.bde.level.tilesets.GrayWallTileset
import oy.sarjakuvat.flamingin.bde.rendition.offscreen.OffscreenFrame
import oy.sarjakuvat.flamingin.bde.rendition.offscreen.TilePainterBase.Companion.tileSize
import oy.sarjakuvat.flamingin.bde.rendition.offscreen.TilesetPainter

class ViewportOrchestrator {
    private val floorSlivers: Array<OffscreenFrame> = Array(9) { OffscreenFrame() }
    private val rooofSlivers: Array<OffscreenFrame> = Array(9) { OffscreenFrame() }
    private val spriteSheet: OffscreenFrame = OffscreenFrame()
    private var width: Int = 0
    private var height: Int = 0

    private var midpointOffsetX: Int = -720 / 2
    private var midpointOffsetY: Int = -1280 / 2

    fun shareWithOffscreenFramebuffers(pictureTextureName: Int, textureProgram: ShaderTextureProgram) {
        spriteSheet.initializeSharedGpuNames(pictureTextureName, textureProgram)
        for(i in floorSlivers.indices) {
            floorSlivers[i].initializeSharedGpuNames(pictureTextureName, textureProgram)
            rooofSlivers[i].initializeSharedGpuNames(pictureTextureName, textureProgram)
        }
    }

    fun prepareOffscreenFramebuffers(newWidth: Int, newHeight: Int, projectionMatrix: FloatArray) {
        width = newWidth
        height = newHeight
        spriteSheet.prepareOffscreenFramebuffer(width, height, projectionMatrix)
        for(i in floorSlivers.indices) {
            floorSlivers[i].prepareOffscreenFramebuffer(width, height, projectionMatrix)
            rooofSlivers[i].prepareOffscreenFramebuffer(width, height, projectionMatrix)
        }
    }

    fun populateOffscreenFramebuffers() {
        /* we want a populated sprite sheet first */
        val populator = DrawableToTexture(width, height)
        val sink = populator.sink
        sink.drawColor(Color.TRANSPARENT)

        /* debug-draw a clumsy pack in it */
        val gwT = GrayWallTileset()
        gwT.load()
        val tsP = TilesetPainter(gwT, sink)

        for(y in 2..36) {
            placeTestTile(sink, tsP, 2, y)
            placeTestTile(sink, tsP, 30, y)
        }

        for(x in 2..30) {
            placeTestTile(sink, tsP, x, 2)
            placeTestTile(sink, tsP, x, 36)
        }

        placeTestTile(sink, tsP, 3, 3)

        placeTestTile(sink, tsP, 35, 3)
        placeTestTile(sink, tsP, 34, 4)

        placeTestTile(sink, tsP, 35, 29)
        placeTestTile(sink, tsP, 34, 28)
        placeTestTile(sink, tsP, 33, 27)

        placeTestTile(sink, tsP, 3, 29)
        placeTestTile(sink, tsP, 4, 28)
        placeTestTile(sink, tsP, 5, 27)
        placeTestTile(sink, tsP, 6, 29)

        sink.save()
        sink.translate(15f * tileSize, 15f * tileSize)
        val tileSource = gwT.tileNumberToSheetCoordinates(GrayWallTileset.marbleFloor, 15,15)
        tsP.paintStaticTile(tileSource.x, tileSource.y)
        sink.restore()

        val paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 17f
        paint.color=Color.MAGENTA
        sink.drawRoundRect(RectF(100f,100f,width - 100f,height - 100f),30f,30f, paint)


        val textureName = populator.asNewTexture()
        spriteSheet.populate(textureName)
        populator.deleteTextureAfterUse(textureName)

        for(i in floorSlivers.indices) {
            floorSlivers[i].populate()
            rooofSlivers[i].populate()
        }
    }

    private fun placeTestTile(sink: Canvas, tsP: TilesetPainter, x: Int, y: Int) {
        sink.save()
        sink.translate(x.toFloat() * tileSize, y.toFloat() * tileSize)
        tsP.paintBlobTile(MonominoLookup.primeIndexShy)
        sink.restore()
    }

    fun blitV8() {
        val topLeftF = getFloorSliver(0,0)
        val topRightF = getFloorSliver(1,0)
        val bottomLeftF = getFloorSliver(0,1)
        val bottomRightF = getFloorSliver(1,1)
        val topLeftR = getRooofSliver(0,0)
        val topRightR = getRooofSliver(1,0)
        val bottomLeftR = getRooofSliver(0,1)
        val bottomRightR = getRooofSliver(1,1)
        val region = getSliverRegion()
        topLeftF.blit(
            region[TopLeftSrcLeft], region [TopLeftSrcTop], region[TopLeftSrcRight], region[TopLeftSrcBottom],
            region[TopLeftDstLeft], region [TopLeftDstTop], region[TopLeftDstRight], region[TopLeftDstBottom]
        )

        topRightF.blit(
            region[TopRightSrcLeft], region [TopRightSrcTop], region[TopRightSrcRight], region[TopRightSrcBottom],
            region[TopRightDstLeft], region [TopRightDstTop], region[TopRightDstRight], region[TopRightDstBottom]
        )

        bottomLeftF.blit(
            region[BottomLeftSrcLeft], region [BottomLeftSrcTop], region[BottomLeftSrcRight], region[BottomLeftSrcBottom],
            region[BottomLeftDstLeft], region [BottomLeftDstTop], region[BottomLeftDstRight], region[BottomLeftDstBottom]
        )

        bottomRightF.blit(
            region[BottomRightSrcLeft], region [BottomRightSrcTop], region[BottomRightSrcRight], region[BottomRightSrcBottom],
            region[BottomRightDstLeft], region [BottomRightDstTop], region[BottomRightDstRight], region[BottomRightDstBottom]
        )

        /* cylinder head screw tightening pattern */
        topLeftR.blit(
            region[TopLeftSrcLeft], region [TopLeftSrcTop], region[TopLeftSrcRight], region[TopLeftSrcBottom],
            region[TopLeftDstLeft], region [TopLeftDstTop], region[TopLeftDstRight], region[TopLeftDstBottom]
        )

        bottomLeftR.blit(
            region[BottomLeftSrcLeft], region [BottomLeftSrcTop], region[BottomLeftSrcRight], region[BottomLeftSrcBottom],
            region[BottomLeftDstLeft], region [BottomLeftDstTop], region[BottomLeftDstRight], region[BottomLeftDstBottom]
        )

        topRightR.blit(
            region[TopRightSrcLeft], region [TopRightSrcTop], region[TopRightSrcRight], region[TopRightSrcBottom],
            region[TopRightDstLeft], region [TopRightDstTop], region[TopRightDstRight], region[TopRightDstBottom]
        )

        bottomRightR.blit(
            region[BottomRightSrcLeft], region [BottomRightSrcTop], region[BottomRightSrcRight], region[BottomRightSrcBottom],
            region[BottomRightDstLeft], region [BottomRightDstTop], region[BottomRightDstRight], region[BottomRightDstBottom]
        )

        spriteSheet.blit(
            region[BottomRightSrcLeft], region [BottomRightSrcTop], region[BottomRightSrcRight], region[BottomRightSrcBottom],
            region[BottomRightDstLeft], region [BottomRightDstTop], region[BottomRightDstRight], region[BottomRightDstBottom]
        )
    }

    private fun getSliverRegion(): Array<Int> {
        return arrayOf(
            /* top left src dst */
            width + midpointOffsetX,height + midpointOffsetY, width, height,
            0, 0, -midpointOffsetX, -midpointOffsetY,
            /* top right src dst */
            0,height + midpointOffsetY, -midpointOffsetX, height,
            -midpointOffsetX, 0, width, -midpointOffsetY,
            /* bottom left src dest */
            width + midpointOffsetX, 0, width, -midpointOffsetY,
            0, -midpointOffsetY, -midpointOffsetX, height,
            /* bottom right src dest */
            0, 0, width + midpointOffsetX, height + midpointOffsetY,
            width + midpointOffsetX, height + midpointOffsetY, width, height
        )
    }
    
    fun destroyOffscreenFramebuffers() {
        for(i in rooofSlivers.indices) {
            rooofSlivers[i].destroyOffscreenFramebuffer()
            floorSlivers[i].destroyOffscreenFramebuffer()
        }

        spriteSheet.destroyOffscreenFramebuffer()
    }

    private fun getFloorSliver(sliverX: Int, sliverY: Int) : OffscreenFrame {
        return floorSlivers[sliverY * 3 + sliverX]
    }

    private fun getRooofSliver(sliverX: Int, sliverY: Int) : OffscreenFrame {
        return rooofSlivers[sliverY * 3 + sliverX]
    }
    
    companion object {
        private const val TopLeftSrcLeft = 0
        private const val TopLeftSrcTop = 1
        private const val TopLeftSrcRight = 2
        private const val TopLeftSrcBottom = 3
        private const val TopLeftDstLeft = 4
        private const val TopLeftDstTop = 5
        private const val TopLeftDstRight = 6
        private const val TopLeftDstBottom = 7

        private const val TopRightSrcLeft = 8
        private const val TopRightSrcTop = 9
        private const val TopRightSrcRight = 10
        private const val TopRightSrcBottom = 11
        private const val TopRightDstLeft = 12
        private const val TopRightDstTop = 13
        private const val TopRightDstRight = 14
        private const val TopRightDstBottom = 15

        private const val BottomLeftSrcLeft = 16
        private const val BottomLeftSrcTop = 17
        private const val BottomLeftSrcRight = 18
        private const val BottomLeftSrcBottom = 19
        private const val BottomLeftDstLeft = 20
        private const val BottomLeftDstTop = 21
        private const val BottomLeftDstRight = 22
        private const val BottomLeftDstBottom = 23

        private const val BottomRightSrcLeft = 24
        private const val BottomRightSrcTop = 25
        private const val BottomRightSrcRight = 26
        private const val BottomRightSrcBottom = 27
        private const val BottomRightDstLeft = 28
        private const val BottomRightDstTop = 29
        private const val BottomRightDstRight = 30
        private const val BottomRightDstBottom = 31
    }
}
