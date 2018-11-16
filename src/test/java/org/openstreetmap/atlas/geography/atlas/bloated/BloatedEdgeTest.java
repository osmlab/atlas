package org.openstreetmap.atlas.geography.atlas.bloated;

import java.util.Map;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.utilities.collections.Maps;

/**
 * @author matthieun
 */
public class BloatedEdgeTest
{
    @Rule
    public BloatedTestRule rule = new BloatedTestRule();

    @Test
    public void testFull()
    {
        final Atlas atlas = this.rule.getAtlas();
        final Edge source = atlas.edge(3);
        final BloatedEdge result = BloatedEdge.fromEdge(source);
        Assert.assertEquals(source.getIdentifier(), result.getIdentifier());
        Assert.assertEquals(source.bounds(), result.bounds());
        Assert.assertEquals(source.asPolyLine(), result.asPolyLine());
        Assert.assertEquals(source.start().getIdentifier(), result.start().getIdentifier());
        Assert.assertEquals(source.end().getIdentifier(), result.end().getIdentifier());
        Assert.assertEquals(source.getTags(), result.getTags());
    }

    @Test
    public void testShallow()
    {
        final Atlas atlas = this.rule.getAtlas();
        final Edge source = atlas.edge(3);
        final BloatedEdge result = BloatedEdge.fromEdge(source);
        Assert.assertEquals(source.getIdentifier(), result.getIdentifier());
        Assert.assertEquals(source.bounds(), result.bounds());
        result.withPolyLine(PolyLine.TEST_POLYLINE);
        Assert.assertEquals(PolyLine.TEST_POLYLINE.bounds(), result.bounds());
        final Map<String, String> tags = Maps.hashMap("key", "value");
        result.withTags(tags);
        Assert.assertEquals(tags, result.getTags());
        final long startNodeIdentifier = 5;
        result.withStartNodeIdentifier(startNodeIdentifier);
        Assert.assertEquals(startNodeIdentifier, result.start().getIdentifier());
        final long endNodeIdentifier = 6;
        result.withEndNodeIdentifier(endNodeIdentifier);
        Assert.assertEquals(endNodeIdentifier, result.end().getIdentifier());
    }
}
