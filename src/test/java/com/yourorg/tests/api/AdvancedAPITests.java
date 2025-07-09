package com.yourorg.tests.api;

import com.yourorg.api.builders.RequestBuilder;
import com.yourorg.api.chains.APIChainExecutor;
import com.yourorg.api.chains.ChainDefinition;
import com.yourorg.api.chains.ChainResult;
import com.yourorg.api.managers.ResponseManager;
import com.yourorg.api.managers.TokenManager;
import com.yourorg.api.validators.SchemaValidator;
import com.yourorg.api.validators.StatusCodeValidator;
import com.yourorg.base.BaseTest;
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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Epic("Advanced API Tests")
@Feature("Complex API Scenarios")
public class AdvancedAPITests extends BaseTest {
    private static final Logger logger = LogManager.getLogger(AdvancedAPITests.class);
    private ResponseManager responseManager;
    private APIChainExecutor chainExecutor;
    private TokenManager tokenManager;
    private SchemaValidator schemaValidator;

    @BeforeClass(groups = {"api", "advanced"})
    public void setupAdvancedTests() throws Exception {
        logger.info("Setting up advanced API tests");
        
        responseManager = new ResponseManager();
        chainExecutor = new APIChainExecutor();
        tokenManager = TokenManager.getInstance();
        schemaValidator = new SchemaValidator();
        
        // Initialize authentication
        String username = ConfigLoader.get("api.test.username", "testuser");
        String password = ConfigLoader.get("api.test.password", "testpass");
        tokenManager.refreshToken("user");
    }

    @Test(priority = 1, groups = {"api", "large-dataset", "performance"})
    @Description("Fetch specific JSON object from API response containing thousands of records")
    @Severity(SeverityLevel.NORMAL)
    public void testFetchSpecificRecordFromLargeDataset() throws Exception {
        logger.info("Starting large dataset record fetch test");
        
        try {
            // Get all users (simulating large dataset)
            String token = tokenManager.getValidToken("user");
            
            Response response = new RequestBuilder()
                    .baseUrl(ConfigLoader.get("api.base.url"))
                    .endpoint("/users")
                    .auth(token)
                    .queryParam("limit", 1000)
                    .build()
                    .get("/users");
            
            StatusCodeValidator.validateAndExplain(response, 200);
            
            // Find specific record by ID
            String targetUserId = "user_12345";
            Map<String, Object> specificUser = responseManager.findRecordById(response, "id", targetUserId);
            
            Assert.assertFalse(specificUser.isEmpty(), "Should find specific user record");
            Assert.assertEquals(specificUser.get("id"), targetUserId, "Should return correct user");
            
            // Use the found record for subsequent API call
            String userEmail = (String) specificUser.get("email");
            
            Response profileResponse = new RequestBuilder()
                    .baseUrl(ConfigLoader.get("api.base.url"))
                    .endpoint("/users/profile")
                    .auth(token)
                    .queryParam("email", userEmail)
                    .build()
                    .get("/users/profile");
            
            StatusCodeValidator.validateAndExplain(profileResponse, 200);
            
            logger.info("Successfully fetched specific record and used in subsequent request");
        } catch (Exception e) {
            logger.error("Large dataset fetch test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 2, groups = {"api", "token-refresh", "resilience"})
    @Description("Handle token refresh during upload API test execution")
    @Severity(SeverityLevel.CRITICAL)
    public void testTokenRefreshDuringUpload() throws Exception {
        logger.info("Starting token refresh during upload test");
        
        try {
            // Start with a valid token
            String initialToken = tokenManager.getValidToken("user");
            
            // Simulate file upload
            Map<String, Object> uploadData = Map.of(
                    "fileName", "test-file.pdf",
                    "fileSize", 1024000,
                    "fileType", "application/pdf"
            );
            
            Response uploadResponse = new RequestBuilder()
                    .baseUrl(ConfigLoader.get("api.base.url"))
                    .endpoint("/files/upload")
                    .auth(initialToken)
                    .body(uploadData)
                    .build()
                    .post("/files/upload");
            
            // Check if token was refreshed during the request
            if (uploadResponse.getStatusCode() == 401) {
                logger.info("Token expired during upload, refreshing...");
                
                // Refresh token
                tokenManager.refreshToken("user");
                String newToken = tokenManager.getValidToken("user");
                
                // Retry upload with new token
                uploadResponse = new RequestBuilder()
                        .baseUrl(ConfigLoader.get("api.base.url"))
                        .endpoint("/files/upload")
                        .auth(newToken)
                        .body(uploadData)
                        .build()
                        .post("/files/upload");
            }
            
            StatusCodeValidator.validateAndExplain(uploadResponse, 201);
            
            // Verify upload was successful
            String uploadId = uploadResponse.jsonPath().getString("uploadId");
            Assert.assertNotNull(uploadId, "Upload ID should be returned");
            
            logger.info("Token refresh during upload handled successfully");
        } catch (Exception e) {
            logger.error("Token refresh during upload test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 3, groups = {"api", "authentication", "error-handling"})
    @Description("Handle authentication failures during test execution")
    @Severity(SeverityLevel.CRITICAL)
    public void testAuthenticationFailureHandling() throws Exception {
        logger.info("Starting authentication failure handling test");
        
        try {
            // Test with invalid token
            Response invalidTokenResponse = new RequestBuilder()
                    .baseUrl(ConfigLoader.get("api.base.url"))
                    .endpoint("/users/profile")
                    .auth("invalid-token-12345")
                    .build()
                    .get("/users/profile");
            
            StatusCodeValidator.validateAuthenticationFailure(invalidTokenResponse);
            
            // Test with expired token (simulate)
            Response expiredTokenResponse = new RequestBuilder()
                    .baseUrl(ConfigLoader.get("api.base.url"))
                    .endpoint("/users/profile")
                    .auth("expired.token.here")
                    .build()
                    .get("/users/profile");
            
            StatusCodeValidator.validateAuthenticationFailure(expiredTokenResponse);
            
            // Test recovery with valid token
            String validToken = tokenManager.getValidToken("user");
            Response validResponse = new RequestBuilder()
                    .baseUrl(ConfigLoader.get("api.base.url"))
                    .endpoint("/users/profile")
                    .auth(validToken)
                    .build()
                    .get("/users/profile");
            
            StatusCodeValidator.validateAndExplain(validResponse, 200);
            
            logger.info("Authentication failure handling test completed successfully");
        } catch (Exception e) {
            logger.error("Authentication failure handling test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 4, groups = {"api", "status-codes", "validation"})
    @Description("Validate different HTTP status codes and their meanings")
    @Severity(SeverityLevel.NORMAL)
    public void testStatusCodeValidation() throws Exception {
        logger.info("Starting comprehensive status code validation test");
        
        try {
            String token = tokenManager.getValidToken("user");
            
            // Test 201 - Created
            Map<String, Object> createData = Map.of(
                    "name", "Test Resource",
                    "description", "Test Description"
            );
            
            Response createResponse = new RequestBuilder()
                    .baseUrl(ConfigLoader.get("api.base.url"))
                    .endpoint("/resources")
                    .auth(token)
                    .body(createData)
                    .build()
                    .post("/resources");
            
            StatusCodeValidator.validateCreationResponse(createResponse);
            
            String resourceId = createResponse.jsonPath().getString("id");
            
            // Test 200 - OK
            Response getResponse = new RequestBuilder()
                    .baseUrl(ConfigLoader.get("api.base.url"))
                    .endpoint("/resources/" + resourceId)
                    .auth(token)
                    .build()
                    .get("/resources/" + resourceId);
            
            StatusCodeValidator.validateAndExplain(getResponse, 200);
            
            // Test 404 - Not Found
            Response notFoundResponse = new RequestBuilder()
                    .baseUrl(ConfigLoader.get("api.base.url"))
                    .endpoint("/resources/nonexistent-id")
                    .auth(token)
                    .build()
                    .get("/resources/nonexistent-id");
            
            StatusCodeValidator.validateAndExplain(notFoundResponse, 404);
            
            // Test 405 - Method Not Allowed
            Response methodNotAllowedResponse = new RequestBuilder()
                    .baseUrl(ConfigLoader.get("api.base.url"))
                    .endpoint("/resources/" + resourceId)
                    .auth(token)
                    .build()
                    .patch("/resources/" + resourceId);
            
            StatusCodeValidator.validateMethodNotAllowed(methodNotAllowedResponse);
            
            // Test 204 - No Content (DELETE)
            Response deleteResponse = new RequestBuilder()
                    .baseUrl(ConfigLoader.get("api.base.url"))
                    .endpoint("/resources/" + resourceId)
                    .auth(token)
                    .build()
                    .delete("/resources/" + resourceId);
            
            StatusCodeValidator.validateDeletionResponse(deleteResponse);
            
            logger.info("Status code validation test completed successfully");
        } catch (Exception e) {
            logger.error("Status code validation test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 5, groups = {"api", "schema", "dynamic-validation"})
    @Description("Validate dynamic response schemas that change frequently")
    @Severity(SeverityLevel.NORMAL)
    public void testDynamicSchemaValidation() throws Exception {
        logger.info("Starting dynamic schema validation test");
        
        try {
            String token = tokenManager.getValidToken("user");
            
            // Test user schema validation
            Response userResponse = new RequestBuilder()
                    .baseUrl(ConfigLoader.get("api.base.url"))
                    .endpoint("/users/profile")
                    .auth(token)
                    .build()
                    .get("/users/profile");
            
            StatusCodeValidator.validateAndExplain(userResponse, 200);
            
            SchemaValidator.SchemaDefinition userSchema = SchemaValidator.CommonSchemas.userSchema();
            boolean isValidUserSchema = schemaValidator.validateDynamicSchema(userResponse, userSchema);
            Assert.assertTrue(isValidUserSchema, "User response should match expected schema");
            
            // Test paginated response schema
            Response paginatedResponse = new RequestBuilder()
                    .baseUrl(ConfigLoader.get("api.base.url"))
                    .endpoint("/users")
                    .auth(token)
                    .queryParam("page", 1)
                    .queryParam("size", 10)
                    .build()
                    .get("/users");
            
            StatusCodeValidator.validateAndExplain(paginatedResponse, 200);
            
            SchemaValidator.SchemaDefinition paginatedSchema = SchemaValidator.CommonSchemas.paginatedResponseSchema();
            boolean isValidPaginatedSchema = schemaValidator.validateDynamicSchema(paginatedResponse, paginatedSchema);
            Assert.assertTrue(isValidPaginatedSchema, "Paginated response should match expected schema");
            
            // Test custom schema with flexible rules
            SchemaValidator.SchemaDefinition customSchema = SchemaValidator.schema()
                    .requireField("data")
                    .fieldType("data", List.class)
                    .customRule("nonEmptyData", node -> {
                        if (node.has("data")) {
                            return node.get("data").size() > 0;
                        }
                        return false;
                    })
                    .build();
            
            boolean isValidCustomSchema = schemaValidator.validateDynamicSchema(paginatedResponse, customSchema);
            Assert.assertTrue(isValidCustomSchema, "Response should match custom schema rules");
            
            logger.info("Dynamic schema validation test completed successfully");
        } catch (Exception e) {
            logger.error("Dynamic schema validation test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 6, groups = {"api", "chained-requests", "workflow"})
    @Description("Execute chained flow of POST, GET, and DELETE requests with reusable payloads")
    @Severity(SeverityLevel.CRITICAL)
    public void testChainedAPIFlow() throws Exception {
        logger.info("Starting chained API flow test");
        
        try {
            // Create CRUD chain
            Map<String, Object> resourceData = Map.of(
                    "name", "Test Resource",
                    "description", "Created via chained API flow",
                    "category", "test",
                    "isActive", true
            );
            
            ChainDefinition crudChain = APIChainExecutor.createCrudChain("resources", resourceData);
            ChainResult result = chainExecutor.executeChain(crudChain);
            
            Assert.assertTrue(result.isSuccess(), "CRUD chain should execute successfully");
            
            // Verify each step
            Response createResponse = result.getStepResult("create_resources");
            Assert.assertEquals(createResponse.getStatusCode(), 201, "Create should return 201");
            
            Response getResponse = result.getStepResult("get_resources");
            Assert.assertEquals(getResponse.getStatusCode(), 200, "Get should return 200");
            
            Response updateResponse = result.getStepResult("update_resources");
            Assert.assertEquals(updateResponse.getStatusCode(), 200, "Update should return 200");
            
            Response deleteResponse = result.getStepResult("delete_resources");
            Assert.assertTrue(deleteResponse.getStatusCode() == 200 || deleteResponse.getStatusCode() == 204,
                    "Delete should return 200 or 204");
            
            // Test e-commerce order chain
            Map<String, Object> orderData = Map.of(
                    "items", List.of(
                            Map.of("productId", "PROD_001", "quantity", 2, "price", 29.99),
                            Map.of("productId", "PROD_002", "quantity", 1, "price", 49.99)
                    ),
                    "totalAmount", 109.97,
                    "currency", "USD"
            );
            
            ChainDefinition orderChain = APIChainExecutor.createOrderChain(orderData);
            ChainResult orderResult = chainExecutor.executeChain(orderChain);
            
            Assert.assertTrue(orderResult.isSuccess(), "Order chain should execute successfully");
            
            logger.info("Chained API flow test completed successfully");
        } catch (Exception e) {
            logger.error("Chained API flow test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 7, groups = {"api", "filtering", "large-dataset"})
    @Description("Filter and process large datasets with complex criteria")
    @Severity(SeverityLevel.NORMAL)
    public void testComplexDataFiltering() throws Exception {
        logger.info("Starting complex data filtering test");
        
        try {
            String token = tokenManager.getValidToken("user");
            
            // Get large dataset
            Response response = new RequestBuilder()
                    .baseUrl(ConfigLoader.get("api.base.url"))
                    .endpoint("/users")
                    .auth(token)
                    .queryParam("limit", 1000)
                    .build()
                    .get("/users");
            
            StatusCodeValidator.validateAndExplain(response, 200);
            
            // Define complex filters
            Map<String, Predicate<Object>> filters = Map.of(
                    "isActive", value -> Boolean.TRUE.equals(value),
                    "role", value -> "customer".equals(value),
                    "createdAt", value -> {
                        // Filter users created in the last 30 days
                        if (value instanceof String) {
                            // Simplified date check - in real scenario, parse and compare dates
                            return ((String) value).contains("2024");
                        }
                        return false;
                    }
            );
            
            List<Map<String, Object>> filteredUsers = responseManager.filterRecords(response, filters);
            
            Assert.assertFalse(filteredUsers.isEmpty(), "Should find filtered users");
            
            // Verify filtering worked correctly
            for (Map<String, Object> user : filteredUsers) {
                Assert.assertEquals(user.get("isActive"), true, "All users should be active");
                Assert.assertEquals(user.get("role"), "customer", "All users should be customers");
            }
            
            logger.info("Found {} filtered users out of total dataset", filteredUsers.size());
            
            // Extract specific data for subsequent processing
            List<Object> userIds = responseManager.extractNestedValues(response, "$.data[*].id");
            Assert.assertFalse(userIds.isEmpty(), "Should extract user IDs");
            
            logger.info("Complex data filtering test completed successfully");
        } catch (Exception e) {
            logger.error("Complex data filtering test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 8, groups = {"api", "pagination", "performance"})
    @Description("Handle pagination for large datasets efficiently")
    @Severity(SeverityLevel.NORMAL)
    public void testPaginationHandling() throws Exception {
        logger.info("Starting pagination handling test");
        
        try {
            String token = tokenManager.getValidToken("user");
            
            // Collect all records using pagination
            List<Map<String, Object>> allRecords = responseManager.getAllRecordsPaginated(
                    "/users", token, 50
            );
            
            Assert.assertFalse(allRecords.isEmpty(), "Should collect paginated records");
            
            // Verify no duplicates
            long uniqueIds = allRecords.stream()
                    .map(record -> record.get("id"))
                    .distinct()
                    .count();
            
            Assert.assertEquals(uniqueIds, allRecords.size(), "Should not have duplicate records");
            
            logger.info("Successfully collected {} unique records via pagination", allRecords.size());
            
            logger.info("Pagination handling test completed successfully");
        } catch (Exception e) {
            logger.error("Pagination handling test failed: {}", e.getMessage());
            throw e;
        }
    }
}