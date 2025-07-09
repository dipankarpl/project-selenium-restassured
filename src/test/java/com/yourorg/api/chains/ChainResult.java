package com.yourorg.api.chains;

import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * Result of API chain execution
 */
public class ChainResult {
    private final String chainName;
    private final Map<String, Response> stepResults;
    private boolean success;
    private String error;
    
    public ChainResult(String chainName) {
        this.chainName = chainName;
        this.stepResults = new HashMap<>();
        this.success = false;
    }
    
    public void addStepResult(String stepName, Response response) {
        stepResults.put(stepName, response);
    }
    
    public Response getStepResult(String stepName) {
        return stepResults.get(stepName);
    }
    
    public Map<String, Response> getAllStepResults() {
        return new HashMap<>(stepResults);
    }
    
    // Getters and setters
    public String getChainName() { return chainName; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}