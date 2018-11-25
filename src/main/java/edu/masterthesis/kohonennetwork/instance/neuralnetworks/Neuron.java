package edu.masterthesis.kohonennetwork.instance.neuralnetworks;

import java.util.ArrayList;
import java.util.List;

public class Neuron {
    private Double output;

    private List<NeuralConnection> inputs = new ArrayList<>();

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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (NeuralConnection connection: inputs) {
            sb.append("Connection ").append(i++).append(": ").append(connection).append("\n");
        }
        return sb.toString();
    }
}
