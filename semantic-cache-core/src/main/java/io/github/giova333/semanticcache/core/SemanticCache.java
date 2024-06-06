package io.github.giova333.semanticcache.core;

import java.time.Duration;
import java.util.Optional;

/**
 * The SemanticCache interface defines the operations for a semantic caching system.
 * The cache stores key-value pairs, where the keys are natural language queries and the values are the corresponding answers.
 *
 * <p>This caching system is unique in that it allows for retrieval of values based on semantic similarity rather than exact key match.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 *     SemanticCache cache = ... // obtain an instance of SemanticCache
 *     cache.set("largest city in USA by population", "New York"); // store a value
 *     Optional value = cache.get("which is the most populated city in the USA?"); // retrieve "New York"
 * </pre>
 */
public interface SemanticCache {

    /**
     * Stores a value in the cache.
     *
     * @param key the key under which the value is to be stored
     * @param value the value to be stored
     */
    void set(String key, String value);

    /**
     * Stores a value in the cache with a specified time-to-live (TTL).
     * After the TTL has passed, the value is automatically removed from the cache.
     *
     * @param key the key under which the value is to be stored
     * @param value the value to be stored
     * @param ttl the time-to-live duration for the value
     */
    void set(String key, String value, Duration ttl);

    /**
     * Retrieves a value from the cache by performing a similarity search.
     * The top matching value that meets the {@link SemanticCacheProperties#getSimilarityThreshold()} threshold is returned.
     *
     * @param key the key of the value to be retrieved
     * @return an Optional containing the value if it exists, or an empty Optional if the key does not exist in the cache
     */
    Optional<String> get(String key);
}
