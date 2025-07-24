package com.yourorg.utils;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class APIUtils {
    private static final Logger logger = LogManager.getLogger(APIUtils.class);
    private static String authToken;

    static {
        String baseURI = ConfigLoader.get("api.base.url");
        if (baseURI != null) {
            RestAssured.baseURI = baseURI;
        }
    }

    // Simple request specification
    public static RequestSpecification getRequestSpec() {
        RequestSpecification spec = RestAssured.given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");
        
        if (authToken != null) {
            spec = spec.header("Authorization", "Bearer " + authToken);
        }
        
        return spec;
    }

    // HTTP Methods
    public static Response get(String endpoint) {
        logger.info("GET request to: {}", endpoint);
        Response response = getRequestSpec().when().get(endpoint);
        logger.info("GET response - Status: {}, Time: {}ms", response.getStatusCode(), response.getTime());
        return response;
    }

    public static Response post(String endpoint, Object body) {
        logger.info("POST request to: {}", endpoint);
        Response response = getRequestSpec().body(body).when().post(endpoint);
        logger.info("POST response - Status: {}, Time: {}ms", response.getStatusCode(), response.getTime());
        return response;
    }

    public static Response put(String endpoint, Object body) {
        logger.info("PUT request to: {}", endpoint);
        Response response = getRequestSpec().body(body).when().put(endpoint);
        logger.info("PUT response - Status: {}, Time: {}ms", response.getStatusCode(), response.getTime());
        return response;
    }

    public static Response delete(String endpoint) {
        logger.info("DELETE request to: {}", endpoint);
        Response response = getRequestSpec().when().delete(endpoint);
        logger.info("DELETE response - Status: {}, Time: {}ms", response.getStatusCode(), response.getTime());
        return response;
    }

    // Authentication
    public static String authenticateUser(String username, String password) throws Exception {
        String authEndpoint = ConfigLoader.get("api.auth.endpoint", "/auth/login");
        
        Map<String, String> credentials = Map.of(
                "username", username,
                "password", password
        );
        
        Response response = post(authEndpoint, credentials);
        
        if (response.getStatusCode() == 200) {
            String token = response.jsonPath().getString("token");
            setAuthToken(token);
            logger.info("Authentication successful");
            return token;
        } else {
            logger.error("Authentication failed with status: {}", response.getStatusCode());
            throw new Exception("Authentication failed");
        }
    }

    public static void setAuthToken(String token) {
        authToken = token;
        logger.info("Authentication token set");
    }

    // Validation
    public static void validateStatusCode(Response response, int expectedStatus) throws Exception {
        int actualStatus = response.getStatusCode();
        if (actualStatus != expectedStatus) {
            logger.error("Status code mismatch. Expected: {}, Actual: {}", expectedStatus, actualStatus);
            throw new Exception("Status code mismatch. Expected: " + expectedStatus + ", Actual: " + actualStatus);
        }
    }

    public static void validateResponseTime(Response response, long maxTimeMs) throws Exception {
        long actualTime = response.getTime();
        if (actualTime > maxTimeMs) {
            logger.error("Response time exceeded. Expected: {}ms, Actual: {}ms", maxTimeMs, actualTime);
            throw new Exception("Response time exceeded");
        }
    }
}