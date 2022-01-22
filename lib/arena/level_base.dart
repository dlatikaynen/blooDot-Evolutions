import 'dart:ui';

import 'package:bloo_dot_evolutions/algo/blob_tile_painter_base.dart';
import 'package:bloo_dot_evolutions/algo/blob_tileset_painter.dart';
import 'package:flame/components.dart';
import 'package:flutter/material.dart';

import 'arena_object.dart';

abstract class LevelBase extends SpriteComponent {
  static const tileSize = 33.0;

  final linePaint = Paint()
    ..style = PaintingStyle.stroke
    ..color = Colors.white12;

  late List<ArenaObject> staticObjects = [];
  late List<ArenaObject> activeObjects = [];
  late List<List<ArenaObject?>> arena = [];
  BlobTilePainter? _selectedTileSet;
  late Canvas canvas;

  String get levelName;
  int get levelNumTilesX;
  int get levelNumTilesY;
  double get sizePixelsX => levelNumTilesX * LevelBase.tileSize;
  double get sizePixelsY => levelNumTilesY * LevelBase.tileSize;
  int get midTileX => levelNumTilesX ~/ 2;
  int get midTileY => levelNumTilesY ~/ 2;

  LevelBase();

  Future drawSpecific();

  @override
  Future<void>? onLoad() async {
    sprite = await _createLevel();
    size = sprite!.originalSize;
    return super.onLoad();
  }

  Future selectTileSet(BlobTilePainter tileSet) async {
    await tileSet.load();
    _selectedTileSet = tileSet;
  }

  Future paintBackdrop() async {
    // var universeImage = await gameRef.images.load("universe_seamless.png");
    // if (universeImage.width < sizePixelsX) {
    //   throw ArgumentError("Level too wide for background");
    // }
    //
    // if (universeImage.height < sizePixelsY) {
    //   throw ArgumentError("Level too high for background");
    // }
    //
    // canvas.drawImageRect(universeImage, Rect.fromLTWH(0, 0, sizePixelsX, sizePixelsY),
    //     Rect.fromLTWH(0, 0, sizePixelsX, sizePixelsY), Paint());
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

  void placeBlobTile(Canvas paintTo, int gridX, int gridY, int tileIndex) {
    if (_selectedTileSet == null) {
      return;
    }

    addStaticTileObject(gridX, gridY, tileIndex);
    paintTo.save();
    paintTo.translate(gridX * tileSize, gridY * tileSize);
    TileSetPainter(_selectedTileSet!, paintTo).paintBlobTile(tileIndex);
    paintTo.restore();
  }

  void placeStaticTile(Canvas paintTo, int gridX, int gridY, int tileIndexX, int tileIndexY) {
    if (_selectedTileSet == null) {
      return;
    }

    paintTo.save();
    paintTo.translate(gridX * tileSize, gridY * tileSize);
    TileSetPainter(_selectedTileSet!, paintTo).paintStaticTile(tileIndexX, tileIndexY);
    paintTo.restore();
  }

  void addStaticTileObject(int gridX, int gridY, int tileIndex) {
    var object = ArenaObject()
      ..anchorTileX = gridX
      ..anchorTileY = gridY
      ..boundingBox = const Offset(LevelBase.tileSize, LevelBase.tileSize)
      ..currentPosition = Offset(gridX * LevelBase.tileSize, gridY * LevelBase.tileSize)
      ..fromTileSet = _selectedTileSet!
      ..indexInTileSet = tileIndex;

    arena[gridX][gridY] = object;
    staticObjects.add(object);
  }

  Future<Sprite> _createLevel() async {
    arena = List.filled(levelNumTilesX, List.filled(levelNumTilesY, null));
    var sink = PictureRecorder();
    canvas = Canvas(sink);
    await paintBackdrop();
    await paintTileGrid();
    await drawSpecific();
    var picture = sink.endRecording();
    var image =
        await picture.toImage(levelNumTilesX * LevelBase.tileSize.toInt(), levelNumTilesY * LevelBase.tileSize.toInt());

    return Sprite(image);
  }
}
