package com.yourorg.dataproviders;

import com.yourorg.interfaces.ITestDataProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.DataProvider;

import java.util.List;
import java.util.Map;

/**
 * TestNG DataProvider implementations for different data sources
 */
public class TestNGDataProviders {
    private static final Logger logger = LogManager.getLogger(TestNGDataProviders.class);
    
    @DataProvider(name = "jsonDataProvider")
    public static Object[][] jsonDataProvider() {
        try {
            String dataDirectory = "src/test/resources/testdata";
            JsonTestDataProvider provider = new JsonTestDataProvider(dataDirectory);
            
            // Get all available test data
            List<String> availableTests = provider.getAvailableTestData();
            Object[][] data = new Object[availableTests.size()][];
            
            for (int i = 0; i < availableTests.size(); i++) {
                String testName = availableTests.get(i);
                Map<String, Object> testData = provider.getTestData(testName);
                data[i] = new Object[]{testName, testData};
            }
            
            logger.info("JSON DataProvider loaded {} test cases", data.length);
            return data;
            
        } catch (Exception e) {
            logger.error("Failed to load JSON data provider: {}", e.getMessage());
            return new Object[0][0];
        }
    }
    
    @DataProvider(name = "excelDataProvider")
    public static Object[][] excelDataProvider() {
        try {
            String filePath = "src/test/resources/testdata/testdata.xlsx";
            ExcelDataProvider provider = new ExcelDataProvider(filePath);
            
            // Get data from first sheet
            List<Map<String, Object>> bulkData = provider.getBulkTestData("TestData");
            Object[][] data = new Object[bulkData.size()][];
            
            for (int i = 0; i < bulkData.size(); i++) {
                data[i] = new Object[]{bulkData.get(i)};
            }
            
            logger.info("Excel DataProvider loaded {} test cases", data.length);
            return data;
            
        } catch (Exception e) {
            logger.error("Failed to load Excel data provider: {}", e.getMessage());
            return new Object[0][0];
        }
    }
    
    @DataProvider(name = "csvDataProvider")
    public static Object[][] csvDataProvider() {
        try {
            String filePath = "src/test/resources/testdata/testdata.csv";
            CsvDataProvider provider = new CsvDataProvider(filePath);
            
            return provider.getDataForTestNG();
            
        } catch (Exception e) {
            logger.error("Failed to load CSV data provider: {}", e.getMessage());
            return new Object[0][0];
        }
    }
    
    @DataProvider(name = "loginDataProvider")
    public static Object[][] loginDataProvider() {
        try {
            String filePath = "src/test/resources/testdata/login_data.csv";
            CsvDataProvider provider = new CsvDataProvider(filePath);
            
            return provider.getColumnsForTestNG("username", "password", "expectedResult");
            
        } catch (Exception e) {
            logger.error("Failed to load login data provider: {}", e.getMessage());
            return new Object[][]{
                {"testuser", "testpass", "success"},
                {"invaliduser", "invalidpass", "failure"}
            };
        }
    }
    
    @DataProvider(name = "userRegistrationDataProvider")
    public static Object[][] userRegistrationDataProvider() {
        try {
            String filePath = "src/test/resources/testdata/registration_data.xlsx";
            ExcelDataProvider provider = new ExcelDataProvider(filePath);
            
            List<Map<String, Object>> registrationData = provider.getBulkTestData("RegistrationData");
            Object[][] data = new Object[registrationData.size()][];
            
            for (int i = 0; i < registrationData.size(); i++) {
                Map<String, Object> row = registrationData.get(i);
                data[i] = new Object[]{
                    row.get("firstName"),
                    row.get("lastName"),
                    row.get("email"),
                    row.get("password"),
                    row.get("expectedResult")
                };
            }
            
            return data;
            
        } catch (Exception e) {
            logger.error("Failed to load registration data provider: {}", e.getMessage());
            return new Object[0][0];
        }
    }
    
    @DataProvider(name = "apiTestDataProvider")
    public static Object[][] apiTestDataProvider() {
        try {
            String dataDirectory = "src/test/resources/testdata/api";
            JsonTestDataProvider provider = new JsonTestDataProvider(dataDirectory);
            
            List<Map<String, Object>> apiTestData = provider.getBulkTestData("api_tests");
            Object[][] data = new Object[apiTestData.size()][];
            
            for (int i = 0; i < apiTestData.size(); i++) {
                data[i] = new Object[]{apiTestData.get(i)};
            }
            
            return data;
            
        } catch (Exception e) {
            logger.error("Failed to load API test data provider: {}", e.getMessage());
            return new Object[0][0];
        }
    }
    
    @DataProvider(name = "searchDataProvider")
    public static Object[][] searchDataProvider() {
        return new Object[][]{
            {"laptop", "electronics", true},
            {"phone", "electronics", true},
            {"book", "books", true},
            {"invaliditem", "none", false},
            {"", "none", false}
        };
    }
    
    @DataProvider(name = "dynamicDataProvider")
    public static Object[][] dynamicDataProvider() {
        // This can be configured to load from different sources based on environment
        String environment = System.getProperty("environment", "qa");
        String dataSource = System.getProperty("dataSource", "json");
        
        try {
            ITestDataProvider provider;
            
            switch (dataSource.toLowerCase()) {
                case "excel":
                    provider = new ExcelDataProvider("src/test/resources/testdata/" + environment + "_data.xlsx");
                    break;
                case "csv":
                    provider = new CsvDataProvider("src/test/resources/testdata/" + environment + "_data.csv");
                    break;
                default:
                    provider = new JsonTestDataProvider("src/test/resources/testdata/" + environment);
                    break;
            }
            
            List<Map<String, Object>> dynamicData = provider.getBulkTestData("dynamic_tests");
            Object[][] data = new Object[dynamicData.size()][];
            
            for (int i = 0; i < dynamicData.size(); i++) {
                data[i] = new Object[]{dynamicData.get(i)};
            }
            
            logger.info("Dynamic DataProvider loaded {} test cases from {} source", data.length, dataSource);
            return data;
            
        } catch (Exception e) {
            logger.error("Failed to load dynamic data provider: {}", e.getMessage());
            return new Object[0][0];
        }
    }
}