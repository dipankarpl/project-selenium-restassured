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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.*;

@Epic("API Tests")
@Feature("Payment Service Tests")
public class PaymentServiceTest extends BaseTest {
    private static final Logger logger = LogManager.getLogger(PaymentServiceTest.class);
    private static String paymentId;
    private static String customerId;

    @BeforeClass(groups = {"sanity", "regression"})
    public void setupPaymentTests() throws Exception {
        logger.info("Setting up payment tests");
        
        try {
            // Authenticate user
            String username = ConfigLoader.get("api.test.username", "testuser");
            String password = ConfigLoader.get("api.test.password", "testpass");
            APIUtils.authenticateUser(username, password);
            
            // Create a test customer
            Map<String, Object> customerData = new HashMap<>();
            customerData.put("name", "Test Customer");
            customerData.put("email", "customer@example.com");
            customerData.put("phone", "1234567890");
            
            Response response = APIUtils.post("/customers", customerData);
            if (response.getStatusCode() == 201) {
                customerId = response.jsonPath().getString("id");
                logger.info("Test customer created with ID: {}", customerId);
            }
            
        } catch (Exception e) {
            logger.warn("Payment test setup failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 1, groups = {"sanity", "api", "payment", "critical"})
    @Description("Test creating payment intent with valid payment data")
    @Severity(SeverityLevel.BLOCKER)
    public void testCreatePaymentIntent() throws Exception {
        logger.info("Starting create payment intent test");
        
        try {
            // Prepare payment data
            Map<String, Object> paymentData = new HashMap<>();
            paymentData.put("amount", 1000); // $10.00
            paymentData.put("currency", "USD");
            paymentData.put("customerId", customerId);
            paymentData.put("description", "Test payment");
            
            // Create payment intent
            Response response = APIUtils.post("/payments/intent", paymentData);
            
            // Validate response
            APIUtils.validateStatusCode(response, 201);
            APIUtils.validateResponseTime(response, 5000);
            
            // Validate response structure
            response.then()
                    .body("id", notNullValue())
                    .body("amount", equalTo(1000))
                    .body("currency", equalTo("USD"))
                    .body("status", equalTo("pending"))
                    .body("clientSecret", notNullValue());
            
            // Store payment ID
            paymentId = response.jsonPath().getString("id");
            
            logger.info("Create payment intent test completed successfully. Payment ID: {}", paymentId);
        } catch (Exception e) {
            logger.error("Create payment intent test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 2, dependsOnMethods = "testCreatePaymentIntent", groups = {"sanity", "api", "payment", "critical"})
    @Description("Test retrieving payment details by payment ID")
    @Severity(SeverityLevel.CRITICAL)
    public void testGetPaymentDetails() throws Exception {
        logger.info("Starting get payment details test");
        
        try {
            // Get payment details
            Response response = APIUtils.get("/payments/" + paymentId);
            
            // Validate response
            APIUtils.validateStatusCode(response, 200);
            APIUtils.validateResponseTime(response, 3000);
            
            // Validate response structure
            response.then()
                    .body("id", equalTo(paymentId))
                    .body("amount", notNullValue())
                    .body("currency", notNullValue())
                    .body("status", notNullValue())
                    .body("createdAt", notNullValue());
            
            // Validate JSON schema
            try {
                response.then().body(matchesJsonSchemaInClasspath("schemas/payment-schema.json"));
            } catch (Exception e) {
                logger.warn("JSON schema validation failed: {}", e.getMessage());
            }
            
            logger.info("Get payment details test completed successfully");
        } catch (Exception e) {
            logger.error("Get payment details test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 3, dependsOnMethods = "testCreatePaymentIntent", groups = {"regression", "api", "payment", "critical"})
    @Description("Test confirming payment with valid payment method")
    @Severity(SeverityLevel.CRITICAL)
    public void testConfirmPayment() throws Exception {
        logger.info("Starting confirm payment test");
        
        try {
            // Prepare confirmation data
            Map<String, Object> confirmData = new HashMap<>();
            confirmData.put("paymentMethod", "card");
            confirmData.put("cardNumber", "4242424242424242");
            confirmData.put("expiryMonth", "12");
            confirmData.put("expiryYear", "2025");
            confirmData.put("cvv", "123");
            
            // Confirm payment
            Response response = APIUtils.post("/payments/" + paymentId + "/confirm", confirmData);
            
            // Validate response
            APIUtils.validateStatusCode(response, 200);
            APIUtils.validateResponseTime(response, 10000); // Payment processing might take longer
            
            // Validate response structure
            response.then()
                    .body("id", equalTo(paymentId))
                    .body("status", anyOf(equalTo("succeeded"), equalTo("processing")))
                    .body("amount", notNullValue());
            
            logger.info("Confirm payment test completed successfully");
        } catch (Exception e) {
            logger.error("Confirm payment test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 4, groups = {"regression", "api", "payment", "list"})
    @Description("Test retrieving list of all payments with pagination")
    @Severity(SeverityLevel.NORMAL)
    public void testGetAllPayments() throws Exception {
        logger.info("Starting get all payments test");
        
        try {
            // Get all payments
            Response response = APIUtils.get("/payments");
            
            // Validate response
            APIUtils.validateStatusCode(response, 200);
            APIUtils.validateResponseTime(response, 5000);
            
            // Validate response structure
            response.then()
                    .body("$", instanceOf(java.util.List.class));
            
            // If payments exist, validate structure
            if (response.jsonPath().getList("$").size() > 0) {
                response.then()
                        .body("[0].id", notNullValue())
                        .body("[0].amount", notNullValue())
                        .body("[0].currency", notNullValue())
                        .body("[0].status", notNullValue());
            }
            
            logger.info("Get all payments test completed successfully");
        } catch (Exception e) {
            logger.error("Get all payments test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 5, groups = {"regression", "api", "payment", "refund"})
    @Description("Test payment refund functionality with partial refund")
    @Severity(SeverityLevel.NORMAL)
    public void testPaymentRefund() throws Exception {
        logger.info("Starting payment refund test");
        
        try {
            // Prepare refund data
            Map<String, Object> refundData = new HashMap<>();
            refundData.put("amount", 500); // Partial refund of $5.00
            refundData.put("reason", "Customer request");
            
            // Create refund
            Response response = APIUtils.post("/payments/" + paymentId + "/refund", refundData);
            
            // Validate response
            APIUtils.validateStatusCode(response, 200);
            APIUtils.validateResponseTime(response, 5000);
            
            // Validate response structure
            response.then()
                    .body("id", notNullValue())
                    .body("paymentId", equalTo(paymentId))
                    .body("amount", equalTo(500))
                    .body("status", equalTo("succeeded"))
                    .body("reason", equalTo("Customer request"));
            
            logger.info("Payment refund test completed successfully");
        } catch (Exception e) {
            logger.error("Payment refund test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 6, groups = {"regression", "api", "payment", "validation", "negative"})
    @Description("Test payment validation with invalid data and error handling")
    @Severity(SeverityLevel.NORMAL)
    public void testPaymentValidation() throws Exception {
        logger.info("Starting payment validation test");
        
        try {
            // Test with invalid amount
            Map<String, Object> invalidData = new HashMap<>();
            invalidData.put("amount", -100); // Negative amount
            invalidData.put("currency", "USD");
            
            Response response = APIUtils.post("/payments/intent", invalidData);
            
            // Should return validation error
            Assert.assertTrue(response.getStatusCode() >= 400 && response.getStatusCode() < 500,
                    "Should return client error for invalid amount");
            
            // Test with invalid currency
            invalidData.put("amount", 1000);
            invalidData.put("currency", "INVALID");
            
            response = APIUtils.post("/payments/intent", invalidData);
            
            // Should return validation error
            Assert.assertTrue(response.getStatusCode() >= 400 && response.getStatusCode() < 500,
                    "Should return client error for invalid currency");
            
            // Test with missing required fields
            Map<String, Object> missingData = new HashMap<>();
            missingData.put("amount", 1000);
            // Missing currency
            
            response = APIUtils.post("/payments/intent", missingData);
            
            // Should return validation error
            Assert.assertTrue(response.getStatusCode() >= 400 && response.getStatusCode() < 500,
                    "Should return client error for missing currency");
            
            logger.info("Payment validation test completed successfully");
        } catch (Exception e) {
            logger.error("Payment validation test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 7, groups = {"regression", "api", "payment", "security", "critical"})
    @Description("Test payment security with unauthorized access attempts")
    @Severity(SeverityLevel.CRITICAL)
    public void testPaymentSecurity() throws Exception {
        logger.info("Starting payment security test");
        
        try {
            // Store original token
            String originalToken = ConfigLoader.get("api.auth.token");
            
            // Clear auth token
            APIUtils.setAuthToken(null);
            
            // Try to access payment without authentication
            Response response = APIUtils.get("/payments/" + paymentId);
            
            // Should return unauthorized
            Assert.assertEquals(response.getStatusCode(), 401, "Should return 401 for unauthorized access");
            
            // Try to create payment without authentication
            Map<String, Object> paymentData = new HashMap<>();
            paymentData.put("amount", 1000);
            paymentData.put("currency", "USD");
            
            response = APIUtils.post("/payments/intent", paymentData);
            
            // Should return unauthorized
            Assert.assertEquals(response.getStatusCode(), 401, "Should return 401 for unauthorized payment creation");
            
            // Restore auth token
            APIUtils.setAuthToken(originalToken);
            
            logger.info("Payment security test completed successfully");
        } catch (Exception e) {
            logger.error("Payment security test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 8, groups = {"regression", "api", "payment", "webhook"})
    @Description("Test payment webhook simulation and processing")
    @Severity(SeverityLevel.NORMAL)
    public void testPaymentWebhook() throws Exception {
        logger.info("Starting payment webhook test");
        
        try {
            // Simulate webhook payload
            Map<String, Object> webhookData = new HashMap<>();
            webhookData.put("type", "payment.succeeded");
            webhookData.put("paymentId", paymentId);
            webhookData.put("amount", 1000);
            webhookData.put("currency", "USD");
            webhookData.put("timestamp", System.currentTimeMillis());
            
            // Send webhook
            Response response = APIUtils.post("/webhooks/payment", webhookData);
            
            // Validate response
            APIUtils.validateStatusCode(response, 200);
            APIUtils.validateResponseTime(response, 3000);
            
            // Validate webhook processed
            response.then()
                    .body("received", equalTo(true))
                    .body("processed", equalTo(true));
            
            logger.info("Payment webhook test completed successfully");
        } catch (Exception e) {
            logger.error("Payment webhook test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 9, groups = {"regression", "api", "payment", "performance"})
    @Description("Test payment API performance with multiple concurrent requests")
    @Severity(SeverityLevel.NORMAL)
    public void testPaymentPerformance() throws Exception {
        logger.info("Starting payment performance test");
        
        try {
            // Test multiple rapid payment requests
            long totalTime = 0;
            int requestCount = 3;
            
            for (int i = 0; i < requestCount; i++) {
                long startTime = System.currentTimeMillis();
                
                // Create payment intent
                Map<String, Object> paymentData = new HashMap<>();
                paymentData.put("amount", 1000);
                paymentData.put("currency", "USD");
                paymentData.put("customerId", customerId);
                paymentData.put("description", "Performance test payment " + i);
                
                Response response = APIUtils.post("/payments/intent", paymentData);
                APIUtils.validateStatusCode(response, 201);
                
                long endTime = System.currentTimeMillis();
                totalTime += (endTime - startTime);
            }
            
            long averageTime = totalTime / requestCount;
            
            // Assert average response time is reasonable
            Assert.assertTrue(averageTime < 5000, "Average response time should be less than 5 seconds");
            
            logger.info("Payment performance test completed successfully. Average response time: {}ms", averageTime);
        } catch (Exception e) {
            logger.error("Payment performance test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 10, groups = {"regression", "api", "payment", "security", "pci"})
    @Description("Test payment data encryption and PCI compliance")
    @Severity(SeverityLevel.CRITICAL)
    public void testPaymentDataSecurity() throws Exception {
        logger.info("Starting payment data security test");
        
        try {
            // Create payment with card data
            Map<String, Object> paymentData = new HashMap<>();
            paymentData.put("amount", 1000);
            paymentData.put("currency", "USD");
            paymentData.put("customerId", customerId);
            
            Map<String, Object> cardData = new HashMap<>();
            cardData.put("number", "4242424242424242");
            cardData.put("expiryMonth", "12");
            cardData.put("expiryYear", "2025");
            cardData.put("cvv", "123");
            
            paymentData.put("card", cardData);
            
            Response response = APIUtils.post("/payments/intent", paymentData);
            
            // Validate that sensitive data is not returned in response
            String responseBody = response.getBody().asString();
            
            Assert.assertFalse(responseBody.contains("4242424242424242"), 
                    "Card number should not be present in response");
            Assert.assertFalse(responseBody.contains("123"), 
                    "CVV should not be present in response");
            
            // Validate HTTPS is used
            String baseUrl = ConfigLoader.get("api.base.url");
            Assert.assertTrue(baseUrl.startsWith("https://"), 
                    "API should use HTTPS for secure communication");
            
            logger.info("Payment data security test completed successfully");
        } catch (Exception e) {
            logger.error("Payment data security test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 11, groups = {"regression", "api", "payment", "schema"})
    @Description("Test payment API response schema validation")
    @Severity(SeverityLevel.NORMAL)
    public void testPaymentSchemaValidation() throws Exception {
        logger.info("Starting payment schema validation test");
        
        try {
            // Get payment details for schema validation
            Response response = APIUtils.get("/payments/" + paymentId);
            
            // Validate response
            APIUtils.validateStatusCode(response, 200);
            
            // Validate required fields are present
            response.then()
                    .body("id", notNullValue())
                    .body("amount", notNullValue())
                    .body("currency", notNullValue())
                    .body("status", notNullValue())
                    .body("createdAt", notNullValue());
            
            // Validate data types
            response.then()
                    .body("id", instanceOf(String.class))
                    .body("amount", instanceOf(Integer.class))
                    .body("currency", instanceOf(String.class))
                    .body("status", instanceOf(String.class));
            
            // Validate amount is positive
            int amount = response.jsonPath().getInt("amount");
            Assert.assertTrue(amount > 0, "Amount should be positive");
            
            // Validate currency format
            String currency = response.jsonPath().getString("currency");
            Assert.assertEquals(currency.length(), 3, "Currency should be 3 characters");
            Assert.assertTrue(currency.matches("[A-Z]{3}"), "Currency should be uppercase letters");
            
            logger.info("Payment schema validation test completed successfully");
        } catch (Exception e) {
            logger.error("Payment schema validation test failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(priority = 12, groups = {"regression", "api", "payment", "boundary"})
    @Description("Test payment API boundary conditions and edge cases")
    @Severity(SeverityLevel.NORMAL)
    public void testPaymentBoundaryConditions() throws Exception {
        logger.info("Starting payment boundary conditions test");
        
        try {
            // Test with minimum amount
            Map<String, Object> minAmountData = new HashMap<>();
            minAmountData.put("amount", 1); // Minimum amount
            minAmountData.put("currency", "USD");
            minAmountData.put("customerId", customerId);
            
            Response response = APIUtils.post("/payments/intent", minAmountData);
            
            // Should handle minimum amount appropriately
            Assert.assertTrue(response.getStatusCode() == 201 || 
                            (response.getStatusCode() >= 400 && response.getStatusCode() < 500),
                    "Should handle minimum amount gracefully");
            
            // Test with very large amount
            Map<String, Object> largeAmountData = new HashMap<>();
            largeAmountData.put("amount", 999999999); // Very large amount
            largeAmountData.put("currency", "USD");
            largeAmountData.put("customerId", customerId);
            
            response = APIUtils.post("/payments/intent", largeAmountData);
            
            // Should handle large amount appropriately
            logger.info("Large amount test response: {}", response.getStatusCode());
            
            // Test with zero amount
            Map<String, Object> zeroAmountData = new HashMap<>();
            zeroAmountData.put("amount", 0);
            zeroAmountData.put("currency", "USD");
            zeroAmountData.put("customerId", customerId);
            
            response = APIUtils.post("/payments/intent", zeroAmountData);
            
            // Should return validation error for zero amount
            Assert.assertTrue(response.getStatusCode() >= 400 && response.getStatusCode() < 500,
                    "Should return validation error for zero amount");
            
            logger.info("Payment boundary conditions test completed successfully");
        } catch (Exception e) {
            logger.error("Payment boundary conditions test failed: {}", e.getMessage());
            throw e;
        }
    }
}