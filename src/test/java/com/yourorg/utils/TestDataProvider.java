package com.yourorg.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class TestDataProvider {
    private static final Logger logger = LogManager.getLogger(TestDataProvider.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Get test data from JSON file
    public static Map<String, Object> getTestData(String fileName) {
        try {
            String filePath = "src/test/resources/testdata/" + fileName + ".json";
            String jsonContent = Files.readString(Paths.get(filePath));
            Map<String, Object> data = objectMapper.readValue(jsonContent, new TypeReference<Map<String, Object>>() {});
            logger.info("Test data loaded from: {}", fileName);
            return data;
        } catch (Exception e) {
            logger.error("Failed to load test data from {}: {}", fileName, e.getMessage());
            return new HashMap<>();
        }
    }

    // Get bulk test data from JSON array
    public static List<Map<String, Object>> getBulkTestData(String fileName) {
        try {
            String filePath = "src/test/resources/testdata/" + fileName + ".json";
            String jsonContent = Files.readString(Paths.get(filePath));
            List<Map<String, Object>> data = objectMapper.readValue(jsonContent, new TypeReference<List<Map<String, Object>>>() {});
            logger.info("Bulk test data loaded from: {} ({} records)", fileName, data.size());
            return data;
        } catch (Exception e) {
            logger.error("Failed to load bulk test data from {}: {}", fileName, e.getMessage());
            return new ArrayList<>();
        }
    }

    // Convert to TestNG data provider format
    public static Object[][] convertToTestNGData(List<Map<String, Object>> dataList) {
        Object[][] testData = new Object[dataList.size()][];
        for (int i = 0; i < dataList.size(); i++) {
            testData[i] = new Object[]{dataList.get(i)};
        }
        return testData;
    }

    // Simple CSV data provider
    public static Object[][] getCSVData(String fileName) {
        try {
            String filePath = "src/test/resources/testdata/" + fileName + ".csv";
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            
            if (lines.size() < 2) {
                return new Object[0][0];
            }
            
            String[] headers = lines.get(0).split(",");
            Object[][] data = new Object[lines.size() - 1][headers.length];
            
            for (int i = 1; i < lines.size(); i++) {
                String[] values = lines.get(i).split(",");
                for (int j = 0; j < headers.length && j < values.length; j++) {
                    data[i - 1][j] = values[j].trim();
                }
            }
            
            logger.info("CSV data loaded from: {} ({} records)", fileName, data.length);
            return data;
        } catch (Exception e) {
            logger.error("Failed to load CSV data from {}: {}", fileName, e.getMessage());
            return new Object[0][0];
        }
    }
}