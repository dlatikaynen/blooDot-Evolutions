class MonominoLookup {
  static const primeIndexShy = 0;
  static const primeIndexU = 1;
  static const primeIndexKnee = 5;
  static const primeIndexL = 7;
  static const primeIndexPipe = 17;
  static const primeIndexT = 21;
  static const primeIndexWSE = 23;
  static const primeIndexWNE = 29;
  static const primeIndexW = 31;
  static const primeIndexCross = 85;
  static const primeIndexInvThreeNE = 87;
  static const primeIndexTwoNWSW = 95;
  static const primeIndexTwoNWSE = 119;
  static const primeIndexThreeNW = 127;
  static const primeIndexImmersed = 0xff;

  /* the clumsy pack as a whole */
  static const clumsyPack = [
    [0, 4, 92, 124, 116, 80, 0],
    [16, 20, 87, 223, 241, 21, 64],
    [29, 117, 85, 71, 221, 125, 112],
    [31, 253, 113, 28, 127, 247, 209],
    [23, 199, 213, 95, 255, 245, 81],
    [5, 84, 93, 119, 215, 193, 17],
    [0, 1, 7, 197, 69, 68, 65]
  ];

  final int primeIndex;
  final int numRotations;

  MonominoLookup(this.primeIndex, {this.numRotations = 0});
}
