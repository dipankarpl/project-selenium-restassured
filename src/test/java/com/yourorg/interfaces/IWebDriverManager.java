package com.yourorg.interfaces;

import org.openqa.selenium.WebDriver;

/**
 * Interface for WebDriver management operations
 * Demonstrates abstraction and interface segregation principle
 */
public interface IWebDriverManager {
    WebDriver createDriver();
    void quitDriver();
    WebDriver getDriver();
    void configureDriver(WebDriver driver);
    boolean isDriverActive();
}