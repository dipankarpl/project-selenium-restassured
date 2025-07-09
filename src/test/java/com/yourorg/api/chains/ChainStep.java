package com.yourorg.api.chains;

import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Individual step in an API chain
 */
public class ChainStep {
    private final String name;
    private final String method;
    private final String endpoint;
    private String baseUrl;
    private Object body;
    private Map<String, String> headers;
    private Map<String, Object> queryParams;
    private boolean requiresAuth = false;
    private String tokenType = "user";
    private Function<Response, Map<String, Object>> dataExtractor;
    private Function<Response, Boolean> validator;
    
    public ChainStep(String name, String method, String endpoint) {
        this.name = name;
        this.method = method;
        this.endpoint = endpoint;
        this.headers = new HashMap<>();
        this.queryParams = new HashMap<>();
    }
    
    public ChainStep baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }
    
    public ChainStep body(Object body) {
        this.body = body;
        return this;
    }
    
    public ChainStep header(String key, String value) {
        this.headers.put(key, value);
        return this;
    }
    
    public ChainStep headers(Map<String, String> headers) {
        this.headers.putAll(headers);
        return this;
    }
    
    public ChainStep queryParam(String key, Object value) {
        this.queryParams.put(key, value);
        return this;
    }
    
    public ChainStep requiresAuth(boolean requiresAuth) {
        this.requiresAuth = requiresAuth;
        return this;
    }
    
    public ChainStep tokenType(String tokenType) {
        this.tokenType = tokenType;
        return this;
    }
    
    public ChainStep dataExtractor(Function<Response, Map<String, Object>> extractor) {
        this.dataExtractor = extractor;
        return this;
    }
    
    public ChainStep validator(Function<Response, Boolean> validator) {
        this.validator = validator;
        return this;
    }
    
    // Getters
    public String getName() { return name; }
    public String getMethod() { return method; }
    public String getEndpoint() { return endpoint; }
    public String getBaseUrl() { return baseUrl; }
    public Object getBody() { return body; }
    public Map<String, String> getHeaders() { return headers; }
    public Map<String, Object> getQueryParams() { return queryParams; }
    public boolean isRequiresAuth() { return requiresAuth; }
    public String getTokenType() { return tokenType; }
    public Function<Response, Map<String, Object>> getDataExtractor() { return dataExtractor; }
    public Function<Response, Boolean> getValidator() { return validator; }
}