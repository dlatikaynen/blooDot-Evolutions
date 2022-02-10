package oy.sarjakuvat.flamingin.bde.level

import oy.sarjakuvat.flamingin.bde.algo.MonominoLookup
import oy.sarjakuvat.flamingin.bde.level.tilesets.TileCatalog

class Arena {
    var cells: Array<Array<Cell?>> = Array(gridUnitsEW) { Array(gridUnitsNS) { null } }

    private val gridLockedEntities: HashMap<Entity, Entity> = HashMap()

    fun load(fromSaveFile: String = "") {
        /* 1. procedural generation of overworld */
        generateOverworld()

        /* if a save file is specified, apply its changes */
        if(fromSaveFile.isNotBlank()) {
            applySaveFileDelta(fromSaveFile)
        }
    }

    private fun placeTile(gridX: Int, gridY: Int, tileIndex: Int, primeMonomino: Int = MonominoLookup.coalesceWithNeighbors) {
        val cell = ensureCell(gridX, gridY)
        val candidateEntity = Entity(tileIndex)
        val entity = gridLockedEntities.putIfAbsent(candidateEntity, candidateEntity) ?: candidateEntity
        cell.contents.add(entity)
    }

    private fun ensureCell(gridX: Int, gridY: Int) : Cell {
        var haveCell: Cell? = cells[gridX][gridY]
        if(haveCell == null)
        {
            haveCell = Cell(this, gridX, gridY)
            cells[gridX][gridY] = haveCell
        }

        return haveCell
    }

    private fun generateOverworld() {
        placeTile(midpointX, midpointY, TileCatalog.FloorTiles.marbleFloor, MonominoLookup.primeIndexShy)
        placeTile(midpointX + 1, midpointY + 1, TileCatalog.FloorTiles.marbleFloor, MonominoLookup.primeIndexShy)
        placeTile(midpointX - 3, midpointY, TileCatalog.FloorTiles.marbleFloor, MonominoLookup.primeIndexShy)
    }

    private fun applySaveFileDelta(fromFile: String) {

    }

    companion object {
        const val gridUnitsEW = 1024
        const val gridUnitsNS = 1024
        const val midpointX = 512
        const val midpointY = 512
   }
}