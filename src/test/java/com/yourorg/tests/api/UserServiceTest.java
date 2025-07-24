package com.yourorg.tests.api;

import com.yourorg.base.BaseTest;
import com.yourorg.utils.APIUtils;
import com.yourorg.utils.ConfigLoader;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;

public class UserServiceTest extends BaseTest {
    private static final Logger logger = LogManager.getLogger(UserServiceTest.class);
    private static String userId;
    private static String authToken;

    @Test(priority = 1, groups = {"sanity", "api", "authentication"})
    @Description("Test user authentication with valid credentials")
    @Severity(SeverityLevel.BLOCKER)
    public void testUserAuthentication() throws Exception {
        logger.info("Starting user authentication test");
        
        String username = ConfigLoader.get("api.test.username", "testuser");
        String password = ConfigLoader.get("api.test.password", "testpass");
        
        authToken = APIUtils.authenticateUser(username, password);
        
        Assert.assertNotNull(authToken, "Authentication token should not be null");
        Assert.assertFalse(authToken.isEmpty(), "Authentication token should not be empty");
        
        logger.info("User authentication test completed successfully");
    }

    @Test(priority = 2, dependsOnMethods = "testUserAuthentication", groups = {"sanity", "api"})
    @Description("Test retrieving user profile information")
    @Severity(SeverityLevel.CRITICAL)
    public void testGetUserProfile() throws Exception {
        logger.info("Starting get user profile test");
        
        Response response = APIUtils.get("/users/profile");
        
        APIUtils.validateStatusCode(response, 200);
        APIUtils.validateResponseTime(response, 5000);
        
        response.then()
                .body("id", notNullValue())
                .body("username", notNullValue())
                .body("email", notNullValue());
        
        userId = response.jsonPath().getString("id");
        
        logger.info("Get user profile test completed successfully");
    }

    @Test(priority = 3, groups = {"regression", "api"})
    @Description("Test creating a new user")
    @Severity(SeverityLevel.CRITICAL)
    public void testCreateUser() throws Exception {
        logger.info("Starting create user test");
        
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", "newuser" + System.currentTimeMillis());
        userData.put("email", "newuser" + System.currentTimeMillis() + "@example.com");
        userData.put("password", "newuserpass");
        userData.put("firstName", "New");
        userData.put("lastName", "User");
        
        Response response = APIUtils.post("/users", userData);
        
        APIUtils.validateStatusCode(response, 201);
        
        response.then()
                .body("id", notNullValue())
                .body("username", equalTo(userData.get("username")))
                .body("email", equalTo(userData.get("email")));
        
        String newUserId = response.jsonPath().getString("id");
        Assert.assertNotNull(newUserId, "New user ID should not be null");
        
        logger.info("Create user test completed successfully");
    }

    @Test(priority = 4, groups = {"regression", "api"})
    @Description("Test updating user profile")
    @Severity(SeverityLevel.NORMAL)
    public void testUpdateUserProfile() throws Exception {
        logger.info("Starting update user profile test");
        
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("firstName", "Updated");
        updateData.put("lastName", "User");
        updateData.put("email", "updated.user@example.com");
        
        Response response = APIUtils.put("/users/profile", updateData);
        
        APIUtils.validateStatusCode(response, 200);
        
        response.then()
                .body("firstName", equalTo("Updated"))
                .body("lastName", equalTo("User"))
                .body("email", equalTo("updated.user@example.com"));
        
        logger.info("Update user profile test completed successfully");
    }

    @Test(priority = 5, groups = {"regression", "api", "negative"})
    @Description("Test user validation with invalid data")
    @Severity(SeverityLevel.NORMAL)
    public void testUserValidation() throws Exception {
        logger.info("Starting user validation test");
        
        Map<String, Object> invalidData = new HashMap<>();
        invalidData.put("username", "");
        invalidData.put("email", "invalid-email");
        invalidData.put("password", "");
        
        Response response = APIUtils.post("/users", invalidData);
        
        Assert.assertTrue(response.getStatusCode() >= 400 && response.getStatusCode() < 500,
                "Should return client error for invalid data");
        
        logger.info("User validation test completed successfully");
    }
}