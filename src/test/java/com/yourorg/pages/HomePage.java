package com.yourorg.pages;

import com.yourorg.utils.BrowserUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class HomePage {
    private static final Logger logger = LogManager.getLogger(HomePage.class);
    private final WebDriver driver;
    private final BrowserUtils browserUtils;

    // Simple locators
    private static final By LOGO = By.id("logo");
    private static final By SEARCH_BOX = By.cssSelector(".search-box input");
    private static final By SEARCH_BUTTON = By.cssSelector(".search-box button");
    private static final By LOGIN_BUTTON = By.cssSelector(".login-btn");
    private static final By SIGNUP_BUTTON = By.cssSelector(".signup-btn");

    public HomePage(WebDriver driver) {
        this.driver = driver;
        this.browserUtils = new BrowserUtils(driver);
        logger.info("HomePage initialized");
    }

    // Simple actions
    public void clickLogo() {
        browserUtils.click(LOGO);
        logger.info("Clicked on logo");
    }

    public void searchFor(String searchTerm) {
        browserUtils.sendKeys(SEARCH_BOX, searchTerm);
        browserUtils.click(SEARCH_BUTTON);
        logger.info("Searched for: {}", searchTerm);
    }

    public void clickLoginButton() {
        browserUtils.clickWithFallback(
            LOGIN_BUTTON,
            By.xpath("//button[contains(text(), 'Login')]"),
            By.id("login-button")
        );
        logger.info("Clicked login button");
    }

    public void clickSignupButton() {
        browserUtils.click(SIGNUP_BUTTON);
        logger.info("Clicked signup button");
    }

    // Validation methods
    public boolean isLogoDisplayed() {
        return browserUtils.isElementDisplayed(LOGO);
    }

    public boolean isSearchBoxDisplayed() {
        return browserUtils.isElementDisplayed(SEARCH_BOX);
    }

    public void waitForPageToLoad() {
        browserUtils.waitForElementToBeVisible(LOGO, 15);
        browserUtils.waitForElementToBeVisible(SEARCH_BOX, 10);
        logger.info("Home page loaded successfully");
    }

    public String getPageTitle() {
        return driver.getTitle();
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}