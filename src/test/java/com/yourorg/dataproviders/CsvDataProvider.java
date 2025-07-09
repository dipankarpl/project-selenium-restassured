package com.yourorg.dataproviders;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import com.yourorg.abstracts.AbstractTestDataProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * CSV-based test data provider using OpenCSV
 */
public class CsvDataProvider extends AbstractTestDataProvider {
    private static final Logger logger = LogManager.getLogger(CsvDataProvider.class);
    private final String filePath;
    
    public CsvDataProvider(String filePath) {
        this.filePath = filePath;
        logger.info("CsvDataProvider initialized with file: {}", filePath);
    }
    
    @Override
    public Map<String, Object> getTestData(String testName) {
        // For CSV, we'll look for a specific row by test name
        List<Map<String, Object>> allData = getBulkTestData("data");
        
        return allData.stream()
                .filter(row -> testName.equals(row.get("testName")))
                .findFirst()
                .orElse(new HashMap<>());
    }
    
    @Override
    public List<Map<String, Object>> getBulkTestData(String dataSet) {
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> records = reader.readAll();
            
            if (records.isEmpty()) {
                logger.warn("CSV file is empty: {}", filePath);
                return new ArrayList<>();
            }
            
            // First row contains headers
            String[] headers = records.get(0);
            List<Map<String, Object>> dataList = new ArrayList<>();
            
            // Process data rows
            for (int i = 1; i < records.size(); i++) {
                String[] row = records.get(i);
                Map<String, Object> rowData = new HashMap<>();
                
                for (int j = 0; j < headers.length && j < row.length; j++) {
                    String value = row[j];
                    rowData.put(headers[j], parseValue(value));
                }
                dataList.add(rowData);
            }
            
            logger.info("CSV data loaded: {} records", dataList.size());
            return dataList;
            
        } catch (IOException | CsvException e) {
            logger.error("Failed to load CSV data: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public void saveTestData(String testName, Map<String, Object> data) {
        try {
            // Read existing data
            List<Map<String, Object>> existingData = getBulkTestData("data");
            
            // Add test name to data
            data.put("testName", testName);
            
            // Check if test data already exists and update, otherwise add
            boolean updated = false;
            for (Map<String, Object> row : existingData) {
                if (testName.equals(row.get("testName"))) {
                    row.putAll(data);
                    updated = true;
                    break;
                }
            }
            
            if (!updated) {
                existingData.add(data);
            }
            
            // Write back to CSV
            writeDataToCsv(existingData);
            
            logger.info("Test data saved to CSV for: {}", testName);
            
        } catch (Exception e) {
            logger.error("Failed to save CSV data for {}: {}", testName, e.getMessage());
        }
    }
    
    @Override
    protected boolean checkDataSource(String testName) {
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> records = reader.readAll();
            
            if (records.size() < 2) {
                return false;
            }
            
            // Check if testName column exists and has the specified test
            String[] headers = records.get(0);
            int testNameIndex = -1;
            
            for (int i = 0; i < headers.length; i++) {
                if ("testName".equals(headers[i])) {
                    testNameIndex = i;
                    break;
                }
            }
            
            if (testNameIndex == -1) {
                return false;
            }
            
            // Check if test exists
            for (int i = 1; i < records.size(); i++) {
                String[] row = records.get(i);
                if (testNameIndex < row.length && testName.equals(row[testNameIndex])) {
                    return true;
                }
            }
            
            return false;
            
        } catch (IOException | CsvException e) {
            logger.error("Failed to check CSV data source: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Parse string value to appropriate type
     */
    private Object parseValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        value = value.trim();
        
        // Try to parse as boolean
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.parseBoolean(value);
        }
        
        // Try to parse as integer
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            // Not an integer
        }
        
        // Try to parse as double
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            // Not a double
        }
        
        // Return as string
        return value;
    }
    
    /**
     * Write data back to CSV file
     */
    private void writeDataToCsv(List<Map<String, Object>> dataList) throws IOException {
        if (dataList.isEmpty()) {
            return;
        }
        
        // Get all unique keys as headers
        Set<String> allKeys = new LinkedHashSet<>();
        for (Map<String, Object> row : dataList) {
            allKeys.addAll(row.keySet());
        }
        
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            // Write headers
            String[] headers = allKeys.toArray(new String[0]);
            writer.writeNext(headers);
            
            // Write data rows
            for (Map<String, Object> row : dataList) {
                String[] values = new String[headers.length];
                for (int i = 0; i < headers.length; i++) {
                    Object value = row.get(headers[i]);
                    values[i] = value != null ? value.toString() : "";
                }
                writer.writeNext(values);
            }
        }
    }
    
    /**
     * Get data for TestNG DataProvider
     */
    public Object[][] getDataForTestNG() {
        List<Map<String, Object>> dataList = getBulkTestData("data");
        
        if (dataList.isEmpty()) {
            return new Object[0][0];
        }
        
        Object[][] testData = new Object[dataList.size()][];
        for (int i = 0; i < dataList.size(); i++) {
            testData[i] = new Object[]{dataList.get(i)};
        }
        
        return testData;
    }
    
    /**
     * Get specific columns as TestNG data
     */
    public Object[][] getColumnsForTestNG(String... columnNames) {
        List<Map<String, Object>> dataList = getBulkTestData("data");
        
        if (dataList.isEmpty()) {
            return new Object[0][0];
        }
        
        Object[][] testData = new Object[dataList.size()][columnNames.length];
        for (int i = 0; i < dataList.size(); i++) {
            Map<String, Object> row = dataList.get(i);
            for (int j = 0; j < columnNames.length; j++) {
                testData[i][j] = row.get(columnNames[j]);
            }
        }
        
        return testData;
    }
}