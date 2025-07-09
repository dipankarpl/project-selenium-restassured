package com.yourorg.dataproviders;

import com.yourorg.interfaces.ITestDataProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * Factory class to create appropriate data provider based on file type
 */
public class TestDataProviderFactory {
    private static final Logger logger = LogManager.getLogger(TestDataProviderFactory.class);
    
    /**
     * Create data provider based on file extension
     */
    public static ITestDataProvider createDataProvider(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }
        
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist: " + filePath);
        }
        
        String extension = getFileExtension(filePath).toLowerCase();
        
        switch (extension) {
            case "json":
                logger.info("Creating JSON data provider for: {}", filePath);
                return new JsonTestDataProvider(file.getParent());
                
            case "xlsx":
            case "xls":
                logger.info("Creating Excel data provider for: {}", filePath);
                return new ExcelDataProvider(filePath);
                
            case "csv":
                logger.info("Creating CSV data provider for: {}", filePath);
                return new CsvDataProvider(filePath);
                
            default:
                throw new IllegalArgumentException("Unsupported file type: " + extension);
        }
    }
    
    /**
     * Create data provider with explicit type
     */
    public static ITestDataProvider createDataProvider(String filePath, DataProviderType type) {
        switch (type) {
            case JSON:
                return new JsonTestDataProvider(new File(filePath).getParent());
            case EXCEL:
                return new ExcelDataProvider(filePath);
            case CSV:
                return new CsvDataProvider(filePath);
            default:
                throw new IllegalArgumentException("Unsupported data provider type: " + type);
        }
    }
    
    /**
     * Get file extension from file path
     */
    private static String getFileExtension(String filePath) {
        int lastDotIndex = filePath.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filePath.substring(lastDotIndex + 1);
    }
    
    /**
     * Enum for data provider types
     */
    public enum DataProviderType {
        JSON, EXCEL, CSV
    }
    
    /**
     * Create multiple data providers for different test suites
     */
    public static ITestDataProvider[] createMultipleDataProviders(String... filePaths) {
        ITestDataProvider[] providers = new ITestDataProvider[filePaths.length];
        
        for (int i = 0; i < filePaths.length; i++) {
            providers[i] = createDataProvider(filePaths[i]);
        }
        
        return providers;
    }
    
    /**
     * Check if file type is supported
     */
    public static boolean isFileTypeSupported(String filePath) {
        String extension = getFileExtension(filePath).toLowerCase();
        return extension.equals("json") || extension.equals("xlsx") || 
               extension.equals("xls") || extension.equals("csv");
    }
}