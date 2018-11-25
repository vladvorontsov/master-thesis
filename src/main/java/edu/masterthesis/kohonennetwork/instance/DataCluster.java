package edu.masterthesis.kohonennetwork.instance;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class DataCluster {

    private List<Integer> items;

    private DataRow clusterCenter = null;

    private Double totalError = null;

    public List<DataCluster> childClusters;

    public DataCluster(List<Integer> items) {
        this.items = items;
    }

    public List<DataCluster> getChildClusters() {
        return childClusters;
    }

    public void setChildClusters(List<DataCluster> childClusters) {
        this.childClusters = childClusters;
    }

    public DataRow getClusterCenter(boolean recalculate, List<DataRow> data) {
        if (recalculate || clusterCenter == null) {
            final Integer countOfMarks = data.get(0).getAllMarks().size();
            List<Double> marks = DoubleStream.generate(() -> 0.).limit(countOfMarks).boxed().collect(Collectors.toList());
            items.stream().map(data::get).forEach(row -> {
                for (int i = 0; i < countOfMarks; i++) {
                    marks.set(i, marks.get(i) + row.getMark(i));
                }
            });
            Integer n = items.size();
            DataRow center = new DataRow();
            for (Double mark: marks) {
                center.addMark(mark / n);
            }
            clusterCenter = center;
        }
        return clusterCenter;
    }

    public Double getTotalError(boolean recalculate, List<DataRow> data) {
        if (recalculate || totalError == null) {
            final DataRow center = getClusterCenter(recalculate, data);
            totalError = items.stream().map(data::get).mapToDouble(row -> Math.sqrt(row.getDiffWith(center))).sum();
        }
        return totalError;
    }

    public List<Integer> getItems() {
        return items;
    }

    public void setItems(List<Integer> items) {
        this.items = items;
    }

    public void setClusterCenter(DataRow clusterCenter) {
        this.clusterCenter = clusterCenter;
    }
}
