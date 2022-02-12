package oy.sarjakuvat.flamingin.bde.level

import oy.sarjakuvat.flamingin.bde.algo.MonominoLookup

data class Entity(
    val tileNumberFloor: Int,
    val tileNumberRooof: Int,
    var isGridlocked: Boolean = true,
    var monominoIndex: Int = MonominoLookup.primeIndexShy
) {
    var positionX: Float = 0.0f
    var positionY: Float = 0.0f

    override fun equals(other: Any?): Boolean {
        // we are not interested in the grid position,
        // if this is a static gridlocked tile
        // so it will not be instantiated another time
        return this === other
               || (
                      (other is Entity)
                      && other.tileNumberFloor == tileNumberFloor
                      && other.tileNumberRooof == tileNumberRooof
                      && other.isGridlocked == isGridlocked
                      && other.monominoIndex == monominoIndex
                  )
    }

    override fun hashCode(): Int {
        var result = tileNumberFloor
        result = 31 * result + tileNumberRooof.hashCode()
        result = 31 * result + isGridlocked.hashCode()
        result = 31 * result + monominoIndex.hashCode()
        return result
    }
}
