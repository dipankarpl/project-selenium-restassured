package com.yourorg.tests.api;

import com.yourorg.base.BaseTest;
import com.yourorg.utils.APIUtils;
import com.yourorg.utils.ConfigLoader;
import com.yourorg.utils.FakerDataGenerator;
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

import java.util.Map;

@Epic("API Security Tests")
@Feature("Security Vulnerability Testing")
public class SecurityTests extends BaseTest {
    private static final Logger logger = LogManager.getLogger(SecurityTests.class);
    private static String validToken;

    @BeforeClass(groups = {"security", "api"})
    public void setupSecurityTests() throws Exception {
        logger.info("Setting up security tests");
        
        try {
            // Authenticate to get valid token for manipulation tests
            String username = ConfigLoader.get("api.test.username", "testuser");
            String password = ConfigLoader.get("api.test.password", "testpass");
            validToken = APIUtils.authenticateUser(username, password);
            
        } catch (Exception e) {
            logger.warn("Security test setup failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 1, groups = {"security", "sql-injection", "critical"})
    @Description("Test SQL injection vulnerabilities in user input fields")
    @Severity(SeverityLevel.BLOCKER)
    public void testSQLInjectionVulnerabilities() throws Exception {
        logger.info("Starting SQL injection vulnerability tests");
        
        try {
            Map<String, String> sqlPayloads = FakerDataGenerator.generateSecurityTestData();
            
            // Test SQL injection on login endpoint
            String[] sqlInjectionPayloads = {
                sqlPayloads.get("sqlInjection1"),
                sqlPayloads.get("sqlInjection2"),
                sqlPayloads.get("sqlInjection3"),
                sqlPayloads.get("sqlInjection4"),
                sqlPayloads.get("sqlInjection5")
            };
            
            for (String payload : sqlInjectionPayloads) {
                logger.info("Testing SQL injection payload: {}", payload);
                
                // Test on login endpoint
                Response loginResponse = APIUtils.testSQLInjection("/auth/login", payload);
                
                // Should not return 200 for malicious payloads
                Assert.assertNotEquals(loginResponse.getStatusCode(), 200, 
                        "SQL injection payload should not succeed: " + payload);
                
                // Test on user search endpoint
                Response searchResponse = APIUtils.testSQLInjection("/users/search", payload);
                
                // Should handle malicious input gracefully
                Assert.assertTrue(searchResponse.getStatusCode() >= 400 && searchResponse.getStatusCode() < 500,
                        "Should return client error for SQL injection attempt");
                
                // Verify no sensitive data is exposed in error messages
                String responseBody = searchResponse.getBody().asString();
                Assert.assertFalse(responseBody.toLowerCase().contains("sql"),
                        "Response should not expose SQL error details");
                Assert.assertFalse(responseBody.toLowerCase().contains("database"),
                        "Response should not expose database error details");
            }
            
            logger.info("SQL injection vulnerability tests completed successfully");
        } catch (Exception e) {
            logger.error("SQL injection vulnerability tests failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 2, groups = {"security", "xss", "critical"})
    @Description("Test XSS attack prevention in user input handling")
    @Severity(SeverityLevel.BLOCKER)
    public void testXSSAttackPrevention() throws Exception {
        logger.info("Starting XSS attack prevention tests");
        
        try {
            Map<String, String> xssPayloads = FakerDataGenerator.generateSecurityTestData();
            
            String[] xssAttackPayloads = {
                xssPayloads.get("xss1"),
                xssPayloads.get("xss2"),
                xssPayloads.get("xss3"),
                xssPayloads.get("xss4"),
                xssPayloads.get("xss5")
            };
            
            for (String payload : xssAttackPayloads) {
                logger.info("Testing XSS payload: {}", payload);
                
                // Test on user profile update
                Response profileResponse = APIUtils.testXSSAttack("/users/profile", payload);
                
                // Should handle XSS attempts gracefully
                Assert.assertTrue(profileResponse.getStatusCode() >= 400 && profileResponse.getStatusCode() < 500,
                        "Should return client error for XSS attempt");
                
                // Test on comment/review endpoints
                Response commentResponse = APIUtils.testXSSAttack("/comments", payload);
                
                // Verify XSS payload is not reflected in response
                String responseBody = commentResponse.getBody().asString();
                Assert.assertFalse(responseBody.contains("<script>"),
                        "Response should not contain unescaped script tags");
                Assert.assertFalse(responseBody.contains("javascript:"),
                        "Response should not contain javascript: protocol");
            }
            
            logger.info("XSS attack prevention tests completed successfully");
        } catch (Exception e) {
            logger.error("XSS attack prevention tests failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 3, groups = {"security", "token-manipulation", "critical"})
    @Description("Test token manipulation and validation scenarios")
    @Severity(SeverityLevel.CRITICAL)
    public void testTokenManipulation() throws Exception {
        logger.info("Starting token manipulation tests");
        
        try {
            // Test with invalid token
            String invalidToken = "invalid.token.here";
            Response invalidResponse = APIUtils.testTokenManipulation("/users/profile", invalidToken);
            Assert.assertEquals(invalidResponse.getStatusCode(), 401, 
                    "Should return 401 for invalid token");
            
            // Test with expired token (simulate by modifying valid token)
            String expiredToken = validToken.substring(0, validToken.length() - 5) + "XXXXX";
            Response expiredResponse = APIUtils.testTokenManipulation("/users/profile", expiredToken);
            Assert.assertTrue(expiredResponse.getStatusCode() == 401 || expiredResponse.getStatusCode() == 403,
                    "Should return 401 or 403 for expired/modified token");
            
            // Test with malformed token
            String malformedToken = "malformed-token-without-proper-structure";
            Response malformedResponse = APIUtils.testTokenManipulation("/users/profile", malformedToken);
            Assert.assertEquals(malformedResponse.getStatusCode(), 401,
                    "Should return 401 for malformed token");
            
            // Test with empty token
            Response emptyTokenResponse = APIUtils.testTokenManipulation("/users/profile", "");
            Assert.assertEquals(emptyTokenResponse.getStatusCode(), 401,
                    "Should return 401 for empty token");
            
            // Test token injection attempts
            String injectionToken = validToken + "'; DROP TABLE users; --";
            Response injectionResponse = APIUtils.testTokenManipulation("/users/profile", injectionToken);
            Assert.assertTrue(injectionResponse.getStatusCode() >= 400,
                    "Should reject token injection attempts");
            
            logger.info("Token manipulation tests completed successfully");
        } catch (Exception e) {
            logger.error("Token manipulation tests failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 4, groups = {"security", "rate-limiting", "normal"})
    @Description("Test API rate limiting for login attempts and API calls")
    @Severity(SeverityLevel.NORMAL)
    public void testAPIRateLimiting() throws Exception {
        logger.info("Starting API rate limiting tests");
        
        try {
            // Test login rate limiting
            logger.info("Testing login rate limiting");
            APIUtils.testRateLimit("/auth/login", 10, 100); // 10 requests with 100ms interval
            
            // Test general API rate limiting
            logger.info("Testing general API rate limiting");
            APIUtils.testRateLimit("/users", 20, 50); // 20 requests with 50ms interval
            
            // Test burst rate limiting
            logger.info("Testing burst rate limiting");
            APIUtils.testRateLimit("/users/profile", 50, 0); // 50 rapid requests
            
            logger.info("API rate limiting tests completed successfully");
        } catch (Exception e) {
            logger.error("API rate limiting tests failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 5, groups = {"security", "transaction-limits", "normal"})
    @Description("Test transaction limits validation (daily, single, monthly)")
    @Severity(SeverityLevel.NORMAL)
    public void testTransactionLimits() throws Exception {
        logger.info("Starting transaction limits tests");
        
        try {
            // Test single transaction limit
            Map<String, Object> largeTransaction = Map.of(
                    "amount", 999999999,
                    "currency", "USD",
                    "description", "Large transaction test"
            );
            
            Response largeTransactionResponse = APIUtils.post("/transactions", largeTransaction);
            Assert.assertTrue(largeTransactionResponse.getStatusCode() >= 400,
                    "Should reject transactions exceeding single transaction limit");
            
            // Test daily limit simulation
            for (int i = 0; i < 5; i++) {
                Map<String, Object> dailyTransaction = Map.of(
                        "amount", 1000,
                        "currency", "USD",
                        "description", "Daily limit test " + i
                );
                
                Response dailyResponse = APIUtils.post("/transactions", dailyTransaction);
                logger.info("Daily transaction #{} response: {}", i + 1, dailyResponse.getStatusCode());
            }
            
            // Test negative amount
            Map<String, Object> negativeTransaction = Map.of(
                    "amount", -100,
                    "currency", "USD",
                    "description", "Negative amount test"
            );
            
            Response negativeResponse = APIUtils.post("/transactions", negativeTransaction);
            Assert.assertTrue(negativeResponse.getStatusCode() >= 400,
                    "Should reject negative transaction amounts");
            
            logger.info("Transaction limits tests completed successfully");
        } catch (Exception e) {
            logger.error("Transaction limits tests failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 6, groups = {"security", "authorization", "critical"})
    @Description("Test authorization bypass attempts and privilege escalation")
    @Severity(SeverityLevel.CRITICAL)
    public void testAuthorizationBypass() throws Exception {
        logger.info("Starting authorization bypass tests");
        
        try {
            // Test accessing admin endpoints with regular user token
            Response adminResponse = APIUtils.get("/admin/users");
            Assert.assertTrue(adminResponse.getStatusCode() == 403 || adminResponse.getStatusCode() == 401,
                    "Regular user should not access admin endpoints");
            
            // Test accessing other user's data
            Response otherUserResponse = APIUtils.get("/users/999999/profile");
            Assert.assertTrue(otherUserResponse.getStatusCode() >= 400,
                    "Should not access other user's private data");
            
            // Test privilege escalation attempts
            Map<String, Object> escalationData = Map.of(
                    "role", "admin",
                    "permissions", "all"
            );
            
            Response escalationResponse = APIUtils.put("/users/profile", escalationData);
            // Should either ignore the role change or reject it
            if (escalationResponse.getStatusCode() == 200) {
                // Verify role was not actually changed
                Response profileCheck = APIUtils.get("/users/profile");
                String role = profileCheck.jsonPath().getString("role");
                Assert.assertNotEquals(role, "admin", "Role should not be escalated");
            }
            
            logger.info("Authorization bypass tests completed successfully");
        } catch (Exception e) {
            logger.error("Authorization bypass tests failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 7, groups = {"security", "input-validation", "normal"})
    @Description("Test input validation and boundary conditions")
    @Severity(SeverityLevel.NORMAL)
    public void testInputValidation() throws Exception {
        logger.info("Starting input validation tests");
        
        try {
            Map<String, String> boundaryData = FakerDataGenerator.generateBoundaryTestData();
            
            // Test with very long strings
            Map<String, Object> longStringData = Map.of(
                    "username", boundaryData.get("longString"),
                    "email", "test@example.com",
                    "password", "password123"
            );
            
            Response longStringResponse = APIUtils.post("/users", longStringData);
            Assert.assertTrue(longStringResponse.getStatusCode() >= 400,
                    "Should reject excessively long input");
            
            // Test with special characters
            Map<String, Object> specialCharData = Map.of(
                    "username", boundaryData.get("specialChars"),
                    "email", "test@example.com",
                    "password", "password123"
            );
            
            Response specialCharResponse = APIUtils.post("/users", specialCharData);
            logger.info("Special character test response: {}", specialCharResponse.getStatusCode());
            
            // Test with null values
            Map<String, Object> nullData = Map.of(
                    "username", "",
                    "email", "",
                    "password", ""
            );
            
            Response nullResponse = APIUtils.post("/users", nullData);
            Assert.assertTrue(nullResponse.getStatusCode() >= 400,
                    "Should reject null/empty required fields");
            
            logger.info("Input validation tests completed successfully");
        } catch (Exception e) {
            logger.error("Input validation tests failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 8, groups = {"security", "headers", "normal"})
    @Description("Test security headers and HTTPS enforcement")
    @Severity(SeverityLevel.NORMAL)
    public void testSecurityHeaders() throws Exception {
        logger.info("Starting security headers tests");
        
        try {
            // Test security headers on various endpoints
            String[] endpoints = {"/users", "/auth/login", "/users/profile"};
            
            for (String endpoint : endpoints) {
                Response response = APIUtils.get(endpoint);
                APIUtils.validateSecurityHeaders(response);
                
                // Verify HTTPS is enforced
                String baseUrl = ConfigLoader.get("api.base.url");
                Assert.assertTrue(baseUrl.startsWith("https://"),
                        "API should enforce HTTPS");
            }
            
            logger.info("Security headers tests completed successfully");
        } catch (Exception e) {
            logger.error("Security headers tests failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 9, groups = {"security", "data-exposure", "critical"})
    @Description("Test for sensitive data exposure in API responses")
    @Severity(SeverityLevel.CRITICAL)
    public void testSensitiveDataExposure() throws Exception {
        logger.info("Starting sensitive data exposure tests");
        
        try {
            // Test user profile endpoint
            Response profileResponse = APIUtils.get("/users/profile");
            
            if (profileResponse.getStatusCode() == 200) {
                String responseBody = profileResponse.getBody().asString();
                
                // Check for sensitive data that should not be exposed
                Assert.assertFalse(responseBody.contains("password"),
                        "Password should not be exposed in API response");
                Assert.assertFalse(responseBody.contains("ssn"),
                        "SSN should not be exposed in API response");
                Assert.assertFalse(responseBody.contains("creditCard"),
                        "Credit card info should not be exposed in API response");
                Assert.assertFalse(responseBody.contains("secret"),
                        "Secret keys should not be exposed in API response");
            }
            
            // Test error responses for information disclosure
            Response errorResponse = APIUtils.get("/nonexistent-endpoint");
            String errorBody = errorResponse.getBody().asString();
            
            Assert.assertFalse(errorBody.contains("stack trace"),
                    "Stack traces should not be exposed");
            Assert.assertFalse(errorBody.contains("database"),
                    "Database information should not be exposed");
            Assert.assertFalse(errorBody.contains("internal"),
                    "Internal system information should not be exposed");
            
            logger.info("Sensitive data exposure tests completed successfully");
        } catch (Exception e) {
            logger.error("Sensitive data exposure tests failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 10, groups = {"security", "session-management", "normal"})
    @Description("Test session management and timeout scenarios")
    @Severity(SeverityLevel.NORMAL)
    public void testSessionManagement() throws Exception {
        logger.info("Starting session management tests");
        
        try {
            // Test concurrent sessions
            String token1 = APIUtils.authenticateUser("testuser1", "password");
            String token2 = APIUtils.authenticateUser("testuser1", "password");
            
            // Both tokens should be valid (or system should invalidate old one)
            APIUtils.setAuthToken(token1);
            Response response1 = APIUtils.get("/users/profile");
            
            APIUtils.setAuthToken(token2);
            Response response2 = APIUtils.get("/users/profile");
            
            logger.info("Concurrent session test - Token1: {}, Token2: {}", 
                    response1.getStatusCode(), response2.getStatusCode());
            
            // Test session timeout (simulate by waiting)
            logger.info("Testing session timeout behavior");
            Thread.sleep(2000); // Wait 2 seconds
            
            Response timeoutResponse = APIUtils.get("/users/profile");
            logger.info("Session timeout test response: {}", timeoutResponse.getStatusCode());
            
            logger.info("Session management tests completed successfully");
        } catch (Exception e) {
            logger.error("Session management tests failed: {}", e.getMessage());
            throw e;
        }
    }
}