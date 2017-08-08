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
public class LongDictionary<Type> implements Serializable
{
    private static final long serialVersionUID = 6060400113166584385L;
    private final Map<Type, Long> wordToIndex;
    private final Map<Long, Type> indexToWord;

    private long index = 0;

    public LongDictionary()
    {
        this.wordToIndex = new HashMap<>();
        this.indexToWord = new HashMap<>();
    }

    public synchronized long add(final Type word)
    {
        if (this.wordToIndex.containsKey(word))
        {
            return this.wordToIndex.get(word);
        }
        this.wordToIndex.put(word, this.index);
        this.indexToWord.put(this.index, word);
        return this.index++;
    }

    public Type word(final long index)
    {
        return this.indexToWord.get(index);
    }
}
