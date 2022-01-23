import 'package:flutter/foundation.dart';

@immutable
class Coords2<T> {
  final T x;
  final T y;

  const Coords2(this.x, this.y);
}

typedef IntCoords2 = Coords2<int>;
typedef IntSize2 = Coords2<int>;
