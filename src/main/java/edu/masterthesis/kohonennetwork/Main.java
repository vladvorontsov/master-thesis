package edu.masterthesis.kohonennetwork;

import edu.masterthesis.kohonennetwork.instance.DataRow;
import edu.masterthesis.kohonennetwork.instance.neuralnetworks.NeuralNetwork;
import edu.masterthesis.kohonennetwork.service.DataService;
import edu.masterthesis.kohonennetwork.service.NeuralNetworkService;
import edu.masterthesis.kohonennetwork.service.TableService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {

    private static final TableService TABLE_SERVICE = TableService.getService();
    private static final DataService DATA_SERVICE = DataService.getDataService();
    private static final NeuralNetworkService NN_SERVICE = NeuralNetworkService.getNeuralNetworkService();
    private static final String PATH_TO_ORIGINAL_DATA = "class10.xlsx";
    private static final int NUMBER_OF_CLUSTERS = 10;
    private static final int STEP = 250;

    public static void main(String[] args) {
        //TABLE_SERVICE.createTrainingAndWorkingSet(PATH_TO_ORIGINAL_DATA);
        //TABLE_SERVICE.createSpacesInWorkingSet();
        List<DataRow> trainingData = DATA_SERVICE.getDataFromPathClusterIncluded(TableService.TRAINING_SET_FILE);
        log("Training data reading done: ");
        DATA_SERVICE.normalizeData(trainingData);
        trainingData.stream().limit(10).forEach(Main::log);
        final int countOfMarks = trainingData.get(0).getAllMarks().size();
        NeuralNetwork kohonenNetwork = NeuralNetwork.getKohonenNetwork(countOfMarks, NUMBER_OF_CLUSTERS);
        log("Kohonen Network created");
        List<DataRow> initialCenters = NN_SERVICE.getCentersOfClusters(trainingData, NUMBER_OF_CLUSTERS);
        log("Initial clusters created");
        kohonenNetwork.setInitialWeight(initialCenters);
        log("Initial network:");
        log(kohonenNetwork);
        log("Train Kohonen network");
        NN_SERVICE.trainNetwork(
                kohonenNetwork,trainingData,
                NeuralNetworkService.COMPETITOR_COEFFICIENT,
                NeuralNetworkService.WINNER_COEFFICIENT,
                NeuralNetworkService.EPOCHS_MAX);
        log("Trained network");
        log(kohonenNetwork);

        Map<Integer, Integer> mapOfClusters = calculatePrecision(trainingData, kohonenNetwork, null);

        List<DataRow> workingData =
                DATA_SERVICE.getDataFromPathClusterIncluded(TableService.WORKING_SET_WITH_SPACES_FILE);
        workingData.stream().limit(10).forEach(Main::log);

        log("Calculate clusters for working data");
        for (int start = 0; start < workingData.size(); start = start + STEP) {
            int end = start + STEP;
            if (end > workingData.size()) {
                end = workingData.size();
            }
            List<DataRow> sublist = workingData.subList(start, end);
            calculatePrecision(sublist, kohonenNetwork, mapOfClusters);
        }

    }

    private static Map<Integer, Integer> calculatePrecision(List<DataRow> data, NeuralNetwork kohonenNetwork,
                                                            Map<Integer, Integer> mapOfClusters) {
        List<Integer> oldClusters = data.stream().map(DataRow::getCluster).collect(Collectors.toList());
        NN_SERVICE.calculateClusters(kohonenNetwork, data);
        List<Integer> newClusters = data.stream().map(DataRow::getCluster).collect(Collectors.toList());
        if (mapOfClusters == null) {
            log("Map clusters");
            mapOfClusters = DATA_SERVICE.creatingMappingBetweenClusters(oldClusters, newClusters);
        }
        Double trainingPercent = DATA_SERVICE.measurePrecision(mapOfClusters, oldClusters, newClusters);
        log("Percent of precision of initial data " + trainingPercent);
        return mapOfClusters;
    }

    private static void log(Object message) {
        System.out.println(message);
    }
}
