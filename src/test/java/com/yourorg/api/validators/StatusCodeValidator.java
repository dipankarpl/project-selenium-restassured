package com.yourorg.api.validators;

import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Comprehensive status code validator with detailed explanations
 */
public class StatusCodeValidator {
    private static final Logger logger = LogManager.getLogger(StatusCodeValidator.class);
    
    /**
     * Validate and explain HTTP status codes
     */
    public static void validateAndExplain(Response response, int expectedStatus) throws Exception {
        int actualStatus = response.getStatusCode();
        
        if (actualStatus != expectedStatus) {
            String explanation = getStatusCodeExplanation(actualStatus);
            String expectedExplanation = getStatusCodeExplanation(expectedStatus);
            
            logger.error("Status code mismatch:");
            logger.error("Expected: {} - {}", expectedStatus, expectedExplanation);
            logger.error("Actual: {} - {}", actualStatus, explanation);
            
            throw new Exception(String.format(
                "Status code mismatch. Expected: %d (%s), Actual: %d (%s)",
                expectedStatus, expectedExplanation, actualStatus, explanation
            ));
        }
    }
    
    /**
     * Get detailed explanation of status codes
     */
    public static String getStatusCodeExplanation(int statusCode) {
        switch (statusCode) {
            // 2xx Success
            case 200:
                return "OK - Request successful, response contains data";
            case 201:
                return "Created - Resource successfully created";
            case 202:
                return "Accepted - Request accepted for processing but not completed";
            case 204:
                return "No Content - Request successful but no content to return (common for DELETE)";
            
            // 3xx Redirection
            case 301:
                return "Moved Permanently - Resource has been moved to new URL";
            case 302:
                return "Found - Resource temporarily moved";
            case 304:
                return "Not Modified - Resource hasn't changed since last request";
            
            // 4xx Client Errors
            case 400:
                return "Bad Request - Invalid request syntax or parameters";
            case 401:
                return "Unauthorized - Authentication required or invalid credentials";
            case 403:
                return "Forbidden - Valid credentials but insufficient permissions";
            case 404:
                return "Not Found - Resource doesn't exist";
            case 405:
                return "Method Not Allowed - HTTP method not supported for this endpoint";
            case 409:
                return "Conflict - Request conflicts with current state (duplicate resource)";
            case 422:
                return "Unprocessable Entity - Request syntax correct but semantically invalid";
            case 429:
                return "Too Many Requests - Rate limit exceeded";
            
            // 5xx Server Errors
            case 500:
                return "Internal Server Error - Generic server error";
            case 501:
                return "Not Implemented - Server doesn't support the functionality";
            case 502:
                return "Bad Gateway - Invalid response from upstream server";
            case 503:
                return "Service Unavailable - Server temporarily unavailable";
            case 504:
                return "Gateway Timeout - Upstream server timeout";
            
            default:
                return "Unknown status code: " + statusCode;
        }
    }
    
    /**
     * Validate specific status code scenarios
     */
    public static void validateCreationResponse(Response response) throws Exception {
        validateAndExplain(response, 201);
        
        // Additional validations for creation
        String location = response.getHeader("Location");
        if (location == null) {
            logger.warn("Location header missing in creation response");
        }
        
        String resourceId = response.jsonPath().getString("id");
        if (resourceId == null) {
            logger.warn("Resource ID missing in creation response");
        }
    }
    
    public static void validateDeletionResponse(Response response) throws Exception {
        int statusCode = response.getStatusCode();
        
        // DELETE can return 200, 202, or 204
        if (statusCode != 200 && statusCode != 202 && statusCode != 204) {
            String explanation = getStatusCodeExplanation(statusCode);
            throw new Exception(String.format(
                "Invalid deletion status code: %d (%s). Expected: 200, 202, or 204",
                statusCode, explanation
            ));
        }
        
        logger.info("Deletion successful with status: {} - {}", 
                statusCode, getStatusCodeExplanation(statusCode));
    }
    
    public static void validateAuthenticationFailure(Response response) throws Exception {
        int statusCode = response.getStatusCode();
        
        if (statusCode == 401) {
            logger.info("Authentication failed as expected: 401 - Unauthorized");
        } else if (statusCode == 403) {
            logger.info("Authorization failed as expected: 403 - Forbidden");
        } else {
            throw new Exception(String.format(
                "Expected authentication/authorization failure (401/403), got: %d (%s)",
                statusCode, getStatusCodeExplanation(statusCode)
            ));
        }
    }
    
    public static void validateMethodNotAllowed(Response response) throws Exception {
        validateAndExplain(response, 405);
        
        // Check for Allow header
        String allowHeader = response.getHeader("Allow");
        if (allowHeader != null) {
            logger.info("Allowed methods: {}", allowHeader);
        }
    }
    
    public static void validateRateLimitExceeded(Response response) throws Exception {
        validateAndExplain(response, 429);
        
        // Check for rate limit headers
        String retryAfter = response.getHeader("Retry-After");
        String rateLimitRemaining = response.getHeader("X-RateLimit-Remaining");
        String rateLimitReset = response.getHeader("X-RateLimit-Reset");
        
        if (retryAfter != null) {
            logger.info("Retry after: {} seconds", retryAfter);
        }
        if (rateLimitRemaining != null) {
            logger.info("Rate limit remaining: {}", rateLimitRemaining);
        }
        if (rateLimitReset != null) {
            logger.info("Rate limit reset time: {}", rateLimitReset);
        }
    }
}