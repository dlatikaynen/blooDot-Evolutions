package oy.sarjakuvat.flamingin.bde.algo

import org.junit.Assert
import org.junit.Test
import oy.sarjakuvat.flamingin.bde.level.Arena
import oy.sarjakuvat.flamingin.bde.rendition.offscreen.TilePainterBase.Companion.tileSize

class ArenaTesselationTest {
    @Test
    fun when_tesselateSingleCell_expect_boundsIdentical() {
        val tesselation = ArenaTesselation(tileSize,tileSize,0,0)

        Assert.assertEquals(tileSize, tesselation.gridWidth)
        Assert.assertEquals(tileSize, tesselation.gridHeight)
        Assert.assertEquals(1, tesselation.numTilesX)
        Assert.assertEquals(1, tesselation.numTilesY)
        Assert.assertEquals(Arena.midpointX, tesselation.arenaGridIndexLeft)
        Assert.assertEquals(Arena.midpointY, tesselation.arenaGridIndexTop)
        Assert.assertEquals(0f, tesselation.gridLeftXpx)
        Assert.assertEquals(0f, tesselation.gridTopYpx)
    }

    @Test
    fun when_tesselateRealisticCenter_expect_fractionalBounds() {
        val tesselation = ArenaTesselation(128,96,0,0)

        Assert.assertEquals(4, tesselation.numTilesX)
        Assert.assertEquals(3, tesselation.numTilesY)
        Assert.assertEquals(4 * tileSize, tesselation.gridWidth)
        Assert.assertEquals(3 * tileSize, tesselation.gridHeight)
        Assert.assertEquals(Arena.midpointX - 2, tesselation.arenaGridIndexLeft)
        Assert.assertEquals(Arena.midpointY - 1, tesselation.arenaGridIndexTop)
        Assert.assertEquals(-2f, tesselation.gridLeftXpx)
        Assert.assertEquals(-1.5f, tesselation.gridTopYpx)
    }

    @Test
    fun when_tesselateRealisticNW_expect_localBounds() {
        val tesselation = ArenaTesselation(128,96,-1,-1)

        Assert.assertEquals(4, tesselation.numTilesX)
        Assert.assertEquals(3, tesselation.numTilesY)
        Assert.assertEquals(4 * tileSize, tesselation.gridWidth)
        Assert.assertEquals(3 * tileSize, tesselation.gridHeight)
        Assert.assertEquals(Arena.midpointX - 2, tesselation.arenaGridIndexLeft)
        Assert.assertEquals(Arena.midpointY - 1, tesselation.arenaGridIndexTop)
        Assert.assertEquals(-2f, tesselation.gridLeftXpx)
        Assert.assertEquals(-1.5f, tesselation.gridTopYpx)
    }
}
