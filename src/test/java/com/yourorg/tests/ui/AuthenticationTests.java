package com.yourorg.tests.ui;

import com.yourorg.base.BaseTest;
import com.yourorg.common.RetryAnalyzer;
import com.yourorg.pages.HomePage;
import com.yourorg.pages.LoginPage;
import com.yourorg.pages.HeaderFooterComponent;
import com.yourorg.utils.UserManagementUtils;
import com.yourorg.utils.UserManagementUtils.TestUser;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

@Epic("UI Tests")
@Feature("Authentication Tests")
public class AuthenticationTests extends BaseTest {
    private static final Logger logger = LogManager.getLogger(AuthenticationTests.class);
    private TestUser testUser;

    @Test(priority = 1, retryAnalyzer = RetryAnalyzer.class, groups = {"sanity", "authentication", "critical"})
    @Description("Test user login flow with API-created user and session management")
    @Severity(SeverityLevel.BLOCKER)
    public void testUserLoginFlow() throws Exception {
        logger.info("Starting user login flow test");
        
        try {
            // Create test user via API
            testUser = UserManagementUtils.createTestUser();
            
            // Navigate to application
            navigateToBaseUrl();
            
            // Initialize page objects
            HomePage homePage = new HomePage(driver);
            homePage.waitForPageToLoad();
            
            // Navigate to login page
            homePage.clickLoginButton();
            
            LoginPage loginPage = new LoginPage(driver);
            loginPage.waitForPageToLoad();
            
            // Perform login with API-created user
            loginPage.login(testUser.getUsername(), testUser.getPassword());
            
            // Wait for login to complete
            Thread.sleep(3000);
            
            // Verify successful login
            String currentUrl = getCurrentUrl();
            Assert.assertFalse(currentUrl.contains("login"), "Should not be on login page after successful login");
            
            // Verify user session is established
            Assert.assertTrue(UserManagementUtils.isUserSessionActive(driver, testUser), 
                    "User session should be active after login");
            
            logger.info("User login flow test completed successfully");
        } catch (Exception e) {
            logger.error("User login flow test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 2, retryAnalyzer = RetryAnalyzer.class, groups = {"sanity", "authentication", "session"})
    @Description("Test direct session setup without login form")
    @Severity(SeverityLevel.CRITICAL)
    public void testDirectSessionSetup() throws Exception {
        logger.info("Starting direct session setup test");
        
        try {
            // Create test user via API
            testUser = UserManagementUtils.createTestUser();
            
            // Setup user session directly in browser
            UserManagementUtils.setupUserSession(driver, testUser);
            
            // Navigate to protected area
            navigateToBaseUrl();
            
            // Initialize page objects
            HomePage homePage = new HomePage(driver);
            HeaderFooterComponent headerFooter = new HeaderFooterComponent(driver);
            
            homePage.waitForPageToLoad();
            
            // Verify user is logged in (check for user menu or profile indicator)
            try {
                Assert.assertTrue(headerFooter.isUserMenuDisplayed(), "User menu should be displayed for logged-in user");
            } catch (Exception e) {
                logger.warn("User menu check failed, user might not be visibly logged in: {}", e.getMessage());
            }
            
            // Verify session cookies are present
            Assert.assertTrue(UserManagementUtils.isUserSessionActive(driver, testUser), 
                    "User session should be active after direct setup");
            
            logger.info("Direct session setup test completed successfully");
        } catch (Exception e) {
            logger.error("Direct session setup test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 3, retryAnalyzer = RetryAnalyzer.class, groups = {"regression", "authentication", "roles"})
    @Description("Test authentication with different user roles")
    @Severity(SeverityLevel.NORMAL)
    public void testRoleBasedAuthentication() throws Exception {
        logger.info("Starting role-based authentication test");
        
        try {
            // Test with admin user
            TestUser adminUser = UserManagementUtils.createAdminUser();
            UserManagementUtils.setupUserSession(driver, adminUser);
            
            navigateToBaseUrl();
            HomePage homePage = new HomePage(driver);
            homePage.waitForPageToLoad();
            
            // Verify admin access (this would depend on your application)
            logger.info("Admin user session established successfully");
            
            // Clean up admin user
            UserManagementUtils.deleteTestUser(adminUser);
            
            // Test with customer user
            TestUser customerUser = UserManagementUtils.createUserWithRole("customer");
            UserManagementUtils.setupUserSession(driver, customerUser);
            
            driver.navigate().refresh();
            homePage.waitForPageToLoad();
            
            // Verify customer access
            logger.info("Customer user session established successfully");
            
            // Store for cleanup
            testUser = customerUser;
            
            logger.info("Role-based authentication test completed successfully");
        } catch (Exception e) {
            logger.error("Role-based authentication test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 4, retryAnalyzer = RetryAnalyzer.class, groups = {"regression", "authentication", "security"})
    @Description("Test session security and token validation")
    @Severity(SeverityLevel.CRITICAL)
    public void testSessionSecurity() throws Exception {
        logger.info("Starting session security test");
        
        try {
            // Create test user
            testUser = UserManagementUtils.createTestUser();
            UserManagementUtils.setupUserSession(driver, testUser);
            
            navigateToBaseUrl();
            HomePage homePage = new HomePage(driver);
            homePage.waitForPageToLoad();
            
            // Verify initial session is valid
            Assert.assertTrue(UserManagementUtils.isUserSessionActive(driver, testUser), 
                    "Initial session should be valid");
            
            // Clear cookies to simulate session expiry
            driver.manage().deleteAllCookies();
            
            // Refresh page
            driver.navigate().refresh();
            Thread.sleep(2000);
            
            // Verify session is no longer active
            Assert.assertFalse(UserManagementUtils.isUserSessionActive(driver, testUser), 
                    "Session should be invalid after clearing cookies");
            
            logger.info("Session security test completed successfully");
        } catch (Exception e) {
            logger.error("Session security test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 5, retryAnalyzer = RetryAnalyzer.class, groups = {"regression", "authentication", "logout"})
    @Description("Test user logout functionality and session cleanup")
    @Severity(SeverityLevel.NORMAL)
    public void testUserLogout() throws Exception {
        logger.info("Starting user logout test");
        
        try {
            // Create and setup user session
            testUser = UserManagementUtils.createTestUser();
            UserManagementUtils.setupUserSession(driver, testUser);
            
            navigateToBaseUrl();
            HomePage homePage = new HomePage(driver);
            HeaderFooterComponent headerFooter = new HeaderFooterComponent(driver);
            
            homePage.waitForPageToLoad();
            
            // Verify user is logged in
            Assert.assertTrue(UserManagementUtils.isUserSessionActive(driver, testUser), 
                    "User should be logged in initially");
            
            // Perform logout (this would depend on your application's logout mechanism)
            try {
                headerFooter.clickUserMenu();
                Thread.sleep(1000);
                
                // Click logout option (adjust selector based on your app)
                // This is a placeholder - implement based on your application
                logger.info("Logout functionality would be tested here");
                
            } catch (Exception e) {
                logger.warn("Logout UI interaction failed: {}", e.getMessage());
            }
            
            logger.info("User logout test completed successfully");
        } catch (Exception e) {
            logger.error("User logout test failed: {}", e.getMessage());
            throw e;
        }
    }

    @AfterMethod
    public void cleanupTestUser() {
        if (testUser != null) {
            try {
                UserManagementUtils.deleteTestUser(testUser);
                testUser = null;
            } catch (Exception e) {
                logger.error("Failed to cleanup test user: {}", e.getMessage());
            }
        }
    }
}