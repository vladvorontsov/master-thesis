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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
                            } else {
                                dataRow.setCluster((int) cell.getNumericCellValue());
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

    public Map<Integer, Integer> creatingMappingBetweenClusters(List<Integer> oldClusters, List<Integer> newClusters) {
        if (oldClusters.size() != newClusters.size()) {
            throw new RuntimeException("Can't map clusters for lists of different size");
        }
        Map<Integer, Map<Integer, Integer>> counter = new HashMap<>();
        for (int i = 0; i < oldClusters.size(); i++) {
            Integer oldC = oldClusters.get(i);
            Integer newC = newClusters.get(i);
            counter.computeIfAbsent(oldC, k -> new HashMap<>());
            Map<Integer, Integer> counterOfC = counter.get(oldC);
            counterOfC.putIfAbsent(newC, 0);
            Integer count = counterOfC.get(newC);
            counterOfC.put(newC, count + 1);
        }
        return counter.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, i -> getMostResent(i.getValue())));
    }

    private static Integer getMostResent(Map<Integer, Integer> counter) {
        Map.Entry<Integer, Integer> best =
                counter.entrySet().stream().max(Comparator.comparingInt(Map.Entry::getValue)).orElse(null);
        if (best == null) {
            throw new RuntimeException("Failed to find best cluster to map");
        }
        return best.getKey();
    }

    public Double measurePrecision(Map<Integer, Integer> mapOfClusters, List<Integer> oldClusters,
                                   List<Integer> newClusters) {
        if (oldClusters.size() != newClusters.size()) {
            throw new RuntimeException("Can't map clusters for lists of different size");
        }
        Integer counter = 0;
        for (int i = 0; i < oldClusters.size(); i++) {
            Integer oldC = oldClusters.get(i);
            Integer newC = newClusters.get(i);
            if (mapOfClusters.get(oldC).equals(newC)) {
                counter++;
            }
        }
        return counter.doubleValue() / oldClusters.size();
    }
}
