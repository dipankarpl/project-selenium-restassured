# Test Automation Framework

A simple, scalable test automation framework built with Java, Selenium, TestNG, and RestAssured.

## Framework Architecture

The framework is organized in simple layers, each with a specific purpose:

```
src/test/java/com/yourorg/
├── base/              # Foundation layer - Base test classes
├── pages/             # Page layer - Page objects for UI
├── api/               # API layer - API testing components  
├── utils/             # Utility layer - Common helper functions
├── tests/             # Test layer - Actual test cases
├── dataproviders/     # Data layer - Test data management
├── listeners/         # Reporting layer - Test listeners and reports
└── browser/           # Browser layer - WebDriver utilities
```

### Layer Explanation:

**Base Layer** - Contains foundation classes that all tests inherit from
**Page Layer** - Contains page objects representing UI pages
**API Layer** - Contains API testing utilities and request builders
**Utils Layer** - Contains reusable utility functions
**Test Layer** - Contains actual test cases (UI and API)
**Data Layer** - Manages test data from different sources
**Reporting Layer** - Handles test reporting and logging
**Browser Layer** - Manages browser interactions and WebDriver

## Key Framework Questions & Answers

### 1. What is your approach to designing a scalable automation framework from scratch?

**Simple Layered Approach:**
```java
// Base Test - Foundation for all tests
public class BaseTest {
    protected WebDriver driver;
    
    @BeforeMethod
    public void setup() {
        driver = RemoteWebDriverFactory.createDriver();
    }
    
    @AfterMethod  
    public void teardown() {
        RemoteWebDriverFactory.quitDriver();
    }
}
```

**Key Principles:**
- Each layer has one responsibility
- Easy to add new features
- Reusable components
- Clear separation of concerns

### 2. How do you implement Page Object Model (POM) in Selenium projects?

**Simple Page Object:**
```java
public class HomePage {
    private final WebDriver driver;
    private final BrowserUtils browserUtils;

    // Locators as constants
    private static final By LOGO = By.id("logo");
    private static final By SEARCH_BOX = By.cssSelector(".search-box input");
    private static final By LOGIN_BUTTON = By.cssSelector(".login-btn");
    
    public HomePage(WebDriver driver) throws Exception {
        this.driver = driver;
        this.browserUtils = new BrowserUtils(driver);
    }

    // Simple actions
    public void clickLogo() throws Exception {
        browserUtils.click(LOGO);
    }
    
    public void searchFor(String searchTerm) throws Exception {
        browserUtils.sendKeys(SEARCH_BOX, searchTerm);
    }
}
```

**Benefits:**
- No WebElement variables (more reliable)
- Direct locator usage
- Simple action methods
- Easy to maintain

### 3. How do you manage WebDriver instances in your framework?

**Thread-Safe WebDriver Management:**
```java
public class RemoteWebDriverFactory {
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    public static WebDriver createDriver() {
        String browserName = EnvReader.get("BROWSER", "chrome");
        boolean headless = EnvReader.getBoolean("HEADLESS", false);
        
        WebDriver driver = createLocalDriver(browserName, headless);
        configureDriver(driver);
        driverThreadLocal.set(driver);
        return driver;
    }
    
    public static WebDriver getDriver() {
        return driverThreadLocal.get();
    }
    
    public static void quitDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            driver.quit();
            driverThreadLocal.remove();
        }
    }
}
```

**Key Features:**
- Thread-safe for parallel execution
- Supports multiple browsers
- Automatic cleanup
- Environment-based configuration

### 4. What are common utilities you include in your automation framework?

**Browser Utilities:**
```java
public class BrowserUtils {
    private final WebDriver driver;
    
    public void click(By locator) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
        element.click();
    }
    
    public void sendKeys(By locator, String text) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        element.clear();
        element.sendKeys(text);
    }
    
    public String getText(By locator) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        return element.getText();
    }
}
```

**Screenshot Utilities:**
```java
public class ScreenshotUtils {
    public static String captureScreenshot(WebDriver driver, String testName) {
        TakesScreenshot screenshot = (TakesScreenshot) driver;
        File sourceFile = screenshot.getScreenshotAs(OutputType.FILE);
        
        String fileName = testName + "_" + timestamp + ".png";
        String filePath = "reports/screenshots/" + fileName;
        
        FileUtils.copyFile(sourceFile, new File(filePath));
        return filePath;
    }
}
```

### 5. How do you manage configurations in your framework?

**Environment-Based Configuration:**
```java
public class ConfigLoader {
    private static Properties properties;
    
    static {
        String environment = EnvReader.get("ENVIRONMENT", "qa");
        String configFile = "config/" + environment + "-config.properties";
        properties.load(new FileInputStream(configFile));
    }
    
    public static String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
```

**Configuration Files:**
```
config/
├── qa-config.properties       # QA settings
├── uat-config.properties      # UAT settings  
└── prod-config.properties     # Production settings
```

**Sample Configuration:**
```properties
# qa-config.properties
app.base.url=https://qa.example.com
browser.name=chrome
browser.headless=false
test.username=qa_testuser
test.password=qa_testpass
```

### 6. How do you design a hybrid framework, and why?

**Hybrid Framework = Data-Driven + Keyword-Driven + Modular**

```java
// Data-Driven Component
@Test(dataProvider = "loginData")
public void testLogin(String username, String password, String expected) {
    loginPage.login(username, password);
    // Validate result
}

// Keyword-Driven Component  
public void performAction(String action, String element, String data) {
    switch(action) {
        case "click": browserUtils.click(getLocator(element)); break;
        case "type": browserUtils.sendKeys(getLocator(element), data); break;
    }
}

// Modular Component
public class LoginModule {
    public void login(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickLoginButton();
    }
}
```

### 7. How do you structure reusable functions for different pages/modules?

**Component-Based Approach:**
```java
// Reusable Header Component
public class HeaderFooterComponent {
    public void clickNavigationItem(String itemName) throws Exception {
        browserUtils.clickWithFallback(
            By.xpath("//nav//a[contains(text(), '" + itemName + "')]"),
            By.cssSelector(".nav-menu a[href*='" + itemName.toLowerCase() + "']")
        );
    }
    
    public void searchInHeader(String searchTerm) throws Exception {
        browserUtils.sendKeys(By.cssSelector(".search-bar input"), searchTerm);
        browserUtils.click(By.cssSelector(".search-bar button"));
    }
}
```

### 8. How do you manage dynamic elements or Ajax-heavy pages in Selenium?

**Smart Wait Strategies:**
```java
public class BrowserUtils {
    // Wait for element to be clickable
    public WebElement waitForElementToBeClickable(By locator, int timeoutSeconds) {
        WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        return customWait.until(ExpectedConditions.elementToBeClickable(locator));
    }
    
    // Wait for element to disappear (for loading spinners)
    public boolean waitForElementToDisappear(By locator, int timeoutSeconds) {
        try {
            WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            return customWait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
        } catch (Exception e) {
            return false;
        }
    }
    
    // Wait for element to be visible
    public boolean waitForElementToBeVisible(By locator, int timeoutSeconds) {
        try {
            WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            customWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

### 9. How do you ensure parallel execution in your framework?

**TestNG Parallel Configuration:**
```xml
<suite name="ParallelTestSuite" parallel="tests" thread-count="3">
    <test name="UI Tests">
        <classes>
            <class name="com.yourorg.tests.ui.SanityTest"/>
        </classes>
    </test>
    <test name="API Tests">
        <classes>
            <class name="com.yourorg.tests.api.UserServiceTest"/>
        </classes>
    </test>
</suite>
```

**Thread-Safe Base Test:**
```java
public class BaseTest {
    protected WebDriver driver;
    
    @BeforeMethod
    public void beforeMethod() {
        driver = RemoteWebDriverFactory.createDriver(); // Thread-safe
    }
    
    @AfterMethod
    public void afterMethod() {
        RemoteWebDriverFactory.quitDriver(); // Clean up per thread
    }
}
```

### 10. How do you design framework logs and reports?

**Simple Logging Setup:**
```java
public class TestListener implements ITestListener {
    private static ExtentReports extent;
    private static ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();

    @Override
    public void onTestStart(ITestResult result) {
        ExtentTest test = extent.createTest(result.getMethod().getMethodName());
        extentTest.set(test);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = extentTest.get();
        test.log(Status.FAIL, "Test failed");
        
        // Capture screenshot on failure
        String screenshotPath = ScreenshotUtils.captureScreenshot(driver, result.getMethod().getMethodName());
        test.addScreenCaptureFromPath(screenshotPath);
    }
}
```

### 11. How do you design your test suites in TestNG for flexibility and scalability?

**Flexible Test Suite Design:**
```xml
<suite name="FlexibleSuite" parallel="tests" thread-count="3">
    <test name="Sanity Tests">
        <groups>
            <run>
                <include name="sanity"/>
                <include name="critical"/>
            </run>
        </groups>
        <classes>
            <class name="com.yourorg.tests.ui.SanityTest"/>
        </classes>
    </test>
</suite>
```

**Test Grouping:**
```java
@Test(priority = 1, groups = {"sanity", "critical"})
public void testHomePageLoads() {
    // Test implementation
}

@Test(priority = 2, groups = {"regression", "ui"})
public void testLoginFunctionality() {
    // Test implementation
}
```

### 12. How do you handle test data in your automation framework?

**Multiple Data Sources:**
```java
// JSON Data Provider
public class JsonTestDataProvider {
    public Map<String, Object> getTestData(String testName) {
        String jsonContent = Files.readString(Paths.get(dataDirectory, testName + ".json"));
        return objectMapper.readValue(jsonContent, Map.class);
    }
}

// CSV Data Provider  
public class CsvDataProvider {
    public Object[][] getDataForTestNG() {
        List<Map<String, Object>> dataList = getBulkTestData("data");
        Object[][] testData = new Object[dataList.size()][];
        for (int i = 0; i < dataList.size(); i++) {
            testData[i] = new Object[]{dataList.get(i)};
        }
        return testData;
    }
}
```

**Data-Driven Test:**
```java
@Test(dataProvider = "loginDataProvider")
public void testLoginWithMultipleData(String username, String password, String expectedResult) {
    loginPage.login(username, password);
    // Validate based on expectedResult
}
```

### 13. How do you externalize locators in a scalable way?

**Locator Constants in Page Classes:**
```java
public class HomePage {
    // Locators as constants
    private static final By LOGO = By.id("logo");
    private static final By SEARCH_BOX = By.cssSelector(".search-box input");
    private static final By LOGIN_BUTTON_PRIMARY = By.cssSelector(".login-btn");
    private static final By LOGIN_BUTTON_FALLBACK = By.xpath("//button[contains(text(), 'Login')]");
    
    // Fallback locator approach
    public void clickLoginButton() throws Exception {
        browserUtils.clickWithFallback(
            LOGIN_BUTTON_PRIMARY,
            LOGIN_BUTTON_FALLBACK,
            By.id("login-button")
        );
    }
}
```

### 14. How do you maintain a clean separation of concerns in your framework?

**Clear Layer Separation:**
```java
// Base Layer - Foundation
public class BaseTest {
    // Common setup/teardown
}

// Page Layer - UI Interactions
public class LoginPage {
    // Only login page related actions
}

// Utils Layer - Reusable Functions
public class BrowserUtils {
    // Browser interaction utilities
}

// Test Layer - Test Logic
public class LoginTests extends BaseTest {
    // Only test logic, no page interactions
}
```

### 15. How do you integrate API and UI testing in a single framework?

**Combined API + UI Testing:**
```java
public class UserManagementUtils {
    // Create user via API
    public static TestUser createTestUser() throws Exception {
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", "testuser_" + System.currentTimeMillis());
        userData.put("email", "test@example.com");
        userData.put("password", "TestPass123!");
        
        Response response = APIUtils.post("/users", userData);
        String userId = response.jsonPath().getString("id");
        String authToken = APIUtils.authenticateUser(username, password);
        
        return new TestUser(userId, username, email, password, authToken);
    }
    
    // Setup UI session with API-created user
    public static void setupUserSession(WebDriver driver, TestUser user) throws Exception {
        driver.get(ConfigLoader.get("app.base.url"));
        Cookie authCookie = new Cookie("auth_token", user.getAuthToken());
        driver.manage().addCookie(authCookie);
        driver.navigate().refresh();
    }
}
```

### 16. What's your approach to building cross-browser test capabilities?

**Browser Configuration:**
```java
public class RemoteWebDriverFactory {
    public static WebDriver createDriver() {
        String browserName = EnvReader.get("BROWSER", "chrome");
        
        switch (browserName.toLowerCase()) {
            case "chrome":
                return new ChromeDriver(configureChromeOptions());
            case "firefox":
                return new FirefoxDriver(configureFirefoxOptions());
            case "edge":
                return new EdgeDriver(configureEdgeOptions());
            default:
                return new ChromeDriver(configureChromeOptions());
        }
    }
    
    private static ChromeOptions configureChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage");
        if (EnvReader.getBoolean("HEADLESS", false)) {
            options.addArguments("--headless=new");
        }
        return options;
    }
}
```

### 17. How do you handle flaky tests in your framework?

**Retry Mechanism:**
```java
public class RetryAnalyzer implements IRetryAnalyzer {
    private int retryCount = 0;
    private static final int MAX_RETRY_COUNT = 2;

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < MAX_RETRY_COUNT) {
            retryCount++;
            logger.warn("Retrying test '{}' - Attempt {}/{}", 
                    result.getMethod().getMethodName(), retryCount, MAX_RETRY_COUNT);
            return true;
        }
        return false;
    }
}

// Usage in test
@Test(retryAnalyzer = RetryAnalyzer.class)
public void testLogin() {
    // Test implementation
}
```

### 18. How do you build browser-agnostic locators for better test resilience?

**Fallback Locator Strategy:**
```java
public class BrowserUtils {
    // Multiple locator fallback
    public void clickWithFallback(By... locators) {
        for (By locator : locators) {
            try {
                click(locator);
                return; // Success, exit
            } catch (Exception e) {
                logger.debug("Locator {} failed, trying next", locator);
            }
        }
        throw new RuntimeException("All fallback locators failed");
    }
}

// Usage in page
public void clickLoginButton() throws Exception {
    browserUtils.clickWithFallback(
        By.cssSelector(".login-btn"),           // Primary
        By.xpath("//button[text()='Login']"),   // Fallback 1
        By.id("login-button"),                  // Fallback 2
        By.name("login")                        // Fallback 3
    );
}
```

### 19. How do you implement reusable custom wait strategies?

**Custom Wait Utilities:**
```java
public class BrowserUtils {
    private final WebDriverWait wait;
    
    public BrowserUtils(WebDriver driver) {
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }
    
    // Custom wait with timeout
    public WebElement waitForElement(By locator, int timeoutSeconds) {
        WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        return customWait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }
    
    // Wait for element to disappear
    public boolean waitForElementToDisappear(By locator, int timeoutSeconds) {
        try {
            WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            return customWait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
        } catch (Exception e) {
            return false;
        }
    }
}
```

### 20. How do you implement role-based test flows (admin, user, guest)?

**Role-Based User Creation:**
```java
public class UserManagementUtils {
    public static TestUser createAdminUser() throws Exception {
        Map<String, Object> adminPermissions = new HashMap<>();
        adminPermissions.put("admin", true);
        adminPermissions.put("manage_users", true);
        
        return createTestUser("admin", adminPermissions);
    }
    
    public static TestUser createUserWithRole(String role) throws Exception {
        Map<String, Object> rolePermissions = new HashMap<>();
        
        switch (role.toLowerCase()) {
            case "customer":
                rolePermissions.put("place_orders", true);
                break;
            case "vendor":
                rolePermissions.put("manage_products", true);
                break;
        }
        
        return createTestUser(role, rolePermissions);
    }
}
```

## Getting Started

### Quick Setup
```bash
# Clone the repository
git clone <repository-url>
cd advanced-test-automation-framework

# Run tests
mvn test -Psanity                    # Run sanity tests
mvn test -Pregression               # Run regression tests
mvn test -Psanity -Dbrowser=firefox # Run with Firefox
mvn test -Psanity -Dheadless=true   # Run headless
```

### Configuration
Set environment variables:
```bash
export ENVIRONMENT=qa
export BROWSER=chrome
export HEADLESS=false
export THREAD_COUNT=3
```

### Project Structure
```
├── config/                 # Environment configurations
├── src/test/java/          # Test source code
│   ├── base/              # Base test classes
│   ├── pages/             # Page objects
│   ├── tests/             # Test cases
│   ├── utils/             # Utilities
│   └── api/               # API testing
├── src/test/resources/     # Test resources
│   ├── testdata/          # Test data files
│   └── testng-*.xml       # TestNG suites
└── reports/               # Test reports
```

## Key Benefits

✅ **Simple to understand** - Clear layer separation  
✅ **Easy to maintain** - Fallback locators and clean code  
✅ **Parallel execution** - Thread-safe WebDriver management  
✅ **Cross-browser support** - Chrome, Firefox, Edge  
✅ **Data-driven testing** - Multiple data sources  
✅ **API + UI integration** - Combined testing approach  
✅ **Rich reporting** - Extent Reports with screenshots  
✅ **CI/CD ready** - Docker and Jenkins support  

## Framework Highlights

- **No WebElement variables** - Direct locator usage for reliability
- **Fallback locators** - Multiple strategies for resilient tests  
- **Thread-safe design** - Supports parallel execution
- **Environment-based config** - Easy environment switching
- **Comprehensive utilities** - Browser, screenshot, data utilities
- **Hybrid approach** - Data-driven + modular + keyword-driven
- **Clean architecture** - SOLID principles applied
- **Rich reporting** - Detailed HTML reports with screenshots

This framework provides a solid foundation for scalable test automation while keeping complexity manageable.