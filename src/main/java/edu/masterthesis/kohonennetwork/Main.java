package edu.masterthesis.kohonennetwork;

import edu.masterthesis.kohonennetwork.instance.DataRow;
import edu.masterthesis.kohonennetwork.instance.neuralnetworks.NeuralNetwork;
import edu.masterthesis.kohonennetwork.service.DataService;
import edu.masterthesis.kohonennetwork.service.NeuralNetworkService;
import edu.masterthesis.kohonennetwork.service.TableService;

import java.util.List;

public class Main {

    private static final TableService TABLE_SERVICE = TableService.getService();
    private static final DataService DATA_SERVICE = DataService.getDataService();
    private static final NeuralNetworkService NN_SERVICE = NeuralNetworkService.getNeuralNetworkService();
    private static final String PATH_TO_ORIGINAL_DATA = "class10.xlsx";
    private static final int NUMBER_OF_CLUSTERS = 10;

    public static void main(String[] args) {
        //TABLE_SERVICE.createTrainingAndWorkingSet(PATH_TO_ORIGINAL_DATA);
        //TABLE_SERVICE.createSpacesInWorkingSet();
        List<DataRow> trainingData = DATA_SERVICE.getDataFromPathClusterIncluded(TableService.TRAINING_SET_FILE);
        log("Training data reading done: ");
        DATA_SERVICE.normalizeData(trainingData);
        trainingData.stream().limit(10).forEach(el -> System.out.print(el + "\n"));
        final int countOfMarks = trainingData.get(0).getAllMarks().size();
        NeuralNetwork kohonenNetwork = NeuralNetwork.getKohonenNetwork(countOfMarks, NUMBER_OF_CLUSTERS);
        log("Kohonen Network created");
        List<DataRow> initialCenters = NN_SERVICE.getCentersOfClusters(trainingData, NUMBER_OF_CLUSTERS);
        log("Initial clusters created");
        kohonenNetwork.setInitialWeight(initialCenters);
        log(kohonenNetwork.toString());
    }

    private static void log(String message) {
        System.out.println(message);
    }
}
