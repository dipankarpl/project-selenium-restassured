package com.yourorg.pages;

import com.yourorg.browser.BrowserUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Home Page using improved locator-based approach
 * No WebElement variables - direct locator usage with BrowserUtils
 */
public class HomePage {
    private static final Logger logger = LogManager.getLogger(HomePage.class);
    private final WebDriver driver;
    private final BrowserUtils browserUtils;

    // Page Locators - defined as constants
    private static final By LOGO = By.id("logo");
    private static final By MAIN_NAVIGATION = By.xpath("//nav[@class='main-nav']");
    private static final By SEARCH_BOX = By.cssSelector(".search-box input");
    private static final By SEARCH_BUTTON = By.cssSelector(".search-box button");
    private static final By HERO_SECTION = By.cssSelector(".hero-section");
    private static final By FEATURED_PRODUCTS = By.cssSelector(".featured-products");
    
    // Login/Signup buttons with fallback locators
    private static final By LOGIN_BUTTON_PRIMARY = By.cssSelector(".login-btn");
    private static final By LOGIN_BUTTON_FALLBACK1 = By.xpath("//button[contains(text(), 'Login')]");
    private static final By LOGIN_BUTTON_FALLBACK2 = By.xpath("//a[contains(text(), 'Sign In')]");
    private static final By LOGIN_BUTTON_FALLBACK3 = By.id("login-button");
    
    private static final By SIGNUP_BUTTON = By.cssSelector(".signup-btn");

    // Constructor
    public HomePage(WebDriver driver) throws Exception {
        this.driver = driver;
        this.browserUtils = new BrowserUtils(driver);
        logger.info("HomePage initialized");
    }

    // Page Actions using direct locator approach
    public boolean isLogoDisplayed() {
        try {
            return browserUtils.isElementDisplayed(LOGO);
        } catch (Exception e) {
            logger.error("Error checking logo visibility: {}", e.getMessage());
            return false;
        }
    }

    public void clickLogo() throws Exception {
        try {
            browserUtils.click(LOGO);
            logger.info("Clicked on logo");
        } catch (Exception e) {
            logger.error("Failed to click logo: {}", e.getMessage());
            throw new Exception("Logo click failed", e);
        }
    }

    public void searchFor(String searchTerm) throws Exception {
        try {
            browserUtils.sendKeys(SEARCH_BOX, searchTerm);
            browserUtils.click(SEARCH_BUTTON);
            logger.info("Searched for: {}", searchTerm);
        } catch (Exception e) {
            logger.error("Failed to search for '{}': {}", searchTerm, e.getMessage());
            throw new Exception("Search failed for term: " + searchTerm, e);
        }
    }

    public void clickLoginButton() throws Exception {
        try {
            // Use fallback approach for better reliability
            browserUtils.clickWithFallback(
                LOGIN_BUTTON_PRIMARY,
                LOGIN_BUTTON_FALLBACK1,
                LOGIN_BUTTON_FALLBACK2,
                LOGIN_BUTTON_FALLBACK3
            );
            logger.info("Clicked on login button");
        } catch (Exception e) {
            logger.error("Failed to click login button: {}", e.getMessage());
            throw new Exception("Login button click failed", e);
        }
    }

    public void clickSignupButton() throws Exception {
        try {
            browserUtils.click(SIGNUP_BUTTON);
            logger.info("Clicked on signup button");
        } catch (Exception e) {
            logger.error("Failed to click signup button: {}", e.getMessage());
            throw new Exception("Signup button click failed", e);
        }
    }

    public boolean isHeroSectionDisplayed() {
        try {
            return browserUtils.isElementDisplayed(HERO_SECTION);
        } catch (Exception e) {
            logger.error("Error checking hero section visibility: {}", e.getMessage());
            return false;
        }
    }

    public boolean isNavigationDisplayed() {
        try {
            return browserUtils.isElementDisplayed(MAIN_NAVIGATION);
        } catch (Exception e) {
            logger.error("Error checking navigation visibility: {}", e.getMessage());
            return false;
        }
    }

    public boolean isFeaturedProductsDisplayed() {
        try {
            return browserUtils.isElementDisplayed(FEATURED_PRODUCTS);
        } catch (Exception e) {
            logger.error("Error checking featured products visibility: {}", e.getMessage());
            return false;
        }
    }

    public String getPageTitle() {
        try {
            return driver.getTitle();
        } catch (Exception e) {
            logger.error("Failed to get page title: {}", e.getMessage());
            return "";
        }
    }

    public String getCurrentUrl() {
        try {
            return driver.getCurrentUrl();
        } catch (Exception e) {
            logger.error("Failed to get current URL: {}", e.getMessage());
            return "";
        }
    }

    public void scrollToFeaturedProducts() throws Exception {
        try {
            browserUtils.scrollToElement(FEATURED_PRODUCTS);
            logger.info("Scrolled to featured products section");
        } catch (Exception e) {
            logger.error("Failed to scroll to featured products: {}", e.getMessage());
            throw new Exception("Scroll to featured products failed", e);
        }
    }

    public void waitForPageToLoad() throws Exception {
        try {
            // Wait for multiple key elements to ensure page is loaded
            browserUtils.waitForElementToBeVisible(LOGO, 15);
            browserUtils.waitForElementToBeVisible(MAIN_NAVIGATION, 10);
            
            logger.info("Home page loaded successfully");
        } catch (Exception e) {
            logger.error("Home page failed to load: {}", e.getMessage());
            throw new Exception("Home page load failed", e);
        }
    }
    
    // Additional utility methods
    public void performQuickSearch(String searchTerm) throws Exception {
        try {
            // Clear search box first, then enter text
            browserUtils.clearField(SEARCH_BOX);
            browserUtils.sendKeys(SEARCH_BOX, searchTerm);
            browserUtils.click(SEARCH_BUTTON);
            logger.info("Performed quick search for: {}", searchTerm);
        } catch (Exception e) {
            logger.error("Quick search failed for '{}': {}", searchTerm, e.getMessage());
            throw new Exception("Quick search failed", e);
        }
    }
    
    public boolean isPageFullyLoaded() {
        try {
            return browserUtils.isElementDisplayed(LOGO) &&
                   browserUtils.isElementDisplayed(MAIN_NAVIGATION) &&
                   browserUtils.isElementDisplayed(HERO_SECTION);
        } catch (Exception e) {
            logger.error("Error checking if page is fully loaded: {}", e.getMessage());
            return false;
        }
    }
    
    public int getFeaturedProductsCount() {
        try {
            By productItems = By.cssSelector(".featured-products .product-item");
            return browserUtils.getElementCount(productItems);
        } catch (Exception e) {
            logger.error("Failed to get featured products count: {}", e.getMessage());
            return 0;
        }
    }
}