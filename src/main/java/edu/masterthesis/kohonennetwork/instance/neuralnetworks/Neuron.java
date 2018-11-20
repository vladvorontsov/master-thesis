package edu.masterthesis.kohonennetwork.instance.neuralnetworks;

import java.util.ArrayList;
import java.util.List;

public class Neuron {
    private Double output;

    List<NeuralConnection> inputs = new ArrayList<>();

    public Double getOutput() {
        return output;
    }

    public void setOutput(Double output) {
        this.output = output;
    }

    public List<NeuralConnection> getInputs() {
        return inputs;
    }

    public void setInputs(List<NeuralConnection> inputs) {
        this.inputs = inputs;
    }
}
