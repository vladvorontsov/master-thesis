package edu.masterthesis.kohonennetwork.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

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

    public Double getDiffWith(DataRow dataRow) {
        List<Double> otherListOfValues = dataRow.getAllMarks();
        if (listOfValues.size() != otherListOfValues.size()) {
            throw new RuntimeException("Not comparable rows");
        }
        return IntStream.range(0, listOfValues.size()).boxed().mapToDouble(i -> Math.pow(listOfValues.get(i) - otherListOfValues.get(i), 2)).sum();
    }
}
