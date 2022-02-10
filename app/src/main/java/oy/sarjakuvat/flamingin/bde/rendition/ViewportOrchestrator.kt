package oy.sarjakuvat.flamingin.bde.rendition

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import oy.sarjakuvat.flamingin.bde.algo.MonominoLookup
import oy.sarjakuvat.flamingin.bde.gles.ShaderTextureProgram
import oy.sarjakuvat.flamingin.bde.level.tilesets.GrayWallTileset
import oy.sarjakuvat.flamingin.bde.level.tilesets.TileCatalog
import oy.sarjakuvat.flamingin.bde.rendition.offscreen.OffscreenFrame
import oy.sarjakuvat.flamingin.bde.rendition.offscreen.TilePainterBase.Companion.tileSize
import oy.sarjakuvat.flamingin.bde.rendition.offscreen.TilesetPainter

class ViewportOrchestrator {
    private val floorSlivers: Array<OffscreenFrame> = Array(9) { OffscreenFrame() }
    private val rooofSlivers: Array<OffscreenFrame> = Array(9) { OffscreenFrame() }
    private val spriteSheet: OffscreenFrame = OffscreenFrame()
    private var width: Int = 0
    private var height: Int = 0

    private var midpointOffsetX: Int = -640 / 2
    private var midpointOffsetY: Int = -360 / 2

    fun shareWithOffscreenFramebuffers(
        pictureTextureName: Int,
        textureProgram: ShaderTextureProgram
    ) {
        spriteSheet.initializeSharedGpuNames(pictureTextureName, textureProgram)
        for (i in floorSlivers.indices) {
            floorSlivers[i].initializeSharedGpuNames(pictureTextureName, textureProgram)
            rooofSlivers[i].initializeSharedGpuNames(pictureTextureName, textureProgram)
        }
    }

    fun prepareOffscreenFramebuffers(newWidth: Int, newHeight: Int, projectionMatrix: FloatArray) {
        width = newWidth
        height = newHeight
        spriteSheet.prepareOffscreenFramebuffer(width, height, projectionMatrix)
        for (i in floorSlivers.indices) {
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

        for (x in 2..18) {
            placeTestTile(sink, tsP, x, 2)
            placeTestTile(sink, tsP, x, 10)
        }

        for (y in 2..10) {
            placeTestTile(sink, tsP, 2, y)
            placeTestTile(sink, tsP, 18, y)
        }

        placeTestTile(sink, tsP, 3, 3)

        placeTestTile(sink, tsP, 17, 3)
        placeTestTile(sink, tsP, 16, 4)

        placeTestTile(sink, tsP, 17, 9)
        placeTestTile(sink, tsP, 16, 8)
        placeTestTile(sink, tsP, 15, 7)

        placeTestTile(sink, tsP, 3, 9)
        placeTestTile(sink, tsP, 4, 8)
        placeTestTile(sink, tsP, 5, 7)
        placeTestTile(sink, tsP, 6, 6)

        sink.save()
        sink.translate(15f * tileSize, 15f * tileSize)
        val tileSource = gwT.tileNumberToSheetCoordinates(TileCatalog.FloorTiles.marbleFloor, 15, 15)
        tsP.paintStaticTile(tileSource.x, tileSource.y)
        sink.restore()

        val paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 17f
        paint.color = Color.MAGENTA
        sink.drawRoundRect(RectF(100f, 100f, width - 100f, height - 100f), 30f, 30f, paint)


        val textureName = populator.asNewTexture()
        spriteSheet.populate(textureName)
        populator.deleteTextureAfterUse(textureName)
        for (i in floorSlivers.indices) {
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
        val topLeftF = getFloorSliver(0, 0)
        val topRightF = getFloorSliver(1, 0)
        val bottomLeftF = getFloorSliver(0, 1)
        val bottomRightF = getFloorSliver(1, 1)
        val topLeftR = getRooofSliver(0, 0)
        val topRightR = getRooofSliver(1, 0)
        val bottomLeftR = getRooofSliver(0, 1)
        val bottomRightR = getRooofSliver(1, 1)
        val region = getSliverRegion()
        topLeftF.blit(
            region[TopLeftSrcLeft],
            region[TopLeftSrcTop],
            region[TopLeftSrcRight],
            region[TopLeftSrcBottom],
            region[TopLeftDstLeft],
            region[TopLeftDstTop],
            region[TopLeftDstRight],
            region[TopLeftDstBottom]
        )

        topRightF.blit(
            region[TopRightSrcLeft],
            region[TopRightSrcTop],
            region[TopRightSrcRight],
            region[TopRightSrcBottom],
            region[TopRightDstLeft],
            region[TopRightDstTop],
            region[TopRightDstRight],
            region[TopRightDstBottom]
        )

        bottomLeftF.blit(
            region[BottomLeftSrcLeft],
            region[BottomLeftSrcTop],
            region[BottomLeftSrcRight],
            region[BottomLeftSrcBottom],
            region[BottomLeftDstLeft],
            region[BottomLeftDstTop],
            region[BottomLeftDstRight],
            region[BottomLeftDstBottom]
        )

        bottomRightF.blit(
            region[BottomRightSrcLeft],
            region[BottomRightSrcTop],
            region[BottomRightSrcRight],
            region[BottomRightSrcBottom],
            region[BottomRightDstLeft],
            region[BottomRightDstTop],
            region[BottomRightDstRight],
            region[BottomRightDstBottom]
        )

        /* cylinder head screw tightening pattern */
        topLeftR.blit(
            region[TopLeftSrcLeft],
            region[TopLeftSrcTop],
            region[TopLeftSrcRight],
            region[TopLeftSrcBottom],
            region[TopLeftDstLeft],
            region[TopLeftDstTop],
            region[TopLeftDstRight],
            region[TopLeftDstBottom]
        )

        bottomLeftR.blit(
            region[BottomLeftSrcLeft],
            region[BottomLeftSrcTop],
            region[BottomLeftSrcRight],
            region[BottomLeftSrcBottom],
            region[BottomLeftDstLeft],
            region[BottomLeftDstTop],
            region[BottomLeftDstRight],
            region[BottomLeftDstBottom]
        )

        topRightR.blit(
            region[TopRightSrcLeft],
            region[TopRightSrcTop],
            region[TopRightSrcRight],
            region[TopRightSrcBottom],
            region[TopRightDstLeft],
            region[TopRightDstTop],
            region[TopRightDstRight],
            region[TopRightDstBottom]
        )

        bottomRightR.blit(
            region[BottomRightSrcLeft],
            region[BottomRightSrcTop],
            region[BottomRightSrcRight],
            region[BottomRightSrcBottom],
            region[BottomRightDstLeft],
            region[BottomRightDstTop],
            region[BottomRightDstRight],
            region[BottomRightDstBottom]
        )

        /* OK ... bottom-left corner visible is FOUR
        spriteSheet.blit(
            region[TopRightSrcLeft], region[TopRightSrcTop], region[TopRightSrcRight], region[TopRightSrcBottom],
            region[TopRightDstLeft], region[TopRightDstTop], region[TopRightDstRight], region[TopRightDstBottom]
        )
        */

        /* OK ... bottom-right corner visible is THREE
        spriteSheet.blit(
            region[TopLeftSrcLeft], region [TopLeftSrcTop], region[TopLeftSrcRight], region[TopLeftSrcBottom],
            region[TopLeftDstLeft], region [TopLeftDstTop], region[TopLeftDstRight], region[TopLeftDstBottom]
        )
        */

        /* OK ... visible in bottom-right corner is the ONE
        spriteSheet.blit(
            region[BottomRightSrcLeft], region [BottomRightSrcTop], region[BottomRightSrcRight], region[BottomRightSrcBottom],
            region[BottomRightDstLeft], region [BottomRightDstTop], region[BottomRightDstRight], region[BottomRightDstBottom]
        )
        */

        /* OK ... top-right corner visible is TWO */
        spriteSheet.blit(
            region[BottomLeftSrcLeft], region [BottomLeftSrcTop], region[BottomLeftSrcRight], region[BottomLeftSrcBottom],
            region[BottomLeftDstLeft], region [BottomLeftDstTop], region[BottomLeftDstRight], region[BottomLeftDstBottom]
        )
    }

    private fun getSliverRegion(): Array<Int> {
        return arrayOf(
            /* bottom left src dst */
            width + midpointOffsetX, height + midpointOffsetY, width, height,
            0, 0, -midpointOffsetX, -midpointOffsetY,
            /* bottom right src dst */
            0, height + midpointOffsetY, -midpointOffsetX, height,
            -midpointOffsetX, 0, width, -midpointOffsetY,
            /* top left src dest */
            width + midpointOffsetX, 0, width, -midpointOffsetY,
            0, -midpointOffsetY, -midpointOffsetX, height,
            /* top right src dest */
            0, 0, width + midpointOffsetX, height + midpointOffsetY,
            width + midpointOffsetX, height + midpointOffsetY, width, height
        )
    }

    fun destroyOffscreenFramebuffers() {
        for (i in rooofSlivers.indices) {
            rooofSlivers[i].destroyOffscreenFramebuffer()
            floorSlivers[i].destroyOffscreenFramebuffer()
        }

        spriteSheet.destroyOffscreenFramebuffer()
    }

    private fun getFloorSliver(sliverX: Int, sliverY: Int): OffscreenFrame {
        return floorSlivers[sliverY * 3 + sliverX]
    }

    private fun getRooofSliver(sliverX: Int, sliverY: Int): OffscreenFrame {
        return rooofSlivers[sliverY * 3 + sliverX]
    }

    companion object {
        private const val TopLeftSrcLeft = 16
        private const val TopLeftSrcTop = 17
        private const val TopLeftSrcRight = 18
        private const val TopLeftSrcBottom = 19
        private const val TopLeftDstLeft = 20
        private const val TopLeftDstTop = 21
        private const val TopLeftDstRight = 22
        private const val TopLeftDstBottom = 23

        private const val TopRightSrcLeft = 24
        private const val TopRightSrcTop = 25
        private const val TopRightSrcRight = 26
        private const val TopRightSrcBottom = 27
        private const val TopRightDstLeft = 28
        private const val TopRightDstTop = 29
        private const val TopRightDstRight = 30
        private const val TopRightDstBottom = 31

        private const val BottomLeftSrcLeft = 0
        private const val BottomLeftSrcTop = 1
        private const val BottomLeftSrcRight = 2
        private const val BottomLeftSrcBottom = 3
        private const val BottomLeftDstLeft = 4
        private const val BottomLeftDstTop = 5
        private const val BottomLeftDstRight = 6
        private const val BottomLeftDstBottom = 7

        private const val BottomRightSrcLeft = 8
        private const val BottomRightSrcTop = 9
        private const val BottomRightSrcRight = 10
        private const val BottomRightSrcBottom = 11
        private const val BottomRightDstLeft = 12
        private const val BottomRightDstTop = 13
        private const val BottomRightDstRight = 14
        private const val BottomRightDstBottom = 15
    }
}