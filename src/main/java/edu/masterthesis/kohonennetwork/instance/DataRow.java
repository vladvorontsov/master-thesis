package edu.masterthesis.kohonennetwork.instance;

import java.util.ArrayList;
import java.util.List;

public class DataRow {
    private Integer key;
    private List<Double> listOfValues = new ArrayList<>();

    public Integer getCluster() {
        return key;
    }

    public void setCluster(Integer key) {
        this.key = key;
    }

    public Double getMark(int i) {
        if (i >= listOfValues.size()) {
            throw new IndexOutOfBoundsException(String.format("Number of marks is %d, but trying to get element by " +
                    "index = %d", listOfValues.size(), i));
        }
        return listOfValues.get(i);
    }

    public void setMark(int i, Double mark) {
        if (i >= listOfValues.size()) {
            throw new IndexOutOfBoundsException(String.format("Number of marks is %d, but trying to get element by " +
                    "index = %d", listOfValues.size(), i));
        }
        listOfValues.set(i, mark);
    }

    public List<Double> getAllMarks() {
        return listOfValues;
    }

    public void addMark(Double value) {
        listOfValues.add(value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Marks: ");
        listOfValues.forEach(mark -> sb.append(mark).append("; "));
        sb.append("Cluster: ").append(key).append(".");
        return sb.toString();
    }
}
