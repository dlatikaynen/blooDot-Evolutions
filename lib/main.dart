import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:bloo_dot_evolutions/load_screen.dart';
import 'package:flame/flame.dart';
import 'package:flame/game.dart';
import 'package:flame_audio/flame_audio.dart';
import 'package:flutter/material.dart';

import 'arena/arena.dart';
import 'arena/follower.dart';
import 'arena/level_base.dart';
import 'arena/viewport_sliver.dart';
import 'enums/direction.dart';
import 'extensions/list_swap.dart';
import 'level/level_001.dart';

class BlooDotEvolutionsGame extends FlameGame {
  LevelBase? _level;
  late Follower _follower;

  @override
  Future<void> onLoad() async {
    await super.onLoad();
    _level ??= Level001(this);
    if (!_level!.isLoaded) {
      _follower = Follower();
    }

    await add(_level!);
    await add(_follower);

    _follower.position = _follower.absoluteToLocal(_level!.size / 2 - _follower.size / 2);
    camera.followComponent(_follower, worldBounds: Rect.fromLTRB(0, 0, _level!.size.x, _level!.size.y));
  }

  @override
  void update(double dt) {
    _follower.position.x += 12 * dt;
    super.update(dt);
  }
}

class BlooDotEvolutionsWidget extends StatelessWidget {
  const BlooDotEvolutionsWidget({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return GameWidget(game: BlooDotEvolutionsGame());
  }
}

void main2() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Flame.device.setLandscapeLeftOnly();
  await Flame.device.fullScreen();
  runApp(const LoadScreenWidget());
}

late ui.Rect paintBounds = ui.Offset.zero & (ui.window.physicalSize / ui.window.devicePixelRatio);
late double devicePixelRatio = ui.window.devicePixelRatio;
late double t = 0;
late double deltaT = 0;
late double frameAverage = 0;
final viewportSlivers = List<ViewportSliver>.empty(growable: true);
late int frameNumber = 0;
late double roofX = 0.0;
late Arena arena;
late bool movingLeftUp = true;
late int rolledOver = 0;

void onMetricsChanged() {
  devicePixelRatio = ui.window.devicePixelRatio;
  paintBounds = ui.Offset.zero & (ui.window.physicalSize / devicePixelRatio);
  viewportSlivers.clear();
}

void onFlap(Direction flapDirection) {
  switch (flapDirection) {
    case Direction.left:
      _rolloverLeft();
      break;

    case Direction.right:
      _rolloverRight();
      break;

    case Direction.up:
      _rolloverUp();
      break;

    case Direction.down:
      _rolloverDown();
      break;
  }
}

void beginFrame(Duration timeStamp) async {
  if (viewportSlivers.isEmpty) {
    arena = Arena(paintBounds);
    arena.onFlap = onFlap;
    for (var y = 0; y < 3; ++y) {
      for (var x = 0; x < 3; ++x) {
        viewportSlivers.add(ViewportSliver(x, y)
          ..floorImage = await prepareFloorImage(x, y, paintBounds)
          ..rooofImage = await prepareRooofImage(x, y, paintBounds));
      }
    }
  }

  final ui.PictureRecorder recorder = ui.PictureRecorder();
  final ui.Canvas canvas = ui.Canvas(recorder, paintBounds);
  //canvas.translate(paintBounds.width / 2.0, paintBounds.height / 2.0);

  final double curT = timeStamp.inMicroseconds / Duration.microsecondsPerMillisecond / 1800.0;
  deltaT = t == 0.0 ? 0.0 : curT - t;
  t = curT;
  frameAverage += deltaT;
  frameAverage /= 2.0;
  final fps = 1.0 / frameAverage;

  if (++frameNumber % 1000 == 0) {
    FlameAudio.play('opening_sample.ogg');
  }

  // Here we determine the rotation according to the timeStamp given to us by
  // the engine.
  /*
  canvas.save();
  canvas.rotate(math.pi * (t % 1.0));
  canvas.drawRect(const ui.Rect.fromLTRB(-200.0, -200.0, 200.0, 200.0),
      ui.Paint()..color = ui.Color.fromARGB(255, t.round() % 250, 165, 150));

  canvas.rotate(math.pi * (t % 1.0));
  canvas.drawRect(const ui.Rect.fromLTRB(-200.0, -200.0, 200.0, 200.0),
      ui.Paint()..color = ui.Color.fromARGB(255, 17, t.round() % 255, 250));

  canvas.restore();
  */

  var blitters = arena.viewportBlitRegions;
  for (var blitter in blitters) {
    var srcIndex = blitter.iy * 3 + blitter.ix;
    canvas.drawImageRect(viewportSlivers[srcIndex].floorImage, blitter.srcRect, blitter.dstRect, Paint());
    canvas.drawImageRect(viewportSlivers[srcIndex].rooofImage, blitter.srcRect, blitter.dstRect, Paint());
  }

  if (movingLeftUp) {
    arena.moveInWorld(const Offset(0, -3));
  } else {
    arena.moveInWorld(const Offset(0, 3));
  }

  /* DEBUG DRAWERY */
  canvas.translate(paintBounds.width / 2.0, paintBounds.height / 2.0);
  TextSpan span = TextSpan(style: const TextStyle(color: Colors.white), text: "${fps.toStringAsFixed(0)} fps");
  TextPainter tp = TextPainter(text: span, textAlign: TextAlign.left, textDirection: TextDirection.ltr);
  tp.layout();
  tp.paint(canvas, const Offset(-5.0, -80.0));

  var debugStroke = Paint()
    ..style = PaintingStyle.stroke
    ..strokeWidth = 1
    ..color = Colors.white30;

  canvas.drawRect(const Rect.fromLTWH(-135, -60, 270, 150), debugStroke);
  canvas.drawLine(const Offset(-135, -10), const Offset(135, -10), debugStroke);
  canvas.drawLine(const Offset(-135, 40), const Offset(135, 40), debugStroke);
  canvas.drawLine(const Offset(-45, -60), const Offset(-45, 90), debugStroke);
  canvas.drawLine(const Offset(45, -60), const Offset(45, 90), debugStroke);

  // COMPOSITE
  final ui.Picture picture = recorder.endRecording();
  final Float64List deviceTransform = Float64List(16)
    ..[0] = devicePixelRatio
    ..[5] = devicePixelRatio
    ..[10] = 1.0
    ..[15] = 1.0;

  final ui.SceneBuilder sceneBuilder = ui.SceneBuilder()
    ..pushTransform(deviceTransform)
    ..addPicture(ui.Offset.zero, picture)
    ..pop();
  ui.window.render(sceneBuilder.build());
  ui.window.scheduleFrame();
}

void _rolloverLeft() {
  /*    0  1  2
   *    3  4  5
   *    6  7  8
   *
   *    0  2  1
   *    3  5  4
   *    6  8  7
   *
   *    2  0  1
   *    5  3  4
   *    8  6  7
   */
  viewportSlivers.swap(1, 2);
  viewportSlivers.swap(4, 5);
  viewportSlivers.swap(7, 8);
  viewportSlivers.swap(0, 1);
  viewportSlivers.swap(3, 4);
  viewportSlivers.swap(6, 7);

  print('flap! rolloverLeft');
  ++rolledOver;

  /* don't await these... run in "background" */
  var topLeftSliver = viewportSlivers[0 * 3 + 0];
  prepareFloorImage(topLeftSliver.originalIx, topLeftSliver.originalIy, paintBounds)
      .then((image) => topLeftSliver.floorImage = image);
  prepareRooofImage(topLeftSliver.originalIx, topLeftSliver.originalIy, paintBounds)
      .then((image) => topLeftSliver.rooofImage = image);

  var centerLeftSliver = viewportSlivers[1 * 3 + 0];
  prepareFloorImage(centerLeftSliver.originalIx, centerLeftSliver.originalIy, paintBounds)
      .then((image) => centerLeftSliver.floorImage = image);
  prepareRooofImage(centerLeftSliver.originalIx, centerLeftSliver.originalIy, paintBounds)
      .then((image) => centerLeftSliver.rooofImage = image);

  var bottomLeftSliver = viewportSlivers[2 * 3 + 0];
  prepareFloorImage(bottomLeftSliver.originalIx, bottomLeftSliver.originalIy, paintBounds)
      .then((image) => bottomLeftSliver.floorImage = image);
  prepareRooofImage(bottomLeftSliver.originalIx, bottomLeftSliver.originalIy, paintBounds)
      .then((image) => bottomLeftSliver.rooofImage = image);
}

void _rolloverRight() {
  /*    0  1  2
   *    3  4  5
   *    6  7  8
   *
   *    1  0  2
   *    4  3  5
   *    7  6  8
   *
   *    1  2  0
   *    4  5  3
   *    7  8  6
   */
  viewportSlivers.swap(0, 1);
  viewportSlivers.swap(3, 4);
  viewportSlivers.swap(6, 7);
  viewportSlivers.swap(1, 2);
  viewportSlivers.swap(4, 5);
  viewportSlivers.swap(7, 8);

  print('flap! rolloverRight');
  ++rolledOver;
  /* don't await these... run in "background" */
  var topRightSliver = viewportSlivers[0 * 3 + 2];
  prepareFloorImage(topRightSliver.originalIx, topRightSliver.originalIy, paintBounds)
      .then((image) => topRightSliver.floorImage = image);
  prepareRooofImage(topRightSliver.originalIx, topRightSliver.originalIy, paintBounds)
      .then((image) => topRightSliver.rooofImage = image);

  var centerRightSliver = viewportSlivers[1 * 3 + 2];
  prepareFloorImage(centerRightSliver.originalIx, centerRightSliver.originalIy, paintBounds)
      .then((image) => centerRightSliver.floorImage = image);
  prepareRooofImage(centerRightSliver.originalIx, centerRightSliver.originalIy, paintBounds)
      .then((image) => centerRightSliver.rooofImage = image);

  var bottomRightSliver = viewportSlivers[2 * 3 + 2];
  prepareFloorImage(bottomRightSliver.originalIx, bottomRightSliver.originalIy, paintBounds)
      .then((image) => bottomRightSliver.floorImage = image);
  prepareRooofImage(bottomRightSliver.originalIx, bottomRightSliver.originalIy, paintBounds)
      .then((image) => bottomRightSliver.rooofImage = image);
}

void _rolloverUp() {
  /*    0  1  2
   *    3  4  5
   *    6  7  8
   *
   *    0  1  2
   *    6  7  8
   *    3  4  5
   *
   *    6  7  8
   *    0  1  2
   *    3  4  5
   */
  viewportSlivers.swap(3, 6);
  viewportSlivers.swap(4, 7);
  viewportSlivers.swap(5, 8);
  viewportSlivers.swap(0, 3);
  viewportSlivers.swap(1, 4);
  viewportSlivers.swap(2, 5);

  print('flap! rolloverUp');
  ++rolledOver;

  /* don't await these... run in "background" */
  var leftTopSliver = viewportSlivers[0 * 3 + 0];
  prepareFloorImage(leftTopSliver.originalIx, leftTopSliver.originalIy, paintBounds)
      .then((image) => leftTopSliver.floorImage = image);
  prepareRooofImage(leftTopSliver.originalIx, leftTopSliver.originalIy, paintBounds)
      .then((image) => leftTopSliver.rooofImage = image);

  var centerTopSliver = viewportSlivers[0 * 3 + 1];
  prepareFloorImage(centerTopSliver.originalIx, centerTopSliver.originalIy, paintBounds)
      .then((image) => centerTopSliver.floorImage = image);
  prepareRooofImage(centerTopSliver.originalIx, centerTopSliver.originalIy, paintBounds)
      .then((image) => centerTopSliver.rooofImage = image);

  var rightTopSliver = viewportSlivers[0 * 3 + 2];
  prepareFloorImage(rightTopSliver.originalIx, rightTopSliver.originalIy, paintBounds)
      .then((image) => rightTopSliver.floorImage = image);
  prepareRooofImage(rightTopSliver.originalIx, rightTopSliver.originalIy, paintBounds)
      .then((image) => rightTopSliver.rooofImage = image);
}

void _rolloverDown() {
  /*    0  1  2
   *    3  4  5
   *    6  7  8
   *
   *    3  4  5
   *    0  1  2
   *    6  7  8
   *
   *    3  4  5
   *    6  7  8
   *    0  1  2
   */
  viewportSlivers.swap(0, 3);
  viewportSlivers.swap(1, 4);
  viewportSlivers.swap(2, 5);
  viewportSlivers.swap(3, 6);
  viewportSlivers.swap(4, 7);
  viewportSlivers.swap(5, 8);

  print('flap! rolloverDown');
  ++rolledOver;

  /* don't await these... run in "background" */
  var bottomLeftSliver = viewportSlivers[2 * 3 + 0];
  prepareFloorImage(bottomLeftSliver.originalIx, bottomLeftSliver.originalIy, paintBounds)
      .then((image) => bottomLeftSliver.floorImage = image);
  prepareRooofImage(bottomLeftSliver.originalIx, bottomLeftSliver.originalIy, paintBounds)
      .then((image) => bottomLeftSliver.rooofImage = image);

  var bottomCenterSliver = viewportSlivers[2 * 3 + 1];
  prepareFloorImage(bottomCenterSliver.originalIx, bottomCenterSliver.originalIy, paintBounds)
      .then((image) => bottomCenterSliver.floorImage = image);
  prepareRooofImage(bottomCenterSliver.originalIx, bottomCenterSliver.originalIy, paintBounds)
      .then((image) => bottomCenterSliver.rooofImage = image);

  var bottomRightSliver = viewportSlivers[2 * 3 + 2];
  prepareFloorImage(bottomRightSliver.originalIx, bottomRightSliver.originalIy, paintBounds)
      .then((image) => bottomRightSliver.floorImage = image);
  prepareRooofImage(bottomRightSliver.originalIx, bottomRightSliver.originalIy, paintBounds)
      .then((image) => bottomRightSliver.rooofImage = image);
}

Future<ui.Image> prepareFloorImage(int sliverX, int sliverY, ui.Rect bounds) async {
  final ui.PictureRecorder recorder = ui.PictureRecorder();
  final ui.Canvas canvas = ui.Canvas(recorder, bounds);
  canvas.drawRRect(
      RRect.fromRectAndRadius(bounds.inflate(-1), const Radius.circular(5)),
      Paint()
        ..color = Colors.indigoAccent
        ..style = ui.PaintingStyle.stroke
        ..strokeWidth = 1.5);

  var span = TextSpan(style: const TextStyle(color: Colors.purple), text: "Floor sliver $rolledOver");
  var tp = TextPainter(text: span, textAlign: TextAlign.left, textDirection: TextDirection.ltr);
  tp.layout();
  tp.paint(canvas, const Offset(35, 75));

  return await recorder.endRecording().toImage(bounds.width.round(), bounds.height.round());
}

Future<ui.Image> prepareRooofImage(int sliverX, int sliverY, ui.Rect bounds) async {
  final ui.PictureRecorder recorder = ui.PictureRecorder();
  final ui.Canvas canvas = ui.Canvas(recorder, bounds);
  canvas.drawRRect(
      RRect.fromRectAndRadius(bounds.inflate(-4), const Radius.circular(5)),
      Paint()
        ..color = Colors.teal
        ..style = ui.PaintingStyle.stroke
        ..strokeWidth = 1.5);

  TextSpan span = TextSpan(style: const TextStyle(color: Colors.white), text: "($sliverX,$sliverY})");
  TextPainter tp = TextPainter(text: span, textAlign: TextAlign.left, textDirection: TextDirection.ltr);
  tp.layout();
  tp.paint(canvas, const Offset(35, 35));

  span = TextSpan(style: const TextStyle(color: Colors.purpleAccent), text: "Rooof sliver $rolledOver");
  tp = TextPainter(text: span, textAlign: TextAlign.left, textDirection: TextDirection.ltr);
  tp.layout();
  tp.paint(canvas, const Offset(35, 55));

  return await recorder.endRecording().toImage(bounds.width.round(), bounds.height.round());
}

void main() async {
  ui.window.platformDispatcher.setIsolateDebugName("blooDot Evolutions");
  WidgetsFlutterBinding.ensureInitialized();
  await Flame.device.setLandscapeLeftOnly();
  await Flame.device.fullScreen();
  ui.window.onBeginFrame = beginFrame;
  ui.window.scheduleFrame();
  ui.window.onMetricsChanged = onMetricsChanged;
}
