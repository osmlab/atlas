package org.openstreetmap.atlas.utilities.maps;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Objects;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasSerializer;
import org.openstreetmap.atlas.proto.adapters.ProtoAdapter;
import org.openstreetmap.atlas.utilities.arrays.Arrays;
import org.openstreetmap.atlas.utilities.arrays.LargeArray;
import org.openstreetmap.atlas.utilities.arrays.LongArrayOfArrays;
import org.openstreetmap.atlas.utilities.arrays.PrimitiveArray;
import org.openstreetmap.atlas.utilities.scalars.Ratio;

/**
 * Large map that is based on {@link LargeArray}s.
 *
 * @author matthieun
 * @author lcram
 * @param <K>
 *            The key type
 * @param <V>
 *            The value type
 */
public abstract class LargeMap<K, V> implements Iterable<K>, Serializable
{
    private static final long serialVersionUID = -6051549542042379037L;

    protected static final int DEFAULT_HASH_MODULO_RATIO = 10;

    private final LargeArray<V> values;
    private final LargeArray<K> keys;
    private final int hashSize;
    private final long maximumSize;
    private final String name;

    // Each index of this list is a hash value (modulo-ed)
    // Each Value is an array of indices to look up the items in the key/value arrays
    private final LongArrayOfArrays hashes;

    /**
     * This nullary constructor exists solely for subclasses of {@link LargeMap} that wish to
     * implement their own nullary constructor. These nullary constructors should only be used by
     * serialization code in {@link PackedAtlasSerializer} that needs to obtain
     * {@link ProtoAdapter}s. The objects they initialize are corrupted for general use and should
     * be discarded.
     */
    protected LargeMap()
    {
        this.values = null;
        this.keys = null;
        this.hashSize = 0;
        this.maximumSize = 0;
        this.name = null;
        this.hashes = null;
    }

    /**
     * Construct a large map
     *
     * @param maximumSize
     *            Estimate of the number of keys.
     */
    protected LargeMap(final long maximumSize)
    {
        this("LargeMap", maximumSize);
    }

    /**
     * Construct a large map
     *
     * @param maximumSize
     *            Estimate of the number of keys.
     * @param hashSize
     *            The size of the hash space. The hash used is modulo. If this is too small, there
     *            will be lots of collisions, and the map will be really slow. If this is too big,
     *            the hash space will be sparse, and use up a lot of memory for nothing.
     */
    protected LargeMap(final long maximumSize, final int hashSize)
    {
        this("LargeMap", maximumSize, hashSize);
    }

    /**
     * Construct a large map
     *
     * @param name
     *            The name of the map
     * @param maximumSize
     *            Estimate of the number of keys.
     */
    protected LargeMap(final String name, final long maximumSize)
    {
        this(name, maximumSize,
                (int) Math.min(maximumSize / DEFAULT_HASH_MODULO_RATIO, Integer.MAX_VALUE));
    }

    /**
     * Construct a large map
     *
     * @param name
     *            The name of the map
     * @param maximumSize
     *            Estimate of the number of keys.
     * @param hashSize
     *            The size of the hash space. The hash used is modulo. If this is too small, there
     *            will be lots of collisions, and the map will be really slow. If this is too big,
     *            the hash space will be sparse, and use up a lot of memory for nothing.
     */
    protected LargeMap(final String name, final long maximumSize, final int hashSize)
    {
        this(name, maximumSize, hashSize, -1, -1, -1, -1);
    }

    /**
     * Construct a large map
     *
     * @param name
     *            The name of the map
     * @param maximumSize
     *            Estimate of the number of keys.
     * @param hashSize
     *            The size of the hash space. The hash used is modulo. If this is too small, there
     *            will be lots of collisions, and the map will be really slow. If this is too big,
     *            the hash space will be sparse, and use up a lot of memory for nothing.
     * @param keyMemoryBlockSize
     *            The initial memory allocation size for the keys sub arrays
     * @param keySubArraySize
     *            The maximum size of a keys sub array
     * @param valueMemoryBlockSize
     *            The initial memory allocation size for the values sub arrays
     * @param valueSubArraySize
     *            The maximum size of a values sub array
     */
    protected LargeMap(final String name, final long maximumSize, final int hashSize,
            final int keyMemoryBlockSize, final int keySubArraySize, final int valueMemoryBlockSize,
            final int valueSubArraySize)
    {
        if (maximumSize < 0 || hashSize <= 0)
        {
            throw new CoreException("maximumSize(" + maximumSize + ") has to be >=0 and hashSize("
                    + hashSize + ") has to be > 0");
        }
        this.name = name;
        this.hashes = new LongArrayOfArrays(hashSize, hashSize, hashSize);
        for (int i = 0; i < hashSize; i++)
        {
            // All the hashes are empty-populated
            this.hashes.add(new long[0]);
        }
        this.hashSize = hashSize;
        this.maximumSize = maximumSize;

        // Those 2 depend on the above values being set first.
        this.keys = createKeys(keyMemoryBlockSize, keySubArraySize);
        this.values = createValues(valueMemoryBlockSize, valueSubArraySize);
    }

    /**
     * @param key
     *            The key to test
     * @return true if the value is contained at the specified key
     */
    public boolean containsKey(final Object key)
    {
        final long[] possibleIndices = this.hashes.get(modulo(key.hashCode()));
        for (final long index : possibleIndices)
        {
            if (this.keys.get(index).equals(key))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * A basic equals() implementation. Note that if this class is parameterized with an array type,
     * this method may not work as expected (due to array equals() performing a reference
     * comparison). Child classes of {@link LargeMap} may want to override this method to improve
     * its behavior in special cases.
     */
    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof LargeMap)
        {
            if (this == other)
            {
                return true;
            }
            @SuppressWarnings("unchecked")
            final LargeMap<K, V> that = (LargeMap<K, V>) other;
            if (!Objects.equals(this.getName(), that.getName()))
            {
                return false;
            }
            if (this.size() != that.size())
            {
                return false;
            }
            final Iterable<K> iterable = () -> this.iterator();
            for (final K key : iterable)
            {
                if (!that.containsKey(key))
                {
                    return false;
                }
                final V thisValue = this.get(key);
                final V thatValue = that.get(key);
                if (!thisValue.equals(thatValue))
                {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * @param key
     *            The key to get the value at
     * @return The value at the specified key
     */
    public V get(final Object key)
    {
        final long[] possibleIndices = this.hashes.get(modulo(key.hashCode()));
        for (final long index : possibleIndices)
        {
            if (this.keys.get(index).equals(key))
            {
                return this.values.get(index);
            }
        }
        return null;
    }

    public long getMaximumSize()
    {
        return this.maximumSize;
    }

    /**
     * @return The name of this map
     */
    public String getName()
    {
        return this.name;
    }

    @Override
    public int hashCode()
    {
        final int initialPrime = 31;
        final int hashSeed = 37;

        final int nameHash = this.getName() == null ? 0 : this.getName().hashCode();
        int hash = hashSeed * initialPrime + nameHash;
        hash = hashSeed * hash + Long.valueOf(this.size()).hashCode();

        final Iterable<K> iterable = () -> this.iterator();
        for (final K key : iterable)
        {
            final V value = this.get(key);
            hash = hashSeed * hash + key.hashCode();
            hash = hashSeed * hash + value.hashCode();
        }

        return hash;
    }

    public boolean isEmpty()
    {
        return this.keys.size() <= 0;
    }

    @Override
    public Iterator<K> iterator()
    {
        return this.keys.iterator();
    }

    public synchronized void put(final K key, final V value)
    {
        // Find already all the possible collisions
        long[] possibleIndices;
        final long hashIndex = modulo(key.hashCode());
        possibleIndices = this.hashes.get(hashIndex);
        for (final long index : possibleIndices)
        {
            if (this.keys.get(index).equals(key))
            {
                // We have the same key already in, override it!
                this.values.set(index, value);
                return;
            }
        }
        if (size() >= this.maximumSize)
        {
            throw new CoreException("The map is full.");
        }
        // The given key does not exist yet
        final long index = this.keys.size();
        this.keys.add(key);
        this.values.add(value);
        possibleIndices = Arrays.addNewItem(possibleIndices, index);
        this.hashes.set(hashIndex, possibleIndices);
    }

    public long size()
    {
        return this.keys.size();
    }

    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(this.getClass().getSimpleName());
        builder.append(" ");
        this.forEach(key ->
        {
            if (builder.length() > 0)
            {
                builder.append(", ");
            }
            builder.append(asKeyString(key));
            builder.append(" -> ");
            builder.append(asValueString(get(key)));
        });
        builder.append("]");
        return builder.toString();
    }

    /**
     * Trim this {@link LargeMap}. WARNING: As much as this might save memory on arrays filled a
     * small amount compared to the memoryBlockSize, it will resize the last {@link PrimitiveArray}
     * of this {@link LargeMap}'s keys' and values' {@link LargeArray} which might take a lot of
     * time, and (temporarily) a lot of memory.
     */
    public void trim()
    {
        this.keys.trim();
        this.values.trim();
    }

    /**
     * Trim this {@link LargeMap} if and only if the fill {@link Ratio} of the last
     * {@link PrimitiveArray} of this {@link LargeMap}'s keys' and values' {@link LargeArray} is
     * less than the provided {@link Ratio}
     *
     * @param ratio
     *            The provided reference {@link Ratio}
     */
    public void trimIfLessFilledThan(final Ratio ratio)
    {
        this.keys.trimIfLessFilledThan(ratio);
        this.values.trimIfLessFilledThan(ratio);
    }

    /**
     * Override to change how the key is printed
     *
     * @param key
     *            A key to print
     * @return The String representation of the key
     */
    protected String asKeyString(final K key)
    {
        return key.toString();
    }

    /**
     * Override to change how the value is printed
     *
     * @param value
     *            A value to print
     * @return The String representation of the value
     */
    protected String asValueString(final V value)
    {
        return value.toString();
    }

    /**
     * @param memoryBlockSize
     *            The initial memory allocation size for the keys sub arrays
     * @param subArraySize
     *            The maximum size of a keys sub array
     * @return An empty array of keys
     */
    protected abstract LargeArray<K> createKeys(int memoryBlockSize, int subArraySize);

    /**
     * @param memoryBlockSize
     *            The initial memory allocation size for the values sub arrays
     * @param subArraySize
     *            The maximum size of a values sub array
     * @return An empty array of values
     */
    protected abstract LargeArray<V> createValues(int memoryBlockSize, int subArraySize);

    private int modulo(final int hashValue)
    {
        final boolean negative = hashValue < 0;
        long value = hashValue;
        if (negative)
        {
            value = -value;
        }
        return (int) (value % this.hashSize);
    }
}
