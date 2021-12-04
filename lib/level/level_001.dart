import 'package:bloo_dot_evolutions/algo/monomino_lookup.dart';
import 'package:bloo_dot_evolutions/arena/level_base.dart';
import 'package:bloo_dot_evolutions/level/tilesets/gray_wall_tile_set.dart';
import 'package:flame/game.dart';

class Level001 extends LevelBase {
  Level001(FlameGame gameRef) : super(gameRef);

  @override
  String get levelName => "Got to get in to get out";

  @override
  int get levelNumTilesX => 50;

  @override
  int get levelNumTilesY => 20;

  @override
  Future paintSpecific() async {
    selectTileSet(GreyWallTileSet());
    for (var y = 0; y < 7; ++y) {
      for (var x = 0; x < 7; ++x) {
        placeTile(canvas, midTileX + x, midTileY + y - 4, MonominoLookup.clumsyPack[y][x]);
      }
    }
  }
}
