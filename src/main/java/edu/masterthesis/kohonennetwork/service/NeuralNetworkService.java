package edu.masterthesis.kohonennetwork.service;

import edu.masterthesis.kohonennetwork.instance.DataCluster;
import edu.masterthesis.kohonennetwork.instance.DataRow;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class NeuralNetworkService {

    private static NeuralNetworkService instance;

    private NeuralNetworkService() {}

    public static NeuralNetworkService getNeuralNetworkService() {
        if (instance == null) {
            instance = new NeuralNetworkService();
        }
        return instance;
    }

    public List<DataRow> getCentersOfClusters(List<DataRow> initialCluster, Integer numOfClusters) {
        Map<Double, DataCluster> clusters = new HashMap<>();
        List<Integer> coefficient = IntStream.range(0, initialCluster.size()).boxed().collect(Collectors.toList());
        DataCluster initial = new DataCluster(coefficient);
        System.out.println("Start creating clusters");
        putClusterToMap(clusters, initial, initialCluster);
        clusters = separateClustersToNeededCount(clusters, numOfClusters, initialCluster);
        return clusters.values().stream().map(el -> el.getClusterCenter(false, initialCluster)).collect(Collectors.toList());
    }

    private void putClusterToMap(Map<Double, DataCluster> clusters, DataCluster initial, List<DataRow> initialCluster) {
        List<List<Integer>> childClustersIndexed = breakCluster(initial.getItems(), initialCluster);
        List<DataCluster> childClusters = childClustersIndexed.stream().map(DataCluster::new).collect(Collectors.toList());
        initial.setChildClusters(childClusters);
        Double clusteringError = calculateClusteringError(initial, childClusters, initialCluster);
        clusters.put(clusteringError, initial);
    }

    private Map<Double, DataCluster> separateClustersToNeededCount(Map<Double, DataCluster> clusters,
                                                                   Integer numOfClusters,
                                                                   List<DataRow> initialCluster) {
        DataCluster deletedCluster = removeWorstCluster(clusters);
        System.out.println("Count of clusters = " + clusters.size());
        if (clusters.size() >= numOfClusters) {
            return clusters;
        }
        DataCluster leftChild = deletedCluster.getChildClusters().get(0);
        DataCluster rightChild = deletedCluster.getChildClusters().get(1);
        putClusterToMap(clusters, leftChild, initialCluster);
        putClusterToMap(clusters, rightChild, initialCluster);
        return separateClustersToNeededCount(clusters, numOfClusters, initialCluster);
    }

    private DataCluster removeWorstCluster(Map<Double, DataCluster> clusters) {
        Double max = clusters.keySet().stream().mapToDouble(el -> el).min().orElse(Double.MAX_VALUE);
        if (max.equals(Double.MAX_VALUE)) {
            throw new RuntimeException("Failed to delete worst cluster");
        }
        return clusters.remove(max);
    }

    private Double calculateClusteringError(DataCluster clusterToBreak, List<DataCluster> childClusters,
                                            List<DataRow> initialCluster) {
        if (childClusters.size() != 2) {
            throw new RuntimeException("Wrong number of child clusters: " + childClusters.size());
        }
        Double bigError = clusterToBreak.getTotalError(false, initialCluster);
        Double leftError = childClusters.get(0).getTotalError(false, initialCluster);
        Double rightError = childClusters.get(1).getTotalError(false, initialCluster);
        return bigError - leftError - rightError;
    }

    private List<List<Integer>> breakCluster(List<Integer> coefficients, List<DataRow> initialCluster) {
        Integer maxDiffIndex = getIndexOfVariableToSeparate(coefficients, initialCluster);
        ToDoubleFunction<Integer> toDoubleFunction = value -> initialCluster.get(value).getMark(maxDiffIndex);
        List<Integer> sortedInitialCluster =
                coefficients.stream().sorted(Comparator.comparingDouble(toDoubleFunction)).collect(Collectors.toList());
        List<Double> diffBetweenCenters = getDiffBetweenCenters(sortedInitialCluster, initialCluster);
        List<Double> sumDiff = getDiffSumm(diffBetweenCenters);
        Double centroid = sumDiff.stream().mapToDouble(Double::doubleValue).sum() / sumDiff.size();
        Double closestMark = getClosestMark(centroid, sumDiff, sortedInitialCluster, initialCluster, maxDiffIndex);
        return breakClusterWithCentroid(closestMark, sortedInitialCluster, initialCluster, maxDiffIndex);
    }

    private List<List<Integer>> breakClusterWithCentroid(Double closestMark, List<Integer> coefficients,
                                                         List<DataRow> initialCluster, Integer maxDiffIndex) {
        List<List<Integer>> returnList = new LinkedList<>();
        List<Integer> leftSide = new ArrayList<>();
        List<Integer> rightSide = new ArrayList<>();
        for (Integer index: coefficients) {
            Double diff = initialCluster.get(index).getMark(maxDiffIndex) - closestMark;
            if (diff < 0) {
                leftSide.add(index);
            } else {
                rightSide.add(index);
            }
        }
        returnList.add(leftSide);
        returnList.add(rightSide);
        return returnList;
    }

    private Double getClosestMark(Double centroid, List<Double> sumDiff, List<Integer> sortedInitialCluster,
                                  List<DataRow> initialCluster, Integer maxDiffIndex) {
        ToDoubleFunction<Integer> toDoubleFunction = index -> Math.abs(centroid - sumDiff.get(index));
        Integer indexInSumDiff =
                IntStream.range(0, sumDiff.size()).boxed().min(Comparator.comparingDouble(toDoubleFunction)).orElse(null);
        if (indexInSumDiff == null) {
            throw new RuntimeException("Failed to find closest mark");
        }
        Integer indexInInitialCluster = sortedInitialCluster.get(indexInSumDiff);
        return initialCluster.get(indexInInitialCluster).getMark(maxDiffIndex);
    }

    private List<Double> getDiffSumm(List<Double> diffBetweenCenters) {
        Double sum = 0.;
        List<Double> sumDiff = new ArrayList<>();
        for (Double diff: diffBetweenCenters) {
            sum += diff;
            sumDiff.add(sum);
        }
        return sumDiff;
    }

    private List<Double> getDiffBetweenCenters(List<Integer> sortedInitialCluster, List<DataRow> initialCluster) {
        List<Double> resultList = new ArrayList<>();
        for (int i = 0; i < sortedInitialCluster.size() - 1; i++) {
            Integer prev = sortedInitialCluster.get(i);
            Integer next = sortedInitialCluster.get(i + 1);
            Double diff = initialCluster.get(prev).getDiffWith(initialCluster.get(next));
            resultList.add(diff);
        }
        return resultList;
    }

    private Integer getIndexOfVariableToSeparate(List<Integer> indexes, List<DataRow> initialCluster) {
        List<List<Double>> variables =
                Stream.generate((Supplier<ArrayList<Double>>) ArrayList::new).limit(initialCluster.get(0).getAllMarks().size())
                        .collect(Collectors.toList());
        for (Integer n: indexes) {
            DataRow row = initialCluster.get(n);
            for (int i = 0; i < row.getAllMarks().size(); i++) {
                variables.get(i).add(row.getMark(i));
            }
        }
        List<Double> diff = variables.stream().map(el -> {
            Double max = el.stream().max(Double::compareTo).orElse(null);
            Double min = el.stream().min(Double::compareTo).orElse(null);
            if (min == null || max == null) {
                throw new RuntimeException("Failed to find min or max");
            }
            return max - min;
        }).collect(Collectors.toList());
        Integer maxDiffIndex = IntStream.range(0, diff.size()).boxed().max(Comparator.comparingDouble(diff::get)).orElse(null);
        if (maxDiffIndex == null) {
            throw new RuntimeException("Failed to find min or max");
        }
        return maxDiffIndex;
    }
}
