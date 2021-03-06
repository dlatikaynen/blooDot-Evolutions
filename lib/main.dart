import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:bloo_dot_evolutions/load_screen.dart';
import 'package:flame/flame.dart';
import 'package:flame/game.dart';
import 'package:flame_audio/flame_audio.dart';
import 'package:flutter/foundation.dart' as foundation;
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
    _level ??= Level001();
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
late ui.Size viewportSize = ui.Size(paintBounds.width, paintBounds.height);
late double devicePixelRatio = ui.window.devicePixelRatio;
late double t = 0;
late double deltaT = 0;
late double frameAverage = 0;
final viewportSlivers = List<ViewportSliver>.empty(growable: true);
late int frameNumber = 0;
late double roofX = 0.0;
late Arena arena;
late LevelBase level;
late bool movingLeftUp = true;
late int rolledOver = 0;

void onMetricsChanged() {
  devicePixelRatio = ui.window.devicePixelRatio;
  paintBounds = ui.Offset.zero & (ui.window.physicalSize / devicePixelRatio);
  viewportSize = ui.Size(paintBounds.width, paintBounds.height);
  viewportSlivers.clear();
}

void onFlap(Direction flapDirection) {
  switch (flapDirection) {
    case Direction.none:
      assert(flapDirection != Direction.none);
      break;

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
    level = Level001();
    arena.onFlap = onFlap;
    for (var y = 0; y < 3; ++y) {
      for (var x = 0; x < 3; ++x) {
        var sliver = ViewportSliver(x, y);
        sliver.initializePositionInWorld(ui.Size(paintBounds.width, paintBounds.height));
        sliver.floorImage = await prepareFloorImage(sliver, paintBounds);
        sliver.rooofImage = await prepareRooofImage(sliver, paintBounds);
        viewportSlivers.add(sliver);
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

  if (rolledOver < 3) {
    var blitters = arena.viewportBlitRegions;
    for (var blitter in blitters) {
      var srcIndex = blitter.iy * 3 + blitter.ix;
      canvas.drawImageRect(viewportSlivers[srcIndex].floorImage, blitter.srcRect, blitter.dstRect, Paint());
      canvas.drawImageRect(viewportSlivers[srcIndex].rooofImage, blitter.srcRect, blitter.dstRect, Paint());
    }

    if (movingLeftUp) {
      arena.moveInWorld(const Offset(0, -1));
    } else {
      arena.moveInWorld(const Offset(0, 3));
    }
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

  if (!foundation.kReleaseMode) {
    //ignore:avoid_print
    print('flap! rolloverLeft');
  }

  ++rolledOver;

  /* don't await these... run in "background" */
  _prepareSliverAsync(0 * 3 + 0, 0 * 3 + 1, Direction.right);
  _prepareSliverAsync(1 * 3 + 0, 1 * 3 + 1, Direction.right);
  _prepareSliverAsync(2 * 3 + 0, 2 * 3 + 1, Direction.right);
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

  if (!foundation.kReleaseMode) {
    //ignore:avoid_print
    print('flap! rolloverRight');
  }

  ++rolledOver;

  /* don't await these... run in "background" */
  _prepareSliverAsync(0 * 3 + 2, 0 * 3 + 1, Direction.left);
  _prepareSliverAsync(1 * 3 + 2, 1 * 3 + 1, Direction.left);
  _prepareSliverAsync(2 * 3 + 2, 2 * 3 + 1, Direction.left);
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

  if (!foundation.kReleaseMode) {
    //ignore:avoid_print
    print('flap! rolloverUp');
  }

  ++rolledOver;

  /* don't await these... run in "background" */
  _prepareSliverAsync(0 * 3 + 0, 1 * 3 + 0, Direction.down);
  _prepareSliverAsync(0 * 3 + 1, 1 * 3 + 1, Direction.down);
  _prepareSliverAsync(0 * 3 + 2, 1 * 3 + 2, Direction.down);
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

  if (!foundation.kReleaseMode) {
    //ignore:avoid_print
    print('flap! rolloverDown');
  }

  ++rolledOver;

  /* don't await these... run in "background" */
  _prepareSliverAsync(2 * 3 + 0, 1 * 3 + 0, Direction.up);
  _prepareSliverAsync(2 * 3 + 1, 1 * 3 + 1, Direction.up);
  _prepareSliverAsync(2 * 3 + 2, 1 * 3 + 2, Direction.up);
}

void _prepareSliverAsync(int sliverIndex, int neighborIndex, Direction neighborInDirection) {
  var viewportSliver = viewportSlivers[sliverIndex].initializePositionInWorld(viewportSize,
      neighboringSliver: viewportSlivers[neighborIndex], neighborInDirection: neighborInDirection);

  prepareFloorImage(viewportSliver, paintBounds).then((image) => viewportSliver.floorImage = image);
  prepareRooofImage(viewportSliver, paintBounds).then((image) => viewportSliver.rooofImage = image);
}

Future<ui.Image> prepareFloorImage(ViewportSliver sliver, ui.Rect bounds) async {
  final ui.PictureRecorder recorder = ui.PictureRecorder();
  final ui.Canvas canvas = ui.Canvas(recorder, bounds);
  var runningX = sliver.topLeftInWorld.dx;
  do {
    canvas.drawLine(Offset(runningX, bounds.top), Offset(runningX, bounds.bottom), Paint()..color = Colors.white70);
    runningX += LevelBase.tileSize;
  } while (runningX <= bounds.width);

  var runningY = sliver.topLeftInWorld.dy;
  do {
    canvas.drawLine(Offset(bounds.left, runningY), Offset(bounds.right, runningY), Paint()..color = Colors.white70);
    runningY += LevelBase.tileSize;
  } while (runningY <= bounds.height);

  if (!level.isLoaded) {
    await level.onLoad();
  }

  canvas.drawImage(await level.sprite!.toImage(), sliver.topLeftInWorld, Paint());
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

Future<ui.Image> prepareRooofImage(ViewportSliver sliver, ui.Rect bounds) async {
  final ui.PictureRecorder recorder = ui.PictureRecorder();
  final ui.Canvas canvas = ui.Canvas(recorder, bounds);
  canvas.drawRRect(
      RRect.fromRectAndRadius(bounds.inflate(-4), const Radius.circular(5)),
      Paint()
        ..color = Colors.teal
        ..style = ui.PaintingStyle.stroke
        ..strokeWidth = 1.5);

  TextSpan span =
      TextSpan(style: const TextStyle(color: Colors.white), text: "(${sliver.originalIx},${sliver.originalIy})");
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
