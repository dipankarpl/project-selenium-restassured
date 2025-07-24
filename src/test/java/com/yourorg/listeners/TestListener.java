package com.yourorg.listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.yourorg.base.WebDriverFactory;
import com.yourorg.utils.ScreenshotUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.ITestListener;
import org.testng.ITestResult;

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
        sparkReporter.config().setReportName("Test Results");
        
        extent = new ExtentReports();
        extent.attachReporter(sparkReporter);
        
        // Add system info
        extent.setSystemInfo("OS", System.getProperty("os.name"));
        extent.setSystemInfo("Java Version", System.getProperty("java.version"));
        extent.setSystemInfo("Environment", System.getProperty("environment", "qa"));
        
        logger.info("Extent Reports initialized: {}", reportPath);
    }

    @Override
    public void onTestStart(ITestResult result) {
        logger.info("Test started: {}", result.getMethod().getMethodName());
        
        ExtentTest test = extent.createTest(result.getMethod().getMethodName());
        extentTest.set(test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        logger.info("Test passed: {}", result.getMethod().getMethodName());
        
        ExtentTest test = extentTest.get();
        test.log(Status.PASS, "Test passed successfully");
        
        long executionTime = result.getEndMillis() - result.getStartMillis();
        test.info("Execution Time: " + executionTime + "ms");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        logger.error("Test failed: {}", result.getMethod().getMethodName());
        
        ExtentTest test = extentTest.get();
        test.log(Status.FAIL, "Test failed");
        test.log(Status.FAIL, result.getThrowable());
        
        // Take screenshot on failure
        String screenshotPath = captureScreenshotOnFailure(result);
        if (screenshotPath != null) {
            test.addScreenCaptureFromPath(screenshotPath);
        }
        
        long executionTime = result.getEndMillis() - result.getStartMillis();
        test.info("Execution Time: " + executionTime + "ms");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        logger.warn("Test skipped: {}", result.getMethod().getMethodName());
        
        ExtentTest test = extentTest.get();
        test.log(Status.SKIP, "Test skipped");
        if (result.getThrowable() != null) {
            test.log(Status.SKIP, result.getThrowable());
        }
    }

    @Override
    public void onFinish(org.testng.ITestContext context) {
        logger.info("Test suite finished: {}", context.getName());
        
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
    }

    private String captureScreenshotOnFailure(ITestResult result) {
        try {
            WebDriver driver = WebDriverFactory.getDriver();
            if (driver != null) {
                String testName = result.getMethod().getMethodName();
                return ScreenshotUtils.captureFailureScreenshot(driver, testName, result.getThrowable());
            }
        } catch (Exception e) {
            logger.error("Error during screenshot capture: {}", e.getMessage());
        }
        return null;
    }
}