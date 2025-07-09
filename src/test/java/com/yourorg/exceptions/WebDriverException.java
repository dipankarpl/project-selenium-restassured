package com.yourorg.exceptions;

/**
 * Exception for WebDriver related errors
 */
public class WebDriverException extends FrameworkException {
    
    public WebDriverException(String message) {
        super(message, "WEBDRIVER_ERROR", "WEBDRIVER");
    }
    
    public WebDriverException(String message, Throwable cause) {
        super(message, cause, "WEBDRIVER_ERROR", "WEBDRIVER");
    }
    
    public WebDriverException(String message, String errorCode) {
        super(message, errorCode, "WEBDRIVER");
    }
}