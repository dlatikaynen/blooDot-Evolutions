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
    /* compute the world/viewport coordinate complex
     * invariant part depending only on sheet and tile size */
    val numTilesX: Int = ceil(viewportWidth / TilePainterBase.tileSize.toFloat()).toInt()
    val numTilesY: Int = ceil(viewportHeight / TilePainterBase.tileSize.toFloat()).toInt()
    val gridWidth: Int = numTilesX * TilePainterBase.tileSize
    val gridHeight: Int = numTilesY * TilePainterBase.tileSize

    /* dynamic part depending on sheet's position in in arena */
    val gridLeftXpx: Float = viewportWidth / 2f - gridWidth / 2f
    val gridTopYpx: Float = viewportHeight / 2f - gridHeight / 2f
    val arenaGridIndexLeft: Int = Arena.midpointX - numTilesX / 2
    val arenaGridIndexTop: Int = Arena.midpointY - numTilesY / 2
}
