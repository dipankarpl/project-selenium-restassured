package com.yourorg.interfaces;

import org.openqa.selenium.WebDriver;

/**
 * Base interface for all page objects
 * Ensures consistent page object implementation
 */
public interface IPageObject {
    void waitForPageToLoad() throws Exception;
    boolean isPageLoaded();
    String getPageTitle();
    String getCurrentUrl();
    void refreshPage();
}