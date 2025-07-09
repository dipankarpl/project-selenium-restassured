package com.yourorg.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StateRetentionManager {
    private static final Logger logger = LogManager.getLogger(StateRetentionManager.class);
    private static final Map<String, Map<String, Object>> sessionState = new ConcurrentHashMap<>();
    private static final Map<String, Object> globalState = new ConcurrentHashMap<>();

    public static void setSessionState(String sessionId, String key, Object value) {
        sessionState.computeIfAbsent(sessionId, k -> new HashMap<>()).put(key, value);
        logger.debug("Session state set - Session: {}, Key: {}", sessionId, key);
    }

    public static Object getSessionState(String sessionId, String key) {
        Map<String, Object> session = sessionState.get(sessionId);
        if (session != null) {
            return session.get(key);
        }
        return null;
    }

    public static void setGlobalState(String key, Object value) {
        globalState.put(key, value);
        logger.debug("Global state set - Key: {}", key);
    }

    public static Object getGlobalState(String key) {
        return globalState.get(key);
    }

    public static void clearSessionState(String sessionId) {
        sessionState.remove(sessionId);
        logger.debug("Session state cleared - Session: {}", sessionId);
    }

    public static void clearGlobalState() {
        globalState.clear();
        logger.debug("Global state cleared");
    }

    public static void clearAllStates() {
        sessionState.clear();
        globalState.clear();
        logger.debug("All states cleared");
    }

    public static Map<String, Object> getSessionStateMap(String sessionId) {
        return sessionState.get(sessionId);
    }

    public static Map<String, Object> getGlobalStateMap() {
        return new HashMap<>(globalState);
    }

    public static boolean hasSessionState(String sessionId, String key) {
        Map<String, Object> session = sessionState.get(sessionId);
        return session != null && session.containsKey(key);
    }

    public static boolean hasGlobalState(String key) {
        return globalState.containsKey(key);
    }

    public static void logCurrentState() {
        logger.info("Current Global State: {}", globalState);
        logger.info("Current Session States: {}", sessionState);
    }
}