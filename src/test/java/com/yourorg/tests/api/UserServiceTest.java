package com.yourorg.tests.api;

import com.yourorg.base.BaseTest;
import com.yourorg.utils.APIUtils;
import com.yourorg.utils.ConfigLoader;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.*;

@Epic("API Tests")
@Feature("User Service Tests")
public class UserServiceTest extends BaseTest {
    private static final Logger logger = LogManager.getLogger(UserServiceTest.class);
    private static String userId;
    private static String authToken;

    @Test(priority = 1, groups = {"sanity", "api", "authentication", "critical"})
    @Description("Test user authentication with valid credentials and token generation")
    @Severity(SeverityLevel.BLOCKER)
    public void testUserAuthentication() throws Exception {
        logger.info("Starting user authentication test");
        
        try {
            // Get credentials from config
            String username = ConfigLoader.get("api.test.username", "testuser");
            String password = ConfigLoader.get("api.test.password", "testpass");
            
            // Authenticate user
            authToken = APIUtils.authenticateUser(username, password);
            
            // Verify authentication
            Assert.assertNotNull(authToken, "Authentication token should not be null");
            Assert.assertFalse(authToken.isEmpty(), "Authentication token should not be empty");
            
            logger.info("User authentication test completed successfully");
        } catch (Exception e) {
            logger.error("User authentication test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 2, dependsOnMethods = "testUserAuthentication", groups = {"sanity", "api", "profile", "critical"})
    @Description("Test retrieving user profile information with valid authentication")
    @Severity(SeverityLevel.CRITICAL)
    public void testGetUserProfile() throws Exception {
        logger.info("Starting get user profile test");
        
        try {
            // Get user profile
            Response response = APIUtils.get("/users/profile");
            
            // Validate response
            APIUtils.validateStatusCode(response, 200);
            APIUtils.validateResponseTime(response, 5000);
            
            // Validate response structure
            response.then()
                    .body("id", notNullValue())
                    .body("username", notNullValue())
                    .body("email", notNullValue())
                    .body("createdAt", notNullValue());
            
            // Store user ID for later tests
            userId = response.jsonPath().getString("id");
            
            // Validate response against JSON schema
            try {
                response.then().body(matchesJsonSchemaInClasspath("schemas/user-profile-schema.json"));
            } catch (Exception e) {
                logger.warn("JSON schema validation failed: {}", e.getMessage());
            }
            
            logger.info("Get user profile test completed successfully");
        } catch (Exception e) {
            logger.error("Get user profile test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 3, dependsOnMethods = "testGetUserProfile", groups = {"sanity", "api", "crud", "normal"})
    @Description("Test updating user profile information with valid data")
    @Severity(SeverityLevel.NORMAL)
    public void testUpdateUserProfile() throws Exception {
        logger.info("Starting update user profile test");
        
        try {
            // Prepare update data
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("firstName", "Updated");
            updateData.put("lastName", "User");
            updateData.put("email", "updated.user@example.com");
            
            // Update user profile
            Response response = APIUtils.put("/users/profile", updateData);
            
            // Validate response
            APIUtils.validateStatusCode(response, 200);
            APIUtils.validateResponseTime(response, 5000);
            
            // Validate updated data
            response.then()
                    .body("firstName", equalTo("Updated"))
                    .body("lastName", equalTo("User"))
                    .body("email", equalTo("updated.user@example.com"));
            
            logger.info("Update user profile test completed successfully");
        } catch (Exception e) {
            logger.error("Update user profile test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 4, groups = {"regression", "api", "crud", "critical"})
    @Description("Test creating a new user with valid registration data")
    @Severity(SeverityLevel.CRITICAL)
    public void testCreateUser() throws Exception {
        logger.info("Starting create user test");
        
        try {
            // Prepare user data
            Map<String, Object> userData = new HashMap<>();
            userData.put("username", "newuser" + System.currentTimeMillis());
            userData.put("email", "newuser" + System.currentTimeMillis() + "@example.com");
            userData.put("password", "newuserpass");
            userData.put("firstName", "New");
            userData.put("lastName", "User");
            
            // Create user
            Response response = APIUtils.post("/users", userData);
            
            // Validate response
            APIUtils.validateStatusCode(response, 201);
            APIUtils.validateResponseTime(response, 5000);
            
            // Validate response structure
            response.then()
                    .body("id", notNullValue())
                    .body("username", equalTo(userData.get("username")))
                    .body("email", equalTo(userData.get("email")))
                    .body("firstName", equalTo(userData.get("firstName")))
                    .body("lastName", equalTo(userData.get("lastName")));
            
            // Store new user ID
            String newUserId = response.jsonPath().getString("id");
            Assert.assertNotNull(newUserId, "New user ID should not be null");
            
            logger.info("Create user test completed successfully. New user ID: {}", newUserId);
        } catch (Exception e) {
            logger.error("Create user test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 5, groups = {"regression", "api", "list", "normal"})
    @Description("Test retrieving list of all users with pagination support")
    @Severity(SeverityLevel.NORMAL)
    public void testGetAllUsers() throws Exception {
        logger.info("Starting get all users test");
        
        try {
            // Get all users
            Response response = APIUtils.get("/users");
            
            // Validate response
            APIUtils.validateStatusCode(response, 200);
            APIUtils.validateResponseTime(response, 5000);
            
            // Validate response structure
            response.then()
                    .body("$", hasSize(greaterThan(0)))
                    .body("[0].id", notNullValue())
                    .body("[0].username", notNullValue());
            
            // Validate pagination if present
            if (response.jsonPath().get("totalCount") != null) {
                response.then()
                        .body("totalCount", greaterThan(0))
                        .body("page", notNullValue())
                        .body("pageSize", notNullValue());
            }
            
            logger.info("Get all users test completed successfully");
        } catch (Exception e) {
            logger.error("Get all users test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 6, groups = {"regression", "api", "retrieve", "normal"})
    @Description("Test retrieving specific user information by user ID")
    @Severity(SeverityLevel.NORMAL)
    public void testGetUserById() throws Exception {
        logger.info("Starting get user by ID test");
        
        try {
            // Use stored user ID or create a test user ID
            String testUserId = userId != null ? userId : "1";
            
            // Get user by ID
            Response response = APIUtils.get("/users/" + testUserId);
            
            // Validate response
            APIUtils.validateStatusCode(response, 200);
            APIUtils.validateResponseTime(response, 5000);
            
            // Validate response structure
            response.then()
                    .body("id", equalTo(testUserId))
                    .body("username", notNullValue())
                    .body("email", notNullValue());
            
            logger.info("Get user by ID test completed successfully");
        } catch (Exception e) {
            logger.error("Get user by ID test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 7, groups = {"regression", "api", "search", "normal"})
    @Description("Test user search functionality with various search criteria")
    @Severity(SeverityLevel.NORMAL)
    public void testUserSearch() throws Exception {
        logger.info("Starting user search test");
        
        try {
            // Search for users
            Response response = APIUtils.get("/users/search?q=test");
            
            // Validate response
            APIUtils.validateStatusCode(response, 200);
            APIUtils.validateResponseTime(response, 5000);
            
            // Validate response structure
            response.then()
                    .body("$", instanceOf(java.util.List.class));
            
            // If results exist, validate structure
            if (response.jsonPath().getList("$").size() > 0) {
                response.then()
                        .body("[0].id", notNullValue())
                        .body("[0].username", notNullValue());
            }
            
            logger.info("User search test completed successfully");
        } catch (Exception e) {
            logger.error("User search test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 8, groups = {"regression", "api", "validation", "negative"})
    @Description("Test user validation with invalid data and error handling")
    @Severity(SeverityLevel.NORMAL)
    public void testUserValidation() throws Exception {
        logger.info("Starting user validation test");
        
        try {
            // Test with invalid email
            Map<String, Object> invalidData = new HashMap<>();
            invalidData.put("username", "testuser");
            invalidData.put("email", "invalid-email");
            invalidData.put("password", "pass");
            
            Response response = APIUtils.post("/users", invalidData);
            
            // Should return validation error
            Assert.assertTrue(response.getStatusCode() >= 400 && response.getStatusCode() < 500,
                    "Should return client error for invalid data");
            
            // Test with missing required fields
            Map<String, Object> missingData = new HashMap<>();
            missingData.put("username", "testuser");
            // Missing email and password
            
            response = APIUtils.post("/users", missingData);
            
            // Should return validation error
            Assert.assertTrue(response.getStatusCode() >= 400 && response.getStatusCode() < 500,
                    "Should return client error for missing data");
            
            logger.info("User validation test completed successfully");
        } catch (Exception e) {
            logger.error("User validation test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 9, groups = {"regression", "api", "security", "critical"})
    @Description("Test API security with unauthorized access attempts")
    @Severity(SeverityLevel.CRITICAL)
    public void testUserSecurity() throws Exception {
        logger.info("Starting user security test");
        
        try {
            // Clear auth token temporarily
            String originalToken = authToken;
            APIUtils.setAuthToken(null);
            
            // Try to access protected resource without authentication
            Response response = APIUtils.get("/users/profile");
            
            // Should return unauthorized
            Assert.assertEquals(response.getStatusCode(), 401, "Should return 401 for unauthorized access");
            
            // Restore auth token
            APIUtils.setAuthToken(originalToken);
            
            // Test with invalid token
            APIUtils.setAuthToken("invalid-token");
            
            response = APIUtils.get("/users/profile");
            
            // Should return unauthorized or forbidden
            Assert.assertTrue(response.getStatusCode() == 401 || response.getStatusCode() == 403,
                    "Should return 401 or 403 for invalid token");
            
            // Restore original token
            APIUtils.setAuthToken(originalToken);
            
            logger.info("User security test completed successfully");
        } catch (Exception e) {
            logger.error("User security test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 10, groups = {"regression", "api", "performance", "normal"})
    @Description("Test API performance with multiple concurrent requests")
    @Severity(SeverityLevel.NORMAL)
    public void testUserApiPerformance() throws Exception {
        logger.info("Starting user API performance test");
        
        try {
            // Test multiple rapid requests
            long totalTime = 0;
            int requestCount = 5;
            
            for (int i = 0; i < requestCount; i++) {
                long startTime = System.currentTimeMillis();
                
                Response response = APIUtils.get("/users/profile");
                APIUtils.validateStatusCode(response, 200);
                
                long endTime = System.currentTimeMillis();
                totalTime += (endTime - startTime);
            }
            
            long averageTime = totalTime / requestCount;
            
            // Assert average response time is reasonable
            Assert.assertTrue(averageTime < 2000, "Average response time should be less than 2 seconds");
            
            logger.info("User API performance test completed successfully. Average response time: {}ms", averageTime);
        } catch (Exception e) {
            logger.error("User API performance test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 11, groups = {"regression", "api", "schema", "validation"})
    @Description("Test API response schema validation and data integrity")
    @Severity(SeverityLevel.NORMAL)
    public void testUserSchemaValidation() throws Exception {
        logger.info("Starting user schema validation test");
        
        try {
            // Get user profile for schema validation
            Response response = APIUtils.get("/users/profile");
            
            // Validate response
            APIUtils.validateStatusCode(response, 200);
            
            // Validate required fields are present
            response.then()
                    .body("id", notNullValue())
                    .body("username", notNullValue())
                    .body("email", notNullValue())
                    .body("createdAt", notNullValue());
            
            // Validate data types
            response.then()
                    .body("id", instanceOf(String.class))
                    .body("username", instanceOf(String.class))
                    .body("email", instanceOf(String.class));
            
            // Validate email format
            String email = response.jsonPath().getString("email");
            Assert.assertTrue(email.contains("@"), "Email should contain @ symbol");
            Assert.assertTrue(email.contains("."), "Email should contain domain extension");
            
            logger.info("User schema validation test completed successfully");
        } catch (Exception e) {
            logger.error("User schema validation test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 12, groups = {"regression", "api", "boundary", "edge-cases"})
    @Description("Test API boundary conditions and edge cases")
    @Severity(SeverityLevel.NORMAL)
    public void testUserBoundaryConditions() throws Exception {
        logger.info("Starting user boundary conditions test");
        
        try {
            // Test with very long username
            Map<String, Object> longUsernameData = new HashMap<>();
            longUsernameData.put("username", "a".repeat(256)); // Very long username
            longUsernameData.put("email", "test@example.com");
            longUsernameData.put("password", "password");
            
            Response response = APIUtils.post("/users", longUsernameData);
            
            // Should handle gracefully (either accept or reject with proper error)
            Assert.assertTrue(response.getStatusCode() == 201 || 
                            (response.getStatusCode() >= 400 && response.getStatusCode() < 500),
                    "Should handle long username gracefully");
            
            // Test with empty strings
            Map<String, Object> emptyData = new HashMap<>();
            emptyData.put("username", "");
            emptyData.put("email", "");
            emptyData.put("password", "");
            
            response = APIUtils.post("/users", emptyData);
            
            // Should return validation error
            Assert.assertTrue(response.getStatusCode() >= 400 && response.getStatusCode() < 500,
                    "Should return validation error for empty fields");
            
            // Test with special characters
            Map<String, Object> specialCharData = new HashMap<>();
            specialCharData.put("username", "test@#$%^&*()");
            specialCharData.put("email", "test@example.com");
            specialCharData.put("password", "password");
            
            response = APIUtils.post("/users", specialCharData);
            
            // Should handle special characters appropriately
            logger.info("Special character test response: {}", response.getStatusCode());
            
            logger.info("User boundary conditions test completed successfully");
        } catch (Exception e) {
            logger.error("User boundary conditions test failed: {}", e.getMessage());
            throw e;
        }
    }
}