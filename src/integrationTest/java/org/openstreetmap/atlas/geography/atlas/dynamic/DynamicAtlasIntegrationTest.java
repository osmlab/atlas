package org.openstreetmap.atlas.geography.atlas.dynamic;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.text.TextAtlasBuilder;
import org.openstreetmap.atlas.geography.atlas.dynamic.policy.DynamicAtlasPolicy;
import org.openstreetmap.atlas.geography.sharding.SlippyTile;
import org.openstreetmap.atlas.geography.sharding.SlippyTileSharding;
import org.openstreetmap.atlas.streaming.compression.Decompressor;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * @author matthieun
 */
public class DynamicAtlasIntegrationTest
{
    private static final DynamicAtlasPolicy POLICY = new DynamicAtlasPolicy(shard ->
    {
        final String fileName = "DMA_" + shard.getName() + ".atlas.txt.gz";
        return Optional.of(new TextAtlasBuilder().read(new InputStreamResource(
                () -> DynamicAtlasIntegrationTest.class.getResourceAsStream(fileName))
                .withName(fileName).withDecompressor(Decompressor.GZIP)));
    }, new SlippyTileSharding(9), SlippyTile.forName("9-168-234"), Rectangle.MAXIMUM);

    @Test
    public void testAreas()
    {
        final Atlas atlas = new DynamicAtlas(POLICY);
        Assert.assertEquals(39672, Iterables.size(atlas.areas()));
    }

    @Test
    public void testEdges()
    {
        final Atlas atlas = new DynamicAtlas(POLICY);
        Assert.assertEquals(18936, Iterables.size(atlas.edges()));
    }

    @Test
    public void testLines()
    {
        final Atlas atlas = new DynamicAtlas(POLICY);
        Assert.assertEquals(1572, Iterables.size(atlas.lines()));
    }

    @Test
    public void testNodes()
    {
        final Atlas atlas = new DynamicAtlas(POLICY);
        Assert.assertEquals(8615, Iterables.size(atlas.nodes()));
    }

    @Test
    public void testPoints()
    {
        final Atlas atlas = new DynamicAtlas(POLICY);
        Assert.assertEquals(348, Iterables.size(atlas.points()));
    }

    @Test
    public void testRelations()
    {
        final Atlas atlas = new DynamicAtlas(POLICY);
        Assert.assertEquals(2, Iterables.size(atlas.relations()));
    }
}
