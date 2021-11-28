import 'dart:ui';

import 'package:bloo_dot_evolutions/algo/blob_tile_painter_base.dart';
import 'package:bloo_dot_evolutions/algo/monomino_lookup.dart';
import 'package:flutter/material.dart';

class GreyWallTileSet extends BlobTilePainterBase {
  final Paint basePaint = Paint()
    ..color = Colors.white12
    ..style = PaintingStyle.fill;

  final Paint decoPaint = Paint()
    ..color = Colors.white54
    ..strokeWidth = 4.0;

  @override
  paintBaseTile(Canvas paintTo, int primeIndex) {
    paintTo.drawRect(const Rect.fromLTWH(0, 0, 31, 31), basePaint);
    switch (primeIndex) {
      case MonominoLookup.primeIndexShy:
      case MonominoLookup.primeIndexU:
      case MonominoLookup.primeIndexKnee:
      case MonominoLookup.primeIndexL:
      case MonominoLookup.primeIndexPipe:
      case MonominoLookup.primeIndexT:
      case MonominoLookup.primeIndexWSE:
      case MonominoLookup.primeIndexWNE:
      case MonominoLookup.primeIndexW:
      case MonominoLookup.primeIndexCross:
      case MonominoLookup.primeIndexInvThreeNE:
      case MonominoLookup.primeIndexTwoNWSW:
      case MonominoLookup.primeIndexTwoNWSE:
      case MonominoLookup.primeIndexThreeNW:
      case MonominoLookup.primeIndexImmersed:
        /* nothing to do - this is identical to the blank */
        break;
      default:
        throw UnsupportedError("${(GreyWallTileSet)} cannot draw $primeIndex");
    }
  }
}
