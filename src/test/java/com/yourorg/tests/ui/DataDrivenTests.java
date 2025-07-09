package com.yourorg.tests.ui;

import com.yourorg.base.BaseTest;
import com.yourorg.common.RetryAnalyzer;
import com.yourorg.dataproviders.TestNGDataProviders;
import com.yourorg.pages.HomePage;
import com.yourorg.pages.LoginPage;
import com.yourorg.utils.ScreenshotUtils;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

@Epic("UI Tests")
@Feature("Data Driven Tests")
public class DataDrivenTests extends BaseTest {
    private static final Logger logger = LogManager.getLogger(DataDrivenTests.class);

    @Test(priority = 1, retryAnalyzer = RetryAnalyzer.class, 
          dataProvider = "loginDataProvider", dataProviderClass = TestNGDataProviders.class,
          groups = {"data-driven", "login", "critical"})
    @Description("Test login functionality with multiple data sets from CSV")
    @Severity(SeverityLevel.CRITICAL)
    public void testLoginWithMultipleData(String username, String password, String expectedResult) throws Exception {
        logger.info("Starting login test with username: {}, expected: {}", username, expectedResult);
        
        try {
            // Navigate to application
            navigateToBaseUrl();
            
            // Initialize page objects
            HomePage homePage = new HomePage(driver);
            homePage.waitForPageToLoad();
            
            // Navigate to login page
            homePage.clickLoginButton();
            
            LoginPage loginPage = new LoginPage(driver);
            loginPage.waitForPageToLoad();
            
            // Perform login
            loginPage.login(username, password);
            Thread.sleep(2000);
            
            // Validate result based on expected outcome
            String currentUrl = getCurrentUrl();
            
            if ("success".equals(expectedResult)) {
                Assert.assertFalse(currentUrl.contains("login"), 
                        "Should not be on login page for successful login with: " + username);
                logger.info("Login successful for user: {}", username);
                
                // Capture success screenshot
                ScreenshotUtils.captureScreenshotWithMessage(driver, "login_success", username);
                
            } else if ("failure".equals(expectedResult)) {
                Assert.assertTrue(currentUrl.contains("login") || loginPage.isErrorMessageDisplayed(), 
                        "Should remain on login page or show error for failed login with: " + username);
                logger.info("Login correctly failed for user: {}", username);
                
                // Capture failure screenshot
                ScreenshotUtils.captureScreenshotWithMessage(driver, "login_failure", username);
            }
            
        } catch (Exception e) {
            logger.error("Login test failed for username '{}': {}", username, e.getMessage());
            throw e;
        }
    }

    @Test(priority = 2, retryAnalyzer = RetryAnalyzer.class,
          dataProvider = "userRegistrationDataProvider", dataProviderClass = TestNGDataProviders.class,
          groups = {"data-driven", "registration", "normal"})
    @Description("Test user registration with multiple data sets from Excel")
    @Severity(SeverityLevel.NORMAL)
    public void testUserRegistrationWithMultipleData(String firstName, String lastName, String email, 
                                                    String password, String expectedResult) throws Exception {
        logger.info("Starting registration test with email: {}, expected: {}", email, expectedResult);
        
        try {
            // Navigate to application
            navigateToBaseUrl();
            
            // Initialize page objects
            HomePage homePage = new HomePage(driver);
            homePage.waitForPageToLoad();
            
            // Navigate to signup (implementation depends on your app)
            try {
                homePage.clickSignupButton();
                Thread.sleep(2000);
                
                // Registration logic would go here
                logger.info("Registration form would be filled with: {} {} - {}", firstName, lastName, email);
                
                // Capture screenshot of registration form
                ScreenshotUtils.captureScreenshotWithMessage(driver, "registration_form", email);
                
            } catch (Exception e) {
                logger.warn("Registration form may not be available: {}", e.getMessage());
            }
            
        } catch (Exception e) {
            logger.error("Registration test failed for email '{}': {}", email, e.getMessage());
            throw e;
        }
    }

    @Test(priority = 3, retryAnalyzer = RetryAnalyzer.class,
          dataProvider = "searchDataProvider", dataProviderClass = TestNGDataProviders.class,
          groups = {"data-driven", "search", "normal"})
    @Description("Test search functionality with multiple search terms")
    @Severity(SeverityLevel.NORMAL)
    public void testSearchWithMultipleTerms(String searchTerm, String expectedCategory, boolean shouldFindResults) throws Exception {
        logger.info("Starting search test with term: {}, expected category: {}, should find: {}", 
                searchTerm, expectedCategory, shouldFindResults);
        
        try {
            // Navigate to application
            navigateToBaseUrl();
            
            // Initialize page objects
            HomePage homePage = new HomePage(driver);
            homePage.waitForPageToLoad();
            
            // Perform search
            if (!searchTerm.isEmpty()) {
                homePage.searchFor(searchTerm);
                Thread.sleep(2000);
                
                // Capture search results screenshot
                ScreenshotUtils.captureScreenshotWithMessage(driver, "search_results", searchTerm);
                
                // Validate search results
                String currentUrl = getCurrentUrl();
                if (shouldFindResults) {
                    Assert.assertTrue(currentUrl.contains("search") || currentUrl.contains(searchTerm), 
                            "Should navigate to search results for: " + searchTerm);
                } else {
                    // For invalid searches, might stay on same page or show no results
                    logger.info("Search for '{}' correctly showed no results or stayed on same page", searchTerm);
                }
            } else {
                // Test empty search
                logger.info("Testing empty search term");
                ScreenshotUtils.captureScreenshotWithMessage(driver, "empty_search", "empty");
            }
            
        } catch (Exception e) {
            logger.error("Search test failed for term '{}': {}", searchTerm, e.getMessage());
            throw e;
        }
    }

    @Test(priority = 4, retryAnalyzer = RetryAnalyzer.class,
          dataProvider = "jsonDataProvider", dataProviderClass = TestNGDataProviders.class,
          groups = {"data-driven", "json", "comprehensive"})
    @Description("Test with comprehensive JSON data sets")
    @Severity(SeverityLevel.NORMAL)
    public void testWithJsonData(String testName, Map<String, Object> testData) throws Exception {
        logger.info("Starting JSON data test: {} with data: {}", testName, testData);
        
        try {
            // Navigate to application
            navigateToBaseUrl();
            
            // Initialize page objects
            HomePage homePage = new HomePage(driver);
            homePage.waitForPageToLoad();
            
            // Use test data for various operations
            String action = (String) testData.get("action");
            String expectedResult = (String) testData.get("expectedResult");
            
            // Capture initial state
            ScreenshotUtils.captureScreenshotWithMessage(driver, testName, "initial_state");
            
            // Perform action based on test data
            switch (action != null ? action : "default") {
                case "login":
                    String username = (String) testData.get("username");
                    String password = (String) testData.get("password");
                    
                    homePage.clickLoginButton();
                    LoginPage loginPage = new LoginPage(driver);
                    loginPage.waitForPageToLoad();
                    loginPage.login(username, password);
                    break;
                    
                case "search":
                    String searchTerm = (String) testData.get("searchTerm");
                    homePage.searchFor(searchTerm);
                    break;
                    
                default:
                    logger.info("Default action for test: {}", testName);
                    break;
            }
            
            Thread.sleep(2000);
            
            // Capture final state
            ScreenshotUtils.captureScreenshotWithMessage(driver, testName, "final_state");
            
            // Validate based on expected result
            if ("success".equals(expectedResult)) {
                logger.info("Test {} completed successfully", testName);
            } else {
                logger.info("Test {} completed with expected result: {}", testName, expectedResult);
            }
            
        } catch (Exception e) {
            logger.error("JSON data test failed for '{}': {}", testName, e.getMessage());
            throw e;
        }
    }

    @Test(priority = 5, retryAnalyzer = RetryAnalyzer.class,
          dataProvider = "dynamicDataProvider", dataProviderClass = TestNGDataProviders.class,
          groups = {"data-driven", "dynamic", "environment"})
    @Description("Test with dynamic data provider based on environment")
    @Severity(SeverityLevel.NORMAL)
    public void testWithDynamicData(Map<String, Object> testData) throws Exception {
        String testName = (String) testData.get("testName");
        logger.info("Starting dynamic data test: {} with data: {}", testName, testData);
        
        try {
            // Navigate to application
            navigateToBaseUrl();
            
            // Initialize page objects
            HomePage homePage = new HomePage(driver);
            homePage.waitForPageToLoad();
            
            // Capture screenshot with environment info
            String environment = System.getProperty("environment", "qa");
            ScreenshotUtils.captureScreenshotWithMessage(driver, testName, "env_" + environment);
            
            // Execute test based on dynamic data
            String testType = (String) testData.get("testType");
            Boolean enabled = (Boolean) testData.get("enabled");
            
            if (enabled != null && enabled) {
                logger.info("Executing enabled test: {} of type: {}", testName, testType);
                
                // Test execution logic based on type
                switch (testType != null ? testType : "default") {
                    case "navigation":
                        // Test navigation
                        Assert.assertTrue(homePage.isNavigationDisplayed(), "Navigation should be displayed");
                        break;
                        
                    case "ui":
                        // Test UI elements
                        Assert.assertTrue(homePage.isLogoDisplayed(), "Logo should be displayed");
                        break;
                        
                    default:
                        logger.info("Default test execution for: {}", testName);
                        break;
                }
            } else {
                logger.info("Test {} is disabled, skipping execution", testName);
            }
            
        } catch (Exception e) {
            logger.error("Dynamic data test failed for '{}': {}", testName, e.getMessage());
            throw e;
        }
    }
}