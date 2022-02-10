package oy.sarjakuvat.flamingin.bde.level

data class Entity(private val tileIndex: Int, var isGridlocked: Boolean = true) {
    var positionX: Float = 0.0f
    var positionY: Float = 0.0f

    override fun equals(other: Any?): Boolean {
        // we are not interested in the grid position,
        // if this is a static gridlocked tile
        // so it will not be instantiated another time
        return this === other
               || (
                      (other is Entity)
                      && other.tileIndex == tileIndex
                      && other.isGridlocked == isGridlocked
                  )
    }

    override fun hashCode(): Int {
        var result = tileIndex
        result = 31 * result + isGridlocked.hashCode()
        return result
    }
}