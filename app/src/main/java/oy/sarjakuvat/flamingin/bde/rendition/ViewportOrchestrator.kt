package oy.sarjakuvat.flamingin.bde.rendition

import android.graphics.*
import oy.sarjakuvat.flamingin.bde.BuildConfig
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
            populateSheet(i, populator, rooofPopulator)
        }

        rooofPopulator.deleteBitmapAfterUse()
        populator.deleteBitmapAfterUse()
    }

    private fun populateSheet(sliverIndex: Int, floorPopulator: DrawableToTexture, rooofPopulator: DrawableToTexture) {
        val viewportSliver = viewportSlivers[sliverIndex]
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
        if(BuildConfig.DEBUG) {
            val paint = Paint()
            paint.textSize = 67f
            paint.textAlign = Paint.Align.CENTER
            val textMetrics = Rect()
            paint.getTextBounds(sliverIndex.toString(), 0, sliverIndex.toString().length, textMetrics)
            paint.style = Paint.Style.FILL
            paint.color = Color.argb(0.25f,0f,0.3f,0.9f)
            rooofSink.drawText(sliverIndex.toString(), width / 2f, height / 2f + textMetrics.height() / 2f, paint)
            paint.style = Paint.Style.STROKE
            paint.color = Color.CYAN
            rooofSink.drawText(sliverIndex.toString(), width / 2f, height / 2f + textMetrics.height() / 2f, paint)
        }

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
        floorToRender[1].setTexture(viewportSlivers[1].floorTextureId)
        floorToRender[2].setTexture(viewportSlivers[2].floorTextureId)
        floorToRender[3].setTexture(viewportSlivers[3].floorTextureId)

        rooofToRender[0].setTexture(viewportSlivers[0].rooofTextureId)
        rooofToRender[1].setTexture(viewportSlivers[1].rooofTextureId)
        rooofToRender[2].setTexture(viewportSlivers[2].rooofTextureId)
        rooofToRender[3].setTexture(viewportSlivers[3].rooofTextureId)

        assignSliverPositions(floorToRender, rooofToRender, 0f, 0f)
    }

    fun assignSliverPositions(floorToRender: Array<Sprite2d>, rooofToRender: Array<Sprite2d>, localScrollOffsetX: Float, localScrollOffsetY: Float) {
        /* top left */
        floorToRender[0].setPosition(localScrollOffsetX, height.toFloat() - localScrollOffsetY)
        rooofToRender[0].setPosition(localScrollOffsetX, height.toFloat() - localScrollOffsetY)

        /* top right */
        floorToRender[1].setPosition(localScrollOffsetX + width.toFloat(), height.toFloat() - localScrollOffsetX)
        rooofToRender[1].setPosition(localScrollOffsetX + width.toFloat(), height.toFloat() - localScrollOffsetX)

        /* bottom left */
        floorToRender[2].setPosition(localScrollOffsetX, -localScrollOffsetX)
        rooofToRender[2].setPosition(localScrollOffsetX, -localScrollOffsetX)

        /* bottom right */
        floorToRender[3].setPosition(localScrollOffsetX + width.toFloat(), -localScrollOffsetX)
        rooofToRender[3].setPosition(localScrollOffsetX + width.toFloat(), -localScrollOffsetY)
    }
}
