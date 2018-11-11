package edu.masterthesis.kohonennetwork;

import edu.masterthesis.kohonennetwork.instance.DataRow;
import edu.masterthesis.kohonennetwork.service.DataService;
import edu.masterthesis.kohonennetwork.service.TableService;

import java.util.List;

public class Main {

    private static final TableService TABLE_SERVICE = TableService.getService();
    private static final DataService DATA_SERVICE = DataService.getDataService();
    private static final String PATH_TO_ORIGINAL_DATA = "class10.xlsx";

    public static void main(String[] args) {
        //TABLE_SERVICE.createTrainingAndWorkingSet(PATH_TO_ORIGINAL_DATA);
        //TABLE_SERVICE.createSpacesInWorkingSet();
        List<DataRow> trainingData = DATA_SERVICE.getDataFromPathClusterIncluded(TableService.TRAINING_SET_FILE);
        log("Training data reading done");
        DATA_SERVICE.normalizeData(trainingData);
        trainingData.stream().limit(10).forEach(el -> System.out.print(el + "\n"));
    }

    private static void log(String message) {
        System.out.println(message);
    }
}
