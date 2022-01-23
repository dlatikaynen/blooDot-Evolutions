import 'dart:ui';

import 'package:bloo_dot_evolutions/level/tilesets/tile_painter_base.dart';

abstract class BlobTilePainter extends TilePainterBase {
  paintBlobTile(Canvas paintTo, int primeIndex);
}
