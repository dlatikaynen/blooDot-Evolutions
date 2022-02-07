package oy.sarjakuvat.flamingin.bde.algo

import java.lang.IllegalArgumentException

object MonominoIndex {
    const val noice = 69

    /* edge bitmask keys in the clumsy pack */
    const val edgeE = 4
    const val edgeN = 1
    const val edgeS = 16
    const val edgeW = 64

    /* corner bitmask keys in the clumsy pack */
    const val cornNE = 2
    const val cornSE = 8
    const val cornSW = 32
    const val cornNW = 128

    /* weird prerequisite for array literals */
    operator fun get(vararg array: Int) = array

    fun primeTileFrom(tileIndex: Int) : MonominoLookup {
        /* prime blobs */
        if (MonominoLookup.primeTiles.contains(tileIndex)) {
            return MonominoLookup(tileIndex)
        }

        var rotationIndex = this [4, 16, 64].indexOf(tileIndex);
        if (rotationIndex >= 0) {
            return MonominoLookup(1, rotationIndex + 1)
        }

        rotationIndex = this [20, 80, 65].indexOf(tileIndex);
        if (rotationIndex >= 0) {
            return MonominoLookup(5, rotationIndex + 1)
        }

        rotationIndex = this [28, 112, 193].indexOf(tileIndex);
        if (rotationIndex >= 0) {
            return MonominoLookup(7, rotationIndex + 1)
        }

        if (68 == tileIndex) {
            return MonominoLookup(17, 1)
        }

        rotationIndex = this [84, 81, noice].indexOf(tileIndex);
        if (rotationIndex >= 0) {
            return MonominoLookup(21, rotationIndex + 1)
        }

        rotationIndex = this [92, 113, 197].indexOf(tileIndex);
        if (rotationIndex >= 0) {
            return MonominoLookup(23, rotationIndex + 1)
        }

        rotationIndex = this [116, 209, 71].indexOf(tileIndex);
        if (rotationIndex >= 0) {
            return MonominoLookup(29, rotationIndex + 1)
        }

        rotationIndex = this [124, 241, 199].indexOf(tileIndex);
        if (rotationIndex >= 0) {
            return MonominoLookup(31, rotationIndex + 1)
        }

        rotationIndex = this [93, 117, 213].indexOf(tileIndex);
        if (rotationIndex >= 0) {
            return MonominoLookup(87, rotationIndex + 1)
        }

        rotationIndex = this [125, 245, 215].indexOf(tileIndex);
        if (rotationIndex >= 0) {
            return MonominoLookup(95, rotationIndex + 1)
        }

        if (221 == tileIndex) {
            return MonominoLookup(119, 1)
        }

        rotationIndex = this [253, 247, 223].indexOf(tileIndex)
        if (rotationIndex >= 0) {
            return MonominoLookup(127, rotationIndex + 1)
        }

        throw IllegalArgumentException("tileIndex $tileIndex is not an index to a S-V2E2 set")
    }

    fun coalescedTileFromNeighborConfiguration(neighborConfiguration: Int): Int {
        return MonominoLookup.seamLess[neighborConfiguration]
    }

    fun coalescedTileFromNeighbors(e: Boolean, ne: Boolean, n: Boolean, nw: Boolean, w: Boolean, sw: Boolean, s: Boolean, se: Boolean): Int {
        val neighborConfiguration = (if(nw) 1 else 0) +
        ((if(n) 1 else 0) shl 1) +
        ((if(ne) 1 else 0) shl 2) +
        ((if(e) 1 else 0) shl 3) +
        ((if(se) 1 else 0) shl 4) +
        ((if(s) 1 else 0) shl 5) +
        ((if(sw) 1 else 0) shl 6) +
        ((if(w) 1 else 0) shl 7)

        return coalescedTileFromNeighborConfiguration(neighborConfiguration)
    }
}
