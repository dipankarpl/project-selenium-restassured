package com.yourorg.interfaces;

import java.util.List;
import java.util.Map;

/**
 * Interface for test data providers
 * Supports multiple data sources (JSON, Excel, Database, API)
 */
public interface ITestDataProvider {
    Map<String, Object> getTestData(String testName);
    List<Map<String, Object>> getBulkTestData(String dataSet);
    void saveTestData(String testName, Map<String, Object> data);
    boolean isDataAvailable(String testName);
}