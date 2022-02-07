package oy.sarjakuvat.flamingin.bde.algo

class TileMatrix(val size: Int) {
    private var matrix : Array<Array<Boolean>>

    init {
        assert(size > 0)
        matrix = Array(size) { Array(size) { false } }
    }

    fun setCoordinates(xCoordinate: Int, yCoordinate : Int) {
        clear()
        setElement(xCoordinate, yCoordinate)
    }

    fun addCoordinates(xCoordinate: Int, yCoordinate: Int) {
        setElement(xCoordinate, yCoordinate)
    }

    fun getElement(colIndex: Int, rowIndex: Int) : Boolean {
        return matrix[rowIndex][colIndex]
    }

    fun setElement(colIndex: Int, rowIndex: Int) {
        (matrix[rowIndex])[colIndex] = true
    }

    fun clearElement(colIndex: Int, rowIndex: Int) {
        matrix[rowIndex][colIndex] = false
    }

    fun clear() {
        while (getY() > 0) {
            clearElement(getX(), getY())
        }
    }

    fun getX(): Int {
        for (y in 0 until size) {
            val row = matrix[y]
            val col = row.indexOf(true)
            if (col >= 0) {
                return col
            }
        }

        return -1
    }

    fun getY(): Int {
        for (y in 0 until size) {
            val row = matrix[y]
            val col = row.indexOf(true)
            if (col >= 0) {
                return y
            }
        }

        return -1
    }

    /// Rotates the matrix 90° clockwise
    fun rotate() {
        val rotatedMatrix = Array(size) { Array(size) { false } }
        // 90° rotation is transpose-and-reverse
        for (y in 0 until size) {
            for (x in 0 until size) {
                rotatedMatrix[y][x] = matrix[x][y]
            }
        }

        for (y in 0 until size) {
            rotatedMatrix[y].reverse()
        }

        matrix = rotatedMatrix
    }
}
