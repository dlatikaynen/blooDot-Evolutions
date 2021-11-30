import 'package:bloo_dot_evolutions/algo/monomino_lookup.dart';

class MonominoIndex {
  static const noice = 69;

  /* corner bitmask keys in the clumsy pack */
  static const edgeN = 1;
  static const edgeE = 4;
  static const edgeS = 16;
  static const edgeW = 64;

  /* edge bitmask keys in the clumsy pack */
  static const cornNE = 2;
  static const cornSE = 8;
  static const cornSW = 32;
  static const cornNW = 128;

  static MonominoLookup primeTileFrom(int tileIndex) {
    /* prime blobs */
    if (MonominoLookup.primeTiles.contains(tileIndex)) {
      return MonominoLookup(tileIndex);
    }

    var rotationIndex = [4, 16, 64].indexOf(tileIndex);
    if (rotationIndex >= 0) {
      return MonominoLookup(1, numRotations: rotationIndex + 1);
    }

    rotationIndex = [20, 80, 65].indexOf(tileIndex);
    if (rotationIndex >= 0) {
      return MonominoLookup(5, numRotations: rotationIndex + 1);
    }

    rotationIndex = [28, 112, 193].indexOf(tileIndex);
    if (rotationIndex >= 0) {
      return MonominoLookup(7, numRotations: rotationIndex + 1);
    }

    if (68 == tileIndex) {
      return MonominoLookup(17, numRotations: 1);
    }

    rotationIndex = [84, 81, noice].indexOf(tileIndex);
    if (rotationIndex >= 0) {
      return MonominoLookup(21, numRotations: rotationIndex + 1);
    }

    rotationIndex = [92, 113, 197].indexOf(tileIndex);
    if (rotationIndex >= 0) {
      return MonominoLookup(23, numRotations: rotationIndex + 1);
    }

    rotationIndex = [116, 209, 71].indexOf(tileIndex);
    if (rotationIndex >= 0) {
      return MonominoLookup(29, numRotations: rotationIndex + 1);
    }

    rotationIndex = [124, 241, 199].indexOf(tileIndex);
    if (rotationIndex >= 0) {
      return MonominoLookup(31, numRotations: rotationIndex + 1);
    }

    rotationIndex = [93, 117, 213].indexOf(tileIndex);
    if (rotationIndex >= 0) {
      return MonominoLookup(87, numRotations: rotationIndex + 1);
    }

    rotationIndex = [125, 245, 215].indexOf(tileIndex);
    if (rotationIndex >= 0) {
      return MonominoLookup(95, numRotations: rotationIndex + 1);
    }

    if (221 == tileIndex) {
      return MonominoLookup(119, numRotations: 1);
    }

    rotationIndex = [253, 247, 223].indexOf(tileIndex);
    if (rotationIndex >= 0) {
      return MonominoLookup(127, numRotations: rotationIndex + 1);
    }

    throw ArgumentError.value(tileIndex, "tileIndex", "Not an index to a S-V2E2 set");
  }

  static int coalescedTileFromNeighborConfiguration(int neighborConfiguration) =>
      MonominoLookup.seamLess[neighborConfiguration];

  static int coalescedTileFromNeighbors(bool e, bool ne, bool n, bool nw, bool w, bool sw, bool s, bool se) {
    var neighborConfiguration = (nw ? 1 : 0) +
        ((n ? 1 : 0) << 1) +
        ((ne ? 1 : 0) << 2) +
        ((e ? 1 : 0) << 3) +
        ((se ? 1 : 0) << 4) +
        ((s ? 1 : 0) << 5) +
        ((sw ? 1 : 0) << 6) +
        ((w ? 1 : 0) << 7);

    return coalescedTileFromNeighborConfiguration(neighborConfiguration);
  }
}
