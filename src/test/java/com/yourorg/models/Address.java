package com.yourorg.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.javafaker.Faker;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Address {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("type")
    private String type; // shipping, billing
    
    @JsonProperty("firstName")
    private String firstName;
    
    @JsonProperty("lastName")
    private String lastName;
    
    @JsonProperty("company")
    private String company;
    
    @JsonProperty("street")
    private String street;
    
    @JsonProperty("street2")
    private String street2;
    
    @JsonProperty("city")
    private String city;
    
    @JsonProperty("state")
    private String state;
    
    @JsonProperty("zipCode")
    private String zipCode;
    
    @JsonProperty("country")
    private String country;
    
    @JsonProperty("isDefault")
    private Boolean isDefault;
    
    // Default constructor
    public Address() {}
    
    // Constructor with Faker data
    public Address(Faker faker, String type) {
        this.type = type;
        this.firstName = faker.name().firstName();
        this.lastName = faker.name().lastName();
        this.company = faker.company().name();
        this.street = faker.address().streetAddress();
        this.street2 = faker.address().secondaryAddress();
        this.city = faker.address().city();
        this.state = faker.address().stateAbbr();
        this.zipCode = faker.address().zipCode();
        this.country = faker.address().countryCode();
        this.isDefault = false;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
    
    public String getStreet2() { return street2; }
    public void setStreet2(String street2) { this.street2 = street2; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    
    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }
    
    @Override
    public String toString() {
        return "Address{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", street='" + street + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", zipCode='" + zipCode + '\'' +
                ", country='" + country + '\'' +
                '}';
    }
}