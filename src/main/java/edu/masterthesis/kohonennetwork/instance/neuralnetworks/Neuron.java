package edu.masterthesis.kohonennetwork.instance.neuralnetworks;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

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
            sb.append("\t\tConnection ").append(i++).append(": ").append(connection).append("\n");
        }
        return sb.toString();
    }

    public void generateOutput() {
        Double sum = IntStream.range(0, inputs.size()).boxed()
                .mapToDouble(i -> {
                    Double inputValue = inputs.get(i).getInput().getValue();
                    if (inputValue == null) {
                        return 0.;
                    }
                    return Math.pow(inputValue - inputs.get(i).getWeight(), 2.);
                }).sum();
        setOutput(sum);
    }
}
