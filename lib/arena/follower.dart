import 'package:flame/components.dart';

class Follower extends SpriteComponent with HasGameRef {
  Follower()
      : super(
          size: Vector2.all(50.0),
        );

  @override
  Future<void> onLoad() async {
    super.onLoad();
    sprite = await gameRef.loadSprite("follower.png");
  }
}
