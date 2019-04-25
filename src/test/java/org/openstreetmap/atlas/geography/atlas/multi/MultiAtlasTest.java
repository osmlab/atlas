package org.openstreetmap.atlas.geography.atlas.multi;

import java.util.Iterator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas.AtlasSerializationFormat;
import org.openstreetmap.atlas.streaming.resource.ByteArrayResource;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
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
    @Rule
    public MultiAtlasTestRule setup = new MultiAtlasTestRule();

    private Atlas base;

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
        this.other = this.setup.getAtlas1();
        this.base = this.setup.getAtlas2();
        this.multi = new MultiAtlas(this.base, this.other);
    }

    @Test
    public void spatialIndexTest()
    {
        final Rectangle ac2Box = Location.TEST_1.boxAround(Distance.ONE_METER);
        Assert.assertEquals(1, Iterables.size(this.multi.nodesWithin(ac2Box)));
        Assert.assertEquals(4, this.multi.nodesWithin(ac2Box).iterator().next().getIdentifier());
        Assert.assertEquals(2, Iterables.size(this.multi.edgesIntersecting(ac2Box)));
        final Iterator<Edge> edgeIterator = this.multi.edgesIntersecting(ac2Box).iterator();
        Assert.assertEquals(6, edgeIterator.next().getIdentifier());
        Assert.assertEquals(5, edgeIterator.next().getIdentifier());
        Assert.assertFalse(edgeIterator.hasNext());
    }

    @Test
    public void testFilter()
    {
        final WritableResource baseResource = new ByteArrayResource();
        final WritableResource otherResource = new ByteArrayResource();
        final PackedAtlas packedBase = this.base.cloneToPackedAtlas();
        packedBase.setSaveSerializationFormat(AtlasSerializationFormat.JAVA);
        packedBase.save(baseResource);
        final PackedAtlas otherBase = this.other.cloneToPackedAtlas();
        otherBase.setSaveSerializationFormat(AtlasSerializationFormat.JAVA);
        otherBase.save(otherResource);

        // filter out all resources from one atlas, make sure load still works
        final Atlas multiFiltered = MultiAtlas
                .loadFromPackedAtlas(Iterables.from(baseResource, otherResource), false, entity ->
                {
                    final boolean test = entity.getIdentifier() != 123L
                            && entity.getIdentifier() != 12345L && entity.getIdentifier() != 4L
                            && entity.getIdentifier() != 5L && entity.getIdentifier() != 6L
                            && entity.getIdentifier() != 1L && entity.getIdentifier() != 3L;
                    return test;
                });

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
