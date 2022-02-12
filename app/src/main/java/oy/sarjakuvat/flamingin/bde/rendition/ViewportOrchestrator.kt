package oy.sarjakuvat.flamingin.bde.rendition

import android.graphics.*
import oy.sarjakuvat.flamingin.bde.algo.MonominoLookup
import oy.sarjakuvat.flamingin.bde.gles.ShaderTextureProgram
import oy.sarjakuvat.flamingin.bde.gles.Sprite2d
import oy.sarjakuvat.flamingin.bde.level.tilesets.GrayWallTileset
import oy.sarjakuvat.flamingin.bde.level.tilesets.TileCatalog
import oy.sarjakuvat.flamingin.bde.rendition.offscreen.OffscreenFrame
import oy.sarjakuvat.flamingin.bde.rendition.offscreen.TilePainterBase.Companion.tileSize
import oy.sarjakuvat.flamingin.bde.rendition.offscreen.TilesetPainter

class ViewportOrchestrator {
    private val floorSlivers: Array<ViewportSliver> = Array(9) { ViewportSliver() }
    private val rooofSlivers: Array<ViewportSliver> = Array(9) { ViewportSliver() }
    private val spriteSheet: OffscreenFrame = OffscreenFrame()
    private var width: Int = 0
    private var height: Int = 0

    private var midpointOffsetX: Int = 640 / 2
    private var midpointOffsetY: Int = 360 / 2

    fun shareWithOffscreenFramebuffers(
        pictureTextureName: Int,
        textureProgram: ShaderTextureProgram
    ) {
        spriteSheet.initializeSharedGpuNames(pictureTextureName, textureProgram)
    }

    fun prepareOffscreenFramebuffers(newWidth: Int, newHeight: Int, projectionMatrix: FloatArray) {
        width = newWidth
        height = newHeight
        midpointOffsetX = newWidth / 2
        midpointOffsetY = newHeight / 2
        spriteSheet.prepareOffscreenFramebuffer(width, height, projectionMatrix)
    }

    fun populateOffscreenFramebuffers() {
        /* we want a populated sprite sheet first */
        val populator = DrawableToTexture(width, height)
        generateTestSheetTexture(populator)
        var textureName = populator.asNewTexture()
        spriteSheet.populate(textureName)
        populator.deleteTextureAfterUse(textureName)
        val rooofPopulator = DrawableToTexture(width, height)
        for (i in floorSlivers.indices) {
            populateSheet(populator, rooofPopulator)
            floorSlivers[i].textureId = populator.asNewTexture()
            rooofSlivers[i].textureId = rooofPopulator.asNewTexture()
        }

        rooofPopulator.deleteBitmapAfterUse()
        populator.deleteBitmapAfterUse()
    }

    private fun populateSheet(floorPopulator: DrawableToTexture, rooofPopulator: DrawableToTexture) {
        val floorSink = floorPopulator.sink
        val rooofSink = rooofPopulator.sink
        floorPopulator.clearBitmapBeforeUse()
        rooofPopulator.clearBitmapBeforeUse()
        floorSink.drawColor(Color.argb(0.2f, 0.75f,0.75f,0.7f))
        rooofSink.drawColor(Color.argb(0.2f,1f,0f,0f))

        val paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 7f
        paint.color = Color.MAGENTA
        floorSink.drawRoundRect(RectF(13f, 13f, width - 13f, height - 13f), 30f, 30f, paint)

        paint.color = Color.argb(0.5f,0f,1f,0f)
        paint.style = Paint.Style.FILL
        floorSink.drawRect(80f,height-120f,120f,height - 80f, paint)
        paint.style = Paint.Style.STROKE

        paint.strokeWidth = 5f
        paint.color = Color.RED
        rooofSink.drawRect(RectF(6f, 6f, width - 6f, height - 6f), paint)
    }

    private fun generateTestSheetTexture(populator: DrawableToTexture) {
        val sink = populator.sink
        populator.clearBitmapBeforeUse()
        sink.drawColor(Color.TRANSPARENT)

        /* debug-draw a clumsy pack in it */
        val gwT = GrayWallTileset()
        gwT.load()
        val tsP = TilesetPainter(gwT, sink)
        val rightTile = width / tileSize - 2
        val bottomTile = height/ tileSize - 2

        for (x in 2..rightTile) {
            placeTestTile(sink, tsP, x, 2)
            placeTestTile(sink, tsP, x, bottomTile)
        }

        for (y in 2..bottomTile) {
            placeTestTile(sink, tsP, 2, y)
            placeTestTile(sink, tsP, rightTile, y)
        }

        placeTestTile(sink, tsP, 3, 3)

        placeTestTile(sink, tsP, rightTile-1, 3)
        placeTestTile(sink, tsP, rightTile-2, 4)

        placeTestTile(sink, tsP, rightTile-1, bottomTile-1)
        placeTestTile(sink, tsP, rightTile-2, bottomTile-2)
        placeTestTile(sink, tsP, rightTile-3, bottomTile-3)

        placeTestTile(sink, tsP, 3, bottomTile-1)
        placeTestTile(sink, tsP, 4, bottomTile-2)
        placeTestTile(sink, tsP, 5, bottomTile-3)
        placeTestTile(sink, tsP, 6, bottomTile-4)

        sink.save()
        sink.translate(15f * tileSize, 15f * tileSize)
        val tileSource = gwT.tileNumberToSheetCoordinates(TileCatalog.FloorTiles.marbleFloor, 15, 15)
        tsP.paintStaticTile(tileSource.x, tileSource.y)
        sink.restore()

        val paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 7f
        paint.color = Color.MAGENTA
        sink.drawRoundRect(RectF(13f, 13f, width - 13f, height - 13f), 30f, 30f, paint)
        paint.strokeWidth = 5f
        paint.color = Color.YELLOW
        sink.drawRect(RectF(6f, 6f, width - 6f, height - 6f), paint)
    }

    private fun placeTestTile(sink: Canvas, tsP: TilesetPainter, x: Int, y: Int) {
        sink.save()
        sink.translate(x.toFloat() * tileSize, y.toFloat() * tileSize)
        tsP.paintBlobTile(MonominoLookup.primeIndexShy)
        sink.restore()
    }

    private fun getSliverRegion(): Array<Int> {
        return arrayOf(
            /* CONFIRMED BOTTOM LEFT */
            width-midpointOffsetX,height-midpointOffsetY,width, height,
            /* bottom left destination */
            0,0,midpointOffsetX,midpointOffsetY,
            /* CONFIRMED BOTTOM RIGHT */
            0,height-midpointOffsetY,width-midpointOffsetX,height,
            /* bottom right destination */
            midpointOffsetX,0,width,midpointOffsetY,
            /* CONFIRMED TOP LEFT */
            width-midpointOffsetX,0,width,height-midpointOffsetY,
            /* top left destination */
            0,midpointOffsetY,midpointOffsetX,height,
            /* CONFIRMED TOP RIGHT */
            0,0,width-midpointOffsetX,height-midpointOffsetY,
            /* top right destination */
            midpointOffsetX,midpointOffsetY,width,height
        )
    }

    fun destroyOffscreenFramebuffers() {
        for (i in rooofSlivers.indices) {
            rooofSlivers[i].releaseTexture()
            floorSlivers[i].releaseTexture()
        }

        spriteSheet.destroyOffscreenFramebuffer()
    }

    fun getFloorSliver(sliverX: Int, sliverY: Int): ViewportSliver {
        return floorSlivers[sliverY * 3 + sliverX]
    }

    private fun getRooofSliver(sliverX: Int, sliverY: Int): ViewportSliver {
        return rooofSlivers[sliverY * 3 + sliverX]
    }

    fun assignSliverPositionsAndTextures(floorToRender: Array<Sprite2d>, rooofToRender: Array<Sprite2d>) {
        floorToRender[0].setTexture(floorSlivers[0].textureId)
        floorToRender[1].setTexture(0)
        floorToRender[2].setTexture(0)
        floorToRender[3].setTexture(0)

        rooofToRender[0].setTexture(rooofSlivers[0].textureId)
        rooofToRender[1].setTexture(0)
        rooofToRender[2].setTexture(0)
        rooofToRender[3].setTexture(0)

        floorToRender[0].setPosition(0f, 0f)
        rooofToRender[0].setPosition(width.toFloat()/2, height.toFloat()/2)

    }

    companion object {
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
    }
}
