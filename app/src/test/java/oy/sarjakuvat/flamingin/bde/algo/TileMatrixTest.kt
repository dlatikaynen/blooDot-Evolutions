package oy.sarjakuvat.flamingin.bde.algo

import org.junit.Test

import org.junit.Assert.*

class TileMatrixTest {
    @Test
    fun when_coordinatesSet_expect_retrieveSame() {
        val testMatrix = TileMatrix(3)
        testMatrix.setCoordinates(2, 1)

        assertEquals(2, testMatrix.getX())
        assertEquals(1, testMatrix.getY())

        testMatrix.clear()

        assertEquals(-1, testMatrix.getX())
        assertEquals(-1, testMatrix.getY())
    }

    @Test
    fun when_rotateOnce_expect_rotatedOnce() {
        val testMatrix = TileMatrix(3)
        testMatrix.setCoordinates(2, 1)

        testMatrix.rotate()

        assertEquals(1, testMatrix.getX())
        assertEquals(2, testMatrix.getY())
    }
}
