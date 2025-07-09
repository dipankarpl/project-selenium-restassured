package com.yourorg.utils;

import com.github.javafaker.Faker;
import com.yourorg.models.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.util.*;

public class FakerDataGenerator {
    private static final Logger logger = LogManager.getLogger(FakerDataGenerator.class);
    private static final Faker faker = new Faker();
    
    /**
     * Generate a random user with Faker data
     */
    public static User generateUser() {
        return new User(faker);
    }
    
    /**
     * Generate a user with specific role
     */
    public static User generateUserWithRole(String role) {
        User user = new User(faker);
        user.setRole(role);
        
        // Set role-specific permissions
        switch (role.toLowerCase()) {
            case "admin":
                user.setPermissions(Arrays.asList("admin", "manage_users", "manage_orders", "view_analytics"));
                break;
            case "vendor":
                user.setPermissions(Arrays.asList("manage_products", "view_sales", "manage_inventory"));
                break;
            case "moderator":
                user.setPermissions(Arrays.asList("moderate_content", "manage_reviews", "view_reports"));
                break;
            default:
                user.setPermissions(Arrays.asList("place_orders", "view_products", "manage_profile"));
        }
        
        return user;
    }
    
    /**
     * Generate user with custom profile data
     */
    public static User generateUserWithProfile(Map<String, Object> profileData) {
        User user = new User(faker);
        
        // Override with custom profile data
        if (profileData.containsKey("firstName")) {
            user.setFirstName((String) profileData.get("firstName"));
        }
        if (profileData.containsKey("lastName")) {
            user.setLastName((String) profileData.get("lastName"));
        }
        if (profileData.containsKey("email")) {
            user.setEmail((String) profileData.get("email"));
        }
        if (profileData.containsKey("phone")) {
            user.setPhone((String) profileData.get("phone"));
        }
        if (profileData.containsKey("dateOfBirth")) {
            user.setDateOfBirth((String) profileData.get("dateOfBirth"));
        }
        
        return user;
    }
    
    /**
     * Generate a random address
     */
    public static Address generateAddress(String type) {
        return new Address(faker, type);
    }
    
    /**
     * Generate a random payment method
     */
    public static PaymentMethod generatePaymentMethod() {
        return new PaymentMethod(faker);
    }
    
    /**
     * Generate a random product
     */
    public static Product generateProduct() {
        return new Product(faker);
    }
    
    /**
     * Generate product with specific category
     */
    public static Product generateProductWithCategory(String category) {
        Product product = new Product(faker);
        product.setCategory(category);
        return product;
    }
    
    /**
     * Generate an order with random items
     */
    public static Order generateOrder(String userId) {
        Order order = new Order();
        order.setOrderNumber(faker.code().ean13());
        order.setUserId(userId);
        order.setStatus("pending");
        order.setCurrency("USD");
        order.setShippingAddress(generateAddress("shipping"));
        order.setBillingAddress(generateAddress("billing"));
        order.setPaymentMethod(generatePaymentMethod());
        order.setNotes(faker.lorem().sentence());
        
        // Generate random order items
        List<OrderItem> items = new ArrayList<>();
        int itemCount = faker.number().numberBetween(1, 5);
        
        BigDecimal subtotal = BigDecimal.ZERO;
        for (int i = 0; i < itemCount; i++) {
            Product product = generateProduct();
            int quantity = faker.number().numberBetween(1, 3);
            
            OrderItem item = new OrderItem(
                product.getId(),
                product.getName(),
                product.getSku(),
                quantity,
                product.getPrice(),
                "USD"
            );
            
            items.add(item);
            subtotal = subtotal.add(item.getTotalPrice());
        }
        
        order.setItems(items);
        order.setSubtotal(subtotal);
        
        // Calculate tax and shipping
        BigDecimal tax = subtotal.multiply(new BigDecimal("0.08")); // 8% tax
        BigDecimal shipping = new BigDecimal("9.99");
        BigDecimal total = subtotal.add(tax).add(shipping);
        
        order.setTax(tax);
        order.setShipping(shipping);
        order.setTotal(total);
        
        return order;
    }
    
    /**
     * Generate test data for forms (UI testing)
     */
    public static Map<String, String> generateFormData(String formType) {
        Map<String, String> formData = new HashMap<>();
        
        switch (formType.toLowerCase()) {
            case "registration":
                formData.put("firstName", faker.name().firstName());
                formData.put("lastName", faker.name().lastName());
                formData.put("email", faker.internet().emailAddress());
                formData.put("username", faker.name().username());
                formData.put("password", faker.internet().password(8, 16, true, true, true));
                formData.put("confirmPassword", formData.get("password"));
                formData.put("phone", faker.phoneNumber().phoneNumber());
                formData.put("dateOfBirth", faker.date().birthday().toString());
                break;
                
            case "address":
                formData.put("firstName", faker.name().firstName());
                formData.put("lastName", faker.name().lastName());
                formData.put("company", faker.company().name());
                formData.put("street", faker.address().streetAddress());
                formData.put("street2", faker.address().secondaryAddress());
                formData.put("city", faker.address().city());
                formData.put("state", faker.address().stateAbbr());
                formData.put("zipCode", faker.address().zipCode());
                formData.put("country", faker.address().countryCode());
                break;
                
            case "payment":
                formData.put("cardNumber", "4242424242424242"); // Test card number
                formData.put("expiryMonth", String.format("%02d", faker.number().numberBetween(1, 13)));
                formData.put("expiryYear", String.valueOf(faker.number().numberBetween(2024, 2030)));
                formData.put("cvv", faker.number().digits(3));
                formData.put("holderName", faker.name().fullName());
                break;
                
            case "contact":
                formData.put("name", faker.name().fullName());
                formData.put("email", faker.internet().emailAddress());
                formData.put("phone", faker.phoneNumber().phoneNumber());
                formData.put("subject", faker.lorem().sentence());
                formData.put("message", faker.lorem().paragraph());
                break;
                
            default:
                logger.warn("Unknown form type: {}", formType);
        }
        
        return formData;
    }
    
    /**
     * Generate malicious/security test data
     */
    public static Map<String, String> generateSecurityTestData() {
        Map<String, String> testData = new HashMap<>();
        
        // SQL Injection payloads
        testData.put("sqlInjection1", "' OR '1'='1");
        testData.put("sqlInjection2", "'; DROP TABLE users; --");
        testData.put("sqlInjection3", "1' UNION SELECT * FROM users --");
        testData.put("sqlInjection4", "admin'--");
        testData.put("sqlInjection5", "' OR 1=1#");
        
        // XSS payloads
        testData.put("xss1", "<script>alert('XSS')</script>");
        testData.put("xss2", "javascript:alert('XSS')");
        testData.put("xss3", "<img src=x onerror=alert('XSS')>");
        testData.put("xss4", "<svg onload=alert('XSS')>");
        testData.put("xss5", "';alert('XSS');//");
        
        // Path traversal
        testData.put("pathTraversal1", "../../../etc/passwd");
        testData.put("pathTraversal2", "..\\..\\..\\windows\\system32\\drivers\\etc\\hosts");
        testData.put("pathTraversal3", "....//....//....//etc/passwd");
        
        // Command injection
        testData.put("cmdInjection1", "; ls -la");
        testData.put("cmdInjection2", "| whoami");
        testData.put("cmdInjection3", "& dir");
        testData.put("cmdInjection4", "`id`");
        
        // LDAP injection
        testData.put("ldapInjection1", "*)(uid=*))(|(uid=*");
        testData.put("ldapInjection2", "*)(|(password=*)");
        
        // NoSQL injection
        testData.put("nosqlInjection1", "'; return true; var dummy='");
        testData.put("nosqlInjection2", "{\"$ne\": null}");
        
        return testData;
    }
    
    /**
     * Generate boundary test data
     */
    public static Map<String, String> generateBoundaryTestData() {
        Map<String, String> testData = new HashMap<>();
        
        // Empty values
        testData.put("empty", "");
        testData.put("null", null);
        testData.put("whitespace", "   ");
        
        // Very long strings
        testData.put("longString", "a".repeat(1000));
        testData.put("veryLongString", "b".repeat(10000));
        
        // Special characters
        testData.put("specialChars", "!@#$%^&*()_+-=[]{}|;':\",./<>?");
        testData.put("unicode", "测试数据 🚀 ñáéíóú");
        testData.put("emoji", "😀😃😄😁😆😅😂🤣");
        
        // Numeric boundaries
        testData.put("maxInt", String.valueOf(Integer.MAX_VALUE));
        testData.put("minInt", String.valueOf(Integer.MIN_VALUE));
        testData.put("zero", "0");
        testData.put("negative", "-1");
        testData.put("decimal", "123.456");
        
        // Date boundaries
        testData.put("futureDate", "2099-12-31");
        testData.put("pastDate", "1900-01-01");
        testData.put("invalidDate", "2023-02-30");
        
        return testData;
    }
    
    /**
     * Generate performance test data
     */
    public static List<User> generateBulkUsers(int count) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            users.add(generateUser());
        }
        return users;
    }
    
    /**
     * Generate performance test data for products
     */
    public static List<Product> generateBulkProducts(int count) {
        List<Product> products = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            products.add(generateProduct());
        }
        return products;
    }
    
    /**
     * Get Faker instance for custom data generation
     */
    public static Faker getFaker() {
        return faker;
    }
}