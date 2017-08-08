package org.openstreetmap.atlas.geography.atlas.multi;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.Segment;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.AtlasSize;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasTest;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test the {@link MultiAtlas}
 *
 * @author matthieun
 */
public class MultiAtlasTest
{
    private static final Logger logger = LoggerFactory.getLogger(MultiAtlasTest.class);

    private final Atlas base = new PackedAtlasTest().getAtlas();
    private Atlas other;

    private MultiAtlas multi;

    @Test
    public void connectivityTest()
    {
        this.multi.forEach(atlasItem -> logger.trace(atlasItem.toString()));

        // Out edges of CC2: 987
        Assert.assertEquals(1, this.multi.edge(6).end().outEdges().size());
        Assert.assertEquals(987,
                this.multi.edge(6).end().outEdges().iterator().next().getIdentifier());
        Assert.assertEquals(2, this.multi.edge(-9).end().outEdges().size());
        Assert.assertEquals(2, this.multi.edge(-9).end().inEdges().size());
        Assert.assertEquals(2, this.multi.edge(987).start().inEdges().size());
    }

    public Atlas getAtlas()
    {
        if (this.multi == null)
        {
            setup();
        }
        return this.multi;
    }

    @Before
    public void setup()
    {
        final PackedAtlasBuilder builder = new PackedAtlasBuilder()
                .withSizeEstimates(new AtlasSize(2, 3, 0, 0, 0, 1));
        final Map<String, String> edge5Tags = new HashMap<>();
        edge5Tags.put("highway", "primary");
        edge5Tags.put("name", "edge5");
        edge5Tags.put("surface", "concrete");
        edge5Tags.put("lanes", "3");

        final Map<String, String> edge6Tags = new HashMap<>();
        edge6Tags.put("highway", "secondary");
        edge6Tags.put("name", "edge98");
        edge6Tags.put("bridge", "cantilever");
        edge6Tags.put("maxspeed", "100");

        final Map<String, String> nodeTags = new HashMap<>();
        nodeTags.put("highway", "traffic_signal");
        // shared
        builder.addNode(123, Location.TEST_6, nodeTags);
        // shared
        builder.addNode(12345, Location.TEST_2, nodeTags);
        // private
        builder.addNode(4, Location.TEST_1, nodeTags);
        builder.addEdge(5, new Segment(Location.TEST_6, Location.TEST_1), edge5Tags);
        builder.addEdge(6, new Segment(Location.TEST_1, Location.TEST_2), edge6Tags);

        // Relation structure and tags
        // This one is already in the base atlas
        final RelationBean structure1 = new RelationBean();
        // structure1.addItem(null, "in", ItemType.EDGE);
        // structure1.addItem(null, "node", ItemType.NODE);
        // structure1.addItem(null, "out", ItemType.EDGE);
        structure1.addItem(4L, "notThere", ItemType.NODE);
        final RelationBean structure3 = new RelationBean();
        structure3.addItem(5L, "in", ItemType.EDGE);
        structure3.addItem(12345L, "node", ItemType.NODE);
        structure3.addItem(6L, "out", ItemType.EDGE);
        // Add relations
        builder.addRelation(1, 1, structure1, this.base.relation(1L).getTags());
        builder.addRelation(3, 2, structure3, this.base.relation(2L).getTags());

        this.other = builder.get();

        this.multi = new MultiAtlas(this.base, this.other);
    }

    @Test
    public void spatialIndexTest()
    {
        final Rectangle ac2Box = Location.TEST_1.boxAround(Distance.ONE_METER);
        Assert.assertEquals(1, Iterables.size(this.multi.nodesWithin(ac2Box)));
        Assert.assertEquals(4L, this.multi.nodesWithin(ac2Box).iterator().next().getIdentifier());
        Assert.assertEquals(2, Iterables.size(this.multi.edgesIntersecting(ac2Box)));
        final Iterator<Edge> edgeIterator = this.multi.edgesIntersecting(ac2Box).iterator();
        Assert.assertEquals(6, edgeIterator.next().getIdentifier());
        Assert.assertEquals(5, edgeIterator.next().getIdentifier());
        Assert.assertFalse(edgeIterator.hasNext());
    }

    @Test
    public void testSlicedRelation()
    {
        final Relation relation1 = this.multi.relation(1L);
        final RelationMemberList members = relation1.members();
        Assert.assertEquals(4, members.size());
        for (int i = 0; i < members.size(); i++)
        {
            Assert.assertTrue(members.get(i) != null);
        }
        // Members are ordered by entity type and ascending member identifier
        Assert.assertEquals(4, members.get(0).getEntity().getIdentifier());
        Assert.assertEquals(1234, members.get(1).getEntity().getIdentifier());
        Assert.assertEquals(-9, members.get(2).getEntity().getIdentifier());
        Assert.assertEquals(9, members.get(3).getEntity().getIdentifier());

        final Relation relation2 = this.multi.relation(2L);
        final RelationMemberList allMembers2 = relation2.allKnownOsmMembers();
        final Relation relation3 = this.multi.relation(3L);
        final RelationMemberList allMembers3 = relation3.allKnownOsmMembers();
        Assert.assertEquals(8, allMembers2.size());
        Assert.assertEquals(8, allMembers3.size());
    }

    @Test
    public void totalTest()
    {
        final Iterator<Edge> edges = this.multi.edges().iterator();
        int numberEdges = 0;
        while (edges.hasNext())
        {
            numberEdges++;
            edges.next();
        }
        Assert.assertEquals(6, numberEdges);
        // Assert.assertEquals(6, Iterables.size(this.multi.edges()));
        // Assert.assertEquals(4, Iterables.size(this.multi.nodes()));
        Assert.assertEquals(6, Iterables.size(this.multi.edges()));
        Assert.assertEquals(4, Iterables.size(this.multi.nodes()));
        Assert.assertEquals(3, Iterables.size(this.multi.relations()));
    }
}
