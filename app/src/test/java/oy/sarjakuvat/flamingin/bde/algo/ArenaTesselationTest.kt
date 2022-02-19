package oy.sarjakuvat.flamingin.bde.algo

import org.junit.Assert
import org.junit.Test
import oy.sarjakuvat.flamingin.bde.level.Arena
import oy.sarjakuvat.flamingin.bde.rendition.offscreen.TilePainterBase.Companion.tileSize

class ArenaTesselationTest {
    @Test
    fun when_tesselateSingleCell_expect_boundsIdentical() {
        var tesselation = ArenaTesselation(tileSize,tileSize,0,0)

        Assert.assertEquals(tileSize, tesselation.gridWidth)
        Assert.assertEquals(tileSize, tesselation.gridHeight)
        Assert.assertEquals(1, tesselation.numTilesX)
        Assert.assertEquals(1, tesselation.numTilesY)
        Assert.assertEquals(Arena.midpointX, tesselation.arenaGridIndexLeft)
        Assert.assertEquals(Arena.midpointY, tesselation.arenaGridIndexTop)
        Assert.assertEquals(0f, tesselation.gridLeftXpx)
        Assert.assertEquals(0f, tesselation.gridTopYpx)
    }
}
