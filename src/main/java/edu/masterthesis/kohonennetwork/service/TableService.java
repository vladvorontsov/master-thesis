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
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TableService {

    private static TableService instance;
    private static final String TRAINING_SET_FILE = "training-set.xlsx";
    private static final String WORKING_SET_FILE = "working-set.xlsx";
    private static final String WORKING_SET_WITH_SPACES_FILE = "working-set-spaces.xlsx";
    private static final Pattern IS_XLSX_FORMAT = Pattern.compile(".*\\.xlsx$");
    private static final Integer TRAINING_SET_SIZE = 5000;
    private static final Integer SPACES_STEP = 250;
    private static final Integer VALUES_NUMBER = 15;


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
        setRow(rowToImport, rowToExport, null);
    }

    private void setRow(Row rowToImport, Row rowToExport, List<Integer> cellsToCut) {
        boolean cutCells = cellsToCut != null;
        Iterator<Cell> cellsToExport = rowToExport.cellIterator();
        Integer cellIndex = 0;
        while (cellsToExport.hasNext()) {
            Cell cell = cellsToExport.next();
            if (cutCells && cellsToCut.contains(cellIndex)) {
                rowToImport.createCell(cellIndex++).setCellType(CellType.BLANK);
                continue;
            }
            if (cell.getCellType().equals(CellType.NUMERIC)) {
                rowToImport.createCell(cellIndex++).setCellValue(cell.getNumericCellValue());
            } else if (cell.getCellType().equals(CellType.STRING)) {
                rowToImport.createCell(cellIndex++).setCellValue(cell.getStringCellValue());
            }
        }
    }

    public void createSpacesInWorkingSet() {
        File importFile = new File(WORKING_SET_FILE);
        File exportFile = new File(WORKING_SET_WITH_SPACES_FILE);
        if (!importFile.exists()) {
            throw new RuntimeException("Couldn't find working set");
        }
        if (exportFile.exists() && !exportFile.delete()) {
            throw new RuntimeException("Can't delete old file");
        }
        try (FileInputStream workingFIS = new FileInputStream(importFile);
             FileOutputStream workingFOS = new FileOutputStream(exportFile)) {

            Workbook workingFile = new XSSFWorkbook(workingFIS);
            Workbook workingFileMod = new XSSFWorkbook();

            Sheet data = workingFile.getSheetAt(0);
            Sheet performedData = workingFileMod.createSheet("working data");

            Iterator<Row> rows = data.rowIterator();
            Row title = rows.next();

            Row newTitle = performedData.createRow(0);
            setRow(newTitle, title);

            int rowCounter = 1;
            int stepCounter = 0;
            int cellsToCut = 1;
            while (rows.hasNext()) {
                Row row = rows.next();
                Row newRow = performedData.createRow(rowCounter++);
                List<Integer> cellsList = getRandomIntegersList(cellsToCut);
                setRow(newRow, row, cellsList);
                if (SPACES_STEP.equals(++stepCounter)) {
                    stepCounter = 0;
                    cellsToCut++;
                }
            }
            workingFileMod.write(workingFOS);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private List<Integer> getRandomIntegersList(Integer cellsToCut) {
        Random random = new Random();
        return random.ints(0, (VALUES_NUMBER + 1)).distinct().limit(cellsToCut).boxed().collect(Collectors.toList());
    }
}
