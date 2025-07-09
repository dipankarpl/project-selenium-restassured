package com.yourorg.listeners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

public class LogListener implements IInvokedMethodListener {
    private static final Logger logger = LogManager.getLogger(LogListener.class);

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        String methodName = method.getTestMethod().getMethodName();
        String className = method.getTestMethod().getTestClass().getName();
        
        logger.info("═══════════════════════════════════════════════════════════");
        logger.info("STARTING: {}.{}", className, methodName);
        logger.info("═══════════════════════════════════════════════════════════");
        
        // Log test description if available
        if (method.getTestMethod().getDescription() != null) {
            logger.info("Test Description: {}", method.getTestMethod().getDescription());
        }
        
        // Log test groups if available
        String[] groups = method.getTestMethod().getGroups();
        if (groups.length > 0) {
            logger.info("Test Groups: {}", String.join(", ", groups));
        }
        
        // Log test priority if available
        int priority = method.getTestMethod().getPriority();
        if (priority != Integer.MAX_VALUE) {
            logger.info("Test Priority: {}", priority);
        }
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        String methodName = method.getTestMethod().getMethodName();
        String className = method.getTestMethod().getTestClass().getName();
        
        long executionTime = testResult.getEndMillis() - testResult.getStartMillis();
        
        String status = getTestStatus(testResult);
        
        logger.info("═══════════════════════════════════════════════════════════");
        logger.info("FINISHED: {}.{} - Status: {} - Duration: {}ms", 
                className, methodName, status, executionTime);
        
        if (testResult.getThrowable() != null) {
            logger.error("Test failed with exception: {}", testResult.getThrowable().getMessage());
        }
        
        logger.info("═══════════════════════════════════════════════════════════");
    }

    private String getTestStatus(ITestResult testResult) {
        switch (testResult.getStatus()) {
            case ITestResult.SUCCESS:
                return "PASSED";
            case ITestResult.FAILURE:
                return "FAILED";
            case ITestResult.SKIP:
                return "SKIPPED";
            case ITestResult.SUCCESS_PERCENTAGE_FAILURE:
                return "PERCENTAGE_FAILURE";
            default:
                return "UNKNOWN";
        }
    }
}