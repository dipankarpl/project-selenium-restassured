# Advanced Test Automation Framework with Enhanced OOP and Layered Architecture

A comprehensive, enterprise-grade test automation framework built with Java, TestNG, Selenium WebDriver, and RestAssured. This framework demonstrates advanced OOP concepts, functional programming with Java 8+ features, Chrome DevTools Protocol (CDP) integration, and a sophisticated layered architecture.

## 🎯 **Improved Page Object Model with Direct Locator Usage**

### **Enhanced BrowserUtils with Locator-Based Methods**
The framework has been significantly improved to eliminate the need for WebElement variables in page classes. Instead, locators are passed directly to BrowserUtils methods, providing several key benefits:

#### **Key Improvements**:
1. **Reduced Code Duplication**: No need to define WebElement variables in page classes
2. **Better Maintainability**: Locators are defined once as constants and used directly
3. **Improved Encapsulation**: BrowserUtils handles all element finding and interaction logic
4. **Cleaner Page Classes**: Focus on business logic rather than element management
5. **Built-in Fallback Support**: Multiple locator strategies with automatic fallback
6. **Enhanced Error Handling**: Comprehensive error handling at the utility level

#### **Before vs After Comparison**:

**Old Approach (Before)**:
```java
// Page class with WebElement variables
public class LoginPage {
    private final By usernameLocator = By.id("username");
    private final By passwordLocator = By.id("password");
    
    public void enterUsername(String username) {
        WebElement usernameElement = driver.findElement(usernameLocator);
        browserUtils.sendKeys(usernameElement, username);
    }
}
```

**New Approach (After)**:
```java
// Page class with direct locator usage
public class LoginPage {
    private static final By USERNAME_FIELD = By.id("username");
    private static final By PASSWORD_FIELD = By.id("password");
    
    public void enterUsername(String username) {
        browserUtils.sendKeys(USERNAME_FIELD, username);
    }
}
```

### **Enhanced BrowserUtils Methods**

#### **Direct Locator Support**:
```java
// Click with locator
browserUtils.click(By.id("loginButton"));

// Send keys with locator
browserUtils.sendKeys(By.id("username"), "testuser");

// Get text with locator
String text = browserUtils.getText(By.cssSelector(".message"));

// Check element state with locator
boolean isDisplayed = browserUtils.isElementDisplayed(By.id("element"));
```

#### **Fallback Locator Support**:
```java
// Click with multiple fallback locators
browserUtils.clickWithFallback(
    By.id("loginBtn"),
    By.cssSelector(".login-button"),
    By.xpath("//button[contains(text(), 'Login')]")
);

// Send keys with fallback locators
browserUtils.sendKeysWithFallback("testuser",
    By.id("username"),
    By.name("username"),
    By.cssSelector("input[type='text']")
);

// Get text with fallback locators
String text = browserUtils.getTextWithFallback(
    By.cssSelector(".error-message"),
    By.xpath("//div[@class='error']"),
    By.id("errorMsg")
);
```

#### **Custom Timeout Support**:
```java
// Click with custom timeout
browserUtils.click(By.id("slowButton"), 30);

// Send keys with custom timeout
browserUtils.sendKeys(By.id("field"), "text", 15);

// Wait for element visibility with custom timeout
browserUtils.waitForElementToBeVisible(By.id("element"), 20);
```

### **Page Class Structure**

#### **Locator Organization**:
```java
public class HomePage {
    // Locators defined as static final constants
    private static final By LOGO = By.id("logo");
    private static final By SEARCH_BOX = By.cssSelector(".search-box input");
    private static final By SEARCH_BUTTON = By.cssSelector(".search-box button");
    
    // Fallback locators for critical elements
    private static final By LOGIN_BUTTON_PRIMARY = By.cssSelector(".login-btn");
    private static final By LOGIN_BUTTON_FALLBACK1 = By.xpath("//button[contains(text(), 'Login')]");
    private static final By LOGIN_BUTTON_FALLBACK2 = By.id("login-button");
    
    // Methods use locators directly
    public void clickLogo() throws Exception {
        browserUtils.click(LOGO);
    }
    
    public void searchFor(String term) throws Exception {
        browserUtils.sendKeys(SEARCH_BOX, term);
        browserUtils.click(SEARCH_BUTTON);
    }
    
    public void clickLoginButton() throws Exception {
        browserUtils.clickWithFallback(
            LOGIN_BUTTON_PRIMARY,
            LOGIN_BUTTON_FALLBACK1,
            LOGIN_BUTTON_FALLBACK2
        );
    }
}
```

### **Advanced BrowserUtils Features**

#### **Element State Checking**:
```java
// Check if element is present with timeout
boolean isPresent = browserUtils.isElementPresent(By.id("element"), 10);

// Check if element is displayed
boolean isDisplayed = browserUtils.isElementDisplayed(By.id("element"));

// Check if element is enabled
boolean isEnabled = browserUtils.isElementEnabled(By.id("button"));

// Get element count
int count = browserUtils.getElementCount(By.cssSelector(".items"));
```

#### **Advanced Interactions**:
```java
// JavaScript click with locator
browserUtils.javascriptClick(By.id("element"));

// Double click with locator
browserUtils.doubleClick(By.id("element"));

// Right click with locator
browserUtils.rightClick(By.id("element"));

// Drag and drop with locators
browserUtils.dragAndDrop(By.id("source"), By.id("target"));

// Move to element with locator
browserUtils.moveToElement(By.id("element"));
```

#### **Form Handling**:
```java
// Clear field with locator
browserUtils.clearField(By.id("inputField"));

// Select dropdown options with locator
browserUtils.selectDropdownByValue(By.id("dropdown"), "value");
browserUtils.selectDropdownByText(By.id("dropdown"), "Option Text");
browserUtils.selectDropdownByIndex(By.id("dropdown"), 2);
```

### **Benefits of the New Approach**

#### **1. Reduced Memory Usage**
- No WebElement objects stored in page classes
- Elements are found fresh each time, reducing stale element exceptions
- Lower memory footprint for page object instances

#### **2. Better Error Handling**
- Centralized error handling in BrowserUtils
- Consistent error messages and logging
- Automatic retry mechanisms for transient failures

#### **3. Improved Maintainability**
- Single source of truth for element interaction logic
- Easier to update interaction behavior across all page classes
- Consistent approach across the entire framework

#### **4. Enhanced Reliability**
- Built-in fallback locator support
- Automatic waiting strategies
- Reduced flakiness from stale element references

#### **5. Cleaner Code**
- Page classes focus on business logic
- Reduced boilerplate code
- More readable and maintainable test code

### **Migration Guide**

To migrate existing page classes to the new approach:

1. **Convert WebElement variables to locator constants**:
```java
// Old
private final By usernameLocator = By.id("username");
private WebElement usernameElement;

// New
private static final By USERNAME_FIELD = By.id("username");
```

2. **Update method calls to use locators directly**:
```java
// Old
usernameElement = driver.findElement(usernameLocator);
browserUtils.sendKeys(usernameElement, username);

// New
browserUtils.sendKeys(USERNAME_FIELD, username);
```

3. **Add fallback locators for critical elements**:
```java
// Define multiple locators for important elements
private static final By LOGIN_BUTTON_PRIMARY = By.id("loginBtn");
private static final By LOGIN_BUTTON_FALLBACK = By.cssSelector(".login-button");

// Use fallback in methods
browserUtils.clickWithFallback(LOGIN_BUTTON_PRIMARY, LOGIN_BUTTON_FALLBACK);
```

This improved approach makes the framework more robust, maintainable, and easier to work with while reducing the complexity of page object classes.

## 🏗️ **Layered Architecture Overview**

The framework follows a sophisticated layered architecture pattern that separates concerns and promotes maintainability, scalability, and testability.

### **Architecture Diagram**

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              TEST LAYER                                        │
├─────────────────────────────────────────────────────────────────────────────────┤
│  • Test Classes (UI/API)     • Test Data Management    • Test Orchestration   │
│  • TestNG Configurations     • Test Execution Logic    • Test Validation      │
└─────────────────────────────────────────────────────────────────────────────────┘
                                        │
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           BUSINESS LOGIC LAYER                                 │
├─────────────────────────────────────────────────────────────────────────────────┤
│  • Business Workflows        • Test Scenarios Logic    • Domain Operations    │
│  • User Journey Flows        • Business Validations    • Process Automation   │
└─────────────────────────────────────────────────────────────────────────────────┘
                                        │
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           PAGE OBJECT LAYER                                    │
├─────────────────────────────────────────────────────────────────────────────────┤
│  • Page Object Classes       • Component Objects       • Element Interactions │
│  • Smart Locator Strategies  • Page Validations        • UI Abstractions      │
└─────────────────────────────────────────────────────────────────────────────────┘
                                        │
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                             DRIVER LAYER                                       │
├─────────────────────────────────────────────────────────────────────────────────┤
│  • WebDriver Management      • Browser Configuration   • CDP Integration      │
│  • Driver Factory Pattern    • Remote/Local Execution  • Performance Monitor  │
└─────────────────────────────────────────────────────────────────────────────────┘
                                        │
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                            UTILITY LAYER                                       │
├─────────────────────────────────────────────────────────────────────────────────┤
│  • Configuration Management  • Data Providers          • String/Collection Utils│
│  • API Utilities            • File Handling           • Exception Management  │
└─────────────────────────────────────────────────────────────────────────────────┘
                                        │
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           REPORTING LAYER                                      │
├─────────────────────────────────────────────────────────────────────────────────┤
│  • Extent Reports           • Allure Integration       • Custom Listeners     │
│  • Screenshot Management    • Test Result Analytics    • Report Generation    │
└─────────────────────────────────────────────────────────────────────────────────┘
                                        │
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           INFRASTRUCTURE LAYER                                 │
├─────────────────────────────────────────────────────────────────────────────────┤
│  • CI/CD Integration         • Docker Containerization • Environment Config   │
│  • Jenkins Pipeline          • Grid Setup              • Deployment Scripts   │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### **Layer Responsibilities**

#### **1. Test Layer**
- **Purpose**: Contains actual test cases and test execution logic
- **Components**: 
  - Test classes organized by functionality (Authentication, E-Commerce, Navigation)
  - TestNG configuration files
  - Test data management
  - Test validation and assertions
- **Key Features**:
  - API-driven user creation and management
  - Session handling with tokens/cookies
  - End-to-end test scenarios
  - Parallel test execution

#### **2. Business Logic Layer**
- **Purpose**: Encapsulates business workflows and domain-specific operations
- **Components**:
  - User management utilities
  - Business process automation
  - Workflow orchestration
  - Domain-specific validations
- **Key Features**:
  - User creation via API
  - Pre-requisite data preparation
  - Business rule validation
  - Complex workflow automation

#### **3. Page Object Layer**
- **Purpose**: Abstracts UI interactions and provides clean interface for test layer
- **Components**:
  - Page Object classes implementing common interface
  - Reusable UI components
  - Smart locator strategies with fallback
  - Element interaction utilities
- **Key Features**:
  - Abstract base classes for consistency
  - Multiple locator strategies
  - Component reusability
  - Page validation methods

#### **4. Driver Layer**
- **Purpose**: Manages WebDriver lifecycle and browser interactions
- **Components**:
  - WebDriver factory with Singleton pattern
  - Browser configuration management
  - Chrome DevTools Protocol (CDP) integration
  - Remote/local execution support
- **Key Features**:
  - Thread-safe driver management
  - CDP for advanced browser automation
  - Performance monitoring
  - Network request/response tracking

#### **5. Utility Layer**
- **Purpose**: Provides common utilities and helper functions
- **Components**:
  - Configuration management
  - File handling utilities
  - String and collection manipulation
  - API utilities with multiple auth methods
- **Key Features**:
  - Advanced string manipulation with regex
  - Collection utilities with streams/lambdas
  - JSON parsing and handling
  - Security testing utilities

#### **6. Reporting Layer**
- **Purpose**: Handles test reporting and result management
- **Components**:
  - Multiple reporting formats (Extent, Allure)
  - Screenshot management
  - Test result analytics
  - Custom listeners
- **Key Features**:
  - Real-time reporting
  - Screenshot capture on failure
  - Performance metrics
  - Test execution analytics

#### **7. Infrastructure Layer**
- **Purpose**: Manages deployment, CI/CD, and environment setup
- **Components**:
  - Jenkins pipeline configuration
  - Docker containerization
  - Environment-specific configurations
  - Grid setup and management
- **Key Features**:
  - Automated deployment
  - Environment isolation
  - Scalable execution
  - Configuration management

## 🎯 **Advanced OOP Concepts Implementation**

### **1. Inheritance**
```java
// Abstract base class for all page objects
public abstract class AbstractBasePage implements IPageObject {
    // Common functionality for all pages
}

// Concrete page implementations
public class HomePage extends AbstractBasePage {
    // Specific implementation for home page
}
```

### **2. Abstraction**
```java
// Interface defining contract for page objects
public interface IPageObject {
    void waitForPageToLoad() throws Exception;
    boolean isPageLoaded();
    String getPageTitle();
}

// Abstract test data provider
public abstract class AbstractTestDataProvider implements ITestDataProvider {
    // Common caching and validation logic
}
```

### **3. Interfaces**
```java
// Multiple interfaces for different concerns
public interface IWebDriverManager { /* Driver management */ }
public interface IReportManager { /* Reporting operations */ }
public interface ITestDataProvider { /* Data operations */ }
```

### **4. Encapsulation**
```java
public class WebDriverManager {
    private static WebDriverManager instance; // Private singleton instance
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
    
    private WebDriverManager() {} // Private constructor
    
    // Public methods with controlled access
    public static WebDriverManager getInstance() { /* Singleton access */ }
}
```

### **5. Composition**
```java
public class SmartLocatorManager {
    private final WebDriver driver;                    // Composition
    private final LocatorFallback locatorFallback;     // Composition
    private final List<LocatorStrategy> strategies;    // Composition
    
    // Uses composed objects to provide functionality
}
```

## 🚀 **Advanced Java Features**

### **Collections with Streams and Lambdas**
```java
// Filter and transform collections
public static <T, R> List<R> filterAndTransform(Collection<T> collection, 
                                               Predicate<T> filter, 
                                               Function<T, R> transformer) {
    return collection.stream()
            .filter(Objects::nonNull)
            .filter(filter)
            .map(transformer)
            .collect(Collectors.toList());
}

// Group elements by classifier
public static <T, K> Map<K, List<T>> groupBy(Collection<T> collection, 
                                            Function<T, K> classifier) {
    return collection.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(classifier));
}
```

### **Exception Handling with Custom Hierarchy**
```java
// Base framework exception
public class FrameworkException extends Exception {
    private final String errorCode;
    private final String component;
    // Comprehensive error handling
}

// Specific exceptions for different components
public class PageObjectException extends FrameworkException { /* Page-specific errors */ }
public class WebDriverException extends FrameworkException { /* Driver-specific errors */ }
public class APIException extends FrameworkException { /* API-specific errors */ }
```

### **File Handling and JSON Parsing**
```java
public class JsonTestDataProvider extends AbstractTestDataProvider {
    // Advanced JSON parsing with Jackson
    public Map<String, Object> parseComplexJson(String jsonContent) {
        return objectMapper.readValue(jsonContent, new TypeReference<Map<String, Object>>() {});
    }
    
    // File operations with streams
    public List<String> getAvailableTestData() {
        return Files.list(dirPath)
                .filter(Files::isRegularFile)
                .map(Path::getFileName)
                .map(Path::toString)
                .filter(name -> name.endsWith(".json"))
                .collect(Collectors.toList());
    }
}
```

### **Ternary Operators and Functional Programming**
```java
// Ternary operators for concise conditional logic
public static boolean isValidInput(String input, int minLength, int maxLength, 
                                 boolean requireNumbers, boolean requireSpecialChars) {
    return input != null 
            ? input.length() >= minLength && input.length() <= maxLength
              && (!requireNumbers || input.matches(".*\\d.*"))
              && (!requireSpecialChars || input.matches(".*[!@#$%^&*()_+\\-=].*"))
            : false;
}

// Optional chaining for null safety
public static String cleanString(String input) {
    return Optional.ofNullable(input)
            .map(String::trim)
            .map(s -> s.replaceAll("\\s+", " "))
            .map(s -> s.replaceAll("[^\\w\\s]", ""))
            .orElse("");
}
```

## 🔍 **Smart Locator Strategy**

### **Multiple Locator Approaches**
```java
// Primary strategy (ID, CSS)
public class PrimaryLocatorStrategy implements LocatorStrategy {
    @Override
    public List<By> getLocators() {
        return List.of(
                By.id(id),
                By.cssSelector(cssSelector)
        );
    }
}

// Fallback strategy (XPath, text-based)
public class FallbackLocatorStrategy implements LocatorStrategy {
    @Override
    public List<By> getLocators() {
        return List.of(
                By.xpath(xpath),
                By.xpath("//*[contains(text(), '" + textContent + "')]"),
                By.className(className)
        );
    }
}
```

### **Smart Locator Manager**
```java
public class SmartLocatorManager {
    public WebElement findElement(String elementName) throws Exception {
        List<By> allLocators = strategies.stream()
                .flatMap(strategy -> strategy.getLocators().stream())
                .collect(Collectors.toList());
        
        return locatorFallback.findElementWithFallback(allLocators);
    }
}
```

## 🌐 **Chrome DevTools Protocol (CDP) Integration**

### **CDP Setup and Configuration**
```java
private void setupCDP(WebDriver driver) {
    if (driver instanceof ChromeDriver chromeDriver) {
        DevTools devTools = chromeDriver.getDevTools();
        devTools.createSession();
        
        // Enable Network domain for monitoring
        devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
        
        // Enable Performance domain
        devTools.send(Performance.enable(Optional.empty()));
        
        // Enable Runtime domain for JavaScript execution
        devTools.send(Runtime.enable());
    }
}
```

### **Network Monitoring**
```java
public void enableNetworkMonitoring() {
    DevTools devTools = getDevTools();
    if (devTools != null) {
        devTools.addListener(Network.requestWillBeSent(), request -> {
            logger.debug("Network Request: {} {}", 
                    request.getRequest().getMethod(), 
                    request.getRequest().getUrl());
        });
        
        devTools.addListener(Network.responseReceived(), response -> {
            logger.debug("Network Response: {} - Status: {}", 
                    response.getResponse().getUrl(),
                    response.getResponse().getStatus());
        });
    }
}
```

### **Performance Metrics**
```java
public Map<String, Object> getPerformanceMetrics() {
    DevTools devTools = getDevTools();
    Map<String, Object> metrics = new HashMap<>();
    
    if (devTools != null) {
        var performanceMetrics = devTools.send(Performance.getMetrics());
        performanceMetrics.forEach(metric -> {
            metrics.put(metric.getName(), metric.getValue());
        });
    }
    
    return metrics;
}
```

### **JavaScript Execution via CDP**
```java
public Object executeJavaScriptCDP(String script) {
    DevTools devTools = getDevTools();
    if (devTools != null) {
        var result = devTools.send(Runtime.evaluate(script, /* parameters */));
        return result.getResult().getValue().orElse(null);
    }
    return null;
}
```

## 🛠️ **Setup and Installation**

### **Prerequisites**
- Java 11 or higher
- Maven 3.6 or higher
- Chrome/Firefox/Edge browser
- Docker (optional)
- Jenkins (optional)

### **Installation Steps**

1. **Clone the repository**
```bash
git clone <repository-url>
cd advanced-test-automation-framework
```

2. **Install dependencies**
```bash
mvn clean install
```

3. **Configure environment**
```bash
cp .env.example .env
# Edit .env with your configuration
```

4. **Set up test data directories**
```bash
mkdir -p src/test/resources/testdata
mkdir -p reports/screenshots
mkdir -p logs
```

## 🏃 **Running Tests**

### **Command Line Execution**

#### **Sanity Tests**
```bash
# Basic sanity tests
mvn test -Psanity

# With specific browser and environment
mvn test -Psanity -Dbrowser=chrome -Denvironment=qa -Dheadless=false

# With CDP monitoring enabled
mvn test -Psanity -Dcdp.monitoring=true
```

#### **Regression Tests**
```bash
# Full regression suite
mvn test -Pregression

# Specific test groups
mvn test -Pregression -Dgroups="authentication,navigation"

# Parallel execution
mvn test -Pregression -Dthread.count=3
```

#### **Security Tests**
```bash
# Security vulnerability tests
mvn test -Psecurity

# Specific security groups
mvn test -Psecurity -Dgroups="sql-injection,xss,rate-limiting"
```

### **IDE Execution (Eclipse)**

1. **Import Project**
   - File → Import → Existing Maven Projects
   - Select the project directory

2. **Install TestNG Plugin**
   - Help → Eclipse Marketplace → Search "TestNG"

3. **Run Tests**
   - Right-click TestNG XML files → Run As → TestNG Suite
   - Right-click test classes → Run As → TestNG Test

### **Docker Execution**

```bash
# Build image
docker build -t test-automation-framework .

# Run tests
docker run --rm -e ENVIRONMENT=qa -e BROWSER=chrome test-automation-framework sanity
```

## 📊 **Advanced Features**

### **API-Driven User Management**
- Create test users dynamically via API
- Setup user sessions using tokens/cookies
- Prepare users with shopping carts, payment methods, addresses
- Automatic cleanup after test completion

### **Multiple Authentication Methods**
- Bearer Token Authentication
- API Key Authentication
- Basic Authentication
- OAuth 2.0 Authentication
- Custom Authorization Headers

### **Comprehensive Security Testing**
- SQL Injection vulnerability testing
- XSS attack prevention validation
- Token manipulation testing
- Rate limiting verification
- Input validation testing

### **Performance Monitoring**
- CDP-based performance metrics
- Network request/response monitoring
- Page load time measurement
- Resource usage tracking

### **Advanced Reporting**
- Multiple report formats (Extent, Allure)
- Real-time test execution tracking
- Screenshot capture on failures
- Performance metrics integration
- Test execution analytics

## 🔧 **Configuration Management**

### **Environment-Specific Configurations**
```properties
# QA Environment (qa-config.properties)
app.base.url=https://qa.example.com
browser.name=chrome
browser.headless=false
api.base.url=https://qa-api.example.com

# Production Environment (prod-config.properties)
app.base.url=https://prod.example.com
browser.headless=true
api.base.url=https://prod-api.example.com
```

### **Runtime Configuration**
```bash
# Override configuration at runtime
mvn test -Psanity -Dapp.base.url=https://custom.example.com -Dbrowser.name=firefox
```

## 📈 **Best Practices Implemented**

### **Code Organization**
- Clear separation of concerns with layered architecture
- Interface-based design for flexibility
- Abstract base classes for common functionality
- Composition over inheritance where appropriate

### **Error Handling**
- Custom exception hierarchy
- Comprehensive try-catch blocks
- Graceful degradation on failures
- Detailed error logging and reporting

### **Performance Optimization**
- Thread-safe driver management
- Efficient element location strategies
- Parallel test execution
- Resource cleanup and management

### **Maintainability**
- Modular design with clear interfaces
- Extensive logging and documentation
- Configuration-driven approach
- Automated cleanup and setup

## 🚀 **Advanced Usage Examples**

### **Using Smart Locator Manager**
```java
SmartLocatorManager locatorManager = new SmartLocatorManager(driver);

// Add multiple strategies
locatorManager.addStrategy(new PrimaryLocatorStrategy("loginButton", "login-btn", ".login-button"));
locatorManager.addStrategy(new FallbackLocatorStrategy("loginButton", "//button[@type='submit']", "Login", "btn-primary"));

// Find element with automatic fallback
WebElement loginButton = locatorManager.findElement("loginButton");
```

### **Advanced Collection Operations**
```java
// Filter and transform test data
List<User> activeUsers = AdvancedCollectionUtils.filterAndTransform(
    allUsers,
    user -> user.isActive(),
    user -> user.getUsername()
);

// Group test results by status
Map<String, List<TestResult>> groupedResults = AdvancedCollectionUtils.groupBy(
    testResults,
    TestResult::getStatus
);
```

### **Complex String Manipulations**
```java
// Extract and validate data
List<String> emails = AdvancedStringUtils.extractEmails(textContent);
List<String> phones = AdvancedStringUtils.extractPhoneNumbers(textContent);

// Mask sensitive data
String maskedData = AdvancedStringUtils.maskSensitiveData(
    creditCardNumber, 
    "\\d{4}-\\d{4}-\\d{4}-\\d{4}", 
    '*'
);
```

### **CDP Performance Monitoring**
```java
WebDriverManager driverManager = WebDriverManager.getInstance();
WebDriver driver = driverManager.createDriver();

// Enable network monitoring
driverManager.enableNetworkMonitoring();

// Execute test actions
// ...

// Capture performance metrics
Map<String, Object> metrics = driverManager.getPerformanceMetrics();
logger.info("Performance metrics: {}", metrics);
```

This enhanced framework provides a robust, scalable, and maintainable foundation for enterprise-level test automation with advanced OOP concepts, modern Java features, and sophisticated architecture patterns.

## 📸 **Screenshot Management**

### **Automatic Screenshot Capture**
The framework automatically captures screenshots on test failures and provides utilities for manual screenshot capture during test execution.

#### **Key Features**:
- **Automatic Failure Screenshots**: Captured automatically when UI tests fail
- **Manual Screenshot Capture**: Capture screenshots at any point during test execution
- **Report Integration**: Screenshots are automatically attached to Extent Reports
- **Organized Storage**: Screenshots stored in organized directory structure with timestamps
- **Cleanup Utilities**: Automatic cleanup of old screenshots to manage disk space

#### **Usage Examples**:
```java
// Automatic failure screenshot (handled by TestListener)
@Test
public void testLogin() {
    // Test logic here
    // Screenshot automatically captured on failure
}

// Manual screenshot capture
String screenshotPath = ScreenshotUtils.captureScreenshot(driver, "login_page");

// Screenshot with custom message
String path = ScreenshotUtils.captureScreenshotWithMessage(driver, "checkout", "payment_step");

// Cleanup old screenshots
ScreenshotUtils.cleanupOldScreenshots(7); // Remove screenshots older than 7 days
```

## 📊 **Data-Driven Testing with Multiple Data Sources**

### **Comprehensive Data Provider Support**
The framework supports multiple data sources for data-driven testing, enabling flexible test data management across different formats.

#### **Supported Data Sources**:
1. **JSON Files**: Structured data with nested objects
2. **Excel Files**: Spreadsheet data with multiple sheets
3. **CSV Files**: Simple comma-separated values
4. **Database**: Direct database queries (via DBUtils)

#### **Data Provider Factory**
```java
// Automatic detection based on file extension
ITestDataProvider provider = TestDataProviderFactory.createDataProvider("testdata.xlsx");

// Explicit type specification
ITestDataProvider csvProvider = TestDataProviderFactory.createDataProvider(
    "testdata.csv", 
    DataProviderType.CSV
);
```

### **TestNG Integration**
```java
@Test(dataProvider = "loginDataProvider", dataProviderClass = TestNGDataProviders.class)
public void testLoginWithMultipleData(String username, String password, String expectedResult) {
    // Test implementation using provided data
}

@Test(dataProvider = "excelDataProvider", dataProviderClass = TestNGDataProviders.class)
public void testWithExcelData(Map<String, Object> testData) {
    // Test implementation using Excel data
}
```

### **JSON Data Provider**
```java
// testdata/login_test.json
{
  "username": "testuser",
  "password": "testpass",
  "expectedResult": "success",
  "userRole": "customer"
}

// Usage in test
JsonTestDataProvider provider = new JsonTestDataProvider("src/test/resources/testdata");
Map<String, Object> data = provider.getTestData("login_test");
```

### **Excel Data Provider**
```java
// Excel file with sheets: TestData, RegistrationData, etc.
ExcelDataProvider provider = new ExcelDataProvider("testdata.xlsx");
List<Map<String, Object>> data = provider.getBulkTestData("TestData");

// Get specific test data
Map<String, Object> loginData = provider.getTestData("LoginTest");
```

### **CSV Data Provider**
```java
// CSV file with headers: username,password,expectedResult
CsvDataProvider provider = new CsvDataProvider("login_data.csv");
Object[][] testData = provider.getDataForTestNG();

// Get specific columns
Object[][] loginData = provider.getColumnsForTestNG("username", "password");
```

### **Dynamic Data Provider**
The framework supports environment-specific data loading:

```java
@Test(dataProvider = "dynamicDataProvider")
public void testWithDynamicData(Map<String, Object> testData) {
    // Data loaded based on environment (qa, uat, prod)
    // Data source determined by system property (json, excel, csv)
}
```

#### **Environment Configuration**:
```bash
# Load from different sources based on environment
mvn test -Denvironment=qa -DdataSource=excel
mvn test -Denvironment=prod -DdataSource=json
```

## 🔧 **Java 14 Compatibility**

The framework has been updated to use Java 14 features while maintaining backward compatibility:

### **Enhanced Switch Expressions**
```java
// Java 14 switch expressions for cleaner code
String result = switch (statusCode) {
    case 200 -> "OK - Request successful";
    case 201 -> "Created - Resource created";
    case 404 -> "Not Found - Resource doesn't exist";
    default -> "Unknown status code: " + statusCode;
};
```

### **Pattern Matching (Preview)**
```java
// Enhanced instanceof with pattern matching
if (driver instanceof ChromeDriver chromeDriver) {
    DevTools devTools = chromeDriver.getDevTools();
    // Use devTools directly
}
```

### **Text Blocks (Preview)**
```java
// Multi-line strings for better readability
String jsonSchema = """
    {
        "type": "object",
        "properties": {
            "username": {"type": "string"},
            "email": {"type": "string"}
        }
    }
    """;
```

## 🏃 **Running Data-Driven Tests**

### **Command Line Execution**
```bash
# Run all data-driven tests
mvn test -Dgroups="data-driven"

# Run with specific data source
mvn test -Dgroups="data-driven" -DdataSource=excel

# Run with environment-specific data
mvn test -Dgroups="data-driven" -Denvironment=qa

# Run specific data-driven test types
mvn test -Dgroups="data-driven,login"
mvn test -Dgroups="data-driven,registration"
```

### **IDE Execution**
1. Right-click on `DataDrivenTests.java`
2. Select "Run As" → "TestNG Test"
3. Data will be automatically loaded from configured sources

## 📁 **Test Data Organization**

```
src/test/resources/testdata/
├── qa/
│   ├── login_test.json
│   ├── user_data.json
│   └── api_tests.json
├── uat/
│   ├── login_test.json
│   └── user_data.json
├── prod/
│   └── production_data.json
├── login_data.csv
├── registration_data.xlsx
└── api_test_data.json
```

## 🎯 **Best Practices for Data-Driven Testing**

### **1. Data Organization**
- **Environment Separation**: Keep environment-specific data in separate directories
- **Logical Grouping**: Group related test data together
- **Consistent Naming**: Use consistent naming conventions across data files

### **2. Data Validation**
- **Schema Validation**: Validate data structure before test execution
- **Data Sanitization**: Clean and validate data during loading
- **Error Handling**: Graceful handling of missing or invalid data

### **3. Performance Optimization**
- **Data Caching**: Cache frequently used data to improve performance
- **Lazy Loading**: Load data only when needed
- **Parallel Execution**: Design data providers for parallel test execution

### **4. Maintenance**
- **Version Control**: Keep test data in version control
- **Documentation**: Document data structure and usage
- **Regular Cleanup**: Remove obsolete test data regularly

## 🔥 **Advanced API Testing Capabilities**

### **1. Handling Large JSON Responses (Thousands of Records)**

#### **Problem**: How do you fetch a specific JSON object from an API response containing thousands of records and use it in a subsequent API request?

#### **Solution**: 
```java
// Use JsonPath for efficient querying of large datasets
ResponseManager responseManager = new ResponseManager();

// Find specific record by ID from large response
Map<String, Object> specificUser = responseManager.findRecordById(response, "id", "user_12345");

// Use extracted data in subsequent request
String userEmail = (String) specificUser.get("email");
Response profileResponse = new RequestBuilder()
        .endpoint("/users/profile")
        .queryParam("email", userEmail)
        .build()
        .get("/users/profile");
```

#### **Key Features**:
- **JsonPath Integration**: Efficient querying of large JSON structures
- **Stream-based Filtering**: Filter thousands of records using functional programming
- **Pagination Support**: Automatically handle paginated responses
- **Memory Optimization**: Process large datasets without memory issues

### **2. Token Refresh During API Operations**

#### **Problem**: What happens when your token gets refreshed in the middle of an upload API test?

#### **Solution**: 
```java
// Thread-safe token manager with automatic refresh
TokenManager tokenManager = TokenManager.getInstance();

public void testTokenRefreshDuringUpload() throws Exception {
    String token = tokenManager.getValidToken("user");
    
    Response uploadResponse = uploadFile(token);
    
    // Handle token expiry during upload
    if (uploadResponse.getStatusCode() == 401) {
        logger.info("Token expired during upload, refreshing...");
        tokenManager.refreshToken("user");
        String newToken = tokenManager.getValidToken("user");
        uploadResponse = uploadFile(newToken); // Retry with new token
    }
}
```

#### **Key Features**:
- **Automatic Token Refresh**: Detects expiry and refreshes automatically
- **Thread-Safe Implementation**: Singleton pattern with concurrent access
- **Multiple Token Types**: Support for admin, user, API key tokens
- **Retry Logic**: Automatic retry with refreshed tokens

### **3. Authentication Failure Handling**

#### **Problem**: How do you handle authentication failures during execution?

#### **Solution**: 
```java
public void handleAuthenticationFailure() throws Exception {
    try {
        Response response = makeAPICall();
        
        if (response.getStatusCode() == 401) {
            // Handle unauthorized access
            logger.warn("Authentication failed, attempting token refresh");
            tokenManager.refreshToken("user");
            response = retryAPICall();
        } else if (response.getStatusCode() == 403) {
            // Handle insufficient permissions
            logger.error("Insufficient permissions for this operation");
            throw new APIException("Access forbidden", 403);
        }
    } catch (Exception e) {
        // Graceful degradation
        handleAuthenticationError(e);
    }
}
```

#### **Key Features**:
- **Comprehensive Error Handling**: Different strategies for 401, 403, etc.
- **Graceful Degradation**: Continue testing with alternative approaches
- **Detailed Logging**: Track authentication issues for debugging
- **Recovery Mechanisms**: Automatic retry with proper credentials

### **4. HTTP Status Code Validation**

#### **Problem**: What is the real difference between status codes like 204, 401, 403, and 405, and how do you automate validation for them?

#### **Solution**: 
```java
public class StatusCodeValidator {
    
    public static String getStatusCodeExplanation(int statusCode) {
        return switch (statusCode) {
            case 200 -> "OK - Request successful, response contains data";
            case 201 -> "Created - Resource successfully created";
            case 204 -> "No Content - Request successful but no content to return (common for DELETE)";
            case 401 -> "Unauthorized - Authentication required or invalid credentials";
            case 403 -> "Forbidden - Valid credentials but insufficient permissions";
            case 404 -> "Not Found - Resource doesn't exist";
            case 405 -> "Method Not Allowed - HTTP method not supported for this endpoint";
            case 429 -> "Too Many Requests - Rate limit exceeded";
            default -> "Unknown status code: " + statusCode;
        };
    }
    
    public static void validateCreationResponse(Response response) throws Exception {
        validateAndExplain(response, 201);
        // Additional validations for creation responses
        String location = response.getHeader("Location");
        String resourceId = response.jsonPath().getString("id");
        // Validate presence of required headers and fields
    }
}
```

#### **Status Code Differences Explained**:
- **204 vs 200**: 204 means success with no content (DELETE), 200 means success with content
- **401 vs 403**: 401 means "who are you?" (authentication), 403 means "I know who you are but you can't do this" (authorization)
- **404 vs 405**: 404 means resource not found, 405 means resource exists but method not allowed

### **5. Dynamic Schema Validation**

#### **Problem**: How do you validate dynamic response schemas that change frequently?

#### **Solution**: 
```java
// Flexible schema definition with builder pattern
SchemaValidator.SchemaDefinition schema = SchemaValidator.schema()
        .requireField("id")
        .requireField("username")
        .fieldType("id", String.class)
        .fieldType("isActive", Boolean.class)
        .customRule("validEmail", node -> {
            if (node.has("email")) {
                String email = node.get("email").asText();
                return email.contains("@") && email.contains(".");
            }
            return true;
        })
        .nestedSchema("profile", profileSchema)
        .build();

boolean isValid = schemaValidator.validateDynamicSchema(response, schema);
```

#### **Key Features**:
- **Flexible Schema Definition**: Builder pattern for easy schema creation
- **Custom Validation Rules**: Lambda-based custom validation logic
- **Nested Schema Support**: Validate complex nested JSON structures
- **Runtime Schema Adaptation**: Adjust validation rules based on API version

### **6. Chained API Workflows**

#### **Problem**: How would you design a chained flow of POST, GET, and DELETE requests in Rest Assured using reusable payloads?

#### **Solution**: 
```java
// Define reusable chain workflows
ChainDefinition crudChain = APIChainExecutor.createCrudChain("users", userData);
ChainResult result = chainExecutor.executeChain(crudChain);

// E-commerce order workflow
ChainDefinition orderChain = APIChainExecutor.createOrderChain(orderData);
ChainResult orderResult = chainExecutor.executeChain(orderChain);

// Custom chain with data extraction
ChainDefinition customChain = new ChainDefinition("CUSTOM_WORKFLOW")
        .addStep(new ChainStep("create_user", "POST", "/users")
                .body(userData)
                .dataExtractor(response -> Map.of("userId", response.jsonPath().getString("id")))
                .validator(response -> response.getStatusCode() == 201))
        .addStep(new ChainStep("get_user", "GET", "/users/${userId}")
                .validator(response -> response.getStatusCode() == 200));
```

#### **Key Features**:
- **Chain of Responsibility Pattern**: Modular, reusable workflow steps
- **Data Extraction**: Automatically extract data between steps
- **Context Substitution**: Use previous step data in subsequent requests
- **Validation Pipeline**: Validate each step before proceeding

### **7. Parallel API Test Execution**

#### **Problem**: What are the best practices for handling parallel execution of API tests in a CI/CD pipeline?

#### **Best Practices Implemented**:

```java
// Thread-safe token management
public class TokenManager {
    private static volatile TokenManager instance;
    private final ConcurrentHashMap<String, TokenInfo> tokens = new ConcurrentHashMap<>();
    private static final ReentrantLock lock = new ReentrantLock();
    
    public String getValidToken(String tokenType) throws Exception {
        // Thread-safe token retrieval and refresh
    }
}

// TestNG parallel execution configuration
<suite name="ParallelAPITests" parallel="tests" thread-count="5">
    <test name="API Tests Group 1">
        <classes>
            <class name="UserServiceTest"/>
        </classes>
    </test>
    <test name="API Tests Group 2">
        <classes>
            <class name="PaymentServiceTest"/>
        </classes>
    </test>
</suite>
```

#### **Parallel Execution Best Practices**:
- **Thread-Safe Components**: All managers use concurrent collections
- **Isolated Test Data**: Each thread uses unique test data
- **Resource Management**: Proper cleanup in @AfterMethod
- **Shared State Avoidance**: No shared mutable state between tests
- **Connection Pooling**: Efficient HTTP connection management

### **8. Production Bug Analysis**

#### **Problem**: How do you debug a situation where your regression suite passes but a production bug still slips through?

#### **Solution Framework**:

```java
// Enhanced logging and monitoring
public class ProductionBugAnalyzer {
    
    public void analyzeProductionIssue(String bugReport) {
        // 1. Environment Comparison
        compareEnvironmentConfigurations();
        
        // 2. Data Analysis
        analyzeTestDataVsProductionData();
        
        // 3. Test Coverage Analysis
        analyzeTestCoverage();
        
        // 4. Timing and Load Analysis
        analyzeTimingAndLoadDifferences();
    }
    
    private void compareEnvironmentConfigurations() {
        // Compare test vs production configurations
        // Database versions, API versions, feature flags
    }
    
    private void analyzeTestDataVsProductionData() {
        // Analyze differences in data patterns
        // Edge cases not covered in test data
    }
}
```

#### **Debug Strategies**:
1. **Environment Parity**: Ensure test environments match production
2. **Data Analysis**: Compare test data patterns with production data
3. **Load Testing**: Test under production-like load conditions
4. **Edge Case Coverage**: Identify and test edge cases
5. **Monitoring Integration**: Real-time monitoring in test environments
6. **Chaos Engineering**: Introduce controlled failures during testing

## 🎨 **Advanced Design Patterns Implementation**

### **1. Builder Pattern**
```java
// Fluent API request building
Response response = new RequestBuilder()
        .baseUrl("https://api.example.com")
        .endpoint("/users")
        .auth(token)
        .header("Content-Type", "application/json")
        .queryParam("page", 1)
        .body(userData)
        .timeout(30000)
        .build()
        .post("/users");
```

### **2. Singleton Pattern with Thread Safety**
```java
public class TokenManager {
    private static volatile TokenManager instance;
    private static final ReentrantLock lock = new ReentrantLock();
    
    public static TokenManager getInstance() {
        if (instance == null) {
            lock.lock();
            try {
                if (instance == null) {
                    instance = new TokenManager();
                }
            } finally {
                lock.unlock();
            }
        }
        return instance;
    }
}
```

### **3. Strategy Pattern**
```java
// Multiple locator strategies
public interface LocatorStrategy {
    List<By> getLocators();
    String getDescription();
    int getPriority();
}

public class SmartLocatorManager {
    private final List<LocatorStrategy> strategies;
    
    public WebElement findElement(String elementName) throws Exception {
        // Use strategies in priority order with automatic fallback
    }
}
```

### **4. Chain of Responsibility Pattern**
```java
// API workflow chains
public class APIChainExecutor {
    public ChainResult executeChain(ChainDefinition chainDefinition) throws Exception {
        for (ChainStep step : chainDefinition.getSteps()) {
            Response response = executeStep(step);
            // Extract data for next steps
            // Validate response before proceeding
        }
    }
}
```

### **5. Factory Pattern**
```java
// WebDriver factory with multiple browser support
public class WebDriverManager {
    public WebDriver createDriver() {
        return switch (browserName.toLowerCase()) {
            case "chrome" -> createChromeDriver();
            case "firefox" -> createFirefoxDriver();
            case "edge" -> createEdgeDriver();
            default -> createDefaultDriver();
        };
    }
}
```

### **6. Observer Pattern**
```java
// Test execution listeners
public class TestListener implements ITestListener {
    @Override
    public void onTestStart(ITestResult result) {
        // Notify all observers of test start
    }
    
    @Override
    public void onTestFailure(ITestResult result) {
        // Notify observers and take screenshots
    }
}
```

## 🔧 **Thread Safety Implementation**

### **Concurrent Collections Usage**
```java
// Thread-safe token storage
private final ConcurrentHashMap<String, TokenInfo> tokens = new ConcurrentHashMap<>();

// Thread-safe driver management
private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
private static final ThreadLocal<DevTools> devToolsThreadLocal = new ThreadLocal<>();
```

### **Synchronization Mechanisms**
```java
// ReentrantLock for critical sections
private static final ReentrantLock lock = new ReentrantLock();

public void refreshToken(String tokenType) throws Exception {
    lock.lock();
    try {
        // Critical section - token refresh logic
    } finally {
        lock.unlock();
    }
}
```

### **Atomic Operations**
```java
// Atomic counters for test metrics
private final AtomicInteger testCount = new AtomicInteger(0);
private final AtomicLong totalExecutionTime = new AtomicLong(0);
```

## 🚀 **Running Advanced API Tests**

### **Command Line Execution**
```bash
# Run advanced API tests
mvn test -Papi-advanced

# Run with specific groups
mvn test -Papi-advanced -Dgroups="large-dataset,token-refresh,chained-requests"

# Run with parallel execution
mvn test -Papi-advanced -Dthread.count=5 -Dparallel=tests

# Run with custom configuration
mvn test -Papi-advanced -Dapi.base.url=https://staging-api.example.com
```

### **CI/CD Pipeline Integration**
```yaml
# Jenkins Pipeline Example
pipeline {
    agent any
    stages {
        stage('API Tests') {
            parallel {
                stage('User Service Tests') {
                    steps {
                        sh 'mvn test -Dtest=UserServiceTest'
                    }
                }
                stage('Payment Service Tests') {
                    steps {
                        sh 'mvn test -Dtest=PaymentServiceTest'
                    }
                }
                stage('Advanced API Tests') {
                    steps {
                        sh 'mvn test -Dtest=AdvancedAPITests'
                    }
                }
            }
        }
    }
}
```

This enhanced framework provides comprehensive solutions for all advanced API testing challenges, implements sophisticated design patterns, and ensures thread safety for parallel execution in enterprise environments.