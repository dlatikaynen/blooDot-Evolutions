import 'dart:ui';

import 'package:bloo_dot_evolutions/algo/monomino_lookup.dart';
import 'package:bloo_dot_evolutions/arena/level_base.dart';
import 'package:bloo_dot_evolutions/level/tilesets/gray_wall_tile_set.dart';
import 'package:flame/components.dart';
import 'package:flutter/material.dart';

class Level001 extends LevelBase {
  @override
  Future<Sprite> createLevel() async {
    int numTilesX = 200, numTilesY = 77;

    var sink = PictureRecorder();
    var canvas = Canvas(sink);
    canvas.drawRect(Rect.fromLTWH(0, 0, numTilesX * LevelBase.tileSize, numTilesY * LevelBase.tileSize),
        Paint()..color = Colors.black54);

    canvas.drawRect(
        const Rect.fromLTWH(2 * LevelBase.tileSize, 2 * LevelBase.tileSize, LevelBase.tileSize, LevelBase.tileSize),
        Paint()..color = Colors.grey);

    canvas.drawRect(
        const Rect.fromLTWH(3 * LevelBase.tileSize, 2 * LevelBase.tileSize, LevelBase.tileSize, LevelBase.tileSize),
        Paint()..color = Colors.grey);

    canvas.drawRect(
        const Rect.fromLTWH(20 * LevelBase.tileSize, 2 * LevelBase.tileSize, LevelBase.tileSize, LevelBase.tileSize),
        Paint()..color = Colors.grey);

    canvas.drawRect(
        const Rect.fromLTWH(50 * LevelBase.tileSize, 2 * LevelBase.tileSize, LevelBase.tileSize, LevelBase.tileSize),
        Paint()..color = Colors.grey);

    canvas.drawRect(
        const Rect.fromLTWH(3 * LevelBase.tileSize, 3 * LevelBase.tileSize, LevelBase.tileSize, LevelBase.tileSize),
        Paint()..color = Colors.grey);

    var midPointX = numTilesX ~/ 2;
    var midPointY = numTilesY ~/ 2 - 4;
    selectTileSet(GreyWallTileSet());

    for (var y = 0; y < 7; ++y) {
      for (var x = 0; x < 7; ++x) {
        placeTile(canvas, midPointX + x, midPointY + y, MonominoLookup.clumsyPack[y][x]);
      }
    }

    var picture = sink.endRecording();
    var image = await picture.toImage(numTilesX * LevelBase.tileSize.toInt(), numTilesY * LevelBase.tileSize.toInt());
    return Sprite(image);
  }
}
