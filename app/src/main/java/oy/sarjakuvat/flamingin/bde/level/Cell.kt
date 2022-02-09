package oy.sarjakuvat.flamingin.bde.level

class Cell {
    val statics: MutableList<Entity> = listOf<Entity>().toMutableList()
    val mobs: MutableList<Entity> = listOf<Entity>().toMutableList()

    var neighborW: Cell = this
    var neighborNW: Cell = this
    var neighborN: Cell = this
    var neighborNE: Cell = this
    var neighborE: Cell = this
    var neighborSE: Cell = this
    var neighborS: Cell = this
    var neighborSW: Cell = this

    var neighbors = listOf(
        neighborW,
        neighborNW,
        neighborN,
        neighborNE,
        neighborE,
        neighborSE,
        neighborS,
        neighborSW
    )

    init {

    }
}