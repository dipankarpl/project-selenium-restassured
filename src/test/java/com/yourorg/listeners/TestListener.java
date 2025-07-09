package com.yourorg.listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.yourorg.base.RemoteWebDriverFactory;
import com.yourorg.utils.StateRetentionManager;
import com.yourorg.utils.ScreenshotUtils;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TestListener implements ITestListener {
    private static final Logger logger = LogManager.getLogger(TestListener.class);
    private static ExtentReports extent;
    private static ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();

    @Override
    public void onStart(org.testng.ITestContext context) {
        logger.info("Test suite started: {}", context.getName());
        
        // Initialize Extent Reports
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        String reportPath = "reports/extent-report/ExtentReport_" + timestamp + ".html";
        
        ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
        sparkReporter.config().setDocumentTitle("Test Automation Report");
        sparkReporter.config().setReportName("Automated Test Results");
        sparkReporter.config().setTheme(Theme.STANDARD);
        
        extent = new ExtentReports();
        extent.attachReporter(sparkReporter);
        
        // Add system information
        extent.setSystemInfo("Operating System", System.getProperty("os.name"));
        extent.setSystemInfo("Java Version", System.getProperty("java.version"));
        extent.setSystemInfo("User Name", System.getProperty("user.name"));
        extent.setSystemInfo("Time Zone", System.getProperty("user.timezone"));
        
        logger.info("Extent Reports initialized: {}", reportPath);
    }

    @Override
    public void onTestStart(ITestResult result) {
        logger.info("Test started: {}.{}", result.getTestClass().getName(), result.getMethod().getMethodName());
        
        // Create test in Extent Reports
        ExtentTest test = extent.createTest(result.getMethod().getMethodName());
        test.assignCategory(result.getTestClass().getName());
        extentTest.set(test);
        
        // Log test parameters if any
        Object[] parameters = result.getParameters();
        if (parameters.length > 0) {
            StringBuilder params = new StringBuilder();
            for (Object param : parameters) {
                params.append(param.toString()).append(", ");
            }
            test.info("Test Parameters: " + params.toString());
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        logger.info("Test passed: {}.{}", result.getTestClass().getName(), result.getMethod().getMethodName());
        
        ExtentTest test = extentTest.get();
        test.log(Status.PASS, "Test passed successfully");
        
        // Log execution time
        long executionTime = result.getEndMillis() - result.getStartMillis();
        test.info("Execution Time: " + executionTime + "ms");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        logger.error("Test failed: {}.{}", result.getTestClass().getName(), result.getMethod().getMethodName());
        
        ExtentTest test = extentTest.get();
        test.log(Status.FAIL, "Test failed");
        test.log(Status.FAIL, result.getThrowable());
        
        // Take screenshot on failure
        String screenshotPath = captureScreenshotOnFailure(result);
        if (screenshotPath != null) {
            test.addScreenCaptureFromPath(screenshotPath);
        }
        
        // Log execution time
        long executionTime = result.getEndMillis() - result.getStartMillis();
        test.info("Execution Time: " + executionTime + "ms");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        logger.warn("Test skipped: {}.{}", result.getTestClass().getName(), result.getMethod().getMethodName());
        
        ExtentTest test = extentTest.get();
        test.log(Status.SKIP, "Test skipped");
        test.log(Status.SKIP, result.getThrowable());
    }

    @Override
    public void onFinish(org.testng.ITestContext context) {
        logger.info("Test suite finished: {}", context.getName());
        
        // Flush extent reports
        if (extent != null) {
            extent.flush();
        }
        
        // Log test summary
        int totalTests = context.getAllTestMethods().length;
        int passedTests = context.getPassedTests().size();
        int failedTests = context.getFailedTests().size();
        int skippedTests = context.getSkippedTests().size();
        
        logger.info("Test Summary - Total: {}, Passed: {}, Failed: {}, Skipped: {}", 
                totalTests, passedTests, failedTests, skippedTests);
        
        // Log final state
        StateRetentionManager.logCurrentState();
    }

    private String captureScreenshotOnFailure(ITestResult result) {
        try {
            WebDriver driver = RemoteWebDriverFactory.getDriver();
            if (driver != null) {
                String testName = result.getMethod().getMethodName();
                return ScreenshotUtils.captureFailureScreenshot(driver, testName, result.getThrowable());
            }
        } catch (Exception e) {
            logger.error("Error during screenshot capture: {}", e.getMessage());
        }
        return null;
    }

    public static ExtentTest getExtentTest() {
        return extentTest.get();
    }
}