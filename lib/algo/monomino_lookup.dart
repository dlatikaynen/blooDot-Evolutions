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

  final int primeIndex;
  final int numRotations;

  MonominoLookup(this.primeIndex, {this.numRotations = 0});
}
