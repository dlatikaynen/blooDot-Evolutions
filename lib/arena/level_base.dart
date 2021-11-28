import 'dart:ui';

import 'package:bloo_dot_evolutions/algo/blob_tile_painter_base.dart';
import 'package:bloo_dot_evolutions/algo/blob_tileset_painter.dart';
import 'package:flame/components.dart';
import 'package:flutter/material.dart';

abstract class LevelBase extends SpriteComponent with HasGameRef {
  static const tileSize = 33.0;
  BlobTilePainterBase? _selectedTileSet;

  Future<Sprite> createLevel();

  @override
  Future<void>? onLoad() async {
    sprite = await createLevel();
    size = sprite!.originalSize;
    return super.onLoad();
  }

  void selectTileSet(BlobTilePainterBase tileSet) => _selectedTileSet = tileSet;

  void placeTile(Canvas paintTo, int gridX, int gridY, int tileIndex) {
    if (_selectedTileSet == null) {
      return;
    }

    paintTo.save();
    paintTo.translate(gridX * tileSize, gridY * tileSize);
    BlobTileSetPainter(_selectedTileSet!, paintTo).paintTile(tileIndex);
    paintTo.restore();
  }
}
