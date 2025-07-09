package com.yourorg.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Advanced string manipulation utilities using lambdas and streams
 */
public class AdvancedStringUtils {
    private static final Logger logger = LogManager.getLogger(AdvancedStringUtils.class);
    
    // Functional interfaces for string operations
    @FunctionalInterface
    public interface StringTransformer extends Function<String, String> {}
    
    @FunctionalInterface
    public interface StringValidator extends Predicate<String> {}
    
    /**
     * Transform strings using lambda expressions
     */
    public static List<String> transformStrings(List<String> strings, StringTransformer transformer) {
        return strings.stream()
                .filter(Objects::nonNull)
                .map(transformer)
                .collect(Collectors.toList());
    }
    
    /**
     * Filter strings using predicates and lambda expressions
     */
    public static List<String> filterStrings(List<String> strings, StringValidator validator) {
        return strings.stream()
                .filter(Objects::nonNull)
                .filter(validator)
                .collect(Collectors.toList());
    }
    
    /**
     * Extract email addresses from text using regex and streams
     */
    public static List<String> extractEmails(String text) {
        Pattern emailPattern = Pattern.compile(
                "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b"
        );
        
        return emailPattern.matcher(text)
                .results()
                .map(java.util.regex.MatchResult::group)
                .distinct()
                .collect(Collectors.toList());
    }
    
    /**
     * Extract phone numbers using regex patterns
     */
    public static List<String> extractPhoneNumbers(String text) {
        List<Pattern> phonePatterns = List.of(
                Pattern.compile("\\(\\d{3}\\)\\s*\\d{3}-\\d{4}"),  // (123) 456-7890
                Pattern.compile("\\d{3}-\\d{3}-\\d{4}"),           // 123-456-7890
                Pattern.compile("\\d{3}\\.\\d{3}\\.\\d{4}"),       // 123.456.7890
                Pattern.compile("\\d{10}")                        // 1234567890
        );
        
        return phonePatterns.stream()
                .flatMap(pattern -> pattern.matcher(text).results())
                .map(java.util.regex.MatchResult::group)
                .distinct()
                .collect(Collectors.toList());
    }
    
    /**
     * Clean and normalize strings using method chaining
     */
    public static String cleanString(String input) {
        return Optional.ofNullable(input)
                .map(String::trim)
                .map(s -> s.replaceAll("\\s+", " "))
                .map(s -> s.replaceAll("[^\\w\\s]", ""))
                .orElse("");
    }
    
    /**
     * Generate random strings with specific patterns
     */
    public static String generateRandomString(int length, boolean includeNumbers, 
                                            boolean includeSpecialChars) {
        StringBuilder charsBuilder = new StringBuilder("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
        if (includeNumbers) {
            charsBuilder.append("0123456789");
        }
        if (includeSpecialChars) {
            charsBuilder.append("!@#$%^&*()_+-=");
        }
        String chars = charsBuilder.toString();
        
        Random random = new Random();
        
        return IntStream.range(0, length)
                .mapToObj(i -> String.valueOf(chars.charAt(random.nextInt(chars.length()))))
                .collect(Collectors.joining());
    }
    
    /**
     * Validate strings using multiple criteria with ternary operators
     */
    public static boolean isValidInput(String input, int minLength, int maxLength, 
                                     boolean requireNumbers, boolean requireSpecialChars) {
        return input != null 
                ? input.length() >= minLength && input.length() <= maxLength
                  && (!requireNumbers || input.matches(".*\\d.*"))
                  && (!requireSpecialChars || input.matches(".*[!@#$%^&*()_+\\-=].*"))
                : false;
    }
    
    /**
     * Parse CSV data using streams
     */
    public static List<Map<String, String>> parseCsvData(String csvContent) {
        String[] lines = csvContent.split("\n");
        
        if (lines.length < 2) {
            return new ArrayList<>();
        }
        
        String[] headers = lines[0].split(",");
        
        return Arrays.stream(lines)
                .skip(1)
                .map(line -> line.split(","))
                .filter(values -> values.length == headers.length)
                .map(values -> IntStream.range(0, headers.length)
                        .boxed()
                        .collect(Collectors.toMap(
                                i -> headers[i].trim(),
                                i -> values[i].trim(),
                                (existing, replacement) -> replacement,
                                LinkedHashMap::new
                        )))
                .collect(Collectors.toList());
    }
    
    /**
     * Mask sensitive data in strings
     */
    public static String maskSensitiveData(String input, String pattern, char maskChar) {
        if (input == null || pattern == null) {
            return input;
        }
        
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(input);
        
        StringBuffer masked = new StringBuffer();
        while (matcher.find()) {
            String match = matcher.group();
            String replacement = match.length() > 4 
                    ? match.substring(0, 2) + String.valueOf(maskChar).repeat(match.length() - 4) + match.substring(match.length() - 2)
                    : String.valueOf(maskChar).repeat(match.length());
            matcher.appendReplacement(masked, replacement);
        }
        matcher.appendTail(masked);
        
        return masked.toString();
    }
    
    /**
     * Calculate string similarity using Levenshtein distance
     */
    public static double calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return 0.0;
        }
        
        int maxLength = Math.max(s1.length(), s2.length());
        if (maxLength == 0) {
            return 1.0;
        }
        
        int distance = levenshteinDistance(s1, s2);
        return (maxLength - distance) / (double) maxLength;
    }
    
    private static int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
    
    /**
     * Extract and validate URLs from text
     */
    public static List<String> extractValidUrls(String text) {
        Pattern urlPattern = Pattern.compile(
                "https?://[\\w\\-]+(\\.[\\w\\-]+)+([\\w\\-\\.,@?^=%&:/~\\+#]*[\\w\\-\\@?^=%&/~\\+#])?"
        );
        
        return urlPattern.matcher(text)
                .results()
                .map(java.util.regex.MatchResult::group)
                .filter(url -> isValidUrl(url))
                .distinct()
                .collect(Collectors.toList());
    }
    
    private static boolean isValidUrl(String url) {
        try {
            new java.net.URL(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Convert camelCase to snake_case using regex
     */
    public static String camelToSnakeCase(String camelCase) {
        return Optional.ofNullable(camelCase)
                .map(s -> s.replaceAll("([a-z])([A-Z])", "$1_$2"))
                .map(String::toLowerCase)
                .orElse("");
    }
    
    /**
     * Convert snake_case to camelCase
     */
    public static String snakeToCamelCase(String snakeCase) {
        return Optional.ofNullable(snakeCase)
                .map(s -> Arrays.stream(s.split("_"))
                        .map(String::toLowerCase)
                        .reduce("", (acc, word) -> acc.isEmpty() 
                                ? word 
                                : acc + word.substring(0, 1).toUpperCase() + word.substring(1)))
                .orElse("");
    }
    
    /**
     * Count word frequency using streams and collectors
     */
    public static Map<String, Long> getWordFrequency(String text) {
        return Optional.ofNullable(text)
                .map(String::toLowerCase)
                .map(s -> s.replaceAll("[^a-zA-Z\\s]", ""))
                .map(s -> Arrays.stream(s.split("\\s+")))
                .orElse(Arrays.stream(new String[0]))
                .filter(word -> !word.isEmpty())
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()
                ));
    }
}