package com.yourorg.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.javafaker.Faker;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentMethod {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("type")
    private String type; // credit_card, debit_card, paypal, bank_account
    
    @JsonProperty("provider")
    private String provider; // visa, mastercard, amex, etc.
    
    @JsonProperty("last4")
    private String last4;
    
    @JsonProperty("expiryMonth")
    private String expiryMonth;
    
    @JsonProperty("expiryYear")
    private String expiryYear;
    
    @JsonProperty("holderName")
    private String holderName;
    
    @JsonProperty("billingAddress")
    private Address billingAddress;
    
    @JsonProperty("isDefault")
    private Boolean isDefault;
    
    @JsonProperty("isVerified")
    private Boolean isVerified;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    // Default constructor
    public PaymentMethod() {}
    
    // Constructor with Faker data
    public PaymentMethod(Faker faker) {
        this.type = "credit_card";
        this.provider = faker.options().option("visa", "mastercard", "amex", "discover");
        this.last4 = faker.number().digits(4);
        this.expiryMonth = String.format("%02d", faker.number().numberBetween(1, 13));
        this.expiryYear = String.valueOf(faker.number().numberBetween(2024, 2030));
        this.holderName = faker.name().fullName();
        this.billingAddress = new Address(faker, "billing");
        this.isDefault = false;
        this.isVerified = true;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    
    public String getLast4() { return last4; }
    public void setLast4(String last4) { this.last4 = last4; }
    
    public String getExpiryMonth() { return expiryMonth; }
    public void setExpiryMonth(String expiryMonth) { this.expiryMonth = expiryMonth; }
    
    public String getExpiryYear() { return expiryYear; }
    public void setExpiryYear(String expiryYear) { this.expiryYear = expiryYear; }
    
    public String getHolderName() { return holderName; }
    public void setHolderName(String holderName) { this.holderName = holderName; }
    
    public Address getBillingAddress() { return billingAddress; }
    public void setBillingAddress(Address billingAddress) { this.billingAddress = billingAddress; }
    
    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }
    
    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    @Override
    public String toString() {
        return "PaymentMethod{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", provider='" + provider + '\'' +
                ", last4='" + last4 + '\'' +
                ", expiryMonth='" + expiryMonth + '\'' +
                ", expiryYear='" + expiryYear + '\'' +
                ", holderName='" + holderName + '\'' +
                ", isDefault=" + isDefault +
                ", isVerified=" + isVerified +
                '}';
    }
}