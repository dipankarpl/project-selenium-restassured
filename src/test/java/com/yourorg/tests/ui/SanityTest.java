package com.yourorg.tests.ui;

import com.yourorg.base.BaseTest;
import com.yourorg.pages.HomePage;
import com.yourorg.pages.LoginPage;
import com.yourorg.utils.ConfigLoader;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SanityTest extends BaseTest {
    private static final Logger logger = LogManager.getLogger(SanityTest.class);

    @Test(priority = 1, groups = {"sanity", "critical"})
    @Description("Verify home page loads successfully")
    @Severity(SeverityLevel.BLOCKER)
    public void testHomePageLoads() throws Exception {
        logger.info("Starting home page load test");
        
        navigateToBaseUrl();
        
        HomePage homePage = new HomePage(driver);
        homePage.waitForPageToLoad();
        
        Assert.assertTrue(homePage.isLogoDisplayed(), "Logo should be displayed");
        Assert.assertTrue(homePage.isSearchBoxDisplayed(), "Search box should be displayed");
        
        String pageTitle = homePage.getPageTitle();
        Assert.assertFalse(pageTitle.isEmpty(), "Page title should not be empty");
        
        logger.info("Home page load test completed successfully");
    }

    @Test(priority = 2, groups = {"sanity", "critical"})
    @Description("Verify login page loads successfully")
    @Severity(SeverityLevel.CRITICAL)
    public void testLoginPageLoads() throws Exception {
        logger.info("Starting login page load test");
        
        navigateToBaseUrl();
        
        HomePage homePage = new HomePage(driver);
        homePage.waitForPageToLoad();
        homePage.clickLoginButton();
        
        LoginPage loginPage = new LoginPage(driver);
        loginPage.waitForPageToLoad();
        
        Assert.assertTrue(loginPage.isUsernameFieldDisplayed(), "Username field should be displayed");
        Assert.assertTrue(loginPage.isPasswordFieldDisplayed(), "Password field should be displayed");
        Assert.assertTrue(loginPage.isLoginButtonDisplayed(), "Login button should be displayed");
        
        logger.info("Login page load test completed successfully");
    }

    @Test(priority = 3, groups = {"sanity", "authentication"})
    @Description("Verify login functionality with valid credentials")
    @Severity(SeverityLevel.BLOCKER)
    public void testValidLogin() throws Exception {
        logger.info("Starting valid login test");
        
        navigateToBaseUrl();
        
        HomePage homePage = new HomePage(driver);
        homePage.waitForPageToLoad();
        homePage.clickLoginButton();
        
        LoginPage loginPage = new LoginPage(driver);
        loginPage.waitForPageToLoad();
        
        String username = ConfigLoader.get("test.username", "testuser");
        String password = ConfigLoader.get("test.password", "testpass");
        
        loginPage.login(username, password);
        Thread.sleep(2000);
        
        String currentUrl = getCurrentUrl();
        Assert.assertFalse(currentUrl.contains("login"), "Should not be on login page after successful login");
        
        logger.info("Valid login test completed successfully");
    }

    @Test(priority = 4, groups = {"sanity", "negative"})
    @Description("Verify error handling for invalid login")
    @Severity(SeverityLevel.NORMAL)
    public void testInvalidLogin() throws Exception {
        logger.info("Starting invalid login test");
        
        navigateToBaseUrl();
        
        HomePage homePage = new HomePage(driver);
        homePage.waitForPageToLoad();
        homePage.clickLoginButton();
        
        LoginPage loginPage = new LoginPage(driver);
        loginPage.waitForPageToLoad();
        
        loginPage.login("invalid_user", "invalid_pass");
        Thread.sleep(2000);
        
        Assert.assertTrue(loginPage.isErrorMessageDisplayed(), "Error message should be displayed");
        
        String errorMessage = loginPage.getErrorMessage();
        Assert.assertFalse(errorMessage.isEmpty(), "Error message should not be empty");
        
        logger.info("Invalid login test completed successfully");
    }

    @Test(priority = 5, groups = {"sanity", "search"})
    @Description("Verify search functionality works")
    @Severity(SeverityLevel.NORMAL)
    public void testSearchFunctionality() throws Exception {
        logger.info("Starting search functionality test");
        
        navigateToBaseUrl();
        
        HomePage homePage = new HomePage(driver);
        homePage.waitForPageToLoad();
        
        String searchTerm = ConfigLoader.get("test.search.term", "test");
        homePage.searchFor(searchTerm);
        Thread.sleep(2000);
        
        String currentUrl = getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("search") || currentUrl.contains(searchTerm), 
                "URL should contain search term or search indicator");
        
        logger.info("Search functionality test completed successfully");
    }
}