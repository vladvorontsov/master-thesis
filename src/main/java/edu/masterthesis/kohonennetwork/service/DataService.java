package edu.masterthesis.kohonennetwork.service;

import edu.masterthesis.kohonennetwork.instance.DataRow;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DataService {

    private static DataService instance;
    private static final int VALUES_COUNT = 16;

    private DataService() {
    }

    public static DataService getDataService() {
        if (instance == null) {
            instance = new DataService();
        }
        return instance;
    }

    public List<DataRow> getDataFromPathClusterIncluded(String path) {
        File dataFile = new File(path);
        List<DataRow> resultList = new ArrayList<>();
        try (FileInputStream fisData = new FileInputStream(dataFile)) {
            Workbook dataBook = new XSSFWorkbook(fisData);
            Sheet dataSheet = dataBook.getSheetAt(0);
            Iterator<Row> rows = dataSheet.rowIterator();
            rows.next(); //Don't need title in the result list

            while (rows.hasNext()) {
                Row row = rows.next();
                DataRow dataRow = new DataRow();
                Iterator<Cell> cellIterator = row.cellIterator();
                int cellCounter = 0;

                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    switch (cell.getCellType()) {
                        case BLANK:
                            if (cellCounter < VALUES_COUNT) {
                                dataRow.addMark(null);
                            }
                            break;
                        case NUMERIC:
                            if (cellCounter < VALUES_COUNT) {
                                dataRow.addMark(cell.getNumericCellValue());
                            }
                            break;
                        default:
                            throw new RuntimeException("Unexpected cell type");
                    }
                    cellCounter++;
                }

                resultList.add(dataRow);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex.getMessage());
        }
        return resultList;
    }

    public void normalizeData(List<DataRow> notNormalizedData) {
        Double maxMark = notNormalizedData.stream().flatMap(row -> row.getAllMarks().stream()).max(Double::compareTo).orElse(null);
        Double minMark = notNormalizedData.stream().flatMap(row -> row.getAllMarks().stream()).min(Double::compareTo).orElse(null);
        if (maxMark == null || minMark == null) {
            throw new RuntimeException("Failed to calculate max/min value of Data");
        }
        Double diff = maxMark - minMark;
        for (DataRow row : notNormalizedData) {
            for (int i = 0; i < row.getAllMarks().size(); i++) {
                Double mark = row.getMark(i);
                row.setMark(i, (mark - minMark) / diff);
            }
        }
    }
}
