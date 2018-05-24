package org.openstreetmap.atlas.proto.adapters;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.compression.IntegerDictionary;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lcram
 */
public class ProtoIntegerStringDictionaryAdapterTest
{
    private static final Logger logger = LoggerFactory
            .getLogger(ProtoIntegerStringDictionaryAdapterTest.class);
    private static final int NUMBER_OF_ENTRIES = 100_000;
    private final ProtoIntegerStringDictionaryAdapter adapter = new ProtoIntegerStringDictionaryAdapter();

    @Test
    public void testConsistency()
    {
        final IntegerDictionary<String> dictionary = new IntegerDictionary<>();
        for (int index = 0; index < NUMBER_OF_ENTRIES; index++)
        {
            dictionary.add("testword" + index);
        }

        Time startTime = Time.now();
        final byte[] contents = this.adapter.serialize(dictionary);
        logger.info("Took {} to serialize IntegerDictionary<String>", startTime.elapsedSince());

        startTime = Time.now();
        @SuppressWarnings("unchecked")
        final IntegerDictionary<String> parsedFrom = (IntegerDictionary<String>) this.adapter
                .deserialize(contents);
        logger.info("Took {} to deserialize IntegerDictionary<String> from bytestream",
                startTime.elapsedSince());

        logger.info("Testing equality...");
        Assert.assertEquals(dictionary, parsedFrom);
    }

    @Test
    public void testContainsNullElements()
    {
        final IntegerDictionary<String> dictionary = new IntegerDictionary<>();

        dictionary.add("testword1");
        dictionary.add(null);
        dictionary.add("testword2");

        final byte[] contents = this.adapter.serialize(dictionary);
        @SuppressWarnings("unchecked")
        final IntegerDictionary<String> parsedFrom = (IntegerDictionary<String>) this.adapter
                .deserialize(contents);

        Assert.assertEquals(dictionary, parsedFrom);
    }

    @Test(expected = CoreException.class)
    public void testExceptionOnInvalidOwnerTypeParameter()
    {
        // Create an IntegerDictionary parameterized with a type other than String
        final IntegerDictionary<Object> dictionary = new IntegerDictionary<>();
        dictionary.add(new Object());

        // this line will throw a CoreException when attempting to serialize the dictionary
        @SuppressWarnings("unused")
        final byte[] contents = dictionary.getProtoAdapter().serialize(dictionary);
    }
}
