class TileMatrix {
  final int size;
  late List<List<bool>> matrix;

  TileMatrix(this.size) : assert(size > 0) {
    matrix = List.generate(size, (i) => List.generate(size, (j) => false));
  }

  void setCoordinates(int xCoordinate, int yCoordinate) {
    clear();
    setElement(xCoordinate, yCoordinate);
  }

  void addCoordinates(int xCoordinate, int yCoordinate) {
    setElement(xCoordinate, yCoordinate);
  }

  bool getElement(int colIndex, int rowIndex) => matrix[rowIndex][colIndex];

  void setElement(int colIndex, int rowIndex) => matrix[rowIndex][colIndex] = true;

  void clearElement(int colIndex, int rowIndex) => matrix[rowIndex][colIndex] = false;

  void clear() {
    while (getY() > 0) {
      clearElement(getX(), getY());
    }
  }

  int getX() {
    for (var y = 0; y < size; ++y) {
      var row = matrix[y];
      var col = row.indexOf(true);
      if (col >= 0) {
        return col;
      }
    }

    return -1;
  }

  int getY() {
    for (var y = 0; y < size; ++y) {
      var row = matrix[y];
      var col = row.indexOf(true);
      if (col >= 0) {
        return y;
      }
    }

    return -1;
  }

  /// Rotates the matrix 90° clockwise
  void rotate() {
    var rotatedMatrix = List.generate(size, (i) => List.generate(size, (j) => false));
    // 90° rotation is transpose-and-reverse
    for (var y = 0; y < size; ++y) {
      for (var x = 0; x < size; ++x) {
        rotatedMatrix[y][x] = matrix[x][y];
      }
    }

    for (var y = 0; y < size; ++y) {
      rotatedMatrix[y].replaceRange(0, size, rotatedMatrix[y].reversed);
    }

    matrix = rotatedMatrix;
  }
}
