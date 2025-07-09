package com.yourorg.interfaces;

import org.testng.ITestResult;

/**
 * Interface for report management
 * Supports multiple reporting formats
 */
public interface IReportManager {
    void initializeReport();
    void createTest(String testName, String description);
    void logInfo(String message);
    void logPass(String message);
    void logFail(String message, Throwable throwable);
    void logSkip(String message);
    void attachScreenshot(String screenshotPath);
    void finalizeReport();
    void onTestStart(ITestResult result);
    void onTestSuccess(ITestResult result);
    void onTestFailure(ITestResult result);
    void onTestSkipped(ITestResult result);
}