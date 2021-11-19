import 'gene_expression.dart';

abstract class GeneInterface {}

abstract class Gene implements GeneInterface {
  final GeneExpression appliesTo;

  Gene(this.appliesTo);
}