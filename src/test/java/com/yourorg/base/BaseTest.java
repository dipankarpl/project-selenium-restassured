package com.yourorg.base;

import com.yourorg.listeners.TestListener;
import com.yourorg.utils.ConfigLoader;
import com.yourorg.utils.DBUtils;
import com.yourorg.utils.StateRetentionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.*;

@Listeners({TestListener.class})
public class BaseTest {
    private static final Logger logger = LogManager.getLogger(BaseTest.class);
    protected WebDriver driver;
    protected String testSessionId;

    @BeforeSuite
    public void beforeSuite() {
        logger.info("=== Test Suite Started ===");
        
        // Initialize database connection if configured
        try {
            DBUtils.initializeDB();
        } catch (Exception e) {
            logger.warn("Database initialization failed: {}", e.getMessage());
        }
        
        // Clear global state
        StateRetentionManager.clearGlobalState();
        
        logger.info("Suite setup completed");
    }

    @BeforeTest
    public void beforeTest() {
        logger.info("=== Test Started ===");
        testSessionId = Thread.currentThread().getName() + "-" + System.currentTimeMillis();
        StateRetentionManager.setSessionState(testSessionId, "startTime", System.currentTimeMillis());
    }

    @BeforeMethod
    public void beforeMethod() {
        logger.info("=== Test Method Started ===");
        try {
            driver = RemoteWebDriverFactory.createDriver();
            StateRetentionManager.setSessionState(testSessionId, "driver", driver);
        } catch (Exception e) {
            logger.error("Failed to create driver in beforeMethod: {}", e.getMessage());
            throw new RuntimeException("Driver creation failed", e);
        }
    }

    @AfterMethod
    public void afterMethod() {
        logger.info("=== Test Method Completed ===");
        try {
            if (driver != null) {
                RemoteWebDriverFactory.quitDriver();
            }
        } catch (Exception e) {
            logger.error("Error in afterMethod: {}", e.getMessage());
        }
    }

    @AfterTest
    public void afterTest() {
        logger.info("=== Test Completed ===");
        StateRetentionManager.clearSessionState(testSessionId);
    }

    @AfterSuite
    public void afterSuite() {
        logger.info("=== Test Suite Completed ===");
        
        // Close database connection
        try {
            DBUtils.closeConnection();
        } catch (Exception e) {
            logger.warn("Error closing database connection: {}", e.getMessage());
        }
        
        // Clear all states
        StateRetentionManager.clearAllStates();
        
        logger.info("Suite cleanup completed");
    }

    protected void navigateToUrl(String url) {
        if (driver != null) {
            logger.info("Navigating to URL: {}", url);
            driver.get(url);
        } else {
            logger.error("Driver is null. Cannot navigate to URL: {}", url);
            throw new RuntimeException("Driver is null");
        }
    }

    protected void navigateToBaseUrl() {
        String baseUrl = ConfigLoader.get("app.base.url");
        if (baseUrl != null) {
            navigateToUrl(baseUrl);
        } else {
            logger.error("Base URL not configured");
            throw new RuntimeException("Base URL not configured");
        }
    }

    protected String getCurrentUrl() {
        if (driver != null) {
            return driver.getCurrentUrl();
        } else {
            logger.error("Driver is null. Cannot get current URL");
            throw new RuntimeException("Driver is null");
        }
    }

    protected String getPageTitle() {
        if (driver != null) {
            return driver.getTitle();
        } else {
            logger.error("Driver is null. Cannot get page title");
            throw new RuntimeException("Driver is null");
        }
    }
}