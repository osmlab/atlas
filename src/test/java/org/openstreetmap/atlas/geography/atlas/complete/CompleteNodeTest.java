package org.openstreetmap.atlas.geography.atlas.complete;

import java.util.Arrays;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.collections.Sets;

/**
 * @author matthieun
 */
public class CompleteNodeTest
{
    @Rule
    public CompleteTestRule rule = new CompleteTestRule();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testBloatedEquals()
    {
        final CompleteNode node11 = new CompleteNode(123L, null, null, null, null, null);
        final CompleteNode node12 = new CompleteNode(123L, null, null, null, null, null);
        final CompleteNode node21 = new CompleteNode(123L, Location.COLOSSEUM, null, null, null,
                null);
        final CompleteNode node22 = new CompleteNode(123L, Location.COLOSSEUM, null, null, null,
                null);
        final CompleteNode node23 = new CompleteNode(123L, Location.EIFFEL_TOWER, null, null, null,
                null);
        final CompleteNode node31 = new CompleteNode(123L, null, Maps.hashMap("key", "value"), null,
                null, null);
        final CompleteNode node32 = new CompleteNode(123L, null, Maps.hashMap("key", "value"), null,
                null, null);
        final CompleteNode node33 = new CompleteNode(123L, null, Maps.hashMap(), null, null, null);
        final CompleteNode node41 = new CompleteNode(123L, null, null, null, null,
                Sets.hashSet(1L, 2L));
        final CompleteNode node42 = new CompleteNode(123L, null, null, null, null,
                Sets.hashSet(1L, 2L));
        final CompleteNode node43 = new CompleteNode(123L, null, null, null, null,
                Sets.hashSet(1L));
        final CompleteNode node51 = new CompleteNode(123L, null, null, Sets.treeSet(1L, 2L), null,
                null);
        final CompleteNode node52 = new CompleteNode(123L, null, null, Sets.treeSet(1L, 2L), null,
                null);
        final CompleteNode node53 = new CompleteNode(123L, null, null, Sets.treeSet(1L), null,
                null);
        final CompleteNode node61 = new CompleteNode(123L, null, null, null, Sets.treeSet(1L, 2L),
                null);
        final CompleteNode node62 = new CompleteNode(123L, null, null, null, Sets.treeSet(1L, 2L),
                null);
        final CompleteNode node63 = new CompleteNode(123L, null, null, null, Sets.treeSet(1L),
                null);

        Assert.assertEquals(node11, node12);
        Assert.assertEquals(node21, node22);
        Assert.assertEquals(node31, node32);
        Assert.assertEquals(node41, node42);
        Assert.assertEquals(node51, node52);
        Assert.assertEquals(node61, node62);

        Assert.assertNotEquals(node11, node21);
        Assert.assertNotEquals(node11, node31);
        Assert.assertNotEquals(node11, node41);
        Assert.assertNotEquals(node11, node51);
        Assert.assertNotEquals(node11, node61);
        Assert.assertNotEquals(node21, node23);
        Assert.assertNotEquals(node31, node33);
        Assert.assertNotEquals(node41, node43);
        Assert.assertNotEquals(node51, node53);
        Assert.assertNotEquals(node61, node63);
    }

    @Test
    public void testFull()
    {
        final Atlas atlas = this.rule.getAtlas();
        final Node source = atlas.node(1);
        final CompleteNode result = CompleteNode.from(source);
        Assert.assertEquals(source.getIdentifier(), result.getIdentifier());
        Assert.assertEquals(source.bounds(), result.bounds());
        Assert.assertEquals(source.getLocation(), result.getLocation());
        Assert.assertEquals(
                source.inEdges().stream().map(Edge::getIdentifier)
                        .collect(Collectors.toCollection(TreeSet::new)),
                result.inEdges().stream().map(Edge::getIdentifier)
                        .collect(Collectors.toCollection(TreeSet::new)));
        Assert.assertEquals(
                source.outEdges().stream().map(Edge::getIdentifier)
                        .collect(Collectors.toCollection(TreeSet::new)),
                result.outEdges().stream().map(Edge::getIdentifier)
                        .collect(Collectors.toCollection(TreeSet::new)));
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
        final CompleteNode superShallow = new CompleteNode(123L, null, null, null, null, null);
        Assert.assertTrue(superShallow.isShallow());
    }

    @Test
    public void testNodeShallowCopyNullBounds()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("bounds were null");

        final CompleteNode node = new CompleteNode(1L, null, null, null, null, null);
        CompleteNode.shallowFrom(node);
    }

    @Test
    public void testNonFullNodeCopy()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("but it was not full");

        final CompleteNode node = new CompleteNode(1L, null, null, null, null, null);
        CompleteNode.from(node);
    }

    @Test
    public void testShallow()
    {
        final Atlas atlas = this.rule.getAtlas();
        final Node source = atlas.node(1);
        final CompleteNode result = CompleteNode.shallowFrom(source);
        Assert.assertEquals(source.getIdentifier(), result.getIdentifier());
        Assert.assertEquals(source.bounds(), result.bounds());
        result.withLocation(Location.CENTER);
        // When we update a location, the bounds should update to the bounds of the new location
        Assert.assertEquals(Rectangle.forLocated(Location.CENTER), result.bounds());
        final Map<String, String> tags = Maps.hashMap("key", "value");
        result.withTags(tags);
        Assert.assertEquals(tags, result.getTags());
        final SortedSet<Long> inEdgeIdentifiers = Sets.treeSet(5L, 6L);
        result.withInEdgeIdentifiers(inEdgeIdentifiers);
        Assert.assertEquals(inEdgeIdentifiers, result.inEdges().stream().map(Edge::getIdentifier)
                .collect(Collectors.toCollection(TreeSet::new)));
        final SortedSet<Long> outEdgeIdentifiers = Sets.treeSet(7L, 8L);
        result.withOutEdgeIdentifiers(outEdgeIdentifiers);
        Assert.assertEquals(outEdgeIdentifiers, result.outEdges().stream().map(Edge::getIdentifier)
                .collect(Collectors.toCollection(TreeSet::new)));
        result.withRelationIdentifiers(source.relations().stream().map(Relation::getIdentifier)
                .collect(Collectors.toSet()));
        Assert.assertEquals(
                source.relations().stream().map(Relation::getIdentifier)
                        .collect(Collectors.toSet()),
                result.relations().stream().map(Relation::getIdentifier)
                        .collect(Collectors.toSet()));

        result.withLocation(Location.COLOSSEUM);
        // When we update the location again, the bounds recalculation should "forget" about the
        // first update
        Assert.assertEquals(Rectangle.forLocated(Location.COLOSSEUM), result.bounds());
    }

    @Test
    public void testToWkt()
    {
        final CompleteNode node1 = new CompleteNode(123L);
        node1.withLocation(Location.forString("0,0"));
        Assert.assertEquals("POINT (0 0)", node1.toWkt());

        final CompleteNode node2 = new CompleteNode(123L);
        Assert.assertNull(node2.toWkt());
    }

    @Test
    public void testWithGeometry()
    {
        final CompleteNode node = new CompleteNode(1L);
        node.withGeometry(Arrays.asList(Location.COLOSSEUM));
        Assert.assertEquals(Location.COLOSSEUM, node.getLocation());
    }
}
