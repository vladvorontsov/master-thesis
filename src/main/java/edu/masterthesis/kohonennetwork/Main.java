package edu.masterthesis.kohonennetwork;

import edu.masterthesis.kohonennetwork.service.TableService;

public class Main {

    private static final TableService TABLE_SERVICE = TableService.getService();
    private static final String PATH_TO_ORIGINAL_DATA = "class10.xlsx";

    public static void main(String[] args) {
        TABLE_SERVICE.createTrainingAndWorkingSet(PATH_TO_ORIGINAL_DATA);
    }
}
