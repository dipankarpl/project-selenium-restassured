package com.yourorg.exceptions;

/**
 * Base exception class for the framework
 * Demonstrates exception hierarchy and encapsulation
 */
public class FrameworkException extends Exception {
    private final String errorCode;
    private final String component;
    
    public FrameworkException(String message) {
        super(message);
        this.errorCode = "FRAMEWORK_ERROR";
        this.component = "UNKNOWN";
    }
    
    public FrameworkException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "FRAMEWORK_ERROR";
        this.component = "UNKNOWN";
    }
    
    public FrameworkException(String message, String errorCode, String component) {
        super(message);
        this.errorCode = errorCode;
        this.component = component;
    }
    
    public FrameworkException(String message, Throwable cause, String errorCode, String component) {
        super(message, cause);
        this.errorCode = errorCode;
        this.component = component;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getComponent() {
        return component;
    }
    
    @Override
    public String toString() {
        return String.format("FrameworkException[%s:%s] - %s", component, errorCode, getMessage());
    }
}