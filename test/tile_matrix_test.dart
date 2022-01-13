import 'package:bloo_dot_evolutions/algo/tile_matrix.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  group('algo::matrix', () {
    test("When_CoordinatesSet_Expect_RetrieveSame", () async {
      var testMatrix = TileMatrix(3);
      testMatrix.setCoordinates(2, 1);

      expect(testMatrix.getX(), 2);
      expect(testMatrix.getY(), 1);

      testMatrix.clear();

      expect(testMatrix.getX(), -1);
      expect(testMatrix.getY(), -1);
    });

    test("When_RotateOnce_Expect_RotatedOnce", () async {
      var testMatrix = TileMatrix(3);
      testMatrix.setCoordinates(2, 1);

      testMatrix.rotate();

      expect(testMatrix.getX(), 1);
      expect(testMatrix.getY(), 2);
    });
  });
}
