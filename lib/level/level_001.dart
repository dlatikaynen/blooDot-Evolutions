import 'dart:ui';

import 'package:bloo_dot_evolutions/algo/monomino_lookup.dart';
import 'package:bloo_dot_evolutions/arena/level_base.dart';
import 'package:bloo_dot_evolutions/level/tilesets/gray_wall_tile_set.dart';
import 'package:flame/components.dart';
import 'package:flutter/material.dart';

class Level001 extends LevelBase {
  @override
  Future<Sprite> createLevel() async {
    int numTilesX = 50, numTilesY = 20;

    var sink = PictureRecorder();
    var canvas = Canvas(sink);
    var linePaint = Paint()
      ..style = PaintingStyle.stroke
      ..color = Colors.white12;

    for (var y = 1.0; y < numTilesY; ++y) {
      canvas.drawLine(
          Offset(0, y * LevelBase.tileSize), Offset(numTilesX * LevelBase.tileSize, y * LevelBase.tileSize), linePaint);
    }

    for (var x = 1; x < numTilesX; ++x) {
      canvas.drawLine(
          Offset(x * LevelBase.tileSize, 0), Offset(x * LevelBase.tileSize, numTilesY * LevelBase.tileSize), linePaint);
    }

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
