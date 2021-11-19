import 'gene.dart';
import 'gene_expression.dart';

class Chromosome {
  final GeneExpression geneticGroup;
  final List<Gene> containsGenes;

  Chromosome(this.geneticGroup, this.containsGenes);
}