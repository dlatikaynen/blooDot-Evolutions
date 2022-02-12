package oy.sarjakuvat.flamingin.bde.rendition

import android.graphics.*
import oy.sarjakuvat.flamingin.bde.algo.MonominoLookup
import oy.sarjakuvat.flamingin.bde.gles.ShaderTextureProgram
import oy.sarjakuvat.flamingin.bde.gles.Sprite2d
import oy.sarjakuvat.flamingin.bde.level.Arena
import oy.sarjakuvat.flamingin.bde.level.tilesets.GrayWallTileset
import oy.sarjakuvat.flamingin.bde.level.tilesets.TileCatalog
import oy.sarjakuvat.flamingin.bde.rendition.offscreen.OffscreenFrame
import oy.sarjakuvat.flamingin.bde.rendition.offscreen.TilePainterBase.Companion.tileSize
import oy.sarjakuvat.flamingin.bde.rendition.offscreen.TilesetPainter
import kotlin.math.ceil

class ViewportOrchestrator {
    private val viewportSlivers: Array<ViewportSliver> = Array(9) { ViewportSliver() }
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
        val textureName = populator.asNewTexture()
        spriteSheet.populate(textureName)
        populator.deleteTextureAfterUse(textureName)
        val rooofPopulator = DrawableToTexture(width, height)
        for (i in viewportSlivers.indices) {
            populateSheet(viewportSlivers[i], populator, rooofPopulator)
        }

        rooofPopulator.deleteBitmapAfterUse()
        populator.deleteBitmapAfterUse()
    }

    private fun populateSheet(viewportSliver: ViewportSliver, floorPopulator: DrawableToTexture, rooofPopulator: DrawableToTexture) {
        val floorSink = floorPopulator.sink
        val rooofSink = rooofPopulator.sink
        floorPopulator.clearBitmapBeforeUse()
        rooofPopulator.clearBitmapBeforeUse()

        /* compute the world/viewport coordinate complex */
        val numTilesX = ceil(width / tileSize.toFloat()).toInt()
        val gridWidth = numTilesX * tileSize
        val gridLeftXpx = midpointOffsetX - gridWidth / 2f
        val numTilesY = ceil(height / tileSize.toFloat()).toInt()
        val gridHeight = numTilesY * tileSize
        val gridTopYpx = midpointOffsetY - gridHeight / 2f
        val arenaGridIndexLeft = Arena.midpointX - numTilesX / 2
        val arenaGridIndexTop = Arena.midpointY - numTilesY / 2

        viewportSliver.populate(width, height, numTilesX, numTilesY, gridLeftXpx, gridTopYpx, arenaGridIndexLeft, arenaGridIndexTop, floorSink, rooofSink)
        viewportSliver.floorTextureId = floorPopulator.asNewTexture()
        viewportSliver.rooofTextureId = rooofPopulator.asNewTexture()
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
            placeTestTile(tsP, x, 2)
            placeTestTile(tsP, x, bottomTile)
        }

        for (y in 2..bottomTile) {
            placeTestTile(tsP, 2, y)
            placeTestTile(tsP, rightTile, y)
        }

        placeTestTile(tsP, 3, 3)

        placeTestTile(tsP, rightTile-1, 3)
        placeTestTile(tsP, rightTile-2, 4)

        placeTestTile(tsP, rightTile-1, bottomTile-1)
        placeTestTile(tsP, rightTile-2, bottomTile-2)
        placeTestTile(tsP, rightTile-3, bottomTile-3)

        placeTestTile(tsP, 3, bottomTile-1)
        placeTestTile(tsP, 4, bottomTile-2)
        placeTestTile(tsP, 5, bottomTile-3)
        placeTestTile(tsP, 6, bottomTile-4)

        tsP.paintTile(15f * tileSize, 15f * tileSize, TileCatalog.Tiles.marbleFloor)

        val paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 7f
        paint.color = Color.MAGENTA
        sink.drawRoundRect(RectF(13f, 13f, width - 13f, height - 13f), 30f, 30f, paint)
        paint.strokeWidth = 5f
        paint.color = Color.YELLOW
        sink.drawRect(RectF(6f, 6f, width - 6f, height - 6f), paint)
    }

    private fun placeTestTile(tsP: TilesetPainter, x: Int, y: Int) {
        tsP.paintTile((x * tileSize).toFloat(), (y * tileSize).toFloat(), TileCatalog.Tiles.classicWall, MonominoLookup.primeIndexShy)
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
        for (i in viewportSlivers.indices) {
            viewportSlivers[i].releaseTextures()
        }

        spriteSheet.destroyOffscreenFramebuffer()
    }

    fun getViewportSliver(sliverX: Int, sliverY: Int): ViewportSliver {
        return viewportSlivers[sliverY * 3 + sliverX]
    }

    fun assignSliverPositionsAndTextures(floorToRender: Array<Sprite2d>, rooofToRender: Array<Sprite2d>) {
        floorToRender[0].setTexture(viewportSlivers[0].floorTextureId)
        floorToRender[1].setTexture(0)
        floorToRender[2].setTexture(0)
        floorToRender[3].setTexture(0)

        rooofToRender[0].setTexture(viewportSlivers[0].rooofTextureId)
        rooofToRender[1].setTexture(0)
        rooofToRender[2].setTexture(0)
        rooofToRender[3].setTexture(0)

        floorToRender[0].setPosition(width.toFloat()/2, height.toFloat()/2)
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
