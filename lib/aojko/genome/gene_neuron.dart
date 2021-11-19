import 'gene.dart';
import 'gene_expression.dart';

class GeneNeuron extends Gene {
  late int inputConnection;
  late int outputConnection;
  late int networkNodeWeight;

  GeneNeuron() : super(GeneExpression.brainCell);
}