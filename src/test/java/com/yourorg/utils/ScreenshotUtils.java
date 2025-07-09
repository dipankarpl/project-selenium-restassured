package com.yourorg.utils;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class for capturing and managing screenshots
 */
public class ScreenshotUtils {
    private static final Logger logger = LogManager.getLogger(ScreenshotUtils.class);
    private static final String SCREENSHOT_DIR = "reports/screenshots/";
    
    static {
        // Create screenshots directory if it doesn't exist
        File screenshotDir = new File(SCREENSHOT_DIR);
        if (!screenshotDir.exists()) {
            screenshotDir.mkdirs();
        }
    }
    
    /**
     * Capture screenshot and return the file path
     */
    public static String captureScreenshot(WebDriver driver, String testName) {
        if (driver == null) {
            logger.warn("Driver is null, cannot capture screenshot");
            return null;
        }
        
        try {
            TakesScreenshot screenshot = (TakesScreenshot) driver;
            File sourceFile = screenshot.getScreenshotAs(OutputType.FILE);
            
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS").format(new Date());
            String fileName = testName + "_" + timestamp + ".png";
            String filePath = SCREENSHOT_DIR + fileName;
            
            File destFile = new File(filePath);
            FileUtils.copyFile(sourceFile, destFile);
            
            logger.info("Screenshot captured: {}", filePath);
            return filePath;
            
        } catch (IOException e) {
            logger.error("Failed to capture screenshot: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Error during screenshot capture: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Capture screenshot on test failure
     */
    public static String captureFailureScreenshot(WebDriver driver, String testName, Throwable throwable) {
        String screenshotPath = captureScreenshot(driver, testName + "_FAILURE");
        
        if (screenshotPath != null) {
            logger.error("Test failed: {} - Screenshot saved: {}", testName, screenshotPath);
            logger.error("Failure reason: {}", throwable.getMessage());
        }
        
        return screenshotPath;
    }
    
    /**
     * Capture screenshot with custom message
     */
    public static String captureScreenshotWithMessage(WebDriver driver, String testName, String message) {
        String screenshotPath = captureScreenshot(driver, testName + "_" + message.replaceAll("[^a-zA-Z0-9]", "_"));
        
        if (screenshotPath != null) {
            logger.info("Screenshot captured for {}: {} - Path: {}", testName, message, screenshotPath);
        }
        
        return screenshotPath;
    }
    
    /**
     * Clean up old screenshots (older than specified days)
     */
    public static void cleanupOldScreenshots(int daysOld) {
        File screenshotDir = new File(SCREENSHOT_DIR);
        
        if (!screenshotDir.exists()) {
            return;
        }
        
        long cutoffTime = System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000L);
        
        File[] files = screenshotDir.listFiles();
        if (files != null) {
            int deletedCount = 0;
            for (File file : files) {
                if (file.isFile() && file.lastModified() < cutoffTime) {
                    if (file.delete()) {
                        deletedCount++;
                    }
                }
            }
            logger.info("Cleaned up {} old screenshots", deletedCount);
        }
    }
    
    /**
     * Get screenshot directory path
     */
    public static String getScreenshotDirectory() {
        return SCREENSHOT_DIR;
    }
}