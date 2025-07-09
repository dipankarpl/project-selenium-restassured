package com.yourorg.browser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class BrowserUtils {
    private static final Logger logger = LogManager.getLogger(BrowserUtils.class);
    private static final int DEFAULT_TIMEOUT = 10;

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final Actions actions;

    public BrowserUtils(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT));
        this.actions = new Actions(driver);
    }
    
    // Overloaded constructor with custom timeout
    public BrowserUtils(WebDriver driver, int timeoutSeconds) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        this.actions = new Actions(driver);
    }
    
    // Method to create custom wait with different timeout
    private WebDriverWait createCustomWait(int timeoutSeconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
    }

    // Click Actions
    public void click(WebElement element) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element));
            element.click();
            logger.debug("Clicked on element: {}", element.toString());
        } catch (Exception e) {
            logger.error("Failed to click element: {}", e.getMessage());
            throw new RuntimeException("Click failed", e);
        }
    }

    public void click(By locator) {
        try {
            WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
            element.click();
            logger.debug("Clicked on element with locator: {}", locator.toString());
        } catch (Exception e) {
            logger.error("Failed to click element with locator {}: {}", locator.toString(), e.getMessage());
            throw new RuntimeException("Click failed", e);
        }
    }

    public void click(By locator, int timeoutSeconds) {
        try {
            WebDriverWait customWait = createCustomWait(timeoutSeconds);
            WebElement element = customWait.until(ExpectedConditions.elementToBeClickable(locator));
            element.click();
            logger.debug("Clicked on element with locator: {} (timeout: {}s)", locator.toString(), timeoutSeconds);
        } catch (Exception e) {
            logger.error("Failed to click element with locator {} (timeout: {}s): {}", 
                    locator.toString(), timeoutSeconds, e.getMessage());
            throw new RuntimeException("Click failed", e);
        }
    }
    
    public void clickWithFallback(By... locators) {
        for (By locator : locators) {
            try {
                click(locator);
                return; // Success, exit method
            } catch (Exception e) {
                logger.debug("Locator {} failed, trying next fallback", locator.toString());
            }
        }
        throw new RuntimeException("All fallback locators failed: " + Arrays.toString(locators));
    }

    public void javascriptClick(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
            logger.debug("JavaScript click performed on element: {}", element.toString());
        } catch (Exception e) {
            logger.error("Failed to perform JavaScript click: {}", e.getMessage());
            throw new RuntimeException("JavaScript click failed", e);
        }
    }

    public void javascriptClick(By locator) {
        try {
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
            logger.debug("JavaScript click performed on element with locator: {}", locator.toString());
        } catch (Exception e) {
            logger.error("Failed to perform JavaScript click with locator {}: {}", 
                    locator.toString(), e.getMessage());
            throw new RuntimeException("JavaScript click failed", e);
        }
    }

    public void doubleClick(WebElement element) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element));
            actions.doubleClick(element).perform();
            logger.debug("Double clicked on element: {}", element.toString());
        } catch (Exception e) {
            logger.error("Failed to double click element: {}", e.getMessage());
            throw new RuntimeException("Double click failed", e);
        }
    }

    public void doubleClick(By locator) {
        try {
            WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
            actions.doubleClick(element).perform();
            logger.debug("Double clicked on element with locator: {}", locator.toString());
        } catch (Exception e) {
            logger.error("Failed to double click element with locator {}: {}", 
                    locator.toString(), e.getMessage());
            throw new RuntimeException("Double click failed", e);
        }
    }

    public void rightClick(WebElement element) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element));
            actions.contextClick(element).perform();
            logger.debug("Right clicked on element: {}", element.toString());
        } catch (Exception e) {
            logger.error("Failed to right click element: {}", e.getMessage());
            throw new RuntimeException("Right click failed", e);
        }
    }

    public void rightClick(By locator) {
        try {
            WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
            actions.contextClick(element).perform();
            logger.debug("Right clicked on element with locator: {}", locator.toString());
        } catch (Exception e) {
            logger.error("Failed to right click element with locator {}: {}", 
                    locator.toString(), e.getMessage());
            throw new RuntimeException("Right click failed", e);
        }
    }

    // Text Actions
    public void sendKeys(WebElement element, String text) {
        try {
            wait.until(ExpectedConditions.visibilityOf(element));
            element.clear();
            element.sendKeys(text);
            logger.debug("Entered text '{}' into element: {}", text, element.toString());
        } catch (Exception e) {
            logger.error("Failed to enter text into element: {}", e.getMessage());
            throw new RuntimeException("Send keys failed", e);
        }
    }

    public void sendKeys(By locator, String text) {
        try {
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            element.clear();
            element.sendKeys(text);
            logger.debug("Entered text '{}' into element with locator: {}", text, locator.toString());
        } catch (Exception e) {
            logger.error("Failed to enter text into element with locator {}: {}", locator.toString(), e.getMessage());
            throw new RuntimeException("Send keys failed", e);
        }
    }

    public void sendKeys(By locator, String text, int timeoutSeconds) {
        try {
            WebDriverWait customWait = createCustomWait(timeoutSeconds);
            WebElement element = customWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            element.clear();
            element.sendKeys(text);
            logger.debug("Entered text '{}' into element with locator: {} (timeout: {}s)", 
                    text, locator.toString(), timeoutSeconds);
        } catch (Exception e) {
            logger.error("Failed to enter text into element with locator {} (timeout: {}s): {}", 
                    locator.toString(), timeoutSeconds, e.getMessage());
            throw new RuntimeException("Send keys failed", e);
        }
    }
    
    public void sendKeysWithFallback(String text, By... locators) {
        for (By locator : locators) {
            try {
                sendKeys(locator, text);
                return; // Success, exit method
            } catch (Exception e) {
                logger.debug("Locator {} failed for sendKeys, trying next fallback", locator.toString());
            }
        }
        throw new RuntimeException("All fallback locators failed for sendKeys: " + Arrays.toString(locators));
    }

    public void sendKeysWithoutClear(WebElement element, String text) {
        try {
            wait.until(ExpectedConditions.visibilityOf(element));
            element.sendKeys(text);
            logger.debug("Appended text '{}' to element: {}", text, element.toString());
        } catch (Exception e) {
            logger.error("Failed to append text to element: {}", e.getMessage());
            throw new RuntimeException("Send keys failed", e);
        }
    }

    public void sendKeysWithoutClear(By locator, String text) {
        try {
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            element.sendKeys(text);
            logger.debug("Appended text '{}' to element with locator: {}", text, locator.toString());
        } catch (Exception e) {
            logger.error("Failed to append text to element with locator {}: {}", 
                    locator.toString(), e.getMessage());
            throw new RuntimeException("Send keys failed", e);
        }
    }

    public String getText(WebElement element) {
        try {
            wait.until(ExpectedConditions.visibilityOf(element));
            String text = element.getText();
            logger.debug("Retrieved text '{}' from element: {}", text, element.toString());
            return text;
        } catch (Exception e) {
            logger.error("Failed to get text from element: {}", e.getMessage());
            throw new RuntimeException("Get text failed", e);
        }
    }

    public String getText(By locator) {
        try {
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            String text = element.getText();
            logger.debug("Retrieved text '{}' from element with locator: {}", text, locator.toString());
            return text;
        } catch (Exception e) {
            logger.error("Failed to get text from element with locator {}: {}", locator.toString(), e.getMessage());
            throw new RuntimeException("Get text failed", e);
        }
    }

    public String getTextWithFallback(By... locators) {
        for (By locator : locators) {
            try {
                return getText(locator);
            } catch (Exception e) {
                logger.debug("Locator {} failed for getText, trying next fallback", locator.toString());
            }
        }
        throw new RuntimeException("All fallback locators failed for getText: " + Arrays.toString(locators));
    }
    
    public String getText(By locator, int timeoutSeconds) {
        try {
            WebDriverWait customWait = createCustomWait(timeoutSeconds);
            WebElement element = customWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            String text = element.getText();
            logger.debug("Retrieved text '{}' from element with locator: {} (timeout: {}s)", 
                    text, locator.toString(), timeoutSeconds);
            return text;
        } catch (Exception e) {
            logger.error("Failed to get text from element with locator {} (timeout: {}s): {}", 
                    locator.toString(), timeoutSeconds, e.getMessage());
            throw new RuntimeException("Get text failed", e);
        }
    }

    // Attribute Actions
    public String getAttribute(WebElement element, String attributeName) {
        try {
            wait.until(ExpectedConditions.visibilityOf(element));
            String attributeValue = element.getAttribute(attributeName);
            logger.debug("Retrieved attribute '{}' = '{}' from element: {}", attributeName, attributeValue, element.toString());
            return attributeValue;
        } catch (Exception e) {
            logger.error("Failed to get attribute '{}' from element: {}", attributeName, e.getMessage());
            throw new RuntimeException("Get attribute failed", e);
        }
    }

    public String getAttribute(By locator, String attributeName) {
        try {
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            String attributeValue = element.getAttribute(attributeName);
            logger.debug("Retrieved attribute '{}' = '{}' from element with locator: {}", 
                    attributeName, attributeValue, locator.toString());
            return attributeValue;
        } catch (Exception e) {
            logger.error("Failed to get attribute '{}' from element with locator {}: {}", 
                    attributeName, locator.toString(), e.getMessage());
            throw new RuntimeException("Get attribute failed", e);
        }
    }

    // Wait Actions
    public WebElement waitForElement(By locator, int timeoutSeconds) {
        try {
            WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            WebElement element = customWait.until(ExpectedConditions.presenceOfElementLocated(locator));
            logger.debug("Element found with locator: {}", locator.toString());
            return element;
        } catch (Exception e) {
            logger.error("Element not found with locator {} within {} seconds: {}", locator.toString(), timeoutSeconds, e.getMessage());
            throw new RuntimeException("Element not found", e);
        }
    }

    public WebElement waitForElementToBeClickable(By locator, int timeoutSeconds) {
        try {
            WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            WebElement element = customWait.until(ExpectedConditions.elementToBeClickable(locator));
            logger.debug("Element clickable with locator: {}", locator.toString());
            return element;
        } catch (Exception e) {
            logger.error("Element not clickable with locator {} within {} seconds: {}", locator.toString(), timeoutSeconds, e.getMessage());
            throw new RuntimeException("Element not clickable", e);
        }
    }

    public boolean waitForElementToDisappear(By locator, int timeoutSeconds) {
        try {
            WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            return customWait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
        } catch (Exception e) {
            logger.warn("Element did not disappear with locator {} within {} seconds", locator.toString(), timeoutSeconds);
            return false;
        }
    }

    public boolean waitForElementToDisappear(By locator) {
        return waitForElementToDisappear(locator, DEFAULT_TIMEOUT);
    }
    
    public boolean waitForElementToBeVisible(By locator, int timeoutSeconds) {
        try {
            WebDriverWait customWait = createCustomWait(timeoutSeconds);
            customWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Dropdown Actions
    public void selectDropdownByValue(WebElement element, String value) {
        try {
            wait.until(ExpectedConditions.visibilityOf(element));
            Select select = new Select(element);
            select.selectByValue(value);
            logger.debug("Selected dropdown option by value: {}", value);
        } catch (Exception e) {
            logger.error("Failed to select dropdown option by value '{}': {}", value, e.getMessage());
            throw new RuntimeException("Dropdown selection failed", e);
        }
    }

    public void selectDropdownByValue(By locator, String value) {
        try {
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            Select select = new Select(element);
            select.selectByValue(value);
            logger.debug("Selected dropdown option by value: {} for locator: {}", value, locator.toString());
        } catch (Exception e) {
            logger.error("Failed to select dropdown option by value '{}' for locator {}: {}", 
                    value, locator.toString(), e.getMessage());
            throw new RuntimeException("Dropdown selection failed", e);
        }
    }

    public void selectDropdownByText(WebElement element, String text) {
        try {
            wait.until(ExpectedConditions.visibilityOf(element));
            Select select = new Select(element);
            select.selectByVisibleText(text);
            logger.debug("Selected dropdown option by text: {}", text);
        } catch (Exception e) {
            logger.error("Failed to select dropdown option by text '{}': {}", text, e.getMessage());
            throw new RuntimeException("Dropdown selection failed", e);
        }
    }

    public void selectDropdownByText(By locator, String text) {
        try {
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            Select select = new Select(element);
            select.selectByVisibleText(text);
            logger.debug("Selected dropdown option by text: {} for locator: {}", text, locator.toString());
        } catch (Exception e) {
            logger.error("Failed to select dropdown option by text '{}' for locator {}: {}", 
                    text, locator.toString(), e.getMessage());
            throw new RuntimeException("Dropdown selection failed", e);
        }
    }

    public void selectDropdownByIndex(WebElement element, int index) {
        try {
            wait.until(ExpectedConditions.visibilityOf(element));
            Select select = new Select(element);
            select.selectByIndex(index);
            logger.debug("Selected dropdown option by index: {}", index);
        } catch (Exception e) {
            logger.error("Failed to select dropdown option by index '{}': {}", index, e.getMessage());
            throw new RuntimeException("Dropdown selection failed", e);
        }
    }

    public void selectDropdownByIndex(By locator, int index) {
        try {
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            Select select = new Select(element);
            select.selectByIndex(index);
            logger.debug("Selected dropdown option by index: {} for locator: {}", index, locator.toString());
        } catch (Exception e) {
            logger.error("Failed to select dropdown option by index '{}' for locator {}: {}", 
                    index, locator.toString(), e.getMessage());
            throw new RuntimeException("Dropdown selection failed", e);
        }
    }

    // Scroll Actions
    public void scrollToElement(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
            logger.debug("Scrolled to element: {}", element.toString());
        } catch (Exception e) {
            logger.error("Failed to scroll to element: {}", e.getMessage());
            throw new RuntimeException("Scroll failed", e);
        }
    }

    public void scrollToElement(By locator) {
        try {
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
            logger.debug("Scrolled to element with locator: {}", locator.toString());
        } catch (Exception e) {
            logger.error("Failed to scroll to element with locator {}: {}", locator.toString(), e.getMessage());
            throw new RuntimeException("Scroll failed", e);
        }
    }

    public void scrollToTop() {
        try {
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
            logger.debug("Scrolled to top of page");
        } catch (Exception e) {
            logger.error("Failed to scroll to top: {}", e.getMessage());
            throw new RuntimeException("Scroll failed", e);
        }
    }

    public void scrollToBottom() {
        try {
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
            logger.debug("Scrolled to bottom of page");
        } catch (Exception e) {
            logger.error("Failed to scroll to bottom: {}", e.getMessage());
            throw new RuntimeException("Scroll failed", e);
        }
    }

    // Mouse Actions
    public void moveToElement(WebElement element) {
        try {
            actions.moveToElement(element).perform();
            logger.debug("Moved to element: {}", element.toString());
        } catch (Exception e) {
            logger.error("Failed to move to element: {}", e.getMessage());
            throw new RuntimeException("Move to element failed", e);
        }
    }

    public void moveToElement(By locator) {
        try {
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
            actions.moveToElement(element).perform();
            logger.debug("Moved to element with locator: {}", locator.toString());
        } catch (Exception e) {
            logger.error("Failed to move to element with locator {}: {}", locator.toString(), e.getMessage());
            throw new RuntimeException("Move to element failed", e);
        }
    }

    public void dragAndDrop(WebElement source, WebElement target) {
        try {
            actions.dragAndDrop(source, target).perform();
            logger.debug("Performed drag and drop from {} to {}", source.toString(), target.toString());
        } catch (Exception e) {
            logger.error("Failed to perform drag and drop: {}", e.getMessage());
            throw new RuntimeException("Drag and drop failed", e);
        }
    }

    public void dragAndDrop(By sourceLocator, By targetLocator) {
        try {
            WebElement source = wait.until(ExpectedConditions.presenceOfElementLocated(sourceLocator));
            WebElement target = wait.until(ExpectedConditions.presenceOfElementLocated(targetLocator));
            actions.dragAndDrop(source, target).perform();
            logger.debug("Performed drag and drop from {} to {}", sourceLocator.toString(), targetLocator.toString());
        } catch (Exception e) {
            logger.error("Failed to perform drag and drop from {} to {}: {}", 
                    sourceLocator.toString(), targetLocator.toString(), e.getMessage());
            throw new RuntimeException("Drag and drop failed", e);
        }
    }

    // Window Actions
    public void switchToWindow(String windowHandle) {
        try {
            driver.switchTo().window(windowHandle);
            logger.debug("Switched to window: {}", windowHandle);
        } catch (Exception e) {
            logger.error("Failed to switch to window '{}': {}", windowHandle, e.getMessage());
            throw new RuntimeException("Window switch failed", e);
        }
    }

    public void switchToNewWindow() {
        try {
            String currentWindow = driver.getWindowHandle();
            Set<String> allWindows = driver.getWindowHandles();
            for (String window : allWindows) {
                if (!window.equals(currentWindow)) {
                    driver.switchTo().window(window);
                    logger.debug("Switched to new window: {}", window);
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("Failed to switch to new window: {}", e.getMessage());
            throw new RuntimeException("Window switch failed", e);
        }
    }

    public void closeCurrentWindow() {
        try {
            driver.close();
            logger.debug("Closed current window");
        } catch (Exception e) {
            logger.error("Failed to close current window: {}", e.getMessage());
            throw new RuntimeException("Window close failed", e);
        }
    }

    // Frame Actions
    public void switchToFrame(WebElement frameElement) {
        try {
            driver.switchTo().frame(frameElement);
            logger.debug("Switched to frame: {}", frameElement.toString());
        } catch (Exception e) {
            logger.error("Failed to switch to frame: {}", e.getMessage());
            throw new RuntimeException("Frame switch failed", e);
        }
    }

    public void switchToFrame(By frameLocator) {
        try {
            WebElement frameElement = wait.until(ExpectedConditions.presenceOfElementLocated(frameLocator));
            driver.switchTo().frame(frameElement);
            logger.debug("Switched to frame with locator: {}", frameLocator.toString());
        } catch (Exception e) {
            logger.error("Failed to switch to frame with locator {}: {}", frameLocator.toString(), e.getMessage());
            throw new RuntimeException("Frame switch failed", e);
        }
    }

    public void switchToFrameByIndex(int index) {
        try {
            driver.switchTo().frame(index);
            logger.debug("Switched to frame by index: {}", index);
        } catch (Exception e) {
            logger.error("Failed to switch to frame by index '{}': {}", index, e.getMessage());
            throw new RuntimeException("Frame switch failed", e);
        }
    }

    public void switchToDefaultContent() {
        try {
            driver.switchTo().defaultContent();
            logger.debug("Switched to default content");
        } catch (Exception e) {
            logger.error("Failed to switch to default content: {}", e.getMessage());
            throw new RuntimeException("Default content switch failed", e);
        }
    }

    // Alert Actions
    public void acceptAlert() {
        try {
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            String alertText = alert.getText();
            alert.accept();
            logger.debug("Accepted alert with text: {}", alertText);
        } catch (Exception e) {
            logger.error("Failed to accept alert: {}", e.getMessage());
            throw new RuntimeException("Alert accept failed", e);
        }
    }

    public void dismissAlert() {
        try {
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            String alertText = alert.getText();
            alert.dismiss();
            logger.debug("Dismissed alert with text: {}", alertText);
        } catch (Exception e) {
            logger.error("Failed to dismiss alert: {}", e.getMessage());
            throw new RuntimeException("Alert dismiss failed", e);
        }
    }

    public String getAlertText() {
        try {
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            String alertText = alert.getText();
            logger.debug("Retrieved alert text: {}", alertText);
            return alertText;
        } catch (Exception e) {
            logger.error("Failed to get alert text: {}", e.getMessage());
            throw new RuntimeException("Get alert text failed", e);
        }
    }

    // Utility Methods
    public boolean isElementPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public boolean isElementPresent(By locator, int timeoutSeconds) {
        try {
            WebDriverWait customWait = createCustomWait(timeoutSeconds);
            customWait.until(ExpectedConditions.presenceOfElementLocated(locator));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isElementDisplayed(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isElementDisplayed(By locator) {
        try {
            WebElement element = driver.findElement(locator);
            return element.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isElementEnabled(WebElement element) {
        try {
            return element.isEnabled();
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

    public boolean isElementSelected(WebElement element) {
        try {
            return element.isSelected();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isElementSelected(By locator) {
        try {
            WebElement element = driver.findElement(locator);
            return element.isSelected();
        } catch (Exception e) {
            return false;
        }
    }

    public List<WebElement> findElements(By locator) {
        try {
            return driver.findElements(locator);
        } catch (Exception e) {
            logger.error("Failed to find elements with locator {}: {}", locator.toString(), e.getMessage());
            throw new RuntimeException("Find elements failed", e);
        }
    }

    public int getElementCount(By locator) {
        try {
            List<WebElement> elements = driver.findElements(locator);
            logger.debug("Found {} elements with locator: {}", elements.size(), locator.toString());
            return elements.size();
        } catch (Exception e) {
            logger.error("Failed to count elements with locator {}: {}", locator.toString(), e.getMessage());
            return 0;
        }
    }

    public void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Sleep interrupted: {}", e.getMessage());
        }
    }

    public void refreshPage() {
        try {
            driver.navigate().refresh();
            logger.debug("Page refreshed");
        } catch (Exception e) {
            logger.error("Failed to refresh page: {}", e.getMessage());
            throw new RuntimeException("Page refresh failed", e);
        }
    }

    public void navigateBack() {
        try {
            driver.navigate().back();
            logger.debug("Navigated back");
        } catch (Exception e) {
            logger.error("Failed to navigate back: {}", e.getMessage());
            throw new RuntimeException("Navigate back failed", e);
        }
    }

    public void navigateForward() {
        try {
            driver.navigate().forward();
            logger.debug("Navigated forward");
        } catch (Exception e) {
            logger.error("Failed to navigate forward: {}", e.getMessage());
            throw new RuntimeException("Navigate forward failed", e);
        }
    }
    
    // Utility methods for better locator handling
    public WebElement findElement(By locator) {
        try {
            return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        } catch (Exception e) {
            logger.error("Failed to find element with locator {}: {}", locator.toString(), e.getMessage());
            throw new RuntimeException("Element not found", e);
        }
    }
    
    public WebElement findElement(By locator, int timeoutSeconds) {
        try {
            WebDriverWait customWait = createCustomWait(timeoutSeconds);
            return customWait.until(ExpectedConditions.presenceOfElementLocated(locator));
        } catch (Exception e) {
            logger.error("Failed to find element with locator {} (timeout: {}s): {}", 
                    locator.toString(), timeoutSeconds, e.getMessage());
            throw new RuntimeException("Element not found", e);
        }
    }
    
    public void clearField(By locator) {
        try {
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            element.clear();
            logger.debug("Cleared field with locator: {}", locator.toString());
        } catch (Exception e) {
            logger.error("Failed to clear field with locator {}: {}", locator.toString(), e.getMessage());
            throw new RuntimeException("Clear field failed", e);
        }
    }
}