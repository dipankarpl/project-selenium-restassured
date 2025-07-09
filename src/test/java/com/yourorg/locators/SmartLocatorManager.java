package com.yourorg.locators;

import com.yourorg.common.LocatorFallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Smart locator manager that uses multiple strategies with fallback
 * Demonstrates strategy pattern and composition
 */
public class SmartLocatorManager {
    private static final Logger logger = LogManager.getLogger(SmartLocatorManager.class);
    private final WebDriver driver;
    private final LocatorFallback locatorFallback;
    private final List<LocatorStrategy> strategies;
    
    public SmartLocatorManager(WebDriver driver) {
        this.driver = driver;
        this.locatorFallback = new LocatorFallback(driver);
        this.strategies = new ArrayList<>();
    }
    
    public void addStrategy(LocatorStrategy strategy) {
        strategies.add(strategy);
        // Sort strategies by priority
        strategies.sort(Comparator.comparingInt(LocatorStrategy::getPriority));
        logger.debug("Added locator strategy: {}", strategy.getDescription());
    }
    
    public WebElement findElement(String elementName) throws Exception {
        List<By> allLocators = strategies.stream()
                .flatMap(strategy -> strategy.getLocators().stream())
                .collect(Collectors.toList());
        
        if (allLocators.isEmpty()) {
            throw new Exception("No locator strategies defined for element: " + elementName);
        }
        
        try {
            WebElement element = locatorFallback.findElementWithFallback(allLocators);
            logger.info("Element found using smart locator manager: {}", elementName);
            return element;
        } catch (Exception e) {
            logger.error("Failed to find element '{}' using all strategies: {}", elementName, e.getMessage());
            throw new Exception("Element not found: " + elementName, e);
        }
    }
    
    public WebElement findClickableElement(String elementName) throws Exception {
        List<By> allLocators = strategies.stream()
                .flatMap(strategy -> strategy.getLocators().stream())
                .collect(Collectors.toList());
        
        if (allLocators.isEmpty()) {
            throw new Exception("No locator strategies defined for element: " + elementName);
        }
        
        try {
            WebElement element = locatorFallback.findClickableElementWithFallback(allLocators);
            logger.info("Clickable element found using smart locator manager: {}", elementName);
            return element;
        } catch (Exception e) {
            logger.error("Failed to find clickable element '{}' using all strategies: {}", elementName, e.getMessage());
            throw new Exception("Clickable element not found: " + elementName, e);
        }
    }
    
    public List<WebElement> findElements(String elementName) {
        List<By> allLocators = strategies.stream()
                .flatMap(strategy -> strategy.getLocators().stream())
                .collect(Collectors.toList());
        
        if (allLocators.isEmpty()) {
            logger.warn("No locator strategies defined for elements: {}", elementName);
            return new ArrayList<>();
        }
        
        List<WebElement> elements = locatorFallback.findElementsWithFallback(allLocators);
        logger.info("Found {} elements using smart locator manager: {}", elements.size(), elementName);
        return elements;
    }
    
    public boolean isElementPresent(String elementName) {
        List<By> allLocators = strategies.stream()
                .flatMap(strategy -> strategy.getLocators().stream())
                .collect(Collectors.toList());
        
        if (allLocators.isEmpty()) {
            return false;
        }
        
        return locatorFallback.isElementPresentWithFallback(allLocators);
    }
    
    public void clearStrategies() {
        strategies.clear();
        logger.info("All locator strategies cleared");
    }
    
    public List<String> getStrategyDescriptions() {
        return strategies.stream()
                .map(LocatorStrategy::getDescription)
                .collect(Collectors.toList());
    }
}