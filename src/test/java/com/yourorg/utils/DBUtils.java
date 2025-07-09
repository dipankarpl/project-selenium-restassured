package com.yourorg.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBUtils {
    private static final Logger logger = LogManager.getLogger(DBUtils.class);
    private static Connection connection;

    public static void initializeDB() {
        String dbUrl = ConfigLoader.get("db.url");
        String dbUsername = ConfigLoader.get("db.username");
        String dbPassword = ConfigLoader.get("db.password");
        String dbDriver = ConfigLoader.get("db.driver");

        if (dbUrl == null || dbUsername == null || dbPassword == null) {
            logger.warn("Database configuration not found. Skipping DB initialization.");
            return;
        }

        try {
            Class.forName(dbDriver);
            connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
            logger.info("Database connection established successfully");
        } catch (ClassNotFoundException | SQLException e) {
            logger.error("Failed to establish database connection: {}", e.getMessage());
            throw new RuntimeException("Database connection failed", e);
        }
    }

    public static List<Map<String, Object>> executeQuery(String query) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        if (connection == null) {
            logger.warn("Database connection not initialized. Returning empty results.");
            return results;
        }

        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            while (resultSet.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = resultSet.getObject(i);
                    row.put(columnName, value);
                }
                results.add(row);
            }
            
            logger.info("Query executed successfully. Retrieved {} rows", results.size());
        } catch (SQLException e) {
            logger.error("Error executing query: {}", e.getMessage());
            throw new RuntimeException("Query execution failed", e);
        }
        
        return results;
    }

    public static int executeUpdate(String query) {
        if (connection == null) {
            logger.warn("Database connection not initialized. Cannot execute update.");
            return 0;
        }

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            int rowsAffected = statement.executeUpdate();
            logger.info("Update executed successfully. Rows affected: {}", rowsAffected);
            return rowsAffected;
        } catch (SQLException e) {
            logger.error("Error executing update: {}", e.getMessage());
            throw new RuntimeException("Update execution failed", e);
        }
    }

    public static Map<String, Object> getTestData(String testName) {
        String query = "SELECT * FROM test_data WHERE test_name = '" + testName + "'";
        List<Map<String, Object>> results = executeQuery(query);
        
        if (results.isEmpty()) {
            logger.warn("No test data found for test: {}", testName);
            return new HashMap<>();
        }
        
        return results.get(0);
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                logger.info("Database connection closed successfully");
            } catch (SQLException e) {
                logger.error("Error closing database connection: {}", e.getMessage());
            }
        }
    }
}