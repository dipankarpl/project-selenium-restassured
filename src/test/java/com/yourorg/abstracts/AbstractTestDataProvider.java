package com.yourorg.abstracts;

import com.yourorg.interfaces.ITestDataProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract base class for test data providers
 * Implements caching and common functionality
 */
public abstract class AbstractTestDataProvider implements ITestDataProvider {
    protected static final Logger logger = LogManager.getLogger(AbstractTestDataProvider.class);
    protected final Map<String, Object> cache = new ConcurrentHashMap<>();
    
    @Override
    public boolean isDataAvailable(String testName) {
        return cache.containsKey(testName) || checkDataSource(testName);
    }
    
    protected abstract boolean checkDataSource(String testName);
    
    protected void cacheData(String key, Object data) {
        cache.put(key, data);
        logger.debug("Data cached for key: {}", key);
    }
    
    protected Object getCachedData(String key) {
        return cache.get(key);
    }
    
    protected void clearCache() {
        cache.clear();
        logger.info("Cache cleared");
    }
}