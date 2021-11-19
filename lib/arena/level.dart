import 'dart:ui';
import 'package:flame/components.dart';
import 'package:flutter/material.dart';

abstract class LevelBase extends SpriteComponent with HasGameRef {
  Future<Sprite> createLevel();

  @override
  Future<void>? onLoad() async {
    sprite = await createLevel();
    size = sprite!.originalSize;
    return super.onLoad();
  }
}

class Level extends LevelBase {
  @override
  Future<Sprite> createLevel() async {
    int numTilesX = 200, numTilesY = 77;

    var sink = PictureRecorder();
    var canvas = Canvas(sink);
    canvas.drawRect(
        Rect.fromLTWH(0, 0, numTilesX * 33, numTilesY * 33),
        Paint()..color=Colors.blueGrey);

    canvas.drawRect(
        Rect.fromLTWH((numTilesX / 2) * 33, (numTilesY / 2) * 33, 33, 33),
        Paint()..color=Colors.grey);

    canvas.drawRect(
        const Rect.fromLTWH(2 * 33, 2 * 33, 33, 33),
        Paint()..color=Colors.grey);

    canvas.drawRect(
        const Rect.fromLTWH(3 * 33, 2 * 33, 33, 33),
        Paint()..color=Colors.grey);

    canvas.drawRect(
        const Rect.fromLTWH(20 * 33, 2 * 33, 33, 33),
        Paint()..color=Colors.grey);

    canvas.drawRect(
        const Rect.fromLTWH(50 * 33, 2 * 33, 33, 33),
        Paint()..color=Colors.grey);

    canvas.drawRect(
        const Rect.fromLTWH(3 * 33, 3 * 33, 33, 33),
        Paint()..color=Colors.grey);

    var picture = sink.endRecording();
    var image = await picture.toImage(numTilesX * 33, numTilesY * 33);
    return Sprite(image);
  }
}