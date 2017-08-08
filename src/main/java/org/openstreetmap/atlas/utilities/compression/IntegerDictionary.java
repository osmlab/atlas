package org.openstreetmap.atlas.utilities.compression;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple dictionary encoding for {@link String}s
 *
 * @author matthieun
 * @param <Type>
 *            The type to encode. Typically called word
 */
public class IntegerDictionary<Type> implements Serializable
{
    private static final long serialVersionUID = -1781411097803512149L;
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

    public Type word(final int index)
    {
        return this.indexToWord.get(index);
    }
}
