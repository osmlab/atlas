package org.openstreetmap.atlas.geography.atlas.builder.proto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
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
    public void testReadWriteConsistency()
    {
        final WritableResource resource = new ByteArrayResource();
        final ProtoAtlasBuilder protoAtlasBuilder = new ProtoAtlasBuilder();
        final PackedAtlasBuilder packedAtlasBuilder = setUpTestAtlasBuilder();

        // make sure the atlases are the same
        final Atlas outAtlas = packedAtlasBuilder.get();
        protoAtlasBuilder.write(outAtlas, resource);
        final Atlas inAtlas = protoAtlasBuilder.read(resource);
        Assert.assertEquals(outAtlas, inAtlas);
    }

    private PackedAtlasBuilder setUpTestAtlasBuilder()
    {
        final PackedAtlasBuilder packedAtlasBuilder = new PackedAtlasBuilder();
        final Map<String, String> tags = new HashMap<>();

        tags.put("building", "yes");
        tags.put("name", "eiffel_tower");
        packedAtlasBuilder.addPoint(0, Location.EIFFEL_TOWER, tags);

        tags.clear();
        tags.put("building", "yes");
        tags.put("name", "colosseum");
        packedAtlasBuilder.addPoint(1, Location.COLOSSEUM, tags);

        tags.clear();
        tags.put("path", "yes");
        final List<Location> shapePoints = new ArrayList<>();
        shapePoints.add(Location.EIFFEL_TOWER);
        shapePoints.add(Location.COLOSSEUM);
        packedAtlasBuilder.addLine(2, new PolyLine(shapePoints), tags);

        return packedAtlasBuilder;
    }
}
