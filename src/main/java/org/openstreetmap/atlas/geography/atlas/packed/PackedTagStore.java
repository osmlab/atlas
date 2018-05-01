package org.openstreetmap.atlas.geography.atlas.packed;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.proto.ProtoSerializable;
import org.openstreetmap.atlas.proto.adapters.ProtoAdapter;
import org.openstreetmap.atlas.proto.adapters.ProtoPackedTagStoreAdapter;
import org.openstreetmap.atlas.utilities.arrays.Arrays;
import org.openstreetmap.atlas.utilities.arrays.IntegerArrayOfArrays;
import org.openstreetmap.atlas.utilities.compression.IntegerDictionary;

/**
 * Store OSM Key-Value pairs, relying on the sub-class to provide Dictionaries. This allows for
 * sharing dictionaries if necessary. The key/value storage is in arrays to minimize space, which
 * assumes each item will have a reasonably small number of key-value pairs.
 *
 * @author matthieun
 * @author lcram
 */
public class PackedTagStore implements Serializable, ProtoSerializable
{
    private static final long serialVersionUID = -5240324410665237846L;

    // Keep track of the field names for reflection code in the ProtoAdapter
    public static final String FIELD_KEYS = "keys";
    public static final String FIELD_VALUES = "values";
    public static final String FIELD_INDEX = "index";

    private final IntegerArrayOfArrays keys;
    private final IntegerArrayOfArrays values;
    private transient IntegerDictionary<String> dictionary;

    private long index = 0L;

    public PackedTagStore()
    {
        this.keys = null;
        this.values = null;
        this.dictionary = null;
    }

    public PackedTagStore(final long maximumSize, final int memoryBlockSize, final int subArraySize,
            final IntegerDictionary<String> dictionary)
    {
        this.keys = new IntegerArrayOfArrays(maximumSize, memoryBlockSize, subArraySize);
        this.values = new IntegerArrayOfArrays(maximumSize, memoryBlockSize, subArraySize);
        this.dictionary = dictionary;
    }

    /**
     * Add a key/value pair at the specified index
     *
     * @param index
     *            The index
     * @param key
     *            The key
     * @param value
     *            The value
     */
    public void add(final long index, final String key, final String value)
    {
        if (index > size())
        {
            throw new CoreException("Cannot add. Invalid index {} is bigger than the size {}",
                    index, size());
        }
        final int keyIndex = keysDictionary().add(key);
        final int valueIndex = valuesDictionary().add(value);
        final int[] keyArray;
        final int[] valueArray;
        if (index == this.index)
        {
            // We are adding a new row
            if (key == null || value == null)
            {
                keyArray = new int[0];
                valueArray = new int[0];
            }
            else
            {
                keyArray = new int[1];
                valueArray = new int[1];
                keyArray[0] = keyIndex;
                valueArray[0] = valueIndex;
            }
            this.keys.add(keyArray);
            this.values.add(valueArray);
            this.index++;
        }
        else
        {
            // We are adding a key/value pair to an existing item
            if (key == null || value == null)
            {
                // Do not add anything
                return;
            }
            keyArray = Arrays.addNewItem(this.keys.get(index), keyIndex);
            valueArray = Arrays.addNewItem(this.values.get(index), valueIndex);
            this.keys.set(index, keyArray);
            this.values.set(index, valueArray);
        }
    }

    /**
     * @param index
     *            The index to check for
     * @param key
     *            The key to test the presence of
     * @return True if the key is present at the specified index
     */
    public boolean containsKey(final long index, final String key)
    {
        if (key == null)
        {
            throw new CoreException("Cannot test if a null key is contained");
        }
        final int[] keyArray = this.keys.get(index);
        for (final int keyIndex : keyArray)
        {
            if (key.equals(keysDictionary().word(keyIndex)))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof PackedTagStore)
        {
            if (this == other)
            {
                return true;
            }
            final PackedTagStore that = (PackedTagStore) other;
            if (!this.keys.equals(that.keys))
            {
                return false;
            }
            if (!this.values.equals(that.values))
            {
                return false;
            }
            if (!Objects.equals(this.keysDictionary(), that.keysDictionary()))
            {
                return false;
            }
            if (!Objects.equals(this.valuesDictionary(), that.valuesDictionary()))
            {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * @param index
     *            The index to check for
     * @param key
     *            The key to get the value from
     * @return The value for the specified key at the specified index. Returns null if the key is
     *         not present.
     */
    public String get(final long index, final String key)
    {
        if (key == null)
        {
            throw new CoreException("Cannot get a null key's value");
        }
        final int[] keyArray = this.keys.get(index);
        for (int i = 0; i < keyArray.length; i++)
        {
            final int keyIndex = keyArray[i];
            if (key.equals(keysDictionary().word(keyIndex)))
            {
                final int valueIndex = this.values.get(index)[i];
                return valuesDictionary().word(valueIndex);
            }
        }
        return null;
    }

    @Override
    public ProtoAdapter getProtoAdapter()
    {
        return new ProtoPackedTagStoreAdapter();
    }

    @Override
    public int hashCode()
    {
        final int initialPrime = 31;
        final int hashSeed = 37;

        int hash = hashSeed * initialPrime + this.keys.hashCode();
        hash = hashSeed * hash + this.values.hashCode();

        final int keysDictionaryHash = this.keysDictionary() == null ? 0
                : this.keysDictionary().hashCode();
        final int valuesDictionaryHash = this.valuesDictionary() == null ? 0
                : this.valuesDictionary().hashCode();

        hash = hashSeed * hash + keysDictionaryHash;
        hash = hashSeed * hash + valuesDictionaryHash;

        return hash;
    }

    /**
     * @return The dictionary for keys
     */
    public IntegerDictionary<String> keysDictionary()
    {
        return this.dictionary;
    }

    /**
     * @param index
     *            The index to look for
     * @return All the keys at a specified index
     */
    public Set<String> keySet(final long index)
    {
        final Set<String> result = new HashSet<>();
        final int[] keyArray = this.keys.get(index);
        for (final int keyIndex : keyArray)
        {
            result.add(keysDictionary().word(keyIndex));
        }
        return result;
    }

    /**
     * @param index
     *            The index to look for
     * @return All the key/value pairs at a specified index
     */
    public Map<String, String> keyValuePairs(final long index)
    {
        final Map<String, String> result = new HashMap<>();
        if (this.keys.size() == 0)
        {
            // No tags
            return result;
        }
        final int[] keyArray = this.keys.get(index);
        final int[] valueArray = this.values.get(index);
        for (int i = 0; i < keyArray.length; i++)
        {
            final int keyIndex = keyArray[i];
            final int valueIndex = valueArray[i];
            result.put(keysDictionary().word(keyIndex), valuesDictionary().word(valueIndex));
        }
        return result;
    }

    public void setDictionary(final IntegerDictionary<String> dictionary)
    {
        this.dictionary = dictionary;
    }

    /**
     * @return The size of this tag store
     */
    public long size()
    {
        return this.index;
    }

    public void trim()
    {
        this.keys.trim();
        this.values.trim();
    }

    /**
     * @return The dictionary for values
     */
    public IntegerDictionary<String> valuesDictionary()
    {
        return this.dictionary;
    }
}
