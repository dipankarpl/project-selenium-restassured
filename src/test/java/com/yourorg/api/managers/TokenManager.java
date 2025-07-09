package com.yourorg.api.managers;

import com.yourorg.utils.ConfigLoader;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe token manager with automatic refresh capability
 * Demonstrates singleton pattern with thread safety
 */
public class TokenManager {
    private static final Logger logger = LogManager.getLogger(TokenManager.class);
    private static volatile TokenManager instance;
    private static final ReentrantLock lock = new ReentrantLock();
    
    private final ConcurrentHashMap<String, TokenInfo> tokens = new ConcurrentHashMap<>();
    
    private TokenManager() {}
    
    public static TokenManager getInstance() {
        if (instance == null) {
            lock.lock();
            try {
                if (instance == null) {
                    instance = new TokenManager();
                }
            } finally {
                lock.unlock();
            }
        }
        return instance;
    }
    
    public String getValidToken(String tokenType) throws Exception {
        TokenInfo tokenInfo = tokens.get(tokenType);
        
        if (tokenInfo == null || isTokenExpired(tokenInfo)) {
            refreshToken(tokenType);
            tokenInfo = tokens.get(tokenType);
        }
        
        return tokenInfo != null ? tokenInfo.token : null;
    }
    
    public void refreshToken(String tokenType) throws Exception {
        lock.lock();
        try {
            logger.info("Refreshing token for type: {}", tokenType);
            
            String newToken;
            switch (tokenType.toLowerCase()) {
                case "admin":
                    newToken = authenticateAdmin();
                    break;
                case "user":
                    newToken = authenticateUser();
                    break;
                case "api":
                    newToken = authenticateApiKey();
                    break;
                default:
                    throw new Exception("Unknown token type: " + tokenType);
            }
            
            TokenInfo tokenInfo = new TokenInfo(newToken, LocalDateTime.now().plusHours(1));
            tokens.put(tokenType, tokenInfo);
            
            logger.info("Token refreshed successfully for type: {}", tokenType);
        } finally {
            lock.unlock();
        }
    }
    
    private boolean isTokenExpired(TokenInfo tokenInfo) {
        return LocalDateTime.now().isAfter(tokenInfo.expiryTime.minus(5, ChronoUnit.MINUTES));
    }
    
    private String authenticateAdmin() throws Exception {
        // Implementation for admin authentication
        String username = ConfigLoader.get("admin.username", "admin");
        String password = ConfigLoader.get("admin.password", "admin123");
        
        Response response = io.restassured.RestAssured.given()
                .contentType("application/json")
                .body(java.util.Map.of("username", username, "password", password))
                .post("/auth/admin/login");
        
        if (response.getStatusCode() == 200) {
            return response.jsonPath().getString("token");
        } else {
            throw new Exception("Admin authentication failed: " + response.getStatusCode());
        }
    }
    
    private String authenticateUser() throws Exception {
        // Implementation for user authentication
        String username = ConfigLoader.get("test.username", "testuser");
        String password = ConfigLoader.get("test.password", "testpass");
        
        Response response = io.restassured.RestAssured.given()
                .contentType("application/json")
                .body(java.util.Map.of("username", username, "password", password))
                .post("/auth/login");
        
        if (response.getStatusCode() == 200) {
            return response.jsonPath().getString("token");
        } else {
            throw new Exception("User authentication failed: " + response.getStatusCode());
        }
    }
    
    private String authenticateApiKey() throws Exception {
        // Implementation for API key authentication
        String apiKey = ConfigLoader.get("api.key", "test-api-key");
        
        Response response = io.restassured.RestAssured.given()
                .header("X-API-Key", apiKey)
                .post("/auth/validate");
        
        if (response.getStatusCode() == 200) {
            return response.jsonPath().getString("token");
        } else {
            throw new Exception("API key authentication failed: " + response.getStatusCode());
        }
    }
    
    public void invalidateToken(String tokenType) {
        tokens.remove(tokenType);
        logger.info("Token invalidated for type: {}", tokenType);
    }
    
    public void invalidateAllTokens() {
        tokens.clear();
        logger.info("All tokens invalidated");
    }
    
    private static class TokenInfo {
        final String token;
        final LocalDateTime expiryTime;
        
        TokenInfo(String token, LocalDateTime expiryTime) {
            this.token = token;
            this.expiryTime = expiryTime;
        }
    }
}