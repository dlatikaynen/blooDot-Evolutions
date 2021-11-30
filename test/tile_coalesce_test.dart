import 'package:bloo_dot_evolutions/algo/monomino_index.dart';
import 'package:bloo_dot_evolutions/algo/monomino_lookup.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  test("When_IterateAllNeighborConfigurations_Expect_CorrectHistogram", () async {
    Map<int, int> histogram = {};
    for (var nc = 0; nc <= 0xff; ++nc) {
      var tileIndex = MonominoIndex.coalescedTileFromNeighborConfiguration(nc);
      if (histogram.containsKey(tileIndex)) {
        histogram[tileIndex] = histogram[tileIndex]! + 1;
      } else {
        histogram[tileIndex] = 1;
      }
    }

    var distinctTiles = histogram.length;
    var distinctFrequencies = histogram.values.toSet().toList();
    distinctFrequencies.sort();

    expect(distinctTiles, MonominoLookup.clumsyPack.expand((element) => element).toSet().length);
    expect(distinctFrequencies, [1, 2, 4, 6, 8, 10, 12, 16, 20]);
  });
}
