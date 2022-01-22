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
  late double flapTresholdX, flapTresholdY;

  void Function(Direction flapDirection)? onFlap;

  List<ViewportBlitRegion> get viewportBlitRegions => _currentIntersection;

  void moveInWorld(ui.Offset offset) {
    assert(offset.dx < viewportBounds.width);
    assert(offset.dy < viewportBounds.height);
    var previousPartitioningSituation = _partitioningSituation;
    flapTresholdX = viewportBounds.width / 3.0;
    flapTresholdY = viewportBounds.height / 3.0;
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

  void _moveRight(double dx) {
    switch (_partitioningSituation) {
      case ViewportPartitioningSituation.singleIntersection:
        _doubleIntersectionHorz[0].ix = 1;
        _doubleIntersectionHorz[0].iy = 1;
        _doubleIntersectionHorz[0].srcRect = ui.Rect.fromLTWH(dx, 0, viewportBounds.width - dx, viewportBounds.height);
        _doubleIntersectionHorz[0].dstRect = ui.Rect.fromLTWH(0, 0, viewportBounds.width - dx, viewportBounds.height);
        _doubleIntersectionHorz[1].ix = 2;
        _doubleIntersectionHorz[1].iy = 1;
        _doubleIntersectionHorz[1].srcRect = ui.Rect.fromLTWH(0, 0, dx, viewportBounds.height);
        _doubleIntersectionHorz[1].dstRect = ui.Rect.fromLTWH(viewportBounds.width - dx, 0, dx, viewportBounds.height);
        _partitioningSituation = ViewportPartitioningSituation.twinIntersectionHorz;
        break;

      case ViewportPartitioningSituation.twinIntersectionHorz:
        _moveRightSliver(_doubleIntersectionHorz[0], _doubleIntersectionHorz[1], dx);
        break;

      case ViewportPartitioningSituation.twinIntersectionVert:
        break;
    }

    centerInWorld = centerInWorld.translate(dx, 0);
  }

  void _moveRightSliver(ViewportBlitRegion leftHalf, ViewportBlitRegion rightHalf, double dx) {
    var rightBefore = rightHalf.srcRect.width;
    rightHalf.srcRect = ui.Rect.fromLTWH(0, 0, rightHalf.srcRect.width + dx, viewportBounds.height);

    /*************************************************************************************
     *(0,y)                      *(1,y)                      *(2,y)               |      *
     *                           *                   |----------------------------|      *
     *                           *                   |VP     *                    |      *
     *                           *                   |       *                 ==>|      *
     *                           *                   |       *                    |      *
     *                           *                   |----------------------------|      *
     *                           *                           *                    |      *
     *************************************************************************************
                                                                                  T      */
    if (rightHalf.srcRect.width == viewportBounds.width) {
      _partitioningSituation = ViewportPartitioningSituation.singleIntersection;
      return;
    } else if (rightBefore < (2 * flapTresholdX) && rightHalf.srcRect.width >= (2 * flapTresholdX)) {
      onFlap?.call(Direction.right);
      leftHalf.ix = 0;
      rightHalf.ix = 1;
    }

    if (rightHalf.srcRect.width > viewportBounds.width) {
      var overShoot = rightHalf.srcRect.width - viewportBounds.width;
      leftHalf.ix = 1;
      rightHalf.ix = 2;
      leftHalf.srcRect = ui.Rect.fromLTWH(overShoot, 0, viewportBounds.width - overShoot, viewportBounds.height);
      leftHalf.dstRect = ui.Rect.fromLTWH(0, 0, viewportBounds.width - overShoot, viewportBounds.height);
      rightHalf.dstRect = ui.Rect.fromLTWH(viewportBounds.width - overShoot, 0, overShoot, viewportBounds.height);
      rightHalf.srcRect = ui.Rect.fromLTWH(0, 0, overShoot, viewportBounds.height);
      return;
    }

    rightHalf.dstRect =
        ui.Rect.fromLTWH(rightHalf.dstRect.left - dx, 0, rightHalf.dstRect.width + dx, viewportBounds.height);
    leftHalf.srcRect =
        ui.Rect.fromLTWH(leftHalf.srcRect.left + dx, 0, leftHalf.srcRect.width - dx, viewportBounds.height);
    leftHalf.dstRect = ui.Rect.fromLTWH(0, 0, leftHalf.dstRect.width - dx, viewportBounds.height);
  }

  void _moveUp(double dy) {
    switch (_partitioningSituation) {
      case ViewportPartitioningSituation.singleIntersection:
        _doubleIntersectionVert[0].ix = 1;
        _doubleIntersectionVert[0].iy = 0;
        _doubleIntersectionVert[0].srcRect = ui.Rect.fromLTWH(0, viewportBounds.height - dy, viewportBounds.width, dy);
        _doubleIntersectionVert[0].dstRect = ui.Rect.fromLTWH(0, 0, viewportBounds.width, dy);
        _doubleIntersectionVert[1].ix = 1;
        _doubleIntersectionVert[1].iy = 1;
        _doubleIntersectionVert[1].srcRect = ui.Rect.fromLTWH(0, 0, viewportBounds.width, viewportBounds.height - dy);
        _doubleIntersectionVert[1].dstRect = ui.Rect.fromLTWH(0, dy, viewportBounds.width, viewportBounds.height - dy);
        _partitioningSituation = ViewportPartitioningSituation.twinIntersectionVert;
        break;

      case ViewportPartitioningSituation.twinIntersectionVert:
        _moveUpSliver(_doubleIntersectionVert[0], _doubleIntersectionVert[1], dy);
        break;

      case ViewportPartitioningSituation.twinIntersectionHorz:
        break;
    }

    centerInWorld = centerInWorld.translate(0, -dy);
  }

  void _moveUpSliver(ViewportBlitRegion upperHalf, ViewportBlitRegion lowerHalf, double dy) {
    var topBefore = upperHalf.srcRect.top;
    upperHalf.srcRect =
        ui.Rect.fromLTWH(0, upperHalf.srcRect.top - dy, viewportBounds.width, upperHalf.srcRect.height + dy);

    if (upperHalf.srcRect.top == 0) {
      _partitioningSituation = ViewportPartitioningSituation.singleIntersection;
      return;
    } else if (topBefore > flapTresholdY && upperHalf.srcRect.top <= flapTresholdY) {
      onFlap?.call(Direction.up);
      upperHalf.iy = 1;
      lowerHalf.iy = 2;
    }

    if (upperHalf.srcRect.top < 0) {
      upperHalf.iy = 0;
      lowerHalf.iy = 1;
      lowerHalf.srcRect = ui.Rect.fromLTWH(0, 0, viewportBounds.width, viewportBounds.height + upperHalf.srcRect.top);
      lowerHalf.dstRect = ui.Rect.fromLTWH(
          0, -upperHalf.srcRect.top, viewportBounds.width, viewportBounds.height + upperHalf.srcRect.top);

      upperHalf.dstRect = ui.Rect.fromLTWH(0, 0, viewportBounds.width, -upperHalf.srcRect.top);
      upperHalf.srcRect = ui.Rect.fromLTWH(
          0, viewportBounds.height + upperHalf.srcRect.top, viewportBounds.width, -upperHalf.srcRect.top);

      return;
    }

    upperHalf.dstRect = ui.Rect.fromLTWH(0, 0, viewportBounds.width, upperHalf.dstRect.height + dy);
    lowerHalf.srcRect = ui.Rect.fromLTWH(0, 0, viewportBounds.width, lowerHalf.srcRect.height - dy);
    lowerHalf.dstRect =
        ui.Rect.fromLTWH(0, lowerHalf.dstRect.top + dy, viewportBounds.width, lowerHalf.dstRect.height - dy);
  }

  void _moveDown(double dy) {
    switch (_partitioningSituation) {
      case ViewportPartitioningSituation.singleIntersection:
        _doubleIntersectionVert[0].ix = 1;
        _doubleIntersectionVert[0].iy = 1;
        _doubleIntersectionVert[0].srcRect = ui.Rect.fromLTWH(0, dy, viewportBounds.width, viewportBounds.height - dy);
        _doubleIntersectionVert[0].dstRect = ui.Rect.fromLTWH(0, 0, viewportBounds.width, viewportBounds.height - dy);
        _doubleIntersectionVert[1].ix = 1;
        _doubleIntersectionVert[1].iy = 2;
        _doubleIntersectionVert[1].srcRect = ui.Rect.fromLTWH(0, 0, viewportBounds.width, dy);
        _doubleIntersectionVert[1].dstRect = ui.Rect.fromLTWH(0, viewportBounds.height - dy, viewportBounds.width, dy);
        _partitioningSituation = ViewportPartitioningSituation.twinIntersectionVert;
        break;

      case ViewportPartitioningSituation.twinIntersectionVert:
        _moveDownSliver(_doubleIntersectionVert[0], _doubleIntersectionVert[1], dy);
        break;

      case ViewportPartitioningSituation.twinIntersectionHorz:
        break;
    }

    centerInWorld = centerInWorld.translate(0, dy);
  }

  void _moveDownSliver(ViewportBlitRegion upperHalf, ViewportBlitRegion lowerHalf, double dy) {
    var bottomBefore = lowerHalf.srcRect.height;
    lowerHalf.srcRect = ui.Rect.fromLTWH(0, 0, viewportBounds.width, lowerHalf.srcRect.height + dy);
    if (lowerHalf.srcRect.height == viewportBounds.height) {
      _partitioningSituation = ViewportPartitioningSituation.singleIntersection;
      return;
    } else if (bottomBefore < (2 * flapTresholdY) && lowerHalf.srcRect.height >= (2 * flapTresholdY)) {
      onFlap?.call(Direction.down);
      upperHalf.iy = 0;
      lowerHalf.iy = 1;
    }

    if (lowerHalf.srcRect.height > viewportBounds.height) {
      var overShoot = lowerHalf.srcRect.height - viewportBounds.height;
      upperHalf.iy = 1;
      lowerHalf.iy = 2;
      upperHalf.srcRect = ui.Rect.fromLTWH(0, overShoot, viewportBounds.width, viewportBounds.height - overShoot);
      upperHalf.dstRect = ui.Rect.fromLTWH(0, 0, viewportBounds.width, viewportBounds.height - overShoot);
      lowerHalf.dstRect = ui.Rect.fromLTWH(0, viewportBounds.height - overShoot, viewportBounds.width, overShoot);
      lowerHalf.srcRect = ui.Rect.fromLTWH(0, 0, viewportBounds.width, overShoot);
      return;
    }

    lowerHalf.dstRect =
        ui.Rect.fromLTWH(0, lowerHalf.dstRect.top - dy, viewportBounds.width, lowerHalf.dstRect.height + dy);
    upperHalf.srcRect =
        ui.Rect.fromLTWH(0, upperHalf.srcRect.top + dy, viewportBounds.width, upperHalf.srcRect.height - dy);
    upperHalf.dstRect = ui.Rect.fromLTWH(0, 0, viewportBounds.width, upperHalf.dstRect.height - dy);
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
