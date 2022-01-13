import 'package:bloo_dot_evolutions/arena/level_base.dart';
import 'package:flame/extensions.dart';
import 'package:flame/flame.dart';
import 'package:flutter/painting.dart';

abstract class TilePainterBase {
  late Image _spriteSheet;

  String get spriteSheetName => "";

  Future load() async {
    if (spriteSheetName.isNotEmpty) {
      _spriteSheet = await Flame.images.load(spriteSheetName);
    }
  }

  void paintStaticTile(Canvas paintTo, indexOnSpriteSheetX, indexOnSpriteSheetY);
}

class TilePainter extends TilePainterBase {
  @override
  void paintStaticTile(Canvas paintTo, indexOnSpriteSheetX, indexOnSpriteSheetY) {
    paintTo.drawImageRect(
        _spriteSheet,
        Rect.fromLTWH(1 + (LevelBase.tileSize + 1) * indexOnSpriteSheetX,
            1 + (LevelBase.tileSize + 1) * indexOnSpriteSheetY, LevelBase.tileSize, LevelBase.tileSize),
        const Rect.fromLTWH(0, 0, LevelBase.tileSize, LevelBase.tileSize),
        Paint());
  }
}
