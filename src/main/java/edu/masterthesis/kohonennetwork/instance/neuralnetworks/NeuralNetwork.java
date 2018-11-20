package edu.masterthesis.kohonennetwork.instance.neuralnetworks;

import java.util.ArrayList;
import java.util.List;

public class NeuralNetwork {
    List<Input> inputs = new ArrayList<>();

    List<Neuron> neurons = new ArrayList<>();

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
}
