package org.openstreetmap.atlas.geography.atlas.builder;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.exception.AtlasIntegrityException;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class PackedAtlasBuilderTest
{
    private static final Logger logger = LoggerFactory.getLogger(PackedAtlasBuilderTest.class);

    @Test
    public void testManyVeryCloseNodes()
    {
        final PackedAtlasBuilder builder = new PackedAtlasBuilder();
        builder.addNode(1, Location.forString("48.3406719,10.5563445"), Maps.hashMap());
        builder.addNode(2, Location.forString("48.34204,10.55844"), Maps.hashMap());
        builder.addNode(3, Location.forString("48.3406720,10.5563445"), Maps.hashMap());
        builder.addNode(4, Location.forString("48.3406719,10.5563446"), Maps.hashMap());
        // First node is slightly off.
        builder.addEdge(5, PolyLine.wkt("LINESTRING (10.5563444 48.3406717, 10.55844 48.34204)"),
                Maps.hashMap(HighwayTag.KEY, HighwayTag.RESIDENTIAL.getTagValue()));
        final Atlas result = builder.get();
        logger.info("{}", result);
        Assert.assertNotNull(result);
    }

    @Test(expected = AtlasIntegrityException.class)
    public void testNotSoCloseNode()
    {
        final PackedAtlasBuilder builder = new PackedAtlasBuilder();
        builder.addNode(1, Location.forString("48.3406719,10.5563445"), Maps.hashMap());
        builder.addNode(2, Location.forString("48.34204,10.55844"), Maps.hashMap());
        // First node is too off.
        builder.addEdge(3, PolyLine.wkt("LINESTRING (10.5563430 48.3406710, 10.55844 48.34204)"),
                Maps.hashMap(HighwayTag.KEY, HighwayTag.RESIDENTIAL.getTagValue()));
        final Atlas result = builder.get();
        logger.info("{}", result);
        Assert.assertNotNull(result);
    }

    @Test
    public void testVeryCloseNode()
    {
        final PackedAtlasBuilder builder = new PackedAtlasBuilder();
        builder.addNode(1, Location.forString("48.3406719,10.5563445"), Maps.hashMap());
        builder.addNode(2, Location.forString("48.34204,10.55844"), Maps.hashMap());
        // First node is slightly off.
        builder.addEdge(3, PolyLine.wkt("LINESTRING (10.5563444 48.3406717, 10.55844 48.34204)"),
                Maps.hashMap(HighwayTag.KEY, HighwayTag.RESIDENTIAL.getTagValue()));
        final Atlas result = builder.get();
        logger.info("{}", result);
        Assert.assertNotNull(result);
    }
}
