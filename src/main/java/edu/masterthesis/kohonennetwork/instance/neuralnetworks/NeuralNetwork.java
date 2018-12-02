package edu.masterthesis.kohonennetwork.instance.neuralnetworks;

import edu.masterthesis.kohonennetwork.instance.DataRow;

import java.util.ArrayList;
import java.util.List;

public class NeuralNetwork {
    private List<Input> inputs = new ArrayList<>();

    private List<Neuron> neurons = new ArrayList<>();

    public List<Input> getInputs() {
        return inputs;
    }

    public void setInputs(List<Input> inputs) {
        this.inputs = inputs;
    }

    public List<Neuron> getNeurons() {
        return neurons;
    }

    public void setNeurons(List<Neuron> neurons) {
        this.neurons = neurons;
    }

    public static NeuralNetwork getKohonenNetwork(int countOfInputs, int countOfClusters) {
        NeuralNetwork kohonenNetwork = new NeuralNetwork();
        for (int input = 0; input < countOfInputs; input ++) {
            kohonenNetwork.getInputs().add(new Input());
        }
        for (int neuron = 0; neuron < countOfClusters; neuron++) {
            Neuron newNeuron = new Neuron();
            for (int input = 0; input < countOfInputs; input ++) {
                NeuralConnection connection = new NeuralConnection();
                connection.setInput(kohonenNetwork.getInputs().get(input));
                newNeuron.getInputs().add(connection);
            }
            kohonenNetwork.getNeurons().add(newNeuron);
        }
        return kohonenNetwork;
    }

    public void setInitialWeight(List<DataRow> clusters) {
        if (clusters.size() != getNeurons().size()) {
            throw new RuntimeException("Numbers of neurons and clusters are not same");
        }
        for (int neuronIndex = 0; neuronIndex < getNeurons().size(); neuronIndex++) {
            List<Double> weights = clusters.get(neuronIndex).getAllMarks();
            List<NeuralConnection> inputs = getNeurons().get(neuronIndex).getInputs();
            if (weights.size() != getInputs().size()) {
                throw new RuntimeException("Numbers of inputs and values are not same");
            }
            for (int valueIndex = 0; valueIndex < getInputs().size(); valueIndex++) {
                inputs.get(valueIndex).setWeight(weights.get(valueIndex));
            }
        }
    }

    public void generateOutputs() {
        for (Neuron neuron: neurons) {
            neuron.generateOutput();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Inputs:\n");
        int i = 0;
        for (Input input: inputs) {
            sb.append("\t").append(i++).append(" ").append(input).append("\n");
        }
        sb.append("Neurons:\n");
        i = 0;
        for (Neuron neuron: neurons) {
            sb.append("\t").append("Neuron ").append(i++).append("\n").append(neuron);
        }
        return sb.toString();
    }
}
