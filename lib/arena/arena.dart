import "dart:ui" as ui;

import 'package:bloo_dot_evolutions/arena/viewport_blit_region.dart';
import 'package:bloo_dot_evolutions/arena/viewport_partitioning_situations.dart';
import 'package:bloo_dot_evolutions/enums/direction.dart';

class Arena {
  final ui.Rect viewportBounds;
  late ui.Offset centerInWorld;
  late ViewportPartitioningSituation _partitioningSituation = ViewportPartitioningSituation.singleIntersection;

  Arena(this.viewportBounds) : centerInWorld = const ui.Offset(0, 0);

  final _quadIntersection = [ViewportBlitRegion(), ViewportBlitRegion(), ViewportBlitRegion(), ViewportBlitRegion()];
  final _doubleIntersectionHorz = [ViewportBlitRegion(), ViewportBlitRegion()];
  final _doubleIntersectionVert = [ViewportBlitRegion(), ViewportBlitRegion()];
  late final _singleIntersection = [ViewportBlitRegion(ix: 1, iy: 1, srcRect: viewportBounds, dstRect: viewportBounds)];
  late List<ViewportBlitRegion> _currentIntersection = _singleIntersection;
  late double flapTresholdX;

  void Function(Direction flapDirection)? onFlap;

  List<ViewportBlitRegion> get viewportBlitRegions => _currentIntersection;

  void moveInWorld(ui.Offset offset) {
    assert(offset.dx < viewportBounds.width);
    assert(offset.dy < viewportBounds.height);
    var previousPartitioningSituation = _partitioningSituation;
    flapTresholdX = viewportBounds.width / 3.0;
    if (offset.dx > 0) {
      _moveRight(offset.dx);
    } else if (offset.dx < 0) {
      _moveLeft(-offset.dx);
    }

    if (offset.dy > 0) {
      _moveDown(offset.dy);
    } else if (offset.dy < 0) {
      _moveUp(-offset.dy);
    }

    if (_partitioningSituation != previousPartitioningSituation) {
      _switchActivePartitioningSituation();
    }
  }

  void _moveRight(double dx) {
    switch (_partitioningSituation) {
      case ViewportPartitioningSituation.singleIntersection:
        _doubleIntersectionHorz[0].ix = 0;
        _doubleIntersectionHorz[0].iy = 1;
        _doubleIntersectionHorz[0].srcRect = ui.Rect.fromLTWH(dx, 0, viewportBounds.width - dx, viewportBounds.height);
        _doubleIntersectionHorz[0].dstRect = ui.Rect.fromLTWH(0, 0, viewportBounds.width - dx, viewportBounds.height);
        _doubleIntersectionHorz[1].ix = 1;
        _doubleIntersectionHorz[1].iy = 1;
        _doubleIntersectionHorz[1].srcRect = ui.Rect.fromLTWH(0, 0, dx, viewportBounds.height);
        _doubleIntersectionHorz[1].dstRect = ui.Rect.fromLTWH(viewportBounds.width - dx, 0, dx, viewportBounds.height);
        _partitioningSituation = ViewportPartitioningSituation.twinIntersectionHorz;
        break;

      case ViewportPartitioningSituation.twinIntersectionHorz:
        _doubleIntersectionHorz[0].srcRect = ui.Rect.fromLTWH(_doubleIntersectionHorz[0].srcRect.left + dx, 0,
            _doubleIntersectionHorz[0].srcRect.width - dx, viewportBounds.height);

        _doubleIntersectionHorz[0].dstRect =
            ui.Rect.fromLTWH(0, 0, _doubleIntersectionHorz[0].dstRect.width - dx, viewportBounds.height);

        _doubleIntersectionHorz[1].srcRect =
            ui.Rect.fromLTWH(0, 0, _doubleIntersectionHorz[1].srcRect.width + dx, viewportBounds.height);

        _doubleIntersectionHorz[1].dstRect = ui.Rect.fromLTWH(_doubleIntersectionHorz[1].dstRect.left - dx, 0,
            _doubleIntersectionHorz[1].dstRect.width + dx, viewportBounds.height);

        if (_doubleIntersectionHorz[1].dstRect.left == 0) {
          _partitioningSituation = ViewportPartitioningSituation.singleIntersection;
          break;
        }

        break;

      case ViewportPartitioningSituation.twinIntersectionVert:
        break;
    }

    centerInWorld = centerInWorld.translate(dx, 0);
  }

  void _moveLeft(double dx) {
    switch (_partitioningSituation) {
      case ViewportPartitioningSituation.singleIntersection:
        _doubleIntersectionHorz[0].ix = 0;
        _doubleIntersectionHorz[0].iy = 1;
        _doubleIntersectionHorz[0].srcRect = ui.Rect.fromLTWH(viewportBounds.width - dx, 0, dx, viewportBounds.height);
        _doubleIntersectionHorz[0].dstRect = ui.Rect.fromLTWH(0, 0, dx, viewportBounds.height);
        _doubleIntersectionHorz[1].ix = 1;
        _doubleIntersectionHorz[1].iy = 1;
        _doubleIntersectionHorz[1].srcRect = ui.Rect.fromLTWH(0, 0, viewportBounds.width - dx, viewportBounds.height);
        _doubleIntersectionHorz[1].dstRect = ui.Rect.fromLTWH(dx, 0, viewportBounds.width - dx, viewportBounds.height);
        _partitioningSituation = ViewportPartitioningSituation.twinIntersectionHorz;
        break;

      case ViewportPartitioningSituation.twinIntersectionHorz:
        _moveLeftSliver(_doubleIntersectionHorz[0], _doubleIntersectionHorz[1], dx);
        break;

      case ViewportPartitioningSituation.twinIntersectionVert:
        break;
    }

    centerInWorld = centerInWorld.translate(-dx, 0);
  }

  void _moveLeftSliver(ViewportBlitRegion leftHalf, ViewportBlitRegion rightHalf, double dx) {
    var leftBefore = leftHalf.srcRect.left;
    leftHalf.srcRect =
        ui.Rect.fromLTWH(leftHalf.srcRect.left - dx, 0, leftHalf.srcRect.width + dx, viewportBounds.height);

    if (leftHalf.srcRect.left == 0) {
      _partitioningSituation = ViewportPartitioningSituation.singleIntersection;
      return;
    } else if (leftBefore > flapTresholdX && leftHalf.srcRect.left <= flapTresholdX) {
      onFlap?.call(Direction.left);
      leftHalf.ix = 1;
      rightHalf.ix = 2;
    }

    if (leftHalf.srcRect.left < 0) {
      leftHalf.ix = 0;
      rightHalf.ix = 1;
      rightHalf.srcRect = ui.Rect.fromLTWH(0, 0, viewportBounds.width + leftHalf.srcRect.left, viewportBounds.height);

      rightHalf.dstRect = ui.Rect.fromLTWH(
          -leftHalf.srcRect.left, 0, viewportBounds.width + leftHalf.srcRect.left, viewportBounds.height);

      leftHalf.dstRect = ui.Rect.fromLTWH(0, 0, -leftHalf.srcRect.left, viewportBounds.height);

      leftHalf.srcRect = ui.Rect.fromLTWH(
          viewportBounds.width + leftHalf.srcRect.left, 0, -leftHalf.srcRect.left, viewportBounds.height);

      return;
    }

    leftHalf.dstRect = ui.Rect.fromLTWH(0, 0, leftHalf.dstRect.width + dx, viewportBounds.height);
    rightHalf.srcRect = ui.Rect.fromLTWH(0, 0, rightHalf.srcRect.width - dx, viewportBounds.height);
    rightHalf.dstRect =
        ui.Rect.fromLTWH(rightHalf.dstRect.left + dx, 0, rightHalf.dstRect.width - dx, viewportBounds.height);
  }

  void _moveDown(double dy) {
    centerInWorld = centerInWorld.translate(0, dy);
  }

  void _moveUp(double dy) {
    centerInWorld = centerInWorld.translate(0, -dy);
  }

  void _switchActivePartitioningSituation() {
    switch (_partitioningSituation) {
      case ViewportPartitioningSituation.singleIntersection:
        _currentIntersection = _singleIntersection;
        break;

      case ViewportPartitioningSituation.twinIntersectionHorz:
        _currentIntersection = _doubleIntersectionHorz;
        break;

      case ViewportPartitioningSituation.twinIntersectionVert:
        _currentIntersection = _doubleIntersectionVert;
        break;

      case ViewportPartitioningSituation.quadIntersection:
        _currentIntersection = _quadIntersection;
        break;
    }
  }
}
