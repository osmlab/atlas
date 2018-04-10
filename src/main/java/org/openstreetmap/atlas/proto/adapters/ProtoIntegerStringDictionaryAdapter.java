package org.openstreetmap.atlas.proto.adapters;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.proto.ProtoIntegerStringDictionary;
import org.openstreetmap.atlas.proto.ProtoSerializable;
import org.openstreetmap.atlas.utilities.compression.IntegerDictionary;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Implements the {@link ProtoAdapter} interface to connect {@link IntegerDictionary} and
 * {@link ProtoIntegerStringDictionary}. This adapter will fail when attempting to serialize an
 * {@link IntegerDictionary} that is not type parameterized using {@link String}.
 *
 * @author lcram
 */
public class ProtoIntegerStringDictionaryAdapter implements ProtoAdapter
{
    // NOTE this method will ALWAYS give back an IntegerDictionary<String>, no matter what type
    // parameterization was used by the parent IntegerDictionary<T> the adapter belongs to
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

        for (int index = 0; index < protoIntegerStringDictionary.getWordsCount(); index++)
        {
            final String word = protoIntegerStringDictionary.getWords(index);
            dictionary.add(word);
        }

        return dictionary;
    }

    @Override
    public byte[] serialize(final ProtoSerializable serializable)
    {
        if (!(serializable instanceof IntegerDictionary))
        {
            throw new CoreException(
                    "Invalid ProtoSerializable type was provided to {}: cannot serialize {}",
                    this.getClass().getName(), serializable.getClass().getName());
        }
        @SuppressWarnings("unchecked")
        final IntegerDictionary<String> integerDictionary = (IntegerDictionary<String>) serializable;

        final ProtoIntegerStringDictionary.Builder dictionaryBuilder = ProtoIntegerStringDictionary
                .newBuilder();

        int index = 0;
        String word;

        try
        {
            while ((word = integerDictionary.word(index)) != null)
            {
                dictionaryBuilder.addWords(word);
                index++;
            }
        }
        catch (final ClassCastException exception)
        {
            throw new CoreException(
                    "This adapter is incompatible with its owner's ({}) current type parametrization. Must be java.lang.String",
                    serializable.getClass().getName(), exception);
        }

        final ProtoIntegerStringDictionary protoDictionary = dictionaryBuilder.build();
        return protoDictionary.toByteArray();
    }
}
