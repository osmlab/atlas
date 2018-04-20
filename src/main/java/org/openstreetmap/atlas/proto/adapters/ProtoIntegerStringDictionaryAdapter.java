package org.openstreetmap.atlas.proto.adapters;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.proto.ProtoIntegerStringDictionary;
import org.openstreetmap.atlas.proto.ProtoSerializable;
import org.openstreetmap.atlas.utilities.compression.IntegerDictionary;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Implements the {@link ProtoAdapter} interface to connect {@link IntegerDictionary} and
 * {@link ProtoIntegerStringDictionary}. This adapter will fail when attempting to serialize an
 * {@link IntegerDictionary} that is not type parameterized using {@link String}. Also note that
 * {@link ProtoIntegerStringDictionaryAdapter#deserialize(byte[])} will give back an
 * {@link IntegerDictionary} parameterized with {@link String}, no matter what type parameterization
 * was used by the parent {@link IntegerDictionary} the adapter belongs to.
 *
 * @author lcram
 */
public class ProtoIntegerStringDictionaryAdapter implements ProtoAdapter
{
    /*
     * IntegerDictionary does not provide an interface for setting its subfields directly. This
     * class's implementation uses reflection to side-step the issue.
     */

    @Override
    public ProtoSerializable deserialize(final byte[] byteArray)
    {
        ProtoIntegerStringDictionary protoIntegerStringDictionary = null;
        try
        {
            protoIntegerStringDictionary = ProtoIntegerStringDictionary.parseFrom(byteArray);
        }
        catch (final InvalidProtocolBufferException exception)
        {
            throw new CoreException("Error encountered while parsing protobuf bytestream",
                    exception);
        }
        final IntegerDictionary<String> dictionary = new IntegerDictionary<>();

        final Integer currentIndex = protoIntegerStringDictionary.getCurrentIndex();
        final List<Integer> indexes = protoIntegerStringDictionary.getIndexesList();
        final List<String> words = protoIntegerStringDictionary.getWordsList();

        final Map<String, Integer> wordToIndex = new HashMap<>();
        final Map<Integer, String> indexToWord = new HashMap<>();

        for (int index = 0; index < words.size(); index++)
        {
            final String word = words.get(index);
            final Integer theIndex = indexes.get(index);
            wordToIndex.put(word, theIndex);
            indexToWord.put(theIndex, word);
        }

        Field wordToIndexField = null;
        Field indexToWordField = null;
        Field indexField = null;

        try
        {
            wordToIndexField = dictionary.getClass()
                    .getDeclaredField(IntegerDictionary.FIELD_WORD_TO_INDEX);
            wordToIndexField.setAccessible(true);
            wordToIndexField.set(dictionary, wordToIndex);
        }
        catch (final Exception exception)
        {
            throw new CoreException("Unable to set field \"{}\" in {}",
                    IntegerDictionary.FIELD_WORD_TO_INDEX, dictionary.getClass().getName(),
                    exception);
        }

        try
        {
            indexToWordField = dictionary.getClass()
                    .getDeclaredField(IntegerDictionary.FIELD_INDEX_TO_WORD);
            indexToWordField.setAccessible(true);
            indexToWordField.set(dictionary, indexToWord);
        }
        catch (final Exception exception)
        {
            throw new CoreException("Unable to set field \"{}\" in {}",
                    IntegerDictionary.FIELD_INDEX_TO_WORD, dictionary.getClass().getName(),
                    exception);
        }

        try
        {
            indexField = dictionary.getClass().getDeclaredField(IntegerDictionary.FIELD_INDEX);
            indexField.setAccessible(true);
            indexField.set(dictionary, currentIndex);
        }
        catch (final Exception exception)
        {
            throw new CoreException("Unable to set field \"{}\" in {}",
                    IntegerDictionary.FIELD_INDEX, dictionary.getClass().getName(), exception);
        }

        return dictionary;
    }

    @Override
    @SuppressWarnings("unchecked")
    public byte[] serialize(final ProtoSerializable serializable)
    {
        if (!(serializable instanceof IntegerDictionary))
        {
            throw new CoreException(
                    "Invalid ProtoSerializable type was provided to {}: cannot serialize {}",
                    this.getClass().getName(), serializable.getClass().getName());
        }
        final IntegerDictionary<String> integerDictionary = (IntegerDictionary<String>) serializable;

        final ProtoIntegerStringDictionary.Builder protoDictionaryBuilder = ProtoIntegerStringDictionary
                .newBuilder();

        Field indexToWordField = null;
        Map<Integer, String> indexToWord = null;
        Field indexField = null;
        Integer index = -1;

        try
        {
            indexToWordField = integerDictionary.getClass()
                    .getDeclaredField(IntegerDictionary.FIELD_INDEX_TO_WORD);
            indexToWordField.setAccessible(true);
            indexToWord = (Map<Integer, String>) indexToWordField.get(integerDictionary);
        }
        catch (final Exception exception)
        {
            throw new CoreException("Unable to read field \"{}\" from {}",
                    IntegerDictionary.FIELD_INDEX_TO_WORD, integerDictionary.getClass().getName(),
                    exception);
        }

        /*
         * Wondering why we don't read the field wordToIndex? We don't need to, since it is
         * symmetric with the indexToWord field! We can populate the underlying proto arrays by
         * simply grabbing the keys and values from the indexToWord map.
         */

        try
        {
            indexField = integerDictionary.getClass()
                    .getDeclaredField(IntegerDictionary.FIELD_INDEX);
            indexField.setAccessible(true);
            index = (Integer) indexField.get(integerDictionary);
        }
        catch (final Exception exception)
        {
            throw new CoreException("Unable to read field \"{}\" from {}",
                    IntegerDictionary.FIELD_INDEX, integerDictionary.getClass().getName(),
                    exception);
        }

        try
        {
            for (final Integer key : indexToWord.keySet())
            {
                protoDictionaryBuilder.addIndexes(key);
                protoDictionaryBuilder.addWords(indexToWord.get(key));
            }
        }
        catch (final ClassCastException exception)
        {
            throw new CoreException(
                    "This adapter is incompatible with type parametrization of the owning {}. Must be java.lang.String",
                    serializable.getClass().getName(), exception);
        }
        protoDictionaryBuilder.setCurrentIndex(index);

        return protoDictionaryBuilder.build().toByteArray();
    }
}
