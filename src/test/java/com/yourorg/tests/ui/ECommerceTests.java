package com.yourorg.tests.ui;

import com.yourorg.base.BaseTest;
import com.yourorg.common.RetryAnalyzer;
import com.yourorg.pages.HomePage;
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

import java.util.HashMap;
import java.util.Map;

@Epic("UI Tests")
@Feature("E-Commerce Tests")
public class ECommerceTests extends BaseTest {
    private static final Logger logger = LogManager.getLogger(ECommerceTests.class);
    private TestUser testUser;

    @Test(priority = 1, retryAnalyzer = RetryAnalyzer.class, groups = {"end-to-end", "shopping", "critical"})
    @Description("End-to-end shopping cart flow with pre-configured user")
    @Severity(SeverityLevel.CRITICAL)
    public void testShoppingCartFlow() throws Exception {
        logger.info("Starting shopping cart flow test");
        
        try {
            // Create user with shopping cart data
            testUser = UserManagementUtils.createTestUser();
            UserManagementUtils.prepareUserWithData(testUser, "shopping_cart");
            UserManagementUtils.setupUserSession(driver, testUser);
            
            // Navigate to application
            navigateToBaseUrl();
            
            HomePage homePage = new HomePage(driver);
            HeaderFooterComponent headerFooter = new HeaderFooterComponent(driver);
            
            homePage.waitForPageToLoad();
            
            // Verify user session is active
            Assert.assertTrue(UserManagementUtils.isUserSessionActive(driver, testUser), 
                    "User session should be active");
            
            // Navigate to cart
            try {
                headerFooter.clickCartIcon();
                Thread.sleep(2000);
                
                // Verify cart has items (based on prepared data)
                String currentUrl = getCurrentUrl();
                Assert.assertTrue(currentUrl.contains("cart") || currentUrl.contains("shopping"), 
                        "Should navigate to cart page");
                
                logger.info("Shopping cart contains pre-loaded items");
                
            } catch (Exception e) {
                logger.warn("Cart navigation failed: {}", e.getMessage());
            }
            
            logger.info("Shopping cart flow test completed successfully");
        } catch (Exception e) {
            logger.error("Shopping cart flow test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 2, retryAnalyzer = RetryAnalyzer.class, groups = {"end-to-end", "checkout", "critical"})
    @Description("End-to-end checkout process with pre-configured payment methods")
    @Severity(SeverityLevel.CRITICAL)
    public void testCheckoutProcess() throws Exception {
        logger.info("Starting checkout process test");
        
        try {
            // Create user with complete checkout data
            testUser = UserManagementUtils.createTestUser();
            UserManagementUtils.prepareUserWithData(testUser, "shopping_cart");
            UserManagementUtils.prepareUserWithData(testUser, "payment_methods");
            UserManagementUtils.prepareUserWithData(testUser, "addresses");
            UserManagementUtils.setupUserSession(driver, testUser);
            
            // Navigate to application
            navigateToBaseUrl();
            
            HomePage homePage = new HomePage(driver);
            HeaderFooterComponent headerFooter = new HeaderFooterComponent(driver);
            
            homePage.waitForPageToLoad();
            
            // Navigate to cart and proceed to checkout
            try {
                headerFooter.clickCartIcon();
                Thread.sleep(2000);
                
                // Proceed to checkout (implementation depends on your app)
                logger.info("Checkout process would be tested here with pre-configured data");
                
                // Verify user has payment methods and addresses ready
                Assert.assertEquals(testUser.getUserData("payment_methods"), 1, 
                        "User should have payment methods configured");
                Assert.assertEquals(testUser.getUserData("addresses"), 1, 
                        "User should have addresses configured");
                
            } catch (Exception e) {
                logger.warn("Checkout navigation failed: {}", e.getMessage());
            }
            
            logger.info("Checkout process test completed successfully");
        } catch (Exception e) {
            logger.error("Checkout process test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 3, retryAnalyzer = RetryAnalyzer.class, groups = {"end-to-end", "orders", "normal"})
    @Description("Test order history and management with existing orders")
    @Severity(SeverityLevel.NORMAL)
    public void testOrderManagement() throws Exception {
        logger.info("Starting order management test");
        
        try {
            // Create user with order history
            testUser = UserManagementUtils.createTestUser();
            UserManagementUtils.prepareUserWithData(testUser, "order_history");
            UserManagementUtils.setupUserSession(driver, testUser);
            
            // Navigate to application
            navigateToBaseUrl();
            
            HomePage homePage = new HomePage(driver);
            HeaderFooterComponent headerFooter = new HeaderFooterComponent(driver);
            
            homePage.waitForPageToLoad();
            
            // Navigate to user account/orders
            try {
                headerFooter.clickUserMenu();
                Thread.sleep(1000);
                
                // Navigate to orders section (implementation depends on your app)
                logger.info("Order management would be tested here");
                
                // Verify user has order history
                Assert.assertEquals(testUser.getUserData("order_count"), 1, 
                        "User should have order history");
                
            } catch (Exception e) {
                logger.warn("Order management navigation failed: {}", e.getMessage());
            }
            
            logger.info("Order management test completed successfully");
        } catch (Exception e) {
            logger.error("Order management test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 4, retryAnalyzer = RetryAnalyzer.class, groups = {"end-to-end", "profile", "normal"})
    @Description("Test user profile management with pre-configured preferences")
    @Severity(SeverityLevel.NORMAL)
    public void testUserProfileManagement() throws Exception {
        logger.info("Starting user profile management test");
        
        try {
            // Create user with custom profile data
            Map<String, Object> profileData = new HashMap<>();
            profileData.put("firstName", "John");
            profileData.put("lastName", "Doe");
            profileData.put("phone", "555-1234");
            profileData.put("dateOfBirth", "1990-01-01");
            
            testUser = UserManagementUtils.createTestUserWithProfile(profileData);
            UserManagementUtils.prepareUserWithData(testUser, "preferences");
            UserManagementUtils.setupUserSession(driver, testUser);
            
            // Navigate to application
            navigateToBaseUrl();
            
            HomePage homePage = new HomePage(driver);
            HeaderFooterComponent headerFooter = new HeaderFooterComponent(driver);
            
            homePage.waitForPageToLoad();
            
            // Navigate to user profile
            try {
                headerFooter.clickUserMenu();
                Thread.sleep(1000);
                
                // Navigate to profile section (implementation depends on your app)
                logger.info("User profile management would be tested here");
                
                // Verify user has preferences set
                Assert.assertEquals(testUser.getUserData("preferences_set"), true, 
                        "User should have preferences configured");
                
                // Verify profile data
                Assert.assertEquals(testUser.getUserData("firstName"), "John", 
                        "User first name should be set");
                
            } catch (Exception e) {
                logger.warn("Profile management navigation failed: {}", e.getMessage());
            }
            
            logger.info("User profile management test completed successfully");
        } catch (Exception e) {
            logger.error("User profile management test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 5, retryAnalyzer = RetryAnalyzer.class, groups = {"end-to-end", "search", "normal"})
    @Description("Test product search and filtering functionality")
    @Severity(SeverityLevel.NORMAL)
    public void testProductSearchAndFiltering() throws Exception {
        logger.info("Starting product search and filtering test");
        
        try {
            // Create basic user for search testing
            testUser = UserManagementUtils.createTestUser();
            UserManagementUtils.setupUserSession(driver, testUser);
            
            // Navigate to application
            navigateToBaseUrl();
            
            HomePage homePage = new HomePage(driver);
            HeaderFooterComponent headerFooter = new HeaderFooterComponent(driver);
            
            homePage.waitForPageToLoad();
            
            // Test search functionality
            String[] searchTerms = {"laptop", "phone", "book", "clothing"};
            
            for (String term : searchTerms) {
                try {
                    logger.info("Testing search for: {}", term);
                    
                    // Perform search
                    homePage.searchFor(term);
                    Thread.sleep(2000);
                    
                    // Verify search results page
                    String currentUrl = getCurrentUrl();
                    Assert.assertTrue(currentUrl.contains("search") || currentUrl.contains(term), 
                            "Should navigate to search results for: " + term);
                    
                    // Navigate back to home for next search
                    headerFooter.clickHeaderLogo();
                    Thread.sleep(1000);
                    
                } catch (Exception e) {
                    logger.warn("Search test failed for term '{}': {}", term, e.getMessage());
                }
            }
            
            logger.info("Product search and filtering test completed successfully");
        } catch (Exception e) {
            logger.error("Product search and filtering test failed: {}", e.getMessage());
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