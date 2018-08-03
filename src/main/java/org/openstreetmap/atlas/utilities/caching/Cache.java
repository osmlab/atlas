package org.openstreetmap.atlas.utilities.caching;

import java.util.Optional;

/**
 * The basic interface with which all caches must comply.
 * 
 * @author lcram
 * @param <K>
 *            the type of the cache key
 * @param <V>
 *            the type of the cache value
 */
public interface Cache<K, V>
{
    /**
     * Get a value from the cache, given some key.
     * 
     * @param key
     *            the key
     * @return the value wrapped in an {@link Optional}
     */
    Optional<V> get(K key);
}
