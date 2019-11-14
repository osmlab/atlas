package org.openstreetmap.atlas.geography.atlas.dsl.util

/**
 * A map with one and only one entry.
 *
 * @author Yazad Khambata
 */
class SingletonMap<K, V> implements Map<K, V> {

    @Delegate
    private Map<K, V> map

    SingletonMap(K k, V v) {
        this(immutableMap(k, v))
    }

    SingletonMap(final Map<K, V> map) {
        super()

        Valid.isTrue map.size() == 1, "Singleton Maps must have one and only one entry."

        this.map = Collections.unmodifiableMap(map)
    }

    private static <K, V> Map<K, V> immutableMap(final K k, final V v) {
        final Map<K, V> m = new HashMap<>()
        m.put(k, v)

        Collections.unmodifiableMap(m)
    }

    V put(K key, V value) {
        throw new UnsupportedOperationException()
    }

    @Override
    void clear() {
        throw new UnsupportedOperationException()
    }

    @Override
    void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException()
    }

    K getKey() {
        map.keySet().iterator().next()
    }

    V getValue() {
        map.values().iterator().next()
    }

    Map.Entry<K, V> getEntry() {
        map.entrySet().iterator().next()
    }
}
