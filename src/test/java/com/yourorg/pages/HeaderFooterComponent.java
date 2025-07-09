package com.yourorg.pages;

import com.yourorg.browser.BrowserUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Header Footer Component using improved locator-based approach
 * No WebElement variables - direct locator usage with BrowserUtils
 */
public class HeaderFooterComponent {
    private static final Logger logger = LogManager.getLogger(HeaderFooterComponent.class);
    private final WebDriver driver;
    private final BrowserUtils browserUtils;

    // Header Locators - defined as constants
    private static final By HEADER = By.cssSelector(".header");
    private static final By HEADER_LOGO = By.cssSelector(".header .logo");
    private static final By NAVIGATION_MENU = By.cssSelector(".header .nav-menu");
    private static final By USER_MENU = By.cssSelector(".header .user-menu");
    private static final By CART_ICON = By.cssSelector(".header .cart-icon");
    private static final By SEARCH_BAR = By.cssSelector(".header .search-bar");

    // Footer Locators - defined as constants
    private static final By FOOTER = By.cssSelector(".footer");
    private static final By FOOTER_LINKS = By.cssSelector(".footer .links");
    private static final By SOCIAL_MEDIA_LINKS = By.cssSelector(".footer .social-media");
    private static final By NEWSLETTER_SECTION = By.cssSelector(".footer .newsletter");
    private static final By COPYRIGHT_TEXT = By.cssSelector(".footer .copyright");

    // Constructor
    public HeaderFooterComponent(WebDriver driver) throws Exception {
        this.driver = driver;
        this.browserUtils = new BrowserUtils(driver);
        logger.info("HeaderFooterComponent initialized");
    }

    // Header Actions
    public boolean isHeaderDisplayed() throws Exception {
        try {
            return browserUtils.isElementDisplayed(HEADER);
        } catch (Exception e) {
            logger.error("Error checking header visibility: {}", e.getMessage());
            return false;
        }
    }

    public void clickHeaderLogo() throws Exception {
        try {
            browserUtils.clickWithFallback(
                By.cssSelector(".header .logo"),
                By.cssSelector(".logo"),
                By.xpath("//header//img[@alt='Logo']"),
                By.id("logo")
            );
            logger.info("Clicked header logo");
        } catch (Exception e) {
            logger.error("Failed to click header logo: {}", e.getMessage());
            throw new Exception("Header logo click failed", e);
        }
    }

    public void clickNavigationItem(String itemName) throws Exception {
        try {
            browserUtils.clickWithFallback(
                By.xpath("//nav//a[contains(text(), '" + itemName + "')]"),
                By.xpath("//header//a[contains(text(), '" + itemName + "')]"),
                By.cssSelector(".nav-menu a[href*='" + itemName.toLowerCase() + "']"),
                By.xpath("//ul[@class='nav-menu']//a[contains(text(), '" + itemName + "')]")
            );
            logger.info("Clicked navigation item: {}", itemName);
        } catch (Exception e) {
            logger.error("Failed to click navigation item '{}': {}", itemName, e.getMessage());
            throw new Exception("Navigation item click failed for: " + itemName, e);
        }
    }

    public void clickUserMenu() throws Exception {
        try {
            browserUtils.clickWithFallback(
                By.cssSelector(".header .user-menu"),
                By.cssSelector(".user-menu"),
                By.xpath("//header//div[@class='user-menu']"),
                By.cssSelector(".profile-dropdown")
            );
            logger.info("Clicked user menu");
        } catch (Exception e) {
            logger.error("Failed to click user menu: {}", e.getMessage());
            throw new Exception("User menu click failed", e);
        }
    }

    public void clickCartIcon() throws Exception {
        try {
            browserUtils.clickWithFallback(
                By.cssSelector(".header .cart-icon"),
                By.cssSelector(".cart-icon"),
                By.xpath("//header//a[@class='cart-icon']"),
                By.cssSelector(".shopping-cart")
            );
            logger.info("Clicked cart icon");
        } catch (Exception e) {
            logger.error("Failed to click cart icon: {}", e.getMessage());
            throw new Exception("Cart icon click failed", e);
        }
    }

    public void searchInHeader(String searchTerm) throws Exception {
        try {
            browserUtils.sendKeysWithFallback(searchTerm,
                By.cssSelector(".header .search-bar input"),
                By.cssSelector(".search-bar input"),
                By.xpath("//header//input[@type='search']"),
                By.cssSelector(".search-input")
            );
            
            // Find and click search button
            browserUtils.clickWithFallback(
                By.cssSelector(".header .search-bar button"),
                By.cssSelector(".search-bar button"),
                By.xpath("//header//button[@type='submit']"),
                By.cssSelector(".search-btn")
            );
            logger.info("Searched for: {}", searchTerm);
        } catch (Exception e) {
            logger.error("Failed to search for '{}': {}", searchTerm, e.getMessage());
            throw new Exception("Header search failed for term: " + searchTerm, e);
        }
    }

    // Footer Actions
    public boolean isFooterDisplayed() throws Exception {
        try {
            return browserUtils.isElementDisplayed(FOOTER);
        } catch (Exception e) {
            logger.error("Error checking footer visibility: {}", e.getMessage());
            return false;
        }
    }

    public void clickFooterLink(String linkText) throws Exception {
        try {
            browserUtils.clickWithFallback(
                By.xpath("//footer//a[contains(text(), '" + linkText + "')]"),
                By.xpath("//div[@class='footer']//a[contains(text(), '" + linkText + "')]"),
                By.cssSelector(".footer a[href*='" + linkText.toLowerCase() + "']"),
                By.xpath("//footer//a[@title='" + linkText + "']")
            );
            logger.info("Clicked footer link: {}", linkText);
        } catch (Exception e) {
            logger.error("Failed to click footer link '{}': {}", linkText, e.getMessage());
            throw new Exception("Footer link click failed for: " + linkText, e);
        }
    }

    public void clickSocialMediaLink(String platform) throws Exception {
        try {
            browserUtils.clickWithFallback(
                By.xpath("//footer//a[@class='social-" + platform.toLowerCase() + "']"),
                By.xpath("//footer//a[contains(@href, '" + platform.toLowerCase() + "')]"),
                By.cssSelector(".social-media a[href*='" + platform.toLowerCase() + "']"),
                By.xpath("//footer//a[@title='" + platform + "']")
            );
            logger.info("Clicked social media link: {}", platform);
        } catch (Exception e) {
            logger.error("Failed to click social media link '{}': {}", platform, e.getMessage());
            throw new Exception("Social media link click failed for: " + platform, e);
        }
    }

    public void subscribeToNewsletter(String email) throws Exception {
        try {
            browserUtils.sendKeysWithFallback(email,
                By.cssSelector(".footer .newsletter input[type='email']"),
                By.cssSelector(".newsletter input[type='email']"),
                By.xpath("//footer//input[@placeholder='Email']"),
                By.cssSelector(".newsletter-input")
            );
            
            // Find and click subscribe button
            browserUtils.clickWithFallback(
                By.cssSelector(".footer .newsletter button"),
                By.cssSelector(".newsletter button"),
                By.xpath("//footer//button[contains(text(), 'Subscribe')]"),
                By.cssSelector(".newsletter-btn")
            );
            logger.info("Subscribed to newsletter with email: {}", email);
        } catch (Exception e) {
            logger.error("Failed to subscribe to newsletter with email '{}': {}", email, e.getMessage());
            throw new Exception("Newsletter subscription failed for email: " + email, e);
        }
    }

    public String getCopyrightText() throws Exception {
        try {
            String text = browserUtils.getTextWithFallback(
                By.cssSelector(".footer .copyright"),
                By.cssSelector(".copyright"),
                By.xpath("//footer//p[contains(text(), '©')]"),
                By.cssSelector(".footer-copyright")
            );
            logger.info("Retrieved copyright text: {}", text);
            return text;
        } catch (Exception e) {
            logger.error("Failed to get copyright text: {}", e.getMessage());
            throw new Exception("Failed to get copyright text", e);
        }
    }

    public void scrollToFooter() throws Exception {
        try {
            browserUtils.scrollToElement(FOOTER);
            logger.info("Scrolled to footer");
        } catch (Exception e) {
            logger.error("Failed to scroll to footer: {}", e.getMessage());
            throw new Exception("Scroll to footer failed", e);
        }
    }

    public void scrollToHeader() throws Exception {
        try {
            browserUtils.scrollToElement(HEADER);
            logger.info("Scrolled to header");
        } catch (Exception e) {
            logger.error("Failed to scroll to header: {}", e.getMessage());
            throw new Exception("Scroll to header failed", e);
        }
    }

    public boolean isNavigationMenuDisplayed() throws Exception {
        try {
            return browserUtils.isElementDisplayed(NAVIGATION_MENU);
        } catch (Exception e) {
            logger.error("Error checking navigation menu visibility: {}", e.getMessage());
            return false;
        }
    }

    public boolean isUserMenuDisplayed() throws Exception {
        try {
            return browserUtils.isElementDisplayed(USER_MENU);
        } catch (Exception e) {
            logger.error("Error checking user menu visibility: {}", e.getMessage());
            return false;
        }
    }

    public boolean isCartIconDisplayed() throws Exception {
        try {
            return browserUtils.isElementDisplayed(CART_ICON);
        } catch (Exception e) {
            logger.error("Error checking cart icon visibility: {}", e.getMessage());
            return false;
        }
    }

    public boolean isSearchBarDisplayed() throws Exception {
        try {
            return browserUtils.isElementDisplayed(SEARCH_BAR);
        } catch (Exception e) {
            logger.error("Error checking search bar visibility: {}", e.getMessage());
            return false;
        }
    }
}