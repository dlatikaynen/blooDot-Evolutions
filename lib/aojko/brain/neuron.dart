import 'nerve.dart';
import 'neuron_type.dart';

abstract class NeuronInterface {}

abstract class NeuronBase implements NeuronInterface {
  final NeuronType neuronType;
  late List<Nerve> inputConnection;
  late List<Nerve> outputConnections;

  late double actionPotential;
  late bool isStatic;

  NeuronBase(this.neuronType);
}

class Neuron extends NeuronBase {
  Neuron(NeuronType neuronType) : super(neuronType);

}