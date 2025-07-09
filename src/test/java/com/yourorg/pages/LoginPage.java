package com.yourorg.pages;

import com.yourorg.browser.BrowserUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Login Page using improved locator-based approach
 * No WebElement variables - direct locator usage with BrowserUtils
 */
public class LoginPage {
    private static final Logger logger = LogManager.getLogger(LoginPage.class);
    private final WebDriver driver;
    private final BrowserUtils browserUtils;

    // Page Locators - defined as constants
    private static final By USERNAME_FIELD_PRIMARY = By.id("username");
    private static final By USERNAME_FIELD_FALLBACK1 = By.name("username");
    private static final By USERNAME_FIELD_FALLBACK2 = By.cssSelector("input[type='text']");
    private static final By USERNAME_FIELD_FALLBACK3 = By.xpath("//input[@placeholder='Username']");
    
    private static final By PASSWORD_FIELD_PRIMARY = By.id("password");
    private static final By PASSWORD_FIELD_FALLBACK1 = By.name("password");
    private static final By PASSWORD_FIELD_FALLBACK2 = By.cssSelector("input[type='password']");
    private static final By PASSWORD_FIELD_FALLBACK3 = By.xpath("//input[@placeholder='Password']");
    
    private static final By LOGIN_BUTTON_PRIMARY = By.cssSelector(".login-submit");
    private static final By LOGIN_BUTTON_FALLBACK1 = By.xpath("//button[contains(text(), 'Login')]");
    private static final By LOGIN_BUTTON_FALLBACK2 = By.xpath("//input[@type='submit']");
    private static final By LOGIN_BUTTON_FALLBACK3 = By.id("login-btn");
    
    private static final By FORGOT_PASSWORD_LINK = By.cssSelector(".forgot-password");
    private static final By ERROR_MESSAGE = By.cssSelector(".error-message");
    private static final By SUCCESS_MESSAGE = By.cssSelector(".success-message");
    private static final By REMEMBER_ME_CHECKBOX = By.cssSelector(".remember-me");
    private static final By SIGNUP_LINK = By.cssSelector(".signup-link");

    // Constructor
    public LoginPage(WebDriver driver) throws Exception {
        this.driver = driver;
        this.browserUtils = new BrowserUtils(driver);
        logger.info("LoginPage initialized");
    }

    // Page Actions using direct locator approach
    public void enterUsername(String username) throws Exception {
        try {
            browserUtils.sendKeysWithFallback(username,
                USERNAME_FIELD_PRIMARY,
                USERNAME_FIELD_FALLBACK1,
                USERNAME_FIELD_FALLBACK2,
                USERNAME_FIELD_FALLBACK3
            );
            logger.info("Entered username: {}", username);
        } catch (Exception e) {
            logger.error("Failed to enter username: {}", e.getMessage());
            throw new Exception("Username entry failed", e);
        }
    }

    public void enterPassword(String password) throws Exception {
        try {
            browserUtils.sendKeysWithFallback(password,
                PASSWORD_FIELD_PRIMARY,
                PASSWORD_FIELD_FALLBACK1,
                PASSWORD_FIELD_FALLBACK2,
                PASSWORD_FIELD_FALLBACK3
            );
            logger.info("Entered password");
        } catch (Exception e) {
            logger.error("Failed to enter password: {}", e.getMessage());
            throw new Exception("Password entry failed", e);
        }
    }

    public void clickLoginButton() throws Exception {
        try {
            browserUtils.clickWithFallback(
                LOGIN_BUTTON_PRIMARY,
                LOGIN_BUTTON_FALLBACK1,
                LOGIN_BUTTON_FALLBACK2,
                LOGIN_BUTTON_FALLBACK3
            );
            logger.info("Clicked login button");
        } catch (Exception e) {
            logger.error("Failed to click login button: {}", e.getMessage());
            throw new Exception("Login button click failed", e);
        }
    }

    public void login(String username, String password) throws Exception {
        try {
            enterUsername(username);
            enterPassword(password);
            clickLoginButton();
            logger.info("Performed login with username: {}", username);
        } catch (Exception e) {
            logger.error("Login failed for username '{}': {}", username, e.getMessage());
            throw new Exception("Login failed for username: " + username, e);
        }
    }

    public void loginWithRememberMe(String username, String password) throws Exception {
        try {
            enterUsername(username);
            enterPassword(password);
            checkRememberMe();
            clickLoginButton();
            logger.info("Performed login with remember me for username: {}", username);
        } catch (Exception e) {
            logger.error("Login with remember me failed for username '{}': {}", username, e.getMessage());
            throw new Exception("Login with remember me failed for username: " + username, e);
        }
    }

    public void checkRememberMe() throws Exception {
        try {
            if (!browserUtils.isElementSelected(REMEMBER_ME_CHECKBOX)) {
                browserUtils.click(REMEMBER_ME_CHECKBOX);
                logger.info("Checked remember me checkbox");
            }
        } catch (Exception e) {
            logger.error("Failed to check remember me: {}", e.getMessage());
            throw new Exception("Remember me check failed", e);
        }
    }

    public void clickForgotPassword() throws Exception {
        try {
            browserUtils.click(FORGOT_PASSWORD_LINK);
            logger.info("Clicked forgot password link");
        } catch (Exception e) {
            logger.error("Failed to click forgot password: {}", e.getMessage());
            throw new Exception("Forgot password click failed", e);
        }
    }

    public void clickSignupLink() throws Exception {
        try {
            browserUtils.click(SIGNUP_LINK);
            logger.info("Clicked signup link");
        } catch (Exception e) {
            logger.error("Failed to click signup link: {}", e.getMessage());
            throw new Exception("Signup link click failed", e);
        }
    }

    public String getErrorMessage() throws Exception {
        try {
            if (browserUtils.isElementDisplayed(ERROR_MESSAGE)) {
                String message = browserUtils.getText(ERROR_MESSAGE);
                logger.info("Retrieved error message: {}", message);
                return message;
            }
            return "";
        } catch (Exception e) {
            logger.error("Failed to get error message: {}", e.getMessage());
            return "";
        }
    }

    public String getSuccessMessage() throws Exception {
        try {
            if (browserUtils.isElementDisplayed(SUCCESS_MESSAGE)) {
                String message = browserUtils.getText(SUCCESS_MESSAGE);
                logger.info("Retrieved success message: {}", message);
                return message;
            }
            return "";
        } catch (Exception e) {
            logger.error("Failed to get success message: {}", e.getMessage());
            return "";
        }
    }

    public boolean isErrorMessageDisplayed() {
        try {
            return browserUtils.isElementDisplayed(ERROR_MESSAGE);
        } catch (Exception e) {
            logger.error("Error checking error message visibility: {}", e.getMessage());
            return false;
        }
    }

    public boolean isSuccessMessageDisplayed() {
        try {
            return browserUtils.isElementDisplayed(SUCCESS_MESSAGE);
        } catch (Exception e) {
            logger.error("Error checking success message visibility: {}", e.getMessage());
            return false;
        }
    }

    public boolean isUsernameFieldDisplayed() {
        try {
            return browserUtils.isElementDisplayed(USERNAME_FIELD_PRIMARY);
        } catch (Exception e) {
            logger.error("Error checking username field visibility: {}", e.getMessage());
            return false;
        }
    }

    public boolean isPasswordFieldDisplayed() {
        try {
            return browserUtils.isElementDisplayed(PASSWORD_FIELD_PRIMARY);
        } catch (Exception e) {
            logger.error("Error checking password field visibility: {}", e.getMessage());
            return false;
        }
    }

    public boolean isLoginButtonDisplayed() {
        try {
            return browserUtils.isElementDisplayed(LOGIN_BUTTON_PRIMARY);
        } catch (Exception e) {
            logger.error("Error checking login button visibility: {}", e.getMessage());
            return false;
        }
    }

    public void waitForPageToLoad() throws Exception {
        try {
            // Wait for key form elements to be visible
            browserUtils.waitForElementToBeVisible(USERNAME_FIELD_PRIMARY, 15);
            browserUtils.waitForElementToBeVisible(PASSWORD_FIELD_PRIMARY, 10);
            browserUtils.waitForElementToBeVisible(LOGIN_BUTTON_PRIMARY, 10);
            
            logger.info("Login page loaded successfully");
        } catch (Exception e) {
            logger.error("Login page failed to load: {}", e.getMessage());
            throw new Exception("Login page load failed", e);
        }
    }

    public void clearFields() throws Exception {
        try {
            browserUtils.clearField(USERNAME_FIELD_PRIMARY);
            browserUtils.clearField(PASSWORD_FIELD_PRIMARY);
            logger.info("Cleared login fields");
        } catch (Exception e) {
            logger.error("Failed to clear login fields: {}", e.getMessage());
            throw new Exception("Clear fields failed", e);
        }
    }
    
    // Additional utility methods
    public void quickLogin(String username, String password) throws Exception {
        try {
            // Clear fields first, then perform login
            clearFields();
            login(username, password);
            logger.info("Performed quick login for: {}", username);
        } catch (Exception e) {
            logger.error("Quick login failed for '{}': {}", username, e.getMessage());
            throw new Exception("Quick login failed", e);
        }
    }
    
    public boolean isLoginFormReady() {
        try {
            return browserUtils.isElementDisplayed(USERNAME_FIELD_PRIMARY) &&
                   browserUtils.isElementDisplayed(PASSWORD_FIELD_PRIMARY) &&
                   browserUtils.isElementDisplayed(LOGIN_BUTTON_PRIMARY) &&
                   browserUtils.isElementEnabled(LOGIN_BUTTON_PRIMARY);
        } catch (Exception e) {
            logger.error("Error checking if login form is ready: {}", e.getMessage());
            return false;
        }
    }
    
    public void focusOnUsernameField() throws Exception {
        try {
            browserUtils.click(USERNAME_FIELD_PRIMARY);
            logger.info("Focused on username field");
        } catch (Exception e) {
            logger.error("Failed to focus on username field: {}", e.getMessage());
            throw new Exception("Focus on username field failed", e);
        }
    }
}