package com.yourorg.api.validators;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Predicate;

/**
 * Dynamic schema validator for frequently changing API responses
 */
public class SchemaValidator {
    private static final Logger logger = LogManager.getLogger(SchemaValidator.class);
    private final ObjectMapper objectMapper;
    
    public SchemaValidator() {
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Validate dynamic schema with flexible rules
     */
    public boolean validateDynamicSchema(Response response, SchemaDefinition schema) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response.getBody().asString());
            return validateNode(jsonNode, schema);
        } catch (Exception e) {
            logger.error("Schema validation error: {}", e.getMessage());
            return false;
        }
    }
    
    private boolean validateNode(JsonNode node, SchemaDefinition schema) {
        // Validate required fields
        for (String requiredField : schema.getRequiredFields()) {
            if (!node.has(requiredField)) {
                logger.error("Missing required field: {}", requiredField);
                return false;
            }
        }
        
        // Validate field types
        for (Map.Entry<String, Class<?>> entry : schema.getFieldTypes().entrySet()) {
            String fieldName = entry.getKey();
            Class<?> expectedType = entry.getValue();
            
            if (node.has(fieldName)) {
                JsonNode fieldNode = node.get(fieldName);
                if (!validateFieldType(fieldNode, expectedType)) {
                    logger.error("Invalid type for field {}: expected {}, got {}", 
                            fieldName, expectedType.getSimpleName(), getActualType(fieldNode));
                    return false;
                }
            }
        }
        
        // Validate custom rules
        for (Map.Entry<String, Predicate<JsonNode>> entry : schema.getCustomRules().entrySet()) {
            String ruleName = entry.getKey();
            Predicate<JsonNode> rule = entry.getValue();
            
            if (!rule.test(node)) {
                logger.error("Custom rule failed: {}", ruleName);
                return false;
            }
        }
        
        // Validate nested objects
        for (Map.Entry<String, SchemaDefinition> entry : schema.getNestedSchemas().entrySet()) {
            String fieldName = entry.getKey();
            SchemaDefinition nestedSchema = entry.getValue();
            
            if (node.has(fieldName)) {
                JsonNode nestedNode = node.get(fieldName);
                if (nestedNode.isArray()) {
                    for (JsonNode arrayItem : nestedNode) {
                        if (!validateNode(arrayItem, nestedSchema)) {
                            return false;
                        }
                    }
                } else {
                    if (!validateNode(nestedNode, nestedSchema)) {
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
    
    private boolean validateFieldType(JsonNode fieldNode, Class<?> expectedType) {
        if (expectedType == String.class) {
            return fieldNode.isTextual();
        } else if (expectedType == Integer.class) {
            return fieldNode.isInt();
        } else if (expectedType == Long.class) {
            return fieldNode.isLong() || fieldNode.isInt();
        } else if (expectedType == Double.class) {
            return fieldNode.isDouble() || fieldNode.isFloat() || fieldNode.isInt();
        } else if (expectedType == Boolean.class) {
            return fieldNode.isBoolean();
        } else if (expectedType == List.class) {
            return fieldNode.isArray();
        } else if (expectedType == Map.class) {
            return fieldNode.isObject();
        }
        return false;
    }
    
    private String getActualType(JsonNode node) {
        if (node.isTextual()) return "String";
        if (node.isInt()) return "Integer";
        if (node.isLong()) return "Long";
        if (node.isDouble()) return "Double";
        if (node.isBoolean()) return "Boolean";
        if (node.isArray()) return "Array";
        if (node.isObject()) return "Object";
        if (node.isNull()) return "Null";
        return "Unknown";
    }
    
    /**
     * Create schema definition builder
     */
    public static SchemaDefinition.Builder schema() {
        return new SchemaDefinition.Builder();
    }
    
    /**
     * Schema definition class with builder pattern
     */
    public static class SchemaDefinition {
        private final Set<String> requiredFields;
        private final Map<String, Class<?>> fieldTypes;
        private final Map<String, Predicate<JsonNode>> customRules;
        private final Map<String, SchemaDefinition> nestedSchemas;
        
        private SchemaDefinition(Builder builder) {
            this.requiredFields = new HashSet<>(builder.requiredFields);
            this.fieldTypes = new HashMap<>(builder.fieldTypes);
            this.customRules = new HashMap<>(builder.customRules);
            this.nestedSchemas = new HashMap<>(builder.nestedSchemas);
        }
        
        public Set<String> getRequiredFields() { return requiredFields; }
        public Map<String, Class<?>> getFieldTypes() { return fieldTypes; }
        public Map<String, Predicate<JsonNode>> getCustomRules() { return customRules; }
        public Map<String, SchemaDefinition> getNestedSchemas() { return nestedSchemas; }
        
        public static class Builder {
            private final Set<String> requiredFields = new HashSet<>();
            private final Map<String, Class<?>> fieldTypes = new HashMap<>();
            private final Map<String, Predicate<JsonNode>> customRules = new HashMap<>();
            private final Map<String, SchemaDefinition> nestedSchemas = new HashMap<>();
            
            public Builder requireField(String fieldName) {
                requiredFields.add(fieldName);
                return this;
            }
            
            public Builder fieldType(String fieldName, Class<?> type) {
                fieldTypes.put(fieldName, type);
                return this;
            }
            
            public Builder customRule(String ruleName, Predicate<JsonNode> rule) {
                customRules.put(ruleName, rule);
                return this;
            }
            
            public Builder nestedSchema(String fieldName, SchemaDefinition schema) {
                nestedSchemas.put(fieldName, schema);
                return this;
            }
            
            public SchemaDefinition build() {
                return new SchemaDefinition(this);
            }
        }
    }
    
    /**
     * Common schema patterns
     */
    public static class CommonSchemas {
        
        public static SchemaDefinition userSchema() {
            return schema()
                    .requireField("id")
                    .requireField("username")
                    .requireField("email")
                    .fieldType("id", String.class)
                    .fieldType("username", String.class)
                    .fieldType("email", String.class)
                    .fieldType("isActive", Boolean.class)
                    .customRule("validEmail", node -> {
                        if (node.has("email")) {
                            String email = node.get("email").asText();
                            return email.contains("@") && email.contains(".");
                        }
                        return true;
                    })
                    .build();
        }
        
        public static SchemaDefinition paginatedResponseSchema() {
            return schema()
                    .requireField("data")
                    .requireField("page")
                    .requireField("totalPages")
                    .fieldType("data", List.class)
                    .fieldType("page", Integer.class)
                    .fieldType("totalPages", Integer.class)
                    .fieldType("hasNext", Boolean.class)
                    .customRule("validPagination", node -> {
                        if (node.has("page") && node.has("totalPages")) {
                            int page = node.get("page").asInt();
                            int totalPages = node.get("totalPages").asInt();
                            return page >= 1 && page <= totalPages;
                        }
                        return true;
                    })
                    .build();
        }
        
        public static SchemaDefinition errorResponseSchema() {
            return schema()
                    .requireField("error")
                    .requireField("message")
                    .fieldType("error", String.class)
                    .fieldType("message", String.class)
                    .fieldType("code", Integer.class)
                    .fieldType("timestamp", String.class)
                    .build();
        }
    }
}