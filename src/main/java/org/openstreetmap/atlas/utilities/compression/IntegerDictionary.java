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

    // TODO the problem here is that if someone tries to get an adapter for an
    // IntegerDictionary<SomeTypeThatIsNotAString>, this method will return the wrong adapter.
    // Currently, the ProtoIntegerStringDictionaryAdapter class's serialize() method handles this by
    // catching a ClassCastException and rethrowing a CoreException with a better message.
    @Override
    public ProtoAdapter getProtoAdapter()
    {
        return new ProtoIntegerStringDictionaryAdapter();
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
