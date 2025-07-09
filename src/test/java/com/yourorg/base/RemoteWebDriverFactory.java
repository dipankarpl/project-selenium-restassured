package com.yourorg.base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import com.yourorg.utils.ConfigLoader;
import com.yourorg.utils.EnvReader;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

public class RemoteWebDriverFactory {
    private static final Logger logger = LogManager.getLogger(RemoteWebDriverFactory.class);
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    public static WebDriver createDriver() {
        String browserName = EnvReader.get("BROWSER", ConfigLoader.get("browser.name", "chrome"));
        String remoteUrl = EnvReader.get("REMOTE_URL", ConfigLoader.get("remote.url"));
        boolean headless = EnvReader.getBoolean("HEADLESS", ConfigLoader.getBoolean("browser.headless", false));
        
        WebDriver driver;
        
        try {
            if (remoteUrl != null && !remoteUrl.isEmpty()) {
                driver = createRemoteDriver(browserName, remoteUrl, headless);
            } else {
                driver = createLocalDriver(browserName, headless);
            }
            
            configureDriver(driver);
            driverThreadLocal.set(driver);
            logger.info("WebDriver created successfully - Browser: {}, Remote: {}", browserName, remoteUrl != null);
            
        } catch (Exception e) {
            logger.error("Failed to create WebDriver: {}", e.getMessage());
            throw new RuntimeException("WebDriver creation failed", e);
        }
        
        return driver;
    }

    private static WebDriver createLocalDriver(String browserName, boolean headless) {
        WebDriver driver;
        
        switch (browserName.toLowerCase()) {
            case "chrome":
                WebDriverManager.chromedriver().setup();
                ChromeOptions chromeOptions = new ChromeOptions();
                configureChromeOptions(chromeOptions, headless);
                driver = new ChromeDriver(chromeOptions);
                break;
                
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                configureFirefoxOptions(firefoxOptions, headless);
                driver = new FirefoxDriver(firefoxOptions);
                break;
                
            case "edge":
                WebDriverManager.edgedriver().setup();
                EdgeOptions edgeOptions = new EdgeOptions();
                configureEdgeOptions(edgeOptions, headless);
                driver = new EdgeDriver(edgeOptions);
                break;
                
            case "safari":
                driver = new SafariDriver();
                break;
                
            default:
                logger.warn("Unsupported browser: {}. Using Chrome as default.", browserName);
                WebDriverManager.chromedriver().setup();
                ChromeOptions defaultOptions = new ChromeOptions();
                configureChromeOptions(defaultOptions, headless);
                driver = new ChromeDriver(defaultOptions);
                break;
        }
        
        return driver;
    }

    private static WebDriver createRemoteDriver(String browserName, String remoteUrl, boolean headless) throws MalformedURLException {
        WebDriver driver;
        
        switch (browserName.toLowerCase()) {
            case "chrome":
                ChromeOptions chromeOptions = new ChromeOptions();
                configureChromeOptions(chromeOptions, headless);
                driver = new RemoteWebDriver(new URL(remoteUrl), chromeOptions);
                break;
                
            case "firefox":
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                configureFirefoxOptions(firefoxOptions, headless);
                driver = new RemoteWebDriver(new URL(remoteUrl), firefoxOptions);
                break;
                
            case "edge":
                EdgeOptions edgeOptions = new EdgeOptions();
                configureEdgeOptions(edgeOptions, headless);
                driver = new RemoteWebDriver(new URL(remoteUrl), edgeOptions);
                break;
                
            default:
                logger.warn("Unsupported browser for remote: {}. Using Chrome as default.", browserName);
                ChromeOptions defaultOptions = new ChromeOptions();
                configureChromeOptions(defaultOptions, headless);
                driver = new RemoteWebDriver(new URL(remoteUrl), defaultOptions);
                break;
        }
        
        return driver;
    }

    private static void configureChromeOptions(ChromeOptions options, boolean headless) {
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-web-security");
        options.addArguments("--allow-running-insecure-content");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--remote-allow-origins=*");
        
        if (headless) {
            options.addArguments("--headless");
        }
        
        // Window size
        String windowSize = ConfigLoader.get("browser.window.size", "1920,1080");
        options.addArguments("--window-size=" + windowSize);
        
        // User agent
        String userAgent = ConfigLoader.get("browser.user.agent");
        if (userAgent != null) {
            options.addArguments("--user-agent=" + userAgent);
        }
    }

    private static void configureFirefoxOptions(FirefoxOptions options, boolean headless) {
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        
        if (headless) {
            options.addArguments("--headless");
        }
        
        // Window size
        String windowSize = ConfigLoader.get("browser.window.size", "1920,1080");
        String[] dimensions = windowSize.split(",");
        if (dimensions.length == 2) {
            options.addArguments("--width=" + dimensions[0]);
            options.addArguments("--height=" + dimensions[1]);
        }
    }

    private static void configureEdgeOptions(EdgeOptions options, boolean headless) {
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-extensions");
        options.addArguments("--remote-allow-origins=*");
        
        if (headless) {
            options.addArguments("--headless");
        }
        
        // Window size
        String windowSize = ConfigLoader.get("browser.window.size", "1920,1080");
        options.addArguments("--window-size=" + windowSize);
    }

    private static void configureDriver(WebDriver driver) {
        // Set timeouts
        int implicitWait = ConfigLoader.getInt("browser.implicit.wait", 10);
        int pageLoadTimeout = ConfigLoader.getInt("browser.page.load.timeout", 30);
        
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(pageLoadTimeout));
        
        // Maximize window if not headless
        boolean headless = EnvReader.getBoolean("HEADLESS", ConfigLoader.getBoolean("browser.headless", false));
        if (!headless) {
            driver.manage().window().maximize();
        }
    }

    public static WebDriver getDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver == null) {
            logger.warn("No driver found in current thread. Creating new driver.");
            driver = createDriver();
        }
        return driver;
    }

    public static void quitDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            try {
                driver.quit();
                logger.info("WebDriver quit successfully");
            } catch (Exception e) {
                logger.error("Error quitting WebDriver: {}", e.getMessage());
            } finally {
                driverThreadLocal.remove();
            }
        }
    }

    public static void removeDriver() {
        driverThreadLocal.remove();
    }
}