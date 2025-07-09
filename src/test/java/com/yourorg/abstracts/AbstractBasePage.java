package com.yourorg.abstracts;

import com.yourorg.browser.BrowserUtils;
import com.yourorg.common.LocatorFallback;
import com.yourorg.interfaces.IPageObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

/**
 * Abstract base class for all page objects
 * Implements common functionality and enforces structure
 */
public abstract class AbstractBasePage implements IPageObject {
    protected static final Logger logger = LogManager.getLogger(AbstractBasePage.class);
    protected final WebDriver driver;
    protected final BrowserUtils browserUtils;
    protected final LocatorFallback locatorFallback;
    
    protected AbstractBasePage(WebDriver driver) throws Exception {
        this.driver = driver;
        this.browserUtils = new BrowserUtils(driver);
        this.locatorFallback = new LocatorFallback(driver);
        logger.debug("Initialized page object: {}", this.getClass().getSimpleName());
    }
    
    @Override
    public String getPageTitle() {
        return driver.getTitle();
    }
    
    @Override
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
    
    @Override
    public void refreshPage() {
        driver.navigate().refresh();
        logger.info("Page refreshed: {}", this.getClass().getSimpleName());
    }
    
    @Override
    public boolean isPageLoaded() {
        try {
            waitForPageToLoad();
            return true;
        } catch (Exception e) {
            logger.warn("Page not loaded: {}", e.getMessage());
            return false;
        }
    }
    
    // Abstract methods that must be implemented by concrete pages
    @Override
    public abstract void waitForPageToLoad() throws Exception;
    
    // Template method pattern - defines the skeleton of page validation
    public final boolean validatePage() {
        try {
            return validatePageElements() && validatePageUrl() && validatePageTitle();
        } catch (Exception e) {
            logger.error("Page validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    protected abstract boolean validatePageElements();
    protected abstract boolean validatePageUrl();
    protected abstract boolean validatePageTitle();
}