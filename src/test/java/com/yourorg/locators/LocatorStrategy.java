package com.yourorg.locators;

import org.openqa.selenium.By;

import java.util.List;

/**
 * Locator strategy interface for different locator approaches
 */
public interface LocatorStrategy {
    List<By> getLocators();
    String getDescription();
    int getPriority(); // Lower number = higher priority
}