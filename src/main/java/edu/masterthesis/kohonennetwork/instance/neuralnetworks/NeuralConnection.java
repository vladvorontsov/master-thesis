package edu.masterthesis.kohonennetwork.instance.neuralnetworks;

public class NeuralConnection {
    private Double weight = 0.;

    private Input input;

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Input getInput() {
        return input;
    }

    public void setInput(Input input) {
        this.input = input;
    }
}
