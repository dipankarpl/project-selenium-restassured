package com.yourorg.exceptions;

/**
 * Exception for page object related errors
 */
public class PageObjectException extends FrameworkException {
    
    public PageObjectException(String message) {
        super(message, "PAGE_OBJECT_ERROR", "PAGE_OBJECT");
    }
    
    public PageObjectException(String message, Throwable cause) {
        super(message, cause, "PAGE_OBJECT_ERROR", "PAGE_OBJECT");
    }
    
    public PageObjectException(String message, String errorCode) {
        super(message, errorCode, "PAGE_OBJECT");
    }
}