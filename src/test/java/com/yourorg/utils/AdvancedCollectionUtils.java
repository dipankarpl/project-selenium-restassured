package com.yourorg.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Advanced collection utilities demonstrating streams, lambdas, and functional programming
 */
public class AdvancedCollectionUtils {
    private static final Logger logger = LogManager.getLogger(AdvancedCollectionUtils.class);
    
    /**
     * Filter and transform collections using streams and lambdas
     */
    public static <T, R> List<R> filterAndTransform(Collection<T> collection, 
                                                   Predicate<T> filter, 
                                                   Function<T, R> transformer) {
        return collection.stream()
                .filter(Objects::nonNull)
                .filter(filter)
                .map(transformer)
                .collect(Collectors.toList());
    }
    
    /**
     * Group elements by a classifier function
     */
    public static <T, K> Map<K, List<T>> groupBy(Collection<T> collection, 
                                                Function<T, K> classifier) {
        return collection.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(classifier));
    }
    
    /**
     * Partition collection into two groups based on predicate
     */
    public static <T> Map<Boolean, List<T>> partition(Collection<T> collection, 
                                                     Predicate<T> predicate) {
        return collection.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.partitioningBy(predicate));
    }
    
    /**
     * Find duplicates in a collection using streams
     */
    public static <T> Set<T> findDuplicates(Collection<T> collection) {
        Set<T> seen = new HashSet<>();
        return collection.stream()
                .filter(Objects::nonNull)
                .filter(item -> !seen.add(item))
                .collect(Collectors.toSet());
    }
    
    /**
     * Remove duplicates while preserving order
     */
    public static <T> List<T> removeDuplicates(List<T> list) {
        return list.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }
    
    /**
     * Merge multiple maps with conflict resolution
     */
    public static <K, V> Map<K, V> mergeMaps(List<Map<K, V>> maps, 
                                           Function<List<V>, V> conflictResolver) {
        return maps.stream()
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> conflictResolver.apply(entry.getValue())
                ));
    }
    
    /**
     * Create a frequency map of elements
     */
    public static <T> Map<T, Long> createFrequencyMap(Collection<T> collection) {
        return collection.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()
                ));
    }
    
    /**
     * Find the most frequent element
     */
    public static <T> Optional<T> findMostFrequent(Collection<T> collection) {
        return createFrequencyMap(collection)
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }
    
    /**
     * Flatten nested collections
     */
    public static <T> List<T> flatten(Collection<? extends Collection<T>> nestedCollections) {
        return nestedCollections.stream()
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    /**
     * Create sliding windows of specified size
     */
    public static <T> List<List<T>> createSlidingWindows(List<T> list, int windowSize) {
        if (windowSize <= 0 || windowSize > list.size()) {
            return Collections.emptyList();
        }
        
        return IntStream.rangeClosed(0, list.size() - windowSize)
                .mapToObj(start -> list.subList(start, start + windowSize))
                .collect(Collectors.toList());
    }
    
    /**
     * Chunk collection into smaller lists of specified size
     */
    public static <T> List<List<T>> chunk(List<T> list, int chunkSize) {
        if (chunkSize <= 0) {
            return Collections.emptyList();
        }
        
        return IntStream.range(0, (list.size() + chunkSize - 1) / chunkSize)
                .mapToObj(i -> list.subList(
                        i * chunkSize, 
                        Math.min((i + 1) * chunkSize, list.size())
                ))
                .collect(Collectors.toList());
    }
    
    /**
     * Zip two lists together
     */
    public static <T, U, R> List<R> zip(List<T> list1, List<U> list2, 
                                       Function<T, Function<U, R>> zipper) {
        int minSize = Math.min(list1.size(), list2.size());
        
        return IntStream.range(0, minSize)
                .mapToObj(i -> zipper.apply(list1.get(i)).apply(list2.get(i)))
                .collect(Collectors.toList());
    }
    
    /**
     * Create Cartesian product of two collections
     */
    public static <T, U> List<Map.Entry<T, U>> cartesianProduct(Collection<T> collection1, 
                                                               Collection<U> collection2) {
        return collection1.stream()
                .flatMap(item1 -> collection2.stream()
                        .map(item2 -> new AbstractMap.SimpleEntry<>(item1, item2)))
                .collect(Collectors.toList());
    }
    
    /**
     * Rotate list elements by specified positions
     */
    public static <T> List<T> rotate(List<T> list, int positions) {
        if (list.isEmpty()) {
            return new ArrayList<>(list);
        }
        
        int size = list.size();
        int normalizedPositions = ((positions % size) + size) % size;
        
        return IntStream.range(0, size)
                .mapToObj(i -> list.get((i + normalizedPositions) % size))
                .collect(Collectors.toList());
    }
    
    /**
     * Find intersection of multiple sets
     */
    @SafeVarargs
    public static <T> Set<T> intersection(Set<T>... sets) {
        return Arrays.stream(sets)
                .filter(Objects::nonNull)
                .filter(set -> !set.isEmpty())
                .reduce((set1, set2) -> {
                    Set<T> result = new HashSet<>(set1);
                    result.retainAll(set2);
                    return result;
                })
                .orElse(new HashSet<>());
    }
    
    /**
     * Find union of multiple sets
     */
    @SafeVarargs
    public static <T> Set<T> union(Set<T>... sets) {
        return Arrays.stream(sets)
                .filter(Objects::nonNull)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }
    
    /**
     * Find difference between two sets
     */
    public static <T> Set<T> difference(Set<T> set1, Set<T> set2) {
        return set1.stream()
                .filter(item -> !set2.contains(item))
                .collect(Collectors.toSet());
    }
    
    /**
     * Create a map from a list using key and value extractors
     */
    public static <T, K, V> Map<K, V> toMap(Collection<T> collection,
                                           Function<T, K> keyExtractor,
                                           Function<T, V> valueExtractor) {
        return collection.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        keyExtractor,
                        valueExtractor,
                        (existing, replacement) -> replacement,
                        LinkedHashMap::new
                ));
    }
    
    /**
     * Sort collection by multiple criteria
     */
    public static <T> List<T> sortByMultipleCriteria(Collection<T> collection,
                                                    List<Function<T, Comparable>> extractors) {
        Comparator<T> comparator = extractors.stream()
                .map(extractor -> Comparator.comparing(extractor, Comparator.nullsLast(Comparator.naturalOrder())))
                .reduce(Comparator::thenComparing)
                .orElse((a, b) -> 0);
        
        return collection.stream()
                .filter(Objects::nonNull)
                .sorted(comparator)
                .collect(Collectors.toList());
    }
    
    /**
     * Sample random elements from collection
     */
    public static <T> List<T> randomSample(Collection<T> collection, int sampleSize) {
        List<T> list = new ArrayList<>(collection);
        Collections.shuffle(list);
        
        return list.stream()
                .limit(Math.min(sampleSize, list.size()))
                .collect(Collectors.toList());
    }
    
    /**
     * Check if collection contains all elements from another collection
     */
    public static <T> boolean containsAll(Collection<T> collection, Collection<T> elements) {
        return elements.stream()
                .allMatch(collection::contains);
    }
    
    /**
     * Check if collection contains any element from another collection
     */
    public static <T> boolean containsAny(Collection<T> collection, Collection<T> elements) {
        return elements.stream()
                .anyMatch(collection::contains);
    }
}