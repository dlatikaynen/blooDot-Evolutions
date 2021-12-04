import 'dart:ui';

import 'package:bloo_dot_evolutions/algo/blob_tile_painter_base.dart';
import 'package:bloo_dot_evolutions/algo/blob_tileset_painter.dart';
import 'package:flame/components.dart';
import 'package:flame/game.dart';
import 'package:flutter/material.dart';

abstract class LevelBase extends SpriteComponent {
  static const tileSize = 33.0;
  final FlameGame gameRef;
  final linePaint = Paint()
    ..style = PaintingStyle.stroke
    ..color = Colors.white12;

  BlobTilePainterBase? _selectedTileSet;
  late Canvas canvas;

  String get levelName;
  int get levelNumTilesX;
  int get levelNumTilesY;
  double get sizePixelsX => levelNumTilesX * LevelBase.tileSize;
  double get sizePixelsY => levelNumTilesY * LevelBase.tileSize;
  int get midTileX => levelNumTilesX ~/ 2;
  int get midTileY => levelNumTilesY ~/ 2;

  LevelBase(this.gameRef);

  Future paintSpecific();

  @override
  Future<void>? onLoad() async {
    sprite = await _createLevel();
    size = sprite!.originalSize;
    return super.onLoad();
  }

  void selectTileSet(BlobTilePainterBase tileSet) => _selectedTileSet = tileSet;

  Future paintBackdrop() async {
    var universeImage = await gameRef.images.load("universe_seamless.png");
    if (universeImage.width < sizePixelsX) {
      throw ArgumentError("Level too wide for background");
    }

    if (universeImage.height < sizePixelsY) {
      throw ArgumentError("Level too high for background");
    }

    canvas.drawImageRect(universeImage, Rect.fromLTWH(0, 0, sizePixelsX, sizePixelsY),
        Rect.fromLTWH(0, 0, sizePixelsX, sizePixelsY), Paint());
  }

  Future paintTileGrid() async {
    for (var y = 1.0; y < levelNumTilesY; ++y) {
      canvas.drawLine(Offset(0, y * LevelBase.tileSize),
          Offset(levelNumTilesX * LevelBase.tileSize, y * LevelBase.tileSize), linePaint);
    }

    for (var x = 1; x < levelNumTilesX; ++x) {
      canvas.drawLine(Offset(x * LevelBase.tileSize, 0),
          Offset(x * LevelBase.tileSize, levelNumTilesY * LevelBase.tileSize), linePaint);
    }
  }

  void placeTile(Canvas paintTo, int gridX, int gridY, int tileIndex) {
    if (_selectedTileSet == null) {
      return;
    }

    paintTo.save();
    paintTo.translate(gridX * tileSize, gridY * tileSize);
    BlobTileSetPainter(_selectedTileSet!, paintTo).paintTile(tileIndex);
    paintTo.restore();
  }

  Future<Sprite> _createLevel() async {
    var sink = PictureRecorder();
    canvas = Canvas(sink);
    await paintBackdrop();
    await paintTileGrid();
    await paintSpecific();
    var picture = sink.endRecording();
    var image =
        await picture.toImage(levelNumTilesX * LevelBase.tileSize.toInt(), levelNumTilesY * LevelBase.tileSize.toInt());

    return Sprite(image);
  }
}
