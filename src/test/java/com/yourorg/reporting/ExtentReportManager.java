package com.yourorg.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.yourorg.interfaces.IReportManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestResult;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

/**
 * Extent Report Manager implementing IReportManager interface
 * Demonstrates interface implementation and composition
 */
public class ExtentReportManager implements IReportManager {
    private static final Logger logger = LogManager.getLogger(ExtentReportManager.class);
    private ExtentReports extent;
    private final ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();
    private final String reportPath;
    
    public ExtentReportManager(String reportPath) {
        this.reportPath = reportPath != null ? reportPath : generateDefaultReportPath();
    }
    
    private String generateDefaultReportPath() {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        return "reports/extent-report/ExtentReport_" + timestamp + ".html";
    }
    
    @Override
    public void initializeReport() {
        try {
            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
            configureSparkReporter(sparkReporter);
            
            extent = new ExtentReports();
            extent.attachReporter(sparkReporter);
            addSystemInformation();
            
            logger.info("Extent Reports initialized: {}", reportPath);
        } catch (Exception e) {
            logger.error("Failed to initialize Extent Reports: {}", e.getMessage());
            throw new RuntimeException("Report initialization failed", e);
        }
    }
    
    private void configureSparkReporter(ExtentSparkReporter sparkReporter) {
        sparkReporter.config().setDocumentTitle("Test Automation Report");
        sparkReporter.config().setReportName("Automated Test Results");
        sparkReporter.config().setTheme(Theme.STANDARD);
        sparkReporter.config().setTimeStampFormat("EEEE, MMMM dd, yyyy, hh:mm a '('zzz')'");
    }
    
    private void addSystemInformation() {
        extent.setSystemInfo("Operating System", System.getProperty("os.name"));
        extent.setSystemInfo("Java Version", System.getProperty("java.version"));
        extent.setSystemInfo("User Name", System.getProperty("user.name"));
        extent.setSystemInfo("Time Zone", System.getProperty("user.timezone"));
        extent.setSystemInfo("Environment", System.getProperty("environment", "QA"));
    }
    
    @Override
    public void createTest(String testName, String description) {
        ExtentTest test = extent.createTest(testName, description);
        extentTest.set(test);
        logger.debug("Test created in report: {}", testName);
    }
    
    @Override
    public void logInfo(String message) {
        Optional.ofNullable(extentTest.get())
                .ifPresent(test -> test.log(Status.INFO, message));
    }
    
    @Override
    public void logPass(String message) {
        Optional.ofNullable(extentTest.get())
                .ifPresent(test -> test.log(Status.PASS, message));
    }
    
    @Override
    public void logFail(String message, Throwable throwable) {
        Optional.ofNullable(extentTest.get())
                .ifPresent(test -> {
                    test.log(Status.FAIL, message);
                    if (throwable != null) {
                        test.log(Status.FAIL, throwable);
                    }
                });
    }
    
    @Override
    public void logSkip(String message) {
        Optional.ofNullable(extentTest.get())
                .ifPresent(test -> test.log(Status.SKIP, message));
    }
    
    @Override
    public void attachScreenshot(String screenshotPath) {
        Optional.ofNullable(extentTest.get())
                .ifPresent(test -> {
                    try {
                        test.addScreenCaptureFromPath(screenshotPath);
                        logger.debug("Screenshot attached to report: {}", screenshotPath);
                    } catch (Exception e) {
                        logger.warn("Failed to attach screenshot: {}", e.getMessage());
                    }
                });
    }
    
    @Override
    public void finalizeReport() {
        if (extent != null) {
            extent.flush();
            logger.info("Extent Reports finalized");
        }
    }
    
    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String description = Optional.ofNullable(result.getMethod().getDescription())
                .orElse("Test execution");
        
        createTest(testName, description);
        
        // Add test categories/groups
        String[] groups = result.getMethod().getGroups();
        if (groups.length > 0) {
            ExtentTest test = extentTest.get();
            for (String group : groups) {
                test.assignCategory(group);
            }
        }
        
        logInfo("Test started: " + testName);
    }
    
    @Override
    public void onTestSuccess(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        long executionTime = result.getEndMillis() - result.getStartMillis();
        
        logPass("Test passed successfully");
        logInfo("Execution Time: " + executionTime + "ms");
        
        logger.info("Test passed: {}", testName);
    }
    
    @Override
    public void onTestFailure(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        Throwable throwable = result.getThrowable();
        long executionTime = result.getEndMillis() - result.getStartMillis();
        
        logFail("Test failed", throwable);
        logInfo("Execution Time: " + executionTime + "ms");
        
        logger.error("Test failed: {}", testName);
    }
    
    @Override
    public void onTestSkipped(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        Throwable throwable = result.getThrowable();
        
        logSkip("Test skipped");
        if (throwable != null) {
            logInfo("Skip reason: " + throwable.getMessage());
        }
        
        logger.warn("Test skipped: {}", testName);
    }
    
    public ExtentTest getCurrentTest() {
        return extentTest.get();
    }
    
    public void removeTest() {
        extentTest.remove();
    }
}