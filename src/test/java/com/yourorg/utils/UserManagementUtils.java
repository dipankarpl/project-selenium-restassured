package com.yourorg.utils;

import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserManagementUtils {
    private static final Logger logger = LogManager.getLogger(UserManagementUtils.class);
    
    public static class TestUser {
        private String userId;
        private String username;
        private String email;
        private String password;
        private String authToken;
        private Map<String, Object> userData;
        
        public TestUser(String userId, String username, String email, String password, String authToken) {
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.password = password;
            this.authToken = authToken;
            this.userData = new HashMap<>();
        }
        
        // Getters and setters
        public String getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getPassword() { return password; }
        public String getAuthToken() { return authToken; }
        public Map<String, Object> getUserData() { return userData; }
        public void setUserData(String key, Object value) { userData.put(key, value); }
        public Object getUserData(String key) { return userData.get(key); }
    }
    
    /**
     * Creates a new test user via API with random credentials
     */
    public static TestUser createTestUser() throws Exception {
        return createTestUser(null, null);
    }
    
    /**
     * Creates a new test user via API with specified role and permissions
     */
    public static TestUser createTestUser(String role, Map<String, Object> permissions) throws Exception {
        logger.info("Creating new test user via API");
        
        try {
            // Generate unique user data
            String timestamp = String.valueOf(System.currentTimeMillis());
            String username = "testuser_" + timestamp;
            String email = "testuser_" + timestamp + "@example.com";
            String password = "TestPass123!";
            
            // Prepare user creation data
            Map<String, Object> userData = new HashMap<>();
            userData.put("username", username);
            userData.put("email", email);
            userData.put("password", password);
            userData.put("firstName", "Test");
            userData.put("lastName", "User");
            
            if (role != null) {
                userData.put("role", role);
            }
            
            if (permissions != null) {
                userData.put("permissions", permissions);
            }
            
            // Create user via API
            Response response = APIUtils.post("/users", userData);
            APIUtils.validateStatusCode(response, 201);
            
            String userId = response.jsonPath().getString("id");
            
            // Authenticate the new user to get token
            String authToken = APIUtils.authenticateUser(username, password);
            
            TestUser testUser = new TestUser(userId, username, email, password, authToken);
            
            logger.info("Test user created successfully - ID: {}, Username: {}", userId, username);
            return testUser;
            
        } catch (Exception e) {
            logger.error("Failed to create test user: {}", e.getMessage());
            throw new Exception("Test user creation failed", e);
        }
    }
    
    /**
     * Creates a test user with specific profile data
     */
    public static TestUser createTestUserWithProfile(Map<String, Object> profileData) throws Exception {
        logger.info("Creating test user with custom profile data");
        
        try {
            TestUser user = createTestUser();
            
            // Update user profile with custom data
            APIUtils.setAuthToken(user.getAuthToken());
            Response response = APIUtils.put("/users/profile", profileData);
            APIUtils.validateStatusCode(response, 200);
            
            // Store profile data in user object
            profileData.forEach(user::setUserData);
            
            logger.info("Test user profile updated successfully");
            return user;
            
        } catch (Exception e) {
            logger.error("Failed to create test user with profile: {}", e.getMessage());
            throw new Exception("Test user profile creation failed", e);
        }
    }
    
    /**
     * Sets up user session in browser using cookies/tokens
     */
    public static void setupUserSession(WebDriver driver, TestUser user) throws Exception {
        logger.info("Setting up user session in browser for user: {}", user.getUsername());
        
        try {
            // Navigate to the application first
            String baseUrl = ConfigLoader.get("app.base.url");
            driver.get(baseUrl);
            
            // Add authentication cookie/token
            Cookie authCookie = new Cookie("auth_token", user.getAuthToken());
            driver.manage().addCookie(authCookie);
            
            // Add user session cookie
            Cookie userCookie = new Cookie("user_id", user.getUserId());
            driver.manage().addCookie(userCookie);
            
            // Add username cookie for UI personalization
            Cookie usernameCookie = new Cookie("username", user.getUsername());
            driver.manage().addCookie(usernameCookie);
            
            // Refresh page to apply cookies
            driver.navigate().refresh();
            
            // Wait for session to be established
            Thread.sleep(2000);
            
            logger.info("User session established successfully in browser");
            
        } catch (Exception e) {
            logger.error("Failed to setup user session: {}", e.getMessage());
            throw new Exception("User session setup failed", e);
        }
    }
    
    /**
     * Prepares user with prerequisite data for testing
     */
    public static void prepareUserWithData(TestUser user, String dataType) throws Exception {
        logger.info("Preparing user {} with {} data", user.getUsername(), dataType);
        
        try {
            APIUtils.setAuthToken(user.getAuthToken());
            
            switch (dataType.toLowerCase()) {
                case "shopping_cart":
                    prepareShoppingCartData(user);
                    break;
                case "order_history":
                    prepareOrderHistoryData(user);
                    break;
                case "payment_methods":
                    preparePaymentMethodsData(user);
                    break;
                case "preferences":
                    prepareUserPreferencesData(user);
                    break;
                case "addresses":
                    prepareAddressesData(user);
                    break;
                default:
                    logger.warn("Unknown data type: {}", dataType);
            }
            
            logger.info("User data preparation completed for: {}", dataType);
            
        } catch (Exception e) {
            logger.error("Failed to prepare user data: {}", e.getMessage());
            throw new Exception("User data preparation failed", e);
        }
    }
    
    private static void prepareShoppingCartData(TestUser user) throws Exception {
        // Add items to shopping cart
        Map<String, Object> cartItem = new HashMap<>();
        cartItem.put("productId", "PROD_001");
        cartItem.put("quantity", 2);
        cartItem.put("price", 29.99);
        
        Response response = APIUtils.post("/cart/items", cartItem);
        APIUtils.validateStatusCode(response, 201);
        
        user.setUserData("cart_items", 1);
    }
    
    private static void prepareOrderHistoryData(TestUser user) throws Exception {
        // Create a test order
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("items", "[{\"productId\":\"PROD_001\",\"quantity\":1,\"price\":29.99}]");
        orderData.put("totalAmount", 29.99);
        orderData.put("status", "completed");
        
        Response response = APIUtils.post("/orders", orderData);
        APIUtils.validateStatusCode(response, 201);
        
        user.setUserData("order_count", 1);
    }
    
    private static void preparePaymentMethodsData(TestUser user) throws Exception {
        // Add a test payment method
        Map<String, Object> paymentMethod = new HashMap<>();
        paymentMethod.put("type", "credit_card");
        paymentMethod.put("last4", "4242");
        paymentMethod.put("expiryMonth", "12");
        paymentMethod.put("expiryYear", "2025");
        
        Response response = APIUtils.post("/payment-methods", paymentMethod);
        APIUtils.validateStatusCode(response, 201);
        
        user.setUserData("payment_methods", 1);
    }
    
    private static void prepareUserPreferencesData(TestUser user) throws Exception {
        // Set user preferences
        Map<String, Object> preferences = new HashMap<>();
        preferences.put("language", "en");
        preferences.put("currency", "USD");
        preferences.put("notifications", true);
        preferences.put("theme", "light");
        
        Response response = APIUtils.put("/users/preferences", preferences);
        APIUtils.validateStatusCode(response, 200);
        
        user.setUserData("preferences_set", true);
    }
    
    private static void prepareAddressesData(TestUser user) throws Exception {
        // Add shipping address
        Map<String, Object> address = new HashMap<>();
        address.put("type", "shipping");
        address.put("street", "123 Test Street");
        address.put("city", "Test City");
        address.put("state", "TS");
        address.put("zipCode", "12345");
        address.put("country", "US");
        
        Response response = APIUtils.post("/addresses", address);
        APIUtils.validateStatusCode(response, 201);
        
        user.setUserData("addresses", 1);
    }
    
    /**
     * Deletes test user and cleans up all associated data
     */
    public static void deleteTestUser(TestUser user) throws Exception {
        if (user == null) {
            logger.warn("Cannot delete null user");
            return;
        }
        
        logger.info("Deleting test user: {}", user.getUsername());
        
        try {
            // Set authentication for deletion
            APIUtils.setAuthToken(user.getAuthToken());
            
            // Clean up user data first
            cleanupUserData(user);
            
            // Delete the user
            Response response = APIUtils.delete("/users/" + user.getUserId());
            
            // Accept both 200 and 204 as successful deletion
            if (response.getStatusCode() != 200 && response.getStatusCode() != 204) {
                logger.warn("User deletion returned status: {}", response.getStatusCode());
            }
            
            logger.info("Test user deleted successfully: {}", user.getUsername());
            
        } catch (Exception e) {
            logger.error("Failed to delete test user {}: {}", user.getUsername(), e.getMessage());
            // Don't throw exception here to avoid test failures during cleanup
        }
    }
    
    private static void cleanupUserData(TestUser user) throws Exception {
        try {
            // Clean up cart items
            APIUtils.delete("/cart/clear");
            
            // Clean up orders (if test orders)
            APIUtils.delete("/orders/test-data");
            
            // Clean up payment methods
            APIUtils.delete("/payment-methods/all");
            
            // Clean up addresses
            APIUtils.delete("/addresses/all");
            
            logger.info("User data cleanup completed");
            
        } catch (Exception e) {
            logger.warn("Some user data cleanup failed: {}", e.getMessage());
        }
    }
    
    /**
     * Validates user session is active in browser
     */
    public static boolean isUserSessionActive(WebDriver driver, TestUser user) throws Exception {
        try {
            // Check for authentication cookies
            Cookie authCookie = driver.manage().getCookieNamed("auth_token");
            Cookie userCookie = driver.manage().getCookieNamed("user_id");
            
            if (authCookie == null || userCookie == null) {
                return false;
            }
            
            // Validate cookie values
            return authCookie.getValue().equals(user.getAuthToken()) && 
                   userCookie.getValue().equals(user.getUserId());
                   
        } catch (Exception e) {
            logger.error("Failed to validate user session: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Creates admin user with elevated privileges
     */
    public static TestUser createAdminUser() throws Exception {
        logger.info("Creating admin test user");
        
        Map<String, Object> adminPermissions = new HashMap<>();
        adminPermissions.put("admin", true);
        adminPermissions.put("manage_users", true);
        adminPermissions.put("manage_orders", true);
        adminPermissions.put("view_analytics", true);
        
        return createTestUser("admin", adminPermissions);
    }
    
    /**
     * Creates user with specific role (customer, vendor, moderator, etc.)
     */
    public static TestUser createUserWithRole(String role) throws Exception {
        logger.info("Creating test user with role: {}", role);
        
        Map<String, Object> rolePermissions = new HashMap<>();
        
        switch (role.toLowerCase()) {
            case "customer":
                rolePermissions.put("place_orders", true);
                rolePermissions.put("view_products", true);
                break;
            case "vendor":
                rolePermissions.put("manage_products", true);
                rolePermissions.put("view_sales", true);
                break;
            case "moderator":
                rolePermissions.put("moderate_content", true);
                rolePermissions.put("manage_reviews", true);
                break;
        }
        
        return createTestUser(role, rolePermissions);
    }
}