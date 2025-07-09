package com.yourorg.locators;

import org.openqa.selenium.By;

import java.util.List;

/**
 * Primary locator strategy using ID and CSS selectors
 */
public class PrimaryLocatorStrategy implements LocatorStrategy {
    private final String elementName;
    private final String id;
    private final String cssSelector;
    
    public PrimaryLocatorStrategy(String elementName, String id, String cssSelector) {
        this.elementName = elementName;
        this.id = id;
        this.cssSelector = cssSelector;
    }
    
    @Override
    public List<By> getLocators() {
        return List.of(
                By.id(id),
                By.cssSelector(cssSelector)
        );
    }
    
    @Override
    public String getDescription() {
        return "Primary strategy for " + elementName + " using ID and CSS";
    }
    
    @Override
    public int getPriority() {
        return 1;
    }
}