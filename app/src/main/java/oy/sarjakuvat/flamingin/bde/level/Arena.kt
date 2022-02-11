package oy.sarjakuvat.flamingin.bde.level

import android.util.Log
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import oy.sarjakuvat.flamingin.bde.algo.MonominoLookup
import oy.sarjakuvat.flamingin.bde.level.tilesets.TileCatalog

object Arena {
    private const val gridUnitsEW = 1024
    private const val gridUnitsNS = 1024
    private const val midpointX = 512
    private const val midpointY = 512

    private var minOccupiedGridX = midpointX
    private var maxOccupiedGridX = midpointX
    private var minOccupiedGridY = midpointY
    private var maxOccupiedGridY = midpointY

    var cells: Array<Array<Cell?>> = Array(gridUnitsEW) {
        Array(gridUnitsNS) { null }
    }

    private val shadowClones: HashMap<Entity, Entity> = HashMap()

    fun load(fromSaveFile: String = "") {
        /* 1. procedural generation of overworld */
        generateOverworld()

        /* if a save file is specified, apply its changes */
        if(fromSaveFile.isNotBlank()) {
            applySaveFileDelta(fromSaveFile)
        }
    }

    suspend fun dumpDebugInfo() = coroutineScope {
        launch {
            var countCellsNull = 0
            var countCellsEmpty = 0
            var countCellsOccupied = 0
            var countGridlockedEntities = 0
            var countComplexEntities = 0
            for (y in 0 until gridUnitsNS) {
                for (x in 0 until gridUnitsEW) {
                    val cell = cells[x][y]
                    if (cell == null) {
                        ++countCellsNull
                    } else {
                        if (cell.contents.isEmpty()) {
                            ++countCellsEmpty
                        } else {
                            ++countCellsOccupied
                            for (entity in cell.contents) {
                                if (entity.isGridlocked) {
                                    ++countGridlockedEntities
                                } else {
                                    ++countComplexEntities
                                }
                            }
                        }
                    }
                }
            }

            Log.d(Arena::class.simpleName, " #*# arena is $gridUnitsEW wide by $gridUnitsNS tall")
            Log.d(
                Arena::class.simpleName,
                "   # bounding box is ($minOccupiedGridX, $minOccupiedGridY) - ($maxOccupiedGridX, $maxOccupiedGridY)"
            )

            Log.d(Arena::class.simpleName, "  -# ${countCellsNull.toString().padStart(9)} null")
            Log.d(Arena::class.simpleName, "   # ${countCellsEmpty.toString().padStart(9)} empty")

            Log.d(
                Arena::class.simpleName,
                "   # ${countCellsOccupied.toString().padStart(9)} occupied"
            )

            Log.d(
                Arena::class.simpleName,
                "   # ${countGridlockedEntities.toString().padStart(9)} gridlocked entities"
            )

            Log.d(
                Arena::class.simpleName,
                "   # ${countComplexEntities.toString().padStart(9)} complex entities"
            )

            Log.d(
                Arena::class.simpleName,
                "  ~# ${shadowClones.count().toString().padStart(9)} shadow clones"
            )
        }
    }

    private fun placeTile(gridX: Int, gridY: Int, tileIndex: Int, primeMonomino: Int = MonominoLookup.coalesceWithNeighbors) {
        val cell = ensureCell(gridX, gridY)
        val candidateEntity = Entity(tileIndex)
        val entity = shadowClones.putIfAbsent(candidateEntity, candidateEntity) ?: candidateEntity
        cell.contents.add(entity)
    }

    private fun ensureCell(gridX: Int, gridY: Int) : Cell {
        minOccupiedGridX = minOccupiedGridX.coerceAtMost(gridX)
        maxOccupiedGridX = maxOccupiedGridX.coerceAtLeast(gridX)
        minOccupiedGridY = minOccupiedGridY.coerceAtMost(gridY)
        maxOccupiedGridY = maxOccupiedGridY.coerceAtLeast(gridY)
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
}
