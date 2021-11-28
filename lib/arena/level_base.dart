import 'dart:ui';

import 'package:bloo_dot_evolutions/algo/blob_tileset_painter.dart';
import 'package:bloo_dot_evolutions/level/tilesets/gray_wall_tile_set.dart';
import 'package:flame/components.dart';
import 'package:flutter/material.dart';

abstract class LevelBase extends SpriteComponent with HasGameRef {
  static const tileSize = 33.0;

  Future<Sprite> createLevel();

  @override
  Future<void>? onLoad() async {
    sprite = await createLevel();
    size = sprite!.originalSize;
    return super.onLoad();
  }
}

class Level extends LevelBase {
  @override
  Future<Sprite> createLevel() async {
    int numTilesX = 200, numTilesY = 77;

    var sink = PictureRecorder();
    var canvas = Canvas(sink);
    canvas.drawRect(Rect.fromLTWH(0, 0, numTilesX * LevelBase.tileSize, numTilesY * LevelBase.tileSize),
        Paint()..color = Colors.black54);

    var centerRect = Rect.fromLTWH((numTilesX / 2) * LevelBase.tileSize, (numTilesY / 2) * LevelBase.tileSize,
        LevelBase.tileSize, LevelBase.tileSize);
    canvas.drawRect(
        centerRect,
        Paint()
          ..color = Colors.grey
          ..style = PaintingStyle.stroke);

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

    canvas.save();
    canvas.translate(centerRect.left, centerRect.top);
    BlobTileSetPainter(GreyWallTileSet(), canvas).paintTile(209);
    canvas.restore();

    var picture = sink.endRecording();
    var image = await picture.toImage(numTilesX * LevelBase.tileSize.toInt(), numTilesY * LevelBase.tileSize.toInt());
    return Sprite(image);
  }
}
