package com.yourorg.pages;

import com.yourorg.utils.BrowserUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage {
    private static final Logger logger = LogManager.getLogger(LoginPage.class);
    private final WebDriver driver;
    private final BrowserUtils browserUtils;

    // Simple locators
    private static final By USERNAME_FIELD = By.id("username");
    private static final By PASSWORD_FIELD = By.id("password");
    private static final By LOGIN_BUTTON = By.cssSelector(".login-submit");
    private static final By ERROR_MESSAGE = By.cssSelector(".error-message");
    private static final By FORGOT_PASSWORD_LINK = By.cssSelector(".forgot-password");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.browserUtils = new BrowserUtils(driver);
        logger.info("LoginPage initialized");
    }

    // Simple actions
    public void enterUsername(String username) {
        browserUtils.sendKeysWithFallback(username,
            USERNAME_FIELD,
            By.name("username"),
            By.xpath("//input[@placeholder='Username']")
        );
        logger.info("Entered username: {}", username);
    }

    public void enterPassword(String password) {
        browserUtils.sendKeysWithFallback(password,
            PASSWORD_FIELD,
            By.name("password"),
            By.xpath("//input[@type='password']")
        );
        logger.info("Entered password");
    }

    public void clickLoginButton() {
        browserUtils.clickWithFallback(
            LOGIN_BUTTON,
            By.xpath("//button[contains(text(), 'Login')]"),
            By.xpath("//input[@type='submit']")
        );
        logger.info("Clicked login button");
    }

    public void login(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickLoginButton();
        logger.info("Performed login with username: {}", username);
    }

    public void clickForgotPassword() {
        browserUtils.click(FORGOT_PASSWORD_LINK);
        logger.info("Clicked forgot password link");
    }

    // Validation methods
    public String getErrorMessage() {
        if (browserUtils.isElementDisplayed(ERROR_MESSAGE)) {
            return browserUtils.getText(ERROR_MESSAGE);
        }
        return "";
    }

    public boolean isErrorMessageDisplayed() {
        return browserUtils.isElementDisplayed(ERROR_MESSAGE);
    }

    public boolean isUsernameFieldDisplayed() {
        return browserUtils.isElementDisplayed(USERNAME_FIELD);
    }

    public boolean isPasswordFieldDisplayed() {
        return browserUtils.isElementDisplayed(PASSWORD_FIELD);
    }

    public boolean isLoginButtonDisplayed() {
        return browserUtils.isElementDisplayed(LOGIN_BUTTON);
    }

    public void waitForPageToLoad() {
        browserUtils.waitForElementToBeVisible(USERNAME_FIELD, 15);
        browserUtils.waitForElementToBeVisible(PASSWORD_FIELD, 10);
        browserUtils.waitForElementToBeVisible(LOGIN_BUTTON, 10);
        logger.info("Login page loaded successfully");
    }

    public void clearFields() {
        browserUtils.clearField(USERNAME_FIELD);
        browserUtils.clearField(PASSWORD_FIELD);
        logger.info("Cleared login fields");
    }
}