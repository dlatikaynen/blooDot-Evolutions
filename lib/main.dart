import 'package:flame/flame.dart';
import 'package:flame/game.dart';
import 'package:flutter/material.dart';

import 'arena/follower.dart';
import 'arena/level_base.dart';

class BlooDotEvolutionsGame extends FlameGame {
  final _level = Level();
  final Follower _follower = Follower();

  @override
  Future<void> onLoad() async {
    await super.onLoad();
    if (!_level.isLoaded) {
      await _level.onLoad();
    }

    add(_level);
    add(_follower);

    _follower.position = _follower.absoluteToLocal(_level.size / 2 - _follower.size / 2);
    camera.followComponent(_follower, worldBounds: Rect.fromLTRB(0, 0, _level.size.x, _level.size.y));
  }

  @override
  void update(double dt) {
    _follower.position.x += 42 * dt;
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

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Flame.device.setLandscapeLeftOnly();
  await Flame.device.fullScreen();
  runApp(const BlooDotEvolutionsWidget());
}
