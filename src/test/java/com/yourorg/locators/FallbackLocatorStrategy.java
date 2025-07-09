package com.yourorg.locators;

import org.openqa.selenium.By;

import java.util.List;

/**
 * Fallback locator strategy using XPath and text-based locators
 */
public class FallbackLocatorStrategy implements LocatorStrategy {
    private final String elementName;
    private final String xpath;
    private final String textContent;
    private final String className;
    
    public FallbackLocatorStrategy(String elementName, String xpath, String textContent, String className) {
        this.elementName = elementName;
        this.xpath = xpath;
        this.textContent = textContent;
        this.className = className;
    }
    
    @Override
    public List<By> getLocators() {
        return List.of(
                By.xpath(xpath),
                By.xpath("//*[contains(text(), '" + textContent + "')]"),
                By.className(className),
                By.xpath("//*[@class='" + className + "']")
        );
    }
    
    @Override
    public String getDescription() {
        return "Fallback strategy for " + elementName + " using XPath and text";
    }
    
    @Override
    public int getPriority() {
        return 2;
    }
}