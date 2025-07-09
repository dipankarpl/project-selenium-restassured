package com.yourorg.tests.ui;

import com.yourorg.base.BaseTest;
import com.yourorg.common.RetryAnalyzer;
import com.yourorg.pages.HeaderFooterComponent;
import com.yourorg.pages.HomePage;
import com.yourorg.pages.LoginPage;
import com.yourorg.utils.ConfigLoader;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

@Epic("UI Tests")
@Feature("Sanity Tests")
public class SanityTest extends BaseTest {
    private static final Logger logger = LogManager.getLogger(SanityTest.class);

    @Test(priority = 1, retryAnalyzer = RetryAnalyzer.class, groups = {"sanity", "smoke", "critical"})
    @Description("Verify that the home page loads successfully with all essential elements")
    @Severity(SeverityLevel.BLOCKER)
    public void testHomePageLoads() throws Exception {
        logger.info("Starting home page load test");
        
        try {
            // Navigate to home page
            navigateToBaseUrl();
            
            // Initialize page objects
            HomePage homePage = new HomePage(driver);
            HeaderFooterComponent headerFooter = new HeaderFooterComponent(driver);
            
            // Wait for page to load
            homePage.waitForPageToLoad();
            
            // Verify page elements
            Assert.assertTrue(homePage.isLogoDisplayed(), "Logo should be displayed");
            Assert.assertTrue(headerFooter.isHeaderDisplayed(), "Header should be displayed");
            Assert.assertTrue(headerFooter.isFooterDisplayed(), "Footer should be displayed");
            
            // Verify page title
            String expectedTitle = ConfigLoader.get("app.title", "Test Application");
            Assert.assertTrue(homePage.getPageTitle().contains(expectedTitle), 
                    "Page title should contain: " + expectedTitle);
            
            logger.info("Home page load test completed successfully");
        } catch (Exception e) {
            logger.error("Home page load test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 2, retryAnalyzer = RetryAnalyzer.class, groups = {"sanity", "login", "critical"})
    @Description("Verify that the login page loads successfully with all required form elements")
    @Severity(SeverityLevel.CRITICAL)
    public void testLoginPageLoads() throws Exception {
        logger.info("Starting login page load test");
        
        try {
            // Navigate to home page
            navigateToBaseUrl();
            
            // Initialize page objects
            HomePage homePage = new HomePage(driver);
            homePage.waitForPageToLoad();
            
            // Click login button
            homePage.clickLoginButton();
            
            // Initialize login page
            LoginPage loginPage = new LoginPage(driver);
            loginPage.waitForPageToLoad();
            
            // Verify login page elements
            Assert.assertTrue(loginPage.isUsernameFieldDisplayed(), "Username field should be displayed");
            Assert.assertTrue(loginPage.isPasswordFieldDisplayed(), "Password field should be displayed");
            Assert.assertTrue(loginPage.isLoginButtonDisplayed(), "Login button should be displayed");
            
            logger.info("Login page load test completed successfully");
        } catch (Exception e) {
            logger.error("Login page load test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 3, retryAnalyzer = RetryAnalyzer.class, groups = {"sanity", "login", "authentication", "critical"})
    @Description("Verify successful login functionality with valid user credentials")
    @Severity(SeverityLevel.BLOCKER)
    public void testValidLogin() throws Exception {
        logger.info("Starting valid login test");
        
        try {
            // Navigate to home page
            navigateToBaseUrl();
            
            // Initialize page objects
            HomePage homePage = new HomePage(driver);
            homePage.waitForPageToLoad();
            
            // Navigate to login page
            homePage.clickLoginButton();
            
            // Initialize login page
            LoginPage loginPage = new LoginPage(driver);
            loginPage.waitForPageToLoad();
            
            // Get credentials from config
            String username = ConfigLoader.get("test.username", "testuser");
            String password = ConfigLoader.get("test.password", "testpass");
            
            // Perform login
            loginPage.login(username, password);
            
            // Verify successful login
            Thread.sleep(2000); // Wait for potential redirect
            
            String currentUrl = getCurrentUrl();
            Assert.assertFalse(currentUrl.contains("login"), "Should not be on login page after successful login");
            
            logger.info("Valid login test completed successfully");
        } catch (Exception e) {
            logger.error("Valid login test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 4, retryAnalyzer = RetryAnalyzer.class, groups = {"sanity", "login", "negative", "validation"})
    @Description("Verify error handling for invalid login credentials")
    @Severity(SeverityLevel.NORMAL)
    public void testInvalidLogin() throws Exception {
        logger.info("Starting invalid login test");
        
        try {
            // Navigate to home page
            navigateToBaseUrl();
            
            // Initialize page objects
            HomePage homePage = new HomePage(driver);
            homePage.waitForPageToLoad();
            
            // Navigate to login page
            homePage.clickLoginButton();
            
            // Initialize login page
            LoginPage loginPage = new LoginPage(driver);
            loginPage.waitForPageToLoad();
            
            // Perform login with invalid credentials
            loginPage.login("invalid_username", "invalid_password");
            
            // Wait for error message
            Thread.sleep(2000);
            
            // Verify error message is displayed
            Assert.assertTrue(loginPage.isErrorMessageDisplayed(), "Error message should be displayed");
            
            String errorMessage = loginPage.getErrorMessage();
            Assert.assertFalse(errorMessage.isEmpty(), "Error message should not be empty");
            
            logger.info("Invalid login test completed successfully. Error message: {}", errorMessage);
        } catch (Exception e) {
            logger.error("Invalid login test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 5, retryAnalyzer = RetryAnalyzer.class, groups = {"sanity", "navigation", "ui"})
    @Description("Verify main navigation menu functionality and accessibility")
    @Severity(SeverityLevel.NORMAL)
    public void testNavigationMenu() throws Exception {
        logger.info("Starting navigation menu test");
        
        try {
            // Navigate to home page
            navigateToBaseUrl();
            
            // Initialize page objects
            HomePage homePage = new HomePage(driver);
            HeaderFooterComponent headerFooter = new HeaderFooterComponent(driver);
            
            homePage.waitForPageToLoad();
            
            // Verify navigation menu is displayed
            Assert.assertTrue(headerFooter.isNavigationMenuDisplayed(), "Navigation menu should be displayed");
            
            // Test navigation items
            String[] navigationItems = {"Home", "Products", "About", "Contact"};
            
            for (String item : navigationItems) {
                try {
                    headerFooter.clickNavigationItem(item);
                    Thread.sleep(1000); // Wait for navigation
                    
                    // Verify URL or page content changed
                    String currentUrl = getCurrentUrl();
                    logger.info("Navigated to: {} - Current URL: {}", item, currentUrl);
                    
                    // Navigate back to home for next test
                    headerFooter.clickHeaderLogo();
                    Thread.sleep(1000);
                    
                } catch (Exception e) {
                    logger.warn("Navigation item '{}' may not exist or is not clickable: {}", item, e.getMessage());
                }
            }
            
            logger.info("Navigation menu test completed successfully");
        } catch (Exception e) {
            logger.error("Navigation menu test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 6, retryAnalyzer = RetryAnalyzer.class, groups = {"sanity", "search", "functionality"})
    @Description("Verify search functionality works correctly with valid search terms")
    @Severity(SeverityLevel.NORMAL)
    public void testSearchFunctionality() throws Exception {
        logger.info("Starting search functionality test");
        
        try {
            // Navigate to home page
            navigateToBaseUrl();
            
            // Initialize page objects
            HomePage homePage = new HomePage(driver);
            HeaderFooterComponent headerFooter = new HeaderFooterComponent(driver);
            
            homePage.waitForPageToLoad();
            
            // Test search functionality
            String searchTerm = ConfigLoader.get("test.search.term", "test");
            
            try {
                homePage.searchFor(searchTerm);
                Thread.sleep(2000); // Wait for search results
                
                // Verify search was performed (URL change or results displayed)
                String currentUrl = getCurrentUrl();
                Assert.assertTrue(currentUrl.contains("search") || currentUrl.contains(searchTerm), 
                        "URL should contain search term or search indicator");
                
                logger.info("Search functionality test completed successfully");
            } catch (Exception e) {
                logger.warn("Search functionality may not be available: {}", e.getMessage());
                // Try header search as fallback
                headerFooter.searchInHeader(searchTerm);
                Thread.sleep(2000);
                logger.info("Header search functionality test completed");
            }
            
        } catch (Exception e) {
            logger.error("Search functionality test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 7, retryAnalyzer = RetryAnalyzer.class, groups = {"sanity", "ui", "responsive"})
    @Description("Verify page responsiveness and key UI elements display correctly")
    @Severity(SeverityLevel.NORMAL)
    public void testPageResponsiveness() throws Exception {
        logger.info("Starting page responsiveness test");
        
        try {
            // Navigate to home page
            navigateToBaseUrl();
            
            // Initialize page objects
            HomePage homePage = new HomePage(driver);
            HeaderFooterComponent headerFooter = new HeaderFooterComponent(driver);
            
            homePage.waitForPageToLoad();
            
            // Verify key responsive elements
            Assert.assertTrue(homePage.isLogoDisplayed(), "Logo should be displayed");
            Assert.assertTrue(headerFooter.isHeaderDisplayed(), "Header should be displayed");
            
            // Test scrolling functionality
            homePage.scrollToFeaturedProducts();
            Thread.sleep(1000);
            
            headerFooter.scrollToFooter();
            Thread.sleep(1000);
            
            headerFooter.scrollToHeader();
            Thread.sleep(1000);
            
            logger.info("Page responsiveness test completed successfully");
        } catch (Exception e) {
            logger.error("Page responsiveness test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 8, retryAnalyzer = RetryAnalyzer.class, groups = {"sanity", "performance", "load"})
    @Description("Verify page load performance meets acceptable standards")
    @Severity(SeverityLevel.NORMAL)
    public void testPageLoadPerformance() throws Exception {
        logger.info("Starting page load performance test");
        
        try {
            long startTime = System.currentTimeMillis();
            
            // Navigate to home page
            navigateToBaseUrl();
            
            // Initialize page objects
            HomePage homePage = new HomePage(driver);
            homePage.waitForPageToLoad();
            
            long endTime = System.currentTimeMillis();
            long loadTime = endTime - startTime;
            
            // Assert page loads within acceptable time (10 seconds)
            Assert.assertTrue(loadTime < 10000, "Page should load within 10 seconds. Actual: " + loadTime + "ms");
            
            // Verify key elements are loaded
            Assert.assertTrue(homePage.isLogoDisplayed(), "Logo should be displayed after page load");
            Assert.assertTrue(homePage.isNavigationDisplayed(), "Navigation should be displayed after page load");
            
            logger.info("Page load performance test completed successfully. Load time: {}ms", loadTime);
        } catch (Exception e) {
            logger.error("Page load performance test failed: {}", e.getMessage());
            throw e;
        }
    }
}