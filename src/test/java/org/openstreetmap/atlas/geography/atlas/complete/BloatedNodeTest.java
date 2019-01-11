package org.openstreetmap.atlas.geography.atlas.bloated;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
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
public class BloatedNodeTest
{
    @Rule
    public BloatedTestRule rule = new BloatedTestRule();

    @Test
    public void testBloatedEquals()
    {
        final BloatedNode node11 = new BloatedNode(123L, null, null, null, null, null);
        final BloatedNode node12 = new BloatedNode(123L, null, null, null, null, null);
        final BloatedNode node21 = new BloatedNode(123L, Location.COLOSSEUM, null, null, null,
                null);
        final BloatedNode node22 = new BloatedNode(123L, Location.COLOSSEUM, null, null, null,
                null);
        final BloatedNode node23 = new BloatedNode(123L, Location.EIFFEL_TOWER, null, null, null,
                null);
        final BloatedNode node31 = new BloatedNode(123L, null, Maps.hashMap("key", "value"), null,
                null, null);
        final BloatedNode node32 = new BloatedNode(123L, null, Maps.hashMap("key", "value"), null,
                null, null);
        final BloatedNode node33 = new BloatedNode(123L, null, Maps.hashMap(), null, null, null);
        final BloatedNode node41 = new BloatedNode(123L, null, null, null, null,
                Sets.hashSet(1L, 2L));
        final BloatedNode node42 = new BloatedNode(123L, null, null, null, null,
                Sets.hashSet(1L, 2L));
        final BloatedNode node43 = new BloatedNode(123L, null, null, null, null, Sets.hashSet(1L));
        final BloatedNode node51 = new BloatedNode(123L, null, null, Sets.treeSet(1L, 2L), null,
                null);
        final BloatedNode node52 = new BloatedNode(123L, null, null, Sets.treeSet(1L, 2L), null,
                null);
        final BloatedNode node53 = new BloatedNode(123L, null, null, Sets.treeSet(1L), null, null);
        final BloatedNode node61 = new BloatedNode(123L, null, null, null, Sets.treeSet(1L, 2L),
                null);
        final BloatedNode node62 = new BloatedNode(123L, null, null, null, Sets.treeSet(1L, 2L),
                null);
        final BloatedNode node63 = new BloatedNode(123L, null, null, null, Sets.treeSet(1L), null);

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
        final BloatedNode result = BloatedNode.from(source);
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
    }

    @Test
    public void testShallow()
    {
        final Atlas atlas = this.rule.getAtlas();
        final Node source = atlas.node(1);
        final BloatedNode result = BloatedNode.shallowFrom(source);
        Assert.assertEquals(source.getIdentifier(), result.getIdentifier());
        Assert.assertEquals(source.bounds(), result.bounds());
        result.withLocation(Location.CENTER);
        // When we update a location, the bounds should expand to include the original location as
        // well as the updated location
        Assert.assertEquals(Rectangle.forLocated(source.bounds(), Location.CENTER),
                result.bounds());
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
        Assert.assertEquals(Rectangle.forLocated(source.bounds(), Location.COLOSSEUM),
                result.bounds());
    }
}
