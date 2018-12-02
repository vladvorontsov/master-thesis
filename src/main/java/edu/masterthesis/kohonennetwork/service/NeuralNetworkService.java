package edu.masterthesis.kohonennetwork.service;

import edu.masterthesis.kohonennetwork.instance.DataCluster;
import edu.masterthesis.kohonennetwork.instance.DataRow;
import edu.masterthesis.kohonennetwork.instance.neuralnetworks.Input;
import edu.masterthesis.kohonennetwork.instance.neuralnetworks.NeuralConnection;
import edu.masterthesis.kohonennetwork.instance.neuralnetworks.NeuralNetwork;
import edu.masterthesis.kohonennetwork.instance.neuralnetworks.Neuron;

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

    public static final Double WINNER_COEFFICIENT = 0.02;
    public static final Double COMPETITOR_COEFFICIENT = -0.06;
    public static final Integer EPOCHS_MAX = 15;
    private static final Double EPSILON = 0.0001;
    private static NeuralNetworkService instance;

    private NeuralNetworkService() {
    }

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
        for (Integer index : coefficients) {
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
        for (Double diff : diffBetweenCenters) {
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
        for (Integer n : indexes) {
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

    public void trainNetwork(NeuralNetwork kohonenNetwork, List<DataRow> trainingData, Double competitorInitialCoefficient,
                             Double winnerInitialCoefficient, Integer epochsMax) {
        if (kohonenNetwork.getInputs().size() != trainingData.iterator().next().getAllMarks().size()) {
            throw new RuntimeException("Can't train Network with not similar number of inputs and marks");
        }

        Double competitorCoef = competitorInitialCoefficient;
        Double winnerCoef = winnerInitialCoefficient;
        Integer epochCounter = 0;
        List<Integer> wins = new ArrayList<>();
        for (Neuron n : kohonenNetwork.getNeurons()) {
            wins.add(1);
        }
        List<List<Double>> prevResult = generateCenters(kohonenNetwork);
        List<List<Double>> newResult = null;
        do {
            epochCounter++;
            System.out.println("Epoch number " + epochCounter + " is started");
            if (newResult != null) {
                prevResult = newResult;
            }
            for (DataRow row : trainingData) {
                List<Double> marks = row.getAllMarks();
                List<Input> inputs = kohonenNetwork.getInputs();
                for (int i = 0; i < marks.size(); i++) {
                    inputs.get(i).setValue(marks.get(i));
                }
                kohonenNetwork.generateOutputs();
            }

            ToDoubleFunction<Integer> toDouble = ind -> kohonenNetwork.getNeurons().get(ind).getOutput();
            List<Integer> winnerAndCompetitor =
                    IntStream.range(0, kohonenNetwork.getNeurons().size()).boxed()
                            .sorted(Comparator.comparingDouble(toDouble)).limit(2).collect(Collectors.toList());
            if (winnerAndCompetitor.size() < 2) {
                throw new RuntimeException("Competitor or/and Winner doesn't exist");
            }
            Integer winner = winnerAndCompetitor.get(0);
            Integer competitor = winnerAndCompetitor.get(1);
            if (kohonenNetwork.getNeurons().get(winner).getOutput() > kohonenNetwork.getNeurons().get(competitor).getOutput()) {
                throw new RuntimeException("Winner has bigger output that competitor");
            }

            wins.set(winner, wins.get(winner) + 1);
            updateWeights(kohonenNetwork.getNeurons().get(winner), winnerCoef);
            updateWeights(kohonenNetwork.getNeurons().get(competitor), competitorCoef);
            winnerCoef = winnerCoef * (1 - epochCounter.doubleValue() / epochsMax);
            competitorCoef = competitorCoef * (1 - epochCounter.doubleValue() / epochsMax);
            newResult = generateCenters(kohonenNetwork);
        } while (epochCounter < epochsMax && calculateEpsilon(prevResult, newResult) > EPSILON);
        deleteIllegalClusters(kohonenNetwork);
    }

    private void deleteIllegalClusters(NeuralNetwork kohonenNetwork) {
        List<Neuron> neuronsToRemove = new ArrayList<>();
        for (Neuron neuron : kohonenNetwork.getNeurons()) {
            for (NeuralConnection nc : neuron.getInputs()) {
                Double weight = nc.getWeight();
                if (weight < 0 || weight > 1) {
                    neuronsToRemove.add(neuron);
                    break;
                }
            }
        }
        if (neuronsToRemove.size() != 0) {
            kohonenNetwork.getNeurons().removeAll(neuronsToRemove);
            System.out.println(neuronsToRemove.size() + " cluster deleted");
        } else {
            System.out.println("Don't need to remove clusters");
        }
    }

    private Double calculateEpsilon(List<List<Double>> prevResult, List<List<Double>> newResult) {
        if (prevResult.size() != newResult.size()) {
            throw new RuntimeException("can't calculate difference between results");
        }
        return IntStream.range(0, prevResult.size()).boxed().mapToDouble(i -> {
            List<Double> prev = prevResult.get(i);
            List<Double> next = newResult.get(i);
            if (prev.size() != next.size()) {
                throw new RuntimeException("can't calculate difference between results");
            }
            Double sum = IntStream.range(0, prev.size()).boxed()
                    .mapToDouble(n -> Math.pow(prev.get(n) - next.get(n), 2.)).sum();
            return Math.sqrt(sum);
        }).sum();
    }

    private List<List<Double>> generateCenters(NeuralNetwork kohonenNetwork) {
        return kohonenNetwork.getNeurons().stream()
                .map(neuron -> neuron.getInputs().stream().map(NeuralConnection::getWeight).collect(Collectors.toList())).collect(Collectors.toList());
    }

    private void updateWeights(Neuron neuron, Double coefficient) {
        for (NeuralConnection connection : neuron.getInputs()) {
            Double oldWeight = connection.getWeight();
            Double inputValue = connection.getInput().getValue();
            if (inputValue != null)
                connection.setWeight(oldWeight + coefficient * (inputValue - oldWeight));
        }
    }
}
