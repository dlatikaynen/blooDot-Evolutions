import 'package:bloo_dot_evolutions/algo/monomino_lookup.dart';

class MonominoIndex {
  static const _noice = 69;

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
    if([0,1,5,7,17,21,23,29,31,85,87,95,119,127,255].contains(tileIndex)) {
      return MonominoLookup(tileIndex);
    }

    var rotationIndex = [4, 16, 64].indexOf(tileIndex);
    if (rotationIndex >= 0) {
      return MonominoLookup(1, numRotations: rotationIndex + 1);
    }

    rotationIndex = [20,80,65].indexOf(tileIndex);
    if (rotationIndex >= 0) {
      return MonominoLookup(5, numRotations: rotationIndex + 1);
    }

    rotationIndex = [28,112,193].indexOf(tileIndex);
    if (rotationIndex >= 0) {
      return MonominoLookup(7, numRotations: rotationIndex + 1);
    }

    if (68 == tileIndex) {
      return MonominoLookup(17, numRotations: 1);
    }

    rotationIndex = [84,81,_noice].indexOf(tileIndex);
    if (rotationIndex >= 0) {
      return MonominoLookup(21, numRotations: rotationIndex + 1);
    }

    rotationIndex = [92,113,197].indexOf(tileIndex);
    if (rotationIndex >= 0) {
      return MonominoLookup(23, numRotations: rotationIndex + 1);
    }

    rotationIndex = [116,209,71].indexOf(tileIndex);
    if (rotationIndex >= 0) {
      return MonominoLookup(29, numRotations: rotationIndex + 1);
    }

    rotationIndex = [124,241,199].indexOf(tileIndex);
    if (rotationIndex >= 0) {
      return MonominoLookup(31, numRotations: rotationIndex + 1);
    }

    rotationIndex = [93,117,213].indexOf(tileIndex);
    if (rotationIndex >= 0) {
      return MonominoLookup(87, numRotations: rotationIndex + 1);
    }

    rotationIndex = [125,245,215].indexOf(tileIndex);
    if (rotationIndex >= 0) {
      return MonominoLookup(95, numRotations: rotationIndex + 1);
    }

    if (221 == tileIndex) {
      return MonominoLookup(119, numRotations: 1);
    }

    rotationIndex = [253,247,223].indexOf(tileIndex);
    if (rotationIndex >= 0) {
      return MonominoLookup(127, numRotations: 1);
    }

    throw ArgumentError.value(
        tileIndex,
        "tileIndex",
        "Not an index to a clumsy-packed S-V2E2 set");
  }
}
