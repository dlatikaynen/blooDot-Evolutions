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

late double devicePixelRatio = ui.window.devicePixelRatio;
late double t = 0;
late double deltaT = 0;
late double frameAverage = 0;
final floorImages = List<ui.Image>.empty(growable: true);
final rooofImages = List<ui.Image>.empty(growable: true);
late int frameNumber = 0;
late double roofX = 0.0;
late Arena arena;
late bool movingLeft = true;

void onMetricsChanged() {
  devicePixelRatio = ui.window.devicePixelRatio;
  floorImages.clear();
  rooofImages.clear();
}

void beginFrame(Duration timeStamp) async {
  final ui.Rect paintBounds = ui.Offset.zero & (ui.window.physicalSize / ui.window.devicePixelRatio);
  if (floorImages.isEmpty) {
    arena = Arena(paintBounds);
    for (var y = 0; y < 3; ++y) {
      for (var x = 0; x < 3; ++x) {
        await prepareFloorImage(x, y, paintBounds);
        await prepareRooofImage(x, y, paintBounds);
      }
    }

    arena.moveInWorld(const Offset(500, 0));
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
    movingLeft = !movingLeft;
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
    canvas.drawImageRect(floorImages[srcIndex], blitter.srcRect, blitter.dstRect, Paint());
    canvas.drawImageRect(rooofImages[srcIndex], blitter.srcRect, blitter.dstRect, Paint());
  }

  if (movingLeft) {
    arena.moveInWorld(const Offset(-1, 0));
  } else {
    arena.moveInWorld(const Offset(1, 0));
  }

  canvas.translate(paintBounds.width / 2.0, paintBounds.height / 2.0);
  TextSpan span = TextSpan(style: const TextStyle(color: Colors.white), text: "${fps.toStringAsFixed(0)} fps");
  TextPainter tp = TextPainter(text: span, textAlign: TextAlign.left, textDirection: TextDirection.ltr);
  tp.layout();
  tp.paint(canvas, const Offset(-5.0, -5.0));

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

  // After rendering the current frame of the animation, we ask the engine to
  // schedule another frame. The engine will call beginFrame again when its time
  // to produce the next frame.
  ui.window.scheduleFrame();
}

Future prepareFloorImage(int ix, int iy, ui.Rect bounds) async {
  final ui.PictureRecorder recorder = ui.PictureRecorder();
  final ui.Canvas canvas = ui.Canvas(recorder, bounds);
  canvas.drawRRect(
      RRect.fromRectAndRadius(bounds.inflate(-30), const Radius.circular(11)),
      Paint()
        ..color = Colors.indigoAccent
        ..style = ui.PaintingStyle.stroke
        ..strokeWidth = 7);

  floorImages.add(await recorder.endRecording().toImage(bounds.width.round(), bounds.height.round()));
}

Future prepareRooofImage(int ix, int iy, ui.Rect bounds) async {
  final i = rooofImages.length;
  final ui.PictureRecorder recorder = ui.PictureRecorder();
  final ui.Canvas canvas = ui.Canvas(recorder, bounds);
  canvas.drawRRect(
      RRect.fromRectAndRadius(bounds.inflate(-20), const Radius.circular(11)),
      Paint()
        ..color = Colors.teal
        ..style = ui.PaintingStyle.stroke
        ..strokeWidth = 7);

  TextSpan span = TextSpan(style: const TextStyle(color: Colors.white), text: "(${ixFromIndex(i)},${iyFromIndex(i)})");
  TextPainter tp = TextPainter(text: span, textAlign: TextAlign.left, textDirection: TextDirection.ltr);
  tp.layout();
  tp.paint(canvas, const Offset(35, 35));

  rooofImages.add(await recorder.endRecording().toImage(bounds.width.round(), bounds.height.round()));
}

int ixFromIndex(int index) => index % 3;
int iyFromIndex(int index) => (index / 3).floor();

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  ui.window.onBeginFrame = beginFrame;
  ui.window.scheduleFrame();
  ui.window.onMetricsChanged = onMetricsChanged;
}
