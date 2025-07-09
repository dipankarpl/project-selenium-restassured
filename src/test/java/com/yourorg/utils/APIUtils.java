package com.yourorg.utils;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class APIUtils {
    private static final Logger logger = LogManager.getLogger(APIUtils.class);
    private static String baseURI;
    private static String authToken;
    private static String apiKey;
    private static String basicAuthUsername;
    private static String basicAuthPassword;
    private static String bearerToken;
    private static String oauthToken;

    static {
        baseURI = ConfigLoader.get("api.base.url");
        if (baseURI != null) {
            RestAssured.baseURI = baseURI;
        }
    }

    // Authentication Methods
    public static void setAuthToken(String token) {
        authToken = token;
        logger.info("Authentication token set");
    }

    public static void setApiKey(String key) {
        apiKey = key;
        logger.info("API key set");
    }

    public static void setBasicAuth(String username, String password) {
        basicAuthUsername = username;
        basicAuthPassword = password;
        logger.info("Basic authentication credentials set");
    }

    public static void setBearerToken(String token) {
        bearerToken = token;
        logger.info("Bearer token set");
    }

    public static void setOAuthToken(String token) {
        oauthToken = token;
        logger.info("OAuth token set");
    }

    public static RequestSpecification getRequestSpecification() {
        RequestSpecification spec = RestAssured.given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");
        
        // Apply authentication based on available credentials
        if (bearerToken != null) {
            spec = spec.header("Authorization", "Bearer " + bearerToken);
        } else if (authToken != null) {
            spec = spec.header("Authorization", "Bearer " + authToken);
        } else if (oauthToken != null) {
            spec = spec.header("Authorization", "OAuth " + oauthToken);
        } else if (apiKey != null) {
            spec = spec.header("X-API-Key", apiKey);
        } else if (basicAuthUsername != null && basicAuthPassword != null) {
            spec = spec.auth().basic(basicAuthUsername, basicAuthPassword);
        }
        
        return spec;
    }

    public static RequestSpecification getRequestSpecificationWithCustomAuth(String authType, String credentials) {
        RequestSpecification spec = RestAssured.given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");
        
        switch (authType.toLowerCase()) {
            case "bearer":
                spec = spec.header("Authorization", "Bearer " + credentials);
                break;
            case "basic":
                String[] creds = credentials.split(":");
                spec = spec.auth().basic(creds[0], creds[1]);
                break;
            case "apikey":
                spec = spec.header("X-API-Key", credentials);
                break;
            case "oauth":
                spec = spec.header("Authorization", "OAuth " + credentials);
                break;
            case "custom":
                spec = spec.header("Authorization", credentials);
                break;
            default:
                logger.warn("Unknown auth type: {}", authType);
        }
        
        return spec;
    }

    // HTTP Methods
    public static Response get(String endpoint) {
        logger.info("Making GET request to: {}", endpoint);
        Response response = getRequestSpecification()
                .when()
                .get(endpoint);
        
        logger.info("GET response - Status: {}, Time: {}ms", 
                response.getStatusCode(), response.getTime());
        return response;
    }

    public static Response post(String endpoint, Object body) {
        logger.info("Making POST request to: {}", endpoint);
        Response response = getRequestSpecification()
                .body(body)
                .when()
                .post(endpoint);
        
        logger.info("POST response - Status: {}, Time: {}ms", 
                response.getStatusCode(), response.getTime());
        return response;
    }

    public static Response put(String endpoint, Object body) {
        logger.info("Making PUT request to: {}", endpoint);
        Response response = getRequestSpecification()
                .body(body)
                .when()
                .put(endpoint);
        
        logger.info("PUT response - Status: {}, Time: {}ms", 
                response.getStatusCode(), response.getTime());
        return response;
    }

    public static Response delete(String endpoint) {
        logger.info("Making DELETE request to: {}", endpoint);
        Response response = getRequestSpecification()
                .when()
                .delete(endpoint);
        
        logger.info("DELETE response - Status: {}, Time: {}ms", 
                response.getStatusCode(), response.getTime());
        return response;
    }

    public static Response patch(String endpoint, Object body) {
        logger.info("Making PATCH request to: {}", endpoint);
        Response response = getRequestSpecification()
                .body(body)
                .when()
                .patch(endpoint);
        
        logger.info("PATCH response - Status: {}, Time: {}ms", 
                response.getStatusCode(), response.getTime());
        return response;
    }

    // Authentication Methods
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

    public static String authenticateWithApiKey(String apiKey) throws Exception {
        setApiKey(apiKey);
        
        // Test API key validity
        Response response = get("/auth/validate");
        
        if (response.getStatusCode() == 200) {
            logger.info("API key authentication successful");
            return apiKey;
        } else {
            logger.error("API key authentication failed with status: {}", response.getStatusCode());
            throw new Exception("API key authentication failed");
        }
    }

    public static String authenticateWithOAuth(String clientId, String clientSecret) throws Exception {
        Map<String, String> oauthData = Map.of(
                "client_id", clientId,
                "client_secret", clientSecret,
                "grant_type", "client_credentials"
        );
        
        Response response = post("/oauth/token", oauthData);
        
        if (response.getStatusCode() == 200) {
            String token = response.jsonPath().getString("access_token");
            setOAuthToken(token);
            logger.info("OAuth authentication successful");
            return token;
        } else {
            logger.error("OAuth authentication failed with status: {}", response.getStatusCode());
            throw new Exception("OAuth authentication failed");
        }
    }

    // Security Testing Methods
    public static Response testSQLInjection(String endpoint, String payload) {
        logger.info("Testing SQL injection on endpoint: {} with payload: {}", endpoint, payload);
        
        Map<String, String> injectionData = Map.of("input", payload);
        
        Response response = post(endpoint, injectionData);
        logger.info("SQL injection test response - Status: {}", response.getStatusCode());
        
        return response;
    }

    public static Response testXSSAttack(String endpoint, String payload) {
        logger.info("Testing XSS attack on endpoint: {} with payload: {}", endpoint, payload);
        
        Map<String, String> xssData = Map.of("input", payload);
        
        Response response = post(endpoint, xssData);
        logger.info("XSS attack test response - Status: {}", response.getStatusCode());
        
        return response;
    }

    public static Response testTokenManipulation(String endpoint, String manipulatedToken) {
        logger.info("Testing token manipulation on endpoint: {}", endpoint);
        
        RequestSpecification spec = RestAssured.given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + manipulatedToken);
        
        Response response = spec.when().get(endpoint);
        logger.info("Token manipulation test response - Status: {}", response.getStatusCode());
        
        return response;
    }

    public static void testRateLimit(String endpoint, int requestCount, int intervalMs) throws Exception {
        logger.info("Testing rate limit on endpoint: {} with {} requests", endpoint, requestCount);
        
        int successCount = 0;
        int rateLimitedCount = 0;
        
        for (int i = 0; i < requestCount; i++) {
            Response response = get(endpoint);
            
            if (response.getStatusCode() == 200) {
                successCount++;
            } else if (response.getStatusCode() == 429) {
                rateLimitedCount++;
                logger.info("Rate limit triggered at request #{}", i + 1);
            }
            
            if (intervalMs > 0) {
                Thread.sleep(intervalMs);
            }
        }
        
        logger.info("Rate limit test completed - Success: {}, Rate Limited: {}", 
                successCount, rateLimitedCount);
    }

    // Validation Methods
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
            throw new Exception("Response time exceeded. Expected: " + maxTimeMs + "ms, Actual: " + actualTime + "ms");
        }
    }

    public static void validateSecurityHeaders(Response response) {
        logger.info("Validating security headers");
        
        // Check for security headers
        String contentType = response.getHeader("Content-Type");
        String xFrameOptions = response.getHeader("X-Frame-Options");
        String xContentTypeOptions = response.getHeader("X-Content-Type-Options");
        String xXSSProtection = response.getHeader("X-XSS-Protection");
        String strictTransportSecurity = response.getHeader("Strict-Transport-Security");
        
        if (contentType == null || !contentType.contains("application/json")) {
            logger.warn("Content-Type header missing or incorrect");
        }
        
        if (xFrameOptions == null) {
            logger.warn("X-Frame-Options header missing");
        }
        
        if (xContentTypeOptions == null) {
            logger.warn("X-Content-Type-Options header missing");
        }
        
        if (xXSSProtection == null) {
            logger.warn("X-XSS-Protection header missing");
        }
        
        if (strictTransportSecurity == null) {
            logger.warn("Strict-Transport-Security header missing");
        }
    }

    // Performance Testing
    public static void performanceTest(String endpoint, int threadCount, int requestsPerThread) throws Exception {
        logger.info("Starting performance test - Threads: {}, Requests per thread: {}", 
                threadCount, requestsPerThread);
        
        long startTime = System.currentTimeMillis();
        
        // This is a simplified performance test
        // In a real scenario, you'd use proper load testing tools
        for (int i = 0; i < requestsPerThread; i++) {
            Response response = get(endpoint);
            if (response.getStatusCode() != 200) {
                logger.warn("Performance test request failed with status: {}", response.getStatusCode());
            }
        }
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        logger.info("Performance test completed in {}ms", totalTime);
    }

    // Clear authentication
    public static void clearAuthentication() {
        authToken = null;
        apiKey = null;
        basicAuthUsername = null;
        basicAuthPassword = null;
        bearerToken = null;
        oauthToken = null;
        logger.info("All authentication credentials cleared");
    }
}