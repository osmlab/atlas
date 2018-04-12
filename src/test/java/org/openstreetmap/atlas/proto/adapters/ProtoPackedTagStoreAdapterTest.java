package org.openstreetmap.atlas.proto.adapters;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.packed.PackedTagStore;
import org.openstreetmap.atlas.utilities.compression.IntegerDictionary;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lcram
 */
public class ProtoPackedTagStoreAdapterTest
{
    private static final Logger logger = LoggerFactory
            .getLogger(ProtoPackedTagStoreAdapterTest.class);
    private static final int TEST_SIZE = 10;
    private static final int BLOCK_SIZE = 1024;
    private static final int TEST_SUBARRAY_SIZE = 3;
    private final ProtoPackedTagStoreAdapter adapter = new ProtoPackedTagStoreAdapter();

    @Test
    public void testConsistency()
    {
        final IntegerDictionary<String> dictionary = new IntegerDictionary<String>();

        final PackedTagStore store = new PackedTagStore(TEST_SIZE, BLOCK_SIZE, TEST_SUBARRAY_SIZE,
                dictionary);

        for (int index = 0; index < TEST_SIZE; index++)
        {
            for (int subIndex = 0; subIndex < TEST_SUBARRAY_SIZE; subIndex++)
            {
                store.add(index, "key" + index + "_" + subIndex, "value" + index + "_" + subIndex);
            }
        }

        Time startTime = Time.now();
        final byte[] contents = this.adapter.serialize(store);
        logger.info("Took {} to serialize PackedTagStore", startTime.elapsedSince());

        startTime = Time.now();
        final PackedTagStore parsedFrom = (PackedTagStore) this.adapter.deserialize(contents);
        parsedFrom.setDictionary(dictionary);
        logger.info("Took {} to deserialize PackedTagStore from bytestream",
                startTime.elapsedSince());

        logger.info("Testing equality...");
        Assert.assertEquals(store, parsedFrom);
    }
}
