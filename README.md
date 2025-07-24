# Simple Test Automation Framework

A clean, simple test automation framework built with Java, Selenium, TestNG, and RestAssured.

## 🏗️ Framework Architecture

The framework is organized in simple layers:

```
src/test/java/com/yourorg/
├── base/              # Foundation - Base test classes and WebDriver factory
├── pages/             # UI Pages - Page objects for web pages
├── utils/             # Utilities - Helper functions and common operations
├── tests/             # Tests - Actual test cases (UI and API)
├── listeners/         # Reporting - Test listeners and reports
└── common/            # Common - Shared components like retry logic
```

### What Each Layer Does:

- **Base** - Sets up and tears down tests, manages WebDriver
- **Pages** - Represents web pages with actions you can perform
- **Utils** - Common functions like taking screenshots, API calls, config loading
- **Tests** - Your actual test cases
- **Listeners** - Handles reporting and logging
- **Common** - Shared utilities like retry logic

## 🚀 Quick Start

### 1. Simple Setup
```bash
# Clone and run
git clone <repository-url>
cd advanced-test-automation-framework

# Run sanity tests
./run-tests.sh sanity

# Run with different browser
./run-tests.sh sanity --browser firefox

# Run headless
./run-tests.sh sanity --headless

# Run regression tests
./run-tests.sh regression
```

### 2. Windows Users
```cmd
run-tests.bat sanity
run-tests.bat regression --browser edge
run-tests.bat api
```

## 📋 Key Framework Questions & Answers

### 1. How do you design a scalable automation framework?

**Simple Layered Approach:**
```java
// Base Test - Foundation for all tests
public class BaseTest {
    protected WebDriver driver;
    
    @BeforeMethod
    public void setup() {
        driver = WebDriverFactory.createDriver();
    }
    
    @AfterMethod  
    public void teardown() {
        WebDriverFactory.quitDriver();
    }
}
```

### 2. How do you implement Page Object Model?

**Simple Page Object:**
```java
public class HomePage {
    private final WebDriver driver;
    private final BrowserUtils browserUtils;

    // Locators as constants
    private static final By LOGO = By.id("logo");
    private static final By SEARCH_BOX = By.cssSelector(".search-box input");
    private static final By LOGIN_BUTTON = By.cssSelector(".login-btn");
    
    public HomePage(WebDriver driver) {
        this.driver = driver;
        this.browserUtils = new BrowserUtils(driver);
    }

    // Simple actions
    public void clickLogo() {
        browserUtils.click(LOGO);
    }
    
    public void searchFor(String searchTerm) {
        browserUtils.sendKeys(SEARCH_BOX, searchTerm);
        browserUtils.click(By.cssSelector(".search-box button"));
    }
}
```

### 3. How do you manage WebDriver instances?

**Thread-Safe WebDriver Management:**
```java
public class WebDriverFactory {
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    public static WebDriver createDriver() {
        String browserName = System.getProperty("browser", "chrome");
        boolean headless = Boolean.parseBoolean(System.getProperty("headless", "false"));
        
        WebDriver driver;
        switch (browserName.toLowerCase()) {
            case "chrome":
                WebDriverManager.chromedriver().setup();
                driver = new ChromeDriver(getChromeOptions(headless));
                break;
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                driver = new FirefoxDriver(getFirefoxOptions(headless));
                break;
            default:
                driver = new ChromeDriver(getChromeOptions(headless));
        }
        
        driverThreadLocal.set(driver);
        return driver;
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

### 4. What utilities do you include?

**Browser Utilities:**
```java
public class BrowserUtils {
    public void click(By locator) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
        element.click();
    }
    
    public void sendKeys(By locator, String text) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        element.clear();
        element.sendKeys(text);
    }
    
    public void clickWithFallback(By... locators) {
        for (By locator : locators) {
            try {
                click(locator);
                return;
            } catch (Exception e) {
                // Try next locator
            }
        }
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

### 5. How do you manage configurations?

**Environment-Based Configuration:**
```java
public class ConfigLoader {
    private static Properties properties;
    
    static {
        String environment = System.getProperty("environment", "qa");
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

### 6. How do you handle test data?

**Simple Data Provider:**
```java
public class TestDataProvider {
    public static Map<String, Object> getTestData(String fileName) {
        String filePath = "src/test/resources/testdata/" + fileName + ".json";
        String jsonContent = Files.readString(Paths.get(filePath));
        return objectMapper.readValue(jsonContent, Map.class);
    }
    
    public static Object[][] getCSVData(String fileName) {
        List<String> lines = Files.readAllLines(Paths.get("src/test/resources/testdata/" + fileName + ".csv"));
        // Convert to Object[][] for TestNG
    }
}
```

### 7. How do you ensure parallel execution?

**TestNG Parallel Configuration:**
```xml
<suite name="ParallelSuite" parallel="tests" thread-count="3">
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

### 8. How do you handle dynamic elements?

**Smart Wait Strategies:**
```java
public class BrowserUtils {
    public WebElement waitForElement(By locator, int timeoutSeconds) {
        WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        return customWait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }
    
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

### 9. How do you design reports?

**Simple Reporting:**
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
        
        // Capture screenshot
        String screenshotPath = ScreenshotUtils.captureScreenshot(driver, result.getMethod().getMethodName());
        test.addScreenCaptureFromPath(screenshotPath);
    }
}
```

### 10. How do you handle flaky tests?

**Retry Mechanism:**
```java
public class RetryAnalyzer implements IRetryAnalyzer {
    private int retryCount = 0;
    private static final int MAX_RETRY_COUNT = 2;

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < MAX_RETRY_COUNT) {
            retryCount++;
            logger.warn("Retrying test - Attempt {}/{}", retryCount, MAX_RETRY_COUNT);
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

### 11. How do you integrate API and UI testing?

**Combined Testing:**
```java
public class APIUtils {
    public static String authenticateUser(String username, String password) throws Exception {
        Map<String, String> credentials = Map.of("username", username, "password", password);
        Response response = post("/auth/login", credentials);
        
        if (response.getStatusCode() == 200) {
            String token = response.jsonPath().getString("token");
            setAuthToken(token);
            return token;
        }
        throw new Exception("Authentication failed");
    }
    
    public static Response get(String endpoint) {
        return RestAssured.given()
                .header("Authorization", "Bearer " + authToken)
                .when().get(endpoint);
    }
}
```

### 12. How do you build resilient locators?

**Fallback Locator Strategy:**
```java
public void clickLoginButton() {
    browserUtils.clickWithFallback(
        By.cssSelector(".login-btn"),           // Primary
        By.xpath("//button[text()='Login']"),   // Fallback 1
        By.id("login-button"),                  // Fallback 2
        By.name("login")                        // Fallback 3
    );
}
```

## 🎯 Running Tests

### Command Line Options
```bash
# Basic runs
./run-tests.sh sanity                    # Run sanity tests
./run-tests.sh regression               # Run regression tests

# With options
./run-tests.sh sanity --browser firefox # Use Firefox
./run-tests.sh sanity --headless        # Run headless
./run-tests.sh sanity --env uat         # Use UAT environment

# Specific test types
./run-tests.sh api                      # API tests only
./run-tests.sh ui                       # UI tests only
```

### Maven Commands
```bash
mvn test -Psanity                       # Sanity tests
mvn test -Pregression                   # Regression tests
mvn test -Psanity -Dbrowser=firefox     # With Firefox
mvn test -Psanity -Dheadless=true       # Headless mode
mvn test -Psanity -Denvironment=uat     # UAT environment
```

## 📁 Project Structure
```
├── config/                 # Environment configurations
├── src/test/java/          # Test source code
│   ├── base/              # Base classes and WebDriver factory
│   ├── pages/             # Page objects
│   ├── tests/             # Test cases
│   ├── utils/             # Utilities (Browser, API, Screenshot, Config)
│   ├── listeners/         # Test listeners for reporting
│   └── common/            # Common utilities like retry logic
├── src/test/resources/     # Test resources
│   ├── testdata/          # Test data files (JSON, CSV)
│   └── testng-*.xml       # TestNG suite files
├── reports/               # Test reports and screenshots
├── run-tests.sh           # Simple test runner (Linux/Mac)
├── run-tests.bat          # Simple test runner (Windows)
└── Dockerfile             # Docker configuration
```

## 🔧 Configuration

### Environment Files
```properties
# qa-config.properties
app.base.url=https://qa.example.com
browser.name=chrome
browser.headless=false
browser.implicit.wait=10
test.username=qa_testuser
test.password=qa_testpass
api.base.url=https://qa-api.example.com
```

### Test Data
```json
// login_data.json
{
  "validUser": {
    "username": "testuser",
    "password": "testpass",
    "expectedResult": "success"
  },
  "invalidUser": {
    "username": "invalid",
    "password": "invalid",
    "expectedResult": "failure"
  }
}
```

## ✅ Key Benefits

- **Simple to understand** - Clear layer separation
- **Easy to run** - Simple scripts and commands
- **Cross-browser support** - Chrome, Firefox, Edge
- **Parallel execution** - Thread-safe design
- **Rich reporting** - HTML reports with screenshots
- **Retry mechanism** - Handles flaky tests
- **API + UI testing** - Combined approach
- **Environment support** - QA, UAT, Production configs
- **Docker ready** - Containerized execution
- **CI/CD friendly** - Jenkins pipeline included

## 🐳 Docker Support

```bash
# Build and run with Docker
docker build -t test-framework .
docker run -e ENVIRONMENT=qa -e BROWSER=chrome test-framework sanity
```

## 📊 Reports

After test execution, find reports in:
- **HTML Report**: `reports/extent-report/ExtentReport_[timestamp].html`
- **Screenshots**: `reports/screenshots/`
- **Logs**: `logs/automation.log`

## 🔄 CI/CD Integration

The framework includes Jenkins pipeline configuration for easy CI/CD integration:

```groovy
// Jenkinsfile included with parameterized builds
pipeline {
    parameters {
        choice(name: 'ENVIRONMENT', choices: ['qa', 'uat', 'prod'])
        choice(name: 'BROWSER', choices: ['chrome', 'firefox', 'edge'])
        choice(name: 'TEST_SUITE', choices: ['sanity', 'regression'])
        booleanParam(name: 'HEADLESS', defaultValue: true)
    }
    // ... pipeline stages
}
```

This framework provides a solid foundation for test automation while keeping complexity manageable and execution simple.