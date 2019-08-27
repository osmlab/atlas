package org.openstreetmap.atlas.geography.atlas.complete;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.collections.Sets;

/**
 * @author matthieun
 */
public class CompleteEdgeTest
{
    @Rule
    public CompleteTestRule rule = new CompleteTestRule();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testBloatedEquals()
    {
        final CompleteEdge edge11 = new CompleteEdge(123L, null, null, null, null, null);
        final CompleteEdge edge12 = new CompleteEdge(123L, null, null, null, null, null);
        final CompleteEdge edge21 = new CompleteEdge(123L, PolyLine.TEST_POLYLINE, null, null, null,
                null);
        final CompleteEdge edge22 = new CompleteEdge(123L, PolyLine.TEST_POLYLINE, null, null, null,
                null);
        final CompleteEdge edge23 = new CompleteEdge(123L, Polygon.SILICON_VALLEY_2, null, null,
                null, null);
        final CompleteEdge edge31 = new CompleteEdge(123L, null, Maps.hashMap("key", "value"), null,
                null, null);
        final CompleteEdge edge32 = new CompleteEdge(123L, null, Maps.hashMap("key", "value"), null,
                null, null);
        final CompleteEdge edge33 = new CompleteEdge(123L, null, Maps.hashMap(), null, null, null);
        final CompleteEdge edge41 = new CompleteEdge(123L, null, null, null, null,
                Sets.hashSet(1L, 2L));
        final CompleteEdge edge42 = new CompleteEdge(123L, null, null, null, null,
                Sets.hashSet(1L, 2L));
        final CompleteEdge edge43 = new CompleteEdge(123L, null, null, null, null,
                Sets.hashSet(1L));
        final CompleteEdge edge51 = new CompleteEdge(123L, null, null, 1L, null, null);
        final CompleteEdge edge52 = new CompleteEdge(123L, null, null, 1L, null, null);
        final CompleteEdge edge53 = new CompleteEdge(123L, null, null, 2L, null, null);
        final CompleteEdge edge61 = new CompleteEdge(123L, null, null, null, 1L, null);
        final CompleteEdge edge62 = new CompleteEdge(123L, null, null, null, 1L, null);
        final CompleteEdge edge63 = new CompleteEdge(123L, null, null, null, 2L, null);

        Assert.assertEquals(edge11, edge12);
        Assert.assertEquals(edge21, edge22);
        Assert.assertEquals(edge31, edge32);
        Assert.assertEquals(edge41, edge42);
        Assert.assertEquals(edge51, edge52);
        Assert.assertEquals(edge61, edge62);

        Assert.assertNotEquals(edge11, edge21);
        Assert.assertNotEquals(edge11, edge31);
        Assert.assertNotEquals(edge11, edge41);
        Assert.assertNotEquals(edge11, edge51);
        Assert.assertNotEquals(edge11, edge61);
        Assert.assertNotEquals(edge21, edge23);
        Assert.assertNotEquals(edge31, edge33);
        Assert.assertNotEquals(edge41, edge43);
        Assert.assertNotEquals(edge51, edge53);
        Assert.assertNotEquals(edge61, edge63);
    }

    @Test
    public void testEdgeShallowCopyNullBounds()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("bounds were null");

        final CompleteEdge edge = new CompleteEdge(1L, null, null, null, null, null);
        CompleteEdge.shallowFrom(edge);
    }

    @Test
    public void testFull()
    {
        final Atlas atlas = this.rule.getAtlas();
        final Edge source = atlas.edge(3);
        final CompleteEdge result = CompleteEdge.from(source);
        Assert.assertEquals(source.getIdentifier(), result.getIdentifier());
        Assert.assertEquals(source.bounds(), result.bounds());
        Assert.assertEquals(source.asPolyLine(), result.asPolyLine());
        Assert.assertEquals(source.start().getIdentifier(), result.start().getIdentifier());
        Assert.assertEquals(source.end().getIdentifier(), result.end().getIdentifier());
        Assert.assertEquals(source.getTags(), result.getTags());
        Assert.assertEquals(
                source.relations().stream().map(Relation::getIdentifier)
                        .collect(Collectors.toSet()),
                result.relations().stream().map(Relation::getIdentifier)
                        .collect(Collectors.toSet()));

        Assert.assertEquals(result, result.copy());
    }

    @Test
    public void testIsCompletelyShallow()
    {
        final CompleteEdge superShallow = new CompleteEdge(123L, null, null, null, null, null);
        Assert.assertTrue(superShallow.isShallow());
    }

    @Test
    public void testNonFullEdgeCopy()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("but it was not full");

        final CompleteEdge edge = new CompleteEdge(1L, null, null, null, null, null);
        CompleteEdge.from(edge);
    }

    @Test
    public void testShallow()
    {
        final Atlas atlas = this.rule.getAtlas();
        final Edge source = atlas.edge(3);
        final CompleteEdge result = CompleteEdge.shallowFrom(source);
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
        result.withRelationIdentifiers(source.relations().stream().map(Relation::getIdentifier)
                .collect(Collectors.toSet()));
        Assert.assertEquals(
                source.relations().stream().map(Relation::getIdentifier)
                        .collect(Collectors.toSet()),
                result.relations().stream().map(Relation::getIdentifier)
                        .collect(Collectors.toSet()));
    }

    @Test
    public void testToWkt()
    {
        final CompleteEdge edge1 = new CompleteEdge(123L);
        edge1.withPolyLine(new PolyLine(Location.forString("0,0"), Location.forString("1,1")));
        Assert.assertEquals("LINESTRING (0 0, 1 1)", edge1.toWkt());

        final CompleteEdge edge2 = new CompleteEdge(123L);
        Assert.assertNull(edge2.toWkt());
    }

    @Test
    public void testWithGeometry()
    {
        final CompleteEdge edge = new CompleteEdge(1L);
        edge.withGeometry(Arrays.asList(Location.COLOSSEUM, Location.CENTER));
        Assert.assertEquals(new PolyLine(Arrays.asList(Location.COLOSSEUM, Location.CENTER)),
                edge.asPolyLine());
    }
}
