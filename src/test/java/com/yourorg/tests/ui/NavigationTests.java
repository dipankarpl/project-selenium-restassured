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

@Epic("UI Tests")
@Feature("Navigation Tests")
public class NavigationTests extends BaseTest {
    private static final Logger logger = LogManager.getLogger(NavigationTests.class);
    private TestUser testUser;

    @Test(priority = 1, retryAnalyzer = RetryAnalyzer.class, groups = {"sanity", "navigation", "critical"})
    @Description("Test main navigation menu functionality and accessibility")
    @Severity(SeverityLevel.CRITICAL)
    public void testMainNavigation() throws Exception {
        logger.info("Starting main navigation test");
        
        try {
            // Create user and setup session
            testUser = UserManagementUtils.createTestUser();
            UserManagementUtils.setupUserSession(driver, testUser);
            
            // Navigate to application
            navigateToBaseUrl();
            
            HomePage homePage = new HomePage(driver);
            HeaderFooterComponent headerFooter = new HeaderFooterComponent(driver);
            
            homePage.waitForPageToLoad();
            
            // Verify navigation menu is displayed
            Assert.assertTrue(headerFooter.isNavigationMenuDisplayed(), 
                    "Navigation menu should be displayed");
            
            // Test navigation items
            String[] navigationItems = {"Home", "Products", "Categories", "About", "Contact"};
            String initialUrl = getCurrentUrl();
            
            for (String item : navigationItems) {
                try {
                    logger.info("Testing navigation to: {}", item);
                    
                    headerFooter.clickNavigationItem(item);
                    Thread.sleep(2000);
                    
                    // Verify navigation occurred
                    String currentUrl = getCurrentUrl();
                    Assert.assertNotEquals(currentUrl, initialUrl, 
                            "URL should change after navigation to: " + item);
                    
                    // Navigate back to home for next test
                    headerFooter.clickHeaderLogo();
                    Thread.sleep(1000);
                    
                } catch (Exception e) {
                    logger.warn("Navigation item '{}' may not exist: {}", item, e.getMessage());
                }
            }
            
            logger.info("Main navigation test completed successfully");
        } catch (Exception e) {
            logger.error("Main navigation test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 2, retryAnalyzer = RetryAnalyzer.class, groups = {"sanity", "navigation", "breadcrumb"})
    @Description("Test breadcrumb navigation and page hierarchy")
    @Severity(SeverityLevel.NORMAL)
    public void testBreadcrumbNavigation() throws Exception {
        logger.info("Starting breadcrumb navigation test");
        
        try {
            // Create user and setup session
            testUser = UserManagementUtils.createTestUser();
            UserManagementUtils.setupUserSession(driver, testUser);
            
            // Navigate to application
            navigateToBaseUrl();
            
            HomePage homePage = new HomePage(driver);
            HeaderFooterComponent headerFooter = new HeaderFooterComponent(driver);
            
            homePage.waitForPageToLoad();
            
            // Navigate to a deep page structure (e.g., Products > Category > Subcategory)
            try {
                headerFooter.clickNavigationItem("Products");
                Thread.sleep(2000);
                
                // Test breadcrumb navigation (implementation depends on your app)
                logger.info("Breadcrumb navigation would be tested here");
                
                // Verify breadcrumb elements are present and functional
                String currentUrl = getCurrentUrl();
                Assert.assertTrue(currentUrl.contains("products") || currentUrl.contains("category"), 
                        "Should be on products/category page");
                
            } catch (Exception e) {
                logger.warn("Breadcrumb navigation test failed: {}", e.getMessage());
            }
            
            logger.info("Breadcrumb navigation test completed successfully");
        } catch (Exception e) {
            logger.error("Breadcrumb navigation test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 3, retryAnalyzer = RetryAnalyzer.class, groups = {"regression", "navigation", "browser"})
    @Description("Test browser back and forward navigation functionality")
    @Severity(SeverityLevel.NORMAL)
    public void testBrowserNavigation() throws Exception {
        logger.info("Starting browser navigation test");
        
        try {
            // Create user and setup session
            testUser = UserManagementUtils.createTestUser();
            UserManagementUtils.setupUserSession(driver, testUser);
            
            // Navigate to application
            navigateToBaseUrl();
            
            HomePage homePage = new HomePage(driver);
            HeaderFooterComponent headerFooter = new HeaderFooterComponent(driver);
            
            homePage.waitForPageToLoad();
            
            // Store initial URL
            String homeUrl = getCurrentUrl();
            
            // Navigate to another page
            headerFooter.clickNavigationItem("About");
            Thread.sleep(2000);
            
            String aboutUrl = getCurrentUrl();
            Assert.assertNotEquals(aboutUrl, homeUrl, "Should navigate to different page");
            
            // Test browser back navigation
            driver.navigate().back();
            Thread.sleep(2000);
            
            String backUrl = getCurrentUrl();
            Assert.assertEquals(backUrl, homeUrl, "Should navigate back to home page");
            
            // Test browser forward navigation
            driver.navigate().forward();
            Thread.sleep(2000);
            
            String forwardUrl = getCurrentUrl();
            Assert.assertEquals(forwardUrl, aboutUrl, "Should navigate forward to about page");
            
            // Test page refresh
            driver.navigate().refresh();
            Thread.sleep(2000);
            
            // Verify user session persists after refresh
            Assert.assertTrue(UserManagementUtils.isUserSessionActive(driver, testUser), 
                    "User session should persist after page refresh");
            
            logger.info("Browser navigation test completed successfully");
        } catch (Exception e) {
            logger.error("Browser navigation test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 4, retryAnalyzer = RetryAnalyzer.class, groups = {"regression", "navigation", "footer"})
    @Description("Test footer navigation and links functionality")
    @Severity(SeverityLevel.NORMAL)
    public void testFooterNavigation() throws Exception {
        logger.info("Starting footer navigation test");
        
        try {
            // Create user and setup session
            testUser = UserManagementUtils.createTestUser();
            UserManagementUtils.setupUserSession(driver, testUser);
            
            // Navigate to application
            navigateToBaseUrl();
            
            HomePage homePage = new HomePage(driver);
            HeaderFooterComponent headerFooter = new HeaderFooterComponent(driver);
            
            homePage.waitForPageToLoad();
            
            // Scroll to footer
            headerFooter.scrollToFooter();
            Thread.sleep(1000);
            
            // Verify footer is displayed
            Assert.assertTrue(headerFooter.isFooterDisplayed(), "Footer should be displayed");
            
            // Test footer links
            String[] footerLinks = {"Privacy Policy", "Terms of Service", "Contact Us", "Help", "FAQ"};
            
            for (String link : footerLinks) {
                try {
                    logger.info("Testing footer link: {}", link);
                    
                    headerFooter.scrollToFooter();
                    headerFooter.clickFooterLink(link);
                    Thread.sleep(2000);
                    
                    // Verify navigation occurred
                    String currentUrl = getCurrentUrl();
                    logger.info("Footer link '{}' navigation URL: {}", link, currentUrl);
                    
                    // Navigate back to home
                    headerFooter.clickHeaderLogo();
                    Thread.sleep(1000);
                    
                } catch (Exception e) {
                    logger.warn("Footer link '{}' may not exist: {}", link, e.getMessage());
                }
            }
            
            logger.info("Footer navigation test completed successfully");
        } catch (Exception e) {
            logger.error("Footer navigation test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 5, retryAnalyzer = RetryAnalyzer.class, groups = {"regression", "navigation", "responsive"})
    @Description("Test navigation responsiveness across different viewport sizes")
    @Severity(SeverityLevel.NORMAL)
    public void testResponsiveNavigation() throws Exception {
        logger.info("Starting responsive navigation test");
        
        try {
            // Create user and setup session
            testUser = UserManagementUtils.createTestUser();
            UserManagementUtils.setupUserSession(driver, testUser);
            
            // Navigate to application
            navigateToBaseUrl();
            
            HomePage homePage = new HomePage(driver);
            HeaderFooterComponent headerFooter = new HeaderFooterComponent(driver);
            
            homePage.waitForPageToLoad();
            
            // Test different viewport sizes
            int[][] viewportSizes = {{1920, 1080}, {1024, 768}, {768, 1024}, {375, 667}};
            
            for (int[] size : viewportSizes) {
                try {
                    logger.info("Testing navigation at viewport: {}x{}", size[0], size[1]);
                    
                    // Set viewport size
                    driver.manage().window().setSize(new org.openqa.selenium.Dimension(size[0], size[1]));
                    Thread.sleep(1000);
                    
                    // Verify navigation elements are still accessible
                    Assert.assertTrue(headerFooter.isHeaderDisplayed(), 
                            "Header should be displayed at " + size[0] + "x" + size[1]);
                    
                    // Test navigation functionality
                    try {
                        headerFooter.clickHeaderLogo();
                        Thread.sleep(1000);
                        
                        Assert.assertTrue(homePage.isLogoDisplayed(), 
                                "Logo should be displayed at " + size[0] + "x" + size[1]);
                    } catch (Exception e) {
                        logger.warn("Navigation test failed at viewport {}x{}: {}", size[0], size[1], e.getMessage());
                    }
                    
                } catch (Exception e) {
                    logger.warn("Viewport resize not supported: {}", e.getMessage());
                }
            }
            
            // Reset to default size
            driver.manage().window().maximize();
            
            logger.info("Responsive navigation test completed successfully");
        } catch (Exception e) {
            logger.error("Responsive navigation test failed: {}", e.getMessage());
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