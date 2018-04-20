package org.openstreetmap.atlas.utilities.compression;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.atlas.proto.ProtoSerializable;
import org.openstreetmap.atlas.proto.adapters.ProtoAdapter;
import org.openstreetmap.atlas.proto.adapters.ProtoIntegerStringDictionaryAdapter;

/**
 * Simple dictionary encoding for {@link String}s
 *
 * @author matthieun
 * @author lcram
 * @param <Type>
 *            The type to encode. Typically called word
 */
public class IntegerDictionary<Type> implements Serializable, ProtoSerializable
{
    private static final long serialVersionUID = -1781411097803512149L;

    public static final String FIELD_WORD_TO_INDEX = "wordToIndex";
    public static final String FIELD_INDEX_TO_WORD = "indexToWord";
    public static final String FIELD_INDEX = "index";

    private final Map<Type, Integer> wordToIndex;
    private final Map<Integer, Type> indexToWord;

    private int index = 0;

    public IntegerDictionary()
    {
        this.wordToIndex = new HashMap<>();
        this.indexToWord = new HashMap<>();
    }

    public synchronized int add(final Type word)
    {
        if (this.wordToIndex.containsKey(word))
        {
            return this.wordToIndex.get(word);
        }
        this.wordToIndex.put(word, this.index);
        this.indexToWord.put(this.index, word);
        return this.index++;
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof IntegerDictionary)
        {
            if (this == other)
            {
                return true;
            }
            @SuppressWarnings("unchecked")
            final IntegerDictionary<Type> that = (IntegerDictionary<Type>) other;
            if (this.size() != that.size())
            {
                return false;
            }
            if (!this.wordToIndex.equals(that.wordToIndex))
            {
                return false;
            }
            if (!this.indexToWord.equals(that.indexToWord))
            {
                return false;
            }
            return true;
        }
        return false;
    }

    /*
     * TODO the problem here is that if someone tries to get an adapter for an
     * IntegerDictionary<SomeTypeThatIsNotAString>, this method will return the wrong adapter.
     * Currently, the ProtoIntegerStringDictionaryAdapter class's serialize() method handles this by
     * catching a ClassCastException and rethrowing a CoreException with a better message.
     */
    @Override
    public ProtoAdapter getProtoAdapter()
    {
        return new ProtoIntegerStringDictionaryAdapter();
    }

    @Override
    public int hashCode()
    {
        final int initialPrime = 31;
        final int hashSeed = 37;

        int hash = hashSeed * initialPrime + Integer.valueOf(this.size()).hashCode();
        for (final Type key : this.wordToIndex.keySet())
        {
            final Integer value = this.wordToIndex.get(key);
            final int keyHash = key == null ? 0 : key.hashCode();
            final int valueHash = value == null ? 0 : value.hashCode();
            hash = hashSeed * hash + keyHash;
            hash = hashSeed * hash + valueHash;
        }
        for (final Integer key : this.indexToWord.keySet())
        {
            final Type value = this.indexToWord.get(key);
            final int keyHash = key == null ? 0 : key.hashCode();
            final int valueHash = value == null ? 0 : value.hashCode();
            hash = hashSeed * hash + keyHash;
            hash = hashSeed * hash + valueHash;
        }

        return hash;
    }

    public int size()
    {
        return this.index;
    }

    public Type word(final int index)
    {
        return this.indexToWord.get(index);
    }
}
