import 'dart:ui';

import 'package:bloo_dot_evolutions/algo/blob_tile_painter_base.dart';

class ArenaObject {
  late int anchorTileX;
  late int anchorTileY;
  late Offset currentPosition;
  late Offset boundingBox;
  BlobTilePainterBase? fromTileSet;
  late int indexInTileSet = -1;
}
