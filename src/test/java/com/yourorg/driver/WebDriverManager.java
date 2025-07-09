package com.yourorg.driver;

import com.yourorg.interfaces.IWebDriverManager;
import com.yourorg.utils.ConfigLoader;
import com.yourorg.utils.EnvReader;
import com.yourorg.interfaces.IWebDriverManager;
import com.yourorg.utils.ConfigLoader;
import com.yourorg.utils.EnvReader;
//import io.github.bonigarcia.wdm.WebDriverManager as WDM;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v118.network.Network;
import org.openqa.selenium.devtools.v118.performance.Performance;
import org.openqa.selenium.devtools.v118.runtime.Runtime;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Enhanced WebDriver Manager with CDP support
 * Implements Singleton pattern and factory pattern
 */
public class WebDriverManager implements IWebDriverManager {
    private static final Logger logger = LogManager.getLogger(WebDriverManager.class);
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<DevTools> devToolsThreadLocal = new ThreadLocal<>();
    private static WebDriverManager instance;
    
    // Private constructor for Singleton pattern
    private WebDriverManager() {}
    
    // Singleton instance getter with double-checked locking
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
    
    @Override
    public WebDriver createDriver() {
        String browserName = EnvReader.get("BROWSER", ConfigLoader.get("browser.name", "chrome"));
        String remoteUrl = EnvReader.get("REMOTE_URL", ConfigLoader.get("remote.url"));
        boolean headless = EnvReader.getBoolean("HEADLESS", ConfigLoader.getBoolean("browser.headless", false));
        
        WebDriver driver;
        
        try {
            driver = Optional.ofNullable(remoteUrl)
                    .filter(url -> !url.isEmpty())
                    .map(url -> createRemoteDriver(browserName, url, headless))
                    .orElseGet(() -> createLocalDriver(browserName, headless));
            
            configureDriver(driver);
            setupCDP(driver);
            driverThreadLocal.set(driver);
            
            logger.info("WebDriver created successfully - Browser: {}, Remote: {}", 
                    browserName, remoteUrl != null);
            
        } catch (Exception e) {
            logger.error("Failed to create WebDriver: {}", e.getMessage());
            throw new RuntimeException("WebDriver creation failed", e);
        }
        
        return driver;
    }
    
    private WebDriver createLocalDriver(String browserName, boolean headless) {
        switch (browserName.toLowerCase()) {
            case "chrome":
//                WDM.chromedriver().setup();
                return new ChromeDriver(configureChromeOptions(headless));
            case "firefox":
//                WDM.firefoxdriver().setup();
                return new FirefoxDriver(configureFirefoxOptions(headless));
            case "edge":
//                WDM.edgedriver().setup();
                return new EdgeDriver(configureEdgeOptions(headless));
            default:
                logger.warn("Unsupported browser: {}. Using Chrome as default.", browserName);
//                WDM.chromedriver().setup();
                return new ChromeDriver(configureChromeOptions(headless));
        }
    }
    
    private WebDriver createRemoteDriver(String browserName, String remoteUrl, boolean headless) {
        try {
            switch (browserName.toLowerCase()) {
                case "chrome":
                    return new RemoteWebDriver(new URL(remoteUrl), configureChromeOptions(headless));
                case "firefox":
                    return new RemoteWebDriver(new URL(remoteUrl), configureFirefoxOptions(headless));
                case "edge":
                    return new RemoteWebDriver(new URL(remoteUrl), configureEdgeOptions(headless));
                default:
                    logger.warn("Unsupported browser for remote: {}. Using Chrome as default.", browserName);
                    return new RemoteWebDriver(new URL(remoteUrl), configureChromeOptions(headless));
            }
        } catch (Exception e) {
            logger.error("Failed to create remote driver: {}", e.getMessage());
            throw new RuntimeException("Remote driver creation failed", e);
        }
    }
    
    private ChromeOptions configureChromeOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();
        
        // Performance and stability options
        List<String> chromeArgs = List.of(
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-gpu",
                "--disable-extensions",
                "--disable-web-security",
                "--allow-running-insecure-content",
                "--disable-blink-features=AutomationControlled",
                "--remote-allow-origins=*",
                "--disable-background-timer-throttling",
                "--disable-backgrounding-occluded-windows",
                "--disable-renderer-backgrounding"
        );
        
        options.addArguments(chromeArgs);
        
        if (headless) {
            options.addArguments("--headless=new");
        }
        
        // Window size
        String windowSize = ConfigLoader.get("browser.window.size", "1920,1080");
        options.addArguments("--window-size=" + windowSize);
        
        // User agent
        Optional.ofNullable(ConfigLoader.get("browser.user.agent"))
                .ifPresent(userAgent -> options.addArguments("--user-agent=" + userAgent));
        
        // Performance preferences
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.default_content_setting_values.notifications", 2);
        prefs.put("profile.default_content_settings.popups", 0);
        options.setExperimentalOption("prefs", prefs);
        
        return options;
    }
    
    private FirefoxOptions configureFirefoxOptions(boolean headless) {
        FirefoxOptions options = new FirefoxOptions();
        
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage");
        
        if (headless) {
            options.addArguments("--headless");
        }
        
        // Window size
        String windowSize = ConfigLoader.get("browser.window.size", "1920,1080");
        String[] dimensions = windowSize.split(",");
        if (dimensions.length == 2) {
            options.addArguments("--width=" + dimensions[0], "--height=" + dimensions[1]);
        }
        
        return options;
    }
    
    private EdgeOptions configureEdgeOptions(boolean headless) {
        EdgeOptions options = new EdgeOptions();
        
        List<String> edgeArgs = List.of(
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-gpu",
                "--disable-extensions",
                "--remote-allow-origins=*"
        );
        
        options.addArguments(edgeArgs);
        
        if (headless) {
            options.addArguments("--headless");
        }
        
        String windowSize = ConfigLoader.get("browser.window.size", "1920,1080");
        options.addArguments("--window-size=" + windowSize);
        
        return options;
    }
    
    @Override
    public void configureDriver(WebDriver driver) {
        int implicitWait = ConfigLoader.getInt("browser.implicit.wait", 10);
        int pageLoadTimeout = ConfigLoader.getInt("browser.page.load.timeout", 30);
        
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(pageLoadTimeout));
        
        boolean headless = EnvReader.getBoolean("HEADLESS", ConfigLoader.getBoolean("browser.headless", false));
        if (!headless) {
            driver.manage().window().maximize();
        }
    }
    
    /**
     * Setup Chrome DevTools Protocol (CDP) for advanced browser automation
     */
    private void setupCDP(WebDriver driver) {
        if (driver instanceof ChromeDriver) {
            ChromeDriver chromeDriver = (ChromeDriver) driver;
            try {
                DevTools devTools = chromeDriver.getDevTools();
                devTools.createSession();
                
                // Enable Network domain for network monitoring
                devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
                
                // Enable Performance domain for performance monitoring
                devTools.send(Performance.enable(Optional.empty()));
                
                // Enable Runtime domain for JavaScript execution
                devTools.send(Runtime.enable());
                
                // Store DevTools instance for later use
                devToolsThreadLocal.set(devTools);
                
                logger.info("CDP (Chrome DevTools Protocol) enabled successfully");
            } catch (Exception e) {
                logger.warn("Failed to setup CDP: {}", e.getMessage());
            }
        }
    }
    
    /**
     * Get DevTools instance for CDP operations
     */
    public DevTools getDevTools() {
        return devToolsThreadLocal.get();
    }
    
    /**
     * Monitor network requests using CDP
     */
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
    
    /**
     * Capture performance metrics using CDP
     */
    public Map<String, Object> getPerformanceMetrics() {
        DevTools devTools = getDevTools();
        Map<String, Object> metrics = new HashMap<>();
        
        if (devTools != null) {
            try {
                var performanceMetrics = devTools.send(Performance.getMetrics());
                
                performanceMetrics.forEach(metric -> {
                    metrics.put(metric.getName(), metric.getValue());
                });
                
                logger.info("Performance metrics captured: {} metrics", metrics.size());
            } catch (Exception e) {
                logger.warn("Failed to capture performance metrics: {}", e.getMessage());
            }
        }
        
        return metrics;
    }
    
    /**
     * Execute JavaScript using CDP
     */
    public Object executeJavaScriptCDP(String script) {
        DevTools devTools = getDevTools();
        if (devTools != null) {
            try {
                var result = devTools.send(Runtime.evaluate(script, Optional.empty(), 
                        Optional.empty(), Optional.empty(), Optional.empty(), 
                        Optional.empty(), Optional.empty(), Optional.empty(), 
                        Optional.empty(), Optional.empty(), Optional.empty(), 
                        Optional.empty(), Optional.empty(), Optional.empty(), 
                        Optional.empty(), Optional.empty(), Optional.empty()));
                
                return result.getResult().getValue().orElse(null);
            } catch (Exception e) {
                logger.error("Failed to execute JavaScript via CDP: {}", e.getMessage());
            }
        }
        return null;
    }
    
    @Override
    public WebDriver getDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver == null) {
            logger.warn("No driver found in current thread. Creating new driver.");
            driver = createDriver();
        }
        return driver;
    }
    
    @Override
    public void quitDriver() {
        WebDriver driver = driverThreadLocal.get();
        DevTools devTools = devToolsThreadLocal.get();
        
        if (devTools != null) {
            try {
                devTools.close();
                devToolsThreadLocal.remove();
                logger.debug("DevTools session closed");
            } catch (Exception e) {
                logger.warn("Error closing DevTools: {}", e.getMessage());
            }
        }
        
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
    
    @Override
    public boolean isDriverActive() {
        WebDriver driver = driverThreadLocal.get();
        if (driver == null) {
            return false;
        }
        
        try {
            driver.getCurrentUrl();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public void removeDriver() {
        driverThreadLocal.remove();
        devToolsThreadLocal.remove();
    }
}