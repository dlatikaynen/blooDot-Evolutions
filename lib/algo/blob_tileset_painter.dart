import 'dart:math';
import 'dart:ui';

import 'package:bloo_dot_evolutions/algo/blob_tile_painter_base.dart';
import 'package:bloo_dot_evolutions/arena/level_base.dart';

import 'monomino_index.dart';

class BlobTileSetPainter {
  final BlobTilePainterBase _basePainter;
  final Canvas _paintTo;
  static const _clipRect = Rect.fromLTWH(0, 0, LevelBase.tileSize, LevelBase.tileSize);

  BlobTileSetPainter(this._basePainter, this._paintTo);

  void paintTile(int tileIndex) {
    var paintInstructions = MonominoIndex.primeTileFrom(tileIndex);
    _paintTo.save();
    _paintTo.clipRect(_clipRect);
    if (paintInstructions.numRotations > 0) {
      _paintTo.translate(LevelBase.tileSize / 2.0, LevelBase.tileSize / 2.0);
      _paintTo.rotate(pi / 2.0 * paintInstructions.numRotations);
      _paintTo.translate(-LevelBase.tileSize / 2.0, -LevelBase.tileSize / 2.0);
    }

    _basePainter.paintBaseTile(_paintTo, paintInstructions.primeIndex);
    _paintTo.restore();
  }
}
