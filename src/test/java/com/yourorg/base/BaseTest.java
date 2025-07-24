package com.yourorg.base;

import com.yourorg.listeners.TestListener;
import com.yourorg.utils.ConfigLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.*;

@Listeners({TestListener.class})
public class BaseTest {
    private static final Logger logger = LogManager.getLogger(BaseTest.class);
    protected WebDriver driver;

    @BeforeMethod
    public void setup() {
        logger.info("Setting up test");
        try {
            driver = WebDriverFactory.createDriver();
            logger.info("Driver created successfully");
        } catch (Exception e) {
            logger.error("Failed to create driver: {}", e.getMessage());
            throw new RuntimeException("Driver creation failed", e);
        }
    }

    @AfterMethod
    public void teardown() {
        logger.info("Tearing down test");
        try {
            if (driver != null) {
                WebDriverFactory.quitDriver();
                logger.info("Driver quit successfully");
            }
        } catch (Exception e) {
            logger.error("Error during teardown: {}", e.getMessage());
        }
    }

    protected void navigateToUrl(String url) {
        if (driver != null) {
            logger.info("Navigating to URL: {}", url);
            driver.get(url);
        } else {
            throw new RuntimeException("Driver is null");
        }
    }

    protected void navigateToBaseUrl() {
        String baseUrl = ConfigLoader.get("app.base.url");
        if (baseUrl != null) {
            navigateToUrl(baseUrl);
        } else {
            throw new RuntimeException("Base URL not configured");
        }
    }

    protected String getCurrentUrl() {
        return driver != null ? driver.getCurrentUrl() : "";
    }

    protected String getPageTitle() {
        return driver != null ? driver.getTitle() : "";
    }
}