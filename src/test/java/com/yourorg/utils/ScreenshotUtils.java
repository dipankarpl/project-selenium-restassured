package com.yourorg.utils;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScreenshotUtils {
    private static final Logger logger = LogManager.getLogger(ScreenshotUtils.class);
    private static final String SCREENSHOT_DIR = "reports/screenshots/";
    
    static {
        new File(SCREENSHOT_DIR).mkdirs();
    }
    
    public static String captureScreenshot(WebDriver driver, String testName) {
        if (driver == null) {
            logger.warn("Driver is null, cannot capture screenshot");
            return null;
        }
        
        try {
            TakesScreenshot screenshot = (TakesScreenshot) driver;
            File sourceFile = screenshot.getScreenshotAs(OutputType.FILE);
            
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            String fileName = testName + "_" + timestamp + ".png";
            String filePath = SCREENSHOT_DIR + fileName;
            
            File destFile = new File(filePath);
            FileUtils.copyFile(sourceFile, destFile);
            
            logger.info("Screenshot captured: {}", filePath);
            return filePath;
            
        } catch (Exception e) {
            logger.error("Failed to capture screenshot: {}", e.getMessage());
            return null;
        }
    }
    
    public static String captureFailureScreenshot(WebDriver driver, String testName, Throwable throwable) {
        String screenshotPath = captureScreenshot(driver, testName + "_FAILURE");
        
        if (screenshotPath != null) {
            logger.error("Test failed: {} - Screenshot saved: {}", testName, screenshotPath);
            logger.error("Failure reason: {}", throwable.getMessage());
        }
        
        return screenshotPath;
    }
}