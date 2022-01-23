import 'package:bloo_dot_evolutions/algo/coords2.dart';
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

  IntSize2 tilePartitionSizeOnSheet(int tileNumber) => const IntSize2(1, 1);
  IntCoords2 tileNumberToBaseSheetPosition(int tileNumber);

  IntCoords2 tileNumberToSheetCoordinates(int tileNumber, {int strideX = 0, int strideY = 0}) {
    var tilePartitionSize = tilePartitionSizeOnSheet(tileNumber);
    var basePosOnSheet = tileNumberToBaseSheetPosition(tileNumber);
    var absPosOnSheet = IntCoords2(basePosOnSheet.x, basePosOnSheet.y);
    if (tilePartitionSize.x > 1) {
      absPosOnSheet = IntCoords2(absPosOnSheet.x + (strideX % tilePartitionSize.x), absPosOnSheet.y);
    }

    if (tilePartitionSize.y > 1) {
      absPosOnSheet = IntCoords2(absPosOnSheet.x, absPosOnSheet.y + (strideY % tilePartitionSize.y));
    }

    return absPosOnSheet;
  }

  void paintStaticTile(Canvas paintTo, indexOnSpriteSheetX, indexOnSpriteSheetY) {
    paintTo.drawImageRect(
        _spriteSheet,
        Rect.fromLTWH(1 + (LevelBase.tileSize + 1) * indexOnSpriteSheetX,
            1 + (LevelBase.tileSize + 1) * indexOnSpriteSheetY, LevelBase.tileSize, LevelBase.tileSize),
        const Rect.fromLTWH(0, 0, LevelBase.tileSize, LevelBase.tileSize),
        Paint());
  }
}
