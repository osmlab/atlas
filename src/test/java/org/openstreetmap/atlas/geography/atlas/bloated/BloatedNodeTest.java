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
    public void testFull()
    {
        final Atlas atlas = this.rule.getAtlas();
        final Node source = atlas.node(1);
        final BloatedNode result = BloatedNode.fromNode(source);
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
        final BloatedNode result = BloatedNode.shallowFromNode(source);
        Assert.assertEquals(source.getIdentifier(), result.getIdentifier());
        Assert.assertEquals(source.bounds(), result.bounds());
        result.withLocation(Location.CENTER);
        Assert.assertEquals(Rectangle.MINIMUM, result.bounds());
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
    }
}
