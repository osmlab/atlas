package org.openstreetmap.atlas.proto.adapters;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.AtlasMetaData;
import org.openstreetmap.atlas.geography.atlas.builder.AtlasSize;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lcram
 */
public class ProtoAtlasMetaDataAdapterTest
{
    private static final Logger logger = LoggerFactory
            .getLogger(ProtoAtlasMetaDataAdapterTest.class);
    private final ProtoAtlasMetaDataAdapter adapter = new ProtoAtlasMetaDataAdapter();

    @Test
    public void testConsistency()
    {
        final AtlasSize size = new AtlasSize(1, 1, 1, 1, 1, 1);
        final Map<String, String> tags = Maps.hashMap("key1", "value1", "key2", "value2");
        final AtlasMetaData metaData = new AtlasMetaData(size, true, "test", "test", "test", "test",
                tags);

        Time startTime = Time.now();
        final byte[] contents = this.adapter.serialize(metaData);
        logger.info("Took {} to serialize AtlasMetaData", startTime.elapsedSince());

        startTime = Time.now();
        final AtlasMetaData parsedFrom = (AtlasMetaData) this.adapter.deserialize(contents);
        logger.info("Took {} to deserialize AtlasMetaData from bytestream",
                startTime.elapsedSince());

        Assert.assertEquals(metaData, parsedFrom);
    }

    @Test
    public void testNullFields()
    {
        final AtlasMetaData nullified = new AtlasMetaData(null, false, null, null, null, null,
                null);

        Time startTime = Time.now();
        final byte[] contents = this.adapter.serialize(nullified);
        logger.info("Took {} to serialize AtlasMetaData", startTime.elapsedSince());

        startTime = Time.now();
        final AtlasMetaData parsedFrom = (AtlasMetaData) this.adapter.deserialize(contents);
        logger.info("Took {} to deserialize AtlasMetaData from bytestream",
                startTime.elapsedSince());

        Assert.assertEquals(nullified, parsedFrom);
    }
}
