package edu.masterthesis.kohonennetwork.service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Pattern;

public class TableService {

    private static TableService instance;
    private static final String TRAINING_SET_FILE = "training-set.xlsx";
    private static final String WORKING_SET_FILE = "working-set.xlsx";
    private static final Pattern IS_XLSX_FORMAT = Pattern.compile(".*\\.xlsx$");
    private static final Integer TRAINING_SET_SIZE = 5000;


    private TableService() {
    }

    public static TableService getService() {
        if (instance == null) {
            instance = new TableService();
        }
        return instance;
    }

    public void createTrainingAndWorkingSet(String pathToInitialFile) {
        File file = new File(pathToInitialFile);
        File training = new File(TRAINING_SET_FILE);
        File working = new File(WORKING_SET_FILE);
        boolean successfullyDeleted = true;
        if (training.exists()) {
            successfullyDeleted = successfullyDeleted && training.delete();
        }
        if (working.exists()) {
            successfullyDeleted = successfullyDeleted && working.delete();
        }
        if (!successfullyDeleted) {
            throw new RuntimeException("Error occurred while deleting a files");
        }
        if (!file.exists()) {
            throw new RuntimeException("File: \"" + pathToInitialFile + "\" doesn't exists");
        }
        if (!IS_XLSX_FORMAT.matcher(pathToInitialFile).find()) {
            throw new RuntimeException("Not .xlsx file specified");
        }
        try (FileInputStream originalFIS = new FileInputStream(pathToInitialFile);
             FileOutputStream trainingFOS = new FileOutputStream(TRAINING_SET_FILE);
             FileOutputStream workingFOS = new FileOutputStream(WORKING_SET_FILE)) {

            Workbook originalFile = new XSSFWorkbook(originalFIS);
            Sheet originalData = originalFile.getSheetAt(0);

            Workbook trainingSetFile = new XSSFWorkbook();
            Sheet trainingData = trainingSetFile.createSheet("Training data");

            Workbook workingSetFile = new XSSFWorkbook();
            Sheet workingData = workingSetFile.createSheet("Working data");

            Iterator<Row> originalDataRows = originalData.rowIterator();
            Row title = originalDataRows.next();
            Row trainingTitle = trainingData.createRow(0);
            setRow(trainingTitle, title);
            Row workingTitle = workingData.createRow(0);
            setRow(workingTitle, title);

            Integer trainingRowCounter = 1;
            Integer workingRowCounter = 1;
            while (originalDataRows.hasNext()) {
                Row line = originalDataRows.next();
                if (trainingRowCounter <= TRAINING_SET_SIZE) {
                    Row trainingRow = trainingData.createRow(trainingRowCounter++);
                    setRow(trainingRow, line);
                } else {
                    Row workingRow = workingData.createRow(workingRowCounter++);
                    setRow(workingRow, line);
                }
            }

            trainingSetFile.write(trainingFOS);
            workingSetFile.write(workingFOS);

        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage());
        }

    }

    private void setRow(Row rowToImport, Row rowToExport) {
        Iterator<Cell> cellsToExport = rowToExport.cellIterator();
        Integer cellIndex = 0;
        while (cellsToExport.hasNext()) {
            Cell cell = cellsToExport.next();
            if (cell.getCellType().equals(CellType.NUMERIC)) {
                rowToImport.createCell(cellIndex++).setCellValue(cell.getNumericCellValue());
            } else if (cell.getCellType().equals(CellType.STRING)) {
                rowToImport.createCell(cellIndex++).setCellValue(cell.getStringCellValue());
            }
        }
    }

}
