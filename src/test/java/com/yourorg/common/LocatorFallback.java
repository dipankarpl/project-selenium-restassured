package com.yourorg.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.List;

public class LocatorFallback {
    private static final Logger logger = LogManager.getLogger(LocatorFallback.class);
    private final WebDriver driver;
    private final WebDriverWait wait;

    public LocatorFallback(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public WebElement findElementWithFallback(List<By> locators) {
        return findElementWithFallback(locators, 10);
    }

    public WebElement findElementWithFallback(List<By> locators, int timeoutSeconds) {
        WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        
        for (int i = 0; i < locators.size(); i++) {
            By locator = locators.get(i);
            try {
                WebElement element = customWait.until(ExpectedConditions.presenceOfElementLocated(locator));
                if (i > 0) {
                    logger.info("Found element using fallback locator #{}: {}", i + 1, locator.toString());
                }
                return element;
            } catch (Exception e) {
                logger.debug("Locator #{} failed: {} - Error: {}", i + 1, locator.toString(), e.getMessage());
                if (i == locators.size() - 1) {
                    logger.error("All fallback locators failed. Last error: {}", e.getMessage());
                    throw new NoSuchElementException("Element not found with any of the provided locators");
                }
            }
        }
        
        throw new NoSuchElementException("Element not found with any of the provided locators");
    }

    public WebElement findClickableElementWithFallback(List<By> locators) {
        return findClickableElementWithFallback(locators, 10);
    }

    public WebElement findClickableElementWithFallback(List<By> locators, int timeoutSeconds) {
        WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        
        for (int i = 0; i < locators.size(); i++) {
            By locator = locators.get(i);
            try {
                WebElement element = customWait.until(ExpectedConditions.elementToBeClickable(locator));
                if (i > 0) {
                    logger.info("Found clickable element using fallback locator #{}: {}", i + 1, locator.toString());
                }
                return element;
            } catch (Exception e) {
                logger.debug("Clickable locator #{} failed: {} - Error: {}", i + 1, locator.toString(), e.getMessage());
                if (i == locators.size() - 1) {
                    logger.error("All fallback locators failed for clickable element. Last error: {}", e.getMessage());
                    throw new NoSuchElementException("Clickable element not found with any of the provided locators");
                }
            }
        }
        
        throw new NoSuchElementException("Clickable element not found with any of the provided locators");
    }

    public List<WebElement> findElementsWithFallback(List<By> locators) {
        for (int i = 0; i < locators.size(); i++) {
            By locator = locators.get(i);
            try {
                List<WebElement> elements = driver.findElements(locator);
                if (!elements.isEmpty()) {
                    if (i > 0) {
                        logger.info("Found elements using fallback locator #{}: {}", i + 1, locator.toString());
                    }
                    return elements;
                }
            } catch (Exception e) {
                logger.debug("Elements locator #{} failed: {} - Error: {}", i + 1, locator.toString(), e.getMessage());
            }
        }
        
        logger.warn("No elements found with any of the provided locators");
        return List.of(); // Return empty list instead of throwing exception
    }

    public boolean isElementPresentWithFallback(List<By> locators) {
        for (By locator : locators) {
            try {
                driver.findElement(locator);
                return true;
            } catch (NoSuchElementException e) {
                // Continue to next locator
            }
        }
        return false;
    }

    public boolean waitForElementWithFallback(List<By> locators, int timeoutSeconds) {
        WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        
        for (By locator : locators) {
            try {
                customWait.until(ExpectedConditions.presenceOfElementLocated(locator));
                return true;
            } catch (Exception e) {
                // Continue to next locator
            }
        }
        return false;
    }
}