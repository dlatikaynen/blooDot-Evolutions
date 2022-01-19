import "dart:ui" as ui;

class ViewportBlitRegion {
  late int ix;
  late int iy;
  late ui.Rect srcRect;
  late ui.Rect dstRect;

  ViewportBlitRegion({this.ix = 0, this.iy = 0, this.srcRect = ui.Rect.zero, this.dstRect = ui.Rect.zero});
}
