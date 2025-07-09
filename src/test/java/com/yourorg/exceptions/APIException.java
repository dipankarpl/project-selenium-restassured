package com.yourorg.exceptions;

/**
 * Exception for API related errors
 */
public class APIException extends FrameworkException {
    private final int statusCode;
    
    public APIException(String message, int statusCode) {
        super(message, "API_ERROR", "API");
        this.statusCode = statusCode;
    }
    
    public APIException(String message, Throwable cause, int statusCode) {
        super(message, cause, "API_ERROR", "API");
        this.statusCode = statusCode;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    @Override
    public String toString() {
        return String.format("APIException[%s:%s] - Status: %d - %s", 
                getComponent(), getErrorCode(), statusCode, getMessage());
    }
}