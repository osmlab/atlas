package org.openstreetmap.atlas.geography.atlas.builder.proto;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.streaming.resource.ByteArrayResource;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lcram
 */
public class ProtoAtlasBuilderTest
{
    private static final Logger logger = LoggerFactory.getLogger(ProtoAtlasBuilderTest.class);

    @Test
    public void testWriteFunctionality()
    {
        final PackedAtlasBuilder builder = new PackedAtlasBuilder();
        final Map<String, String> tags = new HashMap<>();
        tags.put("building", "yes");
        tags.put("name", "eiffel_tower");
        builder.addPoint(0, Location.EIFFEL_TOWER, tags);

        final Atlas atlas = builder.get();
        final WritableResource resource = new ByteArrayResource();

        final ProtoAtlasBuilder protoAtlasBuilder = new ProtoAtlasBuilder();
        protoAtlasBuilder.write(atlas, resource);
    }
}
