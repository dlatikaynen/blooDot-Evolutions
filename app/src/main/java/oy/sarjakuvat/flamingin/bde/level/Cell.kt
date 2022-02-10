package oy.sarjakuvat.flamingin.bde.level

class Cell(private val arena: Arena, private val x: Int, private val y: Int) {
    val contents: MutableList<Entity> = listOf<Entity>().toMutableList()
    val neighborW: Cell? get() = if(x == 0) null else arena.cells[x - 1][y];

    init {

    }
}