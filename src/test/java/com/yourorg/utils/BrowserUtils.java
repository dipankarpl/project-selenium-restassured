package com.yourorg.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class BrowserUtils {
    private static final Logger logger = LogManager.getLogger(BrowserUtils.class);
    private final WebDriver driver;
    private final WebDriverWait wait;

    public BrowserUtils(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    // Click Actions
    public void click(By locator) {
        try {
            WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
            element.click();
            logger.debug("Clicked on element: {}", locator);
        } catch (Exception e) {
            logger.error("Failed to click element {}: {}", locator, e.getMessage());
            throw new RuntimeException("Click failed", e);
        }
    }

    public void clickWithFallback(By... locators) {
        for (By locator : locators) {
            try {
                click(locator);
                return;
            } catch (Exception e) {
                logger.debug("Locator {} failed, trying next", locator);
            }
        }
        throw new RuntimeException("All fallback locators failed");
    }

    // Text Actions
    public void sendKeys(By locator, String text) {
        try {
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            element.clear();
            element.sendKeys(text);
            logger.debug("Entered text '{}' into element: {}", text, locator);
        } catch (Exception e) {
            logger.error("Failed to enter text into element {}: {}", locator, e.getMessage());
            throw new RuntimeException("Send keys failed", e);
        }
    }

    public void sendKeysWithFallback(String text, By... locators) {
        for (By locator : locators) {
            try {
                sendKeys(locator, text);
                return;
            } catch (Exception e) {
                logger.debug("Locator {} failed for sendKeys, trying next", locator);
            }
        }
        throw new RuntimeException("All fallback locators failed for sendKeys");
    }

    public String getText(By locator) {
        try {
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            String text = element.getText();
            logger.debug("Retrieved text '{}' from element: {}", text, locator);
            return text;
        } catch (Exception e) {
            logger.error("Failed to get text from element {}: {}", locator, e.getMessage());
            throw new RuntimeException("Get text failed", e);
        }
    }

    public String getTextWithFallback(By... locators) {
        for (By locator : locators) {
            try {
                return getText(locator);
            } catch (Exception e) {
                logger.debug("Locator {} failed for getText, trying next", locator);
            }
        }
        throw new RuntimeException("All fallback locators failed for getText");
    }

    // Wait Actions
    public WebElement waitForElement(By locator, int timeoutSeconds) {
        try {
            WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            return customWait.until(ExpectedConditions.presenceOfElementLocated(locator));
        } catch (Exception e) {
            logger.error("Element not found: {} within {} seconds", locator, timeoutSeconds);
            throw new RuntimeException("Element not found", e);
        }
    }

    public boolean waitForElementToBeVisible(By locator, int timeoutSeconds) {
        try {
            WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            customWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Utility Methods
    public boolean isElementDisplayed(By locator) {
        try {
            WebElement element = driver.findElement(locator);
            return element.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isElementEnabled(By locator) {
        try {
            WebElement element = driver.findElement(locator);
            return element.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public void scrollToElement(By locator) {
        try {
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
            logger.debug("Scrolled to element: {}", locator);
        } catch (Exception e) {
            logger.error("Failed to scroll to element {}: {}", locator, e.getMessage());
            throw new RuntimeException("Scroll failed", e);
        }
    }

    public void clearField(By locator) {
        try {
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            element.clear();
            logger.debug("Cleared field: {}", locator);
        } catch (Exception e) {
            logger.error("Failed to clear field {}: {}", locator, e.getMessage());
            throw new RuntimeException("Clear field failed", e);
        }
    }

    public void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Sleep interrupted");
        }
    }
}