import 'dart:ui';

import 'package:bloo_dot_evolutions/algo/blob_tile_painter_base.dart';
import 'package:bloo_dot_evolutions/algo/monomino_lookup.dart';
import 'package:bloo_dot_evolutions/arena/level_base.dart';
import 'package:flutter/material.dart';

class GreyWallTileSet extends BlobTilePainterBase {
  /* https://www.qb64.org/wiki/COLOR */
  final Paint basePaint = Paint()
    ..color = const Color.fromARGB(0xff, 0x54, 0x54, 0x54)
    ..style = PaintingStyle.fill;

  final Paint decoPaint = Paint()
    ..color = const Color.fromARGB(0xff, 0xa8, 0xa8, 0xa8)
    ..style = PaintingStyle.stroke
    ..strokeWidth = 2.43;

  static const cornerNWx = LevelBase.tileSize / 7;
  static const cornerNWy = cornerNWx;
  static const cornerNEx = LevelBase.tileSize - LevelBase.tileSize / 7;
  static const cornerNEy = cornerNWx;
  static const cornerSWx = cornerNWx;
  static const cornerSWy = cornerNEx;
  static const cornerSEx = cornerNEx;
  static const cornerSEy = cornerSWy;

  @override
  paintBaseTile(Canvas paintTo, int primeIndex) {
    paintTo.drawRect(const Rect.fromLTWH(0, 0, LevelBase.tileSize, LevelBase.tileSize), basePaint);
    paintTo.drawRect(
        const Rect.fromLTWH(0, 0, LevelBase.tileSize, LevelBase.tileSize),
        Paint()
          ..style = PaintingStyle.stroke
          ..color = Colors.lightGreenAccent);

    switch (primeIndex) {
      case MonominoLookup.primeIndexShy:
        paintTo.drawRect(const Rect.fromLTRB(cornerNWx, cornerNWy, cornerSEx, cornerSEy), decoPaint);
        break;

      case MonominoLookup.primeIndexU:
        paintTo.drawRect(const Rect.fromLTRB(cornerNWx, -cornerNWy, cornerSEx, cornerSEy), decoPaint);
        break;

      case MonominoLookup.primeIndexKnee:
        paintTo.drawLine(const Offset(cornerNWx, -cornerNWy), const Offset(cornerSWx, cornerSWy), decoPaint);
        paintTo.drawLine(const Offset(cornerSWx, cornerSWy), const Offset(cornerSEx * 2, cornerSEy), decoPaint);
        paintTo.drawLine(const Offset(cornerNEx, -cornerNEy), const Offset(cornerNEx, cornerNEy), decoPaint);
        paintTo.drawLine(const Offset(cornerNEx, cornerNEy), const Offset(cornerNEx * 2, cornerNEy), decoPaint);
        break;

      case MonominoLookup.primeIndexL:
        paintTo.drawLine(const Offset(cornerNWx, -cornerNWy), const Offset(cornerSWx, cornerSWy), decoPaint);
        paintTo.drawLine(const Offset(cornerSWx, cornerSWy), const Offset(cornerSEx * 2, cornerSEy), decoPaint);
        break;

      case MonominoLookup.primeIndexPipe:
        paintTo.drawLine(const Offset(cornerNWx, -cornerNWy), const Offset(cornerSWx, cornerSWy * 2), decoPaint);
        paintTo.drawLine(const Offset(cornerNEx, -cornerNEy), const Offset(cornerSEx, cornerSEy * 2), decoPaint);
        break;

      case MonominoLookup.primeIndexT:
        paintTo.drawLine(const Offset(cornerNWx, -cornerNWy), const Offset(cornerSWx, cornerSWy * 2), decoPaint);
        paintTo.drawLine(const Offset(cornerNEx, -cornerNEy), const Offset(cornerNEx, cornerNEy), decoPaint);
        paintTo.drawLine(const Offset(cornerNEx, cornerNEy), const Offset(cornerNEx * 2, cornerNEy), decoPaint);
        paintTo.drawLine(const Offset(cornerSEx, cornerSEy * 2), const Offset(cornerSEx, cornerSEy), decoPaint);
        paintTo.drawLine(const Offset(cornerSEx, cornerSEy), const Offset(cornerSEx * 2, cornerSEy), decoPaint);
        break;

      case MonominoLookup.primeIndexWSE:
        paintTo.drawLine(const Offset(cornerNWx, -cornerNWy), const Offset(cornerSWx, cornerSWy * 2), decoPaint);
        paintTo.drawLine(const Offset(cornerSEx, cornerSEy * 2), const Offset(cornerSEx, cornerSEy), decoPaint);
        paintTo.drawLine(const Offset(cornerSEx, cornerSEy), const Offset(cornerSEx * 2, cornerSEy), decoPaint);
        break;

      case MonominoLookup.primeIndexWNE:
        paintTo.drawLine(const Offset(cornerNWx, -cornerNWy), const Offset(cornerSWx, cornerSWy * 2), decoPaint);
        paintTo.drawLine(const Offset(cornerNEx, -cornerNEy), const Offset(cornerNEx, cornerNEy), decoPaint);
        paintTo.drawLine(const Offset(cornerNEx, cornerNEy), const Offset(cornerNEx * 2, cornerNEy), decoPaint);
        break;

      case MonominoLookup.primeIndexW:
        paintTo.drawLine(const Offset(cornerNWx, -cornerNWy), const Offset(cornerSWx, cornerSWy * 2), decoPaint);
        break;

      case MonominoLookup.primeIndexCross:
        paintTo.drawLine(const Offset(-cornerNWx, cornerNWy), const Offset(cornerNWx, cornerNWy), decoPaint);
        paintTo.drawLine(const Offset(cornerNWx, cornerNWy), const Offset(cornerNWx, -cornerNWy), decoPaint);
        paintTo.drawLine(const Offset(-cornerSWx, cornerSWy), const Offset(cornerSWx, cornerSWy), decoPaint);
        paintTo.drawLine(const Offset(cornerSWx, cornerSWy), const Offset(cornerSWx, cornerSWy * 2), decoPaint);
        paintTo.drawLine(const Offset(cornerNEx, -cornerNEy), const Offset(cornerNEx, cornerNEy), decoPaint);
        paintTo.drawLine(const Offset(cornerNEx, cornerNEy), const Offset(cornerNEx * 2, cornerNEy), decoPaint);
        paintTo.drawLine(const Offset(cornerSEx, cornerSEy * 2), const Offset(cornerSEx, cornerSEy), decoPaint);
        paintTo.drawLine(const Offset(cornerSEx, cornerSEy), const Offset(cornerSEx * 2, cornerSEy), decoPaint);
        break;

      case MonominoLookup.primeIndexInvThreeNE:
        paintTo.drawLine(const Offset(-cornerNWx, cornerNWy), const Offset(cornerNWx, cornerNWy), decoPaint);
        paintTo.drawLine(const Offset(cornerNWx, cornerNWy), const Offset(cornerNWx, -cornerNWy), decoPaint);
        paintTo.drawLine(const Offset(-cornerSWx, cornerSWy), const Offset(cornerSWx, cornerSWy), decoPaint);
        paintTo.drawLine(const Offset(cornerSWx, cornerSWy), const Offset(cornerSWx, cornerSWy * 2), decoPaint);
        paintTo.drawLine(const Offset(cornerSEx, cornerSEy * 2), const Offset(cornerSEx, cornerSEy), decoPaint);
        paintTo.drawLine(const Offset(cornerSEx, cornerSEy), const Offset(cornerSEx * 2, cornerSEy), decoPaint);
        break;

      case MonominoLookup.primeIndexTwoNWSW:
        paintTo.drawLine(const Offset(-cornerNWx, cornerNWy), const Offset(cornerNWx, cornerNWy), decoPaint);
        paintTo.drawLine(const Offset(cornerNWx, cornerNWy), const Offset(cornerNWx, -cornerNWy), decoPaint);
        paintTo.drawLine(const Offset(-cornerSWx, cornerSWy), const Offset(cornerSWx, cornerSWy), decoPaint);
        paintTo.drawLine(const Offset(cornerSWx, cornerSWy), const Offset(cornerSWx, cornerSWy * 2), decoPaint);
        break;

      case MonominoLookup.primeIndexTwoNWSE:
        paintTo.drawLine(const Offset(-cornerNWx, cornerNWy), const Offset(cornerNWx, cornerNWy), decoPaint);
        paintTo.drawLine(const Offset(cornerNWx, cornerNWy), const Offset(cornerNWx, -cornerNWy), decoPaint);
        paintTo.drawLine(const Offset(cornerSEx, cornerSEy * 2), const Offset(cornerSEx, cornerSEy), decoPaint);
        paintTo.drawLine(const Offset(cornerSEx, cornerSEy), const Offset(cornerSEx * 2, cornerSEy), decoPaint);
        break;

      case MonominoLookup.primeIndexThreeNW:
        paintTo.drawLine(const Offset(-cornerNWx, cornerNWy), const Offset(cornerNWx, cornerNWy), decoPaint);
        paintTo.drawLine(const Offset(cornerNWx, cornerNWy), const Offset(cornerNWx, -cornerNWy), decoPaint);
        break;

      case MonominoLookup.primeIndexImmersed:
        /* nothing to do - this is identical to the blank */
        break;

      default:
        throw UnsupportedError("${(GreyWallTileSet)} cannot draw $primeIndex");
    }
  }
}
