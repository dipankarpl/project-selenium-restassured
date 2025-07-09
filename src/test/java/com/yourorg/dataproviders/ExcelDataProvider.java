package com.yourorg.dataproviders;

import com.yourorg.abstracts.AbstractTestDataProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Excel-based test data provider using Apache POI
 */
public class ExcelDataProvider extends AbstractTestDataProvider {
    private static final Logger logger = LogManager.getLogger(ExcelDataProvider.class);
    private final String filePath;
    
    public ExcelDataProvider(String filePath) {
        this.filePath = filePath;
        logger.info("ExcelDataProvider initialized with file: {}", filePath);
    }
    
    @Override
    public Map<String, Object> getTestData(String testName) {
        // Check cache first
        Object cachedData = getCachedData(testName);
        if (cachedData instanceof Map) {
            return (Map<String, Object>) cachedData;
        }
        
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheet(testName);
            if (sheet == null) {
                logger.warn("Sheet '{}' not found in Excel file", testName);
                return new HashMap<>();
            }
            
            Map<String, Object> data = readSheetData(sheet);
            cacheData(testName, data);
            
            logger.info("Test data loaded from Excel for: {}", testName);
            return data;
            
        } catch (IOException e) {
            logger.error("Failed to load Excel data for {}: {}", testName, e.getMessage());
            return new HashMap<>();
        }
    }
    
    @Override
    public List<Map<String, Object>> getBulkTestData(String sheetName) {
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                logger.warn("Sheet '{}' not found in Excel file", sheetName);
                return new ArrayList<>();
            }
            
            List<Map<String, Object>> dataList = readSheetAsTable(sheet);
            logger.info("Bulk test data loaded from Excel for: {} ({} records)", sheetName, dataList.size());
            return dataList;
            
        } catch (IOException e) {
            logger.error("Failed to load bulk Excel data for {}: {}", sheetName, e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public void saveTestData(String testName, Map<String, Object> data) {
        // Excel writing implementation would go here
        logger.warn("Excel writing not implemented in this version");
    }
    
    @Override
    protected boolean checkDataSource(String testName) {
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            return workbook.getSheet(testName) != null;
            
        } catch (IOException e) {
            logger.error("Failed to check Excel data source: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Read sheet data as key-value pairs (first row as keys, second row as values)
     */
    private Map<String, Object> readSheetData(Sheet sheet) {
        Map<String, Object> data = new HashMap<>();
        
        if (sheet.getPhysicalNumberOfRows() < 2) {
            return data;
        }
        
        Row headerRow = sheet.getRow(0);
        Row dataRow = sheet.getRow(1);
        
        if (headerRow == null || dataRow == null) {
            return data;
        }
        
        for (int i = 0; i < headerRow.getPhysicalNumberOfCells(); i++) {
            Cell headerCell = headerRow.getCell(i);
            Cell dataCell = dataRow.getCell(i);
            
            if (headerCell != null && dataCell != null) {
                String key = getCellValueAsString(headerCell);
                Object value = getCellValue(dataCell);
                data.put(key, value);
            }
        }
        
        return data;
    }
    
    /**
     * Read sheet data as table (first row as headers, subsequent rows as data)
     */
    private List<Map<String, Object>> readSheetAsTable(Sheet sheet) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        
        if (sheet.getPhysicalNumberOfRows() < 2) {
            return dataList;
        }
        
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            return dataList;
        }
        
        // Get headers
        List<String> headers = new ArrayList<>();
        for (int i = 0; i < headerRow.getPhysicalNumberOfCells(); i++) {
            Cell cell = headerRow.getCell(i);
            headers.add(cell != null ? getCellValueAsString(cell) : "Column" + i);
        }
        
        // Read data rows
        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) continue;
            
            Map<String, Object> rowData = new HashMap<>();
            for (int colIndex = 0; colIndex < headers.size(); colIndex++) {
                Cell cell = row.getCell(colIndex);
                Object value = cell != null ? getCellValue(cell) : null;
                rowData.put(headers.get(colIndex), value);
            }
            dataList.add(rowData);
        }
        
        return dataList;
    }
    
    /**
     * Get cell value as appropriate Java type
     */
    private Object getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    double numericValue = cell.getNumericCellValue();
                    // Return as integer if it's a whole number
                    if (numericValue == Math.floor(numericValue)) {
                        return (int) numericValue;
                    }
                    return numericValue;
                }
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                return cell.getCellFormula();
            default:
                return cell.toString();
        }
    }
    
    /**
     * Get cell value as string
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == Math.floor(numericValue)) {
                        return String.valueOf((int) numericValue);
                    }
                    return String.valueOf(numericValue);
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return cell.toString();
        }
    }
    
    /**
     * Get all sheet names from the Excel file
     */
    public List<String> getSheetNames() {
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            List<String> sheetNames = new ArrayList<>();
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                sheetNames.add(workbook.getSheetName(i));
            }
            return sheetNames;
            
        } catch (IOException e) {
            logger.error("Failed to get sheet names: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}