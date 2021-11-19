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

  static int primeTileFrom(int tileIndex) {
    if ([4, 16, 64].contains(tileIndex)) {
      return 1;
    }

    if ([20,80,65].contains(tileIndex)) {
      return 5;
    }

    if ([28,112,193].contains(tileIndex)) {
      return 7;
    }

    if (68 == tileIndex) {
      return 17;
    }

    if ([84,81,_noice].contains(tileIndex)) {
      return 21;
    }

    if ([92,113,197].contains(tileIndex)) {
      return 23;
    }

    if ([116,209,71].contains(tileIndex)) {
      return 29;
    }

    if ([124,241,199].contains(tileIndex)) {
      return 31;
    }

    if ([93,117,213].contains(tileIndex)) {
      return 87;
    }

    if ([125,245,215].contains(tileIndex)) {
      return 95;
    }

    if (221 == tileIndex) {
      return 119;
    }

    if ([253,247,223].contains(tileIndex)) {
      return 127;
    }

    if([0,1,5,7,17,21,23,29,31,85,87,95,119,127,255].contains(tileIndex)) {
      return tileIndex;
    }

    throw ArgumentError.value(
        tileIndex,
        "tileIndex",
        "Not an index to a clumsy-packed S-V2E2 set");
  }
}
