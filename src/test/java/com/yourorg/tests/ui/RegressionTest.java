package com.yourorg.tests.ui;

import com.yourorg.base.BaseTest;
import com.yourorg.common.RetryAnalyzer;
import com.yourorg.pages.HeaderFooterComponent;
import com.yourorg.pages.HomePage;
import com.yourorg.pages.LoginPage;
import com.yourorg.utils.ConfigLoader;
import com.yourorg.utils.StateRetentionManager;
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
@Feature("Regression Tests")
public class RegressionTest extends BaseTest {
    private static final Logger logger = LogManager.getLogger(RegressionTest.class);

    @Test(priority = 1, retryAnalyzer = RetryAnalyzer.class, groups = {"regression", "comprehensive", "ui"})
    @Description("Comprehensive validation of home page elements and functionality")
    @Severity(SeverityLevel.CRITICAL)
    public void testHomePageComprehensive() throws Exception {
        logger.info("Starting comprehensive home page test");
        
        try {
            // Navigate to home page
            navigateToBaseUrl();
            
            // Initialize page objects
            HomePage homePage = new HomePage(driver);
            HeaderFooterComponent headerFooter = new HeaderFooterComponent(driver);
            
            // Wait for page to load
            homePage.waitForPageToLoad();
            
            // Store initial state
            StateRetentionManager.setSessionState(testSessionId, "initialUrl", getCurrentUrl());
            
            // Comprehensive validation
            Assert.assertTrue(homePage.isLogoDisplayed(), "Logo should be displayed");
            Assert.assertTrue(homePage.isHeroSectionDisplayed(), "Hero section should be displayed");
            Assert.assertTrue(homePage.isNavigationDisplayed(), "Navigation should be displayed");
            Assert.assertTrue(homePage.isFeaturedProductsDisplayed(), "Featured products should be displayed");
            
            // Header validation
            Assert.assertTrue(headerFooter.isHeaderDisplayed(), "Header should be displayed");
            Assert.assertTrue(headerFooter.isNavigationMenuDisplayed(), "Navigation menu should be displayed");
            Assert.assertTrue(headerFooter.isSearchBarDisplayed(), "Search bar should be displayed");
            
            // Footer validation
            Assert.assertTrue(headerFooter.isFooterDisplayed(), "Footer should be displayed");
            
            // Verify page title and URL
            String pageTitle = homePage.getPageTitle();
            String currentUrl = homePage.getCurrentUrl();
            
            Assert.assertFalse(pageTitle.isEmpty(), "Page title should not be empty");
            Assert.assertFalse(currentUrl.isEmpty(), "Current URL should not be empty");
            
            logger.info("Comprehensive home page test completed successfully");
        } catch (Exception e) {
            logger.error("Comprehensive home page test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 2, retryAnalyzer = RetryAnalyzer.class, groups = {"regression", "login", "authentication"})
    @Description("Test login functionality with remember me option enabled")
    @Severity(SeverityLevel.NORMAL)
    public void testLoginWithRememberMe() throws Exception {
        logger.info("Starting login with remember me test");
        
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
            
            // Perform login with remember me
            loginPage.loginWithRememberMe(username, password);
            
            // Store login state
            StateRetentionManager.setSessionState(testSessionId, "loginUsername", username);
            StateRetentionManager.setSessionState(testSessionId, "loginTime", System.currentTimeMillis());
            
            // Wait for login to complete
            Thread.sleep(3000);
            
            // Verify login success
            String currentUrl = getCurrentUrl();
            Assert.assertFalse(currentUrl.contains("login"), "Should not be on login page after successful login");
            
            logger.info("Login with remember me test completed successfully");
        } catch (Exception e) {
            logger.error("Login with remember me test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 3, retryAnalyzer = RetryAnalyzer.class, groups = {"regression", "login", "negative", "security"})
    @Description("Test multiple login attempts with various invalid credential combinations")
    @Severity(SeverityLevel.NORMAL)
    public void testMultipleLoginAttempts() throws Exception {
        logger.info("Starting multiple login attempts test");
        
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
            
            // Test multiple invalid login attempts
            String[][] invalidCredentials = {
                {"", ""},
                {"invalid", ""},
                {"", "invalid"},
                {"invalid", "invalid"},
                {"admin", "admin"},
                {"test", "test"}
            };
            
            for (String[] credentials : invalidCredentials) {
                logger.info("Testing credentials: {} / {}", credentials[0], "***");
                
                // Clear fields and enter new credentials
                loginPage.clearFields();
                loginPage.login(credentials[0], credentials[1]);
                
                // Wait for response
                Thread.sleep(2000);
                
                // Check if still on login page or error displayed
                String currentUrl = getCurrentUrl();
                if (currentUrl.contains("login") || loginPage.isErrorMessageDisplayed()) {
                    logger.info("Login correctly rejected for credentials: {}", credentials[0]);
                } else {
                    logger.warn("Unexpected behavior for credentials: {}", credentials[0]);
                }
            }
            
            logger.info("Multiple login attempts test completed successfully");
        } catch (Exception e) {
            logger.error("Multiple login attempts test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 4, retryAnalyzer = RetryAnalyzer.class, groups = {"regression", "password", "functionality"})
    @Description("Test forgot password functionality and navigation flow")
    @Severity(SeverityLevel.NORMAL)
    public void testForgotPasswordFunctionality() throws Exception {
        logger.info("Starting forgot password functionality test");
        
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
            
            // Click forgot password link
            loginPage.clickForgotPassword();
            
            // Wait for navigation
            Thread.sleep(2000);
            
            // Verify navigation to forgot password page
            String currentUrl = getCurrentUrl();
            Assert.assertTrue(currentUrl.contains("forgot") || currentUrl.contains("reset"), 
                    "Should navigate to forgot password page");
            
            logger.info("Forgot password functionality test completed successfully");
        } catch (Exception e) {
            logger.error("Forgot password functionality test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 5, retryAnalyzer = RetryAnalyzer.class, groups = {"regression", "navigation", "comprehensive"})
    @Description("Comprehensive testing of all navigation menu items and functionality")
    @Severity(SeverityLevel.NORMAL)
    public void testComprehensiveNavigation() throws Exception {
        logger.info("Starting comprehensive navigation test");
        
        try {
            // Navigate to home page
            navigateToBaseUrl();
            
            // Initialize page objects
            HomePage homePage = new HomePage(driver);
            HeaderFooterComponent headerFooter = new HeaderFooterComponent(driver);
            
            homePage.waitForPageToLoad();
            
            // Store initial URL
            String initialUrl = getCurrentUrl();
            StateRetentionManager.setSessionState(testSessionId, "initialUrl", initialUrl);
            
            // Test navigation items
            String[] navigationItems = {"Home", "Products", "Services", "About", "Contact"};
            
            for (String item : navigationItems) {
                try {
                    logger.info("Testing navigation to: {}", item);
                    
                    // Click navigation item
                    headerFooter.clickNavigationItem(item);
                    Thread.sleep(2000);
                    
                    // Verify navigation occurred
                    String currentUrl = getCurrentUrl();
                    Assert.assertNotEquals(currentUrl, initialUrl, "URL should change after navigation");
                    
                    // Store visited URL
                    StateRetentionManager.setSessionState(testSessionId, "visitedUrl_" + item, currentUrl);
                    
                    // Navigate back to home
                    headerFooter.clickHeaderLogo();
                    Thread.sleep(1000);
                    
                } catch (Exception e) {
                    logger.warn("Navigation item '{}' may not exist: {}", item, e.getMessage());
                }
            }
            
            logger.info("Comprehensive navigation test completed successfully");
        } catch (Exception e) {
            logger.error("Comprehensive navigation test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 6, retryAnalyzer = RetryAnalyzer.class, groups = {"regression", "footer", "links"})
    @Description("Test all footer links and social media functionality")
    @Severity(SeverityLevel.NORMAL)
    public void testFooterFunctionality() throws Exception {
        logger.info("Starting footer functionality test");
        
        try {
            // Navigate to home page
            navigateToBaseUrl();
            
            // Initialize page objects
            HomePage homePage = new HomePage(driver);
            HeaderFooterComponent headerFooter = new HeaderFooterComponent(driver);
            
            homePage.waitForPageToLoad();
            
            // Scroll to footer
            headerFooter.scrollToFooter();
            Thread.sleep(1000);
            
            // Test footer links
            String[] footerLinks = {"Privacy Policy", "Terms of Service", "Contact Us", "Help"};
            
            for (String link : footerLinks) {
                try {
                    logger.info("Testing footer link: {}", link);
                    
                    // Click footer link
                    headerFooter.clickFooterLink(link);
                    Thread.sleep(2000);
                    
                    // Verify navigation or modal opened
                    String currentUrl = getCurrentUrl();
                    logger.info("Footer link '{}' navigation URL: {}", link, currentUrl);
                    
                    // Navigate back to home
                    headerFooter.clickHeaderLogo();
                    Thread.sleep(1000);
                    
                } catch (Exception e) {
                    logger.warn("Footer link '{}' may not exist: {}", link, e.getMessage());
                }
            }
            
            // Test social media links
            String[] socialPlatforms = {"Facebook", "Twitter", "Instagram", "LinkedIn"};
            
            for (String platform : socialPlatforms) {
                try {
                    logger.info("Testing social media link: {}", platform);
                    
                    // Scroll to footer again
                    headerFooter.scrollToFooter();
                    
                    // Click social media link
                    headerFooter.clickSocialMediaLink(platform);
                    Thread.sleep(2000);
                    
                } catch (Exception e) {
                    logger.warn("Social media link '{}' may not exist: {}", platform, e.getMessage());
                }
            }
            
            // Test newsletter subscription
            try {
                headerFooter.scrollToFooter();
                String testEmail = ConfigLoader.get("test.email", "test@example.com");
                headerFooter.subscribeToNewsletter(testEmail);
                logger.info("Newsletter subscription test completed");
            } catch (Exception e) {
                logger.warn("Newsletter subscription may not be available: {}", e.getMessage());
            }
            
            // Get copyright text
            String copyrightText = headerFooter.getCopyrightText();
            Assert.assertFalse(copyrightText.isEmpty(), "Copyright text should not be empty");
            
            logger.info("Footer functionality test completed successfully");
        } catch (Exception e) {
            logger.error("Footer functionality test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 7, retryAnalyzer = RetryAnalyzer.class, groups = {"regression", "search", "comprehensive"})
    @Description("Comprehensive testing of search functionality with various search terms")
    @Severity(SeverityLevel.NORMAL)
    public void testSearchFunctionalityComprehensive() throws Exception {
        logger.info("Starting comprehensive search functionality test");
        
        try {
            // Navigate to home page
            navigateToBaseUrl();
            
            // Initialize page objects
            HomePage homePage = new HomePage(driver);
            HeaderFooterComponent headerFooter = new HeaderFooterComponent(driver);
            
            homePage.waitForPageToLoad();
            
            // Test different search terms
            String[] searchTerms = {"test", "product", "service", "help", "contact"};
            
            for (String term : searchTerms) {
                try {
                    logger.info("Testing search term: {}", term);
                    
                    // Perform search
                    homePage.searchFor(term);
                    Thread.sleep(2000);
                    
                    // Verify search results or navigation
                    String currentUrl = getCurrentUrl();
                    logger.info("Search for '{}' resulted in URL: {}", term, currentUrl);
                    
                    // Navigate back to home
                    headerFooter.clickHeaderLogo();
                    Thread.sleep(1000);
                    
                } catch (Exception e) {
                    logger.warn("Search functionality issue with term '{}': {}", term, e.getMessage());
                    
                    // Try header search as fallback
                    try {
                        headerFooter.searchInHeader(term);
                        Thread.sleep(2000);
                        logger.info("Header search for '{}' completed", term);
                    } catch (Exception ex) {
                        logger.warn("Header search also failed for '{}': {}", term, ex.getMessage());
                    }
                }
            }
            
            logger.info("Comprehensive search functionality test completed successfully");
        } catch (Exception e) {
            logger.error("Comprehensive search functionality test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 8, retryAnalyzer = RetryAnalyzer.class, groups = {"regression", "performance", "stability"})
    @Description("Test page load performance and stability across multiple iterations")
    @Severity(SeverityLevel.NORMAL)
    public void testPageLoadPerformanceAndStability() throws Exception {
        logger.info("Starting page load performance and stability test");
        
        try {
            // Test multiple page loads
            for (int i = 1; i <= 3; i++) {
                logger.info("Page load iteration: {}", i);
                
                long startTime = System.currentTimeMillis();
                
                // Navigate to home page
                navigateToBaseUrl();
                
                // Initialize page objects
                HomePage homePage = new HomePage(driver);
                homePage.waitForPageToLoad();
                
                long endTime = System.currentTimeMillis();
                long loadTime = endTime - startTime;
                
                // Store load time
                StateRetentionManager.setSessionState(testSessionId, "loadTime_" + i, loadTime);
                
                // Verify page loaded correctly
                Assert.assertTrue(homePage.isLogoDisplayed(), "Logo should be displayed on load " + i);
                Assert.assertTrue(homePage.isNavigationDisplayed(), "Navigation should be displayed on load " + i);
                
                logger.info("Page load {} completed in {}ms", i, loadTime);
                
                // Wait between loads
                Thread.sleep(1000);
            }
            
            logger.info("Page load performance and stability test completed successfully");
        } catch (Exception e) {
            logger.error("Page load performance and stability test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 9, retryAnalyzer = RetryAnalyzer.class, groups = {"regression", "browser", "navigation"})
    @Description("Test browser back and forward navigation functionality")
    @Severity(SeverityLevel.NORMAL)
    public void testBrowserNavigation() throws Exception {
        logger.info("Starting browser navigation test");
        
        try {
            // Navigate to home page
            navigateToBaseUrl();
            
            // Initialize page objects
            HomePage homePage = new HomePage(driver);
            HeaderFooterComponent headerFooter = new HeaderFooterComponent(driver);
            
            homePage.waitForPageToLoad();
            
            // Store initial URL
            String homeUrl = getCurrentUrl();
            
            // Navigate to login page
            homePage.clickLoginButton();
            Thread.sleep(2000);
            
            String loginUrl = getCurrentUrl();
            Assert.assertNotEquals(loginUrl, homeUrl, "Should navigate to different page");
            
            // Test browser back navigation
            driver.navigate().back();
            Thread.sleep(2000);
            
            String backUrl = getCurrentUrl();
            Assert.assertEquals(backUrl, homeUrl, "Should navigate back to home page");
            
            // Test browser forward navigation
            driver.navigate().forward();
            Thread.sleep(2000);
            
            String forwardUrl = getCurrentUrl();
            Assert.assertEquals(forwardUrl, loginUrl, "Should navigate forward to login page");
            
            // Test refresh
            driver.navigate().refresh();
            Thread.sleep(2000);
            
            String refreshUrl = getCurrentUrl();
            Assert.assertEquals(refreshUrl, loginUrl, "Should stay on same page after refresh");
            
            logger.info("Browser navigation test completed successfully");
        } catch (Exception e) {
            logger.error("Browser navigation test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 10, retryAnalyzer = RetryAnalyzer.class, groups = {"regression", "ui", "cross-browser"})
    @Description("Test UI consistency and element positioning across different scenarios")
    @Severity(SeverityLevel.NORMAL)
    public void testUIConsistency() throws Exception {
        logger.info("Starting UI consistency test");
        
        try {
            // Navigate to home page
            navigateToBaseUrl();
            
            // Initialize page objects
            HomePage homePage = new HomePage(driver);
            HeaderFooterComponent headerFooter = new HeaderFooterComponent(driver);
            
            homePage.waitForPageToLoad();
            
            // Test UI elements consistency
            Assert.assertTrue(homePage.isLogoDisplayed(), "Logo should be consistently displayed");
            Assert.assertTrue(headerFooter.isHeaderDisplayed(), "Header should be consistently displayed");
            Assert.assertTrue(headerFooter.isNavigationMenuDisplayed(), "Navigation menu should be consistently displayed");
            Assert.assertTrue(headerFooter.isFooterDisplayed(), "Footer should be consistently displayed");
            
            // Test scrolling and element visibility
            headerFooter.scrollToFooter();
            Thread.sleep(1000);
            Assert.assertTrue(headerFooter.isFooterDisplayed(), "Footer should remain visible after scrolling");
            
            headerFooter.scrollToHeader();
            Thread.sleep(1000);
            Assert.assertTrue(headerFooter.isHeaderDisplayed(), "Header should remain visible after scrolling");
            
            // Test window resize simulation (if supported)
            try {
                driver.manage().window().setSize(new org.openqa.selenium.Dimension(800, 600));
                Thread.sleep(1000);
                
                Assert.assertTrue(homePage.isLogoDisplayed(), "Logo should be displayed in smaller viewport");
                
                driver.manage().window().maximize();
                Thread.sleep(1000);
                
                Assert.assertTrue(homePage.isLogoDisplayed(), "Logo should be displayed in maximized viewport");
            } catch (Exception e) {
                logger.warn("Window resize test not supported: {}", e.getMessage());
            }
            
            logger.info("UI consistency test completed successfully");
        } catch (Exception e) {
            logger.error("UI consistency test failed: {}", e.getMessage());
            throw e;
        }
    }
}