package oy.sarjakuvat.flamingin.bde.algo

import oy.sarjakuvat.flamingin.bde.level.Arena
import oy.sarjakuvat.flamingin.bde.rendition.offscreen.TilePainterBase
import kotlin.math.ceil

class ArenaTesselation(
    viewportWidth: Int,
    viewportHeight: Int,
    private val colSheetInArena: Int,
    private val rowSheetInArena: Int,
) {
    val numTilesX: Int
    val numTilesY: Int
    val gridWidth: Int
    val gridHeight: Int

    val gridLeftXpx: Float
    val gridTopYpx: Float
    val arenaGridIndexLeft: Int
    val arenaGridIndexTop: Int

    init {
        /* compute the world/viewport coordinate complex
         * invariant part depending only on sheet and tile size */
        numTilesX = ceil(viewportWidth / TilePainterBase.tileSize.toFloat()).toInt()
        gridWidth = numTilesX * TilePainterBase.tileSize
        numTilesY = ceil(viewportHeight / TilePainterBase.tileSize.toFloat()).toInt()
        gridHeight = numTilesY * TilePainterBase.tileSize

        /* dynamic part depending on sheet's position in in arena */
        gridLeftXpx = viewportWidth / 2f - gridWidth / 2f
        gridTopYpx = viewportHeight / 2f - gridHeight / 2f
        arenaGridIndexLeft = Arena.midpointX - numTilesX / 2
        arenaGridIndexTop = Arena.midpointY - numTilesY / 2
    }
}
