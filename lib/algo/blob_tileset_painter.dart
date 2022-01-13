import 'dart:math';
import 'dart:ui';

import 'package:bloo_dot_evolutions/algo/blob_tile_painter_base.dart';
import 'package:bloo_dot_evolutions/arena/level_base.dart';

import 'monomino_index.dart';

class TileSetPainter {
  final BlobTilePainter _basePainter;
  final Canvas _paintTo;
  static const _clipRect = Rect.fromLTWH(0, 0, LevelBase.tileSize, LevelBase.tileSize);

  TileSetPainter(this._basePainter, this._paintTo);

  void paintBlobTile(int tileIndex) {
    var paintInstructions = MonominoIndex.primeTileFrom(tileIndex);
    _paintTo.save();
    _paintTo.clipRect(_clipRect);
    _rotate(paintInstructions.numRotations);
    _basePainter.paintBlobTile(_paintTo, paintInstructions.primeIndex);
    _paintTo.restore();
  }

  void paintStaticTile(int indexOnSpriteSheetX, int indexOnSpriteSheetY,
      {int numRotations = 0, int strideX = 1, int strideY = 1}) {
    _paintTo.save();
    _paintTo.clipRect(_clipRect);
    _rotate(numRotations);
    _basePainter.paintStaticTile(_paintTo, indexOnSpriteSheetX, indexOnSpriteSheetY);
    _paintTo.restore();
  }

  void _rotate(int numRotations) {
    if (numRotations > 0) {
      _paintTo.translate(LevelBase.tileSize / 2.0, LevelBase.tileSize / 2.0);
      _paintTo.rotate(pi / 2.0 * numRotations);
      _paintTo.translate(-LevelBase.tileSize / 2.0, -LevelBase.tileSize / 2.0);
    }
  }
}
