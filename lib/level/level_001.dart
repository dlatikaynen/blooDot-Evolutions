import 'package:bloo_dot_evolutions/algo/monomino_lookup.dart';
import 'package:bloo_dot_evolutions/arena/level_base.dart';
import 'package:bloo_dot_evolutions/level/tilesets/gray_wall_tile_set.dart';

class Level001 extends LevelBase {
  Level001() : super();

  static const _walls = [
    "11111111111111111111",
    "10000001000000000001",
    "10000001000000000001",
    "10000001111110000001",
    "10000100100010000001",
    "10000100000010000001"
  ];

  @override
  String get levelName => "Got to get in to get out";

  @override
  int get levelNumTilesX => 24;

  @override
  int get levelNumTilesY => 16;

  @override
  Future drawSpecific() async {
    await selectTileSet(GreyWallTileSet());
    for (var y = 0; y < 12; ++y) {
      for (var x = 0; x < 20; ++x) {
        if (_walls[y > 5 ? (11 - y) : y].substring(x, x + 1) == "1") {
          placeBlobTile(canvas, x + 2, y + 2, MonominoLookup.primeIndexShy);
        }
      }
    }

    floodFillTile(canvas, 4, 4, MonominoLookup.primeIndexShy);
    placeStaticTile(canvas, 3, 3, 0, 0);
    placeStaticTile(canvas, 4, 3, 1, 0);
    placeStaticTile(canvas, 5, 3, 2, 0);
    placeStaticTile(canvas, 3, 4, 0, 1);
    placeStaticTile(canvas, 4, 4, 1, 1);
    placeStaticTile(canvas, 5, 4, 2, 1);
    placeStaticTile(canvas, 3, 5, 0, 2);
    placeStaticTile(canvas, 4, 5, 1, 2);
    placeStaticTile(canvas, 5, 5, 2, 2);
  }
}
