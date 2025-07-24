package com.yourorg.base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import com.yourorg.utils.ConfigLoader;

import java.time.Duration;

public class WebDriverFactory {
    private static final Logger logger = LogManager.getLogger(WebDriverFactory.class);
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    public static WebDriver createDriver() {
        String browserName = System.getProperty("browser", ConfigLoader.get("browser.name", "chrome"));
        boolean headless = Boolean.parseBoolean(System.getProperty("headless", ConfigLoader.get("browser.headless", "false")));
        
        WebDriver driver;
        
        try {
            switch (browserName.toLowerCase()) {
                case "chrome":
                    WebDriverManager.chromedriver().setup();
                    driver = new ChromeDriver(getChromeOptions(headless));
                    break;
                case "firefox":
                    WebDriverManager.firefoxdriver().setup();
                    driver = new FirefoxDriver(getFirefoxOptions(headless));
                    break;
                case "edge":
                    WebDriverManager.edgedriver().setup();
                    driver = new EdgeDriver(getEdgeOptions(headless));
                    break;
                default:
                    logger.warn("Unsupported browser: {}. Using Chrome as default.", browserName);
                    WebDriverManager.chromedriver().setup();
                    driver = new ChromeDriver(getChromeOptions(headless));
                    break;
            }
            
            configureDriver(driver);
            driverThreadLocal.set(driver);
            logger.info("WebDriver created successfully - Browser: {}", browserName);
            
        } catch (Exception e) {
            logger.error("Failed to create WebDriver: {}", e.getMessage());
            throw new RuntimeException("WebDriver creation failed", e);
        }
        
        return driver;
    }

    private static ChromeOptions getChromeOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--remote-allow-origins=*");
        
        if (headless) {
            options.addArguments("--headless");
        }
        
        return options;
    }

    private static FirefoxOptions getFirefoxOptions(boolean headless) {
        FirefoxOptions options = new FirefoxOptions();
        
        if (headless) {
            options.addArguments("--headless");
        }
        
        return options;
    }

    private static EdgeOptions getEdgeOptions(boolean headless) {
        EdgeOptions options = new EdgeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        
        if (headless) {
            options.addArguments("--headless");
        }
        
        return options;
    }

    private static void configureDriver(WebDriver driver) {
        int implicitWait = Integer.parseInt(ConfigLoader.get("browser.implicit.wait", "10"));
        int pageLoadTimeout = Integer.parseInt(ConfigLoader.get("browser.page.load.timeout", "30"));
        
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(pageLoadTimeout));
        driver.manage().window().maximize();
    }

    public static WebDriver getDriver() {
        return driverThreadLocal.get();
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
}