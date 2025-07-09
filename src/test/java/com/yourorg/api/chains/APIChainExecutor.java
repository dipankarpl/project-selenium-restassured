package com.yourorg.api.chains;

import com.yourorg.api.builders.RequestBuilder;
import com.yourorg.api.managers.ResponseManager;
import com.yourorg.api.managers.TokenManager;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Function;

/**
 * API chain executor for complex workflows
 * Demonstrates chain of responsibility pattern and functional composition
 */
public class APIChainExecutor {
    private static final Logger logger = LogManager.getLogger(APIChainExecutor.class);
    private final ResponseManager responseManager;
    private final TokenManager tokenManager;
    private final Map<String, Object> chainContext;
    
    public APIChainExecutor() {
        this.responseManager = new ResponseManager();
        this.tokenManager = TokenManager.getInstance();
        this.chainContext = new HashMap<>();
    }
    
    /**
     * Execute a chained flow of POST, GET, and DELETE requests
     */
    public ChainResult executeChain(ChainDefinition chainDefinition) throws Exception {
        logger.info("Starting API chain execution: {}", chainDefinition.getName());
        
        ChainResult result = new ChainResult(chainDefinition.getName());
        
        try {
            for (ChainStep step : chainDefinition.getSteps()) {
                logger.info("Executing step: {} - {}", step.getName(), step.getMethod());
                
                Response response = executeStep(step);
                result.addStepResult(step.getName(), response);
                
                // Extract data for next steps
                if (step.getDataExtractor() != null) {
                    Map<String, Object> extractedData = step.getDataExtractor().apply(response);
                    chainContext.putAll(extractedData);
                    logger.debug("Extracted data for chain context: {}", extractedData.keySet());
                }
                
                // Validate response
                if (step.getValidator() != null && !step.getValidator().apply(response)) {
                    throw new Exception("Step validation failed: " + step.getName());
                }
            }
            
            result.setSuccess(true);
            logger.info("API chain completed successfully: {}", chainDefinition.getName());
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setError(e.getMessage());
            logger.error("API chain failed: {}", e.getMessage());
            throw e;
        }
        
        return result;
    }
    
    private Response executeStep(ChainStep step) throws Exception {
        RequestBuilder builder = new RequestBuilder()
                .baseUrl(step.getBaseUrl())
                .endpoint(step.getEndpoint())
                .contentType("application/json");
        
        // Add authentication if required
        if (step.isRequiresAuth()) {
            String token = tokenManager.getValidToken(step.getTokenType());
            builder.auth(token);
        }
        
        // Add headers
        if (step.getHeaders() != null) {
            builder.headers(step.getHeaders());
        }
        
        // Add query parameters
        if (step.getQueryParams() != null) {
            builder.queryParams(step.getQueryParams());
        }
        
        // Prepare body with context substitution
        Object body = prepareBody(step.getBody());
        if (body != null) {
            builder.body(body);
        }
        
        // Execute request based on method
        switch (step.getMethod().toUpperCase()) {
            case "POST":
                return builder.build().post(step.getEndpoint());
            case "GET":
                return builder.build().get(step.getEndpoint());
            case "PUT":
                return builder.build().put(step.getEndpoint());
            case "DELETE":
                return builder.build().delete(step.getEndpoint());
            case "PATCH":
                return builder.build().patch(step.getEndpoint());
            default:
                throw new Exception("Unsupported HTTP method: " + step.getMethod());
        }
    }
    
    private Object prepareBody(Object originalBody) {
        if (originalBody == null) {
            return null;
        }
        
        if (originalBody instanceof Map) {
            Map<String, Object> bodyMap = new HashMap<>((Map<String, Object>) originalBody);
            
            // Replace placeholders with context values
            bodyMap.replaceAll((key, value) -> {
                if (value instanceof String) {
                    String stringValue = (String) value;
                    if (stringValue.startsWith("${") && stringValue.endsWith("}")) {
                        String contextKey = stringValue.substring(2, stringValue.length() - 1);
                        return chainContext.getOrDefault(contextKey, value);
                    }
                }
                return value;
            });
            
            return bodyMap;
        }
        
        return originalBody;
    }
    
    /**
     * Create a standard CRUD chain
     */
    public static ChainDefinition createCrudChain(String resourceName, Map<String, Object> createData) {
        ChainDefinition chain = new ChainDefinition("CRUD_" + resourceName);
        
        // Step 1: Create resource
        ChainStep createStep = new ChainStep("create_" + resourceName, "POST", "/" + resourceName)
                .body(createData)
                .requiresAuth(true)
                .dataExtractor(response -> Map.of("resourceId", response.jsonPath().getString("id")))
                .validator(response -> response.getStatusCode() == 201);
        
        // Step 2: Get created resource
        ChainStep getStep = new ChainStep("get_" + resourceName, "GET", "/" + resourceName + "/${resourceId}")
                .requiresAuth(true)
                .validator(response -> response.getStatusCode() == 200);
        
        // Step 3: Update resource
        Map<String, Object> updateData = new HashMap<>(createData);
        updateData.put("updated", true);
        ChainStep updateStep = new ChainStep("update_" + resourceName, "PUT", "/" + resourceName + "/${resourceId}")
                .body(updateData)
                .requiresAuth(true)
                .validator(response -> response.getStatusCode() == 200);
        
        // Step 4: Delete resource
        ChainStep deleteStep = new ChainStep("delete_" + resourceName, "DELETE", "/" + resourceName + "/${resourceId}")
                .requiresAuth(true)
                .validator(response -> response.getStatusCode() == 204 || response.getStatusCode() == 200);
        
        chain.addStep(createStep)
             .addStep(getStep)
             .addStep(updateStep)
             .addStep(deleteStep);
        
        return chain;
    }
    
    /**
     * Create an e-commerce order chain
     */
    public static ChainDefinition createOrderChain(Map<String, Object> orderData) {
        ChainDefinition chain = new ChainDefinition("E_COMMERCE_ORDER");
        
        // Step 1: Create order
        ChainStep createOrder = new ChainStep("create_order", "POST", "/orders")
                .body(orderData)
                .requiresAuth(true)
                .dataExtractor(response -> Map.of(
                        "orderId", response.jsonPath().getString("id"),
                        "orderNumber", response.jsonPath().getString("orderNumber")
                ))
                .validator(response -> response.getStatusCode() == 201);
        
        // Step 2: Process payment
        ChainStep processPayment = new ChainStep("process_payment", "POST", "/payments")
                .body(Map.of(
                        "orderId", "${orderId}",
                        "amount", "${totalAmount}",
                        "method", "credit_card"
                ))
                .requiresAuth(true)
                .dataExtractor(response -> Map.of("paymentId", response.jsonPath().getString("id")))
                .validator(response -> response.getStatusCode() == 200);
        
        // Step 3: Confirm order
        ChainStep confirmOrder = new ChainStep("confirm_order", "PUT", "/orders/${orderId}/confirm")
                .body(Map.of("paymentId", "${paymentId}"))
                .requiresAuth(true)
                .validator(response -> response.getStatusCode() == 200);
        
        // Step 4: Get order status
        ChainStep getOrderStatus = new ChainStep("get_order_status", "GET", "/orders/${orderId}")
                .requiresAuth(true)
                .validator(response -> {
                    String status = response.jsonPath().getString("status");
                    return "confirmed".equals(status) || "processing".equals(status);
                });
        
        chain.addStep(createOrder)
             .addStep(processPayment)
             .addStep(confirmOrder)
             .addStep(getOrderStatus);
        
        return chain;
    }
    
    public void clearContext() {
        chainContext.clear();
        logger.debug("Chain context cleared");
    }
    
    public Map<String, Object> getContext() {
        return new HashMap<>(chainContext);
    }
}