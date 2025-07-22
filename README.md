# Advanced Test Automation Framework

A comprehensive, enterprise-grade test automation framework built with Java, Selenium, TestNG, and RestAssured. This framework demonstrates modern automation practices, design patterns, and scalable architecture.

## Table of Contents
- [Framework Architecture](#framework-architecture)
- [Design Patterns](#design-patterns)
- [Page Object Model Implementation](#page-object-model-implementation)
- [WebDriver Management](#webdriver-management)
- [Configuration Management](#configuration-management)
- [Test Data Management](#test-data-management)
- [Parallel Execution](#parallel-execution)
- [Reporting and Logging](#reporting-and-logging)
- [API and UI Integration](#api-and-ui-integration)
- [Best Practices](#best-practices)

## Framework Architecture

### 1. What is your approach to designing a scalable automation framework from scratch?

Our framework follows a **layered architecture** with clear separation of concerns:

```
src/test/java/com/yourorg/
├── abstracts/          # Abstract base classes
├── api/               # API testing components
├── base/              # Base test classes
├── browser/           # Browser utilities
├── common/            # Common utilities
├── dataproviders/     # Test data providers
├── exceptions/        # Custom exceptions
├── interfaces/        # Framework interfaces
├── listeners/         # TestNG listeners
├── locators/          # Locator strategies
├── models/            # Data models
├── pages/             # Page objects
├── reporting/         # Reporting utilities
├── tests/             # Test classes
└── utils/             # Utility classes
```

**Key Principles:**
- **Single Responsibility**: Each class has one clear purpose
- **Open/Closed**: Open for extension, closed for modification
- **Dependency Inversion**: Depend on abstractions, not concretions
- **Interface Segregation**: Small, focused interfaces

## Design Patterns

### 2. What design patterns have you used in your automation framework and why?

#### Singleton Pattern - WebDriver Management
```java
public class WebDriverManager implements IWebDriverManager {
    private static WebDriverManager instance;
    
    public static WebDriverManager getInstance() {
        if (instance == null) {
            synchronized (WebDriverManager.class) {
                if (instance == null) {
                    instance = new WebDriverManager();
                }
            }
        }
        return instance;
    }
}
```

#### Factory Pattern - Data Provider Creation
```java
public class TestDataProviderFactory {
    public static ITestDataProvider createDataProvider(String filePath) {
        String extension = getFileExtension(filePath).toLowerCase();
        
        switch (extension) {
            case "json":
                return new JsonTestDataProvider(file.getParent());
            case "xlsx":
                return new ExcelDataProvider(filePath);
            case "csv":
                return new CsvDataProvider(filePath);
            default:
                throw new IllegalArgumentException("Unsupported file type: " + extension);
        }
    }
}
```

#### Strategy Pattern - Locator Strategies
```java
public interface LocatorStrategy {
    List<By> getLocators();
    String getDescription();
    int getPriority();
}

public class PrimaryLocatorStrategy implements LocatorStrategy {
    public List<By> getLocators() {
        return List.of(By.id(id), By.cssSelector(cssSelector));
    }
}
```

#### Builder Pattern - API Request Building
```java
public class RequestBuilder {
    public RequestBuilder baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }
    
    public RequestBuilder endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }
    
    public RequestSpecification build() {
        // Build and return request specification
    }
}
```

## Page Object Model Implementation

### 3. How do you implement Page Object Model (POM) in Selenium projects?

#### Abstract Base Page
```java
public abstract class AbstractBasePage implements IPageObject {
    protected final WebDriver driver;
    protected final BrowserUtils browserUtils;
    
    protected AbstractBasePage(WebDriver driver) throws Exception {
        this.driver = driver;
        this.browserUtils = new BrowserUtils(driver);
    }
    
    // Template method pattern
    public final boolean validatePage() {
        try {
            return validatePageElements() && validatePageUrl() && validatePageTitle();
        } catch (Exception e) {
            return false;
        }
    }
    
    protected abstract boolean validatePageElements();
    protected abstract boolean validatePageUrl();
    protected abstract boolean validatePageTitle();
}
```

#### Concrete Page Implementation
```java
public class HomePage {
    private final WebDriver driver;
    private final BrowserUtils browserUtils;

    // Locators as constants
    private static final By LOGO = By.id("logo");
    private static final By SEARCH_BOX = By.cssSelector(".search-box input");
    private static final By LOGIN_BUTTON_PRIMARY = By.cssSelector(".login-btn");
    
    public HomePage(WebDriver driver) throws Exception {
        this.driver = driver;
        this.browserUtils = new BrowserUtils(driver);
    }

    // Actions using direct locator approach
    public void searchFor(String searchTerm) throws Exception {
        browserUtils.sendKeys(SEARCH_BOX, searchTerm);
        browserUtils.click(SEARCH_BUTTON);
    }
    
    public void clickLoginButton() throws Exception {
        browserUtils.clickWithFallback(
            LOGIN_BUTTON_PRIMARY,
            By.xpath("//button[contains(text(), 'Login')]"),
            By.id("login-button")
        );
    }
}
```

### 4. How do you structure a page object in a scalable way?

#### Component-Based Approach
```java
public class HeaderFooterComponent {
    private final BrowserUtils browserUtils;
    
    // Header Locators
    private static final By HEADER_LOGO = By.cssSelector(".header .logo");
    private static final By USER_MENU = By.cssSelector(".header .user-menu");
    
    public void clickNavigationItem(String itemName) throws Exception {
        browserUtils.clickWithFallback(
            By.xpath("//nav//a[contains(text(), '" + itemName + "')]"),
            By.cssSelector(".nav-menu a[href*='" + itemName.toLowerCase() + "']")
        );
    }
}
```

### 5. How do you ensure high maintainability of your page classes?

#### Locator Fallback Strategy
```java
public class LocatorFallback {
    public WebElement findElementWithFallback(List<By> locators, int timeoutSeconds) {
        for (int i = 0; i < locators.size(); i++) {
            try {
                return customWait.until(ExpectedConditions.presenceOfElementLocated(locators.get(i)));
            } catch (Exception e) {
                if (i == locators.size() - 1) {
                    throw new NoSuchElementException("Element not found with any locators");
                }
            }
        }
    }
}
```

## WebDriver Management

### 6. How do you manage WebDriver instances in your framework?

#### Thread-Safe WebDriver Factory
```java
public class RemoteWebDriverFactory {
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    public static WebDriver createDriver() {
        String browserName = EnvReader.get("BROWSER", ConfigLoader.get("browser.name", "chrome"));
        String remoteUrl = EnvReader.get("REMOTE_URL", ConfigLoader.get("remote.url"));
        boolean headless = EnvReader.getBoolean("HEADLESS", false);
        
        WebDriver driver = Optional.ofNullable(remoteUrl)
                .filter(url -> !url.isEmpty())
                .map(url -> createRemoteDriver(browserName, url, headless))
                .orElseGet(() -> createLocalDriver(browserName, headless));
        
        configureDriver(driver);
        driverThreadLocal.set(driver);
        return driver;
    }
    
    public static WebDriver getDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver == null) {
            driver = createDriver();
        }
        return driver;
    }
}
```

### 7. How do you control browser and resolution configurations in parallel executions?

#### Browser Configuration
```java
private ChromeOptions configureChromeOptions(boolean headless) {
    ChromeOptions options = new ChromeOptions();
    
    List<String> chromeArgs = List.of(
        "--no-sandbox",
        "--disable-dev-shm-usage",
        "--disable-gpu",
        "--remote-allow-origins=*"
    );
    
    options.addArguments(chromeArgs);
    
    if (headless) {
        options.addArguments("--headless=new");
    }
    
    String windowSize = ConfigLoader.get("browser.window.size", "1920,1080");
    options.addArguments("--window-size=" + windowSize);
    
    return options;
}
```

## Configuration Management

### 8. How do you manage configurations (e.g., browser, environment, credentials) in your framework?

#### Environment-Based Configuration
```java
public class ConfigLoader {
    private static Properties properties;
    
    static {
        loadConfig();
    }
    
    private static void loadConfig() {
        properties = new Properties();
        String environment = EnvReader.get("ENVIRONMENT", "qa");
        String configFile = CONFIG_PATH + environment + "-config.properties";
        
        try {
            properties.load(new FileInputStream(configFile));
        } catch (IOException e) {
            // Fallback to default config
            properties.load(new FileInputStream(CONFIG_PATH + "config.properties"));
        }
    }
    
    public static String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
```

#### Environment Reader with Multiple Sources
```java
public class EnvReader {
    public static String get(String key) {
        String value = System.getProperty(key);
        if (value == null) {
            value = System.getenv(key);
        }
        if (value == null && dotenv != null) {
            value = dotenv.get(key);
        }
        return value;
    }
}
```

### 9. How do you manage environment-specific behavior?

#### Configuration Files Structure
```
config/
├── config.properties          # Default configuration
├── qa-config.properties       # QA environment
├── uat-config.properties      # UAT environment
└── prod-config.properties     # Production environment
```

## Test Data Management

### 10. How do you handle test data in your automation framework?

#### Abstract Data Provider
```java
public abstract class AbstractTestDataProvider implements ITestDataProvider {
    protected final Map<String, Object> cache = new ConcurrentHashMap<>();
    
    @Override
    public boolean isDataAvailable(String testName) {
        return cache.containsKey(testName) || checkDataSource(testName);
    }
    
    protected void cacheData(String key, Object data) {
        cache.put(key, data);
    }
}
```

#### Multiple Data Source Support
```java
public class JsonTestDataProvider extends AbstractTestDataProvider {
    @Override
    public Map<String, Object> getTestData(String testName) {
        Object cachedData = getCachedData(testName);
        if (cachedData instanceof Map) {
            return (Map<String, Object>) cachedData;
        }
        
        String jsonContent = Files.readString(Paths.get(dataDirectory, testName + ".json"));
        Map<String, Object> data = objectMapper.readValue(jsonContent, 
                new TypeReference<Map<String, Object>>() {});
        
        cacheData(testName, data);
        return data;
    }
}
```

### 11. How do you implement a data-driven test structure in TestNG?

#### TestNG Data Providers
```java
@DataProvider(name = "loginDataProvider")
public static Object[][] loginDataProvider() {
    String filePath = "src/test/resources/testdata/login_data.csv";
    CsvDataProvider provider = new CsvDataProvider(filePath);
    return provider.getColumnsForTestNG("username", "password", "expectedResult");
}

@Test(dataProvider = "loginDataProvider")
public void testLoginWithMultipleData(String username, String password, String expectedResult) {
    // Test implementation
}
```

### 12. How do you automate test data generation for boundary/edge cases?

#### Faker Data Generation
```java
public class FakerDataGenerator {
    private static final Faker faker = new Faker();
    
    public static User generateUser() {
        return new User(faker);
    }
    
    public static Map<String, String> generateSecurityTestData() {
        Map<String, String> testData = new HashMap<>();
        testData.put("sqlInjection1", "' OR '1'='1");
        testData.put("xss1", "<script>alert('XSS')</script>");
        return testData;
    }
    
    public static Map<String, String> generateBoundaryTestData() {
        Map<String, String> testData = new HashMap<>();
        testData.put("longString", "a".repeat(1000));
        testData.put("specialChars", "!@#$%^&*()_+-=");
        return testData;
    }
}
```

## Parallel Execution

### 13. How do you ensure parallel execution in your framework?

#### TestNG Suite Configuration
```xml
<suite name="ParallelTestSuite" parallel="tests" thread-count="3">
    <test name="UI Tests">
        <parameter name="browser" value="chrome"/>
        <classes>
            <class name="com.yourorg.tests.ui.SanityTest"/>
        </classes>
    </test>
</suite>
```

#### Thread-Safe Base Test
```java
@Listeners({TestListener.class})
public class BaseTest {
    protected WebDriver driver;
    protected String testSessionId;

    @BeforeMethod
    public void beforeMethod() {
        try {
            driver = RemoteWebDriverFactory.createDriver();
            StateRetentionManager.setSessionState(testSessionId, "driver", driver);
        } catch (Exception e) {
            throw new RuntimeException("Driver creation failed", e);
        }
    }
    
    @AfterMethod
    public void afterMethod() {
        try {
            if (driver != null) {
                RemoteWebDriverFactory.quitDriver();
            }
        } catch (Exception e) {
            logger.error("Error in afterMethod: {}", e.getMessage());
        }
    }
}
```

## Reporting and Logging

### 14. How do you design framework logs and reports?

#### Extent Report Manager
```java
public class ExtentReportManager implements IReportManager {
    private ExtentReports extent;
    private final ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();
    
    @Override
    public void initializeReport() {
        ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
        configureSparkReporter(sparkReporter);
        
        extent = new ExtentReports();
        extent.attachReporter(sparkReporter);
        addSystemInformation();
    }
    
    @Override
    public void onTestFailure(ITestResult result) {
        logFail("Test failed", result.getThrowable());
        
        String screenshotPath = captureScreenshotOnFailure(result);
        if (screenshotPath != null) {
            getCurrentTest().addScreenCaptureFromPath(screenshotPath);
        }
    }
}
```

#### Structured Logging
```xml
<!-- log4j2.xml -->
<Configuration status="WARN">
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
        </Console>
        <RollingFile name="RollingFileAppender" fileName="logs/automation-rolling.log">
            <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
            <Policies>
                <SizeBasedTriggeringPolicy size="10MB"/>
            </Policies>
        </RollingFile>
    </Appenders>
    
    <Loggers>
        <Logger name="com.yourorg.tests" level="INFO" additivity="false">
            <AppenderRef ref="Console"/>
            <AppenderRef ref="RollingFileAppender"/>
        </Logger>
    </Loggers>
</Configuration>
```

## API and UI Integration

### 15. How do you integrate API and UI testing in a single framework?

#### User Management with API Setup
```java
public class UserManagementUtils {
    public static TestUser createTestUser() throws Exception {
        // Generate unique user data
        String username = "testuser_" + System.currentTimeMillis();
        String email = "testuser_" + System.currentTimeMillis() + "@example.com";
        
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("email", email);
        userData.put("password", "TestPass123!");
        
        // Create user via API
        Response response = APIUtils.post("/users", userData);
        APIUtils.validateStatusCode(response, 201);
        
        String userId = response.jsonPath().getString("id");
        String authToken = APIUtils.authenticateUser(username, "TestPass123!");
        
        return new TestUser(userId, username, email, "TestPass123!", authToken);
    }
    
    public static void setupUserSession(WebDriver driver, TestUser user) throws Exception {
        driver.get(ConfigLoader.get("app.base.url"));
        
        Cookie authCookie = new Cookie("auth_token", user.getAuthToken());
        driver.manage().addCookie(authCookie);
        driver.navigate().refresh();
    }
}
```

#### API Chain Execution
```java
public class APIChainExecutor {
    public ChainResult executeChain(ChainDefinition chainDefinition) throws Exception {
        ChainResult result = new ChainResult(chainDefinition.getName());
        
        for (ChainStep step : chainDefinition.getSteps()) {
            Response response = executeStep(step);
            result.addStepResult(step.getName(), response);
            
            // Extract data for next steps
            if (step.getDataExtractor() != null) {
                Map<String, Object> extractedData = step.getDataExtractor().apply(response);
                chainContext.putAll(extractedData);
            }
        }
        
        return result;
    }
}
```

## Utilities and Common Functions

### 16. What are common utilities you include in your automation framework?

#### Browser Utils with Fallback Support
```java
public class BrowserUtils {
    public void clickWithFallback(By... locators) {
        for (By locator : locators) {
            try {
                click(locator);
                return;
            } catch (Exception e) {
                logger.debug("Locator {} failed, trying next fallback", locator.toString());
            }
        }
        throw new RuntimeException("All fallback locators failed");
    }
    
    public void sendKeysWithFallback(String text, By... locators) {
        for (By locator : locators) {
            try {
                sendKeys(locator, text);
                return;
            } catch (Exception e) {
                logger.debug("Locator {} failed for sendKeys, trying next fallback", locator.toString());
            }
        }
        throw new RuntimeException("All fallback locators failed for sendKeys");
    }
}
```

#### Advanced Collection Utils
```java
public class AdvancedCollectionUtils {
    public static <T, R> List<R> filterAndTransform(Collection<T> collection, 
                                                   Predicate<T> filter, 
                                                   Function<T, R> transformer) {
        return collection.stream()
                .filter(Objects::nonNull)
                .filter(filter)
                .map(transformer)
                .collect(Collectors.toList());
    }
    
    public static <T> Set<T> findDuplicates(Collection<T> collection) {
        Set<T> seen = new HashSet<>();
        return collection.stream()
                .filter(item -> !seen.add(item))
                .collect(Collectors.toSet());
    }
}
```

## Dynamic Elements and Ajax Handling

### 17. How do you manage dynamic elements or Ajax-heavy pages in Selenium?

#### Custom Wait Strategies
```java
public class BrowserUtils {
    public WebElement waitForElementToBeClickable(By locator, int timeoutSeconds) {
        WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        return customWait.until(ExpectedConditions.elementToBeClickable(locator));
    }
    
    public boolean waitForElementToDisappear(By locator, int timeoutSeconds) {
        try {
            WebDriverWait customWait = createCustomWait(timeoutSeconds);
            return customWait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
        } catch (Exception e) {
            return false;
        }
    }
}
```

## Test Suite Design

### 18. How do you design your test suites in TestNG for flexibility and scalability?

#### Flexible Suite Configuration
```xml
<suite name="FlexibleTestSuite" parallel="tests" thread-count="3">
    <parameter name="environment" value="qa"/>
    <parameter name="browser" value="chrome"/>
    
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
    
    <test name="Regression Tests">
        <groups>
            <run>
                <include name="regression"/>
            </run>
        </groups>
        <classes>
            <class name="com.yourorg.tests.ui.RegressionTest"/>
        </classes>
    </test>
</suite>
```

#### Test Prioritization and Grouping
```java
@Test(priority = 1, groups = {"sanity", "critical"}, retryAnalyzer = RetryAnalyzer.class)
@Description("Verify that the home page loads successfully")
@Severity(SeverityLevel.BLOCKER)
public void testHomePageLoads() throws Exception {
    // Test implementation
}
```

## Exception Handling and Retry Logic

### 19. How do you implement retry logic for recoverable test failures?

#### Retry Analyzer
```java
public class RetryAnalyzer implements IRetryAnalyzer {
    private int retryCount = 0;
    private static final int MAX_RETRY_COUNT = ConfigLoader.getInt("test.retry.count", 2);

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
```

#### Custom Exception Hierarchy
```java
public class FrameworkException extends Exception {
    private final String errorCode;
    private final String component;
    
    public FrameworkException(String message, String errorCode, String component) {
        super(message);
        this.errorCode = errorCode;
        this.component = component;
    }
}

public class PageObjectException extends FrameworkException {
    public PageObjectException(String message) {
        super(message, "PAGE_OBJECT_ERROR", "PAGE_OBJECT");
    }
}
```

## State Management

### 20. How do you handle test data setup and teardown at runtime?

#### State Retention Manager
```java
public class StateRetentionManager {
    private static final Map<String, Map<String, Object>> sessionState = new ConcurrentHashMap<>();
    private static final Map<String, Object> globalState = new ConcurrentHashMap<>();

    public static void setSessionState(String sessionId, String key, Object value) {
        sessionState.computeIfAbsent(sessionId, k -> new HashMap<>()).put(key, value);
    }
    
    public static Object getSessionState(String sessionId, String key) {
        Map<String, Object> session = sessionState.get(sessionId);
        return session != null ? session.get(key) : null;
    }
}
```

## Security Testing

### 21. How do you deal with security or permission testing in UI frameworks?

#### Security Test Implementation
```java
public class SecurityTests extends BaseTest {
    @Test(groups = {"security", "sql-injection"})
    public void testSQLInjectionVulnerabilities() throws Exception {
        String[] sqlInjectionPayloads = {
            "' OR '1'='1",
            "'; DROP TABLE users; --",
            "1' UNION SELECT * FROM users --"
        };
        
        for (String payload : sqlInjectionPayloads) {
            Response response = APIUtils.testSQLInjection("/auth/login", payload);
            Assert.assertNotEquals(response.getStatusCode(), 200, 
                    "SQL injection payload should not succeed: " + payload);
        }
    }
}
```

## CI/CD Integration

### 22. How do you ensure your framework is CI/CD compatible?

#### Docker Configuration
```dockerfile
FROM eclipse-temurin:11-jre-alpine

RUN apk add --no-cache \
    curl \
    bash \
    chromium \
    chromium-chromedriver \
    firefox

ENV CHROME_BIN=/usr/bin/chromium-browser
ENV CHROMEDRIVER_PATH=/usr/bin/chromedriver

WORKDIR /app
COPY . .

ENTRYPOINT ["/app/entrypoint.sh"]
```

#### Jenkins Pipeline
```groovy
pipeline {
    agent any
    
    parameters {
        choice(name: 'ENVIRONMENT', choices: ['qa', 'uat', 'prod'])
        choice(name: 'BROWSER', choices: ['chrome', 'firefox', 'edge'])
        choice(name: 'TEST_SUITE', choices: ['sanity', 'regression', 'all'])
    }
    
    stages {
        stage('Run Tests') {
            steps {
                script {
                    if (params.TEST_SUITE == 'sanity') {
                        sh "mvn test -Psanity -Dthread.count=${params.THREAD_COUNT}"
                    } else if (params.TEST_SUITE == 'regression') {
                        sh "mvn test -Pregression -Dthread.count=${params.THREAD_COUNT}"
                    }
                }
            }
        }
        
        stage('Generate Reports') {
            steps {
                sh 'mvn allure:report'
                publishHTML([
                    reportDir: 'reports/extent-report',
                    reportFiles: '*.html',
                    reportName: 'Extent Report'
                ])
            }
        }
    }
}
```

## Advanced Features

### 23. How do you implement business-readable test scenarios?

#### Allure Annotations
```java
@Epic("E-Commerce Tests")
@Feature("Shopping Cart")
@Test(description = "End-to-end shopping cart flow with pre-configured user")
@Severity(SeverityLevel.CRITICAL)
public void testShoppingCartFlow() throws Exception {
    // Create user with shopping cart data
    testUser = UserManagementUtils.createTestUser();
    UserManagementUtils.prepareUserWithData(testUser, "shopping_cart");
    
    // Test implementation with clear steps
    Allure.step("Navigate to application", () -> {
        navigateToBaseUrl();
    });
    
    Allure.step("Verify cart has items", () -> {
        headerFooter.clickCartIcon();
        Assert.assertTrue(currentUrl.contains("cart"));
    });
}
```

### 24. How do you handle large datasets and complex API responses?

#### Response Manager for Large Datasets
```java
public class ResponseManager {
    public Map<String, Object> findRecordById(Response response, String idField, String targetId) {
        String jsonResponse = response.getBody().asString();
        List<Map<String, Object>> records = JsonPath.read(jsonResponse, 
                "$[?(@." + idField + " == '" + targetId + "')]");
        
        return records.isEmpty() ? new HashMap<>() : records.get(0);
    }
    
    public List<Map<String, Object>> filterRecords(Response response, 
                                                   Map<String, Predicate<Object>> filters) {
        List<Map<String, Object>> allRecords = response.jsonPath().getList("$");
        
        return allRecords.stream()
                .filter(record -> filters.entrySet().stream()
                        .allMatch(entry -> entry.getValue().test(record.get(entry.getKey()))))
                .collect(Collectors.toList());
    }
}
```

## Getting Started

### Prerequisites
- Java 11 or higher
- Maven 3.6+
- Chrome/Firefox browsers

### Installation
```bash
git clone <repository-url>
cd advanced-test-automation-framework
mvn clean compile
```

### Running Tests
```bash
# Run sanity tests
mvn test -Psanity

# Run regression tests
mvn test -Pregression

# Run with specific browser
mvn test -Psanity -Dbrowser=firefox

# Run with specific environment
mvn test -Psanity -Denvironment=uat

# Run in headless mode
mvn test -Psanity -Dheadless=true
```

### Configuration

#### Environment Variables
```bash
export ENVIRONMENT=qa
export BROWSER=chrome
export HEADLESS=true
export THREAD_COUNT=3
```

#### Configuration Files
- `config/qa-config.properties` - QA environment settings
- `config/uat-config.properties` - UAT environment settings
- `config/prod-config.properties` - Production environment settings

## Framework Benefits

### Scalability
- **Modular Architecture**: Easy to add new components
- **Parallel Execution**: Supports multiple threads
- **Cross-Browser**: Chrome, Firefox, Edge, Safari
- **Environment Agnostic**: QA, UAT, Production

### Maintainability
- **Locator Fallback**: Multiple locator strategies
- **Page Components**: Reusable UI components
- **Clean Code**: SOLID principles applied
- **Exception Handling**: Comprehensive error management

### Reliability
- **Retry Mechanism**: Automatic retry for flaky tests
- **Wait Strategies**: Smart waiting for elements
- **State Management**: Session and global state handling
- **Screenshot Capture**: Automatic failure screenshots

### Reporting
- **Allure Reports**: Rich, interactive reports
- **Extent Reports**: Detailed HTML reports
- **Logging**: Structured logging with Log4j2
- **CI Integration**: Jenkins, Docker support

## Best Practices Implemented

1. **No WebElement Variables**: Direct locator usage for better reliability
2. **Fallback Locators**: Multiple locator strategies for resilience
3. **Thread Safety**: ThreadLocal WebDriver management
4. **Configuration Management**: Environment-specific configurations
5. **Data Separation**: External test data management
6. **API Integration**: Combined API and UI testing
7. **Security Testing**: Built-in security test capabilities
8. **Performance Testing**: Load and performance test support

## Contributing

1. Follow the established package structure
2. Implement interfaces for new components
3. Add comprehensive logging
4. Include unit tests for utilities
5. Update documentation

## Support

For questions or issues, please refer to the framework documentation or contact the automation team.