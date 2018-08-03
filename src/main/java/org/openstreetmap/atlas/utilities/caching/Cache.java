package org.openstreetmap.atlas.utilities.caching;

import java.util.Optional;

public interface Cache<K, V>
{
    Optional<V> get(K key);
}
