package com.yourorg.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.javafaker.Faker;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Product {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("sku")
    private String sku;
    
    @JsonProperty("price")
    private BigDecimal price;
    
    @JsonProperty("salePrice")
    private BigDecimal salePrice;
    
    @JsonProperty("currency")
    private String currency;
    
    @JsonProperty("category")
    private String category;
    
    @JsonProperty("subcategory")
    private String subcategory;
    
    @JsonProperty("brand")
    private String brand;
    
    @JsonProperty("tags")
    private List<String> tags;
    
    @JsonProperty("images")
    private List<String> images;
    
    @JsonProperty("attributes")
    private Map<String, Object> attributes;
    
    @JsonProperty("inventory")
    private Integer inventory;
    
    @JsonProperty("isActive")
    private Boolean isActive;
    
    @JsonProperty("isFeatured")
    private Boolean isFeatured;
    
    @JsonProperty("rating")
    private Double rating;
    
    @JsonProperty("reviewCount")
    private Integer reviewCount;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
    
    // Default constructor
    public Product() {}
    
    // Constructor with Faker data
    public Product(Faker faker) {
        this.name = faker.commerce().productName();
        this.description = faker.lorem().paragraph();
        this.sku = faker.code().ean13();
        this.price = new BigDecimal(faker.commerce().price().replace(",", ""));
        this.currency = "USD";
        this.category = faker.commerce().department();
        this.brand = faker.company().name();
        this.inventory = faker.number().numberBetween(0, 1000);
        this.isActive = true;
        this.isFeatured = faker.bool().bool();
        this.rating = faker.number().randomDouble(1, 1, 5);
        this.reviewCount = faker.number().numberBetween(0, 500);
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public BigDecimal getSalePrice() { return salePrice; }
    public void setSalePrice(BigDecimal salePrice) { this.salePrice = salePrice; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getSubcategory() { return subcategory; }
    public void setSubcategory(String subcategory) { this.subcategory = subcategory; }
    
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
    
    public Map<String, Object> getAttributes() { return attributes; }
    public void setAttributes(Map<String, Object> attributes) { this.attributes = attributes; }
    
    public Integer getInventory() { return inventory; }
    public void setInventory(Integer inventory) { this.inventory = inventory; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public Boolean getIsFeatured() { return isFeatured; }
    public void setIsFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; }
    
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    
    public Integer getReviewCount() { return reviewCount; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", sku='" + sku + '\'' +
                ", price=" + price +
                ", category='" + category + '\'' +
                ", brand='" + brand + '\'' +
                ", inventory=" + inventory +
                ", isActive=" + isActive +
                ", rating=" + rating +
                '}';
    }
}