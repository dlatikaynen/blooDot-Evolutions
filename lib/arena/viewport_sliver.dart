import "dart:ui" as ui;

class ViewportSliver {
  final int originalIx;
  final int originalIy;
  late ui.Image rooofImage;
  late ui.Image floorImage;

  ViewportSliver(this.originalIx, this.originalIy);
}
