package com.yourorg.api.managers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Response manager for handling large JSON responses and data extraction
 * Demonstrates advanced JSON processing and functional programming
 */
public class ResponseManager {
    private static final Logger logger = LogManager.getLogger(ResponseManager.class);
    private final ObjectMapper objectMapper;
    
    public ResponseManager() {
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Extract specific JSON object from large response containing thousands of records
     */
    public Map<String, Object> findRecordById(Response response, String idField, String targetId) {
        try {
            String jsonResponse = response.getBody().asString();
            
            // Use JsonPath for efficient querying
            List<Map<String, Object>> records = JsonPath.read(jsonResponse, "$[?(@." + idField + " == '" + targetId + "')]");
            
            if (!records.isEmpty()) {
                logger.info("Found record with {}={}", idField, targetId);
                return records.get(0);
            } else {
                logger.warn("No record found with {}={}", idField, targetId);
                return new HashMap<>();
            }
        } catch (Exception e) {
            logger.error("Error finding record by ID: {}", e.getMessage());
            return new HashMap<>();
        }
    }
    
    /**
     * Filter records based on multiple criteria using streams
     */
    public List<Map<String, Object>> filterRecords(Response response, 
                                                   Map<String, Predicate<Object>> filters) {
        try {
            List<Map<String, Object>> allRecords = response.jsonPath().getList("$");
            
            return allRecords.stream()
                    .filter(record -> filters.entrySet().stream()
                            .allMatch(entry -> {
                                Object value = record.get(entry.getKey());
                                return entry.getValue().test(value);
                            }))
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            logger.error("Error filtering records: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Extract nested values from complex JSON structures
     */
    public List<Object> extractNestedValues(Response response, String jsonPath) {
        try {
            String jsonResponse = response.getBody().asString();
            return JsonPath.read(jsonResponse, jsonPath);
        } catch (Exception e) {
            logger.error("Error extracting nested values with path {}: {}", jsonPath, e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Paginate through large datasets and collect all records
     */
    public List<Map<String, Object>> getAllRecordsPaginated(String baseEndpoint, 
                                                           String authToken,
                                                           int pageSize) throws Exception {
        List<Map<String, Object>> allRecords = new ArrayList<>();
        int currentPage = 1;
        boolean hasMoreData = true;
        
        while (hasMoreData) {
            Response response = io.restassured.RestAssured.given()
                    .header("Authorization", "Bearer " + authToken)
                    .queryParam("page", currentPage)
                    .queryParam("size", pageSize)
                    .get(baseEndpoint);
            
            if (response.getStatusCode() == 200) {
                List<Map<String, Object>> pageRecords = response.jsonPath().getList("data");
                allRecords.addAll(pageRecords);
                
                // Check if there are more pages
                Boolean hasNext = response.jsonPath().getBoolean("hasNext");
                hasMoreData = hasNext != null && hasNext;
                currentPage++;
                
                logger.info("Fetched page {} with {} records", currentPage - 1, pageRecords.size());
            } else {
                logger.error("Failed to fetch page {}: {}", currentPage, response.getStatusCode());
                break;
            }
        }
        
        logger.info("Total records collected: {}", allRecords.size());
        return allRecords;
    }
    
    /**
     * Transform response data for subsequent API calls
     */
    public Map<String, Object> transformForNextRequest(Map<String, Object> sourceData, 
                                                      Map<String, String> fieldMapping) {
        return fieldMapping.entrySet().stream()
                .filter(entry -> sourceData.containsKey(entry.getValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> sourceData.get(entry.getValue())
                ));
    }
    
    /**
     * Validate response schema dynamically
     */
    public boolean validateDynamicSchema(Response response, Map<String, Class<?>> expectedFields) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response.getBody().asString());
            
            return expectedFields.entrySet().stream()
                    .allMatch(entry -> {
                        String fieldName = entry.getKey();
                        Class<?> expectedType = entry.getValue();
                        
                        JsonNode fieldNode = jsonNode.get(fieldName);
                        if (fieldNode == null) {
                            logger.warn("Missing field: {}", fieldName);
                            return false;
                        }
                        
                        return validateFieldType(fieldNode, expectedType);
                    });
                    
        } catch (Exception e) {
            logger.error("Error validating dynamic schema: {}", e.getMessage());
            return false;
        }
    }
    
    private boolean validateFieldType(JsonNode fieldNode, Class<?> expectedType) {
        if (expectedType == String.class) {
            return fieldNode.isTextual();
        } else if (expectedType == Integer.class) {
            return fieldNode.isInt();
        } else if (expectedType == Long.class) {
            return fieldNode.isLong() || fieldNode.isInt();
        } else if (expectedType == Double.class) {
            return fieldNode.isDouble() || fieldNode.isFloat();
        } else if (expectedType == Boolean.class) {
            return fieldNode.isBoolean();
        } else if (expectedType == List.class) {
            return fieldNode.isArray();
        } else if (expectedType == Map.class) {
            return fieldNode.isObject();
        }
        return false;
    }
    
    /**
     * Extract data for chained API requests
     */
    public ChainedRequestData extractChainedData(Response response) {
        ChainedRequestData data = new ChainedRequestData();
        
        try {
            // Extract common fields needed for subsequent requests
            data.setId(response.jsonPath().getString("id"));
            data.setUserId(response.jsonPath().getString("userId"));
            data.setTimestamp(response.jsonPath().getString("timestamp"));
            data.setStatus(response.jsonPath().getString("status"));
            
            // Extract nested data
            Map<String, Object> metadata = response.jsonPath().getMap("metadata");
            if (metadata != null) {
                data.setMetadata(metadata);
            }
            
            logger.debug("Extracted chained request data: {}", data);
            return data;
            
        } catch (Exception e) {
            logger.error("Error extracting chained data: {}", e.getMessage());
            return data;
        }
    }
    
    public static class ChainedRequestData {
        private String id;
        private String userId;
        private String timestamp;
        private String status;
        private Map<String, Object> metadata = new HashMap<>();
        
        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
        
        @Override
        public String toString() {
            return String.format("ChainedRequestData{id='%s', userId='%s', status='%s'}", 
                    id, userId, status);
        }
    }
}