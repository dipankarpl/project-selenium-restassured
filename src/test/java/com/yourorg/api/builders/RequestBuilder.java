package com.yourorg.api.builders;

import io.restassured.specification.RequestSpecification;
import io.restassured.RestAssured;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Builder pattern implementation for API requests
 * Demonstrates fluent interface and method chaining
 */
public class RequestBuilder {
    private static final Logger logger = LogManager.getLogger(RequestBuilder.class);
    
    private String baseUrl;
    private String endpoint;
    private Map<String, String> headers;
    private Map<String, Object> queryParams;
    private Object body;
    private String authToken;
    private String contentType;
    private int timeout;
    
    public RequestBuilder() {
        this.headers = new HashMap<>();
        this.queryParams = new HashMap<>();
        this.contentType = "application/json";
        this.timeout = 30000;
    }
    
    public RequestBuilder baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }
    
    public RequestBuilder endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }
    
    public RequestBuilder header(String key, String value) {
        this.headers.put(key, value);
        return this;
    }
    
    public RequestBuilder headers(Map<String, String> headers) {
        this.headers.putAll(headers);
        return this;
    }
    
    public RequestBuilder queryParam(String key, Object value) {
        this.queryParams.put(key, value);
        return this;
    }
    
    public RequestBuilder queryParams(Map<String, Object> params) {
        this.queryParams.putAll(params);
        return this;
    }
    
    public RequestBuilder body(Object body) {
        this.body = body;
        return this;
    }
    
    public RequestBuilder auth(String token) {
        this.authToken = token;
        return this;
    }
    
    public RequestBuilder contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }
    
    public RequestBuilder timeout(int timeout) {
        this.timeout = timeout;
        return this;
    }
    
    public RequestSpecification build() {
        RequestSpecification spec = RestAssured.given();
        
        if (baseUrl != null) {
            spec.baseUri(baseUrl);
        }
        
        spec.contentType(contentType);
        
        if (authToken != null) {
            spec.header("Authorization", "Bearer " + authToken);
        }
        
        headers.forEach(spec::header);
        queryParams.forEach(spec::queryParam);
        
        if (body != null) {
            spec.body(body);
        }
        
        // Note: timeout method may not be available in all RestAssured versions
        // spec.timeout(timeout);
        
        logger.debug("Request built - Endpoint: {}, Headers: {}, Params: {}", 
                endpoint, headers.size(), queryParams.size());
        
        return spec;
    }
    
    public String getFullUrl() {
        return (baseUrl != null ? baseUrl : "") + (endpoint != null ? endpoint : "");
    }
}