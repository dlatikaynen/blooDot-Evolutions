import "dart:ui" as ui;

import 'package:bloo_dot_evolutions/arena/level_base.dart';
import 'package:bloo_dot_evolutions/enums/direction.dart';

class ViewportSliver {
  final int originalIx;
  final int originalIy;
  late ui.Image rooofImage;
  late ui.Image floorImage;

  late ui.Offset topLeftInWorld;
  late int topLeftTileX;
  late int topLeftTileY;

  ViewportSliver(this.originalIx, this.originalIy);

  ViewportSliver initializePositionInWorld(ui.Size viewportSize,
      {ViewportSliver? neighboringSliver, Direction neighborInDirection = Direction.none}) {
    if (neighboringSliver == null) {
      var originDuMondeX = viewportSize.width / 2.0;
      var originDuMondeY = viewportSize.height / 2.0;
      topLeftTileX = -(originDuMondeX / LevelBase.tileSize).ceil();
      topLeftTileY = -(originDuMondeY / LevelBase.tileSize).ceil();
      topLeftInWorld = ui.Offset(
          originDuMondeX + topLeftTileX * LevelBase.tileSize, originDuMondeY + topLeftTileY * LevelBase.tileSize);
    } else {
      var originDuMondeX = viewportSize.width / 2.0;
      var originDuMondeY = viewportSize.height / 2.0;
      topLeftTileX = -(originDuMondeX / LevelBase.tileSize).ceil();
      topLeftTileY = -(originDuMondeY / LevelBase.tileSize).ceil();
      topLeftInWorld = ui.Offset(
          originDuMondeX + topLeftTileX * LevelBase.tileSize, originDuMondeY + topLeftTileY * LevelBase.tileSize);
    }

    return this;
  }
}
