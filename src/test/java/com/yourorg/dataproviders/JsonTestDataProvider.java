package com.yourorg.dataproviders;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourorg.abstracts.AbstractTestDataProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JSON-based test data provider
 * Demonstrates composition and file handling
 */
public class JsonTestDataProvider extends AbstractTestDataProvider {
    private static final Logger logger = LogManager.getLogger(JsonTestDataProvider.class);
    private final ObjectMapper objectMapper;
    private final String dataDirectory;
    
    public JsonTestDataProvider(String dataDirectory) {
        this.dataDirectory = dataDirectory;
        this.objectMapper = new ObjectMapper();
        logger.info("JsonTestDataProvider initialized with directory: {}", dataDirectory);
    }
    
    @Override
    public Map<String, Object> getTestData(String testName) {
        // Check cache first
        Object cachedData = getCachedData(testName);
        if (cachedData instanceof Map) {
            return (Map<String, Object>) cachedData;
        }
        
        try {
            String fileName = testName + ".json";
            Path filePath = Paths.get(dataDirectory, fileName);
            
            if (!Files.exists(filePath)) {
                logger.warn("Test data file not found: {}", filePath);
                return new HashMap<>();
            }
            
            String jsonContent = Files.readString(filePath);
            Map<String, Object> data = objectMapper.readValue(jsonContent, 
                    new TypeReference<Map<String, Object>>() {});
            
            // Cache the data
            cacheData(testName, data);
            
            logger.info("Test data loaded for: {}", testName);
            return data;
            
        } catch (IOException e) {
            logger.error("Failed to load test data for {}: {}", testName, e.getMessage());
            return new HashMap<>();
        }
    }
    
    @Override
    public List<Map<String, Object>> getBulkTestData(String dataSet) {
        try {
            String fileName = dataSet + "_bulk.json";
            Path filePath = Paths.get(dataDirectory, fileName);
            
            if (!Files.exists(filePath)) {
                logger.warn("Bulk test data file not found: {}", filePath);
                return new ArrayList<>();
            }
            
            String jsonContent = Files.readString(filePath);
            List<Map<String, Object>> data = objectMapper.readValue(jsonContent, 
                    new TypeReference<List<Map<String, Object>>>() {});
            
            logger.info("Bulk test data loaded for: {} ({} records)", dataSet, data.size());
            return data;
            
        } catch (IOException e) {
            logger.error("Failed to load bulk test data for {}: {}", dataSet, e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public void saveTestData(String testName, Map<String, Object> data) {
        try {
            String fileName = testName + ".json";
            Path filePath = Paths.get(dataDirectory, fileName);
            
            // Create directory if it doesn't exist
            Files.createDirectories(filePath.getParent());
            
            String jsonContent = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(data);
            Files.writeString(filePath, jsonContent);
            
            // Update cache
            cacheData(testName, data);
            
            logger.info("Test data saved for: {}", testName);
            
        } catch (IOException e) {
            logger.error("Failed to save test data for {}: {}", testName, e.getMessage());
        }
    }
    
    @Override
    protected boolean checkDataSource(String testName) {
        String fileName = testName + ".json";
        Path filePath = Paths.get(dataDirectory, fileName);
        return Files.exists(filePath);
    }
    
    /**
     * Get all available test data files using streams
     */
    public List<String> getAvailableTestData() {
        try {
            Path dirPath = Paths.get(dataDirectory);
            if (!Files.exists(dirPath)) {
                return new ArrayList<>();
            }
            
            return Files.list(dirPath)
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(name -> name.endsWith(".json"))
                    .map(name -> name.substring(0, name.lastIndexOf('.')))
                    .collect(Collectors.toList());
                    
        } catch (IOException e) {
            logger.error("Failed to list available test data: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Parse complex nested JSON structures
     */
    public Map<String, Object> parseComplexJson(String jsonContent) {
        try {
            return objectMapper.readValue(jsonContent, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            logger.error("Failed to parse complex JSON: {}", e.getMessage());
            return new HashMap<>();
        }
    }
    
    /**
     * Extract specific data from nested JSON using lambda expressions
     */
    public List<String> extractNestedValues(Map<String, Object> data, String keyPath) {
        return data.entrySet().stream()
                .filter(entry -> entry.getKey().contains(keyPath))
                .map(Map.Entry::getValue)
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.toList());
    }
    
    /**
     * Transform test data using streams and lambdas
     */
    public Map<String, Object> transformTestData(Map<String, Object> originalData, 
                                                 String prefix) {
        return originalData.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> prefix + "_" + entry.getKey(),
                        Map.Entry::getValue,
                        (existing, replacement) -> replacement,
                        LinkedHashMap::new
                ));
    }
}