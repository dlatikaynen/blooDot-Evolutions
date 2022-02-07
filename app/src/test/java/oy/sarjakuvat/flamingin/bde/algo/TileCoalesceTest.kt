package oy.sarjakuvat.flamingin.bde.algo

import org.junit.Assert.*
import org.junit.Test

class TileCoalesceTest {
    /* weird prerequisite for array literals */
    operator fun get(vararg array: Int) = array

    @Test
    fun when_iterateAllNeighborConfigurations_expect_correctHistogram() {
        val histogram = emptyMap<Int, Int>().toMutableMap()
        for (nc in 0..0xff) {
            val tileIndex = MonominoIndex.coalescedTileFromNeighborConfiguration(nc)
            if (histogram.containsKey(tileIndex)) {
                histogram[tileIndex] = histogram[tileIndex]!! + 1
            } else {
                histogram[tileIndex] = 1
            }
        }

        val distinctTiles = histogram.size
        val distinctFrequencies = histogram.values.toSet().toList()
        val sortedFrequencies = distinctFrequencies.sorted()
        var countInClumsy = 0
        for(row in 0 until MonominoLookup.clumsyPack.size) {
            countInClumsy += MonominoLookup.clumsyPack[row].size
        }

        assertEquals((this [1, 2, 4, 6, 8, 10, 12, 16, 20]).toList(), sortedFrequencies)
        assertEquals(countInClumsy, distinctTiles)
    }
}
